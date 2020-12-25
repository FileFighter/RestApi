package de.filefighter.rest.domain.common.exceptions;

import org.springframework.core.NestedRuntimeException;

public class FileFighterDataException extends NestedRuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "Internal Error occurred.";

    public FileFighterDataException(String msg) {
        super(ERROR_MESSAGE_PREFIX + " " + msg);
    }

    @Override
    public String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
