package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Groups;
import de.filefighter.rest.rest.exceptions.FileFighterDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSystemBusinessServiceUnitTest {

    private final FileSystemRepository fileSystemRepository = mock(FileSystemRepository.class);
    private final UserBusinessService userBusinessService = mock(UserBusinessService.class);
    private final FileSystemTypeRepository fileSystemTypeRepository = mock(FileSystemTypeRepository.class);
    private final FileSystemBusinessService fileSystemBusinessService = new FileSystemBusinessService(fileSystemRepository, userBusinessService, fileSystemTypeRepository);

    @Test
    void getFolderContentsByPath() {
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