package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.authentication.AuthenticationService;
import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.filesystem.business.FileSystemUploadService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileSystemRestService implements FileSystemRestServiceInterface {

    private final FileSystemBusinessService fileSystemBusinessService;
    private final AuthenticationService authenticationService;
    private final InputSanitizerService inputSanitizerService;
    private final FileSystemUploadService fileSystemUploadService;

    public FileSystemRestService(FileSystemBusinessService fileSystemBusinessService, AuthenticationService authenticationService, InputSanitizerService inputSanitizerService, FileSystemUploadService fileSystemUploadService) {
        this.fileSystemBusinessService = fileSystemBusinessService;
        this.authenticationService = authenticationService;
        this.inputSanitizerService = inputSanitizerService;
        this.fileSystemUploadService = fileSystemUploadService;
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
    public ResponseEntity<FileSystemItem> uploadFileSystemItemWithAccessToken(long rootItemId, FileSystemUpload fileSystemUpload, String accessToken) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessToken);
        FileSystemUpload sanitizedUpload = inputSanitizerService.sanitizeUpload(fileSystemUpload);

        return new ResponseEntity<>(fileSystemUploadService.uploadFileSystemItem(rootItemId, sanitizedUpload, authenticatedUser), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<FileSystemUploadPreflightResponse>> preflightUploadOfFileSystemItem(long rootItemId, List<FileSystemUpload> fileSystemUploads, String accessToken) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessToken);
        List<FileSystemUpload> sanitizedUploads = fileSystemUploads.stream().map(inputSanitizerService::sanitizeUpload).collect(Collectors.toList());

        return new ResponseEntity<>(fileSystemUploadService.preflightUploadFileSystemItem(rootItemId, sanitizedUploads, authenticatedUser), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> updateFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
        boolean everythingWasDeleted = fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser);
        if (everythingWasDeleted) {
            return new ResponseEntity<>(new ServerResponse(HttpStatus.OK, "Successfully deleted all requested FileSystemItems."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ServerResponse(HttpStatus.OK, "Not everything got deleted, because you are not allowed to edit some files."), HttpStatus.OK);
        }
    }
}
