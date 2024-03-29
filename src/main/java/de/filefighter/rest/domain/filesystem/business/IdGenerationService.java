package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class IdGenerationService {

    private final FileSystemRepository fileSystemRepository;
    private long counter;

    public IdGenerationService(FileSystemRepository fileSystemRepository) {
        this.fileSystemRepository = fileSystemRepository;
    }

    @SuppressWarnings("java:S3655")
    public void initializeService() {
        // we could optimize the fileSystemIds by looking at the free ids between 0 and the max id.
        List<FileSystemEntity> entityList = fileSystemRepository.findAll();
        Optional<Long> max = entityList
                .stream()
                .map(FileSystemEntity::getFileSystemId)
                .max(Long::compare);

        if (entityList.isEmpty() && max.isEmpty()) {
            log.warn("Database was empty. If this happens during production please contact the developers!");
            counter = -1;
        } else {
            counter = max.get();
        }

        log.debug("Found {} entities in the db.", entityList.size());
        log.debug("IdGeneration start set to {}.", counter);
    }

    public long peekNext() {
        return counter + 1;
    }

    public long consumeNext() {
        counter++;
        return counter;
    }
}
