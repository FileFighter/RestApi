package de.filefighter.rest.rest.exceptions;

import de.filefighter.rest.domain.health.business.SystemHealthBusinessService;
import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.rest.ServerResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Log4j2
@ControllerAdvice
public class DataBaseExceptionAdvise {

    private final SystemHealthBusinessService systemHealthBusinessService;

    @Autowired
    public DataBaseExceptionAdvise(SystemHealthBusinessService systemHealthBusinessService) {
        this.systemHealthBusinessService = systemHealthBusinessService;
    }

    @ResponseBody
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseEntity<ServerResponse> requestDidntMeetFormalRequirements(DataAccessException ex) {
        log.warn(ex.getMessage());
        systemHealthBusinessService.triggerIntegrityChange(SystemHealth.DataIntegrity.UNSTABLE);
        return new ResponseEntity<>(new ServerResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
