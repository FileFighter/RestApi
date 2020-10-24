package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItem;
import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.hateoas.EntityModel;

public interface FileSystemRestServiceInterface {
    EntityModel<FolderContents> getContentsOfFolderByIdAndAccessToken(long fsItemId, String accessToken);
    EntityModel<FileSystemItem> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessToken);
    EntityModel<FileSystemItem> findFileOrFolderByNameAndAccessToken(String name, String accessToken);
    EntityModel<FileSystemItem> uploadFileSystemItemWithAccessToken(FileSystemItemUpdate fileSystemItemUpdate, String accessToken);
    EntityModel<FileSystemItem> updatedFileSystemItemWithIdAndAccessToken(long fsItemId, FileSystemItemUpdate fileSystemItemUpdate, String accessToken);
    EntityModel<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessToken);
}
