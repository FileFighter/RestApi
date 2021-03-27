package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemUpload;
import de.filefighter.rest.rest.ServerResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static de.filefighter.rest.configuration.RestConfiguration.*;

@Log4j2
@RestController
@Tag(name = "FileSystem", description = "FileSystem Controller")
@RequestMapping(BASE_API_URI)
public class FileSystemRestController {

    private final FileSystemRestServiceInterface fileSystemRestService;

    public FileSystemRestController(FileSystemRestServiceInterface fileSystemRestService) {
        this.fileSystemRestService = fileSystemRestService;
    }

    @GetMapping(FS_BASE_URI + "contents")
    public ResponseEntity<ArrayList<FileSystemItem>> getContentsOfFolder(
            @RequestHeader(value = FS_PATH_HEADER, defaultValue = "/") String path,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        log.info("Requested Folder contents of folder with path {}.", path);
        return fileSystemRestService.getContentsOfFolderByPathAndAccessToken(path, accessToken);
    }

    @GetMapping(FS_BASE_URI + "{fsItemId}/info")
    public ResponseEntity<FileSystemItem> getFileOrFolderInfo(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        log.info("Requested information about FileSystemItem with id {}.", fsItemId);
        return fileSystemRestService.getInfoAboutFileOrFolderByIdAndAccessToken(fsItemId, accessToken);
    }

    @GetMapping(FS_BASE_URI + "search")
    public ResponseEntity<FileSystemItem> searchFileOrFolderByName(
            @RequestParam(name = "name", defaultValue = "name") String name,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        log.info("Searching for file or folder with name {}", name);
        return fileSystemRestService.findFileOrFolderByNameAndAccessToken(name, accessToken);
    }

    @PostMapping(FS_BASE_URI + "{fsItemId}/upload")
    public ResponseEntity<FileSystemItem> uploadFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody FileSystemUpload fileSystemUpload,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken) {

        log.info("Tried uploading new FileSystemUpload {}", fileSystemUpload);
        return fileSystemRestService.uploadFileSystemItemWithAccessToken(fsItemId, fileSystemUpload, accessToken);
    }

    @PutMapping(FS_BASE_URI + "{fsItemId}/update")
    public ResponseEntity<FileSystemItem> updateExistingFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody FileSystemItemUpdate fileSystemItemUpdate,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        log.info("Tried updating FileSystemItem {} with {}.", fsItemId, fileSystemItemUpdate);
        return fileSystemRestService.updatedFileSystemItemWithIdAndAccessToken(fsItemId, fileSystemItemUpdate, accessToken);
    }

    @DeleteMapping(FS_BASE_URI + "{fsItemId}/delete")
    public ResponseEntity<ServerResponse> deleteFileOrFolder(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        log.info("Tried deleting FileSystemItem with id {}", fsItemId);
        return fileSystemRestService.deleteFileSystemItemWithIdAndAccessToken(fsItemId, accessToken);
    }
}
