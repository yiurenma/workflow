# UAT Report v26.0

**Document version:** 26.0  
**Date:** 2026-04-11  
**Status:** PASS  
**Related docs:** arch-doc-v26.0.md, test-doc-v26.0.md, ui-test-report-v26.0.md  
**TODO label:** `TODO-vercel-build-missing-dayjs-dependency`

## Executive Summary

**Result:** ✅ PASS  
**Tested by:** Human + Claude Agent  
**Test date:** 2026-04-11 21:13  
**Environment:** https://workflow-ui-gamma.vercel.app

All critical functionality verified after adding missing `dayjs` and `json5` dependencies. Vercel deployment now succeeds, Records page date filtering works correctly, and no regressions detected in canvas editor.

---

## Issues Fixed

### Issue 1: Missing dayjs dependency
- **Symptom:** Vercel build failed with "Rollup failed to resolve import 'dayjs'"
- **Root cause:** `dayjs` imported in `src/routes/records/index.tsx:19` but not declared in `package.json`
- **Fix:** Added `"dayjs": "^1.11.13"` to dependencies
- **Result:** ✅ Fixed

### Issue 2: Missing json5 dependency
- **Symptom:** Vercel build failed with "Rollup failed to resolve import 'json5'"
- **Root cause:** `json5` imported in `WorkflowGeneratorModal.tsx:4` but not declared in `package.json`
- **Fix:** Added `"json5": "^2.2.3"` to dependencies
- **Result:** ✅ Fixed

### Issue 3: Outdated pnpm-lock.yaml
- **Symptom:** Vercel build failed with "Cannot install with frozen-lockfile"
- **Root cause:** `pnpm-lock.yaml` not updated after adding dependencies
- **Fix:** Ran `pnpm install` and committed updated lockfile
- **Result:** ✅ Fixed

---

## Test Results

### ✅ Vercel Deployment
- **Status:** PASS
- **Build time:** ~5-6 minutes
- **Deployment status:** Ready
- **Build logs:** No "failed to resolve import" errors
- **UAT URL accessible:** https://workflow-ui-gamma.vercel.app

### ✅ Local Build Verification
- **npm install:** Success
- **pnpm install:** Success (dayjs@1.11.20, json5@2.2.3)
- **npm run build:** Success (18.67s, 3351 modules transformed)
- **No errors:** Confirmed

### ✅ Records Page Functionality
- **Date picker rendering:** Confirmed working (user verified)
- **Date selection:** Functional
- **Date filtering:** Working correctly
- **Console errors:** None related to dayjs
- **Mobile viewport:** Not explicitly tested but no issues reported

### ✅ Canvas Editor (No Regression)
- **Canvas rendering:** Confirmed working (user verified)
- **@xyflow/react:** No conflicts or errors
- **Node interactions:** Functional
- **Workflow editor:** No regressions detected

---

## Root Cause Analysis

**Why did local builds succeed but Vercel failed?**

1. **Local environment:**
   - Contains transitive dependencies installed by other packages
   - `node_modules` may have `dayjs` and `json5` from previous `npm install` runs
   - Rollup can resolve these packages even if not explicitly declared

2. **Vercel environment:**
   - Clean environment with `--frozen-lockfile` flag
   - Only installs packages explicitly declared in `package.json`
   - Strict dependency resolution catches missing declarations

3. **Lesson learned:**
   - Always run `pnpm install` (or `npm install`) after adding dependencies to `package.json`
   - Commit both `package.json` AND lockfile (`pnpm-lock.yaml`)
   - Test in clean environment when possible to catch missing dependencies

---

## Commits

### workflow-ui repository
1. **03ad800** - Added dayjs to package.json and updated routeTree.gen.ts
2. **1019e69** - Updated pnpm-lock.yaml for dayjs dependency
3. **204d850** - Added json5 to package.json and updated pnpm-lock.yaml

### workflow-agent-teams repository
1. **02a3805** - Added arch-doc-v26.0.md and test-doc-v26.0.md
2. **15fee8d** - Added ui-test-report-v26.0.md and uat-script-v26.0.md

### Main repository
1. **a847647** - Updated submodule pointers for workflow-ui and workflow-agent-teams

---

## Acceptance Criteria Verification

| Criteria | Status | Notes |
|----------|--------|-------|
| AC-BUILD-26.1: dayjs added to package.json | ✅ PASS | dayjs@^1.11.13 added |
| AC-BUILD-26.2: Vercel build succeeds | ✅ PASS | Deployment successful |
| AC-BUILD-26.3: @xyflow/react verified | ✅ PASS | No changes needed, already present |
| AC-BUILD-26.4: UAT environment accessible | ✅ PASS | https://workflow-ui-gamma.vercel.app |
| AC-BUILD-26.5: No other missing dependencies | ✅ PASS | json5 also added |
| AC-BUILD-26.6: Local dev works | ✅ PASS | No regressions |

---

## Additional Findings

- **json5 dependency:** Also missing, discovered during second Vercel build attempt
- **pnpm-lock.yaml:** Must be updated and committed alongside package.json changes
- **Build time:** Vercel builds take ~5-6 minutes for workflow-ui

---

## Recommendations

1. **CI/CD improvement:** Consider adding a pre-commit hook or CI check that verifies all imported packages are declared in package.json
2. **Documentation:** Update developer onboarding docs to emphasize running `pnpm install` after dependency changes
3. **Testing:** Consider periodic clean builds in CI to catch missing dependency declarations early

---

## Conclusion

All issues resolved successfully. Vercel deployments now work correctly, and all functionality verified in UAT environment. No regressions detected. TODO item can be marked as Done.

**Overall Result:** ✅ PASS
