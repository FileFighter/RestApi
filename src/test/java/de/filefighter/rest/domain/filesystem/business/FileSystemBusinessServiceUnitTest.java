package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemContentsNotAccessibleException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeDeletedException;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemNotFoundException;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.Group;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.filefighter.rest.domain.filesystem.type.FileSystemType.FOLDER;
import static de.filefighter.rest.domain.filesystem.type.FileSystemType.TEXT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSystemBusinessServiceUnitTest {

    private final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
    private final UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private final FileSystemTypeRepository fileSystemTypeRepositoryMock = mock(FileSystemTypeRepository.class);
    private final FileSystemHelperService fileSystemHelperServiceMock = mock(FileSystemHelperService.class);

    private final FileSystemBusinessService fileSystemBusinessService = new FileSystemBusinessService(fileSystemRepositoryMock, fileSystemHelperServiceMock, fileSystemTypeRepositoryMock, userBusinessServiceMock);

    @Test
    void getFolderContentsByPathThrows() {
        String notValid = "";
        String wrongFormat = "asd";
        String wrongFormat1 = "as/d";
        String validUsername = "kevin";
        String validPathToFind = "/path";
        String validPath = "/" + validUsername + validPathToFind;

        User dummyUser = User.builder().userId(0).build();

        FileSystemContentsNotAccessibleException ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(notValid, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix() + " Path was in wrong format.", ex.getMessage());

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(wrongFormat, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix() + " Path was in wrong format.", ex.getMessage());

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(wrongFormat1, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix() + " Path was in wrong format. Use a leading backslash.", ex.getMessage());

        when(fileSystemRepositoryMock.findByPath(validPath)).thenReturn(null);
        when(userBusinessServiceMock.findUserByUsername(validUsername)).thenReturn(User.builder().username(validUsername + "somethingelse").build());
        when(fileSystemHelperServiceMock.removeTrailingBackSlashes(validPath)).thenReturn(validPath);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());

        ArrayList<FileSystemEntity> fileSystemEntityArrayList = new ArrayList<>();
        fileSystemEntityArrayList.add(FileSystemEntity.builder().isFile(true).build());
        fileSystemEntityArrayList.add(FileSystemEntity.builder().isFile(false).typeId(-1).build());
        fileSystemEntityArrayList.add(FileSystemEntity.builder().lastUpdatedBy(420).build());

        when(fileSystemRepositoryMock.findByPath(validPath)).thenReturn(fileSystemEntityArrayList);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());

        // throws because the user was not found.
        when(userBusinessServiceMock.findUserByUsername(validUsername)).thenThrow(new UserNotFoundException("Like what?"));
        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());
    }

    @Test
    void getFolderContentsByPathStillThrows() {
        String validUsername = "kevin";
        String validPathToFind = "/path";
        String validPath = "/" + validUsername + validPathToFind;
        long dummyUserId = 420124;
        User authenticatedUser = User.builder().userId(0).build();
        User ownerOfFiles = User.builder().userId(dummyUserId).username(validUsername).build();

        when(fileSystemHelperServiceMock.removeTrailingBackSlashes(validPathToFind)).thenReturn(validPathToFind);

        // throws because no folder was found.
        when(fileSystemRepositoryMock.findByPath(validPathToFind)).thenReturn(null);
        when(userBusinessServiceMock.findUserByUsername(validUsername)).thenReturn(ownerOfFiles);
        FileSystemContentsNotAccessibleException ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, authenticatedUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());

        // collection is empty after removing files.
        ArrayList<FileSystemEntity> listOfPossibleFolders = new ArrayList<>();
        listOfPossibleFolders.add(FileSystemEntity.builder().isFile(true).typeId(FOLDER.getId()).build());
        when(fileSystemRepositoryMock.findByPath(validPathToFind)).thenReturn(listOfPossibleFolders);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, authenticatedUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());

        // more than one folder was found.
        listOfPossibleFolders = new ArrayList<>();
        listOfPossibleFolders.add(FileSystemEntity.builder().fileSystemId(-420).ownerId(dummyUserId).isFile(false).typeId(FOLDER.getId()).build());
        listOfPossibleFolders.add(FileSystemEntity.builder().fileSystemId(1234).isFile(false).ownerId(dummyUserId).typeId(FOLDER.getId()).build());
        when(fileSystemRepositoryMock.findByPath(validPathToFind)).thenReturn(listOfPossibleFolders);

        FileFighterDataException dataException = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Found more than one folder with the path " + validPathToFind, dataException.getMessage());

        // not the rights for the actual entity
        listOfPossibleFolders = new ArrayList<>();
        listOfPossibleFolders.add(FileSystemEntity.builder().fileSystemId(-420).ownerId(dummyUserId).isFile(false).typeId(FOLDER.getId()).build());
        when(fileSystemRepositoryMock.findByPath(validPathToFind)).thenReturn(listOfPossibleFolders);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(any(), eq(authenticatedUser), eq(InteractionType.READ))).thenReturn(false);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, authenticatedUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());
    }

    @Test
    void getFolderContentsByPathWorksWhenRequestingRoot() {
        String path = "/";
        long userId = 420;
        User user = User.builder().userId(userId).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().path("/").ownerId(userId).lastUpdatedBy(RestConfiguration.RUNTIME_USER_ID).isFile(false).lastUpdatedBy(userId).typeId(FOLDER.getId()).build();
        FileSystemItem fileSystemItem = FileSystemItem.builder().build();
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(fileSystemEntity);

        when(fileSystemHelperServiceMock.removeTrailingBackSlashes(path)).thenReturn(path);
        when(fileSystemRepositoryMock.findByPath(path)).thenReturn(entities);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.createDTO(fileSystemEntity, user, path)).thenReturn(fileSystemItem);

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(path, user);
        assertEquals(1, fileSystemItems.size());
        assertEquals(fileSystemItem, fileSystemItems.get(0));
    }

    @Test
    void getFolderContentsByPathWorksWhenRequestingNonRoot() {
        String ownerName = "foobar";
        String path = "/";
        String requestingPath = path + ownerName;
        long userId = 420;
        User user = User.builder().userId(userId).username(ownerName).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().path("/").ownerId(userId).lastUpdatedBy(RestConfiguration.RUNTIME_USER_ID).isFile(false).lastUpdatedBy(userId).typeId(FOLDER.getId()).build();
        FileSystemItem fileSystemItem = FileSystemItem.builder().build();
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(fileSystemEntity);

        String itemName = "baum.txt";
        ArrayList<FileSystemEntity> children = new ArrayList<>();
        FileSystemEntity child = FileSystemEntity.builder().name(itemName).build();
        FileSystemItem childItem = FileSystemItem.builder().build();
        children.add(child);

        when(userBusinessServiceMock.findUserByUsername(ownerName)).thenReturn(user);
        when(fileSystemHelperServiceMock.removeTrailingBackSlashes(path)).thenReturn(path);
        when(fileSystemRepositoryMock.findByPath(path)).thenReturn(entities);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(fileSystemEntity, user, true, false)).thenReturn(children);
        when(fileSystemHelperServiceMock.createDTO(child, user, requestingPath + path + itemName)).thenReturn(childItem);

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(requestingPath, user);
        assertEquals(1, fileSystemItems.size());
        assertEquals(fileSystemItem, fileSystemItems.get(0));
    }

    @Test
    void deleteFileSystemItemByIdThrows() {
        long requestId = 420;
        User authenticatedUser = User.builder().build();
        FileSystemEntity entityToDelete = FileSystemEntity.builder().build();

        FileSystemItemCouldNotBeDeletedException ex = assertThrows(FileSystemItemCouldNotBeDeletedException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(requestId, authenticatedUser));
        assertEquals(FileSystemItemCouldNotBeDeletedException.getErrorMessagePrefix() + " FileSystemId was " + requestId, ex.getMessage());

        when(fileSystemRepositoryMock.findByFileSystemId(requestId)).thenReturn(entityToDelete);

        ex = assertThrows(FileSystemItemCouldNotBeDeletedException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(requestId, authenticatedUser));
        assertEquals(FileSystemItemCouldNotBeDeletedException.getErrorMessagePrefix() + " FileSystemId was " + requestId, ex.getMessage());
    }

    @Test
    void deleteFileSystemItemByIdWorksWithDeletableItemsOnly() {
        long requestId = 420;
        User authenticatedUser = User.builder().build();
        FileSystemEntity entityFolderToDelete = FileSystemEntity.builder().fileSystemId(requestId).isFile(false).fileSystemId(FOLDER.getId()).itemIds(new long[]{123, 321, 1234}).build();
        FileSystemEntity entity0 = FileSystemEntity.builder().isFile(true).typeId(TEXT.getId()).fileSystemId(321).build();
        FileSystemEntity entity1 = FileSystemEntity.builder().isFile(true).typeId(TEXT.getId()).fileSystemId(123).build();
        FileSystemEntity entity2 = FileSystemEntity.builder().isFile(false).typeId(FOLDER.getId()).fileSystemId(1234).build();
        List<FileSystemEntity> contentsOfDirectoryToDelete = Arrays.asList(entity0, entity1, entity2);

        FileSystemItem folderItem = FileSystemItem.builder().build();
        FileSystemItem file0 = FileSystemItem.builder().build();
        FileSystemItem file1 = FileSystemItem.builder().build();
        FileSystemItem file2 = FileSystemItem.builder().build();
        List<FileSystemItem> dtoReturnList = Arrays.asList(folderItem, file0, file1, file2);

        when(fileSystemRepositoryMock.findByFileSystemId(requestId)).thenReturn(entityFolderToDelete);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entityFolderToDelete, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entityFolderToDelete, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(entityFolderToDelete, authenticatedUser, false, false)).thenReturn(contentsOfDirectoryToDelete);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity0, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity0, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity1, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity1, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity2, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity2, authenticatedUser, InteractionType.DELETE)).thenReturn(true);

        // create dtos.
        when(fileSystemHelperServiceMock.createDTO(entityFolderToDelete, authenticatedUser, null)).thenReturn(folderItem);
        when(fileSystemHelperServiceMock.createDTO(entity0, authenticatedUser, null)).thenReturn(file0);
        when(fileSystemHelperServiceMock.createDTO(entity1, authenticatedUser, null)).thenReturn(file1);
        when(fileSystemHelperServiceMock.createDTO(entity2, authenticatedUser, null)).thenReturn(file2);

        // call function
        List<FileSystemItem> actual = fileSystemBusinessService.deleteFileSystemItemById(requestId, authenticatedUser);

        // verify deletion
        verify(fileSystemHelperServiceMock, times(4)).deleteAndUnbindFileSystemEntity(any());

        // check for returns
        assertEquals(dtoReturnList, actual);
    }

    @Test
    void deleteFileSystemItemByIdWorksWithInvisibleItemsOnly() {
        long requestId = 420;
        long userId = 1234;
        long notUserId0 = 123898901;
        long notUserId1 = 908137452;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();
        FileSystemEntity entityFolderToDelete = FileSystemEntity.builder()
                .visibleForGroupIds(new long[]{Group.FAMILY.getGroupId(), Group.ADMIN.getGroupId()})
                .visibleForUserIds(new long[]{userId, notUserId0, notUserId1})
                .fileSystemId(requestId)
                .isFile(false)
                .typeId(FOLDER.getId())
                .itemIds(new long[]{123})
                .build();
        FileSystemEntity entity0 = FileSystemEntity.builder().isFile(true).typeId(TEXT.getId()).fileSystemId(321).build();

        List<FileSystemEntity> contentsOfDirectoryToDelete = Collections.singletonList(entity0);

        FileSystemItem folderItem = FileSystemItem.builder().build();
        FileSystemItem file0 = FileSystemItem.builder().build();

        when(fileSystemRepositoryMock.findByFileSystemId(requestId)).thenReturn(entityFolderToDelete);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entityFolderToDelete, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entityFolderToDelete, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(entityFolderToDelete, authenticatedUser, false, false)).thenReturn(contentsOfDirectoryToDelete);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity0, authenticatedUser, InteractionType.READ)).thenReturn(false);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity0, authenticatedUser, InteractionType.DELETE)).thenReturn(false);

        // create dtos.
        when(fileSystemHelperServiceMock.createDTO(entityFolderToDelete, authenticatedUser, null)).thenReturn(folderItem);
        when(fileSystemHelperServiceMock.createDTO(entity0, authenticatedUser, null)).thenReturn(file0);

        // call function
        List<FileSystemItem> deletedItems = fileSystemBusinessService.deleteFileSystemItemById(requestId, authenticatedUser);

        // verify change of parent


        verify(fileSystemHelperServiceMock, times(1)).removeVisibilityRightsOfFileSystemEntityForUser(entityFolderToDelete, authenticatedUser);
        assertEquals(0, deletedItems.size());
    }

    @Test
    void deleteFileSystemItemByIdWorksWithNonDeletableItems() {
        long requestId = 420;
        long userId = 1234;
        long notUserId0 = 123898901;
        long notUserId1 = 908137452;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();
        FileSystemEntity entityFolderToDelete = FileSystemEntity.builder()
                .visibleForGroupIds(new long[]{Group.FAMILY.getGroupId(), Group.ADMIN.getGroupId()})
                .visibleForUserIds(new long[]{userId, notUserId0, notUserId1})
                .fileSystemId(requestId)
                .isFile(false)
                .typeId(FOLDER.getId())
                .itemIds(new long[]{123})
                .build();
        FileSystemEntity entity0 = FileSystemEntity.builder().isFile(true).typeId(TEXT.getId()).fileSystemId(321).build();

        List<FileSystemEntity> contentsOfDirectoryToDelete = Collections.singletonList(entity0);

        FileSystemItem folderItem = FileSystemItem.builder().build();
        FileSystemItem file0 = FileSystemItem.builder().build();

        when(fileSystemRepositoryMock.findByFileSystemId(requestId)).thenReturn(entityFolderToDelete);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entityFolderToDelete, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entityFolderToDelete, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(entityFolderToDelete, authenticatedUser, false, false)).thenReturn(contentsOfDirectoryToDelete);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity0, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity0, authenticatedUser, InteractionType.DELETE)).thenReturn(false);

        // create dtos.
        when(fileSystemHelperServiceMock.createDTO(entityFolderToDelete, authenticatedUser, null)).thenReturn(folderItem);
        when(fileSystemHelperServiceMock.createDTO(entity0, authenticatedUser, null)).thenReturn(file0);

        // call function
        List<FileSystemItem> actual = fileSystemBusinessService.deleteFileSystemItemById(requestId, authenticatedUser);

        // verify no deletion.
        verify(fileSystemHelperServiceMock, times(0)).deleteAndUnbindFileSystemEntity(any());
        assertTrue(actual.isEmpty());
    }

    @Test
    void getFileSystemItemInfoThrows() {
        long id = 420;
        User dummyUser = User.builder().userId(213421234).build();

        when(fileSystemRepositoryMock.findByFileSystemId(id)).thenReturn(null);
        FileSystemItemNotFoundException ex = assertThrows(FileSystemItemNotFoundException.class, () ->
                fileSystemBusinessService.getFileSystemItemInfo(id, dummyUser));
        assertEquals(FileSystemItemNotFoundException.getErrorMessagePrefix() + " FileSystemId was " + id, ex.getMessage());

        when(fileSystemRepositoryMock.findByFileSystemId(id)).thenReturn(FileSystemEntity.builder().build());
        ex = assertThrows(FileSystemItemNotFoundException.class, () ->
                fileSystemBusinessService.getFileSystemItemInfo(id, dummyUser));
        assertEquals(FileSystemItemNotFoundException.getErrorMessagePrefix() + " FileSystemId was " + id, ex.getMessage());
    }

    @Test
    void getFileSystemItemInfoWorks() {
        long id = 420;
        long userId = 1234321;
        String name = "Folder";
        User dummyUser = User.builder().userId(userId).build();
        FileSystemEntity entity = FileSystemEntity.builder().name(name).lastUpdatedBy(userId).build();
        FileSystemItem item = FileSystemItem.builder().build();

        when(userBusinessServiceMock.getUserById(userId)).thenReturn(dummyUser);
        when(fileSystemRepositoryMock.findByFileSystemId(id)).thenReturn(entity);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(entity, dummyUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.createDTO(entity, dummyUser, null)).thenReturn(item);

        FileSystemItem fileSystemItem = fileSystemBusinessService.getFileSystemItemInfo(id, dummyUser);
        assertEquals(item, fileSystemItem);
    }
}