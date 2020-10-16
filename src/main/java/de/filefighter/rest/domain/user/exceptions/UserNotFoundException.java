package de.filefighter.rest.domain.user.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(long id) {
        super("Could not find employee " + id);
    }

    public UserNotFoundException(String string) {
        super(string);
    }
}
