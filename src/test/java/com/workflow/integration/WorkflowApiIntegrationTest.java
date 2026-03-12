package com.workflow.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.controller.domain.Plugin;
import com.workflow.controller.domain.WorkFlow;
import com.workflow.dao.repository.*;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive integration tests for all Workflow API endpoints.
 *
 * Each test group verifies a different layer of correctness:
 *  - POST: DB records created with the exact right counts and field values
 *  - GET:  round-trip data integrity (all fields decoded and returned correctly)
 *  - DELETE: every DB record belonging to the app is removed (no orphans)
 *  - POST (update): old records replaced — no duplicates, no orphan leftovers
 *  - autoCopy: source and target have independent DB records
 *  - Edge cases: error responses and boundary inputs
 *
 * Fixture counts for workflow-integration-test-data.json (10 plugins):
 *   Plugins  : 10  (one WorkflowEntityAndLinkingIdMapping per plugin)
 *   Rules    : 11  (plugins 1–9 have 1 rule each; plugin 10 has 2 rules)
 *   Types    : 10  (one WorkflowType per plugin action)
 *   RuleType : 11  (one WorkflowRuleAndType row per rule, same count as rules)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkflowEntitySettingRepository entitySettingRepo;
    @Autowired
    private WorkflowEntityAndLinkingIdMappingRepository linkingMappingRepo;
    @Autowired
    private WorkflowRuleAndTypeRepository ruleAndTypeRepo;
    @Autowired
    private WorkflowRuleRepository ruleRepo;
    @Autowired
    private WorkflowTypeRepository typeRepo;
    @Autowired
    private WorkflowReportRepository reportRepo;

    private static final String APP      = "ITEST_APP";
    private static final String APP_COPY = "ITEST_APP_COPY";

    // Counts derived from the 10-step fixture file
    private static final int FIXTURE_PLUGIN_COUNT        = 10;
    private static final int FIXTURE_RULE_COUNT          = 11; // plugin 10 has 2 rules
    private static final int FIXTURE_TYPE_COUNT          = 10;
    private static final int FIXTURE_RULE_AND_TYPE_COUNT = 11;

    // ──────────────────────────────────────────────
    // Setup / teardown helpers
    // ──────────────────────────────────────────────

    @BeforeEach
    void cleanup() throws Exception {
        for (String appName : List.of(APP, APP_COPY)) {
            entitySettingRepo.getWorkflowEntitySettingByApplicationName(appName).forEach(s -> {
                // Delete reports first so the API DELETE is not blocked by the 409 guard
                reportRepo.deleteAll(reportRepo.findByWorkflowEntitySetting_Id(s.getId()));
                // Null any manually-set WorkflowType FK refs so they don't block type deletion
                if (s.getTrackingServiceProviderActionId() != null
                        || s.getTrackingServiceProviderActionId2() != null) {
                    s.setTrackingServiceProviderActionId(null);
                    s.setTrackingServiceProviderActionId2(null);
                    entitySettingRepo.saveAndFlush(s);
                }
            });
            // Ignore 400 if the application does not exist yet
            mockMvc.perform(delete("/api/workflow").param("applicationName", appName));
        }
    }

    // ──────────────────────────────────────────────
    // Shared helpers
    // ──────────────────────────────────────────────

    private WorkFlow loadTestWorkflow() throws IOException {
        String json = new String(
                new ClassPathResource("workflow-integration-test-data.json").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        return objectMapper.readValue(json, WorkFlow.class);
    }

    /** Build a minimal workflow with {@code pluginCount} CONSUMER plugins, each with exactly 1 rule. */
    private WorkFlow buildSimpleWorkflow(int pluginCount) {
        List<Plugin> plugins = new java.util.ArrayList<>();
        for (int i = 1; i <= pluginCount; i++) {
            WorkflowRule rule = WorkflowRule.builder()
                    .key("$.data[?(@.stepId == " + i + ")]")
                    .remark("Rule for step " + i)
                    .build();
            WorkflowType action = WorkflowType.builder()
                    .type("CONSUMER")
                    .provider("TestService")
                    .httpRequestMethod("GET")
                    .httpRequestUrlWithQueryParameter("https://test.example.com/api/" + i)
                    .internalHttpRequestUrlWithQueryParameter("https://test.example.com/internal/" + i)
                    .httpRequestHeaders("{\"Content-Type\":\"application/json\"}")
                    .httpRequestBody("")
                    .trackingNumberSchemaInHttpResponse("{}")
                    .build();
            plugins.add(Plugin.builder()
                    .id(i)
                    .description("Step " + i + ": simple step")
                    .ruleList(List.of(rule))
                    .action(action)
                    .build());
        }
        return WorkFlow.builder().pluginList(plugins).uiMapList(List.of()).build();
    }

    private void postWorkflow(String appName, WorkFlow wf) throws Exception {
        mockMvc.perform(post("/api/workflow")
                        .param("applicationName", appName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wf)))
                .andExpect(status().isOk());
    }

    /** Returns the distinct linkingIds for all steps of the given application. */
    private List<String> getLinkingIdsForApp(String appName) {
        List<WorkflowEntitySetting> settings =
                entitySettingRepo.getWorkflowEntitySettingByApplicationName(appName);
        assertEquals(1, settings.size(),
                "Expected exactly 1 entity setting for " + appName);
        return linkingMappingRepo
                .findAllByWorkflowEntitySettingId(settings.get(0).getId())
                .stream()
                .map(WorkflowEntityAndLinkingIdMapping::getLinkingId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════
    // 1. POST → DB Verification
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("1. POST creates correct DB records")
    class PostCreatesDbRecords {

        @Test
        @DisplayName("Creates exactly 1 WorkflowEntitySetting with correct applicationName and enabled=true")
        void createsEntitySettingWithCorrectFields() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            List<WorkflowEntitySetting> found =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP);
            assertEquals(1, found.size(), "Exactly one entity setting must be created");

            WorkflowEntitySetting s = found.get(0);
            assertEquals(APP, s.getApplicationName(), "applicationName must match");
            assertTrue(s.isEnabled(), "enabled must default to true");
            assertNotNull(s.getId(), "auto-generated ID must be set");
            assertNotNull(s.getWorkflow(),
                    "workflow field must be populated (Base64-encoded UI snapshot)");
        }

        @Test
        @DisplayName("Creates correct number of WorkflowEntityAndLinkingIdMapping rows (one per plugin)")
        void createsCorrectNumberOfLinkingIdMappings() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId());

            assertEquals(FIXTURE_PLUGIN_COUNT, mappings.size(),
                    "Must have exactly one mapping row per plugin");
        }

        @Test
        @DisplayName("Mapping rows have correct logicOrder (1–10) and remark matching plugin description")
        void mappingRowsHaveCorrectOrderAndRemark() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId());
            mappings.sort(Comparator.comparing(WorkflowEntityAndLinkingIdMapping::getLogicOrder));

            List<Plugin> expectedPlugins = wf.getPluginList();
            for (int i = 0; i < FIXTURE_PLUGIN_COUNT; i++) {
                assertEquals(i + 1, mappings.get(i).getLogicOrder(),
                        "logicOrder at index " + i + " must be " + (i + 1));
                assertEquals(expectedPlugins.get(i).getDescription(), mappings.get(i).getRemark(),
                        "remark at index " + i + " must match plugin description");
            }
        }

        @Test
        @DisplayName("Creates correct number of WorkflowRule rows (11 total for 10-plugin fixture)")
        void createsCorrectNumberOfRules() throws Exception {
            long before = ruleRepo.count();
            postWorkflow(APP, loadTestWorkflow());
            assertEquals(FIXTURE_RULE_COUNT, ruleRepo.count() - before,
                    "Must create exactly " + FIXTURE_RULE_COUNT + " new rule rows");
        }

        @Test
        @DisplayName("Creates correct number of WorkflowType rows (one per plugin)")
        void createsCorrectNumberOfTypes() throws Exception {
            long before = typeRepo.count();
            postWorkflow(APP, loadTestWorkflow());
            assertEquals(FIXTURE_TYPE_COUNT, typeRepo.count() - before,
                    "Must create exactly " + FIXTURE_TYPE_COUNT + " new type rows");
        }

        @Test
        @DisplayName("Creates correct number of WorkflowRuleAndType mapping rows (same count as rules)")
        void createsCorrectNumberOfRuleAndTypeMappings() throws Exception {
            long before = ruleAndTypeRepo.count();
            postWorkflow(APP, loadTestWorkflow());
            assertEquals(FIXTURE_RULE_AND_TYPE_COUNT, ruleAndTypeRepo.count() - before,
                    "Must create exactly " + FIXTURE_RULE_AND_TYPE_COUNT + " new rule-type rows");
        }

        @Test
        @DisplayName("Rule rows in DB contain exactly the keys and remarks from the request payload")
        void ruleRowsMatchPayload() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            List<String> expectedKeys = wf.getPluginList().stream()
                    .filter(p -> p.getRuleList() != null)
                    .flatMap(p -> p.getRuleList().stream())
                    .map(WorkflowRule::getKey)
                    .sorted()
                    .toList();

            List<String> dbKeys = ruleAndTypeRepo
                    .findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .map(rt -> rt.getWorkflowRule().getKey())
                    .sorted()
                    .toList();

            assertEquals(expectedKeys.size(), dbKeys.size(), "Rule count must match");
            assertEquals(expectedKeys, dbKeys,
                    "Every rule key in DB must match the corresponding fixture key");
        }

        @Test
        @DisplayName("Type rows in DB contain CONSUMER, IFELSE, and MESSAGE types from the fixture")
        void typeRowsContainCorrectTypeValues() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            List<String> dbTypes = ruleAndTypeRepo
                    .findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .map(rt -> rt.getWorkflowType().getType())
                    .distinct()
                    .sorted()
                    .toList();

            assertTrue(dbTypes.contains("CONSUMER"), "DB must contain CONSUMER type");
            assertTrue(dbTypes.contains("IFELSE"),   "DB must contain IFELSE type");
            assertTrue(dbTypes.contains("MESSAGE"),  "DB must contain MESSAGE type");
        }

        @Test
        @DisplayName("linkingId format is '<entitySettingId>_<typeId>_<pluginId>' for every mapping row")
        void linkingIdFormatIsCorrect() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId());

            for (WorkflowEntityAndLinkingIdMapping m : mappings) {
                assertNotNull(m.getLinkingId(), "linkingId must not be null");
                String[] parts = m.getLinkingId().split("_");
                assertEquals(3, parts.length,
                        "linkingId must have exactly 3 underscore-separated parts");
                assertEquals(String.valueOf(s.getId()), parts[0],
                        "First part of linkingId must be the entity setting ID");
                assertEquals(String.valueOf(m.getLogicOrder()), parts[2],
                        "Third part of linkingId must be the plugin step ID");
            }
        }
    }

    // ══════════════════════════════════════════════
    // 2. GET returns correct data (round-trip)
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("2. GET returns correct data")
    class GetReturnsCorrectData {

        @Test
        @DisplayName("GET for non-existent application returns 400")
        void getNonExistentAppReturns400() throws Exception {
            mockMvc.perform(get("/api/workflow")
                            .param("applicationName", "NO_SUCH_APP_IN_DB"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST then GET returns the same plugin count as the request payload")
        void postThenGetReturnsCorrectPluginCount() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            assertEquals(FIXTURE_PLUGIN_COUNT, got.getPluginList().size(),
                    "Plugin count must match fixture");
        }

        @Test
        @DisplayName("POST then GET returns correct plugin IDs and descriptions for every step")
        void postThenGetReturnsCorrectPluginIdsAndDescriptions() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            for (int i = 0; i < FIXTURE_PLUGIN_COUNT; i++) {
                Plugin expected = wf.getPluginList().get(i);
                Plugin actual   = got.getPluginList().get(i);
                assertEquals(expected.getId(), actual.getId(),
                        "Plugin id at index " + i);
                assertEquals(expected.getDescription(), actual.getDescription(),
                        "Plugin description at index " + i);
            }
        }

        @Test
        @DisplayName("POST then GET returns correct action type and provider for every step")
        void postThenGetReturnsCorrectActionTypeAndProvider() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            for (int i = 0; i < FIXTURE_PLUGIN_COUNT; i++) {
                Plugin expected = wf.getPluginList().get(i);
                Plugin actual   = got.getPluginList().get(i);
                assertEquals(expected.getAction().getType(), actual.getAction().getType(),
                        "Action type at plugin index " + i);
                assertEquals(expected.getAction().getProvider(), actual.getAction().getProvider(),
                        "Action provider at plugin index " + i);
            }
        }

        @Test
        @DisplayName("POST then GET returns correct rule keys and remarks for every step (including multi-rule plugin 10)")
        void postThenGetReturnsCorrectRuleKeysAndRemarks() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            for (int i = 0; i < FIXTURE_PLUGIN_COUNT; i++) {
                Plugin expected = wf.getPluginList().get(i);
                Plugin actual   = got.getPluginList().get(i);
                assertEquals(expected.getRuleList().size(), actual.getRuleList().size(),
                        "Rule count at plugin index " + i);
                for (int j = 0; j < expected.getRuleList().size(); j++) {
                    assertEquals(expected.getRuleList().get(j).getKey(),
                            actual.getRuleList().get(j).getKey(),
                            "Rule key at plugin " + i + " rule " + j);
                    assertEquals(expected.getRuleList().get(j).getRemark(),
                            actual.getRuleList().get(j).getRemark(),
                            "Rule remark at plugin " + i + " rule " + j);
                }
            }
        }

        @Test
        @DisplayName("POST then GET returns correct HTTP action fields (URL, method, headers, body)")
        void postThenGetReturnsCorrectHttpActionFields() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            // Plugin 1 is CONSUMER type — verify all HTTP fields are decoded and match
            Plugin expP1 = wf.getPluginList().get(0);
            Plugin actP1 = got.getPluginList().get(0);
            assertEquals(expP1.getAction().getHttpRequestMethod(),
                    actP1.getAction().getHttpRequestMethod(),
                    "HTTP method (plugin 1) must round-trip correctly");
            assertEquals(expP1.getAction().getHttpRequestUrlWithQueryParameter(),
                    actP1.getAction().getHttpRequestUrlWithQueryParameter(),
                    "HTTP URL (plugin 1) must round-trip correctly");
            assertEquals(expP1.getAction().getHttpRequestHeaders(),
                    actP1.getAction().getHttpRequestHeaders(),
                    "HTTP headers (plugin 1) must round-trip correctly");
            assertEquals(expP1.getAction().getHttpRequestBody(),
                    actP1.getAction().getHttpRequestBody(),
                    "HTTP body (plugin 1) must round-trip correctly");
            assertEquals(expP1.getAction().getTrackingNumberSchemaInHttpResponse(),
                    actP1.getAction().getTrackingNumberSchemaInHttpResponse(),
                    "trackingNumberSchemaInHttpResponse (plugin 1) must round-trip correctly");

            // Plugin 2 is IFELSE — verify elseLogic is decoded
            Plugin expP2 = wf.getPluginList().get(1);
            Plugin actP2 = got.getPluginList().get(1);
            assertEquals(expP2.getAction().getElseLogic(), actP2.getAction().getElseLogic(),
                    "elseLogic (plugin 2 IFELSE) must round-trip correctly");
        }

        @Test
        @DisplayName("POST then GET returns uiMapList with same edge count as the original request")
        void postThenGetReturnsCorrectUiMapList() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            assertNotNull(got.getUiMapList(), "uiMapList must not be null");
            assertEquals(wf.getUiMapList().size(), got.getUiMapList().size(),
                    "uiMapList edge count must match fixture (9 edges for 10 sequential nodes)");
        }
    }

    // ══════════════════════════════════════════════
    // 3. DELETE removes ALL DB records (no orphans)
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("3. DELETE removes all DB records (no orphans)")
    class DeleteRemovesAllDbRecords {

        @Test
        @DisplayName("DELETE removes the WorkflowEntitySetting row from DB")
        void deleteRemovesEntitySetting() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            assertFalse(entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).isEmpty(),
                    "Entity setting must exist before delete");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            assertTrue(entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).isEmpty(),
                    "Entity setting must not exist after delete");
        }

        @Test
        @DisplayName("DELETE removes all WorkflowEntityAndLinkingIdMapping rows for the app")
        void deleteRemovesAllLinkingIdMappings() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            long settingId = s.getId();

            assertFalse(linkingMappingRepo.findAllByWorkflowEntitySettingId(settingId).isEmpty(),
                    "Linking-id mappings must exist before delete");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            assertTrue(linkingMappingRepo.findAllByWorkflowEntitySettingId(settingId).isEmpty(),
                    "All linking-id mapping rows must be removed after delete");
        }

        @Test
        @DisplayName("DELETE removes all WorkflowRuleAndType rows associated with the app")
        void deleteRemovesAllRuleAndTypeMappings() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            List<String> linkingIds = getLinkingIdsForApp(APP);
            assertFalse(ruleAndTypeRepo.findAllByLinkingIdIn(linkingIds).isEmpty(),
                    "RuleAndType rows must exist before delete");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            assertTrue(ruleAndTypeRepo.findAllByLinkingIdIn(linkingIds).isEmpty(),
                    "All rule-type mapping rows must be removed after delete");
        }

        @Test
        @DisplayName("DELETE removes all WorkflowRule rows (11 rows) belonging to the app")
        void deleteRemovesAllRuleRows() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            List<Long> ruleIds = ruleAndTypeRepo
                    .findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .map(rt -> rt.getWorkflowRule().getId())
                    .distinct()
                    .toList();
            assertEquals(FIXTURE_RULE_COUNT, ruleIds.size(),
                    "Expected " + FIXTURE_RULE_COUNT + " rule rows before delete");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            long surviving = ruleIds.stream()
                    .filter(id -> ruleRepo.findById(id).isPresent())
                    .count();
            assertEquals(0, surviving,
                    "All " + FIXTURE_RULE_COUNT + " rule rows must be gone after delete");
        }

        @Test
        @DisplayName("DELETE removes all WorkflowType rows (10 rows) belonging to the app")
        void deleteRemovesAllTypeRows() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            List<Long> typeIds = ruleAndTypeRepo
                    .findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .map(rt -> rt.getWorkflowType().getId())
                    .distinct()
                    .toList();
            assertEquals(FIXTURE_TYPE_COUNT, typeIds.size(),
                    "Expected " + FIXTURE_TYPE_COUNT + " type rows before delete");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            long surviving = typeIds.stream()
                    .filter(id -> typeRepo.findById(id).isPresent())
                    .count();
            assertEquals(0, surviving,
                    "All " + FIXTURE_TYPE_COUNT + " type rows must be gone after delete");
        }

        @Test
        @DisplayName("GET after DELETE returns 400 (application no longer found)")
        void getAfterDeleteReturns400() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE for non-existent application returns 400")
        void deleteNonExistentAppReturns400() throws Exception {
            mockMvc.perform(delete("/api/workflow")
                            .param("applicationName", "NO_SUCH_APP_IN_DB"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE returns 409 when a WorkflowReport exists for the application; entity setting and all data remain intact")
        void deleteBlockedWhenReportExistsAndDataIsUnchanged() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);

            WorkflowReport report = WorkflowReport.builder()
                    .workflowEntitySetting(s)
                    .reportGroup(1L)
                    .enabled(true)
                    .cronExpression("0 0 8 * * ?")
                    .reportTimeRangeByHours(24)
                    .timezone("Asia/Hong_Kong")
                    .build();
            reportRepo.saveAndFlush(report);

            try {
                // Must be blocked
                mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                        .andExpect(status().isConflict());

                // Entity setting and all its data must still be intact
                assertEquals(1,
                        entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).size(),
                        "Entity setting must still exist after blocked delete");
                assertEquals(FIXTURE_PLUGIN_COUNT,
                        linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId()).size(),
                        "All linking-id mappings must be intact after blocked delete");
            } finally {
                reportRepo.delete(report);
            }
        }
    }

    // ══════════════════════════════════════════════
    // 4. POST (update/upsert) replaces old records
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("4. Second POST (upsert) replaces existing DB records without duplication")
    class PostUpdateReplacesRecords {

        @Test
        @DisplayName("Second POST does not create a duplicate WorkflowEntitySetting row")
        void secondPostDoesNotDuplicateEntitySetting() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            postWorkflow(APP, buildSimpleWorkflow(3));

            assertEquals(1,
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).size(),
                    "Exactly one entity setting must exist after two POST calls");
        }

        @Test
        @DisplayName("Second POST deletes all old WorkflowRule rows and inserts exactly the new set")
        void secondPostReplacesRuleRows() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            List<Long> oldRuleIds = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(FIXTURE_RULE_COUNT, oldRuleIds.size());

            postWorkflow(APP, buildSimpleWorkflow(3)); // 3 plugins × 1 rule = 3 new rules

            // All old rule IDs must be gone
            long surviving = oldRuleIds.stream()
                    .filter(id -> ruleRepo.findById(id).isPresent()).count();
            assertEquals(0, surviving, "Old rule rows must not survive after update");

            // Exactly 3 new rule rows must exist
            List<Long> newRuleIds = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(3, newRuleIds.size(),
                    "After update to 3-plugin workflow, exactly 3 rule rows must exist");
        }

        @Test
        @DisplayName("Second POST deletes all old WorkflowType rows and inserts exactly the new set")
        void secondPostReplacesTypeRows() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            List<Long> oldTypeIds = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowType().getId()).distinct().toList();
            assertEquals(FIXTURE_TYPE_COUNT, oldTypeIds.size());

            postWorkflow(APP, buildSimpleWorkflow(3));

            long surviving = oldTypeIds.stream()
                    .filter(id -> typeRepo.findById(id).isPresent()).count();
            assertEquals(0, surviving, "Old type rows must not survive after update");

            List<Long> newTypeIds = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowType().getId()).distinct().toList();
            assertEquals(3, newTypeIds.size(),
                    "After update to 3-plugin workflow, exactly 3 type rows must exist");
        }

        @Test
        @DisplayName("Second POST replaces old linking-id mapping rows with the new set (count changes from 10 to 3)")
        void secondPostReplacesLinkingIdMappings() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            assertEquals(FIXTURE_PLUGIN_COUNT,
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId()).size(),
                    "Before update: must have 10 mapping rows");

            postWorkflow(APP, buildSimpleWorkflow(3));

            assertEquals(3,
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId()).size(),
                    "After update: must have exactly 3 mapping rows");
        }

        @Test
        @DisplayName("GET after update returns the new workflow data, not the old data")
        void getAfterUpdateReturnsNewData() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            postWorkflow(APP, buildSimpleWorkflow(3));

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            assertEquals(3, got.getPluginList().size(),
                    "GET must reflect updated 3-plugin workflow");
            for (int i = 0; i < 3; i++) {
                assertEquals("CONSUMER", got.getPluginList().get(i).getAction().getType(),
                        "Action type at index " + i + " must be CONSUMER");
                assertEquals("Step " + (i + 1) + ": simple step",
                        got.getPluginList().get(i).getDescription(),
                        "Description at index " + i + " must be from the new workflow");
            }
        }
    }

    // ══════════════════════════════════════════════
    // 5. autoCopy creates independent DB records
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("5. autoCopy creates correct, independent DB records for the target application")
    class AutoCopyCreatesDbRecords {

        @Test
        @DisplayName("autoCopy creates a new WorkflowEntitySetting row for the target application")
        void autoCopyCreatesTargetEntitySetting() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            List<WorkflowEntitySetting> targetSettings =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP_COPY);
            assertEquals(1, targetSettings.size(),
                    "Target entity setting must be created");
            assertEquals(APP_COPY, targetSettings.get(0).getApplicationName(),
                    "Target applicationName must be APP_COPY");
        }

        @Test
        @DisplayName("autoCopy creates new, independent rule and type rows for the target (same counts as source)")
        void autoCopyCreatesIndependentRuleAndTypeRows() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            long rulesBefore = ruleRepo.count();
            long typesBefore = typeRepo.count();

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            assertEquals(FIXTURE_RULE_COUNT, ruleRepo.count() - rulesBefore,
                    "autoCopy must insert " + FIXTURE_RULE_COUNT + " new rule rows for the target");
            assertEquals(FIXTURE_TYPE_COUNT, typeRepo.count() - typesBefore,
                    "autoCopy must insert " + FIXTURE_TYPE_COUNT + " new type rows for the target");
        }

        @Test
        @DisplayName("autoCopy response and GET for target both return the same plugin count as the source")
        void autoCopyTargetHasSamePluginCount() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            MvcResult copyResult = mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andReturn();

            WorkFlow copied = objectMapper.readValue(
                    copyResult.getResponse().getContentAsString(), WorkFlow.class);
            assertEquals(FIXTURE_PLUGIN_COUNT, copied.getPluginList().size(),
                    "autoCopy response must return same plugin count as source");

            MvcResult getResult = mockMvc.perform(
                            get("/api/workflow").param("applicationName", APP_COPY))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);
            assertEquals(FIXTURE_PLUGIN_COUNT, got.getPluginList().size(),
                    "GET for target must return same plugin count as source");
        }

        @Test
        @DisplayName("GET for target after autoCopy returns same action types and rule counts as source")
        void autoCopyTargetMatchesSourceActionTypesAndRuleCounts() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            WorkFlow src = objectMapper.readValue(
                    mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                            .andExpect(status().isOk()).andReturn()
                            .getResponse().getContentAsString(), WorkFlow.class);
            WorkFlow tgt = objectMapper.readValue(
                    mockMvc.perform(get("/api/workflow").param("applicationName", APP_COPY))
                            .andExpect(status().isOk()).andReturn()
                            .getResponse().getContentAsString(), WorkFlow.class);

            src.getPluginList().sort(Comparator.comparing(Plugin::getId));
            tgt.getPluginList().sort(Comparator.comparing(Plugin::getId));

            for (int i = 0; i < FIXTURE_PLUGIN_COUNT; i++) {
                assertEquals(src.getPluginList().get(i).getAction().getType(),
                        tgt.getPluginList().get(i).getAction().getType(),
                        "Action type at plugin " + i + " must match source");
                assertEquals(src.getPluginList().get(i).getRuleList().size(),
                        tgt.getPluginList().get(i).getRuleList().size(),
                        "Rule count at plugin " + i + " must match source");
            }
        }

        @Test
        @DisplayName("Source entity setting and DB records are completely unaffected after autoCopy")
        void sourceUnaffectedAfterAutoCopy() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            WorkflowEntitySetting src =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            List<String> srcLinkingIds = getLinkingIdsForApp(APP);

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            assertEquals(1,
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).size(),
                    "Source entity setting must still exist after autoCopy");
            assertEquals(src.getId(),
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0).getId(),
                    "Source entity setting ID must not change after autoCopy");
            assertEquals(FIXTURE_PLUGIN_COUNT,
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(src.getId()).size(),
                    "Source must retain all " + FIXTURE_PLUGIN_COUNT + " mapping rows after autoCopy");
            assertEquals(FIXTURE_RULE_AND_TYPE_COUNT,
                    ruleAndTypeRepo.findAllByLinkingIdIn(srcLinkingIds).size(),
                    "Source must retain all rule-type rows after autoCopy");
        }

        @Test
        @DisplayName("DELETE of copied target does not remove or affect source DB records")
        void deleteTargetDoesNotAffectSource() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            List<String> srcLinkingIds = getLinkingIdsForApp(APP);
            WorkflowEntitySetting src =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_COPY))
                    .andExpect(status().isOk());

            // Source entity setting must still exist
            assertEquals(1,
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).size(),
                    "Source entity setting must survive target deletion");
            // Source rules and types must still exist
            assertEquals(FIXTURE_RULE_AND_TYPE_COUNT,
                    ruleAndTypeRepo.findAllByLinkingIdIn(srcLinkingIds).size(),
                    "Source rule-type rows must survive target deletion");
            // Source linking-id mappings must still exist
            assertEquals(FIXTURE_PLUGIN_COUNT,
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(src.getId()).size(),
                    "Source linking-id mapping rows must survive target deletion");
        }

        @Test
        @DisplayName("autoCopy with same source and target application name returns 400")
        void autoCopySameSourceAndTargetReturns400() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("autoCopy with non-existent source application returns 400")
        void autoCopyNonExistentSourceReturns400() throws Exception {
            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", "NO_SUCH_SOURCE_APP")
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ══════════════════════════════════════════════
    // 6. Edge cases and error boundaries
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("6. Edge cases and error boundaries")
    class EdgeCasesAndErrors {

        @Test
        @DisplayName("POST with no request body returns 400")
        void postWithMissingBodyReturns400() throws Exception {
            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with empty plugin list creates an entity setting but zero rule/type/mapping rows")
        void postEmptyPluginListCreatesEntitySettingOnly() throws Exception {
            WorkFlow emptyWf = WorkFlow.builder()
                    .pluginList(List.of())
                    .uiMapList(List.of())
                    .build();
            postWorkflow(APP, emptyWf);

            List<WorkflowEntitySetting> settings =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP);
            assertEquals(1, settings.size(),
                    "Entity setting must still be created for empty workflow");

            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(settings.get(0).getId());
            assertTrue(mappings.isEmpty(),
                    "No mapping rows must be created for empty plugin list");
        }

        @Test
        @DisplayName("Posting the same workflow twice is idempotent: still 1 entity setting, same plugin count on GET")
        void postTwiceWithSameDataIsIdempotent() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);
            postWorkflow(APP, wf);

            assertEquals(1,
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).size(),
                    "Exactly one entity setting must exist after two identical POSTs");

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            assertEquals(FIXTURE_PLUGIN_COUNT, got.getPluginList().size(),
                    "Plugin count must remain correct after idempotent double-POST");
        }

        @Test
        @DisplayName("POST then DELETE then re-POST creates fresh DB records (correct counts after re-creation)")
        void deleteAndRePostCreatesFreshRecords() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            List<Long> firstRuleIds = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            // Re-create
            postWorkflow(APP, loadTestWorkflow());

            // Old rule IDs must be gone
            long surviving = firstRuleIds.stream()
                    .filter(id -> ruleRepo.findById(id).isPresent()).count();
            assertEquals(0, surviving,
                    "Rules from first POST must not exist after delete + re-POST");

            // New rule count must be correct
            List<Long> newRuleIds = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(FIXTURE_RULE_COUNT, newRuleIds.size(),
                    "Re-POST must create fresh " + FIXTURE_RULE_COUNT + " rule rows");
        }
    }

    // ══════════════════════════════════════════════
    // Shared builder helpers for deeper tests
    // ══════════════════════════════════════════════

    /** Build a 1-plugin workflow whose single plugin has an empty ruleList. */
    private WorkFlow buildWorkflowWithEmptyRuleList() {
        Plugin plugin = Plugin.builder()
                .id(1)
                .description("Step 1: no rules")
                .ruleList(List.of())
                .action(WorkflowType.builder()
                        .type("CONSUMER")
                        .provider("TestProvider")
                        .httpRequestMethod("GET")
                        .httpRequestUrlWithQueryParameter("https://test.example.com/no-rules")
                        .httpRequestHeaders("{\"Content-Type\":\"application/json\"}")
                        .httpRequestBody("")
                        .trackingNumberSchemaInHttpResponse("{}")
                        .build())
                .build();
        return WorkFlow.builder().pluginList(List.of(plugin)).uiMapList(List.of()).build();
    }

    /** Build a workflow whose plugins are provided in a deliberately non-sequential order. */
    private WorkFlow buildOutOfOrderWorkflow() {
        List<Plugin> plugins = new ArrayList<>();
        for (int id : new int[]{3, 1, 2}) {
            plugins.add(Plugin.builder()
                    .id(id)
                    .description("Out-of-order step " + id)
                    .ruleList(List.of(WorkflowRule.builder()
                            .key("$.data[?(@.step == " + id + ")]")
                            .remark("Rule for step " + id)
                            .build()))
                    .action(WorkflowType.builder()
                            .type("CONSUMER")
                            .httpRequestMethod("GET")
                            .httpRequestUrlWithQueryParameter("https://test.example.com/" + id)
                            .httpRequestHeaders("{\"Accept\":\"application/json\"}")
                            .httpRequestBody("")
                            .trackingNumberSchemaInHttpResponse("{}")
                            .build())
                    .build());
        }
        return WorkFlow.builder().pluginList(plugins).uiMapList(List.of()).build();
    }

    // ══════════════════════════════════════════════
    // 7. DB field encoding verification
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("7. DB field encoding: URLs/headers/elseLogic stored as Base64; method/type stored as plain text")
    class DbFieldEncodingVerification {

        @Test
        @DisplayName("httpRequestUrlWithQueryParameter is stored as Base64 (plain) in DB, not plain text")
        void httpRequestUrlStoredAsBase64() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            // Plugin 1 is CONSUMER with a non-null URL
            String plainUrl = wf.getPluginList().get(0).getAction().getHttpRequestUrlWithQueryParameter();
            String expectedBase64 = Base64.getEncoder()
                    .encodeToString(plainUrl.getBytes(StandardCharsets.UTF_8));

            WorkflowType dbType = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .filter(rt -> "CONSUMER".equals(rt.getWorkflowType().getType()))
                    .map(WorkflowRuleAndType::getWorkflowType)
                    .findFirst().orElseThrow();

            assertEquals(expectedBase64, dbType.getHttpRequestUrlWithQueryParameter(),
                    "httpRequestUrlWithQueryParameter must be stored as plain Base64 in DB");
        }

        @Test
        @DisplayName("httpRequestMethod is stored as plain text (NOT Base64) in DB")
        void httpRequestMethodStoredAsPlainText() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            WorkflowType dbType = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .filter(rt -> "CONSUMER".equals(rt.getWorkflowType().getType()))
                    .map(WorkflowRuleAndType::getWorkflowType)
                    .findFirst().orElseThrow();

            assertEquals("GET", dbType.getHttpRequestMethod(),
                    "httpRequestMethod must be stored as plain text 'GET', not Base64");
            assertFalse(dbType.getHttpRequestMethod().contains("="),
                    "httpRequestMethod must not look like Base64 (no '=' padding)");
        }

        @Test
        @DisplayName("httpRequestHeaders is stored as Base64 JSON in DB")
        void httpRequestHeadersStoredAsBase64Json() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            String plainHeaders = wf.getPluginList().get(0).getAction().getHttpRequestHeaders();
            String normalised    = objectMapper.readTree(plainHeaders).toString();
            String expectedBase64 = Base64.getEncoder()
                    .encodeToString(normalised.getBytes(StandardCharsets.UTF_8));

            WorkflowType dbType = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .filter(rt -> "CONSUMER".equals(rt.getWorkflowType().getType()))
                    .map(WorkflowRuleAndType::getWorkflowType)
                    .findFirst().orElseThrow();

            assertEquals(expectedBase64, dbType.getHttpRequestHeaders(),
                    "httpRequestHeaders must be stored as Base64-encoded JSON in DB");
        }

        @Test
        @DisplayName("elseLogic for IFELSE plugin is stored as Base64 JSON in DB")
        void elseLogicStoredAsBase64Json() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            // Plugin 2 (index 1) is IFELSE with non-null elseLogic
            String plainElseLogic = wf.getPluginList().get(1).getAction().getElseLogic();
            assertNotNull(plainElseLogic, "Fixture plugin 2 must have non-null elseLogic");
            String normalised     = objectMapper.readTree(plainElseLogic).toString();
            String expectedBase64 = Base64.getEncoder()
                    .encodeToString(normalised.getBytes(StandardCharsets.UTF_8));

            // Find the IFELSE type for plugin 2 in DB
            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            WorkflowEntityAndLinkingIdMapping plugin2Mapping =
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId())
                            .stream().filter(m -> m.getLogicOrder().equals(2))
                            .findFirst().orElseThrow();
            List<WorkflowRuleAndType> plugin2RuleAndTypes =
                    ruleAndTypeRepo.findAllByLinkingIdIn(List.of(plugin2Mapping.getLinkingId()));
            WorkflowType dbType = plugin2RuleAndTypes.get(0).getWorkflowType();

            assertEquals(expectedBase64, dbType.getElseLogic(),
                    "elseLogic must be stored as Base64-encoded JSON in DB");
        }

        @Test
        @DisplayName("WorkflowType.type and provider are stored as plain text (NOT Base64) in DB")
        void typeAndProviderStoredAsPlainText() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            List<WorkflowRuleAndType> allRt =
                    ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP));

            for (WorkflowRuleAndType rt : allRt) {
                String type = rt.getWorkflowType().getType();
                assertNotNull(type, "type must not be null");
                assertTrue(List.of("CONSUMER", "IFELSE", "MESSAGE").contains(type),
                        "type must be one of the fixture values, found: " + type);
                assertFalse(type.contains("="),
                        "type must not be Base64-encoded, found: " + type);
            }
        }

        @Test
        @DisplayName("WorkflowEntitySetting.workflow field decodes to valid JSON with correct pluginList and uiMapList structure")
        void entitySettingWorkflowFieldDecodesToValidJson() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            assertNotNull(s.getWorkflow(), "workflow field must be populated");

            // Decode from Base64 and parse as JSON
            byte[] decoded = Base64.getDecoder().decode(s.getWorkflow());
            JsonNode workflowJson = objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));

            assertNotNull(workflowJson.get("pluginList"), "decoded workflow must have pluginList");
            assertNotNull(workflowJson.get("uiMapList"),  "decoded workflow must have uiMapList");
            assertEquals(FIXTURE_PLUGIN_COUNT, workflowJson.get("pluginList").size(),
                    "decoded workflow pluginList must contain " + FIXTURE_PLUGIN_COUNT + " plugins");
            assertEquals(9, workflowJson.get("uiMapList").size(),
                    "decoded workflow uiMapList must contain 9 edges");
        }

        @Test
        @DisplayName("internalHttpRequestUrlWithQueryParameter is stored as Base64 (plain) in DB and decoded correctly in GET")
        void internalHttpRequestUrlRoundTrips() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            // Plugin 1 has internalHttpRequestUrlWithQueryParameter
            String plainInternalUrl = wf.getPluginList().get(0).getAction()
                    .getInternalHttpRequestUrlWithQueryParameter();
            assertNotNull(plainInternalUrl, "Fixture plugin 1 must have internalHttpRequestUrlWithQueryParameter");

            // Verify stored as Base64 in DB
            WorkflowType dbType = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .filter(rt -> "CONSUMER".equals(rt.getWorkflowType().getType()))
                    .map(WorkflowRuleAndType::getWorkflowType)
                    .findFirst().orElseThrow();
            String expectedBase64 = Base64.getEncoder()
                    .encodeToString(plainInternalUrl.getBytes(StandardCharsets.UTF_8));
            assertEquals(expectedBase64, dbType.getInternalHttpRequestUrlWithQueryParameter(),
                    "internalHttpRequestUrlWithQueryParameter must be Base64-encoded in DB");

            // Verify decoded correctly in GET response
            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            Plugin p1 = got.getPluginList().stream()
                    .filter(p -> p.getId().equals(1)).findFirst().orElseThrow();
            assertEquals(plainInternalUrl, p1.getAction().getInternalHttpRequestUrlWithQueryParameter(),
                    "internalHttpRequestUrlWithQueryParameter must decode back to original value in GET");
        }
    }

    // ══════════════════════════════════════════════
    // 8. Audit timestamps
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("8. Audit timestamps are set on all entities after POST")
    class AuditTimestamps {

        @Test
        @DisplayName("Entity setting has non-null createdDateTime and lastModifiedDateTime after POST")
        void entitySettingHasAuditTimestamps() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            assertNotNull(s.getCreatedDateTime(),      "createdDateTime must be set");
            assertNotNull(s.getLastModifiedDateTime(), "lastModifiedDateTime must be set");
            assertFalse(s.getLastModifiedDateTime().before(s.getCreatedDateTime()),
                    "lastModifiedDateTime must be >= createdDateTime");
        }

        @Test
        @DisplayName("All WorkflowRule rows have non-null audit timestamps after POST")
        void ruleRowsHaveAuditTimestamps() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            List<WorkflowRuleAndType> allRt =
                    ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP));
            for (WorkflowRuleAndType rt : allRt) {
                WorkflowRule rule = rt.getWorkflowRule();
                assertNotNull(rule.getCreatedDateTime(),
                        "rule " + rule.getId() + " createdDateTime must be set");
                assertNotNull(rule.getLastModifiedDateTime(),
                        "rule " + rule.getId() + " lastModifiedDateTime must be set");
            }
        }

        @Test
        @DisplayName("All WorkflowType rows have non-null audit timestamps after POST")
        void typeRowsHaveAuditTimestamps() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            List<WorkflowRuleAndType> allRt =
                    ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP));
            for (WorkflowRuleAndType rt : allRt) {
                WorkflowType type = rt.getWorkflowType();
                assertNotNull(type.getCreatedDateTime(),
                        "type " + type.getId() + " createdDateTime must be set");
                assertNotNull(type.getLastModifiedDateTime(),
                        "type " + type.getId() + " lastModifiedDateTime must be set");
            }
        }

        @Test
        @DisplayName("Entity setting lastModifiedDateTime is updated on second POST; createdDateTime stays unchanged")
        void secondPostUpdatesLastModifiedButNotCreated() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            WorkflowEntitySetting after1st =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            Date createdAt = after1st.getCreatedDateTime();

            postWorkflow(APP, buildSimpleWorkflow(3));
            WorkflowEntitySetting after2nd =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);

            assertEquals(createdAt, after2nd.getCreatedDateTime(),
                    "createdDateTime must not change after update (updatable=false)");
            assertNotNull(after2nd.getLastModifiedDateTime(),
                    "lastModifiedDateTime must still be non-null after update");
            assertFalse(after2nd.getLastModifiedDateTime().before(after2nd.getCreatedDateTime()),
                    "lastModifiedDateTime must be >= createdDateTime after update");
        }
    }

    // ══════════════════════════════════════════════
    // 9. DB structure deep verification
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("9. DB structure deep verification")
    class DbStructureDeepVerification {

        @Test
        @DisplayName("Plugin with empty ruleList creates exactly one WorkflowRule row with key=\"\" in DB")
        void emptyRuleListCreatesEmptyKeyRule() throws Exception {
            postWorkflow(APP, buildWorkflowWithEmptyRuleList());

            List<String> linkingIds = getLinkingIdsForApp(APP);
            List<WorkflowRuleAndType> ruleAndTypes = ruleAndTypeRepo.findAllByLinkingIdIn(linkingIds);

            assertEquals(1, ruleAndTypes.size(),
                    "Exactly one rule-type row must be created for empty ruleList");
            assertEquals("", ruleAndTypes.get(0).getWorkflowRule().getKey(),
                    "Empty ruleList must produce a rule with key=\"\"");
        }

        @Test
        @DisplayName("Plugins POSTed in non-sequential order: GET returns them sorted by step ID")
        void pluginsPostedOutOfOrderReturnedSorted() throws Exception {
            postWorkflow(APP, buildOutOfOrderWorkflow()); // sends [3, 1, 2]

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            List<Integer> returnedIds = got.getPluginList().stream()
                    .map(Plugin::getId).collect(Collectors.toList());
            assertEquals(List.of(1, 2, 3), returnedIds,
                    "GET must return plugins sorted by ID (1, 2, 3) regardless of POST order");
        }

        @Test
        @DisplayName("Multi-rule plugin 10: both WorkflowRuleAndType rows share the same linkingId and same WorkflowType")
        void multiRulePluginSharesLinkingIdAndType() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            WorkflowEntityAndLinkingIdMapping plugin10Mapping =
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(s.getId())
                            .stream().filter(m -> m.getLogicOrder().equals(10))
                            .findFirst().orElseThrow();

            List<WorkflowRuleAndType> plugin10Rows =
                    ruleAndTypeRepo.findAllByLinkingIdIn(List.of(plugin10Mapping.getLinkingId()));

            assertEquals(2, plugin10Rows.size(),
                    "Plugin 10 with 2 rules must have exactly 2 WorkflowRuleAndType rows");

            // Both rows share the same linkingId
            assertEquals(plugin10Mapping.getLinkingId(), plugin10Rows.get(0).getLinkingId(),
                    "Row 0 linkingId must match mapping linkingId");
            assertEquals(plugin10Mapping.getLinkingId(), plugin10Rows.get(1).getLinkingId(),
                    "Row 1 linkingId must match mapping linkingId");

            // Both rows reference the SAME WorkflowType (1 type per plugin, shared across rules)
            Long typeId0 = plugin10Rows.get(0).getWorkflowType().getId();
            Long typeId1 = plugin10Rows.get(1).getWorkflowType().getId();
            assertEquals(typeId0, typeId1,
                    "Multi-rule plugin must reference the same WorkflowType for both rules");

            // But each row references a DIFFERENT WorkflowRule
            assertNotEquals(
                    plugin10Rows.get(0).getWorkflowRule().getId(),
                    plugin10Rows.get(1).getWorkflowRule().getId(),
                    "Each rule in a multi-rule plugin must be a separate WorkflowRule row");
        }

        @Test
        @DisplayName("GET response linkingIdOfRuleListAndAction is server-generated (entity setting ID prefix), not the request payload value")
        void getLinkingIdIsServerGenerated() throws Exception {
            postWorkflow(APP, loadTestWorkflow()); // fixture sends "4_1_1", "4_2_2" etc.

            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            String entityIdPrefix = s.getId() + "_";

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            for (Plugin p : got.getPluginList()) {
                String lid = p.getLinkingIdOfRuleListAndAction();
                assertNotNull(lid, "linkingIdOfRuleListAndAction must not be null");
                assertTrue(lid.startsWith(entityIdPrefix),
                        "linkingIdOfRuleListAndAction '" + lid + "' must start with entity setting ID prefix '"
                                + entityIdPrefix + "'");
                // Must NOT be the fixture value (e.g. "4_1_1")
                assertFalse(lid.startsWith("4_"),
                        "linkingIdOfRuleListAndAction must not be the request payload value '4_x_x'");
            }
        }

        @Test
        @DisplayName("uiMap node ID, type, and position coordinates are exactly preserved in GET response")
        void uiMapNodeContentPreservedInGet() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            Plugin p1 = got.getPluginList().stream()
                    .filter(p -> p.getId().equals(1)).findFirst().orElseThrow();

            @SuppressWarnings("unchecked")
            Map<String, Object> uiMap = (Map<String, Object>) p1.getUiMap();
            assertNotNull(uiMap, "Plugin 1 uiMap must not be null");
            assertEquals("CONSUMER_1", uiMap.get("id"),   "uiMap id must be 'CONSUMER_1'");
            assertEquals("CONSUMER",   uiMap.get("type"), "uiMap type must be 'CONSUMER'");

            @SuppressWarnings("unchecked")
            Map<String, Object> position = (Map<String, Object>) uiMap.get("position");
            assertNotNull(position, "position must be present");
            assertEquals(100, ((Number) position.get("x")).intValue(), "uiMap x must be 100");
            assertEquals(100, ((Number) position.get("y")).intValue(), "uiMap y must be 100");

            // Verify plugin 10 has the correct position too (y=1000)
            Plugin p10 = got.getPluginList().stream()
                    .filter(p -> p.getId().equals(10)).findFirst().orElseThrow();
            @SuppressWarnings("unchecked")
            Map<String, Object> pos10 = (Map<String, Object>)
                    ((Map<String, Object>) p10.getUiMap()).get("position");
            assertEquals(1000, ((Number) pos10.get("y")).intValue(), "Plugin 10 uiMap y must be 1000");
        }

        @Test
        @DisplayName("WorkflowRule.remark is stored correctly in DB for every rule")
        void ruleRemarkStoredCorrectlyInDb() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            List<String> expectedRemarks = wf.getPluginList().stream()
                    .filter(p -> p.getRuleList() != null)
                    .flatMap(p -> p.getRuleList().stream())
                    .map(WorkflowRule::getRemark)
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            List<String> dbRemarks = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream()
                    .map(rt -> rt.getWorkflowRule().getRemark())
                    .filter(Objects::nonNull)
                    .sorted()
                    .toList();

            assertEquals(expectedRemarks.size(), dbRemarks.size(), "Remark count must match");
            assertEquals(expectedRemarks, dbRemarks, "All rule remarks must match fixture");
        }
    }

    // ══════════════════════════════════════════════
    // 10. Advanced GET behaviour
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("10. Advanced GET behaviour")
    class AdvancedGetBehaviour {

        @Test
        @DisplayName("POST with empty uiMapList: GET auto-generates a default edge list (pluginCount-1 edges)")
        void emptyStoredUiMapListTriggersDefaultEdgeGeneration() throws Exception {
            WorkFlow wf = buildSimpleWorkflow(4); // uiMapList is [] in buildSimpleWorkflow
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            assertNotNull(got.getUiMapList(), "uiMapList must not be null");
            assertEquals(3, got.getUiMapList().size(),
                    "4 plugins with empty uiMapList must produce 3 default edges (pluginCount - 1)");
        }

        @Test
        @DisplayName("Default generated edges have correct source/target node IDs linking consecutive plugins")
        void defaultGeneratedEdgesHaveCorrectSourceAndTarget() throws Exception {
            postWorkflow(APP, buildSimpleWorkflow(3)); // plugins 1, 2, 3 — all CONSUMER type

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            List<Object> edges = got.getUiMapList();
            assertEquals(2, edges.size(), "3 plugins must produce 2 edges");

            @SuppressWarnings("unchecked")
            Map<String, Object> edge0 = (Map<String, Object>) edges.get(0);
            assertEquals("CONSUMER_1", edge0.get("source"), "First edge source must be CONSUMER_1");
            assertEquals("CONSUMER_2", edge0.get("target"), "First edge target must be CONSUMER_2");

            @SuppressWarnings("unchecked")
            Map<String, Object> edge1 = (Map<String, Object>) edges.get(1);
            assertEquals("CONSUMER_2", edge1.get("source"), "Second edge source must be CONSUMER_2");
            assertEquals("CONSUMER_3", edge1.get("target"), "Second edge target must be CONSUMER_3");
        }

        @Test
        @DisplayName("IFELSE plugins with null HTTP fields (url, method, body, headers) return null in GET response")
        void nullHttpFieldsOnIfElseReturnNullInGet() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);

            // All IFELSE plugins in the fixture have null HTTP fields
            List<Plugin> ifElsePlugins = got.getPluginList().stream()
                    .filter(p -> "IFELSE".equals(p.getAction().getType()))
                    .collect(Collectors.toList());
            assertFalse(ifElsePlugins.isEmpty(), "Fixture must contain IFELSE plugins");

            for (Plugin p : ifElsePlugins) {
                assertNull(p.getAction().getHttpRequestMethod(),
                        "IFELSE plugin " + p.getId() + " httpRequestMethod must be null");
                assertNull(p.getAction().getHttpRequestUrlWithQueryParameter(),
                        "IFELSE plugin " + p.getId() + " httpRequestUrlWithQueryParameter must be null");
                assertNull(p.getAction().getHttpRequestHeaders(),
                        "IFELSE plugin " + p.getId() + " httpRequestHeaders must be null");
                assertNull(p.getAction().getHttpRequestBody(),
                        "IFELSE plugin " + p.getId() + " httpRequestBody must be null");
            }
        }

        @Test
        @DisplayName("IFELSE plugins with non-null elseLogic: every IFELSE plugin with elseLogic returns it correctly decoded")
        void ifElsePluginsWithElseLogicDecodedCorrectly() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            // Find plugins that have non-null elseLogic in the fixture
            for (int i = 0; i < wf.getPluginList().size(); i++) {
                String expectedElseLogic = wf.getPluginList().get(i).getAction().getElseLogic();
                String actualElseLogic   = got.getPluginList().get(i).getAction().getElseLogic();
                if (expectedElseLogic != null) {
                    // Normalise both through Jackson so whitespace differences don't matter
                    JsonNode expectedNode = objectMapper.readTree(expectedElseLogic);
                    JsonNode actualNode   = objectMapper.readTree(actualElseLogic);
                    assertEquals(expectedNode, actualNode,
                            "elseLogic at plugin index " + i + " must decode to the original JSON");
                } else {
                    assertNull(actualElseLogic,
                            "elseLogic at plugin index " + i + " must be null when not set");
                }
            }
        }

        @Test
        @DisplayName("trackingNumberSchemaInHttpResponse round-trips correctly (JSON Base64 encoding)")
        void trackingNumberSchemaRoundTrips() throws Exception {
            WorkFlow wf = loadTestWorkflow();
            postWorkflow(APP, wf);

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            // Plugin 1 has trackingNumberSchemaInHttpResponse = "{}"
            assertEquals("{}", got.getPluginList().get(0).getAction().getTrackingNumberSchemaInHttpResponse(),
                    "trackingNumberSchemaInHttpResponse '{}' must round-trip correctly");

            // Plugin 10 has trackingNumberSchemaInHttpResponse = "" (empty → not encoded → returns "")
            assertEquals("", got.getPluginList().get(9).getAction().getTrackingNumberSchemaInHttpResponse(),
                    "Empty trackingNumberSchemaInHttpResponse must round-trip as empty string");
        }
    }

    // ══════════════════════════════════════════════
    // 11. Advanced autoCopy behaviour
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("11. Advanced autoCopy behaviour")
    class AdvancedAutoCopyBehaviour {

        @Test
        @DisplayName("autoCopy copies entity setting metadata fields (retry, tracking, eimId, region) to target")
        void autoCopyCopiesMetadataFieldsToTarget() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            // Manually enrich source entity setting with metadata
            WorkflowEntitySetting src =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            src.setRetry(true);
            src.setTracking(true);
            src.setEimId("EIM_TEST_001");
            src.setRegion("TEST_SGP");
            entitySettingRepo.saveAndFlush(src);

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            WorkflowEntitySetting target =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP_COPY).get(0);
            assertTrue(target.isRetry(),    "retry must be copied from source to target");
            assertTrue(target.isTracking(), "tracking must be copied from source to target");
            assertEquals("EIM_TEST_001", target.getEimId(),   "eimId must be copied from source to target");
            assertEquals("TEST_SGP",     target.getRegion(), "region must be copied from source to target");
        }

        @Test
        @DisplayName("autoCopy to existing target with different data: old target DB records fully replaced")
        void autoCopyOverwritesExistingTargetDbRecords() throws Exception {
            // Source: 10-plugin fixture
            postWorkflow(APP, loadTestWorkflow());
            // Pre-existing target with only 3 plugins
            postWorkflow(APP_COPY, buildSimpleWorkflow(3));

            List<Long> oldTargetRuleIds = ruleAndTypeRepo
                    .findAllByLinkingIdIn(getLinkingIdsForApp(APP_COPY))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(3, oldTargetRuleIds.size(), "Target must have 3 rules before autoCopy");

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Old target rule IDs must be gone
            long surviving = oldTargetRuleIds.stream()
                    .filter(id -> ruleRepo.findById(id).isPresent()).count();
            assertEquals(0, surviving,
                    "Old target rules (3) must be deleted and replaced after autoCopy");

            // Target must now match source plugin count
            WorkflowEntitySetting tgt =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP_COPY).get(0);
            assertEquals(FIXTURE_PLUGIN_COUNT,
                    linkingMappingRepo.findAllByWorkflowEntitySettingId(tgt.getId()).size(),
                    "Target must have source's plugin count (" + FIXTURE_PLUGIN_COUNT + ") after autoCopy");
        }

        @Test
        @DisplayName("autoCopy then DELETE source: target GET still returns correct workflow (fully independent)")
        void autoCopyThenDeleteSourceLeavesTargetIntact() throws Exception {
            postWorkflow(APP, loadTestWorkflow());
            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP)
                            .param("toApplicationName", APP_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Delete source
            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            // Source is gone
            mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isBadRequest());

            // Target is still accessible and has correct plugin count
            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP_COPY))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            assertEquals(FIXTURE_PLUGIN_COUNT, got.getPluginList().size(),
                    "Target must retain all plugins after source is deleted");
        }
    }

    // ══════════════════════════════════════════════
    // 12. Advanced update and delete behaviour
    // ══════════════════════════════════════════════

    @Nested
    @DisplayName("12. Advanced update and delete behaviour")
    class AdvancedUpdateAndDeleteBehaviour {

        @Test
        @DisplayName("Three sequential POSTs with decreasing plugin counts: no record accumulation at any step")
        void threeSequentialPostsNoAccumulation() throws Exception {
            // First POST: 10 plugins → 11 rules, 10 types
            postWorkflow(APP, loadTestWorkflow());
            List<Long> rules1 = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(FIXTURE_RULE_COUNT, rules1.size(), "Step 1 must have 11 rules");

            // Second POST: 5 plugins → 5 rules
            postWorkflow(APP, buildSimpleWorkflow(5));
            assertEquals(0, rules1.stream().filter(id -> ruleRepo.findById(id).isPresent()).count(),
                    "All step-1 rules must be gone after step-2 POST");
            List<Long> rules2 = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(5, rules2.size(), "Step 2 must have exactly 5 rules");

            // Third POST: 2 plugins → 2 rules
            postWorkflow(APP, buildSimpleWorkflow(2));
            assertEquals(0, rules2.stream().filter(id -> ruleRepo.findById(id).isPresent()).count(),
                    "All step-2 rules must be gone after step-3 POST");
            List<Long> rules3 = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            assertEquals(2, rules3.size(), "Step 3 must have exactly 2 rules");

            // Only 1 entity setting throughout
            assertEquals(1, entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).size(),
                    "Exactly one entity setting must exist after three sequential POSTs");
        }

        @Test
        @DisplayName("DELETE of workflow with no plugins (empty ruleList): entity setting is removed, GET returns 400")
        void deleteOfEmptyWorkflowRemovesEntitySetting() throws Exception {
            WorkFlow emptyWf = WorkFlow.builder().pluginList(List.of()).uiMapList(List.of()).build();
            postWorkflow(APP, emptyWf);

            assertFalse(entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).isEmpty(),
                    "Entity setting must exist after POST");
            assertTrue(linkingMappingRepo.findAllByWorkflowEntitySettingId(
                            entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0).getId()).isEmpty(),
                    "No mapping rows expected for empty workflow");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk());

            assertTrue(entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).isEmpty(),
                    "Entity setting must be removed after DELETE of empty workflow");
            mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST update resets trackingServiceProviderActionId to null on the entity setting")
        void postUpdateResetsTrackingServiceProviderActionId() throws Exception {
            postWorkflow(APP, loadTestWorkflow());

            // Manually set the tracking FK to a real WorkflowType
            WorkflowEntitySetting s =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            WorkflowType anyType = ruleAndTypeRepo.findAllByLinkingIdIn(getLinkingIdsForApp(APP))
                    .get(0).getWorkflowType();
            s.setTrackingServiceProviderActionId(anyType);
            entitySettingRepo.saveAndFlush(s);
            assertNotNull(
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0)
                            .getTrackingServiceProviderActionId(),
                    "Precondition: trackingServiceProviderActionId must be set before second POST");

            // Second POST triggers deleteWorkflowRulesMappingsAndTypes which nulls the FK
            postWorkflow(APP, buildSimpleWorkflow(2));

            WorkflowEntitySetting updated =
                    entitySettingRepo.getWorkflowEntitySettingByApplicationName(APP).get(0);
            assertNull(updated.getTrackingServiceProviderActionId(),
                    "trackingServiceProviderActionId must be reset to null after POST update");
            assertNull(updated.getTrackingServiceProviderActionId2(),
                    "trackingServiceProviderActionId2 must be reset to null after POST update");
        }

        @Test
        @DisplayName("GET returns correct plugin descriptions after updating with out-of-order plugins")
        void updateWithOutOfOrderPluginsGetReturnsCorrectDescriptions() throws Exception {
            postWorkflow(APP, buildOutOfOrderWorkflow()); // sends [3, 1, 2]

            MvcResult r = mockMvc.perform(get("/api/workflow").param("applicationName", APP))
                    .andExpect(status().isOk()).andReturn();
            WorkFlow got = objectMapper.readValue(r.getResponse().getContentAsString(), WorkFlow.class);
            got.getPluginList().sort(Comparator.comparing(Plugin::getId));

            assertEquals("Out-of-order step 1", got.getPluginList().get(0).getDescription(),
                    "Plugin at index 0 (id=1) must have correct description");
            assertEquals("Out-of-order step 2", got.getPluginList().get(1).getDescription(),
                    "Plugin at index 1 (id=2) must have correct description");
            assertEquals("Out-of-order step 3", got.getPluginList().get(2).getDescription(),
                    "Plugin at index 2 (id=3) must have correct description");
        }
    }
}
