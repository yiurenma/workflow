package com.workflow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflow.controller.domain.Plugin;
import com.workflow.controller.domain.WorkFlow;
import com.workflow.dao.repository.WorkflowEntityAndLinkingIdMapping;
import com.workflow.dao.repository.WorkflowEntityAndLinkingIdMappingRepository;
import com.workflow.dao.repository.WorkflowEntitySetting;
import com.workflow.dao.repository.WorkflowEntitySettingRepository;
import com.workflow.dao.repository.WorkflowReport;
import com.workflow.dao.repository.WorkflowReportRepository;
import com.workflow.dao.repository.WorkflowRule;
import com.workflow.dao.repository.WorkflowRuleAndType;
import com.workflow.dao.repository.WorkflowRuleAndTypeRepository;
import com.workflow.dao.repository.WorkflowRuleRepository;
import com.workflow.dao.repository.WorkflowType;
import com.workflow.dao.repository.WorkflowTypeRepository;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for Workflow APIs with DB verification:
 * POST/GET/DELETE and auto-copy behaviors are validated against persistent state.
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
    private WorkflowEntitySettingRepository workflowEntitySettingRepository;

    @Autowired
    private WorkflowEntityAndLinkingIdMappingRepository workflowEntityAndLinkingIdMappingRepository;

    @Autowired
    private WorkflowRuleAndTypeRepository workflowRuleAndTypeRepository;

    @Autowired
    private WorkflowRuleRepository workflowRuleRepository;

    @Autowired
    private WorkflowTypeRepository workflowTypeRepository;

    @Autowired
    private WorkflowReportRepository workflowReportRepository;

    private static final String APPLICATION_NAME = "ITEST_APP";
    private static final String SOURCE_APPLICATION_NAME = "ITEST_SOURCE_APP";
    private static final String TARGET_APPLICATION_NAME = "ITEST_TARGET_APP";
    private static final String LOCKED_APPLICATION_NAME = "ITEST_LOCKED_APP";
    private static final String KEEP_APPLICATION_NAME = "ITEST_KEEP_APP";
    private static final String CORRUPT_APPLICATION_NAME = "ITEST_CORRUPT_APP";
    private static final String DUPLICATE_APPLICATION_NAME = "ITEST_DUP_APP";

    @BeforeEach
    void cleanup() {
        workflowReportRepository.deleteAllInBatch();
        workflowEntityAndLinkingIdMappingRepository.deleteAllInBatch();
        workflowRuleAndTypeRepository.deleteAllInBatch();
        workflowEntitySettingRepository.deleteAllInBatch();
        workflowRuleRepository.deleteAllInBatch();
        workflowTypeRepository.deleteAllInBatch();
    }

    @Nested
    @DisplayName("POST and DB verification")
    class PostAndDatabaseChecks {

        @Test
        @DisplayName("POST persists entity, mappings, rules and types correctly")
        void postWorkflowPersistsExpectedData() throws Exception {
            WorkFlow workFlow = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(1, "first", "TYPE_A", List.of("$.a", "$.b")),
                            plugin(2, "second", "TYPE_B", List.of())
                    ))
                    .uiMapList(List.of(Map.of("id", "edge-1"), Map.of("id", "edge-2")))
                    .build();

            postWorkflow(APPLICATION_NAME, workFlow)
                    .andExpect(status().isOk());

            List<WorkflowEntitySetting> entitySettings =
                    workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(APPLICATION_NAME);
            assertEquals(1, entitySettings.size(), "Entity setting should be created once");

            WorkflowEntitySetting entitySetting = entitySettings.get(0);
            assertNotNull(entitySetting.getWorkflow(), "Encoded workflow JSON should be persisted in entity setting");

            List<WorkflowEntityAndLinkingIdMapping> mappings =
                    workflowEntityAndLinkingIdMappingRepository.findAllByWorkflowEntitySettingId(entitySetting.getId());
            assertEquals(2, mappings.size(), "Each plugin should create one entity-linking mapping");

            List<String> linkingIds = mappings.stream()
                    .map(WorkflowEntityAndLinkingIdMapping::getLinkingId)
                    .toList();
            List<WorkflowRuleAndType> ruleAndTypes = workflowRuleAndTypeRepository.findAllByLinkingIdIn(linkingIds);

            assertEquals(3, ruleAndTypes.size(),
                    "Two explicit rules + one generated empty rule for plugin without rules");
            assertEquals(Set.of("$.a", "$.b", ""),
                    ruleAndTypes.stream().map(rt -> rt.getWorkflowRule().getKey()).collect(java.util.stream.Collectors.toSet()));
            assertEquals(Set.of("TYPE_A", "TYPE_B"),
                    ruleAndTypes.stream().map(rt -> rt.getWorkflowType().getType()).collect(java.util.stream.Collectors.toSet()));
        }

        @Test
        @DisplayName("POST update replaces previous mappings/rules/types without leftovers")
        void postWorkflowReplacesOldDataWithoutOrphans() throws Exception {
            WorkFlow firstVersion = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(1, "first-v1", "TYPE_V1", List.of("$.one")),
                            plugin(2, "second-v1", "TYPE_V1_B", List.of("$.two"))
                    ))
                    .uiMapList(List.of(Map.of("id", "v1-edge")))
                    .build();
            postWorkflow(APPLICATION_NAME, firstVersion)
                    .andExpect(status().isOk());

            WorkflowEntitySetting originalEntitySetting = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(APPLICATION_NAME)
                    .get(0);
            List<WorkflowEntityAndLinkingIdMapping> oldMappings = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(originalEntitySetting.getId());
            List<Long> oldMappingIds = oldMappings.stream().map(WorkflowEntityAndLinkingIdMapping::getId).toList();

            List<WorkflowRuleAndType> oldRuleAndTypes = workflowRuleAndTypeRepository.findAllByLinkingIdIn(
                    oldMappings.stream().map(WorkflowEntityAndLinkingIdMapping::getLinkingId).toList()
            );
            List<Long> oldRuleAndTypeIds = oldRuleAndTypes.stream().map(WorkflowRuleAndType::getId).toList();
            List<Long> oldRuleIds = oldRuleAndTypes.stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            List<Long> oldTypeIds = oldRuleAndTypes.stream().map(rt -> rt.getWorkflowType().getId()).distinct().toList();

            WorkFlow secondVersion = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(10, "first-v2", "TYPE_V2", List.of("$.alpha", "$.beta")),
                            plugin(11, "second-v2", "TYPE_V2_B", List.of())
                    ))
                    .uiMapList(List.of(Map.of("id", "v2-edge")))
                    .build();
            postWorkflow(APPLICATION_NAME, secondVersion)
                    .andExpect(status().isOk());

            WorkflowEntitySetting updatedEntitySetting = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(APPLICATION_NAME)
                    .get(0);
            assertEquals(originalEntitySetting.getId(), updatedEntitySetting.getId(),
                    "Update should reuse the same entity setting row");

            for (Long oldMappingId : oldMappingIds) {
                assertFalse(workflowEntityAndLinkingIdMappingRepository.existsById(oldMappingId),
                        "Old entity-linking mapping should be deleted: " + oldMappingId);
            }
            for (Long oldRuleAndTypeId : oldRuleAndTypeIds) {
                assertFalse(workflowRuleAndTypeRepository.existsById(oldRuleAndTypeId),
                        "Old rule-type mapping should be deleted: " + oldRuleAndTypeId);
            }
            for (Long oldRuleId : oldRuleIds) {
                assertFalse(workflowRuleRepository.existsById(oldRuleId),
                        "Old rule should be deleted: " + oldRuleId);
            }
            for (Long oldTypeId : oldTypeIds) {
                assertFalse(workflowTypeRepository.existsById(oldTypeId),
                        "Old action type should be deleted: " + oldTypeId);
            }
        }

        @Test
        @DisplayName("POST then GET keeps action fields decoded and workflow structure consistent")
        void postThenGetReturnsExpectedDecodedFields() throws Exception {
            WorkFlow input = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(1, "decode-first", "TYPE_DECODE", List.of("$.d1", "$.d2")),
                            plugin(2, "decode-second", "TYPE_EMPTY_RULES", List.of())
                    ))
                    .uiMapList(List.of(Map.of("id", "decode-edge")))
                    .build();

            postWorkflow(APPLICATION_NAME, input)
                    .andExpect(status().isOk());

            WorkFlow got = getWorkflow(APPLICATION_NAME);
            assertNotNull(got);
            assertEquals(2, got.getPluginList().size());
            assertEquals(1, got.getUiMapList().size());

            Plugin first = got.getPluginList().get(0);
            assertEquals(1, first.getId());
            assertEquals("decode-first", first.getDescription());
            assertEquals("TYPE_DECODE", first.getAction().getType());
            assertEquals("{\"fallback\":1}", first.getAction().getElseLogic());
            assertEquals("https://example.com/1", first.getAction().getHttpRequestUrlWithQueryParameter());
            assertEquals("https://internal.example.com/1", first.getAction().getInternalHttpRequestUrlWithQueryParameter());
            assertEquals("{\"Content-Type\":\"application/json\"}", first.getAction().getHttpRequestHeaders());
            assertEquals("{\"id\":1}", first.getAction().getHttpRequestBody());
            assertEquals("{\"tracking\":1}", first.getAction().getTrackingNumberSchemaInHttpResponse());
            assertEquals(Set.of("$.d1", "$.d2"),
                    first.getRuleList().stream().map(WorkflowRule::getKey).collect(java.util.stream.Collectors.toSet()));

            Plugin second = got.getPluginList().get(1);
            assertEquals(2, second.getId());
            assertEquals("decode-second", second.getDescription());
            assertEquals("TYPE_EMPTY_RULES", second.getAction().getType());
            assertEquals(1, second.getRuleList().size(), "Plugin without rules should still have generated empty rule");
            assertEquals("", second.getRuleList().get(0).getKey());
        }
    }

    @Nested
    @DisplayName("DELETE and DB cleanup")
    class DeleteAndDatabaseChecks {

        @Test
        @DisplayName("DELETE removes entity and all related mappings/rules/types")
        void deleteWorkflowRemovesAllInsertedData() throws Exception {
            postWorkflow(APPLICATION_NAME, loadTestWorkflow())
                    .andExpect(status().isOk());

            WorkflowEntitySetting entitySetting = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(APPLICATION_NAME)
                    .get(0);
            List<WorkflowEntityAndLinkingIdMapping> mappings = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(entitySetting.getId());
            List<String> linkingIds = mappings.stream().map(WorkflowEntityAndLinkingIdMapping::getLinkingId).toList();
            List<WorkflowRuleAndType> ruleAndTypes = workflowRuleAndTypeRepository.findAllByLinkingIdIn(linkingIds);
            List<Long> ruleAndTypeIds = ruleAndTypes.stream().map(WorkflowRuleAndType::getId).toList();
            List<Long> ruleIds = ruleAndTypes.stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            List<Long> typeIds = ruleAndTypes.stream().map(rt -> rt.getWorkflowType().getId()).distinct().toList();

            mockMvc.perform(delete("/api/workflow").param("applicationName", APPLICATION_NAME))
                    .andExpect(status().isOk());

            assertTrue(workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(APPLICATION_NAME).isEmpty(),
                    "Entity setting should be removed");
            assertTrue(workflowEntityAndLinkingIdMappingRepository.findAllByWorkflowEntitySettingId(entitySetting.getId()).isEmpty(),
                    "Entity-linking mappings should be removed");
            for (Long id : ruleAndTypeIds) {
                assertFalse(workflowRuleAndTypeRepository.existsById(id), "Rule-type mapping should be removed: " + id);
            }
            for (Long id : ruleIds) {
                assertFalse(workflowRuleRepository.existsById(id), "Rule should be removed: " + id);
            }
            for (Long id : typeIds) {
                assertFalse(workflowTypeRepository.existsById(id), "Type should be removed: " + id);
            }

            mockMvc.perform(get("/api/workflow").param("applicationName", APPLICATION_NAME))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE is blocked by existing report and does not remove workflow data")
        void deleteWorkflowShouldKeepDataWhenReportExists() throws Exception {
            postWorkflow(LOCKED_APPLICATION_NAME, loadTestWorkflow())
                    .andExpect(status().isOk());

            WorkflowEntitySetting entitySetting = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(LOCKED_APPLICATION_NAME)
                    .get(0);

            workflowReportRepository.saveAndFlush(WorkflowReport.builder()
                    .reportGroup(1L)
                    .enabled(true)
                    .workflowEntitySetting(entitySetting)
                    .cronExpression("0 0 * * * *")
                    .reportTimeRangeByHours(24)
                    .timezone("UTC")
                    .emailTitle("report")
                    .build());

            mockMvc.perform(delete("/api/workflow").param("applicationName", LOCKED_APPLICATION_NAME))
                    .andExpect(status().isConflict());

            assertEquals(1,
                    workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(LOCKED_APPLICATION_NAME).size(),
                    "Entity setting should still exist");
            assertFalse(workflowEntityAndLinkingIdMappingRepository.findAllByWorkflowEntitySettingId(entitySetting.getId()).isEmpty(),
                    "Mappings should remain when delete is rejected");
        }

        @Test
        @DisplayName("DELETE rejects corrupted mapping with blank linkingId and keeps DB unchanged")
        void deleteWorkflowShouldRejectBlankLinkingIdAndKeepData() throws Exception {
            WorkflowEntitySetting entitySetting = workflowEntitySettingRepository.saveAndFlush(
                    WorkflowEntitySetting.builder()
                            .applicationName(CORRUPT_APPLICATION_NAME)
                            .enabled(true)
                            .build()
            );
            workflowEntityAndLinkingIdMappingRepository.saveAndFlush(WorkflowEntityAndLinkingIdMapping.builder()
                    .workflowEntitySetting(entitySetting)
                    .logicOrder(1)
                    .linkingId(" ")
                    .remark("corrupted")
                    .build());

            mockMvc.perform(delete("/api/workflow").param("applicationName", CORRUPT_APPLICATION_NAME))
                    .andExpect(status().isBadRequest());

            assertEquals(1,
                    workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(CORRUPT_APPLICATION_NAME).size(),
                    "Entity setting should remain when delete fails");
            assertEquals(1,
                    workflowEntityAndLinkingIdMappingRepository.findAllByWorkflowEntitySettingId(entitySetting.getId()).size(),
                    "Mapping should remain when delete fails");
        }
    }

    @Nested
    @DisplayName("Auto-copy integration")
    class AutoCopyChecks {

        @Test
        @DisplayName("Auto-copy creates target workflow and keeps source independent")
        void autoCopyCreatesTargetAndSourceRemainsIntact() throws Exception {
            WorkFlow source = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(1, "source-first", "SRC_A", List.of("$.x")),
                            plugin(2, "source-second", "SRC_B", List.of("$.y", "$.z"))
                    ))
                    .uiMapList(List.of(Map.of("id", "copy-edge")))
                    .build();

            postWorkflow(SOURCE_APPLICATION_NAME, source)
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", SOURCE_APPLICATION_NAME)
                            .param("toApplicationName", TARGET_APPLICATION_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());

            WorkflowEntitySetting sourceEntity = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(SOURCE_APPLICATION_NAME)
                    .get(0);
            WorkflowEntitySetting targetEntity = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(TARGET_APPLICATION_NAME)
                    .get(0);
            assertNotEquals(sourceEntity.getId(), targetEntity.getId(), "Target should be a different entity row");

            List<WorkflowEntityAndLinkingIdMapping> sourceMappings = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(sourceEntity.getId());
            List<WorkflowEntityAndLinkingIdMapping> targetMappings = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(targetEntity.getId());

            sourceMappings.sort(Comparator.comparing(WorkflowEntityAndLinkingIdMapping::getLogicOrder));
            targetMappings.sort(Comparator.comparing(WorkflowEntityAndLinkingIdMapping::getLogicOrder));

            assertEquals(sourceMappings.size(), targetMappings.size(), "Target should have same number of plugin mappings");
            for (WorkflowEntityAndLinkingIdMapping targetMapping : targetMappings) {
                assertTrue(targetMapping.getLinkingId().startsWith(targetEntity.getId() + "_"),
                        "Target mapping linkingId should bind to target entity id");
            }

            mockMvc.perform(delete("/api/workflow").param("applicationName", TARGET_APPLICATION_NAME))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/workflow").param("applicationName", SOURCE_APPLICATION_NAME))
                    .andExpect(status().isOk());
            assertEquals(1,
                    workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(SOURCE_APPLICATION_NAME).size(),
                    "Deleting target should not remove source workflow");
        }

        @Test
        @DisplayName("Auto-copy to existing target replaces target old workflow completely")
        void autoCopyToExistingTargetReplacesOldData() throws Exception {
            WorkFlow source = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(1, "source-first", "COPY_SRC_A", List.of("$.s1")),
                            plugin(2, "source-second", "COPY_SRC_B", List.of("$.s2"))
                    ))
                    .uiMapList(List.of(Map.of("id", "source-edge")))
                    .build();
            WorkFlow oldTarget = WorkFlow.builder()
                    .pluginList(List.of(
                            plugin(99, "target-old", "COPY_OLD", List.of("$.old"))
                    ))
                    .uiMapList(List.of(Map.of("id", "target-old-edge")))
                    .build();

            postWorkflow(SOURCE_APPLICATION_NAME, source).andExpect(status().isOk());
            postWorkflow(TARGET_APPLICATION_NAME, oldTarget).andExpect(status().isOk());

            WorkflowEntitySetting targetBefore = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(TARGET_APPLICATION_NAME)
                    .get(0);
            List<WorkflowEntityAndLinkingIdMapping> oldMappings = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(targetBefore.getId());
            List<WorkflowRuleAndType> oldRuleAndTypes = workflowRuleAndTypeRepository.findAllByLinkingIdIn(
                    oldMappings.stream().map(WorkflowEntityAndLinkingIdMapping::getLinkingId).toList()
            );
            List<Long> oldRuleIds = oldRuleAndTypes.stream().map(rt -> rt.getWorkflowRule().getId()).distinct().toList();
            List<Long> oldTypeIds = oldRuleAndTypes.stream().map(rt -> rt.getWorkflowType().getId()).distinct().toList();

            mockMvc.perform(post("/api/workflow/autoCopy")
                            .param("fromApplicationName", SOURCE_APPLICATION_NAME)
                            .param("toApplicationName", TARGET_APPLICATION_NAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());

            WorkflowEntitySetting targetAfter = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(TARGET_APPLICATION_NAME)
                    .get(0);
            assertEquals(targetBefore.getId(), targetAfter.getId(),
                    "Existing target entity row should be reused during auto-copy");

            for (WorkflowEntityAndLinkingIdMapping oldMapping : oldMappings) {
                assertFalse(workflowEntityAndLinkingIdMappingRepository.existsById(oldMapping.getId()),
                        "Old target mapping should be removed: " + oldMapping.getId());
            }
            for (WorkflowRuleAndType oldRuleAndType : oldRuleAndTypes) {
                assertFalse(workflowRuleAndTypeRepository.existsById(oldRuleAndType.getId()),
                        "Old target rule-type mapping should be removed: " + oldRuleAndType.getId());
            }
            for (Long oldRuleId : oldRuleIds) {
                assertFalse(workflowRuleRepository.existsById(oldRuleId),
                        "Old target rule should be removed: " + oldRuleId);
            }
            for (Long oldTypeId : oldTypeIds) {
                assertFalse(workflowTypeRepository.existsById(oldTypeId),
                        "Old target type should be removed: " + oldTypeId);
            }

            WorkFlow copiedTarget = getWorkflow(TARGET_APPLICATION_NAME);
            assertEquals(2, copiedTarget.getPluginList().size(),
                    "Target should now match source workflow plugin count");
            assertEquals(List.of("source-first", "source-second"),
                    copiedTarget.getPluginList().stream().map(Plugin::getDescription).toList());
        }
    }

    @Nested
    @DisplayName("Failure and guard scenarios")
    class FailureAndGuardChecks {

        @Test
        @DisplayName("POST without request body returns 400 and does not create any DB rows")
        void postWithoutBodyShouldNotCreateData() throws Exception {
            mockMvc.perform(post("/api/workflow")
                            .param("applicationName", APPLICATION_NAME)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            assertEquals(0, workflowEntitySettingRepository.count());
            assertEquals(0, workflowEntityAndLinkingIdMappingRepository.count());
            assertEquals(0, workflowRuleAndTypeRepository.count());
            assertEquals(0, workflowRuleRepository.count());
            assertEquals(0, workflowTypeRepository.count());
        }

        @Test
        @DisplayName("DELETE unknown app returns 400 and does not affect other workflow data")
        void deleteUnknownAppShouldNotAffectOtherData() throws Exception {
            postWorkflow(KEEP_APPLICATION_NAME, loadTestWorkflow())
                    .andExpect(status().isOk());

            WorkflowEntitySetting keepEntity = workflowEntitySettingRepository
                    .getWorkflowEntitySettingByApplicationName(KEEP_APPLICATION_NAME)
                    .get(0);
            int mappingCountBefore = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(keepEntity.getId()).size();

            mockMvc.perform(delete("/api/workflow").param("applicationName", "UNKNOWN_APP"))
                    .andExpect(status().isBadRequest());

            assertEquals(1,
                    workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(KEEP_APPLICATION_NAME).size());
            int mappingCountAfter = workflowEntityAndLinkingIdMappingRepository
                    .findAllByWorkflowEntitySettingId(keepEntity.getId()).size();
            assertEquals(mappingCountBefore, mappingCountAfter,
                    "Failed delete should not alter existing application's mappings");
        }

        @Test
        @DisplayName("Endpoints return 400 when duplicate application rows exist")
        void duplicateApplicationRowsShouldBeRejected() throws Exception {
            workflowEntitySettingRepository.saveAndFlush(WorkflowEntitySetting.builder()
                    .applicationName(DUPLICATE_APPLICATION_NAME)
                    .enabled(true)
                    .build());
            workflowEntitySettingRepository.saveAndFlush(WorkflowEntitySetting.builder()
                    .applicationName(DUPLICATE_APPLICATION_NAME)
                    .enabled(false)
                    .build());

            mockMvc.perform(get("/api/workflow").param("applicationName", DUPLICATE_APPLICATION_NAME))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(delete("/api/workflow").param("applicationName", DUPLICATE_APPLICATION_NAME))
                    .andExpect(status().isBadRequest());
            postWorkflow(DUPLICATE_APPLICATION_NAME,
                    WorkFlow.builder().pluginList(List.of(plugin(1, "dup", "DUP", List.of("$.dup")))).build())
                    .andExpect(status().isBadRequest());

            assertEquals(2,
                    workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(DUPLICATE_APPLICATION_NAME).size(),
                    "Duplicate rows should remain unchanged after rejected requests");
        }
    }

    private org.springframework.test.web.servlet.ResultActions postWorkflow(String applicationName, WorkFlow workFlow) throws Exception {
        return mockMvc.perform(post("/api/workflow")
                .param("applicationName", applicationName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(workFlow)));
    }

    private WorkFlow getWorkflow(String applicationName) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/workflow").param("applicationName", applicationName))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), WorkFlow.class);
    }

    private Plugin plugin(int id, String description, String actionType, List<String> ruleKeys) {
        List<WorkflowRule> rules = ruleKeys.stream()
                .map(key -> WorkflowRule.builder().key(key).remark("rule-" + key).build())
                .toList();

        Map<String, Object> uiMap = new LinkedHashMap<>();
        uiMap.put("id", actionType + "_" + id);
        uiMap.put("type", actionType);
        uiMap.put("position", Map.of("x", 100, "y", id * 100));
        uiMap.put("measured", Map.of("width", 160, "height", 38));

        return Plugin.builder()
                .id(id)
                .description(description)
                .ruleList(rules)
                .action(WorkflowType.builder()
                        .provider("provider-" + id)
                        .type(actionType)
                        .remark("remark-" + id)
                        .elseLogic("{\"fallback\":" + id + "}")
                        .httpRequestMethod("POST")
                        .httpRequestUrlWithQueryParameter("https://example.com/" + id)
                        .internalHttpRequestUrlWithQueryParameter("https://internal.example.com/" + id)
                        .httpRequestHeaders("{\"Content-Type\":\"application/json\"}")
                        .httpRequestBody("{\"id\":" + id + "}")
                        .trackingNumberSchemaInHttpResponse("{\"tracking\":" + id + "}")
                        .build())
                .uiMap(uiMap)
                .build();
    }

    private WorkFlow loadTestWorkflow() throws IOException {
        String json = new String(new ClassPathResource("workflow-integration-test-data.json")
                .getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return objectMapper.readValue(json, WorkFlow.class);
    }
}
