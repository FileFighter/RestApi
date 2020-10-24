package de.filefighter.rest.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    void errorHandlingDoesWork() throws Exception {
        mockMvc.perform(get(RestErrorController.DEFAULT_ERROR_PATH))
                .andExpect(status().is(404))
                .andReturn();
    }

    @Test
    void getErrorPath() {
        String expectedPath = RestErrorController.DEFAULT_ERROR_PATH;
        String actualPath = restErrorController.getErrorPath();

        assertEquals(expectedPath, actualPath);
    }
}