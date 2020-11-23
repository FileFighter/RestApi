package de.filefighter.rest.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static de.filefighter.rest.configuration.RestConfiguration.DEFAULT_ERROR_PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestErrorControllerUnitTest {

    private MockMvc mockMvc;
    private RestErrorController restErrorController;

    @BeforeEach
    void setUp() {
        restErrorController = new RestErrorController();
        mockMvc = MockMvcBuilders.standaloneSetup(restErrorController).build();
    }

    @Test
    void errorHandlingDoesWork() {
        assertDoesNotThrow(() ->
                mockMvc.perform(get(DEFAULT_ERROR_PATH))
                        .andExpect(status().is(404))
                        .andReturn());
    }

    @Test
    void getErrorPath() {
        String actualPath = restErrorController.getErrorPath();

        assertEquals(DEFAULT_ERROR_PATH, actualPath);
    }
}