# PM Doc v21.0 — Application Name: Description Field (DB + API + UI)

**Status:** Awaiting approval
**Label:** `TODO-application-name-description-db-text-ui-api`
**Date:** 2026-04-08

---

## Problem Statement

Every application should have a human-readable description of what it does. The `WorkflowEntitySetting` DB table has no `description` column, so the UI cannot display or store one. The "Description" column in the Applications table currently shows secondary metadata (`eimId` / `defaultServiceAccount` / `region`) as a placeholder.

## User Story

As a **product owner / operations team member**,
I want each application to have a **description** (free text, up to ~2 000 chars),
So that team members know what a workflow does without opening it.

## Acceptance Criteria

1. **DB:** A `description` column of type `TEXT` (nullable) is added to the `workflow_entity_setting` table via a Flyway (or Liquibase) migration in `workflow-operation-api`.
2. **API:** The `WorkflowEntitySetting` entity and its list/detail response DTO include `description`. The existing `PATCH /api/workflow/entity-setting` endpoint accepts `description` in its patch request body and persists it.
3. **UI (`workflow-ui`):** The "Description" column in the Applications table shows the `description` field value (or `—` if empty). The Settings modal (or Rename/Edit flow from v20.0) includes an editable `description` textarea.
4. **Optional check:** Verify whether `workflow-online-api` reads application metadata; if so, confirm `description` flows through without breaking existing contracts.
5. No breaking change to existing API consumers (field is optional / nullable).
