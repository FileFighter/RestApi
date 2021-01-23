package de.filefighter.rest.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Value("${filefighter.version}")
    String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("FileFighter REST Endpoint")
                .version(version)
                .description("FileFighter Spring-Boot REST Endpoint")
                .contact(new Contact()
                        .email("dev@filefighter.de")
                        .url("https://blog.filefighter.de")
                        .name("FileFighter Development Team"))
                .license(new License().name("GPL v3.0").url("https://github.com/filefighter/restapi/blob/master/LICENSE")));
    }
}
