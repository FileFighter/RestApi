package de.filefighter.rest.domain.user.exceptions;

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
class UserNotRegisteredAdvise {

    @ResponseBody
    @ExceptionHandler(UserNotRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ResponseEntity<ServerResponse> userNotRegisteredHandler(UserNotRegisteredException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse(HttpStatus.CONFLICT, ex.getMessage()), HttpStatus.CONFLICT);
    }
}
