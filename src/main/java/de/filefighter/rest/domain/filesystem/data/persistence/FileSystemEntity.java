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
    private long fileSystemId;
    private String name;
    private String path;
    private long typeId;
    private double size;
    private boolean isFile;
    private long createdByUserId; //uploadedBy
    private long lastUpdated;
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