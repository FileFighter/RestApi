package de.filefighter.rest.domain.filesystem.data.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "filesystem")
@Builder
public class FileSystemEntity {

    @MongoId
    private String mongoId;
    @Builder.Default
    private long fileSystemId = -1;
    private String name;
    private String path;
    @Builder.Default
    private long typeId = -1;
    private String mimeType;
    private double size;
    private long lastUpdated;
    @Builder.Default
    private long lastUpdatedBy = -1;
    @Builder.Default
    private boolean isFile = true;
    @Builder.Default
    private long ownerId = -1;
    @Builder.Default
    private long[] visibleForGroupIds = new long[0];
    @Builder.Default
    private long[] editableFoGroupIds = new long[0];
    @Builder.Default
    private long[] visibleForUserIds = new long[0];
    @Builder.Default
    private long[] editableForUserIds = new long[0];
    @Builder.Default
    private long[] itemIds = new long[0];

}
