package com.workflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.controller.domain.Plugin;
import com.workflow.controller.domain.WorkFlow;
import com.workflow.dao.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WorkflowEntitySettingRepository entitySettingRepository;
    @Autowired
    private WorkflowEntityAndLinkingIdMappingRepository linkingIdMappingRepository;
    @Autowired
    private WorkflowRuleAndTypeRepository ruleAndTypeRepository;
    @Autowired
    private WorkflowRuleRepository ruleRepository;
    @Autowired
    private WorkflowTypeRepository typeRepository;
    @Autowired
    private WorkflowReportRepository reportRepository;

    private static final String APP_NAME = "ITEST_APP";
    private static final String APP_NAME_2 = "ITEST_APP_2";
    private static final String APP_NAME_COPY = "ITEST_APP_COPY";

    @BeforeEach
    void cleanup() throws Exception {
        cleanupApp(APP_NAME_COPY);
        cleanupApp(APP_NAME_2);
        cleanupApp(APP_NAME);
    }

    @AfterEach
    void teardown() throws Exception {
        cleanupApp(APP_NAME_COPY);
        cleanupApp(APP_NAME_2);
        cleanupApp(APP_NAME);
    }

    private void cleanupApp(String appName) throws Exception {
        List<WorkflowEntitySetting> settings = entitySettingRepository.getWorkflowEntitySettingByApplicationName(appName);
        for (WorkflowEntitySetting setting : settings) {
            reportRepository.findByWorkflowEntitySetting_Id(setting.getId())
                    .forEach(r -> reportRepository.deleteById(r.getId()));
            reportRepository.flush();
        }
        try {
            mockMvc.perform(delete("/api/workflow").param("applicationName", appName));
        } catch (Exception ignored) {
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST -> DB verification
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST workflow -> verify DB tables populated correctly")
    class PostDbVerification {

        @Test
        @DisplayName("All DB tables should contain expected records after POST")
        void postWorkflowPopulatesAllDbTables() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            List<WorkflowEntitySetting> entitySettings =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME);
            assertEquals(1, entitySettings.size(), "Exactly one entity setting should exist");

            WorkflowEntitySetting setting = entitySettings.get(0);
            assertNotNull(setting.getId());
            assertEquals(APP_NAME, setting.getApplicationName());
            assertTrue(setting.isEnabled(), "Entity setting should be enabled");
            assertNotNull(setting.getWorkflow(), "Workflow JSON should be stored");

            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            assertEquals(requestBody.getPluginList().size(), mappings.size(),
                    "One linking-id mapping per plugin");

            for (WorkflowEntityAndLinkingIdMapping mapping : mappings) {
                assertNotNull(mapping.getLinkingId(), "linkingId must not be null");
                assertFalse(mapping.getLinkingId().isBlank(), "linkingId must not be blank");
                assertNotNull(mapping.getLogicOrder(), "logicOrder must not be null");
                assertNotNull(mapping.getRemark(), "remark (description) must not be null");
            }

            List<String> linkingIds = mappings.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId).distinct().toList();
            List<WorkflowRuleAndType> ruleAndTypes =
                    ruleAndTypeRepository.findAllByLinkingIdIn(linkingIds);
            assertFalse(ruleAndTypes.isEmpty(), "Rule-and-type mappings must exist");

            int expectedTotalRules = requestBody.getPluginList().stream()
                    .mapToInt(p -> p.getRuleList() != null && !p.getRuleList().isEmpty()
                            ? p.getRuleList().size() : 1)
                    .sum();
            assertEquals(expectedTotalRules, ruleAndTypes.size(),
                    "Total rule-and-type mappings should match total rules across all plugins");

            for (WorkflowRuleAndType rat : ruleAndTypes) {
                assertNotNull(rat.getWorkflowRule(), "Rule reference must not be null");
                assertNotNull(rat.getWorkflowType(), "Type reference must not be null");
                assertNotNull(rat.getWorkflowRule().getId());
                assertNotNull(rat.getWorkflowType().getId());
                assertTrue(ruleRepository.existsById(rat.getWorkflowRule().getId()),
                        "Rule should exist in rule table");
                assertTrue(typeRepository.existsById(rat.getWorkflowType().getId()),
                        "Type should exist in type table");
            }

            long distinctTypeIds = ruleAndTypes.stream()
                    .map(rt -> rt.getWorkflowType().getId()).distinct().count();
            assertEquals(requestBody.getPluginList().size(), distinctTypeIds,
                    "One distinct type per plugin");
        }

        @Test
        @DisplayName("LinkingId format should include entitySettingId, typeId, and pluginId")
        void linkingIdFormatIsCorrect() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());

            for (WorkflowEntityAndLinkingIdMapping mapping : mappings) {
                String linkingId = mapping.getLinkingId();
                String[] parts = linkingId.split("_");
                assertEquals(3, parts.length,
                        "LinkingId should have format entitySettingId_typeId_pluginId: " + linkingId);
                assertEquals(String.valueOf(setting.getId()), parts[0],
                        "First part of linkingId should be entity setting ID");
                assertEquals(String.valueOf(mapping.getLogicOrder()), parts[2],
                        "Third part of linkingId should be plugin ID (logicOrder)");
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Round-trip data fidelity
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST then GET -> round-trip data fidelity")
    class RoundTripFidelity {

        @Test
        @DisplayName("Plugin count, IDs, descriptions, action types, and rule keys survive round-trip")
        void fullPluginFidelity() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();
            String requestJson = objectMapper.writeValueAsString(requestBody);

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk());

            MvcResult getResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);
            assertNotNull(got);
            assertEquals(requestBody.getPluginList().size(), got.getPluginList().size(),
                    "Plugin count should match");

            for (int i = 0; i < requestBody.getPluginList().size(); i++) {
                Plugin expected = requestBody.getPluginList().get(i);
                Plugin actual = got.getPluginList().get(i);
                assertEquals(expected.getId(), actual.getId(), "Plugin id at index " + i);
                assertEquals(expected.getDescription(), actual.getDescription(),
                        "Plugin description at index " + i);
                assertEquals(expected.getAction().getType(), actual.getAction().getType(),
                        "Action type at index " + i);
                assertEquals(expected.getAction().getProvider(), actual.getAction().getProvider(),
                        "Action provider at index " + i);
                assertEquals(expected.getAction().getRemark(), actual.getAction().getRemark(),
                        "Action remark at index " + i);

                assertEquals(expected.getRuleList().size(), actual.getRuleList().size(),
                        "Rule count at index " + i);
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
        @DisplayName("Action HTTP fields survive round-trip (method, URL, headers, body)")
        void actionHttpFieldsFidelity() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            MvcResult getResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);

            for (int i = 0; i < requestBody.getPluginList().size(); i++) {
                var expected = requestBody.getPluginList().get(i).getAction();
                var actual = got.getPluginList().get(i).getAction();

                assertEquals(expected.getHttpRequestMethod(), actual.getHttpRequestMethod(),
                        "httpRequestMethod at plugin " + i);
                assertEquals(expected.getHttpRequestUrlWithQueryParameter(),
                        actual.getHttpRequestUrlWithQueryParameter(),
                        "httpRequestUrlWithQueryParameter at plugin " + i);
                assertEquals(expected.getInternalHttpRequestUrlWithQueryParameter(),
                        actual.getInternalHttpRequestUrlWithQueryParameter(),
                        "internalHttpRequestUrlWithQueryParameter at plugin " + i);
                assertEquals(expected.getHttpRequestHeaders(), actual.getHttpRequestHeaders(),
                        "httpRequestHeaders at plugin " + i);
                assertEquals(expected.getHttpRequestBody(), actual.getHttpRequestBody(),
                        "httpRequestBody at plugin " + i);
                assertEquals(expected.getElseLogic(), actual.getElseLogic(),
                        "elseLogic at plugin " + i);
            }
        }

        @Test
        @DisplayName("uiMapList survives round-trip")
        void uiMapListFidelity() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            MvcResult getResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);

            assertNotNull(got.getUiMapList(), "uiMapList should be present");
            assertEquals(requestBody.getUiMapList().size(), got.getUiMapList().size(),
                    "uiMapList size should match");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE -> DB verification
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE workflow -> verify all DB records removed")
    class DeleteDbVerification {

        @Test
        @DisplayName("DELETE removes entity setting, mappings, rules, and types from DB")
        void deleteRemovesAllDbRecords() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting settingBeforeDelete =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            Long settingId = settingBeforeDelete.getId();

            List<WorkflowEntityAndLinkingIdMapping> mappingsBefore =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(settingId);
            List<String> linkingIdsBefore = mappingsBefore.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId).distinct().toList();
            List<WorkflowRuleAndType> ruleAndTypesBefore =
                    ruleAndTypeRepository.findAllByLinkingIdIn(linkingIdsBefore);
            List<Long> ruleIdsBefore = ruleAndTypesBefore.stream()
                    .map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            List<Long> typeIdsBefore = ruleAndTypesBefore.stream()
                    .map(rt -> rt.getWorkflowType().getId()).distinct().toList();

            assertFalse(mappingsBefore.isEmpty(), "Pre-condition: mappings exist before delete");
            assertFalse(ruleIdsBefore.isEmpty(), "Pre-condition: rules exist before delete");
            assertFalse(typeIdsBefore.isEmpty(), "Pre-condition: types exist before delete");

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());

            assertTrue(entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).isEmpty(),
                    "Entity setting should be removed");
            assertTrue(linkingIdMappingRepository.findAllByWorkflowEntitySettingId(settingId).isEmpty(),
                    "Linking-id mappings should be removed");
            assertTrue(ruleAndTypeRepository.findAllByLinkingIdIn(linkingIdsBefore).isEmpty(),
                    "Rule-and-type mappings should be removed");
            for (Long ruleId : ruleIdsBefore) {
                assertFalse(ruleRepository.existsById(ruleId),
                        "Rule " + ruleId + " should be removed");
            }
            for (Long typeId : typeIdsBefore) {
                assertFalse(typeRepository.existsById(typeId),
                        "Type " + typeId + " should be removed");
            }
        }

        @Test
        @DisplayName("DELETE then GET returns 400")
        void deleteThenGetReturns400() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE non-existent application returns 400")
        void deleteNonExistentReturns400() throws Exception {
            mockMvc.perform(delete("/api/workflow").param("applicationName", "NON_EXISTENT_APP"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Double DELETE - second DELETE returns 400")
        void doubleDeleteReturns400OnSecond() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());
            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // UPDATE (re-POST) -> DB verification
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST update (re-POST) -> old data replaced, DB consistent")
    class UpdateDbVerification {

        @Test
        @DisplayName("Re-POST replaces old rules/types/mappings with new ones")
        void rePostReplacesOldData() throws Exception {
            WorkFlow original = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(original)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting settingAfterFirst =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappingsFirst =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(settingAfterFirst.getId());
            List<String> linkingIdsFirst = mappingsFirst.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId).distinct().toList();
            List<WorkflowRuleAndType> ruleAndTypesFirst =
                    ruleAndTypeRepository.findAllByLinkingIdIn(linkingIdsFirst);
            List<Long> oldRuleIds = ruleAndTypesFirst.stream()
                    .map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            List<Long> oldTypeIds = ruleAndTypesFirst.stream()
                    .map(rt -> rt.getWorkflowType().getId()).distinct().toList();

            WorkFlow modified = loadTestWorkflow();
            modified.setPluginList(modified.getPluginList().subList(0, 3));

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(modified)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting settingAfterSecond =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            assertEquals(settingAfterFirst.getId(), settingAfterSecond.getId(),
                    "Entity setting ID should be reused");

            List<WorkflowEntityAndLinkingIdMapping> mappingsSecond =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(settingAfterSecond.getId());
            assertEquals(3, mappingsSecond.size(),
                    "After re-POST with 3 plugins, exactly 3 mappings should exist");

            for (Long oldRuleId : oldRuleIds) {
                assertFalse(ruleRepository.existsById(oldRuleId),
                        "Old rule " + oldRuleId + " should be removed after re-POST");
            }
            for (Long oldTypeId : oldTypeIds) {
                assertFalse(typeRepository.existsById(oldTypeId),
                        "Old type " + oldTypeId + " should be removed after re-POST");
            }

            assertTrue(ruleAndTypeRepository.findAllByLinkingIdIn(linkingIdsFirst).isEmpty(),
                    "Old rule-and-type mappings should be removed after re-POST");

            List<String> newLinkingIds = mappingsSecond.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId).distinct().toList();
            List<WorkflowRuleAndType> newRuleAndTypes =
                    ruleAndTypeRepository.findAllByLinkingIdIn(newLinkingIds);
            assertFalse(newRuleAndTypes.isEmpty(), "New rule-and-type mappings should exist");
        }

        @Test
        @DisplayName("Re-POST returns correct GET response reflecting new data")
        void rePostGetReflectsNewData() throws Exception {
            WorkFlow original = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(original)))
                    .andExpect(status().isOk());

            WorkFlow modified = loadTestWorkflow();
            modified.setPluginList(modified.getPluginList().subList(0, 5));

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(modified)))
                    .andExpect(status().isOk());

            MvcResult getResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);

            assertEquals(5, got.getPluginList().size(),
                    "GET should return 5 plugins after re-POST with 5 plugins");

            for (int i = 0; i < 5; i++) {
                assertEquals(modified.getPluginList().get(i).getId(),
                        got.getPluginList().get(i).getId());
                assertEquals(modified.getPluginList().get(i).getAction().getType(),
                        got.getPluginList().get(i).getAction().getType());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AutoCopy integration
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("AutoCopy workflow -> source intact, target correct in DB")
    class AutoCopyIntegration {

        @Test
        @DisplayName("AutoCopy creates target with same plugin structure as source")
        void autoCopyCreatesTargetWithSameStructure() throws Exception {
            WorkFlow original = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(original)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP_NAME)
                            .param("toApplicationName", APP_NAME_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            MvcResult sourceResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();
            MvcResult targetResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME_COPY))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow source = objectMapper.readValue(
                    sourceResult.getResponse().getContentAsString(), WorkFlow.class);
            WorkFlow target = objectMapper.readValue(
                    targetResult.getResponse().getContentAsString(), WorkFlow.class);

            assertEquals(source.getPluginList().size(), target.getPluginList().size(),
                    "Target should have same number of plugins as source");

            for (int i = 0; i < source.getPluginList().size(); i++) {
                Plugin sp = source.getPluginList().get(i);
                Plugin tp = target.getPluginList().get(i);
                assertEquals(sp.getId(), tp.getId(), "Plugin id at index " + i);
                assertEquals(sp.getAction().getType(), tp.getAction().getType(),
                        "Action type at index " + i);
                assertEquals(sp.getAction().getProvider(), tp.getAction().getProvider(),
                        "Action provider at index " + i);
                assertEquals(sp.getRuleList().size(), tp.getRuleList().size(),
                        "Rule count at index " + i);
                for (int j = 0; j < sp.getRuleList().size(); j++) {
                    assertEquals(sp.getRuleList().get(j).getKey(),
                            tp.getRuleList().get(j).getKey(),
                            "Rule key at plugin " + i + " rule " + j);
                }
            }
        }

        @Test
        @DisplayName("AutoCopy creates independent DB records for target")
        void autoCopyCreatesIndependentDbRecords() throws Exception {
            WorkFlow original = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(original)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP_NAME)
                            .param("toApplicationName", APP_NAME_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            List<WorkflowEntitySetting> sourceSettings =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME);
            List<WorkflowEntitySetting> targetSettings =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME_COPY);
            assertEquals(1, sourceSettings.size());
            assertEquals(1, targetSettings.size());
            assertNotEquals(sourceSettings.get(0).getId(), targetSettings.get(0).getId(),
                    "Source and target should have different entity setting IDs");

            List<WorkflowEntityAndLinkingIdMapping> sourceMappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(sourceSettings.get(0).getId());
            List<WorkflowEntityAndLinkingIdMapping> targetMappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(targetSettings.get(0).getId());
            assertEquals(sourceMappings.size(), targetMappings.size(),
                    "Same number of linking-id mappings");

            List<String> sourceLinkingIds = sourceMappings.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId).toList();
            List<String> targetLinkingIds = targetMappings.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId).toList();

            for (String targetLinkingId : targetLinkingIds) {
                assertFalse(sourceLinkingIds.contains(targetLinkingId),
                        "Target linkingId " + targetLinkingId + " must differ from source IDs");
            }
        }

        @Test
        @DisplayName("Deleting target does not affect source")
        void deletingTargetDoesNotAffectSource() throws Exception {
            WorkFlow original = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(original)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP_NAME)
                            .param("toApplicationName", APP_NAME_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME_COPY))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());

            WorkflowEntitySetting sourceSetting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> sourceMappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(sourceSetting.getId());
            assertEquals(original.getPluginList().size(), sourceMappings.size(),
                    "Source mappings should be intact after deleting target");
        }

        @Test
        @DisplayName("AutoCopy with same source and target returns 400")
        void autoCopySameNameReturns400() throws Exception {
            WorkFlow original = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(original)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", APP_NAME)
                            .param("toApplicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("AutoCopy from non-existent source returns 400")
        void autoCopyNonExistentSourceReturns400() throws Exception {
            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", "NON_EXISTENT")
                            .param("toApplicationName", APP_NAME_COPY)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Delete blocked when reports exist
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DELETE blocked when reports exist (409)")
    class DeleteBlockedByReports {

        @Test
        @DisplayName("DELETE returns 409 when workflow has associated reports")
        void deleteReturns409WhenReportsExist() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);

            WorkflowReport report = WorkflowReport.builder()
                    .workflowEntitySetting(setting)
                    .reportGroup(1L)
                    .enabled(true)
                    .cronExpression("0 0 * * *")
                    .reportTimeRangeByHours(24)
                    .timezone("UTC")
                    .build();
            reportRepository.saveAndFlush(report);

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isConflict());

            assertEquals(1,
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).size(),
                    "Entity setting should still exist after blocked delete");

            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            assertFalse(mappings.isEmpty(),
                    "Mappings should still exist after blocked delete");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Validation tests
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Validation error handling")
    class ValidationErrors {

        @Test
        @DisplayName("POST without body returns 400")
        void postWithoutBodyReturns400() throws Exception {
            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET non-existent application returns 400")
        void getNonExistentReturns400() throws Exception {
            mockMvc.perform(get("/api/workflow")
                            .param("applicationName", "NON_EXISTENT_APP"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST with empty applicationName parameter is rejected")
        void postEmptyApplicationNameRejected() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", "")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/workflow").param("applicationName", ""));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Edge cases
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("POST workflow with empty plugin list")
        void postEmptyPluginList() throws Exception {
            WorkFlow emptyWorkflow = WorkFlow.builder()
                    .pluginList(List.of())
                    .uiMapList(List.of())
                    .build();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyWorkflow)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            assertTrue(mappings.isEmpty(),
                    "No linking-id mappings for empty plugin list");

            MvcResult getResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);
            assertTrue(got.getPluginList().isEmpty(),
                    "GET should return empty plugin list");
        }

        @Test
        @DisplayName("POST workflow with single plugin having multiple rules")
        void postSinglePluginMultipleRules() throws Exception {
            WorkFlow fullWorkflow = loadTestWorkflow();
            Plugin multiRulePlugin = fullWorkflow.getPluginList().get(
                    fullWorkflow.getPluginList().size() - 1);
            assertTrue(multiRulePlugin.getRuleList().size() > 1,
                    "Test data plugin 10 should have multiple rules");

            WorkFlow singlePlugin = WorkFlow.builder()
                    .pluginList(List.of(multiRulePlugin))
                    .uiMapList(List.of())
                    .build();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(singlePlugin)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            assertEquals(1, mappings.size(), "One mapping for one plugin");

            List<WorkflowRuleAndType> ruleAndTypes =
                    ruleAndTypeRepository.findAllByLinkingIdIn(
                            mappings.stream().map(WorkflowEntityAndLinkingIdMapping::getLinkingId).toList());
            assertEquals(multiRulePlugin.getRuleList().size(), ruleAndTypes.size(),
                    "Rule-and-type mapping count should match rule count in the plugin");

            long distinctTypes = ruleAndTypes.stream()
                    .map(rt -> rt.getWorkflowType().getId()).distinct().count();
            assertEquals(1, distinctTypes,
                    "All rules in a single plugin should share the same type");
        }

        @Test
        @DisplayName("POST then DELETE then POST again creates fresh data")
        void postDeletePostCreatesFreshData() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());

            WorkFlow secondPost = loadTestWorkflow();
            secondPost.setPluginList(secondPost.getPluginList().subList(0, 2));

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(secondPost)))
                    .andExpect(status().isOk());

            List<WorkflowEntitySetting> settings =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME);
            assertEquals(1, settings.size());

            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(settings.get(0).getId());
            assertEquals(2, mappings.size(),
                    "After DELETE then POST with 2 plugins, exactly 2 mappings should exist");

            MvcResult getResult = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();
            WorkFlow got = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(), WorkFlow.class);
            assertEquals(2, got.getPluginList().size());
        }

        @Test
        @DisplayName("Multiple applications are isolated from each other")
        void multipleApplicationsIsolated() throws Exception {
            WorkFlow fullWorkflow = loadTestWorkflow();
            WorkFlow partialWorkflow = loadTestWorkflow();
            partialWorkflow.setPluginList(partialWorkflow.getPluginList().subList(0, 3));

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(fullWorkflow)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME_2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partialWorkflow)))
                    .andExpect(status().isOk());

            MvcResult result1 = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME))
                    .andExpect(status().isOk())
                    .andReturn();
            MvcResult result2 = mockMvc.perform(get("/api/workflow")
                            .param("applicationName", APP_NAME_2))
                    .andExpect(status().isOk())
                    .andReturn();

            WorkFlow got1 = objectMapper.readValue(
                    result1.getResponse().getContentAsString(), WorkFlow.class);
            WorkFlow got2 = objectMapper.readValue(
                    result2.getResponse().getContentAsString(), WorkFlow.class);

            assertEquals(fullWorkflow.getPluginList().size(), got1.getPluginList().size());
            assertEquals(3, got2.getPluginList().size());

            mockMvc.perform(delete("/api/workflow").param("applicationName", APP_NAME_2))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/workflow").param("applicationName", APP_NAME))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting1 =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings1 =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting1.getId());
            assertEquals(fullWorkflow.getPluginList().size(), mappings1.size(),
                    "First application's mappings should be intact after deleting second");
        }

        @Test
        @DisplayName("POST workflow with null pluginList treated as empty")
        void postNullPluginListTreatedAsEmpty() throws Exception {
            WorkFlow nullPluginWorkflow = WorkFlow.builder()
                    .pluginList(null)
                    .uiMapList(null)
                    .build();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nullPluginWorkflow)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            assertTrue(mappings.isEmpty(),
                    "No mappings for null plugin list");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DB-level rule and type content verification
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("DB-level content verification")
    class DbContentVerification {

        @Test
        @DisplayName("Rules in DB have correct keys and remarks matching input")
        void rulesInDbMatchInput() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            mappings.sort((a, b) -> a.getLogicOrder().compareTo(b.getLogicOrder()));

            for (int i = 0; i < mappings.size(); i++) {
                Plugin expectedPlugin = requestBody.getPluginList().get(i);
                WorkflowEntityAndLinkingIdMapping mapping = mappings.get(i);

                assertEquals(expectedPlugin.getId(), mapping.getLogicOrder(),
                        "logicOrder should match plugin ID at index " + i);
                assertEquals(expectedPlugin.getDescription(), mapping.getRemark(),
                        "remark should match plugin description at index " + i);

                List<WorkflowRuleAndType> ruleAndTypes =
                        ruleAndTypeRepository.findAllByLinkingIdIn(List.of(mapping.getLinkingId()));
                assertEquals(expectedPlugin.getRuleList().size(), ruleAndTypes.size(),
                        "Rule count in DB should match input for plugin " + i);

                for (int j = 0; j < ruleAndTypes.size(); j++) {
                    WorkflowRule dbRule = ruleAndTypes.get(j).getWorkflowRule();
                    WorkflowRule inputRule = expectedPlugin.getRuleList().get(j);
                    assertEquals(inputRule.getKey(), dbRule.getKey(),
                            "Rule key in DB should match input for plugin " + i + " rule " + j);
                    assertEquals(inputRule.getRemark(), dbRule.getRemark(),
                            "Rule remark in DB should match input for plugin " + i + " rule " + j);
                }
            }
        }

        @Test
        @DisplayName("Types in DB have correct provider and type fields matching input")
        void typesInDbMatchInput() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    linkingIdMappingRepository.findAllByWorkflowEntitySettingId(setting.getId());
            mappings.sort((a, b) -> a.getLogicOrder().compareTo(b.getLogicOrder()));

            for (int i = 0; i < mappings.size(); i++) {
                Plugin expectedPlugin = requestBody.getPluginList().get(i);
                WorkflowEntityAndLinkingIdMapping mapping = mappings.get(i);

                List<WorkflowRuleAndType> ruleAndTypes =
                        ruleAndTypeRepository.findAllByLinkingIdIn(List.of(mapping.getLinkingId()));
                assertFalse(ruleAndTypes.isEmpty());

                WorkflowType dbType = ruleAndTypes.get(0).getWorkflowType();
                assertEquals(expectedPlugin.getAction().getProvider(), dbType.getProvider(),
                        "Provider in DB should match at plugin " + i);
                assertEquals(expectedPlugin.getAction().getType(), dbType.getType(),
                        "Type in DB should match at plugin " + i);
                assertEquals(expectedPlugin.getAction().getRemark(), dbType.getRemark(),
                        "Remark in DB should match at plugin " + i);
                assertEquals(expectedPlugin.getAction().getHttpRequestMethod(),
                        dbType.getHttpRequestMethod(),
                        "httpRequestMethod in DB should match at plugin " + i);
            }
        }

        @Test
        @DisplayName("Workflow JSON in entity setting is base64-encoded and decodable")
        void workflowJsonIsBase64Encoded() throws Exception {
            WorkFlow requestBody = loadTestWorkflow();

            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APP_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(status().isOk());

            WorkflowEntitySetting setting =
                    entitySettingRepository.getWorkflowEntitySettingByApplicationName(APP_NAME).get(0);
            assertNotNull(setting.getWorkflow());
            assertFalse(setting.getWorkflow().isEmpty());

            String decoded = com.workflow.common.util.Base64Util.base64Decode(
                    setting.getWorkflow(), true, objectMapper);
            WorkFlow decodedWorkflow = objectMapper.readValue(decoded, WorkFlow.class);
            assertEquals(requestBody.getPluginList().size(),
                    decodedWorkflow.getPluginList().size(),
                    "Decoded workflow should have same plugin count as input");
        }
    }

    private WorkFlow loadTestWorkflow() throws IOException {
        String json = new String(new ClassPathResource("workflow-integration-test-data.json")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return objectMapper.readValue(json, WorkFlow.class);
    }
}
