package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.rest.ServerResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class FileSystemContentsNotAccessibleAdvise {

    @ResponseBody
    @ExceptionHandler(FileSystemContentsNotAccessibleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ServerResponse> fileSystemContentsNotAccessibleAdvise(FileSystemContentsNotAccessibleException ex) {
        LoggerFactory.getLogger(FileSystemContentsNotAccessibleException.class).warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
