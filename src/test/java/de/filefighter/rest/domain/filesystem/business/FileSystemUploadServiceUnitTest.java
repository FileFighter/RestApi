package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.InteractionType;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.upload.CreateNewFolder;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.exceptions.FileSystemItemCouldNotBeUploadedException;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSystemUploadServiceUnitTest {

    private final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
    private final FileSystemHelperService fileSystemHelperServiceMock = mock(FileSystemHelperService.class);
    private final InputSanitizerService inputSanitizerServiceMock = mock(InputSanitizerService.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private final FileSystemTypeRepository fileSystemTypeRepositoryMock = mock(FileSystemTypeRepository.class);
    private final UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private final IdGenerationService idGenerationServiceMock = mock(IdGenerationService.class);

    private final FileSystemUploadService fileSystemUploadService = new FileSystemUploadService(fileSystemRepositoryMock, fileSystemHelperServiceMock, inputSanitizerServiceMock, fileSystemTypeRepositoryMock, mongoTemplateMock, userBusinessServiceMock, idGenerationServiceMock);

    @Test
    void createNewFolderThrows() {
        long parentId = 420;
        String folderName = "Kevin";
        CreateNewFolder createNewFolder = new CreateNewFolder(folderName);
        User autheticatedUser = User.builder().build();

        FileSystemItemCouldNotBeUploadedException ex = assertThrows(FileSystemItemCouldNotBeUploadedException.class,
                () -> fileSystemUploadService.createNewFolder(parentId, createNewFolder, autheticatedUser));
        assertEquals(FileSystemItemCouldNotBeUploadedException.getErrorMessagePrefix() + " Could not find parent entity or you are not allowed to see it.", ex.getMessage());

        FileSystemEntity parent = FileSystemEntity.builder().build();
        when(fileSystemRepositoryMock.findByFileSystemId(parentId)).thenReturn(parent);

        ex = assertThrows(FileSystemItemCouldNotBeUploadedException.class,
                () -> fileSystemUploadService.createNewFolder(parentId, createNewFolder, autheticatedUser));
        assertEquals(FileSystemItemCouldNotBeUploadedException.getErrorMessagePrefix() + " Could not find parent entity or you are not allowed to see it.", ex.getMessage());

        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(parent, autheticatedUser, InteractionType.READ)).thenReturn(true);

        ex = assertThrows(FileSystemItemCouldNotBeUploadedException.class,
                () -> fileSystemUploadService.createNewFolder(parentId, createNewFolder, autheticatedUser));
        assertEquals(FileSystemItemCouldNotBeUploadedException.getErrorMessagePrefix() + " You dont have write permissions in that directory.", ex.getMessage());

        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(parent, autheticatedUser, InteractionType.CHANGE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(parent, autheticatedUser, false, false)).thenReturn(Collections.singletonList(FileSystemEntity.builder().name(folderName.toUpperCase()).build()));

        ex = assertThrows(FileSystemItemCouldNotBeUploadedException.class,
                () -> fileSystemUploadService.createNewFolder(parentId, createNewFolder, autheticatedUser));
        assertEquals(FileSystemItemCouldNotBeUploadedException.getErrorMessagePrefix() + " A Entity with the same name already exists in this directory.", ex.getMessage());
    }

    @Test
    void createNewFolderWorks() {
        long parentId = 420;
        String folderName = "Kevin";
        CreateNewFolder createNewFolder = new CreateNewFolder(folderName);
        long userId = 420;
        User autheticatedUser = User.builder().build();

        FileSystemEntity parent = FileSystemEntity.builder().path("/parent").ownerId(userId).build();
        when(fileSystemRepositoryMock.findByFileSystemId(parentId)).thenReturn(parent);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(parent, autheticatedUser, InteractionType.READ)).thenReturn(true);
        when(fileSystemHelperServiceMock.userIsAllowedToInteractWithFileSystemEntity(parent, autheticatedUser, InteractionType.CHANGE)).thenReturn(true);
        when(fileSystemHelperServiceMock.getFolderContentsOfEntityAndPermissions(parent, autheticatedUser, false, false)).thenReturn(Collections.singletonList(FileSystemEntity.builder().name("a name").build()));
        when(userBusinessServiceMock.findUserById(userId)).thenReturn(User.builder().username(folderName).build());

        FileSystemItem item = FileSystemItem.builder().build();
        String path = "/" + folderName + "/parent/" + folderName.toLowerCase();
        when(fileSystemHelperServiceMock.createDTO(any(), eq(autheticatedUser), eq(path))).thenReturn(item);

        FileSystemItem actual = fileSystemUploadService.createNewFolder(parentId, createNewFolder, autheticatedUser);
        assertEquals(item, actual);
    }
}