package de.filefighter.rest.configuration;

import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Instant;

import static de.filefighter.rest.domain.filesystem.type.FileSystemType.FOLDER;
import static de.filefighter.rest.domain.filesystem.type.FileSystemType.TEXT;
import static de.filefighter.rest.domain.user.group.Groups.*;

@Log4j2
@Configuration
public class PrepareDataBase {

    private static final String MESSAGE_ON_SUCCESS = "was successful.";
    private static final String MESSAGE_ON_FAILURE = "failed.";

    @Value("${server.port}")
    int serverPort;

    @Value("${filefighter.version}")
    String version;

    @Value("${filefighter.date}")
    String date;

    @SuppressWarnings("squid:S106")
    @Bean
    @Profile({"dev", "prod"})
    CommandLineRunner veryImportantFileFighterStartScript() {
        return args -> {
            System.out.println();
            System.out.println("-------------------------------< REST API >-------------------------------");
            System.out.println();
            System.out.println("  _____   _   _          _____   _           _       _");
            System.out.println(" |  ___| (_) | |   ___  |  ___| (_)   __ _  | |__   | |_    ___   _ __");
            System.out.println(" | |_    | | | |  / _ \\ | |_    | |  / _  | | '_ \\  | __|  / _ \\ | '__|");
            System.out.println(" |  _|   | | | | |  __/ |  _|   | | | (_| | | | | | | |_  |  __/ | |");
            System.out.println(" |_|     |_| |_|  \\___| |_|     |_|  \\__, | |_| |_|  \\__|  \\___| |_|");
            System.out.println("                                     |___/");
            System.out.println("                 Version v" + version + " Last updated at " + date + "");
            System.out.println("               Developed by Gimleux, Valentin, Open-Schnick.");
            System.out.println("               Development Blog: https://blog.filefighter.de");
            System.out.println("        The code can be found at: https://www.github.com/filefighter");
            System.out.println("                    Running on http://localhost:" + serverPort);
            System.out.println();
            System.out.println("-------------------------------< REST API >-------------------------------");
            System.out.println();
        };
    }

    @Bean
    CommandLineRunner cleanDataBase(UserRepository userRepository, FileSystemRepository fileSystemRepository, AccessTokenRepository accessTokenRepository) {
        return args -> {
            log.info("Starting with clean user collection.");
            userRepository.deleteAll();
            log.info("Starting with clean fileSystem collection.");
            fileSystemRepository.deleteAll();
            log.info("Starting with clean accessToken collection.");
            accessTokenRepository.deleteAll();
        };
    }

    @Bean
    CommandLineRunner createRuntimeUser(UserRepository userRepository) {
        return args -> log.info("Adding system runtime user. {}", userRepository.save(UserEntity
                .builder()
                .userId(0L)
                .username("FileFighter")
                .lowercaseUsername("filefighter")
                .password(null)
                .refreshToken(null)
                .groupIds(new long[]{SYSTEM.getGroupId()})
                .build()));
    }

    @Bean
    @Profile("prod")
    CommandLineRunner initDataBaseProd(UserRepository userRepository, FileSystemRepository fileSystemRepository) {
        return args -> {
            log.info("Preloading default admin user: {}.", userRepository.save(UserEntity
                    .builder()
                    .userId(1L)
                    .username("Admin")
                    .lowercaseUsername("admin")
                    .password("admin")
                    .refreshToken("refreshToken1234")
                    .groupIds(new long[]{FAMILY.getGroupId(), ADMIN.getGroupId()})
                    .build()));

            log.info("Preloading default fsStructure: {} {}.", fileSystemRepository.save(FileSystemEntity
                            .builder()
                            .createdByUserId(0)
                            .fileSystemId(0)
                            .isFile(false)
                            .path("/")
                            .itemIds(new long[0])
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("HOME_Admin")
                            .size(0)
                            .typeId(FOLDER.getId())
                            .visibleForGroupIds(new long[]{UNDEFINED.getGroupId(), FAMILY.getGroupId(), ADMIN.getGroupId()})
                            .itemIds(new long[]{1})
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(1)
                            .fileSystemId(1)
                            .isFile(true)
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("dummyFile.txt")
                            .size(420)
                            .typeId(TEXT.getId())
                            .editableFoGroupIds(new long[]{FAMILY.getGroupId()})
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId()})
                            .build()));

            log.info("Inserting Users {}", (userRepository.findAll().size() == 1 ? MESSAGE_ON_SUCCESS : MESSAGE_ON_FAILURE));
            log.info("Inserting fsItems {}", (fileSystemRepository.findAll().size() == 1 ? MESSAGE_ON_SUCCESS : MESSAGE_ON_FAILURE));
        };
    }

    @Bean
    @Profile("dev")
    CommandLineRunner initDataBaseDev(UserRepository userRepository, AccessTokenRepository accessTokenRepository, FileSystemRepository fileSystemRepository) {
        return args -> {
            log.info("Preloading default users: {} {}.",
                    userRepository.save(UserEntity
                            .builder()
                            .userId(1)
                            .username("user")
                            .lowercaseUsername("user")
                            .password("1234")
                            .refreshToken("rft1234")
                            .groupIds(new long[]{ADMIN.getGroupId()})
                            .build()),
                    userRepository.save(UserEntity
                            .builder()
                            .userId(2)
                            .username("user1")
                            .lowercaseUsername("user1")
                            .password("12345")
                            .refreshToken("rft")
                            .groupIds(new long[]{FAMILY.getGroupId()})
                            .build()));

            log.info("Preloading default tokens: {} {}",
                    accessTokenRepository.save(AccessTokenEntity
                            .builder()
                            .userId(1)
                            .value("token")
                            .validUntil(Instant.now().getEpochSecond() + AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS)
                            .build()),
                    accessTokenRepository.save(AccessTokenEntity
                            .builder()
                            .userId(2)
                            .value("token1234")
                            .validUntil(Instant.now().getEpochSecond() + AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS)
                            .build()));

            log.info("Preloading default fsItems: {} {} {}.",
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(0)
                            .fileSystemId(0)
                            .isFile(false)
                            .path("/")
                            .itemIds(new long[]{2})
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("HOME_User")
                            .size(420)
                            .typeId(FOLDER.getId())
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId(), ADMIN.getGroupId()})
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(0)
                            .fileSystemId(1)
                            .isFile(false)
                            .path("/")
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("HOME_User1")
                            .size(420)
                            .typeId(FOLDER.getId())
                            .visibleForGroupIds(new long[]{UNDEFINED.getGroupId(), FAMILY.getGroupId(), ADMIN.getGroupId()})
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(1)
                            .fileSystemId(2)
                            .isFile(true)
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("dummyFile.txt")
                            .size(420)
                            .typeId(TEXT.getId())
                            .editableFoGroupIds(new long[]{FAMILY.getGroupId()})
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId()})
                            .build()));

            log.info("Inserting FileSystemItems {}", (fileSystemRepository.findAll().size() == 3 ? MESSAGE_ON_SUCCESS : MESSAGE_ON_FAILURE));
            log.info("Inserting token {}", (accessTokenRepository.findAll().size() == 2 ? MESSAGE_ON_SUCCESS : MESSAGE_ON_FAILURE));
            log.info("Inserting Users {}", (userRepository.findAll().size() == 2 ? MESSAGE_ON_SUCCESS : MESSAGE_ON_FAILURE));
        };
    }
}