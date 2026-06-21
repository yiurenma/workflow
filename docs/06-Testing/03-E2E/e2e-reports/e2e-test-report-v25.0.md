# E2E Test Report v25.0

**Document version:** 25.0  
**Date:** 2026-04-11  
**Status:** FAILED (deployment required)  
**Related docs:** pm-doc-v25.0.md, arch-doc-v25.0.md, test-doc-v25.0.md, ui-test-report-v25.0.md  
**TODO label:** `TODO-node-editor-form-descriptions-color-contrast`  
**Test Environment:** https://workflow-ui-gamma.vercel.app  
**Test Date:** 2026-04-11 20:09-20:11  
**Tester:** E2E Tester (Playwright)

## Executive Summary

**Overall Status:** ❌ FAILED

E2E tests detected the exact color contrast violations that v25.0 was designed to fix. This confirms:
1. ✅ The test suite correctly identifies the accessibility issue
2. ❌ The fix has NOT been deployed to UAT environment yet
3. ⚠️ UAT environment is running stale code

**Root Cause:** Code fix was pushed to GitHub (workflow-ui commit `f54cb97`) but UAT environment (workflow-ui-gamma.vercel.app) has not been redeployed with the latest code.

## Test Results Summary

**Total Test Cases:** 63  
**Passed:** 59  
**Failed:** 3 (2 unique failures, 1 with retry)  
**Skipped:** 1  
**Pass Rate:** 93.7%

## Failed Test Cases

### TC-NODE-ENHANCED-01: Desktop drawer meets all 5 layers
**Status:** ❌ FAILED (2 attempts)  
**Failure Reason:** Axe accessibility scan detected 3 color contrast violations

**Violations Detected:**

1. **Node Description section subtitle**
   - Element: `<p class="text-xs text-zinc-400 mb-3">The name shown on the canvas. What is this step called?</p>`
   - Foreground: #9f9fa9 (text-zinc-400)
   - Background: #ffffff (white)
   - Contrast: 2.62:1 ❌
   - Required: 4.5:1
   - Location: `.mb-4.p-3.border:nth-child(1) > .mb-3`

2. **Rules section subtitle**
   - Element: `<p class="text-xs text-zinc-400 mb-3">Run only when… — conditions that must all match before this step executes.</p>`
   - Foreground: #9f9fa9 (text-zinc-400)
   - Background: #ffffff (white)
   - Contrast: 2.62:1 ❌
   - Required: 4.5:1
   - Location: `.mb-4.p-3.border:nth-child(2) > .mb-3`

3. **Action section subtitle**
   - Element: `<p class="text-xs text-zinc-400 mb-3">What the system does when this step runs.</p>`
   - Foreground: #9f9fa9 (text-zinc-400)
   - Background: #fafafa (bg-zinc-50)
   - Contrast: 2.51:1 ❌
   - Required: 4.5:1
   - Location: `.mb-4.p-3.bg-zinc-50 > .mb-3`

**Analysis:** These are the exact violations that v25.0 was designed to fix. The test correctly identified the problem.

---

### TC-NODE-ENHANCED-05: Close drawer returns to canvas
**Status:** ❌ FAILED (2 attempts)  
**Failure Reason:** Drawer close button non-functional

**Analysis:** This is a separate known issue tracked in TODO: `TODO-uat-e2e-node-drawer-close-button-broken`. Not related to v25.0 color contrast fix.

---

## Passed Test Cases (59)

All other test cases passed, including:
- Navigation (TC-NAV-01 to TC-NAV-04): 8 tests ✅
- Applications Desktop (TC-APP-DESK-01 to TC-APP-DESK-10): 10 tests ✅
- Applications Mobile (TC-APP-MOB-01 to TC-APP-MOB-10): 10 tests ✅
- Canvas (TC-CANVAS-01 to TC-CANVAS-05): 5 tests ✅
- Canvas Mobile (TC-CANVAS-MOB-01 to TC-CANVAS-MOB-09): 9 tests ✅
- Node Editor (TC-NODE-01 to TC-NODE-02): 2 tests ✅
- Node Editor Enhanced (TC-NODE-ENHANCED-03, TC-NODE-ENHANCED-04): 2 tests ✅
- Records (TC-REC-01 to TC-REC-03): 6 tests ✅
- Explain (TC-EXPLAIN-01 to TC-EXPLAIN-02): 2 tests ✅
- JsonPath (TC-JSONPATH-01 to TC-JSONPATH-05): 5 tests ✅

## Deployment Status

**Code Status:**
- ✅ Fix implemented: workflow-ui commit `f54cb97`
- ✅ Pushed to GitHub: origin/main
- ❌ Deployed to UAT: workflow-ui-gamma.vercel.app still running old code

**Evidence:**
- Axe scan detected `text-zinc-400` (#9f9fa9) in UAT environment
- Code fix changed to `text-zinc-600` (#52525b)
- UAT environment has not picked up the change

## Next Steps

1. **Deploy to UAT**: Trigger Vercel deployment for workflow-ui-gamma.vercel.app
2. **Re-run E2E tests**: After deployment, run `npm run test:e2e` again
3. **Expected outcome**: TC-NODE-ENHANCED-01 should pass with zero color contrast violations
4. **Update reports**: Create e2e-test-report-v25.0-retest.md after successful deployment

## Test Environment Details

**UAT Environment:** https://workflow-ui-gamma.vercel.app  
**Test Framework:** Playwright 1.59.1  
**Browser:** Chromium headless  
**Projects:**
- Desktop Chrome (1280px viewport)
- Mobile Chrome (390×844px viewport, touch enabled)

**Test Duration:** ~2 minutes  
**Workers:** 2 parallel workers  
**Retries:** 1 retry per failed test

## Accessibility Tool Details

**Axe Core Version:** 4.11  
**Rules Tested:** color-contrast (WCAG 2.1 Level AA)  
**Violation Impact:** Serious  
**Tags:** cat.color, wcag2aa, wcag143, TTv5, TT13.c, EN-301-549, EN-9.1.4.3, ACT, RGAAv4, RGAA-3.2.1

## Conclusion

The E2E test suite is working correctly and successfully detected the color contrast violations that v25.0 was designed to fix. The failures are expected because the UAT environment has not been updated with the fix yet.

**Status:** ⚠️ **BLOCKED - Awaiting UAT deployment**

Once the code is deployed to UAT, we expect:
- TC-NODE-ENHANCED-01 to pass (0 color contrast violations)
- Overall pass rate to increase to 96.8% (61/63 tests)
- TC-NODE-ENHANCED-05 will remain failed (separate issue)

## Recommendations

1. ✅ Code fix is correct and ready
2. ⚠️ Deploy workflow-ui commit `f54cb97` to UAT environment
3. ⚠️ Re-run E2E tests after deployment
4. ⚠️ Do not mark TODO as Done until E2E tests pass on UAT

## Process Violation Acknowledgment

**Violation:** E2E tests should have been run BEFORE marking TODO as Done and BEFORE human UAT.

**Correct sequence:**
1. QA test → ui-test-report ✅
2. **E2E test → e2e-test-report** ❌ (skipped initially)
3. Deploy to UAT
4. Human UAT → uat-report
5. Mark TODO as Done

**Actual sequence:**
1. QA test → ui-test-report ✅
2. ~~E2E test~~ (skipped)
3. Human UAT → uat-report ✅
4. Mark TODO as Done ✅
5. E2E test (now) → discovered deployment gap ❌

**Lesson:** E2E tests against UAT environment are critical for catching deployment gaps before human UAT.

## Artifacts

**Screenshots:** test-results/node-editor-enhanced-Node--45d12-p-drawer-meets-all-5-layers-Desktop-Chrome/test-failed-1.png  
**HTML Report:** workflow-ui/playwright-report/index.html  
**Test Output:** Available in task output file
