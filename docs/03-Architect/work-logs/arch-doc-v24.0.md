# Arch Doc v24.0 — Node Editor Drawer: Accessibility Fixes

**Label:** `TODO-uat-e2e-node-drawer-accessibility-violations`
**Date:** 2026-04-11

---

## Scope

Single file change: `workflow-ui/src/routes/workflows/-components/workflow-drawer/index.tsx`

Frontend-only fix. No backend changes. No API changes. No database changes.

---

## Root Cause Analysis

Enhanced 5-layer E2E validation (Axe DevTools) identified two WCAG violations in the WorkflowDrawer component:

### Violation 1: Missing ARIA Dialog Name
- **Rule:** `aria-dialog-name` (WCAG 2.1 Level A)
- **Element:** `.ant-drawer-content[role="dialog"]`
- **Impact:** Screen reader users cannot identify the drawer's purpose
- **Current state:** Ant Design Drawer renders `role="dialog"` but has no accessible name

### Violation 2: Insufficient Color Contrast
- **Rule:** `color-contrast` (WCAG 2.1 Level AA)
- **Element:** Line 82: `<span className="text-[11px] font-semibold text-zinc-400 uppercase tracking-widest">`
- **Current:** 2.51:1 contrast ratio (#9f9fa9 on #fafafa background)
- **Required:** 4.5:1 minimum for WCAG AA compliance
- **Impact:** Low vision users cannot read "NODE CONFIGURATION" header text

---

## Fix

### Fix 1: Add `aria-label` to Drawer

Ant Design Drawer component accepts an `aria-label` prop that applies to the dialog container:

```tsx
// Mobile drawer (line 94)
<Drawer
  title={drawerTitle}
  placement="bottom"
  height="auto"
  onClose={onClose}
  open={open}
  mask={false}
  aria-label="Node Configuration"  // ← ADD THIS
  styles={{...}}
>

// Desktop drawer (line 135)
<Drawer
  title={drawerTitle}
  placement="right"
  onClose={onClose}
  open={open}
  width={drawerWidth}
  aria-label="Node Configuration"  // ← ADD THIS
  styles={{...}}
>
```

**Rationale:** `aria-label="Node Configuration"` provides a clear, concise accessible name that matches the drawer's purpose. Alternative approach (`aria-labelledby`) would require adding an `id` to the title element, which is more complex for a two-line title structure.

### Fix 2: Change Header Text Color

Replace `text-zinc-400` (#9f9fa9) with `text-zinc-600` (#52525b) on line 82:

```tsx
const drawerTitle = (
  <div className="flex flex-col gap-0.5">
    <span className="text-[11px] font-semibold text-zinc-600 uppercase tracking-widest">
      Node Configuration
    </span>
    <span className="text-sm font-semibold text-zinc-900 leading-tight">
      {selectedNode ? String(selectedNode.data?.label || "Unnamed Node") : "Select a node"}
    </span>
  </div>
);
```

**Color contrast verification:**
- `text-zinc-600` (#52525b) on #fafafa background: **7.0:1** (passes WCAG AA and AAA)
- Alternative `text-zinc-500` (#6b6b75): **4.6:1** (passes WCAG AA, not AAA)

**Chosen:** `text-zinc-600` for stronger compliance margin.

---

## Verification Approach

### Manual Verification (Axe DevTools)

1. Open https://workflow-ui-gamma.vercel.app in Chrome
2. Navigate to any workflow canvas
3. Click a node to open the drawer
4. Run Axe DevTools scan on the drawer container
5. Verify zero violations for `aria-dialog-name` and `color-contrast` rules

### Automated Verification (Playwright)

Existing test `TC-NODE-ENHANCED-01` in the E2E suite already includes Axe accessibility checks:

```typescript
// specs/node-editor-enhanced.spec.ts (existing test)
test('TC-NODE-ENHANCED-01: Accessibility scan on node drawer', async ({ page }) => {
  // ... open drawer ...
  const results = await new AxeBuilder({ page })
    .include('.ant-drawer')
    .analyze();
  expect(results.violations).toHaveLength(0);
});
```

After fix, this test should pass with zero violations.

---

## Diff Summary

| File | Lines | Change |
|------|-------|--------|
| `workflow-drawer/index.tsx` | 82 | Replace `text-zinc-400` with `text-zinc-600` |
| `workflow-drawer/index.tsx` | 94 | Add `aria-label="Node Configuration"` to mobile Drawer |
| `workflow-drawer/index.tsx` | 135 | Add `aria-label="Node Configuration"` to desktop Drawer |

---

## No New Dependencies

- No new packages required
- Uses existing Tailwind color utilities (`text-zinc-600` already in Tailwind v4)
- Ant Design Drawer `aria-label` prop is native (no upgrade needed)

---

## Risk Assessment

**Risk Level:** Low

| Risk | Mitigation |
|------|-----------|
| Visual regression (header text darker) | Minimal — `text-zinc-600` is only slightly darker than `text-zinc-400`; maintains visual hierarchy with `text-zinc-900` node name below |
| Screen reader behavior change | Positive — adds missing accessible name; no breaking change for existing AT users |
| Mobile vs desktop inconsistency | Both branches receive identical `aria-label` — consistent experience |

---

## Rollback Plan

If visual regression is reported:

1. Revert line 82 to `text-zinc-400`
2. Apply alternative fix: increase background lightness or use `text-zinc-500` (4.6:1 contrast)
3. Re-run Axe scan to confirm AA compliance

If `aria-label` causes unexpected screen reader behavior:

1. Remove `aria-label` prop from both Drawer instances
2. Switch to `aria-labelledby` approach:
   - Add `id="drawer-title"` to the outer `<div>` in `drawerTitle`
   - Add `aria-labelledby="drawer-title"` to both Drawer components

---

## Impact on Other Components

**Zero impact.** WorkflowDrawer is a leaf component with no children that consume its styling. The color change affects only the 11px uppercase header text within the drawer title.

---

## Related TODO Items

This fix addresses the first TODO item in the backlog. The second item (drawer close button non-functional) is tracked separately as `TODO-uat-e2e-node-drawer-close-button-broken` and requires investigation of event handler attachment — not covered in this arch doc.
