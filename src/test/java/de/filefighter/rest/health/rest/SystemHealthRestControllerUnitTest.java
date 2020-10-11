package de.filefighter.rest.health.rest;

import de.filefighter.rest.rest.RestErrorController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemHealthRestControllerUnitTest {

    private static MockMvc mockMvc;
    private static SystemHealthRestService systemHealthRestServiceMock = mock(SystemHealthRestService.class);
    private static SystemHealthRestController systemHealthRestController;

    @BeforeAll
    public static void setUp() {
        systemHealthRestController = new SystemHealthRestController(systemHealthRestServiceMock);
        mockMvc = MockMvcBuilders.standaloneSetup(systemHealthRestController).build();
    }

    @Test
    void getSystemHealthInfo() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn();

        verify(systemHealthRestServiceMock, times(1)).getSystemHealth();
    }
}