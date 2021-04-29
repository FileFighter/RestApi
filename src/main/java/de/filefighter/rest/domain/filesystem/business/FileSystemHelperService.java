package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
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

import static de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService.DELETION_FAILED_MSG;

@Service
@Log4j2
public class FileSystemHelperService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final UserBusinessService userBusinessService;
    private final MongoTemplate mongoTemplate;

    public FileSystemHelperService(FileSystemRepository fileSystemRepository, FileSystemTypeRepository fileSystemTypeRepository, UserBusinessService userBusinessService, MongoTemplate mongoTemplate) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.userBusinessService = userBusinessService;
        this.mongoTemplate = mongoTemplate;
    }

    public FileSystemEntity sumUpAllPermissionsOfFileSystemEntities(FileSystemEntity parentFileSystemEntity, List<FileSystemEntity> fileSystemEntities) {
        HashSet<Long> visibleForUserIds = new HashSet<>();
        HashSet<Long> visibleForGroupIds = new HashSet<>();
        HashSet<Long> editableForUserIds = new HashSet<>();
        HashSet<Long> editableGroupIds = new HashSet<>();

        for (FileSystemEntity entity : fileSystemEntities) {
            addPermissionsToSets(visibleForUserIds, visibleForGroupIds, editableForUserIds, editableGroupIds, entity);
        }

        parentFileSystemEntity.setVisibleForUserIds(this.transformLongCollectionTolongArray(visibleForUserIds));
        parentFileSystemEntity.setVisibleForGroupIds(this.transformLongCollectionTolongArray(visibleForGroupIds));
        parentFileSystemEntity.setEditableForUserIds(this.transformLongCollectionTolongArray(editableForUserIds));
        parentFileSystemEntity.setEditableFoGroupIds(this.transformLongCollectionTolongArray(editableGroupIds));
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

    public void removeVisibilityRightsOfFileSystemEntityForUser(FileSystemEntity entity, User authenticatedUser) {
        Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(entity.getFileSystemId()));
        Update newUpdate = new Update();

        // user is either directly in the visible ids or in a group that is visible.
        long[] newIdsWithoutCurrentUserId = Arrays.stream(entity.getVisibleForUserIds()).filter(userId -> userId != authenticatedUser.getUserId()).toArray();
        if (newIdsWithoutCurrentUserId.length != entity.getVisibleForUserIds().length) {
            // apply it.
            newUpdate.set("visibleForUserIds", newIdsWithoutCurrentUserId);
        }

        // or user is in a group that can see the filesystem entity.
        long[] newGroupIds = entity.getVisibleForGroupIds();
        if (newGroupIds.length != 0) {
            for (Group group : authenticatedUser.getGroups()) {
                newGroupIds = Arrays.stream(newGroupIds).filter(id -> id != group.getGroupId()).toArray();
            }
            newUpdate.set("visibleForGroupIds", newGroupIds);
        }
        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
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
                && fileSystemEntity.getLastUpdatedBy() == RestConfiguration.RUNTIME_USER_ID)
            return false;

        // user own the file.
        if (fileSystemEntity.getOwnerId() == authenticatedUser.getUserId())
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

    public FileSystemItem createDTO(FileSystemEntity fileSystemEntity, User authenticatedUser, String absolutePathWithUsername) {
        // for better responses and internal problem handling.
        User ownerOfFileSystemItem;
        User lastUpdatedByUser;
        try {
            ownerOfFileSystemItem = userBusinessService.getUserById(fileSystemEntity.getOwnerId());
            lastUpdatedByUser = userBusinessService.getUserById(fileSystemEntity.getLastUpdatedBy());
        } catch (UserNotFoundException exception) {
            log.debug("Found UserNotFoundException in createDTO. Entity: {}.", fileSystemEntity);
            throw new FileFighterDataException("Owner or auther of last change could not be found.");
        }

        boolean isShared = ownerOfFileSystemItem.getUserId() != RestConfiguration.RUNTIME_USER_ID
                && ownerOfFileSystemItem.getUserId() != authenticatedUser.getUserId();
        FileSystemType type = fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId());
        boolean isAFolder = type == FileSystemType.FOLDER && !fileSystemEntity.isFile();
        String entityName = fileSystemEntity.getName();

        if (absolutePathWithUsername != null) {
            if (absolutePathWithUsername.equals("/")) {
                absolutePathWithUsername = absolutePathWithUsername + ownerOfFileSystemItem.getUsername(); // this is only for the case of the path = "/"
                entityName = ownerOfFileSystemItem.getUsername();
            } else {
                absolutePathWithUsername = this.removeTrailingBackSlashes(absolutePathWithUsername) + "/" + fileSystemEntity.getName();
            }
        }

        return FileSystemItem.builder()
                .lastUpdatedBy(lastUpdatedByUser)
                .fileSystemId(fileSystemEntity.getFileSystemId())
                .owner(ownerOfFileSystemItem)
                .lastUpdated(fileSystemEntity.getLastUpdated())
                .name(entityName)
                .size(fileSystemEntity.getSize())
                .type(isAFolder ? FileSystemType.FOLDER : type)
                .path(absolutePathWithUsername)
                .isShared(isShared)
                .mimeType(fileSystemEntity.getMimeType())
                .build();
    }

    public void createBasicFilesForNewUser(UserEntity registeredUserEntity) {
        fileSystemRepository.save(FileSystemEntity
                .builder()
                .fileSystemId(generateNextFileSystemId())
                .ownerId(registeredUserEntity.getUserId())
                .lastUpdatedBy(RestConfiguration.RUNTIME_USER_ID)
                .lastUpdated(getCurrentTimeStamp())
                .typeId(FileSystemType.FOLDER.getId())
                .isFile(false)
                .name("HOME_" + registeredUserEntity.getUserId())
                .path("/")
                .lastUpdated(Instant.now().getEpochSecond())
                .size(0)
                .mimeType(null)
                .build());
    }

    public void deleteAndUnbindFileSystemEntity(FileSystemEntity fileSystemEntity) {
        Long countDeleted = fileSystemRepository.deleteByFileSystemId(fileSystemEntity.getFileSystemId());
        if (countDeleted != 1)
            throw new FileFighterDataException(DELETION_FAILED_MSG + fileSystemEntity.getFileSystemId());

        Query query = new Query().addCriteria(Criteria.where("itemIds").is(fileSystemEntity.getFileSystemId()));
        Update newUpdate;

        // TODO: fix this sizing.
        // only reduce size if the entity is a file.
        if (fileSystemEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId()) != FileSystemType.FOLDER) {
            newUpdate = new Update().pull("itemIds", fileSystemEntity.getFileSystemId()).inc("size", fileSystemEntity.getSize() * -1); // hacky stuff.
        } else {
            newUpdate = new Update().pull("itemIds", fileSystemEntity.getFileSystemId());
        }
        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    public void recursivlyUpdateTimeStamps(FileSystemEntity currentEntity, User autheticatedUser, long currentTimeStamp) {
        Query query = new Query().addCriteria(Criteria.where("fileSystemId").is(currentEntity.getFileSystemId()));
        Update update = new Update().set("lastUpdated", currentTimeStamp).set("lastUpdatedBy", autheticatedUser.getUserId());
        mongoTemplate.findAndModify(query, update, FileSystemEntity.class);

        Query queryParentEntity = new Query().addCriteria(Criteria.where("itemIds").is(currentEntity.getFileSystemId()));
        List<FileSystemEntity> parentFileSystemEntities = mongoTemplate.find(queryParentEntity, FileSystemEntity.class);

        if (parentFileSystemEntities.isEmpty()) {
            // no parents found -> either root folder or an exception
            boolean isFile = currentEntity.isFile() && currentEntity.getTypeId() != FileSystemType.FOLDER.getId();
            boolean isRootFolder = !isFile && currentEntity.getPath().equals("/");
            if (!isRootFolder) {
                log.debug("Found no parent entity for a non root entity: " + currentEntity);
                throw new FileFighterDataException("Found no parent entity for a non root entity.");
            }
            // else return.
        } else {
            if (parentFileSystemEntities.size() > 1) {
                log.debug("Found more than one parent entity for entity: " + currentEntity);
                throw new FileFighterDataException("Found more than one parent entity for entity.");
            }

            recursivlyUpdateTimeStamps(parentFileSystemEntities.get(0), autheticatedUser, currentTimeStamp);
        }
    }

    public String[] splitPathIntoEnitityPaths(String path, String basePath) {
        Object[] paths = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toArray();
        String[] returnString = new String[paths.length];

        StringBuilder pathStringBuilder = new StringBuilder(basePath);
        for (int i = 0; i < paths.length; i++) {
            if (pathStringBuilder.toString().charAt(pathStringBuilder.toString().length() - 1) != '/') {
                pathStringBuilder.append("/");
            }
            pathStringBuilder.append(paths[i]);
            returnString[i] = pathStringBuilder.toString();
        }
        return returnString;
    }

    public String getEntityNameFromPath(String path) {
        String[] splittedPath = path.split("/");
        try {
            return splittedPath[splittedPath.length - 1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            log.debug("path was {}.", path);
            throw new FileFighterDataException("Path to check was not valid");
        }
    }

    public String getParentPathFromPath(String path) {
        String entityName = getEntityNameFromPath(path);
        String parent = path.substring(0, path.length() - entityName.length() - 1);
        return parent.equals("") ? "/" : parent;
    }

    public double getTotalFileSize() {
        List<FileSystemEntity> entities = fileSystemRepository.findByPath("/");
        if (null == entities)
            throw new FileFighterDataException("Couldn't find any Home directories!");

        double size = 0;
        for (FileSystemEntity entity : entities) {
            size += entity.getSize();
        }
        return size;
    }

    public Long[] transformlongArrayToLong(long[] arrayToTransform) {
        Long[] longArgument = new Long[arrayToTransform.length];
        int i = 0;

        for (long temp : arrayToTransform) {
            longArgument[i++] = temp;
        }
        return longArgument;
    }

    public long[] transformLongCollectionTolongArray(Collection<Long> collectionToTransform) {
        return Arrays.stream(collectionToTransform.toArray(new Long[0])).mapToLong(Long::longValue).toArray();
    }

    public long getFileSystemEntityCount() {
        return fileSystemRepository.count();
    }

    // This will update the field. -> Everytime this function gets called a id gets taken. Which means some ids could be lost, when calling this function and not creating something.
    public long generateNextFileSystemId() {
        return getFileSystemEntityCount() + 1;
    }

    public long getCurrentTimeStamp() {
        return Instant.now().getEpochSecond();
    }
}
