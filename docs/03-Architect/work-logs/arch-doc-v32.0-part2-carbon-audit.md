# Architecture — Workflow Studio Unified Program Part 2: IBM Carbon Audit (v32.0)

**Document version:** 32.0 Part 2  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** IBM Carbon residual styling audit + Playwright blind spots  
**Repository:** `workflow-ui`

---

## 1. Technical Context

### 1.1 v28.0 IBM Carbon Refactor Baseline

**Shipped:** `TODO-ui-design-ibm-design-language-refactoring`  
**Docs:** `arch-doc-v28.0.md`, `pm-doc-v28.0.md`, `test-doc-v28.0.md`

**Changes applied:**
- Fonts: IBM Plex Sans (body), IBM Plex Mono (code)
- Primary color: Blue 60 `#0f62fe`
- Navigation: Gray 100 `#161616`
- Border-radius: `0px` (Carbon sharp corners)
- Token system: `--cds-*` CSS variables
- Button styles: Carbon primary/secondary/danger variants

**Known gaps from v28.0:**
- Some imperative modals (`Modal.confirm`) bypass themed wrapper
- Dropdowns/tooltips in portals may lack Carbon classes
- Features added after v28 (e.g., new modals) may not follow Carbon

---

## 2. Audit Methodology

### 2.1 Surface Inventory

**Step 1:** List all user-facing surfaces by route:

| Route | Surfaces |
|-------|----------|
| `/workflows` | Applications table, mobile cards, New Application modal, Settings modal, History modal, Copy modal, Delete confirm, overflow menu |
| `/workflows/:name` | Canvas toolbar, node palette, WorkflowDrawer, JsonPath modal, Explain modal, Generate modal, Straighten button, Run button |
| `/records` | Records table, mobile cards, filters, pagination |
| Global | Toasts, empty states, loading spinners |

**Step 2:** For each surface, capture:
- Screenshot (Desktop 1280px + Mobile 390×844 if applicable)
- Component file path
- Current styling (border-radius, colors, fonts, spacing)
- Carbon compliance status (PASS / FAIL)

---

### 2.2 Gap Classification

**Category A: Imperative Modal.confirm**

**Symptom:** `Modal.confirm({ title, content, onOk })` bypasses themed wrapper.

**Example:**
```tsx
// Non-Carbon (imperative)
Modal.confirm({
  title: 'Delete Application',
  content: 'Are you sure?',
  onOk: handleDelete,
});
```

**Fix:** Replace with declarative `<Modal>` using Carbon classes:
```tsx
// Carbon-compliant (declarative)
<Modal
  open={deleteModalOpen}
  onCancel={() => setDeleteModalOpen(false)}
  onOk={handleDelete}
  className="carbon-modal"
  title="Delete Application"
>
  <p className="text-[--cds-text-primary]">Are you sure?</p>
</Modal>
```

**Carbon modal CSS:**
```css
.carbon-modal .ant-modal-content {
  border-radius: 0;
  background-color: var(--cds-layer-01);
}

.carbon-modal .ant-modal-header {
  border-radius: 0;
  background-color: var(--cds-layer-01);
  border-bottom: 1px solid var(--cds-border-subtle-01);
}

.carbon-modal .ant-btn-primary {
  background-color: #0f62fe; /* Blue 60 */
  border-radius: 0;
}

.carbon-modal .ant-btn-dangerous {
  background-color: #da1e28; /* Red 60 */
  border-radius: 0;
}
```

---

**Category B: Portal components without Carbon classes**

**Symptom:** `Dropdown`, `Tooltip`, `Popconfirm` render in portal; miss Carbon styling.

**Example:**
```tsx
// Non-Carbon
<Dropdown menu={{ items }} trigger={['click']}>
  <Button>⋯</Button>
</Dropdown>
```

**Fix:** Add `overlayClassName` or wrap in Carbon-themed provider:
```tsx
// Carbon-compliant
<Dropdown
  menu={{ items }}
  trigger={['click']}
  overlayClassName="carbon-dropdown"
>
  <Button>⋯</Button>
</Dropdown>
```

**Carbon dropdown CSS:**
```css
.carbon-dropdown .ant-dropdown-menu {
  border-radius: 0;
  background-color: var(--cds-layer-01);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.carbon-dropdown .ant-dropdown-menu-item {
  color: var(--cds-text-primary);
}

.carbon-dropdown .ant-dropdown-menu-item:hover {
  background-color: var(--cds-layer-hover-01);
}
```

---

**Category C: Residual Tailwind or inline styles**

**Symptom:** Hardcoded colors, spacing, or border-radius not using Carbon tokens.

**Example:**
```tsx
// Non-Carbon
<div className="rounded-lg bg-blue-500 p-4">
  Content
</div>
```

**Fix:** Replace with Carbon tokens:
```tsx
// Carbon-compliant
<div className="bg-[--cds-layer-01] p-[--cds-spacing-05]" style={{ borderRadius: 0 }}>
  Content
</div>
```

---

**Category D: Features added after v28**

**Symptom:** New modals, buttons, or forms added after v28.0 without Carbon review.

**Fix:** Apply Carbon patterns from the start; review all PRs merged after v28.0 for Carbon compliance.

---

### 2.3 Audit Execution

**Tool:** Manual inspection + browser DevTools

**Process:**
1. Navigate to each route on UAT (`https://workflow-ui-gamma.vercel.app`)
2. Open each modal, dropdown, tooltip
3. Inspect computed styles:
   - `border-radius` should be `0px`
   - `background-color` should use `--cds-*` tokens or Carbon hex values
   - `font-family` should be `IBM Plex Sans` or `IBM Plex Mono`
4. Screenshot non-compliant surfaces
5. Record in gap list with severity:
   - **HIGH:** User-facing critical path (Delete confirm, Settings modal)
   - **MEDIUM:** Secondary flows (History modal, Copy modal)
   - **LOW:** Edge cases (empty states, rare tooltips)

---

## 3. Remediation Plan

### Phase 1: Audit (2-3 hours)

1. Inventory all surfaces (see table above)
2. Capture screenshots + component file paths
3. Classify gaps by category (A/B/C/D)
4. Produce numbered gap list in `docs/carbon-audit-gaps-v32.0.md`

---

### Phase 2: Fix HIGH severity gaps (4-6 hours)

1. Applications Delete confirm (imperative → declarative)
2. Settings modal (verify Carbon classes)
3. History modal (verify Carbon classes)
4. Canvas modals (JsonPath, Explain, Generate)

---

### Phase 3: Fix MEDIUM/LOW severity gaps (2-4 hours)

1. Copy modal
2. Overflow menus (Applications, Records)
3. Toasts / empty states

---

### Phase 4: Playwright validation (2-3 hours)

1. Add Layer 5 assertions for critical modals:
   - Delete confirm: `border-radius: 0px`, `background-color: #0f62fe`
   - Settings modal: `font-family: IBM Plex Sans`
2. Document which v28-era tests lacked Carbon assertions
3. Optional: add visual regression tests (`toHaveScreenshot()`) for stable surfaces

---

## 4. Testing Strategy

### 4.1 Manual Testing

- Desktop Chrome 1280px: Open all modals, verify Carbon styling
- Mobile Chrome 390×844: Open all modals, verify Carbon styling
- Spot-check: Applications Delete, Settings, History, JsonPath, Explain, Generate

### 4.2 Playwright Testing

- Add Layer 5 assertions to existing test cases (see Phase 4 above)
- Run full E2E suite; verify no regressions
- Document Carbon validation coverage in `ui-test-report-v32.0-part2.md`

---

## 5. Risks & Mitigations

**Risk:** Fixing imperative modals breaks existing behavior.  
**Mitigation:** Test all modal paths (OK, Cancel, X button, outside click, ESC key).

**Risk:** Carbon classes conflict with Ant Design defaults.  
**Mitigation:** Use `!important` sparingly; prefer CSS specificity or CSS-in-JS overrides.

**Risk:** Audit misses surfaces added in parallel PRs.  
**Mitigation:** Review all PRs merged after audit start date; add to gap list if non-compliant.

---

## 6. Deployment

- **Branch:** `main` (workflow-ui)
- **Merge strategy:** One commit per gap category (A/B/C/D) for easier rollback
- **Rollback plan:** Revert individual commits if regressions detected

---

## 7. References

- IBM Carbon Design System: https://carbondesignsystem.com/
- Carbon Design Tokens: https://carbondesignsystem.com/guidelines/color/tokens
- Ant Design Modal API: https://ant.design/components/modal
- `arch-doc-v28.0.md` — v28 IBM Carbon refactor baseline
