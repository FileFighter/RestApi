package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.rest.ServerResponse;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import static de.filefighter.rest.config.RestConfiguration.*;

@RestController
@Api(value = "FileSystem Rest Controller", tags = {"FileSystem"})
@RequestMapping(BASE_API_URI)
public class FileSystemRestController {

    private final static Logger LOG = LoggerFactory.getLogger(FileSystemRestController.class);

    private final FileSystemRestServiceInterface fileSystemRestService;

    public FileSystemRestController(FileSystemRestServiceInterface fileSystemRestService) {
        this.fileSystemRestService = fileSystemRestService;
    }

    @GetMapping(FS_BASE_URI + "{fsItemId}/contents")
    public EntityModel<FolderContents> getContentsOfFolder(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        LOG.info("Requested Folder contents of id {}.", fsItemId);
        return fileSystemRestService.getContentsOfFolderByIdAndAccessToken(fsItemId, accessToken);
    }

    @GetMapping(FS_BASE_URI + "{fsItemId}/info")
    public EntityModel<?> getFileOrFolderInfo(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ) {

        LOG.info("Requested Folder contents of id {}.", fsItemId);
        return fileSystemRestService.getInfoAboutFileOrFolderByIdAndAccessToken(fsItemId, accessToken);
    }

    @GetMapping(FS_BASE_URI+"search")
    public EntityModel<?> searchFileOrFolderByName(
            @RequestParam(name = "name", defaultValue = "name") String name,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Searching for file or folder with name {}", name);
        return fileSystemRestService.findFileOrFolderByNameAndAccessToken(name, accessToken);
    }

    @PostMapping(FS_BASE_URI+"upload")
    public EntityModel<?> uploadFileOrFolder(
            @RequestBody FileSystemItemUpdate fileSystemItemUpdate,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Tried uploading new FileSystemItem {}", fileSystemItemUpdate);
        return fileSystemRestService.uploadFileSystemItemWithAccessToken(fileSystemItemUpdate, accessToken);
    }

    @PutMapping(FS_BASE_URI+"{fsItemId}/update")
    public EntityModel<?> updateExistingFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody FileSystemItemUpdate fileSystemItemUpdate,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Tried updating FileSystemItem {} with {}.", fsItemId, fileSystemItemUpdate);
        return fileSystemRestService.updatedFileSystemItemWithIdAndAccessToken(fileSystemItemUpdate, accessToken);
    }

    @DeleteMapping(FS_BASE_URI+"{fsItemId}/delete")
    public EntityModel<ServerResponse> deleteFileOrFolder(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Tried deleting FileSystemItem with id {}", fsItemId);
        return fileSystemRestService.deleteFileSystemItemWithIdAndAccessToken(fsItemId, accessToken);
    }
}
