package de.filefighter.rest.rest.exceptions;

public class RequestDidntMeetFormalRequirementsException extends RuntimeException{

    public RequestDidntMeetFormalRequirementsException() {
        super("Request didnt meet formal requirements.");
    }

    public RequestDidntMeetFormalRequirementsException(String message) {
        super("Request didnt meet formal requirements. "+message);
    }
}
