package de.filefighter.rest.domain.user.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class UserNotUpdatedException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "User could not get updated";

    public UserNotUpdatedException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public UserNotUpdatedException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    @Override
    public String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
