package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.dto.File;
import de.filefighter.rest.domain.filesystem.data.dto.Folder;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.group.Groups;
import de.filefighter.rest.rest.exceptions.FileFighterDataException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;

@Service
public class FileSystemBusinessService {

    private final FileSystemItemsDTOService fileSystemItemsDTOService;
    private final FileSystemRepository fileSystemRepository;
    private final UserBusinessService userBusinessService;

    public FileSystemBusinessService(FileSystemItemsDTOService fileSystemItemsDTOService, FileSystemRepository fileSystemRepository, UserBusinessService userBusinessService) {
        this.fileSystemItemsDTOService = fileSystemItemsDTOService;
        this.fileSystemRepository = fileSystemRepository;
        this.userBusinessService = userBusinessService;
    }

    // TODO: implement necessary files when a new user is created.

    // path is /username/folder
    // in db only /folder but we know the username that created it.
    // that way we know what contents to display if the same folderName exists twice.

    public FolderContents getFolderContentsByPath(String path, User authenticatedUser) {
        String[] pathWithoutSlashes = path.split("/");

        if (pathWithoutSlashes.length < 2)
            throw new FileSystemContentsNotAccessibleException();

        if (!"".equals(pathWithoutSlashes[0]))
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format. Use a leading backslash.");

        String username = pathWithoutSlashes[1];
        if (!InputSanitizerService.stringIsValid(username))
            throw new FileSystemContentsNotAccessibleException("Path was in wrong format. Username was not found in path.");

        UserEntity userEntityWithUsername = userBusinessService.getUserWithUsername(username);
        if (null == userEntityWithUsername)
            throw new FileSystemContentsNotAccessibleException();

        //TODO: is it a risk to use a user input as regex?
        String pathToFind = path.split("/" + username)[1];
        pathToFind = removeTrailingBackSlashes(pathToFind);

        // find the folder with matching path.
        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        // remove all not accessible items.
        listOfFileSystemEntities.removeIf(entity -> entity.isFile() || entity.getTypeId() != FileSystemType.FOLDER.getId() || userEntityWithUsername.getUserId() != entity.getCreatedByUserId() || !userIsAllowedToSeeFileSystemEntity(entity, authenticatedUser));

        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        if (listOfFileSystemEntities.size() != 1)
            throw new IllegalStateException("More than one folder was found with the path " + pathToFind + " and name " + username);

        // get the contents
        FileSystemEntity fileSystemEntity = listOfFileSystemEntities.get(0);
        return this.getFolderContentsByEntity(fileSystemEntity, authenticatedUser);
    }

    public FolderContents getFolderContentsByEntity(FileSystemEntity fileSystemEntity, User authenticatedUser) {
        long[] folderContentItemIds = fileSystemEntity.getItemIds();

        ArrayList<Folder> folders = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();

        // check if the contents are visible.
        for (long fileSystemId : folderContentItemIds) {
            FileSystemEntity fileSystemEntityInFolder = fileSystemRepository.findByFileSystemId(fileSystemId);

            if (null == fileSystemEntityInFolder)
                throw new FileFighterDataException("FolderContents expected fileSystemItem with id " + fileSystemEntity + " but was empty.");

            if (userIsAllowedToSeeFileSystemEntity(fileSystemEntityInFolder, authenticatedUser)) {
                if (fileSystemEntityInFolder.isFile()) {
                    File file = fileSystemItemsDTOService.createFileDto(fileSystemEntityInFolder);
                    files.add(file);
                } else {
                    Folder folder = fileSystemItemsDTOService.createFolderDto(fileSystemEntityInFolder);
                    folders.add(folder);
                }
            }
        }

        return FolderContents.builder()
                .files(files.toArray(new File[0]))
                .folders(folders.toArray(new Folder[0]))
                .build();
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
}
