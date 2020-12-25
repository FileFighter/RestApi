package de.filefighter.rest.domain.common.exceptions;

public interface FileFighterException {
    static String getErrorMessagePrefix() {
        throw new IllegalArgumentException("Custom exception should overwrite this message.");
    }
}
