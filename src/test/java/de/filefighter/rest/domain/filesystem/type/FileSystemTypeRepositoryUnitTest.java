package de.filefighter.rest.domain.filesystem.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSystemTypeRepositoryUnitTest {

    private final FileSystemTypeRepository fileSystemTypeRepository = new FileSystemTypeRepository();

    @Test
    void findFileSystemTypeByIdThrows() {
        long id = 900;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileSystemTypeRepository.findFileSystemTypeById(id));
        assertEquals("No FileSystemType found for id: " + id, ex.getMessage());
    }

    @Test
    void findFileSystemTypeByIdWorksCorrectly() {
        FileSystemType expectedType = FileSystemType.TEXT;
        long expectedId = expectedType.getId();

        FileSystemType actualType = fileSystemTypeRepository.findFileSystemTypeById(expectedId);

        assertEquals(expectedType, actualType);
    }
}