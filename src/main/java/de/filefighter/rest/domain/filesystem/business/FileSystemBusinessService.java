package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.common.exceptions.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDeletedException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemNotFoundException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemsCouldNotBeUploadedException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.Group;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Log4j2
@Service
public class FileSystemBusinessService {

    private final FileSystemRepository fileSystemRepository;
    private final UserBusinessService userBusinessService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final MongoTemplate mongoTemplate;

    private static final String DELETION_FAILED_MSG = "Failed to delete FileSystemEntity with id ";

    public FileSystemBusinessService(FileSystemRepository fileSystemRepository, UserBusinessService userBusinessService, FileSystemTypeRepository fileSystemTypeRepository, MongoTemplate mongoTemplate) {
        this.fileSystemRepository = fileSystemRepository;
        this.userBusinessService = userBusinessService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<FileSystemItem> getFolderContentsByPath(String path, User authenticatedUser) {
        if (!InputSanitizerService.stringIsValid(path))
            throw new FileSystemContentsNotAccessibleException("Path was not valid.");

        String[] pathWithoutSlashes = path.split("/");

        if (!path.equals("/") && pathWithoutSlashes.length < 2)
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format.");

        if (!path.equals("/") && !"".equals(pathWithoutSlashes[0]))
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format. Use a leading backslash.");

        String pathToFind = removeTrailingBackSlashes(path).toLowerCase();

        // find the folder with matching path.
        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (null == listOfFileSystemEntities) // does return null and not a empty collection.
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || !userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ));

        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        // now only own or shared folders are left.
        ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>();
        String pathWithTrailingSlash = pathToFind.equals("/") ? pathToFind : (pathToFind + "/"); //NOSONAR

        // Well this is just O(n * m)
        for (FileSystemEntity folder : listOfFileSystemEntities) {
            ArrayList<FileSystemEntity> folderContents = (ArrayList<FileSystemEntity>) getFolderContentsOfEntityAndPermissions(folder, authenticatedUser, true, false);
            for (FileSystemEntity fileSystemEntityInFolder : folderContents) {
                fileSystemItems.add(this.createDTO(fileSystemEntityInFolder, authenticatedUser, pathWithTrailingSlash));
            }
        }
        return fileSystemItems;
    }

    public FileSystemItem getFileSystemItemInfo(long fsItemId, User authenticatedUser) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findByFileSystemId(fsItemId);
        if (null == fileSystemEntity)
            throw new FileSystemItemNotFoundException(fsItemId);

        if (!userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, authenticatedUser, InteractionType.READ))
            throw new FileSystemItemNotFoundException(fsItemId);

        return createDTO(fileSystemEntity, authenticatedUser, null);
    }

    public boolean deleteFileSystemItemById(long fsItemId, User authenticatedUser) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findByFileSystemId(fsItemId);
        if (null == fileSystemEntity)
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        if (!(userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, authenticatedUser, InteractionType.READ) && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, authenticatedUser, InteractionType.DELETE)))
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        return recursivelyDeleteFileSystemEntity(fileSystemEntity, authenticatedUser);
    }

    /**
     * Deletes the folder recursively.
     *
     * @param parentFileSystemEntity parent fileSystem entity, can be both folder and file.
     * @param authenticatedUser      user that wants to delete
     * @return true if the folder and all the folders / items were deleted.
     */
    @SuppressWarnings({"squid:S3776", "squid:S1192"})
    public boolean recursivelyDeleteFileSystemEntity(FileSystemEntity parentFileSystemEntity, User authenticatedUser) {
        boolean everythingWasDeleted = false;
        if (parentFileSystemEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(parentFileSystemEntity.getTypeId()) != FileSystemType.FOLDER) {
            // 1. Base of recursion
            // Delete File and update parentFolder.
            Long countDeleted = fileSystemRepository.deleteByFileSystemId(parentFileSystemEntity.getFileSystemId());
            if (countDeleted != 1)
                throw new FileFighterDataException(DELETION_FAILED_MSG + parentFileSystemEntity.getFileSystemId());

            // update.
            Query query = new Query().addCriteria(Criteria.where("itemIds").is(parentFileSystemEntity.getFileSystemId()));
            Update newUpdate = new Update().pull("itemIds", parentFileSystemEntity.getFileSystemId());
            mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);

            everythingWasDeleted = true;
        } else if (!parentFileSystemEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(parentFileSystemEntity.getTypeId()) == FileSystemType.FOLDER) {
            ArrayList<FileSystemEntity> foundEntities = (ArrayList<FileSystemEntity>) getFolderContentsOfEntityAndPermissions(parentFileSystemEntity, authenticatedUser, false, false);

            if (null == foundEntities || foundEntities.isEmpty()) {
                // 2. Base of recursion
                Long countDeleted = fileSystemRepository.deleteByFileSystemId(parentFileSystemEntity.getFileSystemId());
                if (countDeleted != 1)
                    throw new FileFighterDataException(DELETION_FAILED_MSG + parentFileSystemEntity.getFileSystemId());

                everythingWasDeleted = true;
            } else {
                ArrayList<FileSystemEntity> invisibleEntities = new ArrayList<>();
                List<Long> updatedItemIds = LongStream.of(parentFileSystemEntity.getItemIds()).boxed().collect(Collectors.toList());
                int deletedEntities = 0;

                for (FileSystemEntity childrenEntity : foundEntities) {
                    if (userIsAllowedToInteractWithFileSystemEntity(childrenEntity, authenticatedUser, InteractionType.READ)) {
                        if (userIsAllowedToInteractWithFileSystemEntity(childrenEntity, authenticatedUser, InteractionType.DELETE)) {

                            // Folder.
                            if (!childrenEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(childrenEntity.getTypeId()) == FileSystemType.FOLDER) {
                                // Step of recursion
                                if (recursivelyDeleteFileSystemEntity(childrenEntity, authenticatedUser)) {
                                    // there is no need to remove the child entity, because it was already deleted in the recursive function call.
                                    // if it wasn't removed (if = false) we don't remove the folder and we don't delete it.
                                    updatedItemIds.remove(childrenEntity.getFileSystemId());
                                    deletedEntities++;
                                }
                                // File
                            } else if (childrenEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(childrenEntity.getTypeId()) != FileSystemType.FOLDER) {
                                // 3. Base of recursion
                                Long countDeleted = fileSystemRepository.deleteByFileSystemId(childrenEntity.getFileSystemId());
                                if (countDeleted != 1)
                                    throw new FileFighterDataException(DELETION_FAILED_MSG + childrenEntity.getFileSystemId());

                                updatedItemIds.remove(childrenEntity.getFileSystemId());
                                deletedEntities++;
                            } else {
                                throw new FileFighterDataException("FileType was wrong. " + childrenEntity);
                            }
                        }
                        // otherwise the item stays as it is.
                    } else {
                        invisibleEntities.add(childrenEntity);
                    }
                }

                // some entities could not be deleted because he is not allowed or cannot see them.
                if (foundEntities.size() != deletedEntities) {

                    // update itemIds.
                    parentFileSystemEntity.setItemIds(updatedItemIds.stream().mapToLong(Long::longValue).toArray());

                    // save changes of parentFolder to db.
                    Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(parentFileSystemEntity.getFileSystemId()));
                    Update newUpdate = new Update().set("itemIds", parentFileSystemEntity.getItemIds());

                    // only problem appears when there are only invisible entities left.
                    boolean onlyInvisibleEntitiesAreLeftAfterRemovingDeletableEntities = !invisibleEntities.isEmpty() && (invisibleEntities.size() == updatedItemIds.size());
                    if (onlyInvisibleEntitiesAreLeftAfterRemovingDeletableEntities) {
                        // some files do not include the current user to see them. By adding up all these permissions and applying them the the parent folder
                        // we can make sure, that the views of the other users stay the same, while the current user cannot see the folder anymore.
                        // EdgeCase: that the user who requests the deletion is owner of the folder but cannot see cannot happen anymore,
                        // because he the owner has all rights on all files except ones created by runtime users.
                        parentFileSystemEntity = sumUpAllPermissionsOfFileSystemEntities(parentFileSystemEntity, invisibleEntities);

                        newUpdate.set("visibleForUserIds", parentFileSystemEntity.getVisibleForUserIds())
                                .set("visibleForGroupIds", parentFileSystemEntity.getVisibleForGroupIds())
                                .set("editableForUserIds", parentFileSystemEntity.getEditableForUserIds())
                                .set("editableForGroupIds", parentFileSystemEntity.getEditableFoGroupIds());
                    }
                    mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
                } else {
                    // No FileSystemEntities left in folder. -> can be deleted.
                    Long countDeleted = fileSystemRepository.deleteByFileSystemId(parentFileSystemEntity.getFileSystemId());
                    if (countDeleted != 1)
                        throw new FileFighterDataException(DELETION_FAILED_MSG + parentFileSystemEntity.getFileSystemId());

                    everythingWasDeleted = true;
                }
            }
        } else {
            throw new FileFighterDataException("FileType was wrong. " + parentFileSystemEntity);
        }
        return everythingWasDeleted;
    }

    public List<FileSystemItem> uploadFileSystemItems(Long rootItemId, List<FileSystemItemUpdate> fileSystemItemsToUpload, User authenticatedUser) {
        FileSystemEntity rootFileSystemEntity = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == rootFileSystemEntity)
            throw new FileSystemItemNotFoundException(rootItemId);

        // this does fail because the root folder is created by a runtime user.
        if (!userIsAllowedToInteractWithFileSystemEntity(rootFileSystemEntity, authenticatedUser, InteractionType.CHANGE))
            throw new FileSystemItemsCouldNotBeUploadedException(rootItemId);

        // do something.
        return null;
    }

    // ---------------- HELPER -------------------

    public FileSystemEntity sumUpAllPermissionsOfFileSystemEntities(FileSystemEntity parentFileSystemEntity, List<FileSystemEntity> fileSystemEntities) {
        HashSet<Long> visibleForUserIds = new HashSet<>();
        HashSet<Long> visibleForGroupIds = new HashSet<>();
        HashSet<Long> editableForUserIds = new HashSet<>();
        HashSet<Long> editableGroupIds = new HashSet<>();

        for (FileSystemEntity entity : fileSystemEntities) {
            addPermissionsToSets(visibleForUserIds, visibleForGroupIds, editableForUserIds, editableGroupIds, entity);
        }

        parentFileSystemEntity.setVisibleForUserIds(Arrays.stream(visibleForUserIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        parentFileSystemEntity.setVisibleForGroupIds(Arrays.stream(visibleForGroupIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        parentFileSystemEntity.setEditableForUserIds(Arrays.stream(editableForUserIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        parentFileSystemEntity.setEditableFoGroupIds(Arrays.stream(editableGroupIds.toArray(new Long[0])).mapToLong(Long::longValue).toArray());
        return parentFileSystemEntity;
    }

    public void addPermissionsToSets(Set<Long> visibleForUserIds, Set<Long> visibleForGroupIds, Set<Long> editableForUserIds, Set<Long> editableGroupIds, FileSystemEntity fileSystemEntity) {
        for (long i : fileSystemEntity.getVisibleForUserIds()) {
            visibleForUserIds.add(i);
        }
        for (long i : fileSystemEntity.getVisibleForGroupIds()) {
            visibleForGroupIds.add(i);
        }
        for (long i : fileSystemEntity.getEditableForUserIds()) {
            editableForUserIds.add(i);
        }
        for (long i : fileSystemEntity.getEditableFoGroupIds()) {
            editableGroupIds.add(i);
        }
    }

    public List<FileSystemEntity> getFolderContentsOfEntityAndPermissions(FileSystemEntity fileSystemEntity, User authenticatedUser, boolean needsToBeVisible, boolean needsToBeEditable) {
        long[] folderContentItemIds = fileSystemEntity.getItemIds();
        List<FileSystemEntity> fileSystemEntities = new ArrayList<>(folderContentItemIds.length);

        // check if the contents are visible / editable.
        for (long fileSystemId : folderContentItemIds) {
            FileSystemEntity fileSystemEntityInFolder = fileSystemRepository.findByFileSystemId(fileSystemId);

            if (null == fileSystemEntityInFolder)
                throw new FileFighterDataException("FolderContents expected fileSystemItem with id " + fileSystemId + " but was empty.");

            if (needsToBeVisible && !needsToBeEditable && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.READ)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (needsToBeEditable && !needsToBeVisible && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.CHANGE)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (needsToBeVisible && needsToBeEditable && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.READ) && userIsAllowedToInteractWithFileSystemEntity(fileSystemEntityInFolder, authenticatedUser, InteractionType.CHANGE)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (!needsToBeVisible && !needsToBeEditable) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
        }
        return fileSystemEntities;
    }

    @SuppressWarnings("java:S3776")
    public boolean userIsAllowedToInteractWithFileSystemEntity(FileSystemEntity fileSystemEntity, User authenticatedUser, InteractionType interaction) {
        // file was created by runtime user.
        if ((interaction == InteractionType.DELETE)
                && fileSystemEntity.getCreatedByUserId() == RestConfiguration.RUNTIME_USER_ID)
            return false;

        // user created the item
        if (fileSystemEntity.getCreatedByUserId() == authenticatedUser.getUserId())
            return true;

        // user created containing folder.
        if (null != fileSystemEntity.getOwnerIds() && Arrays.stream(fileSystemEntity.getOwnerIds()).asDoubleStream().anyMatch(id -> id == authenticatedUser.getUserId()))
            return true;

        // user got the item shared.
        if (interaction == InteractionType.READ) {
            for (long userId : fileSystemEntity.getVisibleForUserIds()) {
                if (userId == authenticatedUser.getUserId())
                    return true;
            }

            // user is in group that got the item shared.
            long[] fileIsSharedToGroups = fileSystemEntity.getVisibleForGroupIds();
            for (Group group : authenticatedUser.getGroups()) {
                for (long groupId : fileIsSharedToGroups) {
                    if (groupId == group.getGroupId())
                        return true;

                }
            }
        }
        if (interaction == InteractionType.CHANGE || interaction == InteractionType.DELETE) {
            for (long userId : fileSystemEntity.getEditableForUserIds()) {
                if (userId == authenticatedUser.getUserId())
                    return true;
            }

            // user is in group that got the item shared.
            long[] fileIsSharedToGroups = fileSystemEntity.getEditableFoGroupIds();
            for (Group group : authenticatedUser.getGroups()) {
                for (long groupId : fileIsSharedToGroups) {
                    if (groupId == group.getGroupId())
                        return true;

                }
            }
        }
        return false;
    }

    public String removeTrailingBackSlashes(String pathToFind) {
        char[] chars = pathToFind.toCharArray();
        // for the case of "/"
        if (chars.length != 1 && chars[chars.length - 1] == '/') {
            chars = Arrays.copyOf(chars, chars.length - 1);
            return new String(chars);
        }
        return pathToFind;
    }

    public FileSystemItem createDTO(FileSystemEntity fileSystemEntity, User authenticatedUser, String basePath) {
        // for better responses and internal problem handling.
        User ownerOfFileSystemItem;
        try {
            ownerOfFileSystemItem = userBusinessService.getUserById(fileSystemEntity.getCreatedByUserId());
        } catch (UserNotFoundException exception) {
            throw new FileFighterDataException("Owner of a file could not be found.");
        }

        boolean isShared = ownerOfFileSystemItem.getUserId() != authenticatedUser.getUserId();
        FileSystemType type = fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId());
        boolean isAFolder = type == FileSystemType.FOLDER && !fileSystemEntity.isFile();

        return FileSystemItem.builder()
                .createdByUser(ownerOfFileSystemItem)
                .fileSystemId(fileSystemEntity.getFileSystemId())
                .lastUpdated(fileSystemEntity.getLastUpdated())
                .name(fileSystemEntity.getName())
                .size(fileSystemEntity.getSize())
                .type(isAFolder ? FileSystemType.FOLDER : type)
                .path(null == basePath ? null : basePath + fileSystemEntity.getName())
                .isShared(isShared)
                .build();
    }

    public void createBasicFilesForNewUser(UserEntity registeredUserEntity) {
        fileSystemRepository.save(FileSystemEntity
                .builder()
                .createdByUserId(0)
                .typeId(FileSystemType.FOLDER.getId())
                .isFile(false)
                .name("HOME_" + registeredUserEntity.getUsername())
                .path("/")
                .lastUpdated(Instant.now().getEpochSecond())
                .fileSystemId(generateNextFileSystemId())
                .build());
    }

    public double getTotalFileSize() {
        ArrayList<FileSystemEntity> entities = fileSystemRepository.findByPath("/");
        if (null == entities)
            throw new FileFighterDataException("Couldn't find any Home directories!");

        double size = 0;
        for (FileSystemEntity entity : entities) {
            size += entity.getSize();
        }
        return size;
    }

    public long getFileSystemEntityCount() {
        return fileSystemRepository.count();
    }

    public long generateNextFileSystemId() {
        return getFileSystemEntityCount() + 1;
    }
}
