package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdGenerationServiceUnitTest {

    private final FileSystemRepository fileSystemRepositoryMock = mock(FileSystemRepository.class);
    private final IdGenerationService idGenerationService = new IdGenerationService(fileSystemRepositoryMock);

    private final List<FileSystemEntity> mockData = Arrays.asList(
            FileSystemEntity.builder().fileSystemId(1).build(),
            FileSystemEntity.builder().fileSystemId(10).build(),
            FileSystemEntity.builder().fileSystemId(11).build(),
            FileSystemEntity.builder().fileSystemId(100).build()
    );

    @Test
    void initializeServiceWorks() {
        when(fileSystemRepositoryMock.findAll()).thenReturn(mockData);

        idGenerationService.initializeService();
        long nextId = idGenerationService.peekNext();

        assertEquals(101, nextId);
    }

    @Test
    void initializeServiceWorksWhenDatabaseIsEmpty() {
        when(fileSystemRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        idGenerationService.initializeService();
        long nextId = idGenerationService.peekNext();

        assertEquals(0, nextId);
    }

    @Test
    void peekNextWorks() {
        when(fileSystemRepositoryMock.findAll()).thenReturn(mockData);
        idGenerationService.initializeService();

        for (int i = 0; i < 100; i++) {
            long nextId = idGenerationService.peekNext();
            assertEquals(101, nextId);
        }
    }

    @Test
    void consumeNextWorks() {
        when(fileSystemRepositoryMock.findAll()).thenReturn(mockData);
        idGenerationService.initializeService();

        for (int i = 0; i < 100; i++) {
            long nextId = idGenerationService.consumeNext();
            assertEquals(101 + i, nextId);
        }
    }
}