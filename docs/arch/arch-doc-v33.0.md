# Architecture Document v33.0 — E2E Test Failure Remediation

**Document version:** 33.0  
**Date:** 2026-04-12  
**Status:** Approved  
**Recognition label:** `TODO-e2e-fix-test-failures-post-v30`

## Technical Approach

### P0: Mock Data Loading Fixes

**Root Cause Analysis:**
1. Tests navigate to `/workflows/` but don't wait for content to load
2. Mock route handlers in `e2e/mocks.ts` may not match actual API endpoints
3. No explicit wait for table/card rendering after navigation

**Solution:**
1. Add `waitForLoadState('networkidle')` after all `page.goto()` calls
2. Add explicit content selectors: `await page.waitForSelector('table, .ant-flex')`
3. Verify mock route patterns match actual API endpoints from backend
4. Add debug logging to `setupMocks()` to track route matches

**Files to modify:**
- `workflow-ui/e2e/applications-desktop.spec.ts` — add wait strategies
- `workflow-ui/e2e/applications-mobile.spec.ts` — add wait strategies
- `workflow-ui/e2e/navigation.spec.ts` — add wait strategies
- `workflow-ui/e2e/mocks.ts` — verify route patterns, add debug logging

**Implementation pattern:**
```typescript
test.beforeEach(async ({ page }) => {
  await setupMocks(page);
  await page.goto('/workflows/');
  await page.waitForLoadState('networkidle');
  // Wait for content to render
  await page.waitForSelector('table, .ant-flex', { timeout: 10000 });
});
```

### P1: Carbon Design System Compliance

**Root Cause Analysis:**
1. Navigation bar not using Carbon Gray 100 (#161616)
2. Navigation bar height < 48px (Carbon shell spec requires ≥48px)
3. Primary buttons not meeting 40×44px minimum touch target
4. Colors not using `--cds-*` CSS custom properties
5. Border radius not set to 0px (Ant Design default is 2px)

**Solution:**
1. Update theme configuration to override Ant Design defaults
2. Apply Carbon tokens via CSS custom properties
3. Add explicit height/width constraints to nav and buttons
4. Create global CSS overrides for Ant Design components

**Files to modify:**
- `workflow-ui/src/theme.ts` — add Carbon token overrides
- `workflow-ui/src/index.css` — add global Carbon overrides
- `workflow-ui/src/components/layout/Header.tsx` — enforce nav height
- `workflow-ui/src/components/layout/MobileNav.tsx` — enforce nav height

**Carbon Design System Specifications:**
- **Colors:**
  - Primary: Blue 60 (#0f62fe)
  - Nav background: Gray 100 (#161616)
  - Text on dark: Gray 10 (#f4f4f4)
- **Sizing:**
  - Nav height: ≥48px
  - Button height: ≥40px
  - Button width: ≥44px (touch target)
- **Border radius:** 0px (sharp corners)

**Implementation pattern:**
```typescript
// theme.ts
export const theme = {
  token: {
    colorPrimary: '#0f62fe', // Carbon Blue 60
    borderRadius: 0, // Carbon spec
    // Override Ant Design defaults
  },
  components: {
    Layout: {
      headerBg: '#161616', // Carbon Gray 100
      headerHeight: 48, // Carbon shell spec
    },
    Button: {
      controlHeight: 40,
      controlHeightLG: 48,
    },
  },
};
```

### P2: Component Interaction Stability

**Root Cause Analysis:**
1. Drawer animations not completing before assertions
2. Mobile drawer scroll calculations incorrect
3. FAB visibility timing issues

**Solution:**
1. Add explicit animation wait: `await page.waitForTimeout(300)` after drawer open
2. Verify drawer `maxHeight` calculation includes safe area insets
3. Add FAB visibility check with retry logic

**Files to modify:**
- `workflow-ui/e2e/node-editor-enhanced.spec.ts` — add animation waits
- `workflow-ui/e2e/canvas-mobile.spec.ts` — add FAB visibility checks
- `workflow-ui/src/routes/workflows/-components/workflow-drawer/index.tsx` — verify scroll bounds

## Data Flow

### Mock Data Flow
```
Test starts
  → setupMocks(page) registers route handlers
  → page.goto('/workflows/')
  → Browser requests /workflow/entity-setting
  → Mock handler intercepts and returns MOCK_APPS
  → React renders table/cards
  → Test assertions run
```

### Carbon Design System Application
```
App loads
  → theme.ts provides ConfigProvider theme
  → Ant Design components apply theme tokens
  → Global CSS overrides apply Carbon-specific rules
  → Components render with Carbon styling
```

## Security Considerations

No security implications — this is test infrastructure and styling fixes only.

## Performance Considerations

- Adding `waitForLoadState('networkidle')` may increase test runtime by 1-2s per test
- Total suite runtime expected to increase from ~5min to ~6min
- This is acceptable for reliability improvement

## Testing Strategy

1. **Unit testing:** Not applicable (E2E test fixes)
2. **Integration testing:** Run full E2E suite after each fix category
3. **Manual testing:** Verify Carbon styling in browser at https://workflow-ui-gamma.vercel.app

## Rollback Plan

All changes are in test files or styling — no backend changes. Rollback is simple:
```bash
git revert <commit-sha>
```

## Success Metrics

- **Pass rate 100% (106/106 tests)**
- All P0 tests passing (applications-desktop suite)
- All P1 tests passing (carbon-design-desktop suite)
- All P2 tests passing (component interaction suite)
- Test suite runtime <7 minutes
- Zero flaky tests across 3 consecutive runs

## References

- Carbon Design System: https://carbondesignsystem.com/
- Playwright best practices: https://playwright.dev/docs/best-practices
- Prior E2E report: `e2e-test-report-v30.0.md`
