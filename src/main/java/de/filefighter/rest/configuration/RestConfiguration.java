package de.filefighter.rest.configuration;

public class RestConfiguration {

    //Custom static constants
    public static final String BASE_API_URI = "v1";
    public static final String AUTHORIZATION_BASIC_PREFIX = "Basic ";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_ACCESS_TOKEN_COOKIE = "token";
    public static final String FS_BASE_URI = "/filesystem/";
    public static final String FS_PATH_HEADER = "X-FF-PATH";
    public static final String FS_CURRENT_ID_HEADER = "X-FF-CURRENT";
    public static final String FS_DOWNLOAD_NAME_HEADER = "X-FF-NAME";
    public static final String USER_BASE_URI = "/users/";
    public static final String DEFAULT_ERROR_URI = "/error";
    public static final long RUNTIME_USER_ID = 0;

    private RestConfiguration() {
        // Cannot be instantiated.
    }
}
