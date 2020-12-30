package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.common.exceptions.InputSanitizerService;
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
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.Groups;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Log4j2
@Service
public class FileSystemBusinessService {

    private final FileSystemRepository fileSystemRepository;
    private final UserBusinessService userBusinessService;
    private final FileSystemTypeRepository fileSystemTypeRepository;

    public FileSystemBusinessService(FileSystemRepository fileSystemRepository, UserBusinessService userBusinessService, FileSystemTypeRepository fileSystemTypeRepository) {
        this.fileSystemRepository = fileSystemRepository;
        this.userBusinessService = userBusinessService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
    }

    public List<FileSystemItem> getFolderContentsByPath(String path, User authenticatedUser) {
        if (!InputSanitizerService.stringIsValid(path))
            throw new FileSystemContentsNotAccessibleException("Path was not valid.");

        String[] pathWithoutSlashes = path.split("/");

        if (!path.equals("/") && pathWithoutSlashes.length < 2)
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format.");

        if (!path.equals("/") && !"".equals(pathWithoutSlashes[0]))
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format. Use a leading backslash.");

        String pathToFind = removeTrailingBackSlashes(path);

        // find the folder with matching path.
        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (null == listOfFileSystemEntities) // does return null and not a empty collection.
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || !userIsAllowedToSeeFileSystemEntity(entity, authenticatedUser));

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

        if (!userIsAllowedToSeeFileSystemEntity(fileSystemEntity, authenticatedUser))
            throw new FileSystemItemNotFoundException(fsItemId);

        return createDTO(fileSystemEntity, authenticatedUser, null);
    }

    public void deleteFileSystemItemById(long fsItemId, User authenticatedUser) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findByFileSystemId(fsItemId);
        if (null == fileSystemEntity)
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        if (!userIsAllowedToEditFileSystemEntity(fileSystemEntity, authenticatedUser))
            throw new FileSystemItemCouldNotBeDeletedException(fsItemId);

        if (fileSystemEntity.isFile()) {
            fileSystemRepository.delete(fileSystemEntity);
        } else {
            HashSet<FileSystemEntity> foundFolders = new HashSet<>();
            foundFolders.add(fileSystemEntity);
            Iterator<FileSystemEntity> iterator = foundFolders.iterator();

            // Lists for change or deletion
            ArrayList<FileSystemEntity> entitiesToBeDeleted = new ArrayList<>();
            ArrayList<FileSystemEntity> entitiesToBeChanged = new ArrayList<>();

            // I feel the ConcurrentModificationException coming.
            while (iterator.hasNext()) {
                FileSystemEntity nextFolder = iterator.next();
                // no permissions set for comparison.
                ArrayList<FileSystemEntity> foundEntities = (ArrayList<FileSystemEntity>) getFolderContentsOfEntityAndPermissions(nextFolder, authenticatedUser, false, false);
                int countOfChildEntities = foundEntities.size();
                int countOfDeletedEntities = 0;
                int invisibleEntities = 0;

                for (FileSystemEntity fileSystemEntityToBeDeleted : foundEntities) {
                    // check here for permissions.
                    if (userIsAllowedToSeeFileSystemEntity(fileSystemEntityToBeDeleted, authenticatedUser)) {
                        if (userIsAllowedToEditFileSystemEntity(fileSystemEntityToBeDeleted, authenticatedUser)) {
                            if (fileSystemEntityToBeDeleted.isFile()) {
                                // datei -> add to deletion, update parent folder
                                entitiesToBeDeleted.add(fileSystemEntityToBeDeleted);
                                nextFolder.setItemIds(Arrays.stream(nextFolder.getItemIds()).filter(id -> id != fileSystemEntityToBeDeleted.getFileSystemId()).toArray());
                            } else {
                                // folder -> add to set.
                                foundFolders.add(fileSystemEntityToBeDeleted);
                            }
                            countOfDeletedEntities++;
                        }
                    } else {
                        invisibleEntities++;
                    }
                }

                if (countOfChildEntities != countOfDeletedEntities) {
                    // some entities could not be deleted because he is not allowed or cannot see them.
                    if (invisibleEntities > 0) {
                        // 1. non visible entities. (not fine -> empty folder)
                        // TODO: make parentFolder also invisible. BUT do it in way that "sums up" the rights of all files and folders way.
                    }
                    // 2. visible but non deletable entities. (fine)
                    entitiesToBeChanged.add(nextFolder);
                } else {
                    // remove the folder from set and delete it.
                    foundFolders.remove(nextFolder);
                    entitiesToBeDeleted.add(nextFolder);
                }
            }
            // delete the files
            for(FileSystemEntity fsEntityToDelete: entitiesToBeDeleted){
                fileSystemRepository.delete(fsEntityToDelete);
            }
            // TODO: query the changes.
        }
    }

    // ---------------- HELPER -------------------

    public List<FileSystemEntity> getFolderContentsOfEntityAndPermissions(FileSystemEntity fileSystemEntity, User authenticatedUser, boolean needsToBeVisible, boolean needsToBeEditable) {
        long[] folderContentItemIds = fileSystemEntity.getItemIds();
        List<FileSystemEntity> fileSystemEntities = new ArrayList<>(folderContentItemIds.length);

        // check if the contents are visible / editable.
        for (long fileSystemId : folderContentItemIds) {
            FileSystemEntity fileSystemEntityInFolder = fileSystemRepository.findByFileSystemId(fileSystemId);

            if (null == fileSystemEntityInFolder)
                throw new FileFighterDataException("FolderContents expected fileSystemItem with id " + fileSystemId + " but was empty.");

            if (needsToBeVisible && !needsToBeEditable && userIsAllowedToSeeFileSystemEntity(fileSystemEntityInFolder, authenticatedUser)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (needsToBeEditable && !needsToBeVisible && userIsAllowedToEditFileSystemEntity(fileSystemEntityInFolder, authenticatedUser)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (needsToBeVisible && needsToBeEditable && userIsAllowedToSeeFileSystemEntity(fileSystemEntityInFolder, authenticatedUser) && userIsAllowedToEditFileSystemEntity(fileSystemEntityInFolder, authenticatedUser)) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
            if (!needsToBeVisible && !needsToBeEditable) {
                fileSystemEntities.add(fileSystemEntityInFolder);
            }
        }
        return fileSystemEntities;
    }

    public boolean userIsAllowedToSeeFileSystemEntity(FileSystemEntity fileSystemEntity, User authenticatedUser) {
        // user created the item
        if (fileSystemEntity.getCreatedByUserId() == authenticatedUser.getUserId())
            return true;

        // user got the item shared.
        for (long userId : fileSystemEntity.getVisibleForUserIds()) {
            if (userId == authenticatedUser.getUserId())
                return true;
        }

        // user is in group that got the item shared.
        long[] fileIsSharedToGroups = fileSystemEntity.getVisibleForGroupIds();
        for (Groups group : authenticatedUser.getGroups()) {
            for (long groupId : fileIsSharedToGroups) {
                if (groupId == group.getGroupId())
                    return true;

            }
        }
        return false;
    }

    public boolean userIsAllowedToEditFileSystemEntity(FileSystemEntity fileSystemEntity, User authenticatedUser) {
        // user created the item
        if (fileSystemEntity.getCreatedByUserId() == authenticatedUser.getUserId())
            return true;

        // user got the item shared.
        for (long userId : fileSystemEntity.getEditableForUserIds()) {
            if (userId == authenticatedUser.getUserId())
                return true;
        }

        // user is in group that got the item shared.
        long[] fileIsSharedToGroups = fileSystemEntity.getEditableFoGroupIds();
        for (Groups group : authenticatedUser.getGroups()) {
            for (long groupId : fileIsSharedToGroups) {
                if (groupId == group.getGroupId())
                    return true;

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
                .createdByUserId(registeredUserEntity.getUserId())
                .typeId(0)
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
