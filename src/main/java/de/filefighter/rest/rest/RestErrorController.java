package de.filefighter.rest.rest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@ApiIgnore
@RestController
public class RestErrorController implements ErrorController {

    public static final String DEFAULT_ERROR_PATH = "/error";

    @RequestMapping(value = DEFAULT_ERROR_PATH)
    public EntityModel<ServerResponse> error() {
        return new ServerResponse("denied", "This endpoint does not exist.").toModel();
    }

    @Override
    public String getErrorPath() {
        return DEFAULT_ERROR_PATH;
    }
}
