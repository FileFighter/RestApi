package de.filefighter.rest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;

@Configuration
public class RestConfiguration {

    //Custom static constants
    public static final String BASE_API_URI = "v1";
    public static final String AUTHORIZATION_BASIC_PREFIX = "Basic ";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    public static final String FS_BASE_URI = "/filesystem/";
    public static final String FS_PATH_HEADER = "X-FF-PATH";
    public static final String USER_BASE_URI = "/users/";
    public static final String DEFAULT_ERROR_PATH = "/error";

    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return new CorsFilter(source);
    }
}
