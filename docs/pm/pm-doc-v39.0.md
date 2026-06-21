# Product Requirements Document — Import Plugin Type Validation Alignment (v39.0)

**Document version:** 39.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Recognition label:** `TODO-import-plugin-types-match-api-mapper`

---

## 1. Overview

The Import Workflow feature (`ImportWorkflowModal`) currently validates plugin types against a hardcoded list (`FUNCTION_V2`, `FUNCTION_V3`, `HTTP_CALL`, `LOGIC`) that does not match the actual plugin type values stored in the backend (`pluginList[].action.type`). This mismatch causes valid workflows exported from the system to fail validation when re-imported.

This document specifies the correction to align import validation with the actual wire format used by `workflow-ui/src/api/mappers/workFlowMapper.ts` and the backend API.

---

## 2. Problem Statement

**Current behavior:**
- `ImportWorkflowModal.tsx` line 47 defines: `["FUNCTION_V2", "FUNCTION_V3", "HTTP_CALL", "LOGIC"]`
- Actual backend stores: `CONSUMER`, `CONSUMERWITHOUTERROR`, `IFELSE`, `MESSAGE`, `FUNCTION_V2`, `FUNCTION_V3`
- Result: workflows with `CONSUMER`, `CONSUMERWITHOUTERROR`, `IFELSE`, or `MESSAGE` nodes fail import validation with "Invalid plugin type" errors

**Impact:**
- Users cannot round-trip workflows (export → import) if they contain HTTP consumer, conditional logic, or message nodes
- Import feature is effectively broken for most real-world workflows
- No validation coverage for the 6 actual node types in UAT/regression test applications

---

## 3. User Story

**CV-US-54 — Import validation matches actual plugin types**

> **As a** workflow author, **I want** the Import Workflow validator to accept all 6 plugin types that the system actually stores, **so that** I can successfully import workflows I previously exported without false validation errors.

---

## 4. Acceptance Criteria

### CV-AC-54-1 — Validation accepts all 6 wire-format plugin types

The `VALID_PLUGIN_TYPES` constant in `ImportWorkflowModal.tsx` must include all 6 types that `workFlowMapper.ts` recognizes:

- `CONSUMER` (HTTP call without error handling)
- `CONSUMERWITHOUTERROR` (HTTP call with error suppression)
- `IFELSE` (conditional logic)
- `MESSAGE` (message/notification step)
- `FUNCTION_V2` (function execution v2)
- `FUNCTION_V3` (function execution v3)

**Note:** `FUNCTION` (v1) is accepted on **read** for backward compatibility (`backendTypeToPlugin` line 17) but is **not** a valid type for new imports — the system always writes `FUNCTION_V2` or `FUNCTION_V3`.

### CV-AC-54-2 — Validation aligns with workFlowMapper.ts

The validation logic must match the behavior of:
- `backendTypeToPlugin()` (lines 11-20) — what the system reads
- `pluginToBackendType()` (lines 22-39) — what the system writes

Specifically:
- Accept uppercase, no underscores: `CONSUMER`, `CONSUMERWITHOUTERROR`, `IFELSE`, `MESSAGE`
- Accept `FUNCTION_V2` and `FUNCTION_V3` (with underscores)
- Reject `HTTP_CALL` and `LOGIC` (these are UI-only enum names, never stored)

### CV-AC-54-3 — Error messages reference correct types

When validation fails, the error message must show the 6 valid wire-format types, not the old incorrect list.

Example:
```
Invalid plugin type: "HTTP_CALL" (must be one of: CONSUMER, CONSUMERWITHOUTERROR, IFELSE, MESSAGE, FUNCTION_V2, FUNCTION_V3)
```

### CV-AC-54-4 — Test application covers all 6 node types

After the fix, the UAT/regression test application used for import testing must contain **at least one node of each type**:
1. `CONSUMER`
2. `CONSUMERWITHOUTERROR`
3. `IFELSE`
4. `MESSAGE`
5. `FUNCTION_V2`
6. `FUNCTION_V3`

This ensures import/export round-trip coverage for all node types across:
- Save workflow
- Load workflow
- Canvas rendering
- Node editor drawer
- **Import from JSON**
- **Export to JSON** (if/when implemented)

### CV-AC-54-5 — No regression in existing import validation

All other validation rules remain unchanged:
- Required fields (`pluginList`, `uiMapList`)
- Duplicate ID detection
- Edge reference validation
- Rule key validation
- Orphaned node warnings

---

## 5. Out of Scope

- Adding new plugin types beyond the 6 currently supported
- Changing the wire format or mapper logic
- Backend API changes (operation-api already stores these types correctly)
- Exposing an enum or type directory API (optional future enhancement)

---

## 6. Success Metrics

- Zero import validation failures for workflows exported from the system
- 100% node type coverage in UAT test applications
- E2E test suite includes import test cases for all 6 plugin types

---

## 7. Dependencies

- `workflow-ui/src/api/mappers/workFlowMapper.ts` (reference implementation)
- `workflow-ui/src/components/ImportWorkflowModal.tsx` (validation logic to fix)
- Test application with 6-node-type coverage (to be created or updated)

---

*End of PM doc v39.0. Awaiting human approval before proceeding to implementation.*
