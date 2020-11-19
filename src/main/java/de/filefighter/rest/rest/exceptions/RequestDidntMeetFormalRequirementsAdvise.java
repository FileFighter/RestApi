package de.filefighter.rest.rest.exceptions;

import de.filefighter.rest.rest.ServerResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RequestDidntMeetFormalRequirementsAdvise {

    @ResponseBody
    @ExceptionHandler(RequestDidntMeetFormalRequirementsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ServerResponse> requestDidntMeetFormalRequirements(RequestDidntMeetFormalRequirementsException ex) {
        LoggerFactory.getLogger(RequestDidntMeetFormalRequirementsException.class).warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
