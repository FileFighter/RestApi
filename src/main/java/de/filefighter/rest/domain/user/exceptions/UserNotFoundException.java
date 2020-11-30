package de.filefighter.rest.domain.user.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(){
        super("User not found.");
    }

    public UserNotFoundException(long id) {
        super("Could not find user with userId " + id+".");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
