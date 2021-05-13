package de.filefighter.rest.domain.filesystem;

import de.filefighter.rest.RestApplication;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = RestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileSystemRepositoryIntegrationTest {

    private final FileSystemRepository fileSystemRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public FileSystemRepositoryIntegrationTest(FileSystemRepository fileSystemRepository, MongoTemplate mongoTemplate) {
        this.fileSystemRepository = fileSystemRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @BeforeEach
    void prepare() {
        fileSystemRepository.deleteAll();
    }

    @Test
    void findParentEntityByItemIdFinds() {
        FileSystemEntity expectedEntity = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{0, 2, 3}).build();
        fileSystemRepository.insert(expectedEntity);

        FileSystemEntity foundEntity = fileSystemRepository.findByItemIdsContaining(0);
        assertEquals(expectedEntity, foundEntity);
    }

    @Test
    void findParentEntityByItemIdDoesNotFind() {
        FileSystemEntity expectedEntity = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{1, 2, 3}).build();
        fileSystemRepository.insert(expectedEntity);

        FileSystemEntity foundEntity = fileSystemRepository.findByItemIdsContaining(0);
        assertNull(foundEntity);
    }

    @Test
    void findParentEntityByItemIdDoesThrow() {
        FileSystemEntity expectedEntity = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{0, 2, 3}).build();
        FileSystemEntity dummy = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{0, 2, 3}).build();
        fileSystemRepository.insert(expectedEntity);
        fileSystemRepository.insert(dummy);

        IncorrectResultSizeDataAccessException ex = assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> fileSystemRepository.findByItemIdsContaining(0));
    }

    @Test
    void getEntityAndUpdateItDoesntWork() {
        FileSystemEntity expectedEntity = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{0, 2, 3}).build();
        fileSystemRepository.insert(expectedEntity);

        FileSystemEntity foundEntity = fileSystemRepository.findByItemIdsContaining(0);

        // update it
        String newValue = "new Name";
        foundEntity.setName(newValue);

        // flush changes.
        fileSystemRepository.save(foundEntity);

        // This does create another instance of the entity in the database.

        // assert actual change in db.
        IncorrectResultSizeDataAccessException ex = assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> fileSystemRepository.findByFileSystemId(1));
    }

    @Test
    void getEntityAndUpdateItAlsoDoesntWork() {
        FileSystemEntity expectedEntity = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{0, 2, 3}).build();
        fileSystemRepository.insert(expectedEntity);

        FileSystemEntity foundEntity = fileSystemRepository.findByItemIdsContaining(0);

        // update it
        String newValue = "new Name";
        foundEntity.setName(newValue);

        // flush changes.
        mongoTemplate.save(foundEntity);

        // This does create another instance of the entity in the database.

        // assert actual change in db.
        IncorrectResultSizeDataAccessException ex = assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> fileSystemRepository.findByFileSystemId(1));
    }

    @Test
    void getEntityAndUpdateItWorks() {
        FileSystemEntity expectedEntity = FileSystemEntity.builder().fileSystemId(1).itemIds(new long[]{0, 2, 3}).build();
        fileSystemRepository.insert(expectedEntity);

        FileSystemEntity foundEntity = fileSystemRepository.findByItemIdsContaining(0);

        // update it
        String newValue = "new Name";
        foundEntity.setName(newValue);

        // flush changes.
        mongoTemplate.insert(foundEntity);

        // This does create another instance of the entity in the database.

        // assert actual change in db.
        IncorrectResultSizeDataAccessException ex = assertThrows(IncorrectResultSizeDataAccessException.class,
                () -> fileSystemRepository.findByFileSystemId(1));
    }
}
