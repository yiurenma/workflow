# Test Document v27.0 — UI Design: Quiet Luxury Visual Refactoring

**Document version:** 27.0  
**Date:** 2026-04-11  
**Status:** Draft  
**TODO label:** `TODO-ui-design-quiet-luxury-refactoring-frontend-skill`  
**References:** `pm-doc-v27.0.md`, `arch-doc-v27.0.md`

---

## Test Scope

Visual-only refactoring. All tests verify:
1. New design system is applied correctly
2. No functional regressions
3. WCAG AA accessibility maintained
4. Build succeeds

---

## Test Cases

### TC-QL-01 — Build succeeds after refactoring

**Type:** Build  
**Priority:** P0 (blocker)

**Steps:**
1. Run `npm run build` in `workflow-ui/`

**Expected:** Exit code 0, no TypeScript errors, no CSS errors.

---

### TC-QL-02 — Google Fonts load correctly

**Type:** Visual / Network  
**Priority:** P1

**Steps:**
1. Open http://localhost:5173/workflows in browser
2. Open DevTools → Network → filter by `fonts.googleapis.com`
3. Inspect page heading "Applications"

**Expected:**
- Fonts request for Lora + DM Sans returns 200
- "Applications" heading renders in Lora serif font (visually distinct from Inter)
- Body text renders in DM Sans

---

### TC-QL-03 — Navigation header: warm dark background + gold accent

**Type:** Visual  
**Priority:** P1

**Steps:**
1. Open any page
2. Inspect the top navigation header

**Expected:**
- Header background is warm dark charcoal (`#2A2520`), not cold zinc-900 (`#18181B`)
- "Workflow Studio" logo text renders in Lora, warm off-white (`#E8E0D5`)
- Active nav item or logo accent uses warm gold (`#C9A87C`)
- No harsh cold black tones

---

### TC-QL-04 — Application list: warm cream background

**Type:** Visual  
**Priority:** P1

**Steps:**
1. Navigate to `/workflows`
2. Inspect page background and table

**Expected:**
- Page background is warm cream (`#F9F7F4`), not stark `#FAFAFA`
- Table header background is warm sand (`#F3F0EB`)
- Table borders use warm gray (`#DDD8D0`), not cold zinc
- Table row hover shows mint tint (`#EEF5F0`)

---

### TC-QL-05 — Application list: typography hierarchy

**Type:** Visual  
**Priority:** P1

**Steps:**
1. Navigate to `/workflows`
2. Inspect "Applications" heading and subtitle

**Expected:**
- "Applications" heading renders in Lora serif, ~26px, warm charcoal (`#2A2520`)
- Subtitle "Manage and configure workflow applications" renders in DM Sans, warm stone (`#6B6560`)
- Column headers are uppercase, 11px, warm stone (`#6B6560`)

---

### TC-QL-06 — Status tags: soft earth tones

**Type:** Visual  
**Priority:** P1

**Steps:**
1. Navigate to `/workflows`
2. Inspect "Active" and "Inactive" status tags

**Expected:**
- "Active" tag: mint background (`#EAF3EE`), sage text (`#3D6B52`), sage border (`#A8CCBA`)
- "Inactive" tag: sand background (`#F3F0EB`), muted text (`#9C9690`), warm border (`#DDD8D0`)
- No bright green Ant Design default color

---

### TC-QL-07 — Primary button: terracotta color

**Type:** Visual  
**Priority:** P1

**Steps:**
1. Navigate to `/workflows`
2. Inspect "New application" button

**Expected:**
- Button background is terracotta (`#7C4A3A`), not indigo (`#4F46E5`)
- Button text is legible (contrast ≥ 4.5:1)
- Hover state shows lighter terracotta (`#9A5C49`)

---

### TC-QL-08 — Mobile card view: warm styling

**Type:** Visual / Responsive  
**Priority:** P1

**Steps:**
1. Open `/workflows` at 390px viewport width
2. Inspect application cards

**Expected:**
- Card background is white (`#FFFFFF`) on cream page background (`#F9F7F4`)
- Card border uses warm gray (`#DDD8D0`)
- Card shadow uses warm tone (`rgba(42,37,32,0.08)`)
- Application name in warm charcoal, description in warm stone

---

### TC-QL-09 — WCAG AA contrast: primary text

**Type:** Accessibility  
**Priority:** P0 (blocker)

**Steps:**
1. Use browser DevTools accessibility checker or axe extension
2. Check contrast of primary text (`#2A2520`) on cream background (`#F9F7F4`)

**Expected:** Contrast ratio ≥ 4.5:1 (actual: ~12.4:1 ✅)

---

### TC-QL-10 — WCAG AA contrast: secondary text

**Type:** Accessibility  
**Priority:** P0 (blocker)

**Steps:**
1. Check contrast of secondary text (`#6B6560`) on cream background (`#F9F7F4`)

**Expected:** Contrast ratio ≥ 4.5:1 (actual: ~5.1:1 ✅)

---

### TC-QL-11 — WCAG AA contrast: header text

**Type:** Accessibility  
**Priority:** P0 (blocker)

**Steps:**
1. Check contrast of header text (`#E8E0D5`) on header background (`#2A2520`)

**Expected:** Contrast ratio ≥ 4.5:1 (actual: ~8.7:1 ✅)

---

### TC-QL-12 — No functional regression: create application

**Type:** Functional regression  
**Priority:** P0 (blocker)

**Steps:**
1. Navigate to `/workflows`
2. Click "New application"
3. Fill in application name
4. Submit

**Expected:** Dialog opens, form submits, new application appears in list. Identical behavior to pre-refactoring.

---

### TC-QL-13 — No functional regression: open canvas

**Type:** Functional regression  
**Priority:** P0 (blocker)

**Steps:**
1. Navigate to `/workflows`
2. Click "Open" on any application

**Expected:** Canvas loads correctly with all nodes and edges visible.

---

### TC-QL-14 — No functional regression: node editor drawer

**Type:** Functional regression  
**Priority:** P0 (blocker)

**Steps:**
1. Open a canvas
2. Click any node
3. Edit a field
4. Save

**Expected:** Drawer opens, fields are editable, save works. `aria-label="Node Configuration"` preserved.

---

### TC-QL-15 — Canvas nodes: warm accent colors

**Type:** Visual  
**Priority:** P2

**Steps:**
1. Open a canvas with multiple node types
2. Inspect node header colors

**Expected:**
- Begin node: terracotta accent (`#7C4A3A`)
- Consumer node: sage accent (`#3D6B52`)
- Function node: slate accent (`#5C6B7C`)
- IfElse node: warm gold accent (`#9A7C3A`)
- No cold indigo/purple/blue defaults

---

### TC-QL-16 — Transition smoothness

**Type:** Visual / Interaction  
**Priority:** P2

**Steps:**
1. Hover over table rows, buttons, nav items
2. Open/close a drawer or modal

**Expected:**
- Hover transitions are smooth, ~150-200ms
- Drawer open/close is smooth, ~250ms
- No jarring instant state changes

---

### TC-QL-17 — Records page: consistent warm styling

**Type:** Visual  
**Priority:** P2

**Steps:**
1. Navigate to `/records`
2. Inspect page background, table, and typography

**Expected:** Same warm cream background, DM Sans body font, warm border colors as application list.

---

### TC-QL-18 — Visual comparison: before vs after screenshots

**Type:** Visual documentation  
**Priority:** P1

**Steps:**
1. Capture screenshots of UAT environment (before): application list desktop, mobile, canvas
2. Capture screenshots of local dev (after): same views
3. Document in `STYLE_REFACTOR_LOG.md`

**Expected:** Clear visual improvement visible — warmer palette, distinctive typography, refined spacing.

---

## Test Execution Notes

- TC-QL-01 through TC-QL-11 must all pass before UAT
- TC-QL-12 through TC-QL-14 are functional regression blockers — any failure stops UAT
- TC-QL-15 through TC-QL-18 are P2 — failures become new TODO items, do not block UAT
- Visual tests (TC-QL-02 through TC-QL-08, TC-QL-15 through TC-QL-18) require Playwright screenshots for documentation

---

## UAT Script (for human)

After QA and E2E pass, Test Manager will provide a step-by-step UAT script for the human to execute at https://workflow-ui-gamma.vercel.app/workflows covering:

1. Visual inspection of navigation header (warm dark, gold accent, Lora font)
2. Visual inspection of application list (cream background, warm table, terracotta button)
3. Status tag colors (mint active, sand inactive)
4. Open canvas — verify node colors and canvas styling
5. Open node editor drawer — verify warm background, preserved functionality
6. Confirm no functional regressions on core paths (create, open, save)
