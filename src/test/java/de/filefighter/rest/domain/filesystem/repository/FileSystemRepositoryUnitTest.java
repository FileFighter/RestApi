package de.filefighter.rest.domain.filesystem.repository;

import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ActiveProfiles("test")
@SpringBootTest
public class FileSystemRepositoryUnitTest {

    private final FileSystemRepository fileSystemRepository;

    @Autowired
    public FileSystemRepositoryUnitTest(FileSystemRepository fileSystemRepository) {
        this.fileSystemRepository = fileSystemRepository;
    }

    @Test
    void findAllByFileSystemIdInAndNameCaseInsensitive() {
        List<Long> idList = new ArrayList<>();
        idList.add(3L);

        List<FileSystemEntity> itemsToInsert = new ArrayList<>();
        FileSystemEntity fileSystemEntity = FileSystemEntity.builder()
                .fileSystemId(3L)
                .name("SomeFolder")
                .build();

        itemsToInsert.add(fileSystemEntity);
        fileSystemRepository.insert(itemsToInsert);
        List<FileSystemEntity> result = fileSystemRepository.findAllByFileSystemIdInAndName(idList, "somefolder");

        assertFalse(result.isEmpty());
    }

}
