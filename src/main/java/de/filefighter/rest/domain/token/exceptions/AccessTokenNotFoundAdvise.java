package de.filefighter.rest.domain.token.exceptions;

import de.filefighter.rest.domain.user.exceptions.UserAlreadyExistsAdvise;
import de.filefighter.rest.rest.ServerResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AccessTokenNotFoundAdvise {
    @ResponseBody
    @ExceptionHandler(AccessTokenNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)

    ResponseEntity<ServerResponse> tokenNotFoundAdvise(AccessTokenNotFoundException ex) {
        LoggerFactory.getLogger(UserAlreadyExistsAdvise.class).warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse("denied", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
