package de.filefighter.rest.rest.exceptions;

import de.filefighter.rest.rest.ServerResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Log4j2
@ControllerAdvice
public class RequestDidntMeetFormalRequirementsAdvise {

    @ResponseBody
    @ExceptionHandler(RequestDidntMeetFormalRequirementsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ServerResponse> requestDidntMeetFormalRequirements(RequestDidntMeetFormalRequirementsException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
