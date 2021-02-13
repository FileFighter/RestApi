package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;


public interface FileSystemRestServiceInterface {
    ResponseEntity<ArrayList<FileSystemItem>> getContentsOfFolderByPathAndAccessToken(String path, String accessToken);

    ResponseEntity<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessToken);

    ResponseEntity<FileSystemItem> findFileOrFolderByNameAndAccessToken(String name, String accessToken);

    ResponseEntity<List<FileSystemItem>> uploadFileSystemItemWithAccessToken(long rootItemId, List<FileSystemItemUpdate> fileSystemItemUpdate, String accessToken);

    ResponseEntity<FileSystemItem> updatedFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken);

    ResponseEntity<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessToken);
}
