package de.filefighter.rest.domain.user.exceptions;

import de.filefighter.rest.rest.ServerResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class UserNotFoundAdvice {

	@ResponseBody
	@ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ResponseEntity<ServerResponse> employeeNotFoundHandler(UserNotFoundException ex) {
		LoggerFactory.getLogger(UserAlreadyExistsAdvise.class).warn(ex.getMessage());
		return new ResponseEntity<>(new ServerResponse("Denied", ex.getMessage()), HttpStatus.NOT_FOUND);
	}
}