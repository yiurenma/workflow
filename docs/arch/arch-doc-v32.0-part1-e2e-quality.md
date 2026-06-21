# Architecture — Workflow Studio Unified Program Part 1: E2E Quality (v32.0)

**Document version:** 32.0 Part 1  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** E2E drawer close fix + 5-layer validation retrofit  
**Repositories:** `workflow-ui`, `workflow-agent-teams` (test specs)

---

## 1. Technical Context

### 1.1 Drawer Close Defect

**Symptom:** Clicking `.ant-drawer-close` does not close the node editor drawer; drawer stays open.

**Evidence:** TC-NODE-ENHANCED-05 (Layer 5) failure in `specs/uat-report-e2e-pass3.md`.

**Investigation hints:**
- Event handler not bound to close button
- Event propagation stopped somewhere in component tree
- Ant Design `destroyOnClose` / `open` state mismatch
- Z-index or portal overlay blocking hit target
- ReactFlow capturing pointer events

**Root cause analysis required:** Use React DevTools + browser inspector to trace event flow.

---

## 2. Solution Design

### 2.1 Drawer Close Fix

**File:** `workflow-ui/src/components/WorkflowDrawer.tsx` (or equivalent drawer component)

**Approach:**

1. **Verify event binding:** Ensure `onClose` prop passed to `<Drawer>` component.

2. **Check state management:** Confirm `open` state controlled by parent; `onClose` callback updates state correctly.

3. **Portal/z-index audit:** Verify drawer portal not blocked by overlays; close button has correct z-index.

4. **ReactFlow interaction:** If ReactFlow canvas captures pointer events, ensure drawer portal rendered outside ReactFlow container or has higher stacking context.

5. **Ant Design props review:**
   - `destroyOnClose={true}` — ensures drawer removed from DOM on close
   - `maskClosable={true}` — allows clicking outside to close (optional, not primary fix)
   - `keyboard={true}` — allows ESC to close (desktop only)

**Example fix pattern:**

```tsx
// WorkflowDrawer.tsx
const [open, setOpen] = useState(false);

<Drawer
  open={open}
  onClose={() => setOpen(false)} // Ensure this callback fires
  destroyOnClose={true}
  placement="right"
  className="workflow-drawer"
>
  {/* drawer content */}
</Drawer>
```

**Testing:**
- Manual: Click close button on Desktop Chrome 1280px + Mobile Chrome 390×844
- Playwright: Add assertion that drawer absent from DOM after close click

---

### 2.2 Five-Layer Validation Framework

**Layers:**

1. **Exist:** `await expect(locator).toBeVisible()` or `toBeAttached()`
2. **Size:** `const box = await locator.boundingBox(); expect(box.height).toBeGreaterThan(minHeight);`
3. **Viewport:** `await expect(locator).toBeInViewport()`
4. **Interact:** `await locator.click(); await expect(resultLocator).toBeVisible();`
5. **Effect:** 
   - Computed styles: `const color = await locator.evaluate(el => getComputedStyle(el).backgroundColor);`
   - Screenshot: `await expect(locator).toHaveScreenshot('modal.png');`
   - State change: `await expect(page.locator('.success-toast')).toBeVisible();`

**Retrofit strategy:**

1. **Audit existing specs:** Identify tests that only assert Layer 1 (exist) or Layer 4 (interact) without Layer 5 (effect).

2. **Prioritize mobile surfaces:** Drawers, modals, forms where Layer 2 (size) and Layer 3 (viewport) critical.

3. **Add Layer 5 assertions:** For each critical interaction, validate post-condition:
   - Modal open → check `role="dialog"` + `aria-label`
   - Form submit → check success message or navigation
   - Drawer close → check drawer absent from DOM

4. **Document coverage:** In `e2e-test-report-v32.0-part1.md`, list upgraded tests and remaining gaps.

**Example upgrade:**

```typescript
// Before (Layer 1 + 4 only)
test('TC-NODE-01: Open node editor', async ({ page }) => {
  await page.goto('/workflows/TEST_APP');
  await page.locator('.react-flow__node').first().click();
  await expect(page.locator('.ant-drawer')).toBeVisible(); // Layer 1
});

// After (Layer 1 + 2 + 3 + 4 + 5)
test('TC-NODE-01: Open node editor', async ({ page }) => {
  await page.goto('/workflows/TEST_APP');
  await page.locator('.react-flow__node').first().click();
  
  const drawer = page.locator('.ant-drawer');
  await expect(drawer).toBeVisible(); // Layer 1: Exist
  
  const box = await drawer.boundingBox();
  expect(box.height).toBeGreaterThan(viewport.height * 0.35); // Layer 2: Size
  
  await expect(drawer).toBeInViewport(); // Layer 3: Viewport
  
  // Layer 4: Interact (already covered by click above)
  
  // Layer 5: Effect - validate drawer content loaded
  await expect(drawer.locator('text=NODE CONFIGURATION')).toBeVisible();
  const headerColor = await drawer.locator('.drawer-header').evaluate(
    el => getComputedStyle(el).color
  );
  expect(headerColor).toBe('rgb(82, 82, 91)'); // text-zinc-600
});
```

---

## 3. Implementation Plan

### Phase 1: Drawer Close Fix (HIGH priority)

1. Investigate root cause using React DevTools + browser inspector
2. Implement fix in `WorkflowDrawer.tsx`
3. Manual test on Desktop + Mobile
4. Update or create Playwright test case (TC-NODE-ENHANCED-05 or new TC-DRAWER-CLOSE-01)
5. Verify no regressions in existing E2E suite

**Estimated effort:** 2-4 hours

---

### Phase 2: Five-Layer Validation Retrofit

1. Audit existing test specs (canvas.spec.ts, apps-mobile.spec.ts, node-editor.spec.ts)
2. Identify 10-15 high-priority test cases for upgrade
3. Add Layer 2/3/5 assertions to each
4. Run full suite; verify no new failures
5. Document coverage in `e2e-test-report-v32.0-part1.md`

**Estimated effort:** 4-6 hours

---

## 4. Testing Strategy

### 4.1 Manual Testing

- Desktop Chrome 1280px: Click drawer close button; verify drawer closes
- Mobile Chrome 390×844: Tap drawer close button; verify drawer closes
- Test across all node types (HTTP fetch, Safe HTTP fetch, Logic)

### 4.2 Playwright Testing

- Run full E2E suite after drawer close fix
- Verify TC-NODE-ENHANCED-05 (or new close test) passes
- Verify no regressions in existing 82/84 pass rate
- Run upgraded 5-layer tests; document pass/fail in report

---

## 5. Risks & Mitigations

**Risk:** Drawer close fix breaks other drawer interactions (e.g., clicking outside to close).  
**Mitigation:** Test all drawer close paths (button, outside click, ESC key).

**Risk:** Five-layer assertions too brittle; fail on minor style changes.  
**Mitigation:** Use computed styles for critical properties only (e.g., color, visibility); avoid pixel-perfect screenshot comparisons unless stable.

**Risk:** Retrofit introduces new test failures.  
**Mitigation:** Upgrade tests incrementally; run suite after each batch; roll back if failures not related to real bugs.

---

## 6. Deployment

- **Branch:** `main` (workflow-ui)
- **Merge strategy:** Squash commits for drawer close fix; separate commit for 5-layer retrofit
- **Rollback plan:** Revert drawer close commit if regressions detected

---

## 7. References

- Ant Design Drawer API: https://ant.design/components/drawer
- Playwright assertions: https://playwright.dev/docs/test-assertions
- `specs/uat-report-e2e-pass3.md` — UAT findings
- `docs/e2e-testing-guide.md` — E2E conventions
