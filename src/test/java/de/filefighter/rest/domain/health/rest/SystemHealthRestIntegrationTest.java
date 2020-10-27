package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistance.UserEntitiy;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SystemHealthRestIntegrationTest {

    private final Logger LOG = LoggerFactory.getLogger(SystemHealthRestIntegrationTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileSystemRepository fileSystemRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;


    @BeforeEach
    public void cleanDbs(){
        LOG.info("Cleaning Databases.");
        userRepository.deleteAll();
        fileSystemRepository.deleteAll();
        accessTokenRepository.deleteAll();
    }

    @Test
    public void healthCheckShouldReturnUptime() {
        LOG.info("Running: healthCheckShouldReturnUptime");
        String jsonString = this.restTemplate.getForObject("http://localhost:" + port + "/health", String.class);
        assertTrue(jsonString.contains("uptimeInSeconds"));
    }

    @Test
    public void healthCheckShouldReturnUserCount() {
        LOG.info("Running: healthCheckShouldReturnUserCount");
        String jsonString = this.restTemplate.getForObject("http://localhost:" + port + "/health", String.class);
        assertTrue(jsonString.contains("userCount"));
    }

    /*@Test
    public void healthCheckShouldReturnCorrectUserCount() {
        LOG.info("Running: healthCheckShouldReturnCorrectUserCount");
        LOG.info("Preloading default admin user: " + userRepository.save(new UserEntitiy(0L, "admin", "admin", "refreshToken1234", 0, 1)));
        String jsonString = this.restTemplate.getForObject("http://localhost:" + port + "/health", String.class);
        assertTrue(jsonString.contains("userCount") && jsonString.contains(":1"));
    }*/
}