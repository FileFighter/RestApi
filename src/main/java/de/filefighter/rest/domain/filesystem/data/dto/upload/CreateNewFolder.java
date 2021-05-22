package de.filefighter.rest.domain.filesystem.data.dto.upload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class CreateNewFolder {
    private final String name;

    @JsonCreator
    public CreateNewFolder(@JsonProperty("name") String name) {
        this.name = name;
    }
}
