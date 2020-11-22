package de.filefighter.rest.rest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static de.filefighter.rest.configuration.RestConfiguration.DEFAULT_ERROR_PATH;

@ApiIgnore
@RestController
public class RestErrorController implements ErrorController {

    @GetMapping(value = DEFAULT_ERROR_PATH)
    public ResponseEntity<ServerResponse> error() {
        return new ResponseEntity<>(new ServerResponse(HttpStatus.NOT_FOUND, "This endpoint does not exist."), HttpStatus.NOT_FOUND);
    }

    @Override
    public String getErrorPath() {
        return DEFAULT_ERROR_PATH;
    }
}
