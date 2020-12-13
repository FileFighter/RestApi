package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Groups;
import de.filefighter.rest.rest.exceptions.FileFighterDataException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

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

    // TODO: implement necessary files when a new user is created.

    public ArrayList<FileSystemItem> getFolderContentsByPath(String path, User authenticatedUser) {
        if (!InputSanitizerService.stringIsValid(path))
            throw new FileSystemContentsNotAccessibleException("Path was not valid.");

        String[] pathWithoutSlashes = path.split("/");

        if (!path.equals("/") && pathWithoutSlashes.length < 1)
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format.");

        if (!path.equals("/") && !"".equals(pathWithoutSlashes[0]))
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format. Use a leading backslash.");

        String pathToFind = removeTrailingBackSlashes(path);

        // find the folder with matching path.
        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || !userIsAllowedToSeeFileSystemEntity(entity, authenticatedUser));

        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        // now only own or shared folders are left.
        ArrayList<FileSystemItem> fileSystemItems = new ArrayList<>(listOfFileSystemEntities.size());

        for (FileSystemEntity fileSystemEntity : listOfFileSystemEntities) {
            long[] folderContentItemIds = fileSystemEntity.getItemIds();

            // check if the contents are visible.
            for (long fileSystemId : folderContentItemIds) {
                FileSystemEntity fileSystemEntityInFolder = fileSystemRepository.findByFileSystemId(fileSystemId);

                if (null == fileSystemEntityInFolder)
                    throw new FileFighterDataException("FolderContents expected fileSystemItem with id " + listOfFileSystemEntities + " but was empty.");

                if (userIsAllowedToSeeFileSystemEntity(fileSystemEntityInFolder, authenticatedUser)) {
                    fileSystemItems.add(this.createDTO(fileSystemEntityInFolder, authenticatedUser, pathToFind));
                }
            }
        }

        return fileSystemItems;
    }

    private String removeTrailingBackSlashes(String pathToFind) {
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
}
