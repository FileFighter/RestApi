package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDeletedException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemNotFoundException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Log4j2
@Service
public class FileSystemBusinessService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final UserBusinessService userBusinessService;
    private final MongoTemplate mongoTemplate;

    private static final String DELETION_FAILED_MSG = "Failed to delete FileSystemEntity with id ";

    public FileSystemBusinessService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, FileSystemTypeRepository fileSystemTypeRepository, UserBusinessService userBusinessService, MongoTemplate mongoTemplate) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.userBusinessService = userBusinessService;
        this.mongoTemplate = mongoTemplate;
    }

    public List<FileSystemItem> getFolderContentsByPath(String path, User authenticatedUser) {
        String[] pathWithoutSlashes = path.split("/");

        String pathToFind;
        User ownerOfRequestedFolder = null;

        if (path.equals("/")) {
            pathToFind = "/";
        } else {
            if (pathWithoutSlashes.length < 2)
                throw new FileSystemContentsNotAccessibleException("Path was in wrong format.");

            if (!"".equals(pathWithoutSlashes[0]))
                throw new FileSystemContentsNotAccessibleException("Path was in wrong format. Use a leading backslash.");

            // the first path must be the the username.
            try {
                ownerOfRequestedFolder = userBusinessService.findUserByUsername(pathWithoutSlashes[1]);
                String[] fileSystemPath = path.split(ownerOfRequestedFolder.getUsername());
                if (fileSystemPath.length == 1) {
                    if (!fileSystemPath[0].equals("/"))
                        throw new FileSystemContentsNotAccessibleException();

                    pathToFind = "/";
                } else {
                    pathToFind = fileSystemPath[1];
                }
            } catch (UserNotFoundException exception) {
                throw new FileSystemContentsNotAccessibleException();
            }
        }

        pathToFind = fileSystemHelperService.removeTrailingBackSlashes(pathToFind).toLowerCase();

        // find the folder with matching path.
        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (null == listOfFileSystemEntities) // does return null and not a empty collection.
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        // this is only the case if the real / was requested. -> filter by visibility
        if (null == ownerOfRequestedFolder) {
            listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ));
            // do not get the actual contents here but display the folder names as a fake directory.

            ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>();
            for (FileSystemEntity folder : listOfFileSystemEntities) {
                // change names here accordingly.
                fileSystemItems.add(fileSystemHelperService.createDTO(folder, authenticatedUser, "/"));
            }

            return fileSystemItems;
        } else {
            User finalOwnerOfRequestedFolder = ownerOfRequestedFolder;
            listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || entity.getOwnerId() != finalOwnerOfRequestedFolder.getUserId());

            if (listOfFileSystemEntities.isEmpty())
                throw new FileSystemContentsNotAccessibleException();

            // now one Folder should remain
            if (listOfFileSystemEntities.size() != 1)
                throw new FileFighterDataException("Found more than one folder with the path " + pathToFind);

            ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>();
            ArrayList<FileSystemEntity> folderContents =
                    (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(listOfFileSystemEntities.get(0), authenticatedUser, false, false);

            for (FileSystemEntity fileSystemEntityInFolder : folderContents) {
                fileSystemItems.add(fileSystemHelperService.createDTO(fileSystemEntityInFolder, authenticatedUser, "/" + ownerOfRequestedFolder.getUsername() + pathToFind));
            }

            return fileSystemItems;
        }
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
}
