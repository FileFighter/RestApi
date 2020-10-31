package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.*;
import de.filefighter.rest.rest.ServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSystemRestControllerUnitTest {

    private final FileSystemRestServiceInterface fileSystemRestServiceMock = mock(FileSystemRestService.class);
    private FileSystemRestController fileSystemRestController;

    @BeforeEach
    void setUp() {
        fileSystemRestController = new FileSystemRestController(fileSystemRestServiceMock);
    }

    @Test
    void getContentsOfFolder() {
        Folder dummyFolder = new Folder();
        File dummyFile = new File();
        EntityModel<FolderContents> expectedModel = EntityModel.of(FolderContents.builder()
                .files(new File[]{dummyFile})
                .folders(new Folder[]{dummyFolder}).create());

        String path= "/root/data.txt";
        String token = "token";

        when(fileSystemRestServiceMock.getContentsOfFolderByIdAndAccessToken(path, token)).thenReturn(expectedModel);

        EntityModel<FolderContents> actualModel = fileSystemRestController.getContentsOfFolder(path, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void getFileOrFolderInfo() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        long id = 420;
        String token = "token";

        when(fileSystemRestServiceMock.getInfoAboutFileOrFolderByIdAndAccessToken(id, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.getFileOrFolderInfo(id, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void searchFileOrFolderByName() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        String name = "randomFile.exe";
        String token = "token";

        when(fileSystemRestServiceMock.findFileOrFolderByNameAndAccessToken(name, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.searchFileOrFolderByName(name, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void uploadFileOrFolder() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        FileSystemItemUpdate fileSystemItemUpdate = FileSystemItemUpdate.create().name("ugabuga").build();
        String token = "token";

        when(fileSystemRestServiceMock.uploadFileSystemItemWithAccessToken(fileSystemItemUpdate, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.uploadFileOrFolder(fileSystemItemUpdate, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void updateExistingFileOrFolder() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        long id = 420L;
        FileSystemItemUpdate fileSystemItemUpdate = FileSystemItemUpdate.create().name("ugabuga").build();
        String token = "token";

        when(fileSystemRestServiceMock.updatedFileSystemItemWithIdAndAccessToken(id, fileSystemItemUpdate, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.updateExistingFileOrFolder(id, fileSystemItemUpdate, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void deleteFileOrFolder() {
        ServerResponse response = new ServerResponse("denied", "not authorized");
        EntityModel<ServerResponse> expectedModel = EntityModel.of(response);

        long id = 420;
        String token = "token";

        when(fileSystemRestServiceMock.deleteFileSystemItemWithIdAndAccessToken(id, token)).thenReturn(expectedModel);

        EntityModel<ServerResponse> actualModel = fileSystemRestController.deleteFileOrFolder(id, token);
        assertEquals(expectedModel, actualModel);
    }
}