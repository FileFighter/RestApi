package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.authentication.AuthenticationService;
import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.Pair;
import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.filesystem.business.FileSystemUploadService;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.upload.CreateNewFolder;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import de.filefighter.rest.domain.user.data.dto.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    public ResponseEntity<List<FileSystemItem>> getContentsOfFolderByPathAndAccessToken(String path, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
        String cleanPathString = inputSanitizerService.sanitizePath(path);

        Pair<List<FileSystemItem>, Long> folderContents = fileSystemBusinessService.getFolderContentsByPath(cleanPathString, authenticatedUser);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Expose-Headers", RestConfiguration.FS_CURRENT_ID_HEADER);
        responseHeaders.set(RestConfiguration.FS_CURRENT_ID_HEADER, folderContents.getSecond().toString());
        return new ResponseEntity<>(folderContents.getFirst(), responseHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
        return new ResponseEntity<>(fileSystemBusinessService.getFileSystemItemInfo(fsItemId, authenticatedUser), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<FileSystemItem>> downloadFileSystemEntity(List<Long> fsItemIds, Pair<String, String> accessTokenValueOrHeader) {
        User authenticatedUser = authenticationService.authenticateUserWithCookieOrHeader(accessTokenValueOrHeader);
        Pair<List<FileSystemItem>, String> listStringPair = fileSystemBusinessService.downloadFileSystemEntity(fsItemIds, authenticatedUser);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(RestConfiguration.FS_DOWNLOAD_NAME_HEADER, listStringPair.getSecond());
        return new ResponseEntity<>(listStringPair.getFirst(), responseHeaders, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> createNewFolder(long parentId, CreateNewFolder newFolder, String accessToken) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessToken);
        String sanitizedName = inputSanitizerService.sanitizeString(newFolder.getName());
        newFolder = CreateNewFolder.builder().name(sanitizedName).build();

        FileSystemItem folderItem = fileSystemUploadService.createNewFolder(parentId, newFolder, authenticatedUser);
        return new ResponseEntity<>(folderItem, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<FileSystemItem>> uploadFileSystemItemWithAccessToken(long rootItemId, FileSystemUpload fileSystemUpload, String accessToken) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessToken);
        FileSystemUpload sanitizedUpload = inputSanitizerService.sanitizeUpload(fileSystemUpload);

        return new ResponseEntity<>(fileSystemUploadService.uploadFileSystemItem(rootItemId, sanitizedUpload, authenticatedUser), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<FileSystemUploadPreflightResponse>> preflightUploadOfFileSystemItem(long rootItemId, List<FileSystemUpload> fileSystemUploads, String accessToken) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessToken);
        // Do not sanitize this because we want the have the exact information where the path is not valid.
        // List<FileSystemUpload> sanitizedUploads = fileSystemUploads.stream().map(inputSanitizerService::sanitizeUpload).collect(Collectors.toList())

        return new ResponseEntity<>(fileSystemUploadService.preflightUploadFileSystemItem(rootItemId, fileSystemUploads, authenticatedUser), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<FileSystemItem>> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessTokenValue) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenValue);
        return new ResponseEntity<>(fileSystemBusinessService.deleteFileSystemItemById(fsItemId, authenticatedUser), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<FileSystemItem>> findFileOrFolderByNameAndAccessToken(String name, String accessToken) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessToken);
        String sanitizedSearch = inputSanitizerService.sanitizeString(name);
        sanitizedSearch = URLDecoder.decode(sanitizedSearch, StandardCharsets.UTF_8);

        return new ResponseEntity<>(fileSystemBusinessService.searchFileSystemEntity(sanitizedSearch, authenticatedUser), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<FileSystemItem> updateFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }
}
