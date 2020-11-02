package de.filefighter.rest;

import de.filefighter.rest.domain.filesystem.rest.FileSystemRestController;
import de.filefighter.rest.domain.health.rest.SystemHealthRestController;
import de.filefighter.rest.domain.permission.rest.PermissionRestController;
import de.filefighter.rest.domain.user.rest.UserRestController;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestApplicationIntegrationTest {

	@Autowired
    SystemHealthRestController healthController;

    @Autowired
	UserRestController userController;

    @Autowired
	FileSystemRestController fileSystemRestController;

    @Autowired
	PermissionRestController permissionRestController;

	@Test
	public void contextLoads() {
	    assertThat(healthController).isNotNull();
		assertThat(userController).isNotNull();
		assertThat(fileSystemRestController).isNotNull();
		assertThat(permissionRestController).isNotNull();
	}
}