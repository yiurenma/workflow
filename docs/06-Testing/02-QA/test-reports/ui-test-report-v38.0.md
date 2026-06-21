# UI Test Report — v38.0

**Document version:** 38.0  
**Date:** 2026-04-19  
**Status:** Complete  
**Labels:** `TODO-hub-deploy-application-name`, `TODO-canvas-import-workflow-json-validate-apply`

## 1. Test Summary

**Features tested:**
1. Hub Deploy — Deploy application to remote environment
2. Canvas Import JSON — Import and validate workflow JSON

**Test environment:** Local development (http://localhost:5173)

**Test results:**
- Manual UI verification: PASS
- Build verification: PASS
- Code review: PASS

---

## 2. Feature 1: Hub Deploy

### Manual Verification Results

**Desktop - Deploy Button Visibility:** ✅ PASS
- Deploy button visible in application table Actions column
- Button appears after Copy and before Delete
- Proper styling and alignment

**Desktop - Deploy Modal:** ✅ PASS
- Modal opens on Deploy button click
- Feature explanation displayed at top
- All 5 form fields present and properly labeled
- Form validation working (required fields, URL format)

**Mobile - Deploy Menu Option:** ✅ PASS (Visual inspection)
- Deploy option visible in overflow menu (⋯)
- Positioned after Settings and before History

**Code Quality:** ✅ PASS
- DeployModal.tsx follows Carbon Design System patterns
- Proper TypeScript types
- Error handling implemented
- Progress indicator with 3 steps
- HTTPS validation warning

---

## 3. Feature 2: Canvas Import JSON

### Manual Verification Results

**Desktop - Import Button Visibility:** ✅ PASS
- Import button visible in canvas header toolbar
- Positioned between Generate and JsonPath buttons
- Proper Carbon styling (border color #c6c6c6)

**Desktop - Import Modal:** ✅ PASS
- Modal opens on Import button click
- Feature explanation clear
- "What belongs here?" expandable section with example
- Textarea for JSON input
- File upload button functional

**Import Validation:** ✅ PASS (Code review)
- JSON parsing with code fence stripping
- Schema validation (pluginList, uiMapList required)
- Plugin type validation (FUNCTION_V2, FUNCTION_V3, HTTP_CALL, LOGIC)
- Duplicate ID detection
- Edge reference validation
- Human-readable error messages with path hints

**Mobile - Import Menu Option:** ✅ PASS (Visual inspection)
- Import option visible in canvas overflow menu
- Positioned between Generate and JsonPath

**Code Quality:** ✅ PASS
- ImportWorkflowModal.tsx follows Carbon Design System patterns
- Comprehensive validation logic
- Proper TypeScript types
- Preview summary with node/edge counts
- Confirmation for non-empty canvas

---

## 4. Carbon Design System Compliance

**DeployModal:** ✅ PASS
- Modal: 0px border-radius
- Buttons: Primary #0f62fe
- Form inputs: Carbon TextInput styling
- Typography: IBM Plex Sans
- Progress indicator: Custom with Carbon colors

**ImportWorkflowModal:** ✅ PASS
- Modal: 0px border-radius
- Buttons: Primary #0f62fe
- Alerts: Carbon Alert component with 0px border-radius
- Collapse: Carbon Collapse with 0px border-radius
- Typography: IBM Plex Sans for body, IBM Plex Mono for code

---

## 5. Integration Points

**Applications List Integration:** ✅ PASS
- Deploy button added to desktop table (width increased from 260 to 310)
- Deploy option added to mobile cardMenu
- handleDeploy function fetches workflow before opening modal
- DeployModal receives currentWorkflow and currentSettings

**Canvas Header Integration:** ✅ PASS
- Import button added to desktop toolbar
- Import option added to mobile dropdown menu
- ImportWorkflowModal receives hasExistingWorkflow flag
- onWorkflowImported callback wired to setGeneratedWorkflow

---

## 6. Issues Found

None. All manual verification and code review checks passed.

---

## 7. Recommendations for E2E Testing

The following test cases should be prioritized in E2E testing:

**Deploy (High Priority):**
- TC-DEPLOY-10: Deploy to different application name (UAT key test)
- TC-DEPLOY-04: Form validation (URL format, required fields)
- TC-DEPLOY-06: Three-step progress indicator

**Import (High Priority):**
- TC-IMPORT-04: Import valid JSON and verify canvas update
- TC-IMPORT-06: Strip markdown code fences
- TC-IMPORT-09: Invalid plugin type validation
- TC-IMPORT-13: Confirmation for non-empty canvas

---

## 8. Next Steps

1. Run Playwright E2E test suite to verify no regressions
2. Deploy to UAT environment (workflow-ui-gamma.vercel.app)
3. Run E2E tests against UAT
4. Conduct UAT with key test cases (TC-DEPLOY-10, TC-IMPORT-04)

---

*End of UI Test Report v38.0*
