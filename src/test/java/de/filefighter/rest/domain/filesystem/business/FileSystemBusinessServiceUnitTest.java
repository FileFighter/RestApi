package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;

import static de.filefighter.rest.domain.filesystem.type.FileSystemType.FOLDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FileSystemBusinessServiceUnitTest {

    private final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
    private UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private final FileSystemTypeRepository fileSystemTypeRepositoryMock = mock(FileSystemTypeRepository.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private final FileSystemHelperService fileSystemHelperServiceMock = mock(FileSystemHelperService.class);

    private final FileSystemBusinessService fileSystemBusinessService = new FileSystemBusinessService(fileSystemRepositoryMock, fileSystemHelperServiceMock, fileSystemTypeRepositoryMock, userBusinessServiceMock, mongoTemplateMock);

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
    void getFolderContentsByPathWorks() {
        String path = "/uga/buga/buga";
        String pathToRequest = path + "/";
        long userId = 420;
        long fileIdInFolder = 123;
        User user = User.builder().userId(userId).build();
        FileSystemEntity foundFolder = FileSystemEntity.builder().isFile(false).lastUpdatedBy(userId).typeId(0).itemIds(new long[]{fileIdInFolder}).build();
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(foundFolder);

        when(fileSystemHelperServiceMock.removeTrailingBackSlashes(pathToRequest)).thenReturn(path);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundFolder, user, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(foundFolder, user, true, false)).thenReturn(entities);
        when(fileSystemHelperServiceMock.createDTO(foundFolder, user, pathToRequest)).thenReturn(FileSystemItem.builder().build());
        when(fileSystemRepositoryMock.findByPath(path)).thenReturn(entities);
        when(fileSystemRepositoryMock.findByFileSystemId(fileIdInFolder)).thenReturn(FileSystemEntity.builder().lastUpdatedBy(userId).build());
        when(userBusinessServiceMock.getUserById(userId)).thenReturn(User.builder().build());

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(pathToRequest, user);
        assertEquals(1, fileSystemItems.size());
    }

    @Test
    void recursivelyDeleteFileSystemEntityThrows() {
        long fsItemId = 12301231;
        long fileSystemId0 = 420;

        User authenticatedUser = User.builder().build();

        FileSystemEntity rootFile = FileSystemEntity.builder().fileSystemId(fsItemId).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(rootFile);
        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemEntity(rootFile, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Failed to delete FileSystemEntity with id " + fsItemId, ex.getMessage());

        FileSystemEntity rootFolder = FileSystemEntity.builder().fileSystemId(fsItemId).isFile(false).typeId(0).build();
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, false, false)).thenReturn(new ArrayList<>());
        ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemEntity(rootFolder, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Failed to delete FileSystemEntity with id " + fsItemId, ex.getMessage());

        FileSystemEntity rootFolder1 = FileSystemEntity.builder().fileSystemId(fsItemId).isFile(false).typeId(0).itemIds(new long[]{fileSystemId0}).build();
        ArrayList<FileSystemEntity> arrayListMock = new ArrayList<>(1);
        FileSystemEntity fileInRootFolder = FileSystemEntity.builder().fileSystemId(fileSystemId0).build();
        arrayListMock.add(fileInRootFolder);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(rootFolder1, authenticatedUser, false, false)).thenReturn(arrayListMock);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileInRootFolder, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileInRootFolder, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId0)).thenReturn(fileInRootFolder);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);

        ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemEntity(rootFolder1, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Failed to delete FileSystemEntity with id " + fileSystemId0, ex.getMessage());

        when(fileSystemRepositoryMock.deleteByFileSystemId(fileSystemId0)).thenReturn(1L);
        ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemEntity(rootFolder1, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Failed to delete FileSystemEntity with id " + fsItemId, ex.getMessage());
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

        foundEntity = FileSystemEntity.builder().lastUpdatedBy(userId).isFile(true).typeId(0).build();
        when((fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.READ))).thenReturn(true);
        when((fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.DELETE))).thenReturn(true);
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);
        FileFighterDataException ex1 = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " FileType was wrong. " + foundEntity, ex1.getMessage());

        long folderContentId = 13287132;
        FileSystemEntity folderContentEntity = FileSystemEntity.builder().lastUpdatedBy(userId).isFile(true).typeId(0).build();
        ArrayList<FileSystemEntity> fileSystemEntitiesMock = new ArrayList<>();
        fileSystemEntitiesMock.add(folderContentEntity);
        when(fileSystemRepositoryMock.findByFileSystemId(folderContentId)).thenReturn(folderContentEntity);
        foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).lastUpdatedBy(userId).itemIds(new long[]{folderContentId}).build();
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(foundEntity, authenticatedUser, false, false)).thenReturn(fileSystemEntitiesMock);
        ex1 = assertThrows(FileFighterDataException.class, () ->
                fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " FileType was wrong. " + folderContentEntity, ex1.getMessage());
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFile() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        FileSystemEntity foundEntity = FileSystemEntity.builder().fileSystemId(fsItemId).typeId(1).isFile(true).lastUpdatedBy(userId).build();
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemRepositoryMock.deleteByFileSystemId(fsItemId)).thenReturn(1L);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(fsItemId);
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFolder() {
        long folderId = 12332123;
        long fileId = 420;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        FileSystemEntity folder = FileSystemEntity.builder().fileSystemId(folderId).typeId(0).isFile(false).lastUpdatedBy(userId).build();

        when(fileSystemRepositoryMock.findByFileSystemId(folderId)).thenReturn(folder);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);
        when(fileSystemRepositoryMock.deleteByFileSystemId(folderId)).thenReturn(1L);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(folder, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(folder, authenticatedUser, InteractionType.DELETE)).thenReturn(true);

        FileSystemEntity file = FileSystemEntity.builder().typeId(2).fileSystemId(fileId).isFile(true).lastUpdatedBy(userId).itemIds(new long[0]).build();
        ArrayList<FileSystemEntity> foundFiles = new ArrayList<>();
        foundFiles.add(file);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(folder, authenticatedUser, false, false)).thenReturn(foundFiles);

        when(fileSystemRepositoryMock.findByFileSystemId(fileId)).thenReturn(file);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(file, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(file, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(2)).thenReturn(FileSystemType.VIDEO);
        when(fileSystemRepositoryMock.deleteByFileSystemId(fileId)).thenReturn(1L);

        fileSystemBusinessService.deleteFileSystemItemById(folderId, authenticatedUser);
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(folderId);
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(fileId);
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFolderWhenAllItemsCanBeDeleted() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        long itemId0 = 1233212;
        long itemId1 = 9872317;
        long itemId2 = 1923847;
        FileSystemEntity fileSystemEntity0 = FileSystemEntity.builder().isFile(false).typeId(0).lastUpdatedBy(userId).fileSystemId(itemId0).build();
        FileSystemEntity fileSystemEntity1 = FileSystemEntity.builder().fileSystemId(itemId1).typeId(1).lastUpdatedBy(userId).isFile(true).build();
        FileSystemEntity fileSystemEntity2 = FileSystemEntity.builder().fileSystemId(itemId2).typeId(1).lastUpdatedBy(userId).isFile(true).build();
        FileSystemEntity parentFolder = FileSystemEntity.builder().typeId(0).isFile(false).fileSystemId(fsItemId).lastUpdatedBy(userId).itemIds(new long[]{itemId0, itemId1, itemId2}).build();

        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);
        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(parentFolder);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId0)).thenReturn(fileSystemEntity0);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId1)).thenReturn(fileSystemEntity1);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId2)).thenReturn(fileSystemEntity2);

        ArrayList<FileSystemEntity> contentList = new ArrayList<>();
        contentList.add(fileSystemEntity0);
        contentList.add(fileSystemEntity1);
        contentList.add(fileSystemEntity2);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(parentFolder, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(parentFolder, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity0, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity0, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity1, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity1, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity2, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity2, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(parentFolder, authenticatedUser, false, false)).thenReturn(contentList);

        when(fileSystemRepositoryMock.deleteByFileSystemId(fsItemId)).thenReturn(1L);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId0)).thenReturn(1L);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId1)).thenReturn(1L);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId2)).thenReturn(1L);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(fsItemId);
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(itemId0); // empty folder
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(itemId1);
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(itemId2);
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFolderWhenSomeItemsCannotBeDeleted() {
        long fsItemId = 12332123;
        long userId = 243724328;
        long itemId0 = 1233212;
        long itemId1 = 9872317;
        long itemId2 = 1923847;
        long itemId3 = 9817232;
        User authenticatedUser = User.builder().userId(userId).build();
        FileSystemEntity foundFolder = FileSystemEntity.builder().typeId(0).isFile(false).lastUpdatedBy(userId).itemIds(new long[]{itemId0, itemId1, itemId2, itemId3}).build();
        FileSystemEntity visibleEditableEmptyFolder = FileSystemEntity.builder().isFile(false).typeId(0).lastUpdatedBy(userId).fileSystemId(itemId0).build();
        FileSystemEntity invisibleFile = FileSystemEntity.builder().fileSystemId(itemId1).isFile(true).build();
        FileSystemEntity visibleNonEditableFile = FileSystemEntity.builder().fileSystemId(itemId2).visibleForUserIds(new long[]{userId}).isFile(true).build();
        FileSystemEntity visibleEditableFile = FileSystemEntity.builder().fileSystemId(itemId3).lastUpdatedBy(userId).isFile(true).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundFolder);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundFolder, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundFolder, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(visibleEditableEmptyFolder, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(visibleEditableEmptyFolder, authenticatedUser, InteractionType.DELETE)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(visibleNonEditableFile, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(visibleNonEditableFile, authenticatedUser, InteractionType.DELETE)).thenReturn(false);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(visibleEditableFile, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(visibleEditableFile, authenticatedUser, InteractionType.DELETE)).thenReturn(true);

        ArrayList<FileSystemEntity> foundEntities = new ArrayList<>();
        foundEntities.add(visibleEditableFile);
        foundEntities.add(invisibleFile);
        foundEntities.add(visibleNonEditableFile);
        foundEntities.add(visibleEditableEmptyFolder);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(foundFolder, authenticatedUser, false, false)).thenReturn(foundEntities);
        when(fileSystemHelperServiceMock.sumUpAllPermissionsOfFileSystemEntities(any(), any())).thenReturn(foundFolder);

        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(-1)).thenReturn(FileSystemType.UNDEFINED);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId0)).thenReturn(visibleEditableEmptyFolder);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId1)).thenReturn(invisibleFile);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId2)).thenReturn(visibleNonEditableFile);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId3)).thenReturn(visibleEditableFile);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId0)).thenReturn(1L);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId3)).thenReturn(1L);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        // verify deleted entities.
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(itemId0);
        verify(fileSystemRepositoryMock, times(1)).deleteByFileSystemId(itemId3);

        ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplateMock, times(1)).findAndModify(any(), updateArgumentCaptor.capture(), any());
        assertEquals("{ \"$set\" : { \"itemIds\" : [ " + itemId1 + ", " + itemId2 + " ] } }", updateArgumentCaptor.getValue().toString()); // no better way to assert requested changes.
    }

    @Test
    void deleteFileSystemItemByIdWorksWithFolderOnlyInvisible() {
        long fsItemId = 12332123;
        long userId = 243724328;
        User authenticatedUser = User.builder().userId(userId).build();
        long itemId0 = 1233212;
        long itemId1 = 9872317;
        long itemId2 = 1923847;
        FileSystemEntity foundEntity = FileSystemEntity.builder().typeId(0).isFile(false).fileSystemId(fsItemId).itemIds(new long[]{itemId0, itemId1, itemId2}).build();
        FileSystemEntity visibleEditableEmptyFolder = FileSystemEntity.builder().isFile(false).typeId(0).fileSystemId(itemId0).build();
        FileSystemEntity invisibleFile = FileSystemEntity.builder().fileSystemId(itemId1).isFile(true).visibleForUserIds(new long[]{userId - 1}).build();
        FileSystemEntity visibleEditableFile = FileSystemEntity.builder().fileSystemId(itemId2).isFile(true).build();

        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(foundEntity, authenticatedUser, InteractionType.DELETE)).thenReturn(true);

        ArrayList<FileSystemEntity> foundEntities = new ArrayList<>();
        foundEntities.add(visibleEditableFile);
        foundEntities.add(invisibleFile);
        foundEntities.add(visibleEditableEmptyFolder);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(foundEntity, authenticatedUser, false, false)).thenReturn(foundEntities);

        when(fileSystemRepositoryMock.findByFileSystemId(fsItemId)).thenReturn(foundEntity);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(0)).thenReturn(FOLDER);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(-1)).thenReturn(FileSystemType.UNDEFINED);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId0)).thenReturn(visibleEditableEmptyFolder);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId1)).thenReturn(invisibleFile);
        when(fileSystemRepositoryMock.findByFileSystemId(itemId2)).thenReturn(visibleEditableFile);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId0)).thenReturn(1L);
        when(fileSystemRepositoryMock.deleteByFileSystemId(itemId2)).thenReturn(1L);

        FileSystemEntity entityWithSummedUpPermissions = FileSystemEntity.builder().build();
        entityWithSummedUpPermissions.setVisibleForUserIds(invisibleFile.getVisibleForUserIds());
        when(fileSystemHelperServiceMock.sumUpAllPermissionsOfFileSystemEntities(foundEntity, foundEntities)).thenReturn(entityWithSummedUpPermissions);
        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);

        // nothing should be deleted, but parent folder should be changed, so the user cannot see the "deleted" folder anymore.
        ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<Query> queryArgumentCaptor = ArgumentCaptor.forClass(Query.class);

        verify(mongoTemplateMock, times(1)).findAndModify(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(FileSystemEntity.class));
        assertEquals("Query: { \"fileSystemId\" : " + fsItemId + "}, Fields: {}, Sort: {}", queryArgumentCaptor.getValue().toString());
        assertEquals("{ \"$set\" : { \"itemIds\" : [ " + itemId0 + ", " + itemId1 + ", " + itemId2 + " ], \"visibleForUserIds\" : [ " + (userId - 1) + " ], \"visibleForGroupIds\" : [  ], \"editableForUserIds\" : [  ], \"editableForGroupIds\" : [  ] } }", updateArgumentCaptor.getValue().toString()); // no better way to assert requested changes.
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