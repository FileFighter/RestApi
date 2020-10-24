package de.filefighter.rest.config;

import de.filefighter.rest.domain.user.data.persistance.UserEntitiy;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class PrepareDataBaseProd {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareDataBaseProd.class);

    @Bean
    CommandLineRunner initUserDataBase(UserRepository repository) {

        //Note: when the admin user changes his/her password, a new refreshToken will be created.
        return args -> {
            LOG.info("Starting with clean user collection.");
            repository.deleteAll();
            LOG.info("Preloading default admin user: " + repository.save(new UserEntitiy(0L, "admin", "admin", "refreshToken1234", 0, 1)));
            LOG.info("Loading Users" + (repository.findAll().size() == 1 ? " was successful." : " failed."));
        };
    }
}