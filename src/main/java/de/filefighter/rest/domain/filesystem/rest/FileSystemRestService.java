package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FileSystemRestService implements FileSystemRestServiceInterface {

    @Override
    public ResponseEntity<FolderContents> getContentsOfFolderByIdAndAccessToken(String path, String accessToken) {
        return null;
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
