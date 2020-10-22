package de.filefighter.rest.domain.filesystem.rest;

import de.filefighter.rest.domain.filesystem.data.dto.FileSystemItemUpdate;
import de.filefighter.rest.domain.filesystem.data.dto.FolderContents;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.hateoas.EntityModel;

public interface FileSystemRestServiceInterface {
    EntityModel<FolderContents> getContentsOfFolderByIdAndAccessToken(long fsItemId, String accessToken);
    EntityModel<?> getInfoAboutFileOrFolderByIdAndAccessToken(long fsItemId, String accessToken);
    EntityModel<?> findFileOrFolderByNameAndAccessToken(String name, String accessToken);
    EntityModel<?> uploadFileSystemItemWithAccessToken(FileSystemItemUpdate fileSystemItemUpdate, String accessToken);
    EntityModel<?> updatedFileSystemItemWithIdAndAccessToken(FileSystemItemUpdate fileSystemItemUpdate, String accessToken);
    EntityModel<ServerResponse> deleteFileSystemItemWithIdAndAccessToken(long fsItemId, String accessToken);
}
