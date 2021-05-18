package de.filefighter.rest.domain.filesystem.business;


import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.Group;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

import static de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService.DELETION_FAILED_MSG;
import static de.filefighter.rest.domain.filesystem.data.InteractionType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FileSystemHelperServiceUnitTest {

    private final UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
    private final FileSystemTypeRepository fileSystemTypeRepositoryMock = mock(FileSystemTypeRepository.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private final IdGenerationService idGenerationServiceMock = mock(IdGenerationService.class);

    private final FileSystemHelperService fileSystemHelperService = new FileSystemHelperService(
            fileSystemRepositoryMock,
            fileSystemTypeRepositoryMock,
            userBusinessServiceMock,
            mongoTemplateMock,
            idGenerationServiceMock);

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

        FileSystemEntity actualFileSystemEntity = fileSystemHelperService.sumUpAllPermissionsOfFileSystemEntities(parentFileSystemEntity, fileSystemEntityArrayList);
        assertEquals(5, actualFileSystemEntity.getVisibleForUserIds().length);
        assertEquals(5, actualFileSystemEntity.getVisibleForGroupIds().length);
        assertEquals(5, actualFileSystemEntity.getEditableForUserIds().length);
        assertEquals(5, actualFileSystemEntity.getEditableFoGroupIds().length);
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
                fileSystemHelperService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, true, false));
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
        FileSystemEntity fileSystemEntity2 = FileSystemEntity.builder().ownerId(userId).build();

        FileSystemEntity rootFolder = FileSystemEntity.builder().itemIds(new long[]{fileSystemId0, fileSystemId1, fileSystemId2}).build();

        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId0)).thenReturn(fileSystemEntity0);
        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId1)).thenReturn(fileSystemEntity1);
        when(fileSystemRepositoryMock.findByFileSystemId(fileSystemId2)).thenReturn(fileSystemEntity2);

        ArrayList<FileSystemEntity> fs0 = (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, true, false);
        ArrayList<FileSystemEntity> fs1 = (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, false, true);
        ArrayList<FileSystemEntity> fs2 = (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, true, true);
        ArrayList<FileSystemEntity> fs3 = (ArrayList<FileSystemEntity>) fileSystemHelperService.getFolderContentsOfEntityAndPermissions(rootFolder, authenticatedUser, false, false);

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
    void removeTrailingWhiteSpaces() {
        String doesNotRemove0 = "/";
        String doesNotRemove1 = "/ugabuga";
        String doesRemove = "/uga/";
        String removed = "/uga";


        String actual0 = fileSystemHelperService.removeTrailingBackSlashes(doesNotRemove0);
        assertEquals(doesNotRemove0, actual0);

        String actual1 = fileSystemHelperService.removeTrailingBackSlashes(doesNotRemove1);
        assertEquals(doesNotRemove1, actual1);

        String actual2 = fileSystemHelperService.removeTrailingBackSlashes(doesRemove);
        assertEquals(removed, actual2);
    }

    @Test
    void userIsAllowedToReadFileSystemEntity() {
        long userId = 1232783672;
        User user = User.builder().userId(userId).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().ownerId(userId).build();

        // user created fileSystemItem
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, READ));

        // user created containing folder
        fileSystemEntity.setLastUpdatedBy(1203891230);
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, READ));

        // user got it shared.
        fileSystemEntity = FileSystemEntity.builder().visibleForUserIds(new long[]{userId}).build();
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, READ));

        //user is in group
        user = User.builder().userId(123897123).groups(new Group[]{Group.ADMIN}).build();
        fileSystemEntity = FileSystemEntity.builder().fileSystemId(9872347).visibleForGroupIds(new long[]{1}).build();
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, READ));

        // user is not allowed.
        user = User.builder().userId(123).groups(new Group[]{Group.UNDEFINED}).build();
        fileSystemEntity = FileSystemEntity.builder().lastUpdatedBy(321).visibleForGroupIds(new long[]{1}).build();
        assertFalse(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, READ));
    }

    @Test
    void userIsAllowedToEditFileSystemEntity() {
        long userId = 1232783672;
        User user = User.builder().userId(userId).build();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().ownerId(userId).build();

        // fileSystemEntity was created by runtime user.
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(FileSystemEntity.builder().lastUpdatedBy(RestConfiguration.RUNTIME_USER_ID).editableForUserIds(new long[]{userId}).build(), user, CHANGE));

        assertFalse(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(FileSystemEntity.builder().lastUpdatedBy(RestConfiguration.RUNTIME_USER_ID).editableForUserIds(new long[]{userId}).build(), user, DELETE));

        // user created fileSystemItem
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, CHANGE));

        // user created containing folder
        fileSystemEntity.setLastUpdatedBy(1203891230);
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, CHANGE));

        // user got it shared.
        fileSystemEntity = FileSystemEntity.builder().editableForUserIds(new long[]{userId}).build();
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, CHANGE));

        //user is in group
        user = User.builder().userId(0).groups(new Group[]{Group.ADMIN}).build();
        fileSystemEntity = FileSystemEntity.builder().editableFoGroupIds(new long[]{1}).build();
        assertTrue(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, CHANGE));

        // user is not allowed.
        user = User.builder().userId(123).groups(new Group[]{Group.UNDEFINED}).build();
        fileSystemEntity = FileSystemEntity.builder().lastUpdatedBy(321).editableFoGroupIds(new long[]{1}).build();
        assertFalse(fileSystemHelperService.userIsAllowedToInteractWithFileSystemEntity(fileSystemEntity, user, CHANGE));
    }

    @Test
    void createDTOThrows() {
        long userId = 420;
        FileSystemEntity entity = FileSystemEntity.builder().lastUpdatedBy(userId).build();
        User user = User.builder().build();

        when(userBusinessServiceMock.findUserById(userId)).thenThrow(UserNotFoundException.class);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemHelperService.createDTO(entity, user, null));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Owner or auther of last change could not be found.", ex.getMessage());
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
                .ownerId(createdByUserId)
                .itemIds(items)
                .fileSystemId(fileSystemId)
                .isFile(isFile)
                .lastUpdatedBy(createdByUserId)
                .lastUpdated(lastUpdated)
                .name(name)
                .path("") // is empty because its a file.
                .size(size)
                .typeId(typeId)
                .build();

        when(userBusinessServiceMock.findUserById(createdByUserId)).thenReturn(userThatCreatedFile);
        when(fileSystemTypeRepositoryMock.findFileSystemTypeById(typeId)).thenReturn(FileSystemType.UNDEFINED);

        FileSystemItem actual = fileSystemHelperService.createDTO(fileSystemEntity, authenticatedUser, basePath + name);

        assertEquals(createdByUserId, actual.getLastUpdatedBy().getUserId());
        assertEquals(fileSystemId, actual.getFileSystemId());
        assertEquals(lastUpdated, actual.getLastUpdated());
        assertEquals(name, actual.getName());
        assertEquals(size, actual.getSize());
        assertEquals(FileSystemType.UNDEFINED, actual.getType());
        assertEquals((basePath + name).toLowerCase(), actual.getPath());
        assertTrue(actual.isShared());
    }

    @Test
    void getTotalFileSizeThrows() {
        when(fileSystemRepositoryMock.findByPath("/")).thenReturn(null);
        FileFighterDataException ex = assertThrows(FileFighterDataException.class, fileSystemHelperService::getTotalFileSize);
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Couldn't find any Home directories!", ex.getMessage());
    }

    @Test
    void deleteAndUnbindFileSystemEntityThrows() {
        long fileSystemId = 420;
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder().fileSystemId(fileSystemId).build();

        when(fileSystemRepositoryMock.deleteByFileSystemId(fileSystemId)).thenReturn(1234L);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemHelperService.deleteAndUnbindFileSystemEntity(fileSystemEntity));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " " + DELETION_FAILED_MSG + fileSystemId, ex.getMessage());
    }

    @SuppressWarnings("squid:S5778")
    @Test
    void recursivlyUpdateTimeStampsThrows() {
        List<FileSystemEntity> entities = new ArrayList<>();
        entities.add(FileSystemEntity.builder().build());
        entities.add(FileSystemEntity.builder().build());
        when(mongoTemplateMock.find(any(), eq(FileSystemEntity.class))).thenReturn(entities);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemHelperService.recursivlyUpdateTimeStamps(FileSystemEntity.builder().build(), User.builder().build(), 420));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Found more than one parent entity for entity.", ex.getMessage());

        when(mongoTemplateMock.find(any(), any())).thenReturn(new ArrayList<>());

        ex = assertThrows(FileFighterDataException.class, () ->
                fileSystemHelperService.recursivlyUpdateTimeStamps(FileSystemEntity.builder().fileSystemId(123123).isFile(true).path("/").build(), User.builder().build(), 420));
        assertEquals(FileFighterDataException.getErrorMessagePrefix() + " Found no parent entity for a non root entity.", ex.getMessage());
    }

    @Test
    void removeVisibilityRightsFromEntityWorks() {
        long fsItemId = 123;
        long userId = 123123;
        long otherUserId = 120938;
        long groupId = Group.FAMILY.getGroupId();
        long otherGroupId = Group.ADMIN.getGroupId();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder()
                .fileSystemId(fsItemId)
                .visibleForGroupIds(new long[]{groupId, otherGroupId})
                .visibleForUserIds(new long[]{userId, otherUserId})
                .build();
        User authenticatedUser = User.builder()
                .userId(userId)
                .groups(new Group[]{Group.FAMILY})
                .build();

        // run it
        fileSystemHelperService.removeVisibilityRightsOfFileSystemEntityForUser(fileSystemEntity, authenticatedUser);

        // verify it
        ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        ArgumentCaptor<Query> queryArgumentCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplateMock, times(1)).findAndModify(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(FileSystemEntity.class));
        assertEquals("Query: { \"fileSystemId\" : " + fsItemId + "}, Fields: {}, Sort: {}", queryArgumentCaptor.getValue().toString());
        assertEquals("{ \"$set\" : { \"visibleForUserIds\" : [ " + otherUserId + " ], \"visibleForGroupIds\" : [ " + otherGroupId + " ] } }", updateArgumentCaptor.getValue().toString());
    }

    @Test
    void getParentPathFromPathWorks() {
        String path = "/baum/foo/bar/var/schmutz";
        assertEquals("/baum/foo/bar/var", fileSystemHelperService.getParentPathFromPath(path));

        path = "/baum";
        assertEquals("/", fileSystemHelperService.getParentPathFromPath(path));
    }

    @Test
    void recursivlyUpdateTimeStampsWorks() {
        long fsItemId = 420;
        long fsItemId2 = 1234;
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(FileSystemEntity.builder().fileSystemId(fsItemId2).isFile(false).path("/").build());
        ArrayList<FileSystemEntity> emptyList = new ArrayList<>();

        when(mongoTemplateMock.find(eq(new Query().addCriteria(Criteria.where("itemIds").is(fsItemId))), eq(FileSystemEntity.class))).thenReturn(entities);
        when(mongoTemplateMock.find(eq(new Query().addCriteria(Criteria.where("itemIds").is(fsItemId2))), eq(FileSystemEntity.class))).thenReturn(emptyList);

        fileSystemHelperService.recursivlyUpdateTimeStamps(FileSystemEntity.builder().fileSystemId(fsItemId).build(), User.builder().build(), 420);

        verify(mongoTemplateMock, times(2)).findAndModify(any(), any(), any());
    }

    @Test
    void getTotalFileSizeWorks() {
        double size0 = 1.3;
        double size1 = 2.4;
        ArrayList<FileSystemEntity> entities = new ArrayList<>();
        entities.add(FileSystemEntity.builder().size(size0).build());
        entities.add(FileSystemEntity.builder().size(size1).build());

        when(fileSystemRepositoryMock.findByPath("/")).thenReturn(entities);

        double actualSize = fileSystemHelperService.getTotalFileSize();
        assertEquals(size0 + size1, actualSize);
    }
}

