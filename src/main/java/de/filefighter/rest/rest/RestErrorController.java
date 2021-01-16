package de.filefighter.rest.rest;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.filefighter.rest.configuration.RestConfiguration.DEFAULT_ERROR_URI;

@Hidden
@RestController
public class RestErrorController implements ErrorController {

    @GetMapping(value = DEFAULT_ERROR_URI)
    public ResponseEntity<ServerResponse> error() {
        return new ResponseEntity<>(new ServerResponse(HttpStatus.NOT_FOUND, "This endpoint does not exist."), HttpStatus.NOT_FOUND);
    }

    @Override
    public String getErrorPath() {
        return DEFAULT_ERROR_URI;
    }
}
