package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import org.springframework.stereotype.Service;

@Service
public class IdGenerationService {

    private final FileSystemRepository fileSystemRepository;
    private long counter;

    public IdGenerationService(FileSystemRepository fileSystemRepository) {
        this.fileSystemRepository = fileSystemRepository;
    }

    public void initializeService() {
        counter = fileSystemRepository.count();
    }

    public long peekNext() {
        return counter + 1;
    }

    public long consumeNext() {
        counter++;
        return counter;
    }
}
