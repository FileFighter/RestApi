package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.*;
import de.filefighter.rest.rest.ServerResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileSystemRestControllerUnitTest {

    private static final FileSystemRestServiceInterface fileSystemRestService = mock(FileSystemRestService.class);
    private static FileSystemRestController fileSystemRestController;

    @BeforeAll
    static void setUp() {
        fileSystemRestController = new FileSystemRestController(fileSystemRestService);
    }

    @Test
    void getContentsOfFolder() {
        Folder dummyFolder = new Folder();
        File dummyFile = new File();
        EntityModel<FolderContents> expectedModel = EntityModel.of(FolderContents.builder()
                .files(new File[]{dummyFile})
                .folders(new Folder[]{dummyFolder}).create());

        long id = 420;
        String token = "token";

        when(fileSystemRestService.getContentsOfFolderByIdAndAccessToken(id, token)).thenReturn(expectedModel);

        EntityModel<FolderContents> actualModel = fileSystemRestController.getContentsOfFolder(id, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void getFileOrFolderInfo() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        long id = 420;
        String token = "token";

        when(fileSystemRestService.getInfoAboutFileOrFolderByIdAndAccessToken(id, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.getFileOrFolderInfo(id, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void searchFileOrFolderByName() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        String name = "randomFile.exe";
        String token = "token";

        when(fileSystemRestService.findFileOrFolderByNameAndAccessToken(name, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.searchFileOrFolderByName(name, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void uploadFileOrFolder() {
        File file = new File();
        EntityModel<FileSystemItem> expectedModel = EntityModel.of(file);

        FileSystemItemUpdate fileSystemItemUpdate = FileSystemItemUpdate.create().name("ugabuga").build();
        String token = "token";

        when(fileSystemRestService.uploadFileSystemItemWithAccessToken(fileSystemItemUpdate, token)).thenReturn(expectedModel);

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

        when(fileSystemRestService.updatedFileSystemItemWithIdAndAccessToken(id, fileSystemItemUpdate, token)).thenReturn(expectedModel);

        EntityModel<FileSystemItem> actualModel = fileSystemRestController.updateExistingFileOrFolder(id, fileSystemItemUpdate, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void deleteFileOrFolder() {
        ServerResponse response = new ServerResponse("denied", "not authorized");
        EntityModel<ServerResponse> expectedModel = EntityModel.of(response);

        long id = 420;
        String token = "token";

        when(fileSystemRestService.deleteFileSystemItemWithIdAndAccessToken(id, token)).thenReturn(expectedModel);

        EntityModel<ServerResponse> actualModel = fileSystemRestController.deleteFileOrFolder(id, token);
        assertEquals(expectedModel, actualModel);
    }
}