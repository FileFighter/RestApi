package de.filefighter.rest.rest;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServerResponse {
    private final String message;
    private final String status;

    public ServerResponse(HttpStatus status, String message) {
        this.status = status.getReasonPhrase();
        this.message = message;
    }
}
