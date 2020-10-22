package de.filefighter.rest.domain.filesystem.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class FileSystemRestControllerUnitTest {

    private static final FileSystemRestServiceInterface fileSystemRestService = mock(FileSystemRestService.class);
    private static FileSystemRestController fileSystemRestController;

    @BeforeAll
    static void setUp() {
        fileSystemRestController = new FileSystemRestController(fileSystemRestService);
    }

    @Test
    void getContentsOfFolder() {
    }

    @Test
    void getFileOrFolderInfo() {
    }

    @Test
    void searchFileOrFolderByName() {
    }

    @Test
    void uploadFileOrFolder() {
    }

    @Test
    void updateExistingFileOrFolder() {
    }

    @Test
    void deleteFileOrFolder() {
    }
}