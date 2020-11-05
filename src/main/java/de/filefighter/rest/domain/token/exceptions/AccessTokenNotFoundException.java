package de.filefighter.rest.domain.token.exceptions;

public class AccessTokenNotFoundException extends RuntimeException {

    public AccessTokenNotFoundException(String reason) {
        super(reason);
    }
}
