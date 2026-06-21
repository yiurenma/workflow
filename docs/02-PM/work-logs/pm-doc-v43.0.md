# Product Requirements — Import Validation Relaxation v43.0

**Document Version:** 43.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-import-validation-remove-edge-reference-check`

---

## 1. Background

Current import validation rejects workflows where `uiMapList` edges reference node IDs that don't exist in `pluginList`. This blocks legitimate use cases:

- **String IDs:** Users export workflows with string IDs (e.g., `"HTTP_1"`, `"IFELSE_2"`) but validation expects numeric IDs
- **External workflows:** Workflows from other systems may use different ID schemes
- **Partial imports:** Users want to import edge topology separately from node definitions

The validation was added to catch data integrity issues, but it's too strict for real-world import scenarios.

---

## 2. User Story

**US-IMPORT-43-01** — Import workflow with arbitrary edge references

> **As** an integration engineer,  
> **I want** to import workflows without strict edge-to-node validation,  
> **So that** I can import workflows with string IDs, external formats, or partial topologies.

---

## 3. Acceptance Criteria

**AC-IMPORT-43-01** — Edge reference validation removed

- `ImportWorkflowModal` validation no longer checks if `uiMapList[].source` exists in `pluginList`
- `ImportWorkflowModal` validation no longer checks if `uiMapList[].target` exists in `pluginList`
- Workflows with non-existent edge references pass validation
- Other validations (plugin types, duplicate IDs, required fields) remain unchanged

**AC-IMPORT-43-02** — Test coverage

- Test case: import workflow with string node IDs (`"HTTP_1"`, `"IFELSE_2"`) and matching string edge references → **PASS**
- Test case: import workflow with numeric node IDs but string edge references → **PASS**
- Test case: import workflow with edges referencing non-existent nodes → **PASS**

---

## 4. Out of Scope

- Backend validation changes (backend already accepts arbitrary edge references)
- Canvas rendering behavior when edges reference missing nodes (existing behavior preserved)
- Migration of existing workflows (no data migration needed)

---

## 5. Success Metrics

- Zero validation errors for workflows with non-matching edge references
- No regression in other validation checks (plugin types, duplicate IDs)
