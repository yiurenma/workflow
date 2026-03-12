package com.workflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.controller.domain.WorkFlow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Workflow API. Verifies POST/GET round-trip and data storage correctness.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String APPLICATION_NAME = "ITEST_APP";

    @BeforeEach
    void cleanup() throws Exception {
        mockMvc.perform(delete("/api/workflow").param("applicationName", APPLICATION_NAME));
    }

    @Nested
    @DisplayName("POST then GET round-trip")
    class PostGetRoundTrip {

        @Test
        @DisplayName("POST workflow and GET returns equivalent structure - verifies data storage correctness")
        void postWorkflowThenGetReturnsEquivalentData() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();
            String requestJson = objectMapper.writeValueAsString(requestBody);

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APPLICATION_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk());

            MvcResult getResult = mockMvc.perform(get("/api/workflow").param("applicationName", APPLICATION_NAME))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got = objectMapper.readValue(getResult.getResponse().getContentAsString(), WorkFlow.class);
            assertNotNull(got);
            assertEquals(requestBody.getPluginList().size(), got.getPluginList().size(),
                    "Plugin count should match after round-trip");
            for (int i = 0; i < requestBody.getPluginList().size(); i++) {
                var expected = requestBody.getPluginList().get(i);
                var actual = got.getPluginList().get(i);
                assertEquals(expected.getId(), actual.getId(), "Plugin id at index " + i);
                assertEquals(expected.getDescription(), actual.getDescription(), "Plugin description at index " + i);
                assertEquals(expected.getAction().getType(), actual.getAction().getType(), "Action type at index " + i);
                assertEquals(expected.getRuleList().size(), actual.getRuleList().size(), "Rule count at index " + i);
                for (int j = 0; j < expected.getRuleList().size(); j++) {
                    assertEquals(expected.getRuleList().get(j).getKey(), actual.getRuleList().get(j).getKey(),
                            "Rule key at plugin " + i + " rule " + j);
                }
            }
            assertNotNull(got.getUiMapList(), "uiMapList should be present");
        }
    }

    @Nested
    @DisplayName("Update and delete")
    class UpdateAndDelete {

        @Test
        @DisplayName("POST then DELETE then GET returns 400")
        void deleteRemovesWorkflow() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();
            String requestJson = objectMapper.writeValueAsString(requestBody);

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APPLICATION_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/workflow").param("applicationName", APPLICATION_NAME))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/workflow").param("applicationName", APPLICATION_NAME))
                    .andExpect(status().isBadRequest());
        }
    }

    private WorkFlow loadTestWorkflow() throws IOException {
        String json = new String(new ClassPathResource("workflow-integration-test-data.json")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return objectMapper.readValue(json, WorkFlow.class);
    }
}
