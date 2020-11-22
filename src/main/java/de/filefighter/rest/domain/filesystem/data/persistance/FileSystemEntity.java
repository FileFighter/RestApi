package de.filefighter.rest.domain.filesystem.data.persistance;

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
    private long id;
    private String name;
    private String path;
    private long typeId;
    private double size;
    private boolean isFile;
    private long createdByUserId; //uploadedBy
    private long lastUpdated;
    private long[] visibleForGroupIds;
    private long[] editableFoGroupIds;
    private long[] visibleForUserIds;
    private long[] editableForUserIds;
    private long[] itemIds;

}
