package de.filefighter.rest.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RestConfiguration {

    //Custom static constants
    public static final String BASE_API_URI = "/api/v1";
    public static final String AUTHORIZATION_BASIC_PREFIX = "Basic ";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final String FS_BASE_URI = "/filesystem/";
    public static final String FS_PATH_HEADER = "X-FF-PATH";
    public static final String USER_BASE_URI = "/users/";

    // PreventInstantiation
    private RestConfiguration() {
    }
}
