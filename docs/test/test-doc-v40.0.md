# Test Document v40.0

**Document version:** 40.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-validation-relax-uiMapList-ifelse-edges-vs-pluginList`

## Test Scope

Validate that workflow import/validation logic correctly handles IFELSE branch edges without false-positive errors.

## Test Environment

- **Frontend:** https://workflow-ui-gamma.vercel.app
- **Local dev:** http://localhost:5173
- **Test data:** Workflows with IFELSE nodes and branch edges

## Test Cases

### TC-VAL-IFELSE-01: Single IFELSE with TRUE/FALSE branches

**Objective:** Verify workflow with one IFELSE node and both branch edges validates successfully

**Preconditions:** Canvas is empty or has existing workflow

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER", "provider": "http" }
    },
    {
      "id": "IFELSE_1",
      "description": "Check amount",
      "ruleList": [{ "key": "$.data.amount", "remark": "Amount > 100" }],
      "action": { "type": "IFELSE", "logic": { "condition": "$.data.amount > 100" } }
    },
    {
      "id": "MSG_1",
      "description": "High amount message",
      "action": { "type": "MESSAGE", "provider": "email" }
    },
    {
      "id": "MSG_2",
      "description": "Low amount message",
      "action": { "type": "MESSAGE", "provider": "sms" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "IFELSE_1" },
    { "id": "e2", "source": "IFELSE_1_true", "target": "MSG_1" },
    { "id": "e3", "source": "IFELSE_1_false", "target": "MSG_2" }
  ]
}
```

**Steps:**
1. Navigate to canvas page
2. Click Import button
3. Paste test data JSON
4. Observe validation result

**Expected Result:**
- ✅ Validation passes
- Success message shows: "Valid workflow — 4 nodes, 3 edges"
- No errors about IFELSE_1_true or IFELSE_1_false not found
- Apply button is enabled

**Actual Result:** [To be filled by QA]

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-02: Multiple IFELSE nodes

**Objective:** Verify workflow with multiple IFELSE nodes validates correctly

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER" }
    },
    {
      "id": "IFELSE_1",
      "description": "Check amount",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "IFELSE_2",
      "description": "Check status",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "MSG_1",
      "description": "Message 1",
      "action": { "type": "MESSAGE" }
    },
    {
      "id": "MSG_2",
      "description": "Message 2",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "IFELSE_1" },
    { "id": "e2", "source": "IFELSE_1_true", "target": "IFELSE_2" },
    { "id": "e3", "source": "IFELSE_1_false", "target": "MSG_2" },
    { "id": "e4", "source": "IFELSE_2_true", "target": "MSG_1" },
    { "id": "e5", "source": "IFELSE_2_false", "target": "MSG_2" }
  ]
}
```

**Expected Result:**
- ✅ Validation passes
- All IFELSE branch edges (IFELSE_1_true, IFELSE_1_false, IFELSE_2_true, IFELSE_2_false) accepted
- Success message shows: "Valid workflow — 5 nodes, 5 edges"

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-03: Case-insensitive branch suffixes

**Objective:** Verify both uppercase and lowercase branch suffixes are accepted

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "IFELSE_1",
      "description": "Check condition",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "MSG_1",
      "description": "Message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "IFELSE_1_TRUE", "target": "MSG_1" },
    { "id": "e2", "source": "IFELSE_1_false", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ✅ Validation passes
- Both `IFELSE_1_TRUE` (uppercase) and `IFELSE_1_false` (lowercase) accepted

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-04: Orphan IFELSE branch (parent missing)

**Objective:** Verify validation fails when IFELSE branch references non-existent parent

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "MSG_1",
      "description": "Message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "IFELSE_1_true", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ❌ Validation fails
- Error message: "Edge source 'IFELSE_1_true' does not exist in pluginList" OR "IFELSE branch 'IFELSE_1_true' references missing parent node 'IFELSE_1'"
- Apply button is disabled

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-05: Branch reference to non-IFELSE node

**Objective:** Verify validation fails when branch suffix is used on non-IFELSE node

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER" }
    },
    {
      "id": "MSG_1",
      "description": "Message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1_true", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ❌ Validation fails
- Error message: "Edge source 'HTTP_1_true' does not exist in pluginList"
- Apply button is disabled

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-06: Invalid branch suffix

**Objective:** Verify validation fails for invalid branch suffixes (not true/false)

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "IFELSE_1",
      "description": "Check condition",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "MSG_1",
      "description": "Message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "IFELSE_1_maybe", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ❌ Validation fails
- Error message: "Edge source 'IFELSE_1_maybe' does not exist in pluginList"
- Apply button is disabled

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-07: Nested IFELSE (IFELSE inside branch)

**Objective:** Verify nested IFELSE workflows validate correctly

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER" }
    },
    {
      "id": "IFELSE_1",
      "description": "First check",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "IFELSE_2",
      "description": "Second check (nested)",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "MSG_1",
      "description": "Message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "IFELSE_1" },
    { "id": "e2", "source": "IFELSE_1_true", "target": "IFELSE_2" },
    { "id": "e3", "source": "IFELSE_2_true", "target": "MSG_1" },
    { "id": "e4", "source": "IFELSE_2_false", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ✅ Validation passes
- All nested IFELSE branch edges accepted
- Success message shows: "Valid workflow — 4 nodes, 4 edges"

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-08: IFELSE with only one branch

**Objective:** Verify IFELSE with only TRUE or FALSE branch (not both) validates correctly

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER" }
    },
    {
      "id": "IFELSE_1",
      "description": "Check condition",
      "action": { "type": "IFELSE" }
    },
    {
      "id": "MSG_1",
      "description": "Message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "IFELSE_1" },
    { "id": "e2", "source": "IFELSE_1_true", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ✅ Validation passes (one branch is valid — "do X if condition, otherwise do nothing")
- Warning may appear: "Node IFELSE_1 has no connections" (acceptable)
- Success message shows: "Valid workflow — 3 nodes, 2 edges"

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-09: Non-IFELSE node still requires pluginList entry

**Objective:** Verify validation still catches genuinely missing nodes (no false negatives)

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "MSG_999" }
  ]
}
```

**Expected Result:**
- ❌ Validation fails
- Error message: "Edge target 'MSG_999' does not exist in pluginList"
- Apply button is disabled

**Status:** [PASS/FAIL]

---

### TC-VAL-IFELSE-10: Real UAT workflow with IFELSE

**Objective:** Verify real production workflow with IFELSE validates successfully

**Preconditions:** Obtain a real workflow JSON from UAT that previously failed validation

**Test Data:** [Use actual UAT workflow that triggered the bug report]

**Steps:**
1. Export workflow from UAT that contains IFELSE nodes
2. Navigate to local dev or UAT canvas
3. Click Import button
4. Paste exported workflow JSON
5. Observe validation result

**Expected Result:**
- ✅ Validation passes
- No errors about IFELSE branch IDs
- Workflow can be applied to canvas
- Canvas renders correctly with IFELSE nodes and branches

**Status:** [PASS/FAIL]

---

## Regression Test Cases

### TC-VAL-REG-01: Import workflow without IFELSE (existing functionality)

**Objective:** Verify non-IFELSE workflows still validate correctly (no regression)

**Test Data:**
```json
{
  "pluginList": [
    {
      "id": "HTTP_1",
      "description": "Fetch data",
      "action": { "type": "CONSUMER" }
    },
    {
      "id": "FUNC_1",
      "description": "Process",
      "action": { "type": "FUNCTION_V2" }
    },
    {
      "id": "MSG_1",
      "description": "Send message",
      "action": { "type": "MESSAGE" }
    }
  ],
  "uiMapList": [
    { "id": "e1", "source": "HTTP_1", "target": "FUNC_1" },
    { "id": "e2", "source": "FUNC_1", "target": "MSG_1" }
  ]
}
```

**Expected Result:**
- ✅ Validation passes
- Success message shows: "Valid workflow — 3 nodes, 2 edges"

**Status:** [PASS/FAIL]

---

### TC-VAL-REG-02: All existing validation rules still apply

**Objective:** Verify existing validation rules (duplicate IDs, missing fields, invalid types) still work

**Test Data:** Use test cases from `test-doc-v39.0.md` TC-IMPORT-TYPES-01 through TC-IMPORT-TYPES-09

**Expected Result:**
- All existing validation test cases pass
- No regression in validation behavior

**Status:** [PASS/FAIL]

---

## E2E Test Cases (Playwright)

### TC-E2E-IFELSE-01: Import and render IFELSE workflow

**Objective:** End-to-end test of importing IFELSE workflow and rendering on canvas

**Environment:** https://workflow-ui-gamma.vercel.app

**Steps:**
1. Navigate to canvas page
2. Click Import button (desktop toolbar or mobile overflow menu)
3. Paste TC-VAL-IFELSE-01 test data
4. Click "Apply to Canvas"
5. Verify canvas renders correctly

**Expected Result:**
- Import modal opens
- Validation passes
- Canvas shows 4 nodes (HTTP_1, IFELSE_1, MSG_1, MSG_2)
- Canvas shows 3 edges connecting nodes
- IFELSE node has visual indication of branching (if applicable)

**Playwright Assertions:**
```typescript
// Layer 1: Exist
await expect(page.locator('[data-id="HTTP_1"]')).toBeVisible();
await expect(page.locator('[data-id="IFELSE_1"]')).toBeVisible();
await expect(page.locator('[data-id="MSG_1"]')).toBeVisible();
await expect(page.locator('[data-id="MSG_2"]')).toBeVisible();

// Layer 4: Interact
await page.getByRole('button', { name: /import/i }).click();
await page.locator('textarea').fill(testData);
await expect(page.getByText(/valid workflow/i)).toBeVisible();
await page.getByRole('button', { name: /apply/i }).click();

// Layer 5: Effect
const nodes = await page.locator('.react-flow__node').count();
expect(nodes).toBe(4);
```

**Status:** [PASS/FAIL]

---

## Test Execution Plan

### Phase 1: Unit Tests (Developer)
- Implement unit tests for `isIFELSEBranchId()` helper function
- Implement unit tests for updated `validateWorkflow()` logic
- Run: `npm test -- workflowValidation.test.ts`
- **Exit Criteria:** All unit tests pass

### Phase 2: Manual QA (QA Engineer)
- Execute TC-VAL-IFELSE-01 through TC-VAL-IFELSE-10 on local dev
- Execute TC-VAL-REG-01 and TC-VAL-REG-02 on local dev
- Document actual results and status
- **Exit Criteria:** All test cases pass

### Phase 3: E2E Tests (E2E Tester)
- Implement TC-E2E-IFELSE-01 in Playwright
- Run full E2E suite on UAT environment
- **Exit Criteria:** TC-E2E-IFELSE-01 passes, zero regressions in existing tests

### Phase 4: UAT (Test Manager + Human)
- Execute TC-VAL-IFELSE-10 with real UAT workflow
- Verify no validation errors on previously failing workflows
- **Exit Criteria:** Real UAT workflows with IFELSE validate successfully

## Success Criteria

- ✅ All 10 validation test cases pass
- ✅ All 2 regression test cases pass
- ✅ E2E test passes on UAT environment
- ✅ Real UAT workflow with IFELSE validates without errors
- ✅ Zero regressions in existing E2E test suite

## Test Data Repository

Store test data files in: `workflow-ui/tests/fixtures/ifelse-workflows/`

Files:
- `single-ifelse.json` (TC-VAL-IFELSE-01)
- `multiple-ifelse.json` (TC-VAL-IFELSE-02)
- `nested-ifelse.json` (TC-VAL-IFELSE-07)
- `orphan-branch.json` (TC-VAL-IFELSE-04)
- `invalid-branch.json` (TC-VAL-IFELSE-06)
