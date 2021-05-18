package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.rest.ServerResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class FileSystemItemCouldNotBeDownloadedAdvise {

    @ResponseBody
    @ExceptionHandler(FileSystemItemCouldNotBeDownloadedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ServerResponse> fileSystemContentsNotAccessibleAdvise(FileSystemItemCouldNotBeDownloadedException ex) {
        log.warn(ex.getMessage());
        return new ResponseEntity<>(new ServerResponse(HttpStatus.BAD_REQUEST, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}