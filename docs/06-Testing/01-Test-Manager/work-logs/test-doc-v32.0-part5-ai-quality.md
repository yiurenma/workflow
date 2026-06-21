# Test Cases — Workflow Studio Unified Program Part 5: AI Quality (v32.0)

**Document version:** 32.0 Part 5  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** AI workflow generator semantic quality improvements  
**Repository:** `workflow-ui`

---

## 1. Test Strategy

### 1.1 Test Approach

This is primarily a **measurement and iteration** task, not a traditional feature test. The goal is to:
1. Document current failure patterns
2. Improve system prompt
3. Add validation layer
4. Measure improvement

### 1.2 Test Environments

- **UAT:** https://workflow-ui-gamma.vercel.app
- **Test application:** `E2E_TEST_CANVAS`
- **AI providers:** GitHub Models (primary), Anthropic API (fallback)

---

## 2. Measurement Loop Test Cases

### TC-AI-MEASURE-01 — Baseline failure pattern documentation

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Reference workflow exists with known-good configuration

**Steps:**
1. Click **Explain** button
2. Copy AI-generated description
3. Click **Generate** button
4. Paste description into input
5. Click "Generate Workflow"
6. Compare generated workflow to reference:
   - Open each node in drawer
   - Inspect plugin IDs, action kinds, rule keys, rule types
   - Check edges point to valid nodes
7. Document failures:
   - Screenshot of generated canvas
   - JSON diff (reference vs generated)
   - Categorize error type (A/B/C/D/E)

**Expected:**
- Failure patterns documented in `docs/ai-generator-failure-patterns-v32.0.md`
- At least 5 distinct failure examples captured
- Each failure categorized:
  - **Category A:** Invalid plugin IDs
  - **Category B:** Incorrect action kinds
  - **Category C:** Invalid rule keys (JSONPath)
  - **Category D:** Wrong rule types
  - **Category E:** Broken edges

---

### TC-AI-MEASURE-02 — Repeat measurement with different workflows

**Preconditions:**
- Create 3-5 different reference workflows with varying complexity

**Steps:**
1. For each reference workflow:
   - Run Explain → Generate cycle
   - Document failures
2. Aggregate failure patterns across all workflows

**Expected:**
- Failure patterns consistent across workflows
- Common error types identified
- Severity ranking established (which errors most frequent/impactful)

---

## 3. System Prompt Improvement Test Cases

### TC-AI-PROMPT-01 — Enhanced prompt with canonical examples

**Preconditions:**
- Baseline failure patterns documented
- New system prompt drafted with:
  - Valid plugin ID list
  - Valid action kind examples
  - JSONPath syntax rules
  - Rule type examples

**Steps:**
1. Update `WORKFLOW_GENERATOR_SYSTEM_PROMPT` in `constants/prompts.ts`
2. Run measurement loop (TC-AI-MEASURE-01) again
3. Compare failure rate before/after

**Expected:**
- Failure rate reduced by at least 30%
- Category A errors (invalid plugin IDs) reduced significantly
- Category C errors (invalid JSONPath) reduced significantly

---

### TC-AI-PROMPT-02 — Prompt includes enumerated allowed shapes

**Preconditions:**
- System prompt updated with:
  - Complete list of valid plugin IDs
  - Complete list of valid action kinds per plugin
  - Complete list of valid rule types

**Steps:**
1. Generate workflow with prompt: "Create a workflow that fetches user data and sends it to Slack"
2. Inspect generated JSON
3. Verify all plugin IDs, action kinds, rule types are valid

**Expected:**
- No Category A errors (invalid plugin IDs)
- No Category B errors (incorrect action kinds)
- No Category D errors (wrong rule types)

---

## 4. Post-Parse Validation Test Cases

### TC-AI-VALIDATE-01 — Validation catches invalid plugin IDs

**Preconditions:**
- Validation layer implemented in `WorkflowGeneratorModal.tsx`

**Steps:**
1. Generate workflow (may produce invalid plugin ID)
2. Validation runs on generated JSON
3. Check validation result

**Expected:**
- If generated JSON has invalid plugin ID:
  - Validation error shown to user
  - Error message: "Invalid plugin ID: {id}. Valid IDs: http_fetch, safe_http_fetch, logic"
  - User can regenerate or manually fix

---

### TC-AI-VALIDATE-02 — Validation catches invalid JSONPath

**Preconditions:**
- Validation layer checks rule keys against JSONPath syntax

**Steps:**
1. Generate workflow with rules
2. Validation runs on rule keys
3. Check validation result

**Expected:**
- If rule key missing `$` prefix or has comma-separated paths:
  - Validation error shown
  - Error message: "Invalid JSONPath in rule key: {key}"
  - User can regenerate or manually fix

---

### TC-AI-VALIDATE-03 — Validation catches broken edges

**Preconditions:**
- Validation layer checks edge source/target against node IDs

**Steps:**
1. Generate workflow with edges
2. Validation runs on edges array
3. Check validation result

**Expected:**
- If edge points to non-existent node:
  - Validation error shown
  - Error message: "Edge references non-existent node: {nodeId}"
  - User can regenerate or manually fix

---

### TC-AI-VALIDATE-04 — Valid workflow passes validation

**Preconditions:**
- Generate workflow with all valid fields

**Steps:**
1. Generate workflow
2. Validation runs
3. Check result

**Expected:**
- No validation errors
- Workflow loaded onto canvas successfully
- Success toast: "Workflow generated successfully"

---

## 5. User-Visible Repair Test Cases

### TC-AI-REPAIR-01 — User sees validation errors before load

**Preconditions:**
- Generate workflow with validation errors

**Steps:**
1. Click "Generate Workflow"
2. Validation runs
3. Observe UI

**Expected:**
- Modal shows validation errors in a list:
  - "❌ Invalid plugin ID: http_call (should be: http_fetch)"
  - "❌ Invalid JSONPath: data.userId (missing $ prefix)"
  - "❌ Edge references non-existent node: node99"
- Buttons: "Regenerate" (try again), "Load Anyway" (skip validation), "Cancel"

---

### TC-AI-REPAIR-02 — User can regenerate after validation failure

**Preconditions:**
- Validation errors shown

**Steps:**
1. Click "Regenerate" button
2. New generation request sent to AI
3. New JSON returned and validated

**Expected:**
- New workflow generated
- If still has errors: show errors again
- If valid: load onto canvas

---

### TC-AI-REPAIR-03 — User can load anyway (skip validation)

**Preconditions:**
- Validation errors shown

**Steps:**
1. Click "Load Anyway" button

**Expected:**
- Workflow loaded onto canvas despite validation errors
- Warning toast: "Workflow loaded with validation errors. Some nodes may not work correctly."
- User can manually fix in node editor

---

## 6. Playwright Extension Test Cases (Optional)

### TC-AI-E2E-01 — Extend TC-GENERATOR-01 with structure validation

**Preconditions:**
- Existing TC-GENERATOR-01 tests modal open/close

**Steps:**
1. Open workflow canvas
2. Click "Generate" button
3. Enter description: "Fetch user data from API"
4. Click "Generate Workflow"
5. Wait for generation
6. Validate generated workflow structure:
   - At least 1 node created
   - Node has valid plugin ID
   - Node has valid action kind

**Expected:**
- Workflow generated
- Structure validation passes
- No validation errors shown

---

### TC-AI-E2E-02 — Test validation error display

**Preconditions:**
- Mock AI response with invalid plugin ID

**Steps:**
1. Open workflow canvas
2. Click "Generate" button
3. Enter description (triggers mocked invalid response)
4. Click "Generate Workflow"
5. Check for validation error UI

**Expected:**
- Validation error modal shown
- Error message visible
- "Regenerate" button present

---

## 7. Regression Tests

### TC-AI-REG-01 — Existing generator tests still pass

**Preconditions:**
- Run existing Playwright tests

**Steps:**
1. Run TC-GENERATOR-01 (modal open/close)
2. Run TC-GENERATOR-02 (basic generation)

**Expected:**
- All existing tests pass
- No regressions in modal behavior

---

### TC-AI-REG-02 — Explain feature still works

**Preconditions:**
- Open workflow canvas with existing workflow

**Steps:**
1. Click "Explain" button
2. Verify explanation generated

**Expected:**
- Explanation modal opens
- AI-generated description shown
- No errors

---

## 8. Success Metrics

### Quantitative Metrics

- **Baseline failure rate:** Document % of generations with semantic errors
- **Post-improvement failure rate:** Target <30% of baseline
- **Validation catch rate:** % of invalid generations caught by validation
- **User regeneration rate:** % of users who regenerate vs load anyway

### Qualitative Metrics

- Failure patterns documented with examples
- System prompt updated with canonical examples
- Validation layer catches common errors
- User can see and act on validation errors

---

## 9. Test Execution Plan

### Phase 1: Measurement (2-3 hours)
- TC-AI-MEASURE-01, TC-AI-MEASURE-02
- Document baseline failure patterns

### Phase 2: Prompt Improvement (3-4 hours)
- TC-AI-PROMPT-01, TC-AI-PROMPT-02
- Measure improvement

### Phase 3: Validation Layer (4-5 hours)
- TC-AI-VALIDATE-01 through TC-AI-VALIDATE-04
- TC-AI-REPAIR-01 through TC-AI-REPAIR-03

### Phase 4: Optional E2E (2-3 hours)
- TC-AI-E2E-01, TC-AI-E2E-02
- Only if time permits

### Phase 5: Regression (1-2 hours)
- TC-AI-REG-01, TC-AI-REG-02

---

## 10. Deliverables

1. **Failure pattern document:** `docs/ai-generator-failure-patterns-v32.0.md`
2. **Updated system prompt:** `constants/prompts.ts`
3. **Validation layer:** `utils/validateWorkflow.ts` or similar
4. **UI test report:** `docs/ui-test-report-v32.0-part5.md`

---

## 11. References

- `pm-doc-v32.0-part5-ai-quality.md` — Product requirements
- `arch-doc-v32.0-part5-ai-quality.md` — Technical design
- `pm-doc-v23.0.md` — v23.0 generator baseline
- `docs/e2e-testing-guide.md` — E2E conventions
