package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.rest.ServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

class FileSystemRestControllerUnitTest {

    private final FileSystemRestServiceInterface fileSystemRestServiceMock = mock(FileSystemRestService.class);
    private FileSystemRestController fileSystemRestController;

    @BeforeEach
    void setUp() {
        fileSystemRestController = new FileSystemRestController(fileSystemRestServiceMock);
    }

    @Test
    void getContentsOfFolder() {
        ArrayList<FileSystemItem> itemArrayList = new ArrayList<>();
        itemArrayList.add(FileSystemItem.builder().build());

        ResponseEntity<ArrayList<FileSystemItem>> expectedModel = new ResponseEntity<>(itemArrayList, HttpStatus.OK);
        String path = "/username/data.txt";
        String token = "token";

        when(fileSystemRestServiceMock.getContentsOfFolderByPathAndAccessToken(path, token)).thenReturn(expectedModel);

        ResponseEntity<ArrayList<FileSystemItem>> actualModel = fileSystemRestController.getContentsOfFolder(path, token);
        assertEquals(itemArrayList, actualModel.getBody());
    }

    @Test
    void getFileOrFolderInfo() {
        FileSystemItem file = FileSystemItem.builder().build();
        ResponseEntity<FileSystemItem> expectedModel = new ResponseEntity<>(file, OK);

        long id = 420;
        String token = "token";

        when(fileSystemRestServiceMock.getInfoAboutFileOrFolderByIdAndAccessToken(id, token)).thenReturn(expectedModel);

        ResponseEntity<FileSystemItem> actualModel = fileSystemRestController.getFileOrFolderInfo(id, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void searchFileOrFolderByName() {
        FileSystemItem file = FileSystemItem.builder().build();
        ResponseEntity<FileSystemItem> expectedModel = new ResponseEntity<>(file, OK);

        String name = "randomFile.exe";
        String token = "token";

        when(fileSystemRestServiceMock.findFileOrFolderByNameAndAccessToken(name, token)).thenReturn(expectedModel);

        ResponseEntity<FileSystemItem> actualModel = fileSystemRestController.searchFileOrFolderByName(name, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void uploadFileOrFolder() {
        FileSystemItem file = FileSystemItem.builder().build();
        ResponseEntity<FileSystemItem> expectedModel = new ResponseEntity<>(file, OK);

        FileSystemItemUpdate fileSystemItemUpdate = FileSystemItemUpdate.builder().name("ugabuga").build();
        String token = "token";

        when(fileSystemRestServiceMock.uploadFileSystemItemWithAccessToken(fileSystemItemUpdate, token)).thenReturn(expectedModel);

        ResponseEntity<FileSystemItem> actualModel = fileSystemRestController.uploadFileOrFolder(fileSystemItemUpdate, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void updateExistingFileOrFolder() {
        FileSystemItem file = FileSystemItem.builder().build();
        ResponseEntity<FileSystemItem> expectedModel = new ResponseEntity<>(file, OK);

        long id = 420L;
        FileSystemItemUpdate fileSystemItemUpdate = FileSystemItemUpdate.builder().name("ugabuga").build();
        String token = "token";

        when(fileSystemRestServiceMock.updatedFileSystemItemWithIdAndAccessToken(id, fileSystemItemUpdate, token)).thenReturn(expectedModel);

        ResponseEntity<FileSystemItem> actualModel = fileSystemRestController.updateExistingFileOrFolder(id, fileSystemItemUpdate, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void deleteFileOrFolder() {
        ServerResponse response = new ServerResponse(UNAUTHORIZED, "not authorized");
        ResponseEntity<ServerResponse> expectedModel = new ResponseEntity<>(response, OK);

        long id = 420;
        String token = "token";

        when(fileSystemRestServiceMock.deleteFileSystemItemWithIdAndAccessToken(id, token)).thenReturn(expectedModel);

        ResponseEntity<ServerResponse> actualModel = fileSystemRestController.deleteFileOrFolder(id, token);
        assertEquals(expectedModel, actualModel);
    }
}