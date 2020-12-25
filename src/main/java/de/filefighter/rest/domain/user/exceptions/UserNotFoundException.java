package de.filefighter.rest.domain.user.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class UserNotFoundException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "User not found.";

    public UserNotFoundException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public UserNotFoundException(long userId) {
        super(ERROR_MESSAGE_PREFIX + " UserId was " + userId);
    }

    public UserNotFoundException(String username) {
        super(ERROR_MESSAGE_PREFIX + " Username was " + username);
    }

    @Override
    public String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
