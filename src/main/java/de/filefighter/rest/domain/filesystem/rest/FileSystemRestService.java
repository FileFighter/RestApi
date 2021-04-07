package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.authentication.AuthenticationService;
import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.user.data.dto.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileSystemRestService implements FileSystemRestServiceInterface {

    private final FileSystemBusinessService fileSystemBusinessService;
    private final AuthenticationService authenticationService;
    private final InputSanitizerService inputSanitizerService;

    public FileSystemRestService(FileSystemBusinessService fileSystemBusinessService, AuthenticationService authenticationService, InputSanitizerService inputSanitizerService) {
        this.fileSystemBusinessService = fileSystemBusinessService;
        this.authenticationService = authenticationService;
        this.inputSanitizerService = inputSanitizerService;
    }

    @Override
    public ResponseEntity<ArrayList<FileSystemItem>> getContentsOfFolderByPathAndAccessToken(String path, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
        String cleanPathString = inputSanitizerService.sanitizePath(path);

        ArrayList<FileSystemItem> fileSystemItems = (ArrayList<FileSystemItem>) fileSystemBusinessService.getFolderContentsByPath(cleanPathString, authenticatedUser);
        return new ResponseEntity<>(fileSystemItems, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
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
    public ResponseEntity<FileSystemItem> updateFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<List<FileSystemItem>> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
        return new ResponseEntity<>(fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser), HttpStatus.OK);
    }
}
