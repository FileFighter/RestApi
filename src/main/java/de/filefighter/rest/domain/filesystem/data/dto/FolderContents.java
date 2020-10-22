package de.filefighter.rest.domain.filesystem.data.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "FolderContentsBuilder", builderMethodName = "create")
public class FolderContents {
    private final Folder[] folders;
    private final File[] files;
}
