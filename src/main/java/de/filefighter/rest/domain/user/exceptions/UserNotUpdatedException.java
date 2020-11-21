package de.filefighter.rest.domain.user.exceptions;

public class UserNotUpdatedException extends RuntimeException{
    public UserNotUpdatedException() {
        super("User could not get updated");
    }

    public UserNotUpdatedException(String reason) {
        super("User could not get updated. "+reason);
    }
}
