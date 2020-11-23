package de.filefighter.rest.rest.exceptions;

import org.springframework.core.NestedRuntimeException;

public class FileFighterDataException extends NestedRuntimeException {
    public FileFighterDataException(String msg) {
        super("Internal Error occurred. " + msg);
    }
}
