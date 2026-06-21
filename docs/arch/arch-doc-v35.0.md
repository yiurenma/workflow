# Architecture Document — v35.0

**Document version:** 35.0  
**Date:** 2026-04-13  
**Status:** Draft  
**TODO item:** `TODO-ui-ibm-carbon-audit-residual-styling`  
**Related PM doc:** pm-doc-v35.0.md

---

## 1. Overview

This document outlines the technical approach for auditing and fixing IBM Carbon Design Language compliance gaps in Workflow Studio UI, following the v28.0 baseline refactoring.

---

## 2. Architecture Context

### 2.1 Current State (v28.0 Baseline)

The v28.0 refactoring (`TODO-ui-design-ibm-design-language-refactoring`) established:

- **Theme system:** `theme.ts` with Carbon design tokens (`--cds-*`)
- **Global styles:** `index.css` and `carbon-overrides.css` applying Carbon patterns
- **Typography:** IBM Plex Sans (primary) + IBM Plex Mono (code)
- **Color system:** Blue 60 `#0f62fe` primary, Gray 100 `#161616` navigation, Red 60 `#da1e28` danger
- **Component overrides:** Ant Design components styled with 0px border-radius, Carbon colors

### 2.2 Known Gaps

**Root causes identified:**

1. **Imperative modals:** `Modal.confirm()` and `Modal.method()` calls bypass theme context
2. **Portal components:** Dropdown, Tooltip, Popconfirm render outside theme provider
3. **Post-v28 additions:** Features added after v28 without Carbon review
4. **Inline styles:** Residual Tailwind utilities or inline style objects
5. **Test coverage:** Playwright tests lack Layer 5 (computed style) assertions for modals

---

## 3. Technical Approach

### 3.1 Audit Methodology

**Phase 1: Automated scanning**
```bash
# Find imperative modal calls
grep -r "Modal\.confirm\|Modal\.warning\|Modal\.error\|Modal\.success" workflow-ui/src/

# Find portal components without Carbon classes
grep -r "Dropdown\|Tooltip\|Popconfirm" workflow-ui/src/ | grep -v "className.*cds"

# Find residual Tailwind rounded utilities
grep -r "rounded-\|border-radius:" workflow-ui/src/
```

**Phase 2: Manual inspection**
- Navigate all routes in UAT environment (https://workflow-ui-gamma.vercel.app)
- Trigger all modal/confirmation flows (Delete, Copy, Settings, etc.)
- Inspect computed styles in browser DevTools
- Screenshot non-Carbon elements

**Phase 3: Gap documentation**
- Create numbered list with route, component, screenshot, severity
- Document root cause for each gap
- Prioritize: P0 (user-facing critical) > P1 (user-facing minor) > P2 (edge case)

---

### 3.2 Fix Strategies

#### Strategy A: Carbon Modal Wrapper (Imperative Modals)

**Problem:** `Modal.confirm()` bypasses theme context

**Solution:** Create Carbon-themed wrapper function

```typescript
// utils/carbonModal.ts
import { Modal } from 'antd';
import type { ModalFuncProps } from 'antd';

export const carbonConfirm = (config: ModalFuncProps) => {
  return Modal.confirm({
    ...config,
    className: 'carbon-modal',
    okButtonProps: {
      ...config.okButtonProps,
      className: 'carbon-button-primary',
      style: { height: '48px', borderRadius: 0 }
    },
    cancelButtonProps: {
      ...config.cancelButtonProps,
      className: 'carbon-button-secondary',
      style: { height: '48px', borderRadius: 0 }
    },
    // Apply Carbon modal styles
    style: {
      ...config.style,
      borderRadius: 0
    }
  });
};

export const carbonDangerConfirm = (config: ModalFuncProps) => {
  return carbonConfirm({
    ...config,
    okButtonProps: {
      ...config.okButtonProps,
      danger: true,
      className: 'carbon-button-danger'
    }
  });
};
```

**CSS additions to `carbon-overrides.css`:**
```css
.carbon-modal .ant-modal-content {
  border-radius: 0 !important;
  background: #ffffff;
}

.carbon-modal .ant-modal-header {
  border-radius: 0 !important;
  background: #ffffff;
  border-bottom: 1px solid #e0e0e0;
}

.carbon-modal .ant-modal-title {
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 20px;
  font-weight: 400;
  color: #161616;
}

.carbon-modal .ant-modal-body {
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 16px;
  color: #525252;
}

.carbon-button-primary {
  background: #0f62fe !important;
  border-color: #0f62fe !important;
  height: 48px !important;
  border-radius: 0 !important;
}

.carbon-button-danger {
  background: #da1e28 !important;
  border-color: #da1e28 !important;
  height: 48px !important;
  border-radius: 0 !important;
}

.carbon-button-secondary {
  background: #393939 !important;
  border-color: #393939 !important;
  color: #ffffff !important;
  height: 48px !important;
  border-radius: 0 !important;
}
```

**Migration example:**
```typescript
// Before
Modal.confirm({
  title: 'Delete Application',
  content: 'Are you sure you want to delete this application?',
  onOk: handleDelete
});

// After
carbonDangerConfirm({
  title: 'Delete Application',
  content: 'Are you sure you want to delete this application?',
  onOk: handleDelete
});
```

---

#### Strategy B: Portal Component Class Injection

**Problem:** Dropdown, Tooltip, Popconfirm render in portal without Carbon classes

**Solution:** Use `popupClassName` / `overlayClassName` props

```typescript
// Before
<Dropdown menu={{ items }}>
  <Button>Actions</Button>
</Dropdown>

// After
<Dropdown 
  menu={{ items }}
  overlayClassName="carbon-dropdown"
>
  <Button>Actions</Button>
</Dropdown>
```

**CSS additions:**
```css
.carbon-dropdown .ant-dropdown-menu {
  background: #f4f4f4;
  border: 1px solid #c6c6c6;
  border-radius: 0 !important;
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
}

.carbon-dropdown .ant-dropdown-menu-item {
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 14px;
  color: #161616;
  border-radius: 0 !important;
}

.carbon-dropdown .ant-dropdown-menu-item:hover {
  background: #e8e8e8;
}

.carbon-tooltip .ant-tooltip-inner {
  background: #f4f4f4;
  color: #161616;
  border: 1px solid #c6c6c6;
  border-radius: 0 !important;
  font-family: 'IBM Plex Sans', sans-serif;
  font-size: 14px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.3);
}

.carbon-tooltip .ant-tooltip-arrow::before {
  background: #f4f4f4;
  border: 1px solid #c6c6c6;
}
```

---

#### Strategy C: Declarative Modal Migration (Where Feasible)

**Problem:** Imperative modals hard to theme consistently

**Solution:** Migrate to declarative `<Modal>` components where state management allows

```typescript
// Before (imperative)
const handleDelete = () => {
  Modal.confirm({
    title: 'Delete Application',
    content: 'Are you sure?',
    onOk: async () => {
      await deleteApplication(id);
    }
  });
};

// After (declarative)
const [deleteModalOpen, setDeleteModalOpen] = useState(false);
const [deletingId, setDeletingId] = useState<string | null>(null);

const handleDelete = (id: string) => {
  setDeletingId(id);
  setDeleteModalOpen(true);
};

const confirmDelete = async () => {
  if (deletingId) {
    await deleteApplication(deletingId);
    setDeleteModalOpen(false);
    setDeletingId(null);
  }
};

// In JSX
<Modal
  open={deleteModalOpen}
  onCancel={() => setDeleteModalOpen(false)}
  onOk={confirmDelete}
  title="Delete Application"
  className="carbon-modal"
  okButtonProps={{ danger: true, className: 'carbon-button-danger' }}
  cancelButtonProps={{ className: 'carbon-button-secondary' }}
>
  Are you sure you want to delete this application?
</Modal>
```

---

### 3.3 Test Coverage Improvements

#### Layer 5 (Effect) Assertions for Modals

**Problem:** Existing Playwright tests use `toBeVisible()` but don't assert computed styles

**Solution:** Add Layer 5 checks for critical modals

```typescript
// Example: Application delete confirmation
test('TC-APP-DESK-11: Delete confirmation uses Carbon danger modal', async ({ page }) => {
  await page.goto('http://localhost:5173/workflows');
  await page.waitForLoadState('networkidle');
  
  // Trigger delete
  await page.getByRole('row').first().getByRole('button', { name: 'Delete' }).click();
  
  // Layer 1: Exists
  const modal = page.locator('.ant-modal');
  await expect(modal).toBeVisible();
  
  // Layer 2: Size
  const box = await modal.boundingBox();
  expect(box).toBeTruthy();
  expect(box!.width).toBeGreaterThan(400);
  
  // Layer 5: Effect (Carbon styles)
  const modalContent = modal.locator('.ant-modal-content');
  await expect(modalContent).toHaveCSS('border-radius', '0px');
  
  const okButton = modal.getByRole('button', { name: 'OK' });
  await expect(okButton).toHaveCSS('background-color', 'rgb(218, 30, 40)'); // Red 60
  await expect(okButton).toHaveCSS('height', '48px');
  await expect(okButton).toHaveCSS('border-radius', '0px');
  
  const title = modal.locator('.ant-modal-title');
  await expect(title).toHaveCSS('color', 'rgb(22, 22, 22)'); // Gray 100
  await expect(title).toHaveCSS('font-family', /IBM Plex Sans/);
});
```

#### Visual Regression Baselines

**Approach:** Capture screenshots of critical modals and compare in CI

```typescript
test('TC-APP-DESK-12: Delete modal visual baseline', async ({ page }) => {
  await page.goto('http://localhost:5173/workflows');
  await page.waitForLoadState('networkidle');
  
  await page.getByRole('row').first().getByRole('button', { name: 'Delete' }).click();
  
  const modal = page.locator('.ant-modal');
  await expect(modal).toBeVisible();
  
  // Visual baseline
  await expect(modal).toHaveScreenshot('delete-modal-carbon.png', {
    maxDiffPixels: 100
  });
});
```

---

## 4. Implementation Plan

### Phase 1: Audit (Day 1)
1. Run automated scans (grep patterns above)
2. Manual inspection of all routes in UAT
3. Document gaps in numbered list with screenshots
4. Prioritize: P0 > P1 > P2

### Phase 2: Fix P0 Gaps (Days 2-3)
1. Create `carbonModal.ts` wrapper utilities
2. Migrate all imperative modals to `carbonConfirm` / `carbonDangerConfirm`
3. Add `overlayClassName="carbon-dropdown"` to all Dropdown components
4. Add `overlayClassName="carbon-tooltip"` to all Tooltip components
5. Update `carbon-overrides.css` with new classes

### Phase 3: Fix P1 Gaps (Day 3)
1. Address portal components in less-critical flows
2. Remove residual Tailwind rounded utilities
3. Fix inline styles

### Phase 4: Test Coverage (Day 4)
1. Add Layer 5 assertions to existing Playwright tests
2. Create new test cases for previously untested modal flows
3. Add visual baseline screenshots for critical modals

### Phase 5: QA + UAT (Day 5)
1. QA verification against test-doc-v35.0.md
2. E2E Tester runs full Playwright suite on UAT
3. Spot-check all fixed flows in UAT environment

---

## 5. Files to Modify

### New Files
- `workflow-ui/src/utils/carbonModal.ts` — Carbon modal wrapper utilities

### Modified Files
- `workflow-ui/src/styles/carbon-overrides.css` — Add modal/dropdown/tooltip Carbon classes
- `workflow-ui/src/routes/workflows/index.tsx` — Migrate delete confirmation
- `workflow-ui/src/routes/workflows/components/ApplicationTable.tsx` — Add overlayClassName to Dropdown
- `workflow-ui/src/routes/workflows/components/MobileApplicationCard.tsx` — Add overlayClassName to Dropdown
- `workflow-ui/e2e/applications-desktop.spec.ts` — Add Layer 5 assertions
- `workflow-ui/e2e/applications-mobile.spec.ts` — Add Layer 5 assertions

*(Full file list to be determined after audit)*

---

## 6. Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Breaking existing modal behavior | Test all modal flows manually before commit |
| CSS specificity conflicts | Use `!important` sparingly; prefer higher specificity selectors |
| Portal components still bypass theme | Verify `overlayClassName` works in all Ant Design versions |
| Playwright tests become flaky | Use stable wait strategies from v30.0 framework |
| Visual baselines fail in CI | Set reasonable `maxDiffPixels` threshold |

---

## 7. Success Criteria

- All P0 and P1 gaps resolved
- All imperative modals use `carbonConfirm` / `carbonDangerConfirm`
- All portal components have `overlayClassName` / `popupClassName`
- Playwright tests include Layer 5 assertions for critical modals
- UAT spot-check confirms Carbon compliance
- No functional regressions

---

## 8. Dependencies

- v28.0 IBM Carbon baseline
- v30.0 five-layer validation framework
- Ant Design 5.x API for `overlayClassName` / `popupClassName`

---

**End of Architecture Document v35.0**
