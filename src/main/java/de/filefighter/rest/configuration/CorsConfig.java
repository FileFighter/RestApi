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

    private final ArrayList<String> allowedMethods;

    public CorsConfig() {
        this.allowedMethods = new ArrayList<>();
        allowedMethods.add("HEAD");
        allowedMethods.add("GET");
        allowedMethods.add("POST");
        allowedMethods.add("PUT");
        allowedMethods.add("DELETE");
        allowedMethods.add("OPTIONS");
    }

    @Bean
    @Profile({"dev","stage"})
    public CorsFilter corsFilterDev() {
        final CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        ArrayList<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add("*");
        config.setAllowedOrigins(allowedOrigins);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    @Profile({"prod","test"})
    public CorsFilter corsFilterProd() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(allowedMethods);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
