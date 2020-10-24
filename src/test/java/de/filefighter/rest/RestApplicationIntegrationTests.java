package de.filefighter.rest;

import de.filefighter.rest.domain.health.rest.SystemHealthRestController;
import de.filefighter.rest.domain.user.rest.UserRestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RestApplicationIntegrationTests {

    @Autowired
    SystemHealthRestController healthController;

    @Autowired
	UserRestController userController;

	@Test
	void contextLoads() {
	    assertThat(healthController).isNotNull();
		assertThat(userController).isNotNull();
	}
}