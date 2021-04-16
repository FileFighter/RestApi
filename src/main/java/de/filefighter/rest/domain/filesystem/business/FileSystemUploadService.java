package de.filefighter.rest.domain.filesystem.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.filesystem.type.FileSystemTypeRepository;
import de.filefighter.rest.domain.user.data.dto.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class FileSystemUploadService {

    private final FileSystemRepository fileSystemRepository;
    private final FileSystemHelperService fileSystemHelperService;
    private final FileSystemTypeRepository fileSystemTypeRepository;
    private final MongoTemplate mongoTemplate;
    private final InputSanitizerService inputSanitizerService;

    public FileSystemUploadService(FileSystemRepository fileSystemRepository, FileSystemHelperService fileSystemHelperService, FileSystemTypeRepository fileSystemTypeRepository, MongoTemplate mongoTemplate, InputSanitizerService inputSanitizerService) {
        this.fileSystemRepository = fileSystemRepository;
        this.fileSystemHelperService = fileSystemHelperService;
        this.fileSystemTypeRepository = fileSystemTypeRepository;
        this.mongoTemplate = mongoTemplate;
        this.inputSanitizerService = inputSanitizerService;
    }

    public FileSystemItem uploadFileSystemItem(long rootItemId, FileSystemUpload fileSystemUpload, User authenticatedUser) {
        return null;
    }

    public List<FileSystemUploadPreflightResponse> preflightUploadFileSystemItem(long rootItemId, List<FileSystemUpload> uploads, User authenticatedUser) {
        return null;
    }
}
