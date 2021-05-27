package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.Pair;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDeletedException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDownloadedException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemNotFoundException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
public class FileSystemBusinessService {

    public static final String DELETION_FAILED_MSG = "Failed to delete FileSystemEntity with id ";
    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final UserBusinessService userBusinessService;

    public FileSystemBusinessService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, FileSystemTypeRepository fileSystemTypeRepository, UserBusinessService userBusinessService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.userBusinessService = userBusinessService;
    }

    @SuppressWarnings("java:S3776")
    public Pair<List<FileSystemItem>, Long> getFolderContentsByPath(String path, User authenticatedUser) {
        String[] pathWithoutSlashes = path.split("/");

        String pathToFind;
        User ownerOfRequestedFolder = null;

        // make path case insensitive
        path = path.toLowerCase();

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
                String[] fileSystemPath = path.split(ownerOfRequestedFolder.getUsername().toLowerCase());
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
        List<FileSystemEntity> listOfPossibleDirectories = fileSystemRepository.findByPath(pathToFind);
        if (null == listOfPossibleDirectories) // does return null and not a empty collection.
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        // this is only the case if the real / was requested. -> filter by visibility
        boolean actualRootWasRequested = null == ownerOfRequestedFolder;
        if (actualRootWasRequested) {
            listOfPossibleDirectories.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || !fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ));

            // do not get the actual contents here but display the folder names as a fake directory.
            ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>();
            for (FileSystemEntity folder : listOfPossibleDirectories) {
                // change names here accordingly.
                fileSystemItems.add(fileSystemHelperService.createDTO(folder, authenticatedUser, "/"));
            }

            return new Pair<>(fileSystemItems, -1L);
        } else {
            User finalOwnerOfRequestedFolder = ownerOfRequestedFolder;
            listOfPossibleDirectories.removeIf(entity -> (entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || entity.getOwnerId() != finalOwnerOfRequestedFolder.getUserId()));

            if (listOfPossibleDirectories.isEmpty())
                throw new FileSystemContentsNotAccessibleException();

            // now one Folder should remain
            if (listOfPossibleDirectories.size() != 1)
                throw new FileFighterDataException("Found more than one folder with the path " + pathToFind);

            // check if the autheticatedUser can access this.
            FileSystemEntity parentFolder = listOfPossibleDirectories.get(0);
            if (!fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parentFolder, authenticatedUser, InteractionType.READ))
                throw new FileSystemContentsNotAccessibleException();

            ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>();
            List<FileSystemEntity> folderContents =
                    fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parentFolder, authenticatedUser, true, false);

            for (FileSystemEntity fileSystemEntityInFolder : folderContents) {
                String absolutePathToEntity = "/" + ownerOfRequestedFolder.getUsername() + pathToFind;
                if (!pathToFind.equals("/")) {
                    absolutePathToEntity = absolutePathToEntity + "/";
                }
                absolutePathToEntity = absolutePathToEntity + fileSystemEntityInFolder.getName();
                fileSystemItems.add(fileSystemHelperService.createDTO(fileSystemEntityInFolder, authenticatedUser, absolutePathToEntity));
            }

            return new Pair<>(fileSystemItems, parentFolder.getFileSystemId());
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

    public List<FileSystemItem> deleteFileSystemItemById(long fsItemId, User authenticatedUser) {
        FileSystemEntity parentEntity = fileSystemRepository.findByFileSystemId(fsItemId);
        if (null == parentEntity)
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        if (!(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parentEntity, authenticatedUser, InteractionType.READ) && fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(parentEntity, authenticatedUser, InteractionType.DELETE)))
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        // update the time stamps in the file tree
        fileSystemHelperService.recursivlyUpdateTimeStamps(parentEntity, authenticatedUser, fileSystemHelperService.getCurrentTimeStamp());

        log.info("User is {}.", authenticatedUser);

        ArrayList<FileSystemItem> returnList = new ArrayList<>();
        recursivlyDeleteFileSystemEntity(parentEntity, authenticatedUser, returnList);
        return returnList;
    }

    private Pair<Boolean, Boolean> recursivlyDeleteFileSystemEntity(FileSystemEntity parentEntity, User authenticatedUser, ArrayList<FileSystemItem> returnList) {
        boolean foundNonDeletable = false;
        boolean foundInvisible = false;

        // the parentEntity is already checked.
        if (parentEntity.isFile() && fileSystemTypeRepository.findFileSystemTypeById(parentEntity.getTypeId()) != FileSystemType.FOLDER) {
            log.debug("Found file to delete: {}.", parentEntity);
            fileSystemHelperService.deleteAndUnbindFileSystemEntity(parentEntity);
            returnList.add(fileSystemHelperService.createDTO(parentEntity, authenticatedUser, null));
        } else {
            if (parentEntity.getItemIds().length != 0) {
                List<FileSystemEntity> items = fileSystemHelperService.getFolderContentsOfEntityAndPermissions(parentEntity, authenticatedUser, false, false);
                for (FileSystemEntity item : items) {
                    if (fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(item, authenticatedUser, InteractionType.READ)) {
                        if (fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(item, authenticatedUser, InteractionType.DELETE)) {
                            Pair<Boolean, Boolean> recursiveReturn = recursivlyDeleteFileSystemEntity(item, authenticatedUser, returnList);
                            foundInvisible = recursiveReturn.getFirst() || foundInvisible;
                            foundNonDeletable = recursiveReturn.getSecond() || foundNonDeletable;
                        } else {
                            // a entity could not be removed disable the deletion of the parent folder. (current Entity)
                            foundNonDeletable = true;
                        }
                    } else {
                        // the entity also cannot be deleted BUT the current User looses the permissions.
                        foundInvisible = true;
                    }
                }

                log.info("Currently working on: {}.", parentEntity);

                if (foundInvisible && !foundNonDeletable) {
                    // only invisible files left.
                    log.info("Found invisible FileSystemEntity {}", parentEntity);
                    fileSystemHelperService.removeVisibilityRightsOfFileSystemEntityForUser(parentEntity, authenticatedUser);
                } else if (!foundInvisible && !foundNonDeletable) {
                    // every child item of the entity can be deleted.
                    log.info("Found no invisible or non deletable FileSystemEntities.");
                    fileSystemHelperService.deleteAndUnbindFileSystemEntity(parentEntity);
                    returnList.add(fileSystemHelperService.createDTO(parentEntity, authenticatedUser, null));
                } else {
                    // else some files are left. invisible or not. but the entity cannot be deleted.
                    log.info("Some visible entites could not be deleted but are visible.");
                }
            } else {
                fileSystemHelperService.deleteAndUnbindFileSystemEntity(parentEntity);
                returnList.add(fileSystemHelperService.createDTO(parentEntity, authenticatedUser, null));
            }
        }
        return new Pair<>(foundInvisible, foundNonDeletable);
    }

    public Pair<List<FileSystemItem>, String> downloadFileSystemEntity(List<Long> ids, User authenticatedUser) {
        // validate input and check for parent
        if (ids.isEmpty())
            return new Pair<>(new ArrayList<>(), null);

        List<FileSystemEntity> uncheckedEntities = ids.stream()
                .filter(Objects::nonNull)
                .map(id -> {
                    FileSystemEntity possibleEntity = fileSystemRepository.findByFileSystemId(id);
                    if (null == possibleEntity)
                        throw new FileSystemItemCouldNotBeDownloadedException("FileSystemEntity does not exist or you are not allowed to see the entity.");

                    return possibleEntity;
                }).collect(Collectors.toList());

        List<FileSystemEntity> checkedEntities = uncheckedEntities.stream()
                .filter(entity -> fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ))
                .collect(Collectors.toList());

        if (checkedEntities.size() != uncheckedEntities.size()) {
            log.debug("Entities size and ids size does not match after validation. pre: {} / after: {}", uncheckedEntities.size(), checkedEntities.size());
            throw new FileSystemItemCouldNotBeDownloadedException("FileSystemEntity does not exist or you are not allowed to see the entity.");
        }

        boolean allEntitiesAreInRoot = checkedEntities.stream().allMatch(entity -> !entity.isFile() && entity.getPath().equals("/"));
        boolean singleEntity = checkedEntities.size() == 1;

        List<FileSystemItem> returnList = new ArrayList<>();
        String zipName;

        if (singleEntity) {
            FileSystemEntity currentEntity = checkedEntities.get(0);
            zipName = fileSystemHelperService.getNameOfZipWhenOnlyOneEntityNeedsToBeDownloaded(currentEntity, allEntitiesAreInRoot);
            fileSystemHelperService.getContentsOfFolderRecursivly(returnList, currentEntity, authenticatedUser, "", false);

        } else {
            zipName = fileSystemHelperService.getNameOfZipWhenMultipleEntitiesNeedToBeDownloaded(checkedEntities, allEntitiesAreInRoot);
            if (!allEntitiesAreInRoot) {
                long countOfDifferentParents = checkedEntities.stream()
                        .map(entity -> fileSystemHelperService.getParentNameEntity().apply(entity))
                        .distinct()
                        .count();

                if (countOfDifferentParents != 1)
                    throw new FileSystemItemCouldNotBeDownloadedException("FileSystemEntity need to have a common parent entity.");
            }
            checkedEntities.forEach(entity -> fileSystemHelperService.getContentsOfFolderRecursivly(returnList, entity, authenticatedUser, "", true));
        }
        return new Pair<>(returnList, zipName);
    }

    public List<FileSystemItem> searchFileSystemEntity(String sanitizedSearch, User authenticatedUser) {
        // check for username with the same name
        User userWithTheName;
        List<FileSystemEntity> foundEntities = fileSystemRepository.findAllByNameContainingIgnoreCase(sanitizedSearch);

        try {
            userWithTheName = userBusinessService.findUserByUsername(sanitizedSearch);

            if (userWithTheName.getUserId() != RestConfiguration.RUNTIME_USER_ID) {
                // there is a user with the name -> add the users root to list.
                FileSystemEntity userRootEntity = fileSystemHelperService.getRootEntityForUser(userWithTheName);
                userRootEntity.setName(userWithTheName.getUsername());
                foundEntities.add(userRootEntity);
            }
        } catch (UserNotFoundException ignored) {
            log.debug("Searched for {}, was not a username.", sanitizedSearch);
        }

        if (null == foundEntities)
            throw new FileSystemItemNotFoundException();

        List<FileSystemEntity> visibleEntities = foundEntities.stream()
                .filter(entity -> fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(entity, authenticatedUser, InteractionType.READ))
                .collect(Collectors.toList());

        return visibleEntities.stream()
                .map(entity -> {
                    String username = fileSystemHelperService.getOwnerUsernameForEntity(entity);
                    String path;
                    if (entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId()) {
                        FileSystemEntity parent = fileSystemRepository.findByItemIdsContaining(entity.getFileSystemId());
                        if (null == parent)
                            throw new FileFighterDataException("Couldn't find parent entity for id: " + entity.getFileSystemId());
                        path = parent.getPath();
                        if (path.equals("/")) {
                            path += entity.getName();
                        } else {
                            path += "/" + entity.getName();
                        }
                    } else {
                        path = entity.getPath();
                    }
                    return fileSystemHelperService.createDTO(entity, authenticatedUser, "/" + username + path);
                })
                .collect(Collectors.toList());
    }
}
