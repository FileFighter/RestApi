package de.filefighter.rest.domain.token.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class AccessTokenNotFoundException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "AccessToken could not be found.";

    public AccessTokenNotFoundException(long userId) {
        super(ERROR_MESSAGE_PREFIX + " UserId was " + userId);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
