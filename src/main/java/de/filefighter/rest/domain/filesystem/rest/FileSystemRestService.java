package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.business.UserAuthorizationService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;

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
    public ResponseEntity<ArrayList<FileSystemItem>> getContentsOfFolderByPathAndAccessToken(String path, String accessTokenValue) {
        String cleanHeader = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenValue);
        String cleanValue = inputSanitizerService.sanitizeTokenValue(cleanHeader);
        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(cleanValue);
        User authenticatedUser = userAuthorizationService.authenticateUserWithAccessToken(accessToken);
        String cleanPathString = InputSanitizerService.sanitizeString(path);

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(cleanPathString, authenticatedUser);
        return new ResponseEntity<>(fileSystemItems, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessTokenValue) {
        String cleanHeader = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenValue);
        String cleanValue = inputSanitizerService.sanitizeTokenValue(cleanHeader);
        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(cleanValue);
        User authenticatedUser = userAuthorizationService.authenticateUserWithAccessToken(accessToken);

        return new ResponseEntity<>(fileSystemBusinessService.getFileSystemItemInfo(fsItemId, authenticatedUser), HttpStatus.OK);
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
    public ResponseEntity<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessTokenValue) {
        String cleanHeader = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenValue);
        String cleanValue = inputSanitizerService.sanitizeTokenValue(cleanHeader);
        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(cleanValue);
        User authenticatedUser = userAuthorizationService.authenticateUserWithAccessToken(accessToken);

        fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);
        return new ResponseEntity<>(new ServerResponse(HttpStatus.NO_CONTENT, "FileSystemItem was deleted."), HttpStatus.NO_CONTENT);
    }
}
