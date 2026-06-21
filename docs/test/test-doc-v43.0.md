# Test Document — Import Validation Relaxation v43.0

**Document Version:** 43.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-import-validation-remove-edge-reference-check`

---

## 1. Test Scope

**Feature:** Remove validation check requiring `uiMapList` edge references to exist in `pluginList`

**Components:**
- `workflow-ui/src/components/ImportWorkflowModal.tsx` validation logic

**Test Levels:**
- E2E tests (Playwright against UAT environment)

---

## 2. Test Cases

### TC-IMPORT-EDGE-01 — Import workflow with string node IDs and matching string edge references

**Objective:** Verify validation passes when both nodes and edges use string IDs

**Preconditions:**
- Navigate to https://workflow-ui-gamma.vercel.app
- Open any workflow canvas

**Test Data:**
```json
{
  "pluginList": [
    { "id": "HTTP_1", "description": "Fetch data", "action": { "type": "CONSUMER", "provider": "http" } },
    { "id": "MSG_1", "description": "Send message", "action": { "type": "MESSAGE", "provider": "email" } }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "MSG_1" }
  ]
}
```

**Steps:**
1. Click Import button in canvas toolbar
2. Paste test data into textarea
3. Observe validation result

**Expected Result:**
- ✅ Validation passes with "Valid workflow — 2 nodes, 1 edge"
- Apply button enabled

---

### TC-IMPORT-EDGE-02 — Import workflow with numeric node IDs but string edge references

**Objective:** Verify validation passes when node IDs are numeric but edges reference them as strings

**Test Data:**
```json
{
  "pluginList": [
    { "id": 1, "description": "Fetch data", "action": { "type": "CONSUMER", "provider": "http" } },
    { "id": 2, "description": "Send message", "action": { "type": "MESSAGE", "provider": "email" } }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "MSG_1" }
  ]
}
```

**Steps:**
1. Click Import button
2. Paste test data
3. Observe validation result

**Expected Result:**
- ✅ Validation passes
- Apply button enabled

---

### TC-IMPORT-EDGE-03 — Import workflow with edges referencing non-existent nodes

**Objective:** Verify validation passes when edges reference nodes not in pluginList

**Test Data:**
```json
{
  "pluginList": [
    { "id": 1, "description": "Fetch data", "action": { "type": "CONSUMER", "provider": "http" } }
  ],
  "uiMapList": [
    { "id": "e1", "source": "1", "target": "999" },
    { "id": "e2", "source": "NONEXISTENT", "target": "1" }
  ]
}
```

**Steps:**
1. Click Import button
2. Paste test data
3. Observe validation result

**Expected Result:**
- ✅ Validation passes with "Valid workflow — 1 node, 2 edges"
- ⚠️ Warning: "Node 1 has no connections" (orphaned node warning still works)
- Apply button enabled

---

### TC-IMPORT-EDGE-04 — Verify other validations still work (invalid plugin type)

**Objective:** Verify removing edge validation doesn't break other validation checks

**Test Data:**
```json
{
  "pluginList": [
    { "id": 1, "description": "Invalid", "action": { "type": "INVALID_TYPE" } }
  ],
  "uiMapList": []
}
```

**Steps:**
1. Click Import button
2. Paste test data
3. Observe validation result

**Expected Result:**
- ❌ Validation fails with error: `pluginList[0].action.type: Invalid plugin type: "INVALID_TYPE" (must be one of: CONSUMER, CONSUMERWITHOUTERROR, IFELSE, MESSAGE, FUNCTION_V2, FUNCTION_V3)`
- Apply button disabled

---

### TC-IMPORT-EDGE-05 — Verify duplicate ID validation still works

**Objective:** Verify duplicate ID check remains functional

**Test Data:**
```json
{
  "pluginList": [
    { "id": 1, "description": "First", "action": { "type": "CONSUMER" } },
    { "id": 1, "description": "Duplicate", "action": { "type": "MESSAGE" } }
  ],
  "uiMapList": []
}
```

**Steps:**
1. Click Import button
2. Paste test data
3. Observe validation result

**Expected Result:**
- ❌ Validation fails with error: `pluginList: Duplicate plugin IDs: 1`
- Apply button disabled

---

### TC-IMPORT-EDGE-06 — Verify required field validation still works

**Objective:** Verify required field check remains functional

**Test Data:**
```json
{
  "pluginList": [
    { "description": "Missing ID", "action": { "type": "CONSUMER" } }
  ],
  "uiMapList": []
}
```

**Steps:**
1. Click Import button
2. Paste test data
3. Observe validation result

**Expected Result:**
- ❌ Validation fails with error: `pluginList[0].id: Plugin ID required`
- Apply button disabled

---

## 3. Test Environment

**UAT Environment:**
- Frontend: https://workflow-ui-gamma.vercel.app
- Browser: Desktop Chrome 1280px + Mobile Chrome 390×844

---

## 4. Pass Criteria

**All 6 test cases must PASS:**
- TC-IMPORT-EDGE-01: PASS
- TC-IMPORT-EDGE-02: PASS
- TC-IMPORT-EDGE-03: PASS
- TC-IMPORT-EDGE-04: PASS (validation fails correctly)
- TC-IMPORT-EDGE-05: PASS (validation fails correctly)
- TC-IMPORT-EDGE-06: PASS (validation fails correctly)

**Zero regressions** in existing import validation tests.

---

## 5. Test Execution

**QA Tester:** Manual testing via browser
**E2E Tester:** Playwright automated tests against UAT

---

*End of Test Document v43.0*
