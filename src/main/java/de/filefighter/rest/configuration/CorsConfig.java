package de.filefighter.rest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;

@Configuration
public class CorsConfig {

    // Cors again. For local testing only.
    @Bean
    @Profile("dev")
    public CorsFilter corsFilter(){
        final CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        ArrayList<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("*");
        config.setAllowedOrigins(allowedOrigins);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
