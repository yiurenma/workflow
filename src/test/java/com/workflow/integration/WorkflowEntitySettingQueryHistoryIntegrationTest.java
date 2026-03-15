package com.workflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.workflow.controller.domain.WorkFlow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Entity setting query/history integration tests")
class WorkflowEntitySettingQueryHistoryIntegrationTest extends AbstractWorkflowIntegrationTest {

    @Nested
    @DisplayName("Lifecycle checks through API combinations")
    class LifecycleChecks {

        @Test
        @DisplayName("Create + update + query history + delete + query empty")
        void createUpdateDeleteAndQueryFromDifferentAngles() throws Exception {
            WorkFlow initial = loadTestWorkflow();
            postWorkflow(APP_NAME, initial);

            List<String> firstQuery = queryApplicationNames("ITEST");
            assertTrue(firstQuery.contains(APP_NAME), "Fuzzy query should include created application");

            WorkFlow updated = loadTestWorkflow();
            updated.setPluginList(updated.getPluginList().subList(0, 2));
            postWorkflow(APP_NAME, updated);

            int historySize = queryHistorySize(APP_NAME);
            assertTrue(historySize >= 2, "History should contain create + update revisions");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());

            List<String> queryAfterDelete = queryApplicationNames(APP_NAME);
            assertEquals(0, queryAfterDelete.size(), "Entity setting query should be empty after delete");

            mockMvc.perform(get("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Fuzzy query behavior")
    class FuzzyQueryChecks {

        @Test
        @DisplayName("Contains + ignore-case query returns expected applications")
        void fuzzyQueryContainsIgnoreCaseWorks() throws Exception {
            postWorkflow(APP_NAME, loadTestWorkflow());

            WorkFlow second = loadTestWorkflow();
            second.setPluginList(second.getPluginList().subList(0, 3));
            postWorkflow(APP_NAME_2, second);

            List<String> broad = queryApplicationNames("itest_app");
            assertTrue(broad.contains(APP_NAME));
            assertTrue(broad.contains(APP_NAME_2));

            List<String> narrow = queryApplicationNames("APP_2");
            assertEquals(1, narrow.size());
            assertEquals(APP_NAME_2, narrow.get(0));
        }
    }

    @Test
    @DisplayName("History API returns 400 when application does not exist exactly once")
    void historyReturns400WhenApplicationMissing() throws Exception {
        mockMvc.perform(get("/api/workflow/entity-setting/history")
                        .param("applicationName", "NOT_EXIST_APP")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    private List<String> queryApplicationNames(String fuzzyApplicationName) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/workflow/entity-setting")
                        .param("applicationName", fuzzyApplicationName)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode content = root.path("content");
        List<String> names = new ArrayList<>();
        for (JsonNode item : content) {
            names.add(item.path("applicationName").asText());
        }
        return names;
    }

    private int queryHistorySize(String applicationName) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/workflow/entity-setting/history")
                        .param("applicationName", applicationName)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("content").size();
    }
}
