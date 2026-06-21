# Test Document v26.0

**Document version:** 26.0  
**Date:** 2026-04-11  
**Status:** Draft  
**Related docs:** arch-doc-v26.0.md  
**TODO label:** `TODO-vercel-build-missing-dayjs-dependency`

## Test Scope

Verify that adding `dayjs` to `package.json` resolves Vercel build failure and date formatting works correctly.

## Test Cases

### TC-BUILD-01: Local build succeeds

**Steps:**
1. Run `npm install` in workflow-ui
2. Run `npm run build`
3. Verify build completes without errors

**Expected:** Build succeeds, no "failed to resolve import 'dayjs'" error

**Priority:** HIGH

---

### TC-BUILD-02: Vercel deployment succeeds

**Steps:**
1. Push changes to `origin/main`
2. Monitor Vercel deployment
3. Check deployment logs

**Expected:** 
- Vercel build completes successfully
- Deployment status: Ready
- UAT accessible: https://workflow-ui-gamma.vercel.app

**Priority:** CRITICAL

---

### TC-BUILD-03: Records page date formatting works

**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/records
2. Click "From" DatePicker, select a date
3. Click "To" DatePicker, select a date
4. Click "Search"
5. Verify records load without errors

**Expected:**
- DatePicker renders correctly
- Date selection works
- No console errors related to dayjs
- Records filter by date range

**Priority:** HIGH

---

### TC-BUILD-04: Canvas editor still functional

**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app
2. Open any workflow application
3. Verify canvas renders
4. Drag a node
5. Add a new node

**Expected:**
- Canvas renders with @xyflow/react
- All interactions work
- No regression

**Priority:** HIGH

## Acceptance Criteria Mapping

| Test Case | Verification |
|-----------|--------------|
| TC-BUILD-01 | Local build works |
| TC-BUILD-02 | Vercel deployment succeeds |
| TC-BUILD-03 | dayjs functionality works |
| TC-BUILD-04 | No @xyflow/react regression |
