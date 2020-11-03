package de.filefighter.rest.configuration;

import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class PrepareDataBase {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareDataBase.class);

    @Bean
    CommandLineRunner cleanDataBase(UserRepository userRepository, FileSystemRepository fileSystemRepository, AccessTokenRepository accessTokenRepository) {

        //Note: when the admin user changes his/her password, a new refreshToken will be created.
        return args -> {
            LOG.info("Starting with clean user collection.");
            userRepository.deleteAll();
            LOG.info("Starting with clean fileSystem collection.");
            fileSystemRepository.deleteAll();
            LOG.info("Starting with clean accessToken collection.");
            accessTokenRepository.deleteAll();
        };
    }

    @Bean
    @Profile("prod")
    CommandLineRunner initUserDataBaseProd(UserRepository repository) {

        //Note: when the admin user changes his/her password, a new refreshToken will be created.
        return args -> {
            LOG.info("Preloading default admin user: " + repository.save(UserEntity
                    .builder()
                    .userId(0L)
                    .username("admin")
                    .password("admin")
                    .refreshToken("refreshToken1234")
                    .groupIds(new long[]{0, 1})
                    .build()));
            LOG.info("Inserting Users" + (repository.findAll().size() == 1 ? " was successful." : " failed."));
        };
    }

    @Bean
    @Profile("dev")
    CommandLineRunner initUserDataBaseDev(UserRepository repository) {

        return args -> {
            LOG.info("Preloading default users: " +
                    repository.save(UserEntity
                            .builder()
                            .userId(0L)
                            .username("user")
                            .password("1234")
                            .refreshToken("refreshToken1234")
                            .groupIds(new long[]{0})
                            .build()) +
                    repository.save(UserEntity
                            .builder()
                            .userId(0L)
                            .username("user1")
                            .password("12345")
                            .refreshToken("refreshToken1234")
                            .groupIds(new long[]{0})
                            .build()));
            LOG.info("Inserting Users" + (repository.findAll().size() == 2 ? " was successful." : " failed."));
        };
    }
}