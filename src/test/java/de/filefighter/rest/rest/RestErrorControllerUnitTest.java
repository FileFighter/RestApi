package de.filefighter.rest.rest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestErrorControllerUnitTest {

    private static MockMvc mockMvc;
    private static RestErrorController restErrorController;

    @BeforeAll
    static void setUp() {
        restErrorController = new RestErrorController();
        mockMvc = MockMvcBuilders.standaloneSetup(restErrorController).build();
    }

    @Test
    void errorHandleingDoesWork() throws Exception {
        mockMvc.perform(get(RestErrorController.DEFAULT_ERROR_PATH))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void getErrorPath() {
        String expectedPath = RestErrorController.DEFAULT_ERROR_PATH;
        String actualPath = restErrorController.getErrorPath();

        assertEquals(expectedPath, actualPath);
    }
}