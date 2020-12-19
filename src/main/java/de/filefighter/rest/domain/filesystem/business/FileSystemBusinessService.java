package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.group.Groups;
import de.filefighter.rest.rest.exceptions.FileFighterDataException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        return getFolderContentsOfEntities(listOfFileSystemEntities, authenticatedUser, pathToFind);
    }

    public List<FileSystemItem> getFolderContentsOfEntities(List<FileSystemEntity> listOfFileSystemEntities, User authenticatedUser, String pathToFind) {
        List<FileSystemItem> fileSystemItems = new ArrayList<>();

        for (FileSystemEntity fileSystemEntity : listOfFileSystemEntities) {
            long[] folderContentItemIds = fileSystemEntity.getItemIds();

            // check if the contents are visible.
            for (long fileSystemId : folderContentItemIds) {
                FileSystemEntity fileSystemEntityInFolder = fileSystemRepository.findByFileSystemId(fileSystemId);

                if (null == fileSystemEntityInFolder)
                    throw new FileFighterDataException("FolderContents expected fileSystemItem with id " + fileSystemId + " but was empty.");

                if (userIsAllowedToSeeFileSystemEntity(fileSystemEntityInFolder, authenticatedUser)) {
                    String pathWithTrailingSlash = pathToFind.equals("/") ? pathToFind : pathToFind + "/"; //NOSONAR
                    fileSystemItems.add(this.createDTO(fileSystemEntityInFolder, authenticatedUser, pathWithTrailingSlash));
                }
            }
        }

        return fileSystemItems;
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

    public FileSystemItem createDTO(FileSystemEntity fileSystemEntity, User authenticatedUser, String basePath) {
        User ownerOfFileSystemItem = userBusinessService.getUserById(fileSystemEntity.getCreatedByUserId());
        if (null == ownerOfFileSystemItem)
            throw new FileFighterDataException("Owner of File/Folder does not exist.");

        boolean isShared = ownerOfFileSystemItem.getUserId() != authenticatedUser.getUserId();

        return FileSystemItem.builder()
                .createdByUserId(fileSystemEntity.getCreatedByUserId())
                .fileSystemId(fileSystemEntity.getFileSystemId())
                .lastUpdated(fileSystemEntity.getLastUpdated())
                .name(fileSystemEntity.getName())
                .size(fileSystemEntity.getSize())
                .type(fileSystemTypeRepository.findFileSystemTypeById(fileSystemEntity.getTypeId()))
                .path(basePath + fileSystemEntity.getName())
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
