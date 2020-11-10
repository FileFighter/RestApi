package de.filefighter.rest.domain.user.exceptions;

public class UserNotAuthenticatedException extends RuntimeException{
    public UserNotAuthenticatedException(String reason){
        super("User could not be authenticated. "+reason);
    }

    public UserNotAuthenticatedException(long id){
        super("User with the id "+id+" could not be authenticated.");
    }
}
