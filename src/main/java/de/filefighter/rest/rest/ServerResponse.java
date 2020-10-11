package de.filefighter.rest.rest;

import lombok.Data;
import lombok.Getter;
import org.springframework.hateoas.EntityModel;

@Getter
public class ServerResponse {
    private final String message;
    private final String status;

    public ServerResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public EntityModel<ServerResponse> toModel() {
        return EntityModel.of(this);
    }
}
