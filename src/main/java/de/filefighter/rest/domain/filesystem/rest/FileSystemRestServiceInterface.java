package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.common.Pair;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.upload.CreateNewFolder;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUpload;
import de.filefighter.rest.domain.filesystem.data.dto.upload.FileSystemUploadPreflightResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface FileSystemRestServiceInterface {
    ResponseEntity<List<FileSystemItem>> getContentsOfFolderByPathAndAccessToken(String path, String accessToken);

    ResponseEntity<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessToken);

    ResponseEntity<List<FileSystemItem>> findFileOrFolderByNameAndAccessToken(String name, String accessToken);

    ResponseEntity<List<FileSystemItem>> uploadFileSystemItemWithAccessToken(long rootItemId, FileSystemUpload fileSystemUpload, String accessToken);

    ResponseEntity<List<FileSystemUploadPreflightResponse>> preflightUploadOfFileSystemItem(long fsItemId, List<FileSystemUpload> fileSystemUploads, String accessToken);

    ResponseEntity<FileSystemItem> updateFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken);

    ResponseEntity<List<FileSystemItem>> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessToken);

    ResponseEntity<List<FileSystemItem>> downloadFileSystemEntity(List<Long> fsItemIds, Pair<String, String> authPair);

    ResponseEntity<FileSystemItem> createNewFolder(long parentId, CreateNewFolder newFolder, String accessToken);
}
