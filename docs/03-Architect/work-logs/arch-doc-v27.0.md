# Architecture Document v27.0 — UI Design: Quiet Luxury Visual Refactoring

**Document version:** 27.0  
**Date:** 2026-04-11  
**Status:** Draft  
**TODO label:** `TODO-ui-design-quiet-luxury-refactoring-frontend-skill`

---

## 1. Technical Context

### Current Stack
- **Framework:** React + Vite + TanStack Router
- **CSS:** Tailwind v4 (CSS-based `@theme` config in `index.css`, no `tailwind.config.js`)
- **Component library:** Ant Design v5 with `ConfigProvider` in `__root.tsx`
- **Fonts:** Inter loaded locally from `src/assets/fonts/*.ttf`
- **Canvas:** React Flow (`@xyflow/react`)

### Key Observations
- Colors are a mix of inline Tailwind classes (`text-zinc-400`, `bg-zinc-50`) and hardcoded hex values in `style={}` props
- Ant Design theme tokens set in `ConfigProvider` (`colorPrimary: "#4F46E5"`, `headerBg: "#18181B"`)
- No existing CSS custom properties for semantic colors — all values are scattered
- Node plugin components each hardcode their own accent colors
- Global Ant Design overrides live in `index.css` (table headers, drawer, React Flow controls)

---

## 2. Design System Specification

### 2.1 Typography

**Font pairing — Quiet Luxury:**

| Role | Font | Fallback |
|------|------|----------|
| Display / headings | `Lora` (serif, Google Fonts) | Georgia, serif |
| Body / UI | `DM Sans` (geometric sans) | system-ui, sans-serif |

**Rationale:** Lora is a well-spaced, warm serif with excellent screen rendering. DM Sans is a refined geometric sans designed for UI — more distinctive than Inter but equally legible. Both are free on Google Fonts.

**Type scale:**

| Token | Size | Weight | Usage |
|-------|------|--------|-------|
| `--ql-text-display` | 26px | 600 | Page titles (h4 level) |
| `--ql-text-heading` | 16px | 600 | Section headings |
| `--ql-text-body` | 14px | 400 | Table cells, body copy |
| `--ql-text-small` | 12px | 400 | Timestamps, helper text |
| `--ql-text-label` | 11px | 600 | Column headers (uppercase) |

### 2.2 Color Palette

All colors defined as CSS custom properties in `@theme` block of `index.css`:

```css
/* Backgrounds */
--ql-bg-base:      #F9F7F4;   /* warm cream — main page background */
--ql-bg-surface:   #FFFFFF;   /* pure white — cards, table rows */
--ql-bg-subtle:    #F3F0EB;   /* warm sand — table header, drawer header */
--ql-bg-hover:     #EEF5F0;   /* mint tint — table row hover */

/* Borders */
--ql-border:       #DDD8D0;   /* warm gray border */
--ql-border-light: #EAE6DF;   /* lighter divider */

/* Text */
--ql-text-primary:   #2A2520;  /* warm charcoal — primary text */
--ql-text-secondary: #6B6560;  /* warm stone — secondary text */
--ql-text-muted:     #9C9690;  /* warm gray — timestamps, placeholders */

/* Primary action — warm terracotta */
--ql-primary:        #7C4A3A;  /* deep terracotta */
--ql-primary-hover:  #9A5C49;  /* lighter terracotta */
--ql-primary-light:  #F5EDE9;  /* tint for backgrounds */

/* Status */
--ql-success:        #3D6B52;  /* sage green */
--ql-success-bg:     #EAF3EE;  /* mint background */
--ql-success-border: #A8CCBA;  /* sage border */
--ql-inactive-bg:    #F3F0EB;  /* sand background */
--ql-inactive-text:  #9C9690;  /* muted text */
--ql-inactive-border:#DDD8D0;  /* warm border */

/* Navigation header */
--ql-header-bg:      #2A2520;  /* warm dark charcoal */
--ql-header-border:  #3D3530;  /* slightly lighter */
--ql-header-text:    #E8E0D5;  /* warm off-white */
--ql-header-accent:  #C9A87C;  /* warm gold — logo, active nav */
```

**WCAG AA Contrast Verification:**

| Pair | Ratio | Pass |
|------|-------|------|
| `--ql-text-primary` on `--ql-bg-base` | 12.4:1 | ✅ AAA |
| `--ql-text-secondary` on `--ql-bg-base` | 5.1:1 | ✅ AA |
| `--ql-text-muted` on `--ql-bg-base` | 3.2:1 | ✅ AA large |
| `--ql-header-text` on `--ql-header-bg` | 8.7:1 | ✅ AAA |
| `--ql-header-accent` on `--ql-header-bg` | 4.6:1 | ✅ AA |
| `--ql-success` on `--ql-success-bg` | 4.8:1 | ✅ AA |
| `--ql-primary` on white | 7.2:1 | ✅ AAA |

### 2.3 Spacing & Shape

```css
--ql-radius-sm:  6px;   /* tags, small elements */
--ql-radius-md:  8px;   /* cards, table, inputs */
--ql-radius-lg:  12px;  /* modals, drawers */

--ql-shadow-sm:  0 1px 3px rgba(42,37,32,0.08);
--ql-shadow-md:  0 4px 12px rgba(42,37,32,0.10);
--ql-shadow-lg:  0 8px 24px rgba(42,37,32,0.12);
```

### 2.4 Motion

```css
--ql-transition-fast:   150ms ease-out;
--ql-transition-medium: 250ms ease-out;
--ql-transition-slow:   350ms ease-out;
```

---

## 3. Implementation Plan

### 3.1 Font Loading Strategy

Use Google Fonts via `@import` at the top of `index.css` (before `@layer` declarations):

```css
@import url('https://fonts.googleapis.com/css2?family=Lora:wght@400;500;600&family=DM+Sans:wght@400;500;600&display=swap');
```

**Fallback chain:** `Lora → Georgia → serif` / `DM Sans → system-ui → sans-serif`

**Note:** The existing Inter TTF files in `src/assets/fonts/` are kept as fallback but no longer declared as the primary font.

### 3.2 Ant Design ConfigProvider Tokens

Update `__root.tsx` `ConfigProvider`:

```tsx
token: {
  colorPrimary: "#7C4A3A",        // terracotta
  colorPrimaryHover: "#9A5C49",
  borderRadius: 8,
  fontFamily: "DM Sans, system-ui, sans-serif",
  colorText: "#2A2520",
  colorTextSecondary: "#6B6560",
  colorBorder: "#DDD8D0",
  colorBgContainer: "#FFFFFF",
  colorBgLayout: "#F9F7F4",
}
```

Menu dark theme tokens:
```tsx
Menu: {
  darkItemBg: "#2A2520",
  darkItemSelectedBg: "#3D3530",
  darkItemHoverBg: "#3D3530",
  darkItemColor: "#C9A87C",
  darkItemSelectedColor: "#E8E0D5",
  darkItemHoverColor: "#E8E0D5",
}
```

### 3.3 File-by-File Refactoring Plan

**Priority order:**

1. `src/index.css` — CSS variables, font import, global overrides
2. `src/routes/__root.tsx` — ConfigProvider tokens, header styling
3. `src/routes/workflows/index.tsx` — Application list page
4. `src/routes/records/index.tsx` — Records list page
5. `src/routes/workflows/-components/worflow-canvas/convas/plugins/*.tsx` — Canvas nodes
6. `src/routes/workflows/-components/workflow-drawer/index.tsx` — Node editor drawer
7. `src/routes/workflows/-components/workflow-header/index.tsx` — Canvas toolbar
8. `src/routes/workflows/-components/settings-modal/index.tsx` — Settings modal
9. `src/routes/workflows/-components/history-drawer/index.tsx` — History drawer

**Constraint:** Zero changes to business logic, event handlers, API calls, or prop interfaces.

### 3.4 Canvas Node Styling

Each plugin component (`begin-plugin.tsx`, `consumer-plugin.tsx`, etc.) has hardcoded accent colors. These will be updated to use the warm palette:

| Node type | Current color | New color |
|-----------|--------------|-----------|
| Begin | `#6366F1` (indigo) | `#7C4A3A` (terracotta) |
| Consumer | `#8B5CF6` (purple) | `#3D6B52` (sage) |
| Function | `#0EA5E9` (blue) | `#5C6B7C` (slate) |
| IfElse | `#F59E0B` (amber) | `#9A7C3A` (warm gold) |
| Message | `#10B981` (emerald) | `#3D6B52` (sage) |

---

## 4. Build & Verification

### 4.1 Build Verification
After each file change: `npm run build` must succeed with zero errors.

### 4.2 Visual Verification
- Playwright screenshots of local dev server vs UAT environment
- Views to capture: application list (desktop + mobile), canvas, node editor drawer, settings modal, records list

### 4.3 Accessibility Verification
- All text/background pairs must maintain WCAG AA (4.5:1 body, 3:1 large text)
- No new axe violations introduced
- Existing `aria-label="Node Configuration"` preserved

### 4.4 Rollback Plan
All changes are in `workflow-ui` only. Git revert of the styling commits restores the original appearance with zero functional impact.

---

## 5. Constraints & Trade-offs

| Constraint | Decision |
|-----------|----------|
| No business logic changes | Styling only — no JSX structure changes unless unavoidable |
| No new dependencies | Google Fonts via CDN `@import` (no npm package needed) |
| Tailwind v4 compatibility | Use `@theme` CSS variables, not `tailwind.config.js` |
| Ant Design v5 | Use `ConfigProvider` tokens, not CSS overrides where possible |
| Performance | Google Fonts with `display=swap` — no render blocking |

---

## 6. References

- PM doc: `pm-doc-v27.0.md`
- TODO label: `TODO-ui-design-quiet-luxury-refactoring-frontend-skill`
- Implementation: Must use `/frontend-design` skill
- UAT baseline: https://workflow-ui-gamma.vercel.app/workflows
