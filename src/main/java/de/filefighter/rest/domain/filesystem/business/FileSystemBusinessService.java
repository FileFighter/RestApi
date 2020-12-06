package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.dto.File;
import de.filefighter.rest.domain.filesystem.data.dto.Folder;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.user.data.dto.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class FileSystemBusinessService {

    private final FileSystemRepository fileSystemRepository;

    public FileSystemBusinessService(FileSystemRepository fileSystemRepository) {

        this.fileSystemRepository = fileSystemRepository;
    }

    // TODO: implement necessary files when a new user is created.

    // path is /username/folder
    // in db only /folder but we know the username that created it.
    // that way we know what contents to display if the same foldername exists twice.

    public FolderContents getContentsOfFolder(String path, User authenticatedUser) {
        String pathToFind = this.removeTrailingBackSlashFromPath(path);

        ArrayList<FileSystemEntity> listOfFileSystemEntities = fileSystemRepository.findByPath(pathToFind);
        if (listOfFileSystemEntities.isEmpty())
            throw new FileSystemContentsNotAccessibleException();

        if (fileSystemEntity.isFile() || fileSystemEntity.getTypeId() != FileSystemType.FOLDER.getId())
            throw new FileSystemContentsNotAccessibleException();

        if (!userIsAllowedToSeeFileSystemEntity(fileSystemEntity, authenticatedUser))
            throw new FileSystemContentsNotAccessibleException();


        FolderContents folderContents;
        switch (path) {
            case "/":
                folderContents = FolderContents.builder()
                        .files(new File[]{new File(0, "DummyFileInRoot.txt", 420, 0, Instant.now().getEpochSecond(), FileSystemType.TEXT, null)})
                        .folders(new Folder[]{
                                new Folder(1, "/bla", "bla", 12345, 0, Instant.now().getEpochSecond(), null),
                                new Folder(2, "/fasel", "fasel", 12345, 0, Instant.now().getEpochSecond(), null)
                        })
                        .build();
                break;
            case "/bla":
                folderContents = FolderContents.builder()
                        .files(new File[]{
                                new File(3, "DummyFileInBla.pdf", 42, 0, Instant.now().getEpochSecond(), FileSystemType.PDF, null),
                                new File(4, "DummyFileInBla1.jpg", 1234321, 0, Instant.now().getEpochSecond(), FileSystemType.PICTURE, null)
                        })
                        .build();
                break;
            case "/fasel":
                folderContents = FolderContents.builder()
                        .files(new File[]{new File(5, "DummyFileInFasel.txt", 420, 0, Instant.now().getEpochSecond(), FileSystemType.TEXT, null)})
                        .folders(new Folder[]{new Folder(6, "/fasel/johndoessecretchamber", "JohnDoesSecretChamber", 12345, 0, Instant.now().getEpochSecond(), null)})
                        .build();
                break;
            case "/fasel/johndoessecretchamber":
                folderContents = FolderContents.builder()
                        .folders(new Folder[]{new Folder(7, "/fasel/johndoessecretchamber/empty", "Empty", 12345, 0, Instant.now().getEpochSecond(), null)})
                        .build();
                break;
            case "/fasel/johndoessecretchamber/empty":
                folderContents = FolderContents.builder().build();
                break;
            default:
                throw new FileSystemContentsNotAccessibleException();

        }
        return folderContents;
    }

    public String removeTrailingBackSlashFromPath(String path) {
        return null;
    }

    public boolean userIsAllowedToSeeFileSystemEntity(FileSystemEntity entity, User user) {
        return true;
    }
}
