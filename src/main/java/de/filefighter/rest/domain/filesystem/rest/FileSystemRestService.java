package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.business.UserAuthorizationService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FileSystemRestService implements FileSystemRestServiceInterface {

    private final UserAuthorizationService userAuthorizationService;
    private final InputSanitizerService inputSanitizerService;
    private final AccessTokenBusinessService accessTokenBusinessService;
    private final FileSystemBusinessService fileSystemBusinessService;

    public FileSystemRestService(UserAuthorizationService userAuthorizationService, InputSanitizerService inputSanitizerService, AccessTokenBusinessService accessTokenBusinessService, FileSystemBusinessService fileSystemBusinessService) {
        this.userAuthorizationService = userAuthorizationService;
        this.inputSanitizerService = inputSanitizerService;
        this.accessTokenBusinessService = accessTokenBusinessService;
        this.fileSystemBusinessService = fileSystemBusinessService;
    }

    @Override
    public ResponseEntity<FolderContents> getContentsOfFolderByPathAndAccessToken(String path, String accessTokenValue) {
        String cleanHeader = inputSanitizerService.sanitizeRequestHeader(RestConfiguration.AUTHORIZATION_BEARER_PREFIX, accessTokenValue);
        String cleanValue = inputSanitizerService.sanitizeTokenValue(cleanHeader);
        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(cleanValue);
        User authenticatedUser = userAuthorizationService.authenticateUserWithAccessToken(accessToken);
        String cleanPathString = InputSanitizerService.sanitizeString(path);

        FolderContents folderContents = fileSystemBusinessService.getContentsOfFolder(cleanPathString, authenticatedUser);
        return new ResponseEntity<>(folderContents, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<FileSystemItem> findFileOrFolderByNameAndAccessToken(String name, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<FileSystemItem> uploadFileSystemItemWithAccessToken(FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<FileSystemItem> updatedFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessToken) {
        return null;
    }
}
