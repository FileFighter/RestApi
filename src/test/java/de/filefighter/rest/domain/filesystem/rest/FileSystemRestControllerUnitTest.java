package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;

class FileSystemRestControllerUnitTest {

    private final FileSystemRestServiceInterface fileSystemRestServiceMock = mock(FileSystemRestService.class);
    private FileSystemRestController fileSystemRestController;

    @BeforeEach
    void setUp() {
        fileSystemRestController = new FileSystemRestController(fileSystemRestServiceMock);
    }

    @Test
    void getContentsOfFolder() {
        List<FileSystemItem> itemArrayList = new ArrayList<>();
        itemArrayList.add(FileSystemItem.builder().build());

        ResponseEntity<List<FileSystemItem>> expectedModel = new ResponseEntity<>(itemArrayList, HttpStatus.OK);
        String path = "/username/data.txt";
        String token = "token";

        when(fileSystemRestServiceMock.getContentsOfFolderByPathAndAccessToken(path, token)).thenReturn(expectedModel);

        ResponseEntity<List<FileSystemItem>> actualModel = fileSystemRestController.getContentsOfFolder(path, token);
        assertEquals(itemArrayList, actualModel.getBody());
    }

    @Test
    void getFileOrFolderInfo() {
        FileSystemItem file = FileSystemItem.builder().build();
        ResponseEntity<FileSystemItem> expectedModel = new ResponseEntity<>(file, OK);

        long id = 420;
        String token = "token";
        String cookieValue = null;

        when(fileSystemRestServiceMock.getInfoAboutFileOrFolderByIdAndAccessToken(eq(id), any())).thenReturn(expectedModel);

        ResponseEntity<FileSystemItem> actualModel = fileSystemRestController.getFileOrFolderInfo(id, cookieValue, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void searchFileOrFolderByName() {
        FileSystemItem file = FileSystemItem.builder().build();
        List<FileSystemItem> list = new ArrayList<>();
        list.add(file);
        ResponseEntity<List<FileSystemItem>> expectedModel = new ResponseEntity<>(list, OK);

        String name = "randomFile.exe";
        String token = "token";

        when(fileSystemRestServiceMock.findFileOrFolderByNameAndAccessToken(name, token)).thenReturn(expectedModel);

        ResponseEntity<List<FileSystemItem>> actualModel = fileSystemRestController.searchFileOrFolderByName(name, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void preflightUpload() {
        List<FileSystemUpload> uploads = new ArrayList<>();
        List<FileSystemUploadPreflightResponse> responses = new ArrayList<>();
        long id = 1234;
        String token = "asdhalskd";

        ResponseEntity<List<FileSystemUploadPreflightResponse>> responseEntity = new ResponseEntity<>(responses, OK);
        when(fileSystemRestServiceMock.preflightUploadOfFileSystemItem(id, uploads, token)).thenReturn(responseEntity);

        ResponseEntity<List<FileSystemUploadPreflightResponse>> actualEntity = fileSystemRestController.preflightUploadFileOrFolder(id, uploads, token);

        assertEquals(responseEntity, actualEntity);
    }

    @Test
    void uploadFileOrFolder() {
        FileSystemItem file = FileSystemItem.builder().build();
        FileSystemUpload upload = FileSystemUpload.builder().build();
        List<FileSystemItem> items = new ArrayList<>();
        items.add(file);

        String token = "sometoken";
        long rootId = 123;
        ResponseEntity<List<FileSystemItem>> responseEntity = new ResponseEntity<>(items, OK);

        when(fileSystemRestServiceMock.uploadFileSystemItemWithAccessToken(rootId, upload, token)).thenReturn(responseEntity);

        ResponseEntity<List<FileSystemItem>> actualModel = fileSystemRestController.uploadFileOrFolder(rootId, upload, token);
        assertEquals(responseEntity, actualModel);
    }

    @Test
    void updateExistingFileOrFolder() {
        FileSystemItem file = FileSystemItem.builder().build();
        ResponseEntity<FileSystemItem> expectedModel = new ResponseEntity<>(file, OK);

        long id = 420L;
        FileSystemItemUpdate fileSystemItemUpdate = FileSystemItemUpdate.builder().name("ugabuga").build();
        String token = "token";

        when(fileSystemRestServiceMock.updateFileSystemItemWithIdAndAccessToken(id, fileSystemItemUpdate, token)).thenReturn(expectedModel);

        ResponseEntity<FileSystemItem> actualModel = fileSystemRestController.updateExistingFileOrFolder(id, fileSystemItemUpdate, token);
        assertEquals(expectedModel, actualModel);
    }

    @Test
    void deleteFileOrFolder() {
        ArrayList<FileSystemItem> expectedItems = new ArrayList<>();
        ResponseEntity<List<FileSystemItem>> expectedModel = new ResponseEntity<>(expectedItems, OK);

        long id = 420;
        String token = "token";

        when(fileSystemRestServiceMock.deleteFileSystemItemWithIdAndAccessToken(id, token)).thenReturn(expectedModel);

        ResponseEntity<List<FileSystemItem>> actualModel = fileSystemRestController.deleteFileOrFolder(id, token);
        assertEquals(expectedModel, actualModel);
    }
}