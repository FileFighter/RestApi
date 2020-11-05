package de.filefighter.rest.domain.user.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(){
        super("User not found.");
    }

    public UserNotFoundException(long id) {
        super("Could not find user " + id);
    }

    public UserNotFoundException(String string) {
        super(string);
    }
}
