# Product Requirements Document v40.0

**Document version:** 40.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-validation-relax-uiMapList-ifelse-edges-vs-pluginList`

## Overview

UAT environment shows validation failures when workflows contain IFELSE branching logic. The validator incorrectly requires every edge endpoint (source/target) in `uiMapList` to have a corresponding entry in `pluginList`, but IFELSE branch edges reference virtual branch identifiers (e.g., `IFELSE_1_TRUE`, `IFELSE_1_FALSE`) that are not plugin nodes.

## Problem Statement

**Current behavior:**
- User imports or creates a workflow with IFELSE conditional branching
- Validation runs and checks every `uiMapList[n].source` and `uiMapList[n].target`
- For IFELSE branch edges, validation fails with: "IFELSE_1_TRUE does not exist in pluginList"
- Entire workflow marked as "Validation failed"
- User cannot proceed with valid branching workflows

**Root cause:**
- IFELSE nodes create virtual branch points (TRUE/FALSE paths)
- These branch identifiers appear in `uiMapList` edges but not as standalone `pluginList` entries
- Validator treats them as missing nodes instead of recognizing them as branch references

## User Stories

### US-VAL-01: Import workflow with IFELSE branching
**As a** workflow designer  
**I want to** import workflows containing IFELSE conditional logic  
**So that** I can use branching decision flows without validation errors

**Acceptance Criteria:**
- AC-VAL-01.1: Workflow with IFELSE node and TRUE/FALSE branch edges passes validation
- AC-VAL-01.2: Edge with `source: "IFELSE_1_TRUE"` does not require `IFELSE_1_TRUE` in `pluginList`
- AC-VAL-01.3: Edge with `target: "IFELSE_1_FALSE"` does not require `IFELSE_1_FALSE` in `pluginList`
- AC-VAL-01.4: Validation still fails for genuinely missing node references (non-IFELSE cases)

### US-VAL-02: Create workflow with multiple IFELSE branches
**As a** workflow designer  
**I want to** create workflows with multiple IFELSE nodes and nested branches  
**So that** I can build complex decision trees

**Acceptance Criteria:**
- AC-VAL-02.1: Multiple IFELSE nodes (IFELSE_1, IFELSE_2, etc.) each with TRUE/FALSE branches validate correctly
- AC-VAL-02.2: Nested IFELSE branches (IFELSE inside TRUE path of another IFELSE) validate correctly
- AC-VAL-02.3: Validation error messages clearly distinguish between missing nodes and valid branch references

### US-VAL-03: Validation error clarity
**As a** workflow designer  
**I want** clear validation error messages  
**So that** I can quickly identify and fix real issues

**Acceptance Criteria:**
- AC-VAL-03.1: Error message for missing node: "Node 'HTTP_CALL_5' referenced in edge but not found in pluginList"
- AC-VAL-03.2: No error for valid IFELSE branch references
- AC-VAL-03.3: Validation summary shows count of real errors (excludes IFELSE branch references)

## Scope

**In scope:**
- Relax validation rules for `uiMapList` edges where source/target matches IFELSE branch pattern
- Recognize branch identifier patterns: `{nodeId}_TRUE`, `{nodeId}_FALSE` where `{nodeId}` is an IFELSE node
- Maintain strict validation for non-IFELSE node references
- Update validation error messages for clarity

**Out of scope:**
- Changes to IFELSE node execution logic (online-api)
- Changes to canvas rendering of IFELSE branches (workflow-ui visual layer)
- Backend API contract changes (operation-api)

## Success Metrics

- Zero validation failures for valid IFELSE workflows in UAT
- Validation still catches genuinely missing node references (no false negatives)
- User can import and save workflows with IFELSE branching without errors

## Dependencies

- Requires understanding of current validation logic in `workflow-ui`
- May require coordination with mapper logic if validation is shared

## Risks

- **Risk:** Relaxing validation too much could allow invalid workflows through
  - **Mitigation:** Only relax for specific IFELSE branch pattern; maintain strict validation for all other cases
  
- **Risk:** IFELSE branch naming convention might change in future
  - **Mitigation:** Use pattern matching (regex) that can be easily updated; document the convention

## Open Questions

1. Are there other node types besides IFELSE that use virtual branch identifiers?
2. Should validation also check that the parent IFELSE node exists when validating branch references?
3. What is the exact naming pattern for IFELSE branches? (Assumed: `{nodeId}_TRUE` and `{nodeId}_FALSE`)
