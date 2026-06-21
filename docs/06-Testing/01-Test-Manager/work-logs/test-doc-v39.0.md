# Test Document — Import Plugin Type Validation Alignment (v39.0)

**Document version:** 39.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Recognition label:** `TODO-import-plugin-types-match-api-mapper`

---

## 1. Test Scope

This test plan covers the fix to `ImportWorkflowModal` validation logic to accept all 6 actual plugin types stored by the backend, rather than the incorrect UI-only enum names.

**Components under test:**
- `workflow-ui/src/components/ImportWorkflowModal.tsx` (validation logic)
- `workflow-ui/src/api/mappers/workFlowMapper.ts` (reference implementation)

**Test environment:** https://workflow-ui-gamma.vercel.app

---

## 2. Test Cases

### TC-IMPORT-TYPES-01 — Validate CONSUMER type acceptance

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "CONSUMER" }`
2. Observe validation result

**Expected:**
- ✅ Validation passes
- Success message: "Valid workflow — 1 node, 0 edges"
- No error about invalid plugin type

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-02 — Validate CONSUMERWITHOUTERROR type acceptance

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "CONSUMERWITHOUTERROR" }`
2. Observe validation result

**Expected:**
- ✅ Validation passes
- Success message: "Valid workflow — 1 node, 0 edges"
- No error about invalid plugin type

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-03 — Validate IFELSE type acceptance

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "IFELSE" }`
2. Observe validation result

**Expected:**
- ✅ Validation passes
- Success message: "Valid workflow — 1 node, 0 edges"
- No error about invalid plugin type

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-04 — Validate MESSAGE type acceptance

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "MESSAGE" }`
2. Observe validation result

**Expected:**
- ✅ Validation passes
- Success message: "Valid workflow — 1 node, 0 edges"
- No error about invalid plugin type

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-05 — Validate FUNCTION_V2 type acceptance

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "FUNCTION_V2" }`
2. Observe validation result

**Expected:**
- ✅ Validation passes
- Success message: "Valid workflow — 1 node, 0 edges"
- No error about invalid plugin type

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-06 — Validate FUNCTION_V3 type acceptance

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "FUNCTION_V3" }`
2. Observe validation result

**Expected:**
- ✅ Validation passes
- Success message: "Valid workflow — 1 node, 0 edges"
- No error about invalid plugin type

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-07 — Reject HTTP_CALL (UI-only enum name)

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "HTTP_CALL" }`
2. Observe validation result

**Expected:**
- ❌ Validation fails
- Error message: `Invalid plugin type: "HTTP_CALL" (must be one of: CONSUMER, CONSUMERWITHOUTERROR, IFELSE, MESSAGE, FUNCTION_V2, FUNCTION_V3)`
- Apply button disabled

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-08 — Reject LOGIC (UI-only enum name)

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with `"action": { "type": "LOGIC" }`
2. Observe validation result

**Expected:**
- ❌ Validation fails
- Error message: `Invalid plugin type: "LOGIC" (must be one of: CONSUMER, CONSUMERWITHOUTERROR, IFELSE, MESSAGE, FUNCTION_V2, FUNCTION_V3)`
- Apply button disabled

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-09 — Help text shows correct plugin types

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Expand "What belongs here?" section
2. Read the plugin types list in the help text

**Expected:**
- Help text shows: "Plugin types: `CONSUMER`, `CONSUMERWITHOUTERROR`, `IFELSE`, `MESSAGE`, `FUNCTION_V2`, `FUNCTION_V3`"
- Does NOT show `HTTP_CALL` or `LOGIC`

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-10 — Six-node test application round-trip

**Preconditions:** 
- Create test application `test-all-node-types` with 6 nodes (one of each type)
- Save workflow to backend

**Steps:**
1. Open application `test-all-node-types` on canvas
2. Export workflow to JSON (copy from browser DevTools Network tab or use future Export feature)
3. Create new application `test-import-roundtrip`
4. Open Import Workflow modal
5. Paste exported JSON
6. Click "Apply to Canvas"
7. Verify canvas renders all 6 nodes
8. Open each node in drawer and verify configuration

**Expected:**
- Import validation passes with "Valid workflow — 6 nodes, 5 edges"
- Canvas renders 6 nodes in correct positions
- Each node type displays correct icon and label
- Node drawer shows correct configuration for each node
- No validation errors or warnings

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-11 — Mixed-type workflow import

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Paste JSON with 3 nodes: `CONSUMER`, `IFELSE`, `FUNCTION_V2`
2. Observe validation result
3. Click "Apply to Canvas"
4. Verify canvas rendering

**Expected:**
- Validation passes with "Valid workflow — 3 nodes, 2 edges"
- Canvas renders all 3 nodes with correct types
- No validation errors

**Actual:** [QA to fill]

---

### TC-IMPORT-TYPES-12 — No regression in other validation rules

**Preconditions:** Open Import Workflow modal on canvas

**Steps:**
1. Test duplicate ID detection: paste JSON with two nodes having same `id`
2. Test edge reference validation: paste JSON with edge pointing to non-existent node
3. Test required field validation: paste JSON missing `pluginList`
4. Test orphaned node warning: paste JSON with node not connected to any edge

**Expected:**
- Duplicate ID error: "Duplicate plugin IDs: 1"
- Edge reference error: "Edge source 'node-99' does not exist in pluginList"
- Required field error: "Required field missing"
- Orphaned node warning: "Node 1 ('...') has no connections"
- All validation rules work as before

**Actual:** [QA to fill]

---

## 3. Test Data

### Minimal single-node JSON for each type

**CONSUMER:**
```json
{
  "pluginList": [{
    "id": 1,
    "description": "HTTP Consumer",
    "linkingIdOfRuleListAndAction": "rule-1",
    "ruleList": [{ "key": "$.data", "remark": "Data exists" }],
    "action": { "type": "CONSUMER", "provider": "http", "remark": "GET /api/data" },
    "uiMap": { "id": "node-1", "type": "CONSUMER", "position": { "x": 100, "y": 100 } }
  }],
  "uiMapList": []
}
```

**CONSUMERWITHOUTERROR:**
```json
{
  "pluginList": [{
    "id": 1,
    "description": "HTTP Consumer Without Error",
    "linkingIdOfRuleListAndAction": "rule-1",
    "ruleList": [{ "key": "$.retry", "remark": "Retry flag" }],
    "action": { "type": "CONSUMERWITHOUTERROR", "provider": "http", "remark": "POST /api/submit" },
    "uiMap": { "id": "node-1", "type": "CONSUMERWITHOUTERROR", "position": { "x": 100, "y": 100 } }
  }],
  "uiMapList": []
}
```

**IFELSE:**
```json
{
  "pluginList": [{
    "id": 1,
    "description": "Conditional Logic",
    "linkingIdOfRuleListAndAction": "rule-1",
    "ruleList": [{ "key": "$.amount", "remark": "Amount exists" }],
    "action": { "type": "IFELSE", "provider": "logic", "remark": "$.amount > 100" },
    "uiMap": { "id": "node-1", "type": "IFELSE", "position": { "x": 100, "y": 100 } }
  }],
  "uiMapList": []
}
```

**MESSAGE:**
```json
{
  "pluginList": [{
    "id": 1,
    "description": "Send Message",
    "linkingIdOfRuleListAndAction": "rule-1",
    "ruleList": [{ "key": "$.notify", "remark": "Notify flag" }],
    "action": { "type": "MESSAGE", "provider": "notification", "remark": "Send email" },
    "uiMap": { "id": "node-1", "type": "MESSAGE", "position": { "x": 100, "y": 100 } }
  }],
  "uiMapList": []
}
```

**FUNCTION_V2:**
```json
{
  "pluginList": [{
    "id": 1,
    "description": "Function V2",
    "linkingIdOfRuleListAndAction": "rule-1",
    "ruleList": [{ "key": "$.process", "remark": "Process flag" }],
    "action": { "type": "FUNCTION_V2", "provider": "compute", "remark": "Calculate total" },
    "uiMap": { "id": "node-1", "type": "FUNCTION_V2", "position": { "x": 100, "y": 100 } }
  }],
  "uiMapList": []
}
```

**FUNCTION_V3:**
```json
{
  "pluginList": [{
    "id": 1,
    "description": "Function V3",
    "linkingIdOfRuleListAndAction": "rule-1",
    "ruleList": [{ "key": "$.finalize", "remark": "Finalize flag" }],
    "action": { "type": "FUNCTION_V3", "provider": "compute", "remark": "Finalize result" },
    "uiMap": { "id": "node-1", "type": "FUNCTION_V3", "position": { "x": 100, "y": 100 } }
  }],
  "uiMapList": []
}
```

---

## 4. Regression Test Coverage

All existing import test cases (TC-IMPORT-01 through TC-IMPORT-08 from v38.0) must continue to pass:
- JSON parse error handling
- Markdown code fence stripping
- Required field validation
- Duplicate ID detection
- Edge reference validation
- Orphaned node warnings
- Large workflow warnings
- Confirmation prompt for non-empty canvas

---

## 5. Test Execution Summary

**Total test cases:** 12  
**Passed:** [QA to fill]  
**Failed:** [QA to fill]  
**Blocked:** [QA to fill]  

**Critical path:** TC-IMPORT-TYPES-01 through TC-IMPORT-TYPES-06 (all 6 types must pass)

**Blocker criteria:** Any of the 6 valid plugin types rejected by validation

---

*End of Test doc v39.0. Ready for QA execution after implementation.*
