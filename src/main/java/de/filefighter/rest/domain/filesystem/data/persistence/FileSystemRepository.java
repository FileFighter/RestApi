package de.filefighter.rest.domain.filesystem.data.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface FileSystemRepository extends MongoRepository<FileSystemEntity, String> {
    FileSystemEntity findByFileSystemId(long fileSystemId);
}
