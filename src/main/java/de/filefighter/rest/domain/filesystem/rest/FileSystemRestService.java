package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class FileSystemRestService implements FileSystemRestServiceInterface {

    @Override
    public EntityModel<FolderContents> getContentsOfFolderByIdAndAccessToken(long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<FileSystemItem> findFileOrFolderByNameAndAccessToken(String name, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<FileSystemItem> uploadFileSystemItemWithAccessToken(FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<FileSystemItem> updatedFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessToken) {
        return null;
    }
}
