package de.filefighter.rest.domain.token.exceptions;

import de.filefighter.rest.rest.ServerResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Log4j2
public class AccessTokenNotFoundAdvise {
    @ResponseBody
    @ExceptionHandler(AccessTokenNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)

    ResponseEntity<ServerResponse> tokenNotFoundAdvise(AccessTokenNotFoundException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
