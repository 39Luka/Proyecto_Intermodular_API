package org.example.bakeryapi.integration;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequestIdFilterIntegrationTest extends AbstractIntegrationTest {

    @Test
    void health_withoutRequestIdHeader_returnsGeneratedRequestIdHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void health_withRequestIdHeader_echoesSameValue() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header("X-Request-Id", "test-id-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "test-id-123"));
    }
}

