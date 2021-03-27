package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.common.exceptions.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDeletedException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemNotFoundException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemsCouldNotBeUploadedException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.data.dto.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Log4j2
@Service
public class FileSystemBusinessService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final MongoTemplate mongoTemplate;

    private static final String DELETION_FAILED_MSG = "Failed to delete FileSystemEntity with id ";

    public FileSystemBusinessService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, FileSystemTypeRepository fileSystemTypeRepository, MongoTemplate mongoTemplate) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
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

        String pathToFind = fileSystemHelperService.removeTrailingBackSlashes(path).toLowerCase();

        // find the folder with matching path.
        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (null == listOfFileSystemEntities) // does return null and not a empty collection.
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ));

        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        // now only own or shared folders are left.
        ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>();
        String pathWithTrailingSlash = pathToFind.equals("/") ? pathToFind : (pathToFind + "/"); //NOSONAR

        // Well this is just O(n * m)
        for (FileSystemEntity folder : listOfFileSystemEntities) {
            ArrayList<FileSystemEntity> folderContents = (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(folder, authenticatedUser, true, false);
            for (FileSystemEntity fileSystemEntityInFolder : folderContents) {
                fileSystemItems.add(fileSystemHelperService.createDTO(fileSystemEntityInFolder, authenticatedUser, pathWithTrailingSlash));
            }
        }
        return fileSystemItems;
    }

    public FileSystemItem getFileSystemItemInfo(long fsItemId, User authenticatedUser) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findByFileSystemId(fsItemId);
        if (null == fileSystemEntity)
            throw new FileSystemItemNotFoundException(fsItemId);

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, authenticatedUser, InteractionType.READ))
            throw new FileSystemItemNotFoundException(fsItemId);

        return fileSystemHelperService.createDTO(fileSystemEntity, authenticatedUser, null);
    }

    public boolean deleteFileSystemItemById(long fsItemId, User authenticatedUser) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findByFileSystemId(fsItemId);
        if (null == fileSystemEntity)
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        if (!(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, authenticatedUser, InteractionType.READ) && fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, authenticatedUser, InteractionType.DELETE)))
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
            if (countDeleted != 1) // TODO: check this number again.
                throw new FileFighterDataException(DELETION_FAILED_MSG + parentFileSystemEntity.getFileSystemId());

            // update.
            Query query = new Query().addCriteria(Criteria.where("itemIds").is(parentFileSystemEntity.getFileSystemId()));
            Update newUpdate = new Update().pull("itemIds", parentFileSystemEntity.getFileSystemId());
            mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);

            everythingWasDeleted = true;
        } else if (!parentFileSystemEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(parentFileSystemEntity.getTypeId()) == FileSystemType.FOLDER) {
            List<FileSystemEntity> foundList = fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parentFileSystemEntity, authenticatedUser, false, false);

            if (null == foundList || foundList.isEmpty()) {
                // 2. Base of recursion
                Long countDeleted = fileSystemRepository.deleteByFileSystemId(parentFileSystemEntity.getFileSystemId());
                if (countDeleted != 1)
                    throw new FileFighterDataException(DELETION_FAILED_MSG + parentFileSystemEntity.getFileSystemId());

                everythingWasDeleted = true;
            } else {
                ArrayList<FileSystemEntity> foundEntities = (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parentFileSystemEntity, authenticatedUser, false, false);
                ArrayList<FileSystemEntity> invisibleEntities = new ArrayList<>();
                List<Long> updatedItemIds = LongStream.of(parentFileSystemEntity.getItemIds()).boxed().collect(Collectors.toList());
                int deletedEntities = 0;

                for (FileSystemEntity childrenEntity : foundEntities) {
                    if (fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(childrenEntity, authenticatedUser, InteractionType.READ)) {
                        if (fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(childrenEntity, authenticatedUser, InteractionType.DELETE)) {

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
                        parentFileSystemEntity = fileSystemHelperService.sumUpAllPermissionsOfFileSystemEntities(parentFileSystemEntity, invisibleEntities);

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

    public FileSystemItem uploadFileSystemItem(long rootItemId, FileSystemUpload fileSystemUpload, User authenticatedUser) {
        FileSystemEntity parentFileSystemEntity = fileSystemRepository.findByFileSystemId(rootItemId);
        if (null == parentFileSystemEntity)
            throw new FileSystemItemNotFoundException(rootItemId);

        if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parentFileSystemEntity, authenticatedUser, InteractionType.CHANGE))
            throw new FileSystemItemsCouldNotBeUploadedException(rootItemId);

        String[] relativePaths = fileSystemUpload.getPath().split("/");
        List<String> paths = Arrays.stream(relativePaths).filter(path -> !(path.isEmpty() || path.isBlank())).collect(Collectors.toList());

        // variables
        int index = 0;
        long nextId = fileSystemHelperService.generateNextFileSystemId();
        long timeStamp = fileSystemHelperService.getCurrentTimeStamp();
        String pathToCheck = parentFileSystemEntity.getPath();
        String parentPath = null;
        long newFolderId = 0;
        boolean createdNewFolder = false;

        // add trailing backslash
        if (!pathToCheck.equals("/")) {
            pathToCheck += "/";
        }

        // owner list
        long[] newOwnerIds;
        long[] oldOwnerIds = parentFileSystemEntity.getOwnerIds();

        OptionalLong optionalUserId = Arrays.stream(oldOwnerIds).filter(id -> id == authenticatedUser.getUserId()).findAny();
        if (optionalUserId.isEmpty()) {
            newOwnerIds = new long[oldOwnerIds.length + 1];
            System.arraycopy(oldOwnerIds, 0, newOwnerIds, 0, oldOwnerIds.length);
            newOwnerIds[oldOwnerIds.length] = authenticatedUser.getUserId();
        } else {
            newOwnerIds = oldOwnerIds;
        }

        for (String path : paths) {
            if (index == paths.size() - 1) {
                // check if a file already exists with the same name.
                // check for children with the name to be uploaded
                // only necessary when no new folder was created.
                if (!createdNewFolder || index == 0) {
                    List<FileSystemEntity> parentEntityForUploadedFile = fileSystemRepository.findByPath(parentFileSystemEntity.getPath());
                    if (parentEntityForUploadedFile.size() != 1)
                        throw new FileFighterDataException("Didn't find parentFolder for upload.");

                    List<FileSystemEntity> childrenOfTheParentEntity = fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parentEntityForUploadedFile.get(0), authenticatedUser, false, false);
                    Optional<FileSystemEntity> optionalEntityWithTheSameName = childrenOfTheParentEntity.stream().filter(children -> children.getName().equals(fileSystemUpload.getName())).findAny();
                    if (optionalEntityWithTheSameName.isPresent())
                        throw new FileSystemItemsCouldNotBeUploadedException("A file with the same name already exists.");
                }

                FileSystemEntity newFile = FileSystemEntity.builder()
                        .fileSystemId(nextId)
                        .name(fileSystemUpload.getName())
                        .path(null)
                        .typeId(fileSystemTypeRepository.parseMimeType(fileSystemUpload.getMimeType()).getId())
                        .size(fileSystemUpload.getSize())
                        .lastUpdated(timeStamp)
                        .isFile(true)
                        .createdByUserId(authenticatedUser.getUserId())
                        .ownerIds(newOwnerIds)
                        .visibleForUserIds(parentFileSystemEntity.getVisibleForUserIds())
                        .visibleForGroupIds(parentFileSystemEntity.getVisibleForGroupIds())
                        .editableForUserIds(parentFileSystemEntity.getEditableForUserIds())
                        .editableFoGroupIds(parentFileSystemEntity.getEditableFoGroupIds())
                        .build();

                // set flags for parent
                if (parentPath == null) {
                    parentPath = parentFileSystemEntity.getPath();
                    newFolderId = nextId;
                }
                log.info("Creating new file {} for user {}.", newFile, authenticatedUser.getUserId());
                fileSystemRepository.save(newFile);
            } else {
                // add current element
                String possibleParentPath = pathToCheck;
                pathToCheck = fileSystemHelperService.removeTrailingBackSlashes(pathToCheck);
                pathToCheck += "/" + path;

                // check for existing folder with that path
                // TODO: check for visibility
                ArrayList<FileSystemEntity> foundExistingFolder = fileSystemRepository.findByPath(pathToCheck);
                if (foundExistingFolder.isEmpty()) {
                    // create it.
                    FileSystemEntity newFolder = FileSystemEntity.builder()
                            .fileSystemId(nextId)
                            .name(path)
                            .path(pathToCheck)
                            .typeId(FileSystemType.FOLDER.getId())
                            .size(fileSystemUpload.getSize())
                            .lastUpdated(timeStamp)
                            .isFile(false)
                            .createdByUserId(authenticatedUser.getUserId())
                            .ownerIds(newOwnerIds)
                            .visibleForUserIds(parentFileSystemEntity.getVisibleForUserIds())
                            .visibleForGroupIds(parentFileSystemEntity.getVisibleForGroupIds())
                            .editableForUserIds(parentFileSystemEntity.getEditableForUserIds())
                            .editableFoGroupIds(parentFileSystemEntity.getEditableFoGroupIds())
                            .build();

                    // set flags for parent
                    if (parentPath == null) {
                        parentPath = possibleParentPath;
                        newFolderId = nextId;
                    }

                    nextId = fileSystemHelperService.generateNextFileSystemId() + 1; // this is necessary because the file is not created, thus the count of files will not change -> would lead to the same number.
                    newFolder.setItemIds(new long[]{nextId});
                    log.info("Creating new folder {} for user {}.", newFolder, authenticatedUser.getUserId());
                    fileSystemRepository.save(newFolder);
                    createdNewFolder = true;
                } else {
                    if (foundExistingFolder.size() != 1)
                        throw new FileFighterDataException("Found more than one folder with the path " + pathToCheck);

                    // update it
                }
            }
            index++;
        }

        // update parent folder when a new folder was created.
        if (parentPath != null) {
            Query query = new Query().addCriteria(Criteria.where("path").is(fileSystemHelperService.removeTrailingBackSlashes(parentPath)));
            Update newUpdate = new Update().push("itemIds", newFolderId);
            mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
        }
/*


        // save files
        // connect folders
        // owner ids
        // content ids

 */
        return null;
    }
}
