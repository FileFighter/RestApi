package de.filefighter.rest.configuration;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestConfiguration {

    //Custom static constants
    public static final String BASE_API_URI = "/api/v1/";
    public static final String AUTHORIZATION_BASIC_PREFIX = "Basic: ";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer: ";
    public static final String FS_BASE_URI = "/filesystem/";
    public static final String USER_BASE_URI = "/users/";

    @Bean
    public WebMvcConfigurer configurer(){

        return new WebMvcConfigurer(){
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/*")
                        .allowedOrigins("*");
            }
        };
    }
}
