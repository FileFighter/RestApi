package de.filefighter.rest.domain.health.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemHealthRestControllerUnitTest {

    private final SystemHealthRestService systemHealthRestServiceMock = mock(SystemHealthRestService.class);
    private MockMvc mockMvc;
    private SystemHealthRestController systemHealthRestController;

    @BeforeEach
    public void setUp() {
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