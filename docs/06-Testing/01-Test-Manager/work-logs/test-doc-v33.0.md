# Test Document v33.0 — E2E Test Failure Remediation

**Document version:** 33.0  
**Date:** 2026-04-12  
**Status:** Approved  
**Recognition label:** `TODO-e2e-fix-test-failures-post-v30`

## Test Strategy

This document covers fixes to existing E2E tests, not new test cases. The goal is to achieve ≥90% pass rate (96/106 tests) by fixing mock data loading, Carbon Design System validation, and component interaction issues.

## Test Cases

### Category 1: Mock Data Loading (P0)

**TC-MOCK-01: Applications desktop tests load mock data**
- **Precondition:** Mock handlers registered via `setupMocks()`
- **Steps:**
  1. Navigate to `/workflows/`
  2. Wait for `networkidle` load state
  3. Wait for table or card container to render
  4. Verify table has rows or cards are visible
- **Expected:** Table renders with 8 mock applications
- **Validation:** `await expect(page.locator('table tbody tr')).toHaveCount(8)`

**TC-MOCK-02: Mock route handlers match API endpoints**
- **Precondition:** Backend API endpoints documented
- **Steps:**
  1. Review `mocks.ts` route patterns
  2. Compare with actual API endpoints from operation-api
  3. Verify patterns match (e.g., `/workflow/entity-setting` not `/api/workflow/entity-setting`)
- **Expected:** All route patterns match actual endpoints
- **Validation:** Manual code review + debug logging

**TC-MOCK-03: Navigation tests wait for content**
- **Precondition:** Navigation spec uses `setupMocks()`
- **Steps:**
  1. Navigate to `/workflows/`
  2. Verify `waitForLoadState('networkidle')` is called
  3. Verify content selector wait is present
- **Expected:** No timeout errors, content loads reliably
- **Validation:** Test passes consistently (3/3 runs)

### Category 2: Carbon Design System Compliance (P1)

**TC-CARBON-FIX-01: Navigation bar uses Carbon Gray 100**
- **Precondition:** Theme configuration updated
- **Steps:**
  1. Navigate to any page
  2. Get computed background color of nav bar
  3. Verify color is `rgb(22, 22, 22)` (hex #161616)
- **Expected:** Nav background matches Carbon Gray 100
- **Validation:** 
  ```typescript
  const nav = page.locator('header.ant-layout-header');
  const bgColor = await nav.evaluate(el => 
    window.getComputedStyle(el).backgroundColor
  );
  expect(bgColor).toBe('rgb(22, 22, 22)');
  ```

**TC-CARBON-FIX-02: Navigation bar height ≥48px**
- **Precondition:** Header component updated
- **Steps:**
  1. Navigate to any page
  2. Get bounding box of nav bar
  3. Verify height ≥48px
- **Expected:** Nav height meets Carbon shell spec
- **Validation:**
  ```typescript
  const nav = page.locator('header.ant-layout-header');
  const box = await nav.boundingBox();
  expect(box!.height).toBeGreaterThanOrEqual(48);
  ```

**TC-CARBON-FIX-03: Primary buttons meet 40×44px minimum**
- **Precondition:** Button theme tokens updated
- **Steps:**
  1. Navigate to applications list
  2. Get bounding box of "New Application" button
  3. Verify height ≥40px and width ≥44px
- **Expected:** Button meets touch target spec
- **Validation:**
  ```typescript
  const btn = page.getByRole('button', { name: 'New Application' });
  const box = await btn.boundingBox();
  expect(box!.height).toBeGreaterThanOrEqual(40);
  expect(box!.width).toBeGreaterThanOrEqual(44);
  ```

**TC-CARBON-FIX-04: Border radius is 0px**
- **Precondition:** Theme borderRadius set to 0
- **Steps:**
  1. Navigate to applications list
  2. Get computed border-radius of primary button
  3. Verify value is '0px'
- **Expected:** Sharp corners per Carbon spec
- **Validation:**
  ```typescript
  const btn = page.getByRole('button', { name: 'New Application' });
  const radius = await btn.evaluate(el =>
    window.getComputedStyle(el).borderRadius
  );
  expect(radius).toBe('0px');
  ```

### Category 3: Component Interaction Stability (P2)

**TC-INTERACT-01: Drawer animations complete before assertions**
- **Precondition:** Node editor drawer opens
- **Steps:**
  1. Click on canvas node
  2. Wait for drawer to appear
  3. Wait 300ms for animation to complete
  4. Assert on drawer content
- **Expected:** No flaky failures due to animation timing
- **Validation:** Test passes consistently (3/3 runs)

**TC-INTERACT-02: Mobile drawer scroll bounds correct**
- **Precondition:** Mobile viewport (390×844)
- **Steps:**
  1. Open node editor drawer on mobile
  2. Verify drawer height calculation includes safe area insets
  3. Scroll to bottom of drawer
  4. Verify no content clipping
- **Expected:** All drawer content accessible
- **Validation:** Visual inspection + `toBeInViewport()` checks

**TC-INTERACT-03: Canvas mobile FAB visible**
- **Precondition:** Mobile viewport, canvas loaded
- **Steps:**
  1. Navigate to canvas on mobile
  2. Wait for FAB to render
  3. Verify FAB is visible and in viewport
- **Expected:** FAB appears within 2 seconds
- **Validation:**
  ```typescript
  const fab = page.locator('[data-testid="mobile-add-node-fab"]');
  await expect(fab).toBeVisible({ timeout: 5000 });
  await expect(fab).toBeInViewport();
  ```

## Test Execution Plan

### Phase 1: P0 Fixes (Mock Data Loading)
1. Update `applications-desktop.spec.ts` with wait strategies
2. Update `applications-mobile.spec.ts` with wait strategies
3. Update `navigation.spec.ts` with wait strategies
4. Add debug logging to `mocks.ts`
5. Run applications test suite: `pnpm exec playwright test applications`
6. **Success criteria:** All 10 TC-APP-DESK tests pass

### Phase 2: P1 Fixes (Carbon Design System)
1. Update `theme.ts` with Carbon tokens
2. Update `index.css` with global overrides
3. Update Header and MobileNav components
4. Run Carbon test suite: `pnpm exec playwright test carbon-design`
5. **Success criteria:** All 14 TC-CARBON-DESK tests pass

### Phase 3: P2 Fixes (Component Interactions)
1. Add animation waits to `node-editor-enhanced.spec.ts`
2. Verify drawer scroll bounds in `WorkflowDrawer`
3. Add FAB visibility checks to `canvas-mobile.spec.ts`
4. Run affected test suites
5. **Success criteria:** All P2 tests pass

### Phase 4: Full Regression
1. Run complete E2E suite: `pnpm test:e2e`
2. Generate HTML report
3. Verify pass rate ≥90% (96/106 tests)
4. Document any remaining failures

## Test Environment

- **UAT Frontend:** https://workflow-ui-gamma.vercel.app
- **Playwright version:** 1.59.1
- **Browser:** Chromium headless
- **Viewports:**
  - Desktop: 1280×720
  - Mobile: 390×844 (iPhone 12/13 Pro)

## Success Criteria

- ✅ Pass rate ≥90% (96/106 tests)
- ✅ All P0 tests passing (applications-desktop suite)
- ✅ All P1 tests passing (carbon-design-desktop suite)
- ✅ P2 tests passing or documented
- ✅ Test suite runtime <7 minutes
- ✅ HTML report generated with screenshots

## References

- PM doc: `pm-doc-v33.0.md`
- Arch doc: `arch-doc-v33.0.md`
- Prior E2E report: `e2e-test-report-v30.0.md`
- TODO item: Section 2.1 in `TODO.md`
