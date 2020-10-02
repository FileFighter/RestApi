package de.filefighter.rest.rest;

import lombok.Data;
import org.springframework.hateoas.EntityModel;

@Data
public class ServerResponse {
    private String message;
    private String status;

    public ServerResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public EntityModel<ServerResponse> toModel() {
        return EntityModel.of(this);
    }
}
