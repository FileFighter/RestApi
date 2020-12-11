package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.filesystem.data.dto.File;
import de.filefighter.rest.domain.filesystem.data.dto.Folder;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class FileSystemItemsDTOService {

    private final FileSystemTypeRepository fileSystemTypeRepository;

    public FileSystemItemsDTOService(FileSystemTypeRepository fileSystemTypeRepository) {
        this.fileSystemTypeRepository = fileSystemTypeRepository;
    }

    public File createFileDto(FileSystemEntity entity) {
        FileSystemType fileSystemType = fileSystemTypeRepository.findFileSystemTypeById(entity.getTypeId());

        return new File(
                entity.getFileSystemId(),
                entity.getName(),
                entity.getSize(),
                entity.getCreatedByUserId(),
                entity.getLastUpdated(),
                fileSystemType);
    }

    public Folder createFolderDto(FileSystemEntity entity) {
        return new Folder(
                entity.getFileSystemId(),
                entity.getPath(),
                entity.getName(),
                entity.getSize(),
                entity.getCreatedByUserId(),
                entity.getLastUpdated());
    }

}
