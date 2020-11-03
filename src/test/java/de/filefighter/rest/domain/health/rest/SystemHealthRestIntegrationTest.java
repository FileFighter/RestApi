package de.filefighter.rest.domain.health.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SystemHealthRestIntegrationTest {

    @LocalServerPort
    private int port;

    private final Logger LOG = LoggerFactory.getLogger(SystemHealthRestIntegrationTest.class);
    private final ObjectMapper objectMapper;
    private final TestRestTemplate restTemplate;
    private final UserRepository userRepository;
    private final FileSystemRepository fileSystemRepository;
    private final AccessTokenRepository accessTokenRepository;

    @Autowired
    public SystemHealthRestIntegrationTest(TestRestTemplate restTemplate, UserRepository userRepository, FileSystemRepository fileSystemRepository, AccessTokenRepository accessTokenRepository) {
        this.objectMapper = new ObjectMapper();
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.fileSystemRepository = fileSystemRepository;
        this.accessTokenRepository = accessTokenRepository;
    }

    @BeforeEach
    public void cleanDbs() {
        LOG.info("Cleaning Databases.");
        userRepository.deleteAll();
        fileSystemRepository.deleteAll();
        accessTokenRepository.deleteAll();
    }

    @Test
    public void healthCheckShouldContainVariablesAndCorrectValues() throws JsonProcessingException {
        LOG.info("Preloading default admin user: " + userRepository.save(UserEntity
                .builder()
                .userId(0L)
                .username("admin")
                .password("admin")
                .refreshToken("refreshToken1234")
                .groupIds(new long[]{0, 1})
                .build()));
        String jsonString = this.restTemplate.getForObject("http://localhost:" + port + "/health", String.class);

        // Note when a key does not exist, a NullPointerException will be thrown.
        JsonNode root = objectMapper.readTree(jsonString);
        String uptime = root.get("uptimeInSeconds").asText();
        String userCount = root.get("userCount").asText();

        assertTrue(Integer.parseInt(uptime) > 0);
        assertEquals(1, Integer.parseInt(userCount));
    }
}