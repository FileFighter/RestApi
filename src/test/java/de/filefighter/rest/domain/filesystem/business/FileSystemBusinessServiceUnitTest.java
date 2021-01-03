package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
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
import de.filefighter.rest.domain.user.group.Groups;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileSystemBusinessServiceUnitTest {

    private final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
    private final UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private final FileSystemTypeRepository fileSystemTypeRepositoryMock = mock(FileSystemTypeRepository.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private final FileSystemBusinessService fileSystemBusinessService = new FileSystemBusinessService(fileSystemRepositoryMock, userBusinessServiceMock, fileSystemTypeRepositoryMock, mongoTemplateMock);

    @Test
    void getFolderContentsByPathThrows() {
        String notValid = "";
        String wrongFormat = "asd";
        String wrongFormat1 = "as/d";
        String validPath = "/uga/uga/as/sasda/sassasd";

        User dummyUser = User.builder().userId(0).build();

        FileSystemContentsNotAccessibleException ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(notValid, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix() + " Path was not valid.", ex.getMessage());

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(wrongFormat, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix() + " Path was in wrong format.", ex.getMessage());

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(wrongFormat1, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix() + " Path was in wrong format. Use a leading backslash.", ex.getMessage());

        when(fileSystemRepositoryMock.findByPath(validPath)).thenReturn(null);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());

        ArrayList<FileSystemEntity> fileSystemEntityArrayList = new ArrayList<>();
        fileSystemEntityArrayList.add(FileSystemEntity.builder().isFile(true).build());
        fileSystemEntityArrayList.add(FileSystemEntity.builder().isFile(false).typeId(-1).build());
        fileSystemEntityArrayList.add(FileSystemEntity.builder().createdByUserId(420).build());

        when(fileSystemRepositoryMock.findByPath(validPath)).thenReturn(fileSystemEntityArrayList);

        ex = assertThrows(FileSystemContentsNotAccessibleException.class, () ->
                fileSystemBusinessService.getFolderContentsByPath(validPath, dummyUser));
        assertEquals(FileSystemContentsNotAccessibleException.getErrorMessagePrefix(), ex.getMessage());
    }

    @Test
    void getFolderContentsByPathWorks() {
        String path = "/uga/buga/buga";
        String pathToRequest = path + "/";
        long userId = 420;
        long fileIdInFolder = 123;
        User user = User.builder().userId(userId).build();
        FileSystemEntity foundFolder = FileSystemEntity.builder().createdByUserId(userId).typeId(0).itemIds(new long[]{fileIdInFolder}).build();
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(foundFolder);

        when(fileSystemRepositoryMock.findByPath(path)).thenReturn(entities);
        when(fileSystemRepositoryMock.findByFileSystemId(fileIdInFolder)).thenReturn(FileSystemEntity.builder().createdByUserId(userId).build());
        when(userBusinessServiceMock.getUserById(userId)).thenReturn(User.builder().build());

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(pathToRequest, user);
        assertEquals(1, fileSystemItems.size());
    }

    @Test
    void getFolderContentsOfEntityThrows() {
        long fileSystemId0 = 420;
        long fileSystemId1 = 1234;

        User authenticatedUser = User.builder().build();
        FileSystemEntity rootFolder = FileSystemEntity.builder().itemIds(new long[]{fileSystemId0, fileSystemId1}).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId0)).thenReturn(FileSystemEntity.builder().build());
        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId1)).thenReturn(null);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, true, false));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " FolderContents expected fileSystemItem with id " + fileSystemId1 + " but was empty.", ex.getMessage());
    }

    @Test
    void getFolderContentsOfEntityWorks() {
        long fileSystemId0 = 420;
        long fileSystemId1 = 1234;
        long fileSystemId2 = 1231231234;
        long userId = 123123321;

        User authenticatedUser = User.builder().userId(userId).build();

        FileSystemEntity fileSystemEntity0 = FileSystemEntity.builder().visibleForUserIds(new long[]{userId}).build();
        FileSystemEntity fileSystemEntity1 = FileSystemEntity.builder().editableForUserIds(new long[]{userId}).build();
        FileSystemEntity fileSystemEntity2 = FileSystemEntity.builder().createdByUserId(userId).build();

        FileSystemEntity rootFolder = FileSystemEntity.builder().itemIds(new long[]{fileSystemId0, fileSystemId1, fileSystemId2}).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId0)).thenReturn(fileSystemEntity0);
        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId1)).thenReturn(fileSystemEntity1);
        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId2)).thenReturn(fileSystemEntity2);

        ArrayList<FileSystemEntity> fs0 = (ArrayList<FileSystemEntity>) fileSystemBusinessService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, true, false);
        ArrayList<FileSystemEntity> fs1 = (ArrayList<FileSystemEntity>) fileSystemBusinessService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, false, true);
        ArrayList<FileSystemEntity> fs2 = (ArrayList<FileSystemEntity>) fileSystemBusinessService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, true, true);
        ArrayList<FileSystemEntity> fs3 = (ArrayList<FileSystemEntity>) fileSystemBusinessService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, false, false);

        assertEquals(2, fs0.size());
        assertEquals(fileSystemEntity0, fs0.get(0));
        assertEquals(2, fs1.size());
        assertEquals(fileSystemEntity1, fs1.get(0));
        assertEquals(1, fs2.size());
        assertEquals(3, fs3.size());
        // why can't I compare 3 objects at once :(
        assertNotEquals(fs3.get(0), fs3.get(1));
        assertNotEquals(fs3.get(1), fs3.get(2));
        assertNotEquals(fs3.get(0), fs3.get(2));
    }

    @Test
    void sumUpAllPermissionsOfFileSystemEntitiesWorks() {
        FileSystemEntity fileSystemEntity0 = FileSystemEntity.builder().visibleForUserIds(new long[]{0, 2, 4}).visibleForGroupIds(new long[]{0, 2, 4}).editableForUserIds(new long[]{0, 2, 4}).editableFoGroupIds(new long[]{0, 2, 4}).build();
        FileSystemEntity fileSystemEntity1 = FileSystemEntity.builder().visibleForUserIds(new long[]{1, 2, 3, 4}).visibleForGroupIds(new long[]{1, 2, 3, 4}).editableForUserIds(new long[]{1, 2, 3, 4}).editableFoGroupIds(new long[]{1, 2, 3, 4}).build();
        FileSystemEntity fileSystemEntity2 = FileSystemEntity.builder().visibleForUserIds(new long[]{1, 3}).visibleForGroupIds(new long[]{1, 3}).editableForUserIds(new long[]{1, 3}).editableFoGroupIds(new long[]{1, 3}).build();
        FileSystemEntity fileSystemEntity3 = FileSystemEntity.builder().visibleForUserIds(new long[]{2, 4}).visibleForGroupIds(new long[]{2, 4}).editableForUserIds(new long[]{2, 4}).editableFoGroupIds(new long[]{2, 4}).build();

        FileSystemEntity parentFileSystemEntity = FileSystemEntity.builder().visibleForUserIds(new long[]{-10, -99, 9}).build();
        ArrayList<FileSystemEntity> fileSystemEntityArrayList = new ArrayList<>();
        fileSystemEntityArrayList.add(fileSystemEntity0);
        fileSystemEntityArrayList.add(fileSystemEntity1);
        fileSystemEntityArrayList.add(fileSystemEntity2);
        fileSystemEntityArrayList.add(fileSystemEntity3);

        FileSystemEntity actualFileSystemEntity = fileSystemBusinessService.sumUpAllPermissionsOfFileSystemEntities(parentFileSystemEntity, fileSystemEntityArrayList);
        assertEquals(5, actualFileSystemEntity.getVisibleForUserIds().length);
        assertEquals(5, actualFileSystemEntity.getVisibleForGroupIds().length);
        assertEquals(5, actualFileSystemEntity.getEditableForUserIds().length);
        assertEquals(5, actualFileSystemEntity.getEditableFoGroupIds().length);
    }

    @Test
    void deleteFileSystemItemByIdThrows() {
        long fsItemId = 12332123;
        long userId = 123123123;
        User authenticatedUser = User.builder().userId(userId).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(null);
        FileSystemItemCouldNotBeDeletedException ex = assertThrows(FileSystemItemCouldNotBeDeletedException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser));
        assertEquals(FileSystemItemCouldNotBeDeletedException.getErrorMessagePrefix() + " FileSystemId was " + fsItemId, ex.getMessage());

        FileSystemEntity foundEntity = FileSystemEntity.builder().build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        ex = assertThrows(FileSystemItemCouldNotBeDeletedException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser));
        assertEquals(FileSystemItemCouldNotBeDeletedException.getErrorMessagePrefix() + " FileSystemId was " + fsItemId, ex.getMessage());

        foundEntity = FileSystemEntity.builder().createdByUserId(userId).isFile(true).typeId(0).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FileSystemType.FOLDER);
        FileFighterDataException ex1 = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " FileType was wrong. " + foundEntity, ex1.getMessage());

        long folderContentId = 13287132;
        FileSystemEntity folderContentEntity = FileSystemEntity.builder().createdByUserId(userId).isFile(true).typeId(0).build();
        when(fileSystemRepositoryMock.findByFileSystemId(folderContentId)).thenReturn(folderContentEntity);
        foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).createdByUserId(userId).itemIds(new long[]{folderContentId}).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        ex1 = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " FileType was wrong. " + folderContentEntity, ex1.getMessage());
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFile() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        FileSystemEntity foundEntity = FileSystemEntity.builder().typeId(1).isFile(true).createdByUserId(userId).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);
        verify(fileSystemRepositoryMock, times(1)).delete(foundEntity);
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFolder() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        FileSystemEntity foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).createdByUserId(userId).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FileSystemType.FOLDER);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).createdByUserId(userId).itemIds(new long[0]).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);
        verify(fileSystemRepositoryMock, times(2)).delete(foundEntity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteFileSystemItemByIdWorksWithFolderWhenAllItemsCanBeDeleted() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        long itemId0 = 1233212;
        long itemId1 = 9872317;
        long itemId2 = 1923847;
        FileSystemEntity fileSystemEntity0 = FileSystemEntity.builder().isFile(false).typeId(0).createdByUserId(userId).fileSystemId(itemId0).build();
        FileSystemEntity fileSystemEntity1 = FileSystemEntity.builder().fileSystemId(itemId1).createdByUserId(userId).isFile(true).build();
        FileSystemEntity fileSystemEntity2 = FileSystemEntity.builder().fileSystemId(itemId2).createdByUserId(userId).isFile(true).build();
        FileSystemEntity foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).createdByUserId(userId).itemIds(new long[]{itemId0, itemId1, itemId2}).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FileSystemType.FOLDER);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId0)).thenReturn(fileSystemEntity0);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId1)).thenReturn(fileSystemEntity1);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId2)).thenReturn(fileSystemEntity2);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        verify(fileSystemRepositoryMock, times(1)).delete(foundEntity);
        verify(fileSystemRepositoryMock, times(1)).delete(fileSystemEntity0); // empty folder

        // https://stackoverflow.com/questions/11802801/using-mockito-how-do-i-verify-a-method-was-a-called-with-a-certain-argument
        ArgumentCaptor<ArrayList<FileSystemEntity>> savedCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(fileSystemRepositoryMock).deleteAll(savedCaptor.capture());
        assertTrue(savedCaptor.getValue().contains(fileSystemEntity1));
        assertTrue(savedCaptor.getValue().contains(fileSystemEntity2));
        assertEquals(2, savedCaptor.getValue().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteFileSystemItemByIdWorksWithFolderWhenSomeItemsCannotBeDeleted() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        long itemId0 = 1233212;
        long itemId1 = 9872317;
        long itemId2 = 1923847;
        long itemId3 = 9817232;
        FileSystemEntity foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).createdByUserId(userId).itemIds(new long[]{itemId0, itemId1, itemId2, itemId3}).build();
        FileSystemEntity visibleEditableEmptyFolder = FileSystemEntity.builder().isFile(false).typeId(0).createdByUserId(userId).fileSystemId(itemId0).build();
        FileSystemEntity invisibleFile = FileSystemEntity.builder().fileSystemId(itemId1).isFile(true).build();
        FileSystemEntity visibleNonEditableFile = FileSystemEntity.builder().fileSystemId(itemId2).visibleForUserIds(new long[]{userId}).isFile(true).build();
        FileSystemEntity visibleEditableFile = FileSystemEntity.builder().fileSystemId(itemId3).createdByUserId(userId).isFile(true).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FileSystemType.FOLDER);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(-1)).thenReturn(FileSystemType.UNDEFINED);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId0)).thenReturn(visibleEditableEmptyFolder);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId1)).thenReturn(invisibleFile);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId2)).thenReturn(visibleNonEditableFile);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId3)).thenReturn(visibleEditableFile);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        // verify deleted entities.
        ArgumentCaptor<ArrayList<FileSystemEntity>> arrayListArgumentCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(fileSystemRepositoryMock, times(1)).deleteAll(arrayListArgumentCaptor.capture());
        assertTrue(arrayListArgumentCaptor.getValue().contains(visibleEditableFile));
        assertEquals(1, arrayListArgumentCaptor.getValue().size());

        ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, times(1)).findAndModify(any(), updateArgumentCaptor.capture(), any());
        assertEquals("{ \"$set\" : { \"itemIds\" : [ " + itemId1 + ", " + itemId2 + " ] } }", updateArgumentCaptor.getValue().toString()); // no better way to assert requested changes.
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteFileSystemItemByIdWorksWithFolderOnlyInvisible() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        long itemId0 = 1233212;
        long itemId1 = 9872317;
        long itemId2 = 1923847;
        FileSystemEntity foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).createdByUserId(userId).itemIds(new long[]{itemId0, itemId1, itemId2}).build(); // TODO: implement this edge case. (created by.)
        FileSystemEntity visibleEditableEmptyFolder = FileSystemEntity.builder().isFile(false).typeId(0).createdByUserId(userId).fileSystemId(itemId0).build();
        FileSystemEntity invisibleFile = FileSystemEntity.builder().fileSystemId(itemId1).isFile(true).visibleForUserIds(new long[]{userId - 1}).build();
        FileSystemEntity visibleEditableFile = FileSystemEntity.builder().fileSystemId(itemId2).createdByUserId(userId).isFile(true).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FileSystemType.FOLDER);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(-1)).thenReturn(FileSystemType.UNDEFINED);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId0)).thenReturn(visibleEditableEmptyFolder);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId1)).thenReturn(invisibleFile);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId2)).thenReturn(visibleEditableFile);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        // verify deleted entities.
        ArgumentCaptor<ArrayList<FileSystemEntity>> arrayListArgumentCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(fileSystemRepositoryMock, times(1)).deleteAll(arrayListArgumentCaptor.capture());
        assertTrue(arrayListArgumentCaptor.getValue().contains(visibleEditableFile));
        assertEquals(1, arrayListArgumentCaptor.getValue().size());
        verify(fileSystemRepositoryMock, times(1)).delete(visibleEditableEmptyFolder);

        ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, times(1)).findAndModify(any(), updateArgumentCaptor.capture(), any());
        assertEquals("{ \"$set\" : { \"itemIds\" : [ " + itemId1 + " ], \"visibleForUserIds\" : [ " + (userId - 1) + " ], \"visibleForGroupIds\" : [  ], \"editableForUserIds\" : [  ], \"editableForGroupIds\" : [  ] } }", updateArgumentCaptor.getValue().toString()); // no better way to assert requested changes.
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
        FileSystemEntity entity = FileSystemEntity.builder().name(name).createdByUserId(userId).build();

        when(userBusinessServiceMock.getUserById(userId)).thenReturn(dummyUser);
        when(fileSystemRepositoryMock.findByFileSystemId(id)).thenReturn(entity);
        FileSystemItem fileSystemItem = fileSystemBusinessService.getFileSystemItemInfo(id, dummyUser);
        assertEquals(name, fileSystemItem.getName());
        assertEquals(userId, fileSystemItem.getCreatedByUser().getUserId());
        assertNull(fileSystemItem.getPath());
        assertFalse(fileSystemItem.isShared());
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
        user = User.builder().userId(123).groups(new Groups[]{Groups.UNDEFINED}).build();
        fileSystemEntity = FileSystemEntity.builder().createdByUserId(321).visibleForGroupIds(new long[]{1}).build();
        assertFalse(fileSystemBusinessService.userIsAllowedToSeeFileSystemEntity(fileSystemEntity, user));
    }

    @Test
    void userIsAllowedToEditFileSystemEntity() {
        long userId = 1232783672;
        User user = User.builder().userId(userId).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().createdByUserId(userId).build();

        // user created fileSystemItem
        assertTrue(fileSystemBusinessService.userIsAllowedToEditFileSystemEntity(fileSystemEntity, user));

        // user got it shared.
        fileSystemEntity = FileSystemEntity.builder().editableForUserIds(new long[]{userId}).build();
        assertTrue(fileSystemBusinessService.userIsAllowedToEditFileSystemEntity(fileSystemEntity, user));

        //user is in group
        user = User.builder().userId(0).groups(new Groups[]{Groups.ADMIN}).build();
        fileSystemEntity = FileSystemEntity.builder().editableFoGroupIds(new long[]{1}).build();
        assertTrue(fileSystemBusinessService.userIsAllowedToEditFileSystemEntity(fileSystemEntity, user));

        // user is not allowed.
        user = User.builder().userId(123).groups(new Groups[]{Groups.UNDEFINED}).build();
        fileSystemEntity = FileSystemEntity.builder().createdByUserId(321).editableFoGroupIds(new long[]{1}).build();
        assertFalse(fileSystemBusinessService.userIsAllowedToEditFileSystemEntity(fileSystemEntity, user));
    }

    @Test
    void createDTOThrows() {
        long userId = 420;
        FileSystemEntity entity = FileSystemEntity.builder().createdByUserId(userId).build();
        User user = User.builder().build();

        when(userBusinessServiceMock.getUserById(userId)).thenThrow(UserNotFoundException.class);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.createDTO(entity, user, null));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Owner of a file could not be found.", ex.getMessage());
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

        when(userBusinessServiceMock.getUserById(createdByUserId)).thenReturn(userThatCreatedFile);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(typeId)).thenReturn(FileSystemType.UNDEFINED);

        FileSystemItem actual = fileSystemBusinessService.createDTO(fileSystemEntity, authenticatedUser, basePath);

        assertEquals(createdByUserId, actual.getCreatedByUser().getUserId());
        assertEquals(fileSystemId, actual.getFileSystemId());
        assertEquals(lastUpdated, actual.getLastUpdated());
        assertEquals(name, actual.getName());
        assertEquals(size, actual.getSize());
        assertEquals(FileSystemType.UNDEFINED, actual.getType());
        assertEquals(basePath + name, actual.getPath());
        assertTrue(actual.isShared());
    }

    @Test
    void getTotalFileSizeThrows() {
        when(fileSystemRepositoryMock.findByPath("/")).thenReturn(null);
        FileFighterDataException ex = assertThrows(FileFighterDataException.class, fileSystemBusinessService::getTotalFileSize);
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Couldn't find any Home directories!", ex.getMessage());
    }

    @Test
    void getTotalFileSizeWorks() {
        double size0 = 1.3;
        double size1 = 2.4;
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(FileSystemEntity.builder().size(size0).build());
        entities.add(FileSystemEntity.builder().size(size1).build());

        when(fileSystemRepositoryMock.findByPath("/")).thenReturn(entities);

        double actualSize = fileSystemBusinessService.getTotalFileSize();
        assertEquals(size0 + size1, actualSize);
    }
}