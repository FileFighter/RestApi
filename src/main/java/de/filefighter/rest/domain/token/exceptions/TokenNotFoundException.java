package de.filefighter.rest.domain.token.exceptions;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException(String reason) {
        super(reason);
    }
}
