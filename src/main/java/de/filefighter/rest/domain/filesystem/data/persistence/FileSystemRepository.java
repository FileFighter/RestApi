package de.filefighter.rest.domain.filesystem.data.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FileSystemRepository extends MongoRepository<FileSystemEntity, String> {
    FileSystemEntity findByFileSystemId(long fileSystemId);

    List<FileSystemEntity> findByPath(String path);

    List<FileSystemEntity> findAllByNameAndOwnerId(String name, long ownerId);

    FileSystemEntity findByPathAndOwnerId(String path, long ownerId);

    Long deleteByFileSystemId(long fileSystemId);

    List<FileSystemEntity> findAllByFileSystemIdInAndNameIgnoreCase(List<Long> fileSystemId, String name);

    // this does work tho.
    FileSystemEntity findByItemIdsContaining(long id);

    List<FileSystemEntity> findAllByFileSystemIdIn(long[] fileSystemIds);
}

