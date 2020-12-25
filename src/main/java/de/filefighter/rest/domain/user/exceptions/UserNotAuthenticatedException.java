package de.filefighter.rest.domain.user.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class UserNotAuthenticatedException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "User could not be authenticated.";

    public UserNotAuthenticatedException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    public UserNotAuthenticatedException(long userId) {
        super(ERROR_MESSAGE_PREFIX+" UserId was "+userId);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
