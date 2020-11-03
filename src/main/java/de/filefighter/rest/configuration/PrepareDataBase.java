package de.filefighter.rest.configuration;

import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;

@Configuration
public class PrepareDataBase {

    @Value("${server.port}")
    int serverPort;

    private static final Logger LOG = LoggerFactory.getLogger(PrepareDataBase.class);

    @Bean
    CommandLineRunner veryImportantFileFighterStartScript() {
        return args -> {
            System.out.println();
            System.out.println("-------------------------------< REST API >-------------------------------");
            System.out.println();
            System.out.println("  _____   _   _          _____   _           _       _                 ");
            System.out.println(" |  ___| (_) | |   ___  |  ___| (_)   __ _  | |__   | |_    ___   _ __ ");
            System.out.println(" | |_    | | | |  / _ \\ | |_    | |  / _  | | '_ \\  | __|  / _ \\ | '__|");
            System.out.println(" |  _|   | | | | |  __/ |  _|   | | | (_| | | | | | | |_  |  __/ | |   ");
            System.out.println(" |_|     |_| |_|  \\___| |_|     |_|  \\__, | |_| |_|  \\__|  \\___| |_|   ");
            System.out.println("                                     |___/                             ");
            System.out.println("                   Version 0.2 Last updated at 03.11.20                ");
            System.out.println("              Developed by Gimleux, Valentin, Open-Schnick.            ");
            System.out.println("            Development Blog: https://filefighter.github.io            ");
            System.out.println("       The code can be found at: https://www.github.com/filefighter    ");
            System.out.println("                    Running on http://localhost:"+serverPort);
            System.out.println();
            System.out.println("-------------------------------< REST API >-------------------------------");
            System.out.println();
        };
    }

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
                            .userId(0)
                            .username("user")
                            .password("1234")
                            .refreshToken("rft1234")
                            .groupIds(new long[]{0})
                            .build()) +
                    repository.save(UserEntity
                            .builder()
                            .userId(1)
                            .username("user1")
                            .password("12345")
                            .refreshToken("rft")
                            .groupIds(new long[]{-1})
                            .build()));
            LOG.info("Inserting Users" + (repository.findAll().size() == 2 ? " was successful." : " failed."));
        };
    }

    @Bean
    @Profile("dev")
    CommandLineRunner initAccessTokenDataBaseDev(AccessTokenRepository repository) {

        return args -> {
            LOG.info("Preloading default tokens: " +
                    repository.save(AccessTokenEntity
                            .builder()
                            .userId(0)
                            .value("token")
                            .validUntil(Instant.now().getEpochSecond() + AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS)
                            .build()) +
                    repository.save(AccessTokenEntity
                            .builder()
                            .userId(1)
                            .value("token1234")
                            .validUntil(Instant.now().getEpochSecond() + AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS)
                            .build()));
            LOG.info("Inserting token" + (repository.findAll().size() == 2 ? " was successful." : " failed."));
        };
    }
}