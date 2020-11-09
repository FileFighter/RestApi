package de.filefighter.rest.domain.filesystem.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSystemTypeRepositoryUnitTest {

    private final FileSystemTypeRepository fileSystemTypeRepository = new FileSystemTypeRepository();

    @Test
    void findFileSystemTypeByIdThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                fileSystemTypeRepository.findFileSystemTypeById(900));
    }

    @Test
    void findFileSystemTypeByIdWorksCorrectly() {
        FileSystemType expectedType = FileSystemType.TEXT;
        long expectedId = expectedType.getId();

        FileSystemType actualType = fileSystemTypeRepository.findFileSystemTypeById(expectedId);

        assertEquals(expectedType, actualType);
    }
}