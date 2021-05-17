package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<FileSystemItem>> getContentsOfFolder(
            @RequestHeader(value = FS_PATH_HEADER, defaultValue = "/") String path,
            @RequestHeader(value = "Authorization") String accessToken
    ) {

        log.info("Requested Folder contents of folder with path {}.", path);
        return fileSystemRestService.getContentsOfFolderByPathAndAccessToken(path, accessToken);
    }

    @GetMapping(FS_BASE_URI + "{fsItemId}/info")
    public ResponseEntity<FileSystemItem> getFileOrFolderInfo(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization") String accessToken
    ) {

        log.info("Requested information about FileSystemItem with id {}.", fsItemId);
        return fileSystemRestService.getInfoAboutFileOrFolderByIdAndAccessToken(fsItemId, accessToken);
    }

    @GetMapping(FS_BASE_URI + "search")
    public ResponseEntity<FileSystemItem> searchFileOrFolderByName(
            @RequestParam(name = "name", defaultValue = "name") String name,
            @RequestHeader(value = "Authorization") String accessToken
    ) {

        log.info("Searching for file or folder with name {}", name);
        return fileSystemRestService.findFileOrFolderByNameAndAccessToken(name, accessToken);
    }

    @GetMapping(FS_BASE_URI + "download")
    public ResponseEntity<List<FileSystemItem>> downloadFileOrFolder(
            @RequestParam(name = "ids") List<Long> ids,
            @RequestHeader(value = "Authorization") String accessToken) {

        log.info("Tried downloading FileSystemEntities with the ids {}", ids);
        return fileSystemRestService.downloadFileSystemEntity(ids, accessToken);
    }

    @PostMapping(FS_BASE_URI + "{fsItemId}/upload")
    public ResponseEntity<List<FileSystemItem>> uploadFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody FileSystemUpload fileSystemUpload,
            @RequestHeader(value = "Authorization") String accessToken) {

        log.info("Tried uploading new FileSystemUpload {}", fileSystemUpload);
        return fileSystemRestService.uploadFileSystemItemWithAccessToken(fsItemId, fileSystemUpload, accessToken);
    }

    @PostMapping(FS_BASE_URI + "{fsItemId}/upload/preflight")
    public ResponseEntity<List<FileSystemUploadPreflightResponse>> preflightUploadFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody List<FileSystemUpload> fileSystemUpload,
            @RequestHeader(value = "Authorization") String accessToken) {

        log.info("Preflight for {} in id {}.", fileSystemUpload, fsItemId);
        return fileSystemRestService.preflightUploadOfFileSystemItem(fsItemId, fileSystemUpload, accessToken);
    }

    @PutMapping(FS_BASE_URI + "{fsItemId}/update")
    public ResponseEntity<FileSystemItem> updateExistingFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody FileSystemItemUpdate fileSystemItemUpdate,
            @RequestHeader(value = "Authorization") String accessToken
    ) {

        log.info("Tried updating FileSystemItem {} with {}.", fsItemId, fileSystemItemUpdate);
        return fileSystemRestService.updateFileSystemItemWithIdAndAccessToken(fsItemId, fileSystemItemUpdate, accessToken);
    }

    @DeleteMapping(FS_BASE_URI + "{fsItemId}/delete")
    public ResponseEntity<List<FileSystemItem>> deleteFileOrFolder(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization") String accessToken
    ) {

        log.info("Tried deleting FileSystemItem with id {}", fsItemId);
        return fileSystemRestService.deleteFileSystemItemWithIdAndAccessToken(fsItemId, accessToken);
    }
}
