package de.filefighter.rest.domain.user.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class UserNotRegisteredException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "User could not be registered.";

    public UserNotRegisteredException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public UserNotRegisteredException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    @Override
    public String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
