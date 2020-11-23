package de.filefighter.rest.domain.filesystem.data.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface FileSystemRepository extends MongoRepository<FileSystemEntity, String> {
    FileSystemEntity findById(long id);
}
