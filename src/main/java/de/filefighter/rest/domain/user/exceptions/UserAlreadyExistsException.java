package de.filefighter.rest.domain.user.exceptions;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(Integer id) {
        super("A employee already exists with the id " + id);
    }

    public UserAlreadyExistsException(String string) {
        super(string);
    }
}
