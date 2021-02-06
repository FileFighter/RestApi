package de.filefighter.rest.domain.common.exceptions;

import org.springframework.dao.DataAccessException;

@SuppressWarnings("java:S110")
public class FileFighterDataException extends DataAccessException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "Internal Error occurred.";

    public FileFighterDataException(String msg) {
        super(ERROR_MESSAGE_PREFIX + " " + msg);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
