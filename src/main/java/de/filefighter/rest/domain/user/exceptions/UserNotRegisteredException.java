package de.filefighter.rest.domain.user.exceptions;

public class UserNotRegisteredException extends RuntimeException{

    public UserNotRegisteredException() {
        super("User could not be registered.");
    }

    public UserNotRegisteredException(String reason) {
        super("User could not be registered. "+reason);
    }
}
