package de.filefighter.rest.configuration;

import de.filefighter.rest.domain.common.exceptions.FileFighterDataException;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static de.filefighter.rest.configuration.RestConfiguration.RUNTIME_USER_ID;
import static de.filefighter.rest.domain.filesystem.type.FileSystemType.*;
import static de.filefighter.rest.domain.user.group.Group.UNDEFINED;
import static de.filefighter.rest.domain.user.group.Group.*;

@SuppressWarnings({"squid:S1192", "squid:S106"})
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

    @Bean
    @Profile({"dev", "prod"})
    @Autowired
    CommandLineRunner veryImportantFileFighterStartScript(Environment environment) {
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
            System.out.println();
            System.out.println("Version v" + version + ", Last updated at: " + date + "");
            System.out.println("Environment: " + Arrays.toString(environment.getActiveProfiles()));
            System.out.println("Started: " + new Date().toString());
            System.out.println("Running on http://localhost:" + serverPort);
            System.out.println();
            System.out.println("Developed by Gimleux, Valentin, Open-Schnick.");
            System.out.println("Development Blog: https://blog.filefighter.de");
            System.out.println("The code can be found at: https://www.github.com/filefighter");
            System.out.println();
            System.out.println("-------------------------------< REST API >-------------------------------");
            System.out.println();

            /*
            System.out.println("╭---╮")
            System.out.println("|   |")
            System.out.println("╰---╯")
            */
        };
    }

    @Bean
    @Profile("prod")
    CommandLineRunner initDataBaseProd(UserRepository userRepository, FileSystemRepository fileSystemRepository, AccessTokenRepository accessTokenRepository) {
        return args -> {
            ArrayList<UserEntity> foundUsers = (ArrayList<UserEntity>) userRepository.findAll();
            ArrayList<UserEntity> foundFileSystemEntities = (ArrayList<UserEntity>) userRepository.findAll();
            accessTokenRepository.deleteAll(); // Cleanup purposes.

            if (foundUsers.isEmpty() && foundFileSystemEntities.isEmpty()) {
                log.info("Database seems to be empty. Creating new default entities...");
                log.info("Inserting system runtime user: {}", userRepository.save(UserEntity
                        .builder()
                        .userId(RUNTIME_USER_ID)
                        .username("FileFighter")
                        .lowercaseUsername("filefighter")
                        .password(null)
                        .refreshToken(null)
                        .groupIds(new long[]{SYSTEM.getGroupId()})
                        .build()));
                log.info("Inserting default Admin user: {}", userRepository.save(UserEntity
                        .builder()
                        .userId(1)
                        .username("Admin")
                        .lowercaseUsername("admin")
                        .password("admin")
                        .refreshToken("rft1234")
                        .groupIds(new long[]{ADMIN.getGroupId()})
                        .build()));
                log.info("Inserting Home directories and default structure: {} {}.", fileSystemRepository.save(FileSystemEntity
                                .builder()
                                .createdByUserId(RUNTIME_USER_ID)
                                .fileSystemId(0)
                                .isFile(false)
                                .path("/")
                                .itemIds(new long[0])
                                .lastUpdated(Instant.now().getEpochSecond())
                                .name("HOME_Admin")
                                .size(420)
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

                if (userRepository.findAll().size() == 2) {
                    log.info("Inserting Users " + MESSAGE_ON_SUCCESS);
                } else {
                    log.error("Inserting Users " + MESSAGE_ON_FAILURE);
                }

                if (fileSystemRepository.findAll().size() == 2) {
                    log.info("Inserting FileSystemEntities " + MESSAGE_ON_SUCCESS);
                } else {
                    log.error("Inserting FileSystemEntities " + MESSAGE_ON_FAILURE);
                }
            } else if (foundUsers.isEmpty() ^ foundFileSystemEntities.isEmpty()) {
                // Exclusive "or".
                throw new FileFighterDataException("The Database failed the sanity check, contact the developers or reinstall FileFighter.");
            } else {
                log.info("Checked Database, found Entities, didn't change anything.");
            }
        };
    }

    @Bean
    @Profile("dev")
    CommandLineRunner initDataBaseDev(UserRepository userRepository, AccessTokenRepository accessTokenRepository, FileSystemRepository fileSystemRepository) {
        return args -> {
            log.info("Starting with clean user collection.");
            userRepository.deleteAll();
            log.info("Starting with clean fileSystem collection.");
            fileSystemRepository.deleteAll();
            log.info("Starting with clean accessToken collection.");
            accessTokenRepository.deleteAll();

            log.info("Inserting system runtime user. {}", userRepository.save(UserEntity
                    .builder()
                    .userId(RUNTIME_USER_ID)
                    .username("FileFighter")
                    .lowercaseUsername("filefighter")
                    .password(null)
                    .refreshToken(null)
                    .groupIds(new long[]{SYSTEM.getGroupId()})
                    .build()));

            log.info("Inserting default users: {} {}.",
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

            log.info("Inserting default tokens: {} {}",
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

            log.info("Inserting default fsItems:\n {}\n {}\n {}\n {}\n {}\n {}.",
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(RUNTIME_USER_ID)
                            .fileSystemId(0)
                            .isFile(false)
                            .path("/")
                            .itemIds(new long[]{2, 3})
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("HOME_User")
                            .size(4866)
                            .typeId(FOLDER.getId())
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId(), ADMIN.getGroupId()})
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(RUNTIME_USER_ID)
                            .fileSystemId(1)
                            .isFile(false)
                            .path("/")
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("HOME_User1")
                            .size(0)
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
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(1)
                            .fileSystemId(3)
                            .isFile(false)
                            .path("/somefolder")
                            .name("SomeFolder")
                            .lastUpdated(Instant.now().getEpochSecond())
                            .size(4446)
                            .typeId(FOLDER.getId())
                            .editableFoGroupIds(new long[]{FAMILY.getGroupId()})
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId()})
                            .itemIds(new long[]{4, 5})
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(1)
                            .fileSystemId(4)
                            .isFile(true)
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("secretFileInSomeFolder.txt")
                            .size(3214)
                            .typeId(TEXT.getId())
                            .editableFoGroupIds(new long[]{FAMILY.getGroupId()})
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId()})
                            .build()),
                    fileSystemRepository.save(FileSystemEntity.builder()
                            .createdByUserId(1)
                            .fileSystemId(5)
                            .isFile(true)
                            .lastUpdated(Instant.now().getEpochSecond())
                            .name("definitelyNotPorn.mp4")
                            .size(1232)
                            .typeId(VIDEO.getId())
                            .editableFoGroupIds(new long[]{FAMILY.getGroupId()})
                            .visibleForGroupIds(new long[]{FAMILY.getGroupId()})
                            .build())
            );

            if (userRepository.findAll().size() == 3) {
                log.info("Inserting Users " + MESSAGE_ON_SUCCESS);
            } else {
                log.error("Inserting Users " + MESSAGE_ON_FAILURE);
            }

            if (fileSystemRepository.findAll().size() == 6) {
                log.info("Inserting FileSystemEntities " + MESSAGE_ON_SUCCESS);
            } else {
                log.error("Inserting FileSystemEntities " + MESSAGE_ON_FAILURE);
            }
            if (accessTokenRepository.findAll().size() == 2) {
                log.info("Inserting AccessToken " + MESSAGE_ON_SUCCESS);
            } else {
                log.error("Inserting AccessToken " + MESSAGE_ON_FAILURE);
            }
        };
    }
}
