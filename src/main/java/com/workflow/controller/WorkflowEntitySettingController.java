package com.workflow.controller;

import com.querydsl.core.types.Predicate;
import com.workflow.dao.repository.WorkflowEntitySetting;
import com.workflow.dao.repository.WorkflowEntitySettingRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/")
@Hidden
@Tag(name = "DB Repository", description = "[Tracking] Workflow Entity Setting")
@Validated
@RequiredArgsConstructor
public class WorkflowEntitySettingController {

    private final WorkflowEntitySettingRepository workflowEntitySettingRepository;

    @Operation(hidden = true)
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/workflow/entity-setting",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<WorkflowEntitySetting> searchWorkflowEntitySetting(
            @QuerydslPredicate(root = WorkflowEntitySetting.class) Predicate predicate,
            Pageable pageable) {
        return workflowEntitySettingRepository.findAll(predicate, pageable);
    }

    @Operation(summary = "Get entity setting by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity setting found"),
            @ApiResponse(responseCode = "404", description = "Entity setting not found")
    })
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/workflow/entity-setting/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WorkflowEntitySetting getWorkflowEntitySetting(
            @PathVariable @Parameter(description = "Entity setting id") Long id) {
        return workflowEntitySettingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity setting not found, id=" + id));
    }

    @Operation(summary = "Create entity setting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity setting created"),
            @ApiResponse(responseCode = "400", description = "Invalid applicationName")
    })
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/workflow/entity-setting",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WorkflowEntitySetting createWorkflowEntitySetting(@RequestBody @Valid WorkflowEntitySetting request) {
        validateApplicationName(request.getApplicationName());
        request.setId(null);
        return workflowEntitySettingRepository.saveAndFlush(request);
    }

    @Operation(summary = "Update entity setting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity setting updated"),
            @ApiResponse(responseCode = "400", description = "Invalid applicationName"),
            @ApiResponse(responseCode = "404", description = "Entity setting not found")
    })
    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/workflow/entity-setting/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WorkflowEntitySetting updateWorkflowEntitySetting(
            @PathVariable @Parameter(description = "Entity setting id") Long id,
            @RequestBody @Valid WorkflowEntitySetting request) {
        validateApplicationName(request.getApplicationName());
        WorkflowEntitySetting existing = workflowEntitySettingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity setting not found, id=" + id));
        applyUpdatableFields(existing, request);
        return workflowEntitySettingRepository.saveAndFlush(existing);
    }

    @Operation(summary = "Delete entity setting by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity setting deleted"),
            @ApiResponse(responseCode = "404", description = "Entity setting not found")
    })
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/workflow/entity-setting/{id}"
    )
    public void deleteWorkflowEntitySetting(@PathVariable @Parameter(description = "Entity setting id") Long id) {
        WorkflowEntitySetting existing = workflowEntitySettingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity setting not found, id=" + id));
        workflowEntitySettingRepository.delete(existing);
    }

    @Operation(summary = "Get entity setting history by applicationName")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Revision history loaded"),
            @ApiResponse(responseCode = "400", description = "applicationName must match exactly one entity setting")
    })
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/workflow/entity-setting/history",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<Revision<Integer, WorkflowEntitySetting>> getWorkflowEntitySettingHistory(
            @RequestParam @NotBlank @Parameter(example = "APP_A", description = "Application identifier") String applicationName,
            Pageable pageable) {
        List<WorkflowEntitySetting> entitySettings =
                workflowEntitySettingRepository.getWorkflowEntitySettingByApplicationName(applicationName);
        if (entitySettings.size() != 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Application name must exist exactly once; found: " + entitySettings.size()
            );
        }
        WorkflowEntitySetting entitySetting = entitySettings.get(0);
        Pageable revisionPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        return workflowEntitySettingRepository.findRevisions(entitySetting.getId(), revisionPageable);
    }

    private void validateApplicationName(String applicationName) {
        if (!StringUtils.hasText(applicationName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "applicationName must not be blank");
        }
    }

    private void applyUpdatableFields(WorkflowEntitySetting target, WorkflowEntitySetting source) {
        target.setApplicationName(source.getApplicationName());
        target.setRetry(source.isRetry());
        target.setTracking(source.isTracking());
        target.setTrackingServiceProviderActionId(source.getTrackingServiceProviderActionId());
        target.setTrackingServiceProviderActionId2(source.getTrackingServiceProviderActionId2());
        target.setEimId(source.getEimId());
        target.setDefaultServiceAccount(source.getDefaultServiceAccount());
        target.setRegion(source.getRegion());
        target.setEnabled(source.isEnabled());
        target.setWorkflow(source.getWorkflow());
    }
}
