package de.filefighter.rest.rest.exceptions;

import org.springframework.dao.DataAccessException;

public class FileFighterDataException extends DataAccessException {

    public FileFighterDataException(String msg) {
        super("Internal Error occurred. " + msg);
    }

    public FileFighterDataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
