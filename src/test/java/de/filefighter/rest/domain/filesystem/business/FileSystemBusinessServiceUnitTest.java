package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Groups;
import de.filefighter.rest.rest.exceptions.FileFighterDataException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSystemBusinessServiceUnitTest {

    private final FileSystemRepository fileSystemRepository = mock(FileSystemRepository.class);
    private final UserBusinessService userBusinessService = mock(UserBusinessService.class);
    private final FileSystemTypeRepository fileSystemTypeRepository = mock(FileSystemTypeRepository.class);
    private final FileSystemBusinessService fileSystemBusinessService = new FileSystemBusinessService(fileSystemRepository, userBusinessService, fileSystemTypeRepository);

    @Test
    void getFolderContentsByPathThrows() {
        String notValid = "";
        String wrongFormat = "asd";
        String wrongFormat1 = "as/d";
        String validPath = "/uga/uga/as/sasda/sassasd";

        User dummyUser = User.builder().userId(0).build();

        FileSystemContentsNotAccessibleException ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(notValid, dummyUser));
        assertEquals("Folder contents could not be displayed. Path was not valid.", ex.getMessage());

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(wrongFormat, dummyUser));
        assertEquals("Folder contents could not be displayed. Path was in wrong format.", ex.getMessage());

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(wrongFormat1, dummyUser));
        assertEquals("Folder contents could not be displayed. Path was in wrong format. Use a leading backslash.", ex.getMessage());

        when(fileSystemRepository.findByPath(validPath)).thenReturn(null);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals("Folder does not exist, or you are not allowed to see the folder.", ex.getMessage());

        ArrayList<FileSystemEntity> fileSystemEntityArrayList = new ArrayList<>();
        fileSystemEntityArrayList.add(FileSystemEntity.builder().isFile(true).build());
        fileSystemEntityArrayList.add(FileSystemEntity.builder().isFile(false).typeId(-1).build());
        fileSystemEntityArrayList.add(FileSystemEntity.builder().createdByUserId(420).build());

        when(fileSystemRepository.findByPath(validPath)).thenReturn(fileSystemEntityArrayList);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals("Folder does not exist, or you are not allowed to see the folder.", ex.getMessage());
    }

    @Test
    void getFolderContentsByPathWorks() {
        String path = "/uga/buga/buga";
        String pathToRequest = path+"/";
        long userId = 420;
        long fileIdInFolder = 123;
        User user = User.builder().userId(userId).build();
        FileSystemEntity foundFolder = FileSystemEntity.builder().createdByUserId(userId).itemIds(new long[]{fileIdInFolder}).build();
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(foundFolder);

        when(fileSystemRepository.findByPath(path)).thenReturn(entities);
        when(fileSystemRepository.findByFileSystemId(fileIdInFolder)).thenReturn(FileSystemEntity.builder().createdByUserId(userId).build());
        when(userBusinessService.getUserById(userId)).thenReturn(User.builder().build());

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(pathToRequest, user);
        assertEquals(1, fileSystemItems.size());
    }

    @Test
    void getFolderContentsOfEntityThrows() {
        long fileSystemId = 420;

        User authenticatedUser = User.builder().build();
        FileSystemEntity foundFolder = FileSystemEntity.builder().itemIds(new long[]{fileSystemId}).build();
        ArrayList<FileSystemEntity> arrayList = new ArrayList<>();
        arrayList.add(foundFolder);

        when(fileSystemRepository.findByFileSystemId(fileSystemId)).thenReturn(null);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.getFolderContentsOfEntities(arrayList, authenticatedUser, "/"));
        assertEquals("Internal Error occurred. FolderContents expected fileSystemItem with id " + fileSystemId + " but was empty.", ex.getMessage());
    }

    @Test
    void getFolderContentsOfEntityWorks() {
            long userId = 420;
        User authenticatedUser = User.builder().userId(userId).build();
        FileSystemEntity foundFolder = FileSystemEntity.builder().itemIds(new long[]{0, 1, 2, 3, 4}).build();
        ArrayList<FileSystemEntity> arrayList = new ArrayList<>();
        arrayList.add(foundFolder);

        FileSystemEntity dummyEntity = FileSystemEntity.builder().createdByUserId(userId).build();
        when(fileSystemRepository.findByFileSystemId(0)).thenReturn(dummyEntity);
        when(fileSystemRepository.findByFileSystemId(1)).thenReturn(dummyEntity);
        when(fileSystemRepository.findByFileSystemId(2)).thenReturn(dummyEntity);
        when(fileSystemRepository.findByFileSystemId(3)).thenReturn(dummyEntity);
        when(fileSystemRepository.findByFileSystemId(4)).thenReturn(FileSystemEntity.builder().createdByUserId(userId+1).build());
        when(userBusinessService.getUserById(userId)).thenReturn(User.builder().userId(userId).build());

        ArrayList<FileSystemItem> actual = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsOfEntities(arrayList, authenticatedUser, "/");
        assertEquals(4, actual.size());
    }

    @Test
    void removeTrailingWhiteSpaces() {
        String doesNotRemove0 = "/";
        String doesNotRemove1 = "/ugabuga";
        String doesRemove = "/uga/";
        String removed = "/uga";


        String actual0 = fileSystemBusinessService.removeTrailingBackSlashes(doesNotRemove0);
        assertEquals(doesNotRemove0, actual0);

        String actual1 = fileSystemBusinessService.removeTrailingBackSlashes(doesNotRemove1);
        assertEquals(doesNotRemove1, actual1);

        String actual2 = fileSystemBusinessService.removeTrailingBackSlashes(doesRemove);
        assertEquals(removed, actual2);
    }

    @Test
    void userIsAllowedToSeeFileSystemEntity() {
        long userId = 1232783672;
        User user = User.builder().userId(userId).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().createdByUserId(userId).build();

        // user created fileSystemItem
        assertTrue(fileSystemBusinessService.userIsAllowedToSeeFileSystemEntity(fileSystemEntity, user));

        // user got it shared.
        fileSystemEntity = FileSystemEntity.builder().visibleForUserIds(new long[]{userId}).build();
        assertTrue(fileSystemBusinessService.userIsAllowedToSeeFileSystemEntity(fileSystemEntity, user));

        //user is in group
        user = User.builder().groups(new Groups[]{Groups.ADMIN}).build();
        fileSystemEntity = FileSystemEntity.builder().visibleForGroupIds(new long[]{1}).build();
        assertTrue(fileSystemBusinessService.userIsAllowedToSeeFileSystemEntity(fileSystemEntity, user));

        // user is not allowed.
        user = User.builder().groups(new Groups[]{Groups.UNDEFINED}).build();
        fileSystemEntity = FileSystemEntity.builder().visibleForGroupIds(new long[]{1}).build();
        assertFalse(fileSystemBusinessService.userIsAllowedToSeeFileSystemEntity(fileSystemEntity, user));
    }

    @Test
    void createDTOThrows() {
        long userId = 420L;
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().createdByUserId(userId).build();

        when(userBusinessService.getUserById(userId)).thenReturn(null);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.createDTO(fileSystemEntity, null, "/"));

        assertEquals("Internal Error occurred. Owner of File/Folder does not exist.", ex.getMessage());
    }

    @Test
    void createDTOWorks() {
        long createdByUserId = 420L;
        String basePath = "/someTHING/somethingElse/";
        long[] items = new long[]{1, 2, 3};
        long fileSystemId = 123123;
        boolean isFile = true;
        long lastUpdated = 123123;
        String name = "SomeText.txt";
        double size = 123321;
        long typeId = -1;

        User authenticatedUser = User.builder().userId(createdByUserId - 1).build();
        User userThatCreatedFile = User.builder().userId(createdByUserId).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity
                .builder()
                .createdByUserId(createdByUserId)
                .itemIds(items)
                .fileSystemId(fileSystemId)
                .isFile(isFile)
                .lastUpdated(lastUpdated)
                .name(name)
                .path("") // is empty because its a file.
                .size(size)
                .typeId(typeId)
                .build();

        when(userBusinessService.getUserById(createdByUserId)).thenReturn(userThatCreatedFile);
        when(fileSystemTypeRepository.findFileSystemTypeById(typeId)).thenReturn(FileSystemType.UNDEFINED);

        FileSystemItem actual = fileSystemBusinessService.createDTO(fileSystemEntity, authenticatedUser, basePath);

        assertEquals(createdByUserId, actual.getCreatedByUserId());
        assertEquals(fileSystemId, actual.getFileSystemId());
        assertEquals(lastUpdated, actual.getLastUpdated());
        assertEquals(name, actual.getName());
        assertEquals(size, actual.getSize());
        assertEquals(FileSystemType.UNDEFINED, actual.getType());
        assertEquals(basePath + name, actual.getPath());
        assertTrue(actual.isShared());
    }
}