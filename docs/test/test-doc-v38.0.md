# Test Document — Hub Deploy & Canvas Import JSON

**Document version:** 38.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Labels:** `TODO-hub-deploy-application-name`, `TODO-canvas-import-workflow-json-validate-apply`

## 1. Test Strategy

This document defines test cases for two independent features:
1. Hub Deploy — Deploy application to remote environment
2. Canvas Import JSON — Import and validate workflow JSON

All tests must pass before marking the TODOs as complete.

## 2. Test Environments

**Desktop:**
- Browser: Chrome 1280×720
- Viewport: 1280px width

**Mobile:**
- Browser: Chrome (device emulation)
- Viewport: 390×844 (iPhone 12 Pro)
- Settings: `isMobile: true`, `hasTouch: true`

**UAT Environment:** https://workflow-ui-gamma.vercel.app

**UAT Backend:**
- operation-api: https://workflow-operation-api-n9sbp.ondigitalocean.app
- online-api: https://workflow-online-api-nr3e4.ondigitalocean.app

---

## 3. Feature 1: Hub Deploy Test Cases

### TC-DEPLOY-01: Desktop Deploy Button Visibility
**Precondition:** Navigate to Applications list (desktop)  
**Steps:**
1. Verify Deploy button visible in application table row actions
2. Verify Deploy button appears alongside Settings/History/Copy/Delete

**Expected:** Deploy button visible and accessible

---

### TC-DEPLOY-02: Mobile Deploy Menu Option
**Precondition:** Navigate to Applications list (mobile 390×844)  
**Steps:**
1. Tap overflow menu (⋯) on any application card
2. Verify Deploy option appears in menu

**Expected:** Deploy option visible in overflow menu

---

### TC-DEPLOY-03: Deploy Modal - Feature Explanation
**Precondition:** Click Deploy button  
**Steps:**
1. Verify modal opens
2. Verify feature explanation text appears at top of modal
3. Verify explanation describes three-step deployment process

**Expected:** Modal shows clear feature explanation before form

---

### TC-DEPLOY-04: Deploy Form - Field Validation
**Precondition:** Deploy modal open  
**Steps:**
1. Leave all fields empty, click Deploy
2. Verify validation errors for all required fields
3. Enter invalid URL (e.g., "not-a-url"), verify URL validation error
4. Enter valid HTTPS URL, verify no URL error
5. Enter HTTP URL, verify warning about HTTPS requirement

**Expected:** All fields required, URL format validated, HTTPS preferred

---

### TC-DEPLOY-05: Deploy Form - Five Fields Present
**Precondition:** Deploy modal open  
**Steps:**
1. Verify Deploy URL field (text input)
2. Verify Application Name field (text input)
3. Verify Service Account Username field (text input)
4. Verify Service Account Password field (password input, masked)
5. Verify Environment field (text input)

**Expected:** All five fields present and correctly typed

---

### TC-DEPLOY-06: Deploy Progress - Three Steps
**Precondition:** Deploy modal with valid form data  
**Steps:**
1. Click Deploy button
2. Verify progress indicator shows Step 1: Create Application Name (in-progress)
3. Wait for Step 1 completion, verify Step 1 shows success checkmark
4. Verify Step 2: Update Application Name (in-progress)
5. Wait for Step 2 completion, verify Step 2 shows success checkmark
6. Verify Step 3: Save Workflow (in-progress)
7. Wait for Step 3 completion, verify Step 3 shows success checkmark

**Expected:** Three-step progress indicator with sequential execution

---

### TC-DEPLOY-07: Deploy Success - Green Indicator
**Precondition:** All three deploy steps completed successfully  
**Steps:**
1. Verify green success indicator displayed
2. Verify success message "Deployment successful" or similar
3. Verify all three steps show green checkmarks

**Expected:** Clear success state with green visual indicators

---

### TC-DEPLOY-08: Deploy Failure - Step 1 (Create)
**Precondition:** Deploy to UAT with existing application name  
**Steps:**
1. Fill form with application name that already exists on target
2. Click Deploy
3. Verify Step 1 fails with error message
4. Verify error message indicates "Application name already exists" or similar
5. Verify Steps 2 and 3 not executed

**Expected:** Step 1 failure stops execution, clear error message

---

### TC-DEPLOY-09: Deploy Failure - Authentication
**Precondition:** Deploy modal open  
**Steps:**
1. Fill form with invalid username/password
2. Click Deploy
3. Verify authentication failure error
4. Verify error message "Authentication failed - check username/password"

**Expected:** Auth failure detected and reported clearly

---

### TC-DEPLOY-10: Deploy to Different Application Name (UAT Key Test)
**Precondition:** Load existing application "AppA" in canvas  
**Steps:**
1. Click Deploy button
2. Fill Deploy URL: https://workflow-operation-api-n9sbp.ondigitalocean.app
3. Fill Application Name: "AppB" (different from loaded "AppA")
4. Fill valid service account credentials
5. Fill Environment: "UAT"
6. Click Deploy
7. Verify all three steps succeed
8. Verify on UAT backend: application "AppB" exists with "AppA" workflow

**Expected:** Deploy creates new application name with current workflow

---

### TC-DEPLOY-11: Deploy Network Timeout
**Precondition:** Deploy modal open  
**Steps:**
1. Fill form with valid data but unreachable URL
2. Click Deploy
3. Wait for timeout (30 seconds per step)
4. Verify timeout error message displayed
5. Verify which step timed out is indicated

**Expected:** Timeout handled gracefully with clear error

---

### TC-DEPLOY-12: Deploy Modal - Carbon Design Compliance
**Precondition:** Deploy modal open  
**Steps:**
1. Inspect modal container, verify 0px border-radius
2. Inspect Deploy button, verify background-color #0f62fe
3. Inspect form inputs, verify Carbon TextInput styling
4. Verify IBM Plex Sans font on all text
5. Verify progress indicator uses Carbon ProgressIndicator component

**Expected:** All components follow Carbon Design System

---

## 4. Feature 2: Canvas Import JSON Test Cases

### TC-IMPORT-01: Desktop Import Button Visibility
**Precondition:** Navigate to canvas editor (desktop)  
**Steps:**
1. Verify Import button visible in canvas header toolbar
2. Verify Import button appears near Explain/Generate/Run buttons

**Expected:** Import button visible and accessible

---

### TC-IMPORT-02: Mobile Import Menu Option
**Precondition:** Navigate to canvas editor (mobile 390×844)  
**Steps:**
1. Tap canvas overflow menu (⋯)
2. Verify Import option appears in menu

**Expected:** Import option visible in overflow menu

---

### TC-IMPORT-03: Import Modal - Feature Explanation
**Precondition:** Click Import button  
**Steps:**
1. Verify modal opens
2. Verify feature explanation at top
3. Verify "What belongs here?" expandable section
4. Expand section, verify example JSON snippet shown
5. Verify note "Not for plain English — use Generate for that"

**Expected:** Clear instructions and example provided

---

### TC-IMPORT-04: Import Valid JSON - Paste
**Precondition:** Import modal open  
**Steps:**
1. Paste valid workflow JSON into textarea:
```json
{
  "pluginList": [
    {
      "id": 1,
      "description": "Test node",
      "linkingIdOfRuleListAndAction": "rule-1",
      "ruleList": [{"key": "$.data.test"}],
      "action": {"type": "FUNCTION_V2"}
    }
  ],
  "uiMapList": []
}
```
2. Verify validation passes (no errors)
3. Verify preview summary shows "1 node, 0 edges"
4. Click Apply
5. Verify canvas updated with imported workflow

**Expected:** Valid JSON imported successfully

---

### TC-IMPORT-05: Import Valid JSON - File Upload
**Precondition:** Import modal open, prepare valid workflow.json file  
**Steps:**
1. Click file upload button
2. Select workflow.json file
3. Verify JSON loaded into textarea
4. Verify validation passes
5. Click Apply
6. Verify canvas updated

**Expected:** File upload works, JSON imported successfully

---

### TC-IMPORT-06: Import JSON with Markdown Code Fences
**Precondition:** Import modal open  
**Steps:**
1. Paste JSON wrapped in markdown code fences:
```
\`\`\`json
{"pluginList": [], "uiMapList": []}
\`\`\`
```
2. Verify code fences stripped automatically
3. Verify validation passes
4. Verify preview shows "0 nodes, 0 edges"

**Expected:** Markdown fences stripped, JSON parsed correctly

---

### TC-IMPORT-07: Import Invalid JSON - Parse Error
**Precondition:** Import modal open  
**Steps:**
1. Paste invalid JSON: `{"pluginList": [}`
2. Verify parse error displayed
3. Verify error message includes line number if possible
4. Verify Apply button disabled

**Expected:** Parse error caught, clear error message, Apply disabled

---

### TC-IMPORT-08: Import JSON - Missing Required Fields
**Precondition:** Import modal open  
**Steps:**
1. Paste JSON missing pluginList: `{"uiMapList": []}`
2. Verify validation error "pluginList: Required field missing"
3. Verify Apply button disabled

**Expected:** Schema validation catches missing required fields

---

### TC-IMPORT-09: Import JSON - Invalid Plugin Type
**Precondition:** Import modal open  
**Steps:**
1. Paste JSON with invalid plugin type:
```json
{
  "pluginList": [
    {"id": 1, "action": {"type": "INVALID_TYPE"}}
  ],
  "uiMapList": []
}
```
2. Verify validation error "Invalid plugin type: INVALID_TYPE"
3. Verify error indicates valid types (FUNCTION_V2, FUNCTION_V3, HTTP_CALL, LOGIC)
4. Verify Apply button disabled

**Expected:** Invalid plugin type caught with helpful error

---

### TC-IMPORT-10: Import JSON - Duplicate Plugin IDs
**Precondition:** Import modal open  
**Steps:**
1. Paste JSON with duplicate IDs:
```json
{
  "pluginList": [
    {"id": 1, "action": {"type": "FUNCTION_V2"}},
    {"id": 1, "action": {"type": "HTTP_CALL"}}
  ],
  "uiMapList": []
}
```
2. Verify validation error "Duplicate plugin IDs: 1"
3. Verify Apply button disabled

**Expected:** Duplicate IDs detected and reported

---

### TC-IMPORT-11: Import JSON - Invalid Edge References
**Precondition:** Import modal open  
**Steps:**
1. Paste JSON with edge referencing non-existent node:
```json
{
  "pluginList": [{"id": 1}],
  "uiMapList": [{"source": "1", "target": "999"}]
}
```
2. Verify validation error "Edge target '999' does not exist in pluginList"
3. Verify Apply button disabled

**Expected:** Invalid edge references caught

---

### TC-IMPORT-12: Import JSON - Preview Summary
**Precondition:** Import modal with valid JSON  
**Steps:**
1. Paste JSON with 5 nodes and 4 edges
2. Verify preview summary displays:
   - "5 nodes"
   - "4 edges"
3. Verify warnings if any (e.g., orphaned nodes)

**Expected:** Accurate preview summary before apply

---

### TC-IMPORT-13: Import with Non-Empty Canvas - Confirmation
**Precondition:** Canvas has existing workflow (non-empty)  
**Steps:**
1. Click Import button
2. Paste valid JSON
3. Click Apply
4. Verify confirmation dialog appears
5. Verify message "Replace current workflow?" or similar
6. Click Cancel, verify canvas unchanged
7. Click Apply again, click Confirm
8. Verify canvas replaced with imported workflow

**Expected:** Confirmation prompt prevents accidental data loss

---

### TC-IMPORT-14: Import with Empty Canvas - No Confirmation
**Precondition:** Canvas is empty (no nodes)  
**Steps:**
1. Click Import button
2. Paste valid JSON
3. Click Apply
4. Verify no confirmation dialog (canvas is empty)
5. Verify canvas updated immediately

**Expected:** No confirmation needed for empty canvas

---

### TC-IMPORT-15: Import Does Not Auto-Save
**Precondition:** Import valid JSON to canvas  
**Steps:**
1. Import valid workflow JSON
2. Verify canvas updated
3. Refresh page
4. Verify imported workflow NOT persisted (canvas reverts to saved state)
5. Navigate back to canvas, click Save button
6. Refresh page
7. Verify workflow now persisted

**Expected:** Import applies to canvas but does not auto-save to backend

---

### TC-IMPORT-16: Import Modal - Carbon Design Compliance
**Precondition:** Import modal open  
**Steps:**
1. Inspect modal container, verify 0px border-radius
2. Inspect Apply button, verify background-color #0f62fe
3. Inspect textarea, verify Carbon styling
4. Verify IBM Plex Sans font on all text
5. Verify error messages use Carbon InlineNotification

**Expected:** All components follow Carbon Design System

---

### TC-IMPORT-17: Import Large Workflow - Performance
**Precondition:** Prepare JSON with 100 nodes  
**Steps:**
1. Paste 100-node workflow JSON
2. Start timer
3. Wait for validation to complete
4. Verify validation completes within 2 seconds
5. Verify preview summary accurate

**Expected:** Validation completes within 2s for 100 nodes

---

### TC-IMPORT-18: Import vs Generate - Separate Features
**Precondition:** Canvas editor open  
**Steps:**
1. Verify Import button separate from Generate button
2. Click Import, verify modal asks for JSON (not natural language)
3. Close Import modal
4. Click Generate, verify modal asks for natural language description
5. Verify both modals have distinct purposes in UI text

**Expected:** Import and Generate are clearly separate features

---

## 5. Accessibility Test Cases

### TC-A11Y-01: Deploy Modal - Keyboard Navigation
**Precondition:** Deploy modal open  
**Steps:**
1. Tab through all form fields
2. Verify focus order: URL → Name → Username → Password → Environment → Deploy button
3. Press Enter on Deploy button, verify form submits
4. Press Escape, verify modal closes

**Expected:** Full keyboard navigation support

---

### TC-A11Y-02: Deploy Modal - Screen Reader
**Precondition:** Deploy modal open, screen reader active (NVDA/JAWS/VoiceOver)  
**Steps:**
1. Verify modal announces "Deploy Application" or similar
2. Verify each form field announces its label
3. Verify validation errors announced when triggered
4. Verify progress updates announced during deploy

**Expected:** Screen reader announces all relevant information

---

### TC-A11Y-03: Import Modal - Keyboard Navigation
**Precondition:** Import modal open  
**Steps:**
1. Tab through textarea, file upload button, Apply button
2. Verify focus order logical
3. Press Enter on Apply button, verify action triggered
4. Press Escape, verify modal closes

**Expected:** Full keyboard navigation support

---

### TC-A11Y-04: Import Modal - Screen Reader
**Precondition:** Import modal open, screen reader active  
**Steps:**
1. Verify modal announces "Import Workflow" or similar
2. Verify textarea label announced
3. Verify validation errors announced when triggered
4. Verify preview summary announced

**Expected:** Screen reader announces all relevant information

---

## 6. Test Execution Summary

**Total Test Cases:** 38
- Deploy: 12 test cases
- Import: 18 test cases
- Accessibility: 4 test cases
- Carbon Design: 4 test cases (embedded in feature tests)

**Pass Criteria:**
- All 38 test cases must pass
- Zero regressions in existing E2E test suite
- WCAG 2.1 AA compliance verified
- Carbon Design System compliance verified

**UAT Key Test Cases (must pass before marking Done):**
- TC-DEPLOY-10: Deploy to different application name on UAT
- TC-IMPORT-04: Import valid JSON and verify canvas update
- TC-IMPORT-13: Import with non-empty canvas confirmation

---

*End of Test Document v38.0*
