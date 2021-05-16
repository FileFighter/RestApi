package de.filefighter.rest.configuration;

import de.filefighter.rest.RestApplication;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = RestApplication.class)
class SecurityTest {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    SecurityTest(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    void works() {
        String rawPw = passwordEncoder.encode("86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126"); // baum
        String hashedPw = passwordEncoder.encode(rawPw);
        String doItAgain = passwordEncoder.encode(rawPw);

        log.info("raw {}", rawPw);
        log.info("hashed0 {}", hashedPw);
        log.info("hashed1 {}", doItAgain);

        assertTrue(passwordEncoder.matches(rawPw, hashedPw));
        assertTrue(passwordEncoder.matches(rawPw, doItAgain));
    }
}