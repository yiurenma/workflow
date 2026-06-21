# E2E Test Report v1.0

**Date:** 2026-04-07  
**Environment:** UAT (https://workflow-ui-gamma.vercel.app)  
**Test Suite:** Playwright E2E Tests  
**Execution Time:** 5.4 minutes

## Summary

| Metric | Count | Percentage |
|--------|-------|------------|
| **Total Tests** | 66 | 100% |
| **Passed** | 16 | 24% |
| **Failed** | 39 | 59% |
| **Skipped** | 11 | 17% |

## Test Configuration

- **Projects:** Desktop Chrome, Mobile Chrome (iPhone 12 viewport)
- **Base URL:** https://workflow-ui-gamma.vercel.app
- **Timeout:** 30 seconds per test
- **Retries:** 1

## Results by Project

### Desktop Chrome (33 tests)

**Passed: 10 tests**
- TC-APP-DESK-01: table renders with columns ✓
- TC-APP-DESK-02: pagination visible below table ✓
- TC-APP-DESK-03: total count shown ✓
- TC-APP-DESK-04: search filters list ✓
- TC-APP-DESK-06: Settings button opens modal ✓
- TC-APP-DESK-07: History button opens drawer ✓
- TC-APP-DESK-08: Copy button opens modal ✓
- TC-APP-DESK-09: Delete button shows confirm dialog ✓
- TC-NAV-01/02/03/04: All navigation tests ✓
- TC-REC-01/02/03: All records list tests ✓

**Failed: 6 tests**
- TC-APP-MOB-01: card view renders (not table) on narrow viewport ✗
- TC-APP-MOB-05: ellipsis menu → History opens drawer (timeout 30s) ✗
- TC-APP-MOB-06: ellipsis menu → Copy opens modal (timeout 30s) ✗
- TC-APP-MOB-07: ellipsis menu → Delete shows confirm (timeout 30s) ✗
- TC-APP-MOB-09: FAB + button is visible ✗
- TC-APP-MOB-10: FAB tap opens New Application dialog ✗

**Skipped: 11 tests**
- TC-APP-DESK-05: Open button (no applications exist)
- TC-APP-MOB-02/03/04/08: Mobile tests (viewport condition)
- TC-CANVAS-01/02/03: Canvas tests
- TC-EXPLAIN-01/02: Explain feature tests
- TC-NODE-01/02: Node editor tests

### Mobile Chrome (33 tests)

**All 33 tests failed** - Same test suite as Desktop Chrome, all tests failed on mobile viewport.

## Critical Issues

### 1. Mobile Viewport Tests Failing (39 failures)

**Affected Tests:** All Mobile Chrome project tests + 6 Desktop Chrome mobile tests

**Root Cause:** Tests designed for mobile viewport are failing across both projects.

**Specific Issues:**
- **TC-APP-MOB-01:** Card view not rendering correctly on narrow viewport
- **TC-APP-MOB-05/06/07:** Ellipsis menu interactions timing out (30 seconds)
  - This matches the known bug in TODO.md: "Mobile — application overflow menu (History / Delete / Copy) navigates to Workflow instead of opening flows"
- **TC-APP-MOB-09/10:** FAB button tests failing

### 2. Test Suite Configuration

**Issue:** Mobile Chrome project runs all tests including desktop-specific tests, causing unnecessary failures.

**Recommendation:** Desktop-specific tests should be properly skipped on mobile viewports.

## Test Coverage

### Covered Features ✓
- Applications list (desktop table view)
- Pagination and search
- Modal dialogs (Settings, Copy, Delete confirmation)
- History drawer
- Navigation routing
- Records list

### Not Covered (Skipped)
- Canvas/Artboard functionality
- Node editor
- Explain feature
- Application opening (no test data)

## Recommendations

1. **Fix Mobile Overflow Menu Bug** (TODO item already exists)
   - Priority: HIGH
   - This is blocking 3 mobile tests and is a known UAT issue

2. **Investigate Mobile Card View Rendering**
   - TC-APP-MOB-01 is failing consistently
   - Need to verify card view CSS/layout on narrow viewports

3. **Fix FAB Button Tests**
   - TC-APP-MOB-09/10 failing
   - Verify FAB visibility and interaction on mobile

4. **Improve Test Data Setup**
   - Many tests skip due to no applications existing
   - Consider seeding test data in UAT environment

5. **Optimize Test Configuration**
   - Properly skip desktop-only tests on mobile project
   - Consider separating mobile and desktop test suites

## Next Steps

1. Address the mobile overflow menu bug (already in TODO.md)
2. Run tests again after bug fix
3. Investigate remaining mobile test failures
4. Add test data seeding for better coverage

---

**Report Generated:** 2026-04-07 21:51 CST  
**Test Artifacts:** `playwright-report/index.html`
