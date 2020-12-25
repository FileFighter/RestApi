package de.filefighter.rest.domain.common.exceptions;

public class RequestDidntMeetFormalRequirementsException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "Request didnt meet formal requirements.";

    public RequestDidntMeetFormalRequirementsException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public RequestDidntMeetFormalRequirementsException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    @Override
    public String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
