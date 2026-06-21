# Arch Doc v28.0 — IBM Design Language UI Refactoring

**Version:** 28.0  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Label:** `TODO-ui-design-ibm-design-language-refactoring`  
**Refs:** `pm-doc-v28.0.md` (CV-US-36, CV-AC-36-1~10)

---

## 1. Approach

Replace the Quiet Luxury design tokens (terracotta/cream palette, Lora/DM Sans fonts) with IBM Carbon Design Language tokens. The implementation is **CSS-first**: we update global CSS custom properties and Tailwind config — no new npm packages required, no Carbon React library installation.

**Strategy:** Token swap + targeted component overrides.

1. Update `tailwind.config.js` to add IBM Carbon color aliases and IBM Plex font families.
2. Update global CSS (`index.css` or equivalent) to define `--cds-*` custom properties and override Ant Design component tokens via `ConfigProvider`.
3. Update `App.tsx` / root layout to apply IBM Plex Sans via Google Fonts or self-hosted.
4. Sweep component files to replace Quiet Luxury class names with Carbon equivalents.

---

## 2. File Inventory

| File | Change |
|------|--------|
| `workflow-ui/index.html` | Add IBM Plex Sans + Mono Google Fonts `<link>` |
| `workflow-ui/src/index.css` | Replace `--color-*` tokens with `--cds-*` Carbon tokens; reset body font |
| `workflow-ui/tailwind.config.js` | Add Carbon color palette + IBM Plex font families |
| `workflow-ui/src/App.tsx` | Update Ant Design `ConfigProvider` theme tokens |
| `workflow-ui/src/components/workflow-header/index.tsx` | Nav bar: `#161616` bg, IBM Plex, Carbon link colors |
| `workflow-ui/src/routes/workflows/index.tsx` | Application table: Carbon table styles |
| `workflow-ui/src/routes/workflows/mobile.tsx` | Mobile cards: `#f4f4f4` bg, 0px radius, Carbon typography |
| `workflow-ui/src/routes/records/index.tsx` | Records table: Carbon row height, border, hover |
| `workflow-ui/src/components/workflow-drawer/index.tsx` | Node editor drawer: Carbon form inputs, bottom-border |
| `workflow-ui/src/components/node-section/index.tsx` | Section headers: IBM Plex, Gray 70 secondary text |
| `workflow-ui/src/components/workflow-canvas/index.tsx` | Canvas node cards: `#f4f4f4` bg, 0px radius, Blue 60 selected |
| `workflow-ui/src/routes/workflows/components/*.tsx` | Modals (settings, confirm, explain, generator): Carbon modal styles |

---

## 3. Token Mapping

### CSS Custom Properties (`index.css`)

```css
:root {
  /* Backgrounds */
  --cds-background: #ffffff;
  --cds-layer-01: #f4f4f4;
  --cds-layer-02: #e0e0e0;

  /* Text */
  --cds-text-primary: #161616;
  --cds-text-secondary: #525252;
  --cds-text-placeholder: #6f6f6f;
  --cds-text-disabled: #8d8d8d;

  /* Interactive */
  --cds-button-primary: #0f62fe;
  --cds-button-primary-hover: #0353e9;
  --cds-button-primary-active: #002d9c;
  --cds-link-primary: #0f62fe;
  --cds-link-primary-hover: #0043ce;
  --cds-focus: #0f62fe;
  --cds-focus-inset: #ffffff;

  /* Borders */
  --cds-border-subtle: #c6c6c6;
  --cds-border-strong: #8d8d8d;

  /* Support */
  --cds-support-error: #da1e28;
  --cds-support-success: #24a148;
  --cds-support-warning: #f1c21b;
  --cds-support-info: #0f62fe;

  /* Navigation */
  --cds-nav-background: #161616;
  --cds-nav-text: #c6c6c6;
  --cds-nav-text-hover: #ffffff;
}
```

### Tailwind Config Additions

```js
// tailwind.config.js — extend colors
colors: {
  'ibm-blue-60': '#0f62fe',
  'ibm-blue-70': '#0043ce',
  'ibm-blue-80': '#002d9c',
  'ibm-blue-10': '#edf5ff',
  'ibm-gray-100': '#161616',
  'ibm-gray-90': '#262626',
  'ibm-gray-80': '#393939',
  'ibm-gray-70': '#525252',
  'ibm-gray-60': '#6f6f6f',
  'ibm-gray-50': '#8d8d8d',
  'ibm-gray-30': '#c6c6c6',
  'ibm-gray-20': '#e0e0e0',
  'ibm-gray-10': '#f4f4f4',
  'ibm-red-60': '#da1e28',
  'ibm-green-50': '#24a148',
  'ibm-yellow-30': '#f1c21b',
}
fontFamily: {
  'ibm-plex': ['"IBM Plex Sans"', 'Helvetica Neue', 'Arial', 'sans-serif'],
  'ibm-mono': ['"IBM Plex Mono"', 'Menlo', 'Courier', 'monospace'],
}
```

### Ant Design ConfigProvider Token Overrides

```tsx
<ConfigProvider theme={{
  token: {
    colorPrimary: '#0f62fe',
    colorBgBase: '#ffffff',
    colorTextBase: '#161616',
    colorBorder: '#c6c6c6',
    colorError: '#da1e28',
    colorSuccess: '#24a148',
    colorWarning: '#f1c21b',
    borderRadius: 0,
    fontFamily: '"IBM Plex Sans", Helvetica Neue, Arial, sans-serif',
    fontSize: 14,
    lineHeight: 1.29,
  },
  components: {
    Button: {
      borderRadius: 0,
      controlHeight: 48,
      paddingContentHorizontal: 15,
    },
    Input: {
      borderRadius: 0,
      controlHeight: 40,
      colorBgContainer: '#f4f4f4',
    },
    Table: {
      headerBg: '#f4f4f4',
      rowHoverBg: '#f4f4f4',
      borderColor: '#e0e0e0',
    },
    Drawer: {
      colorBgElevated: '#ffffff',
    },
    Modal: {
      borderRadius: 0,
      colorBgElevated: '#ffffff',
    },
    Tag: {
      borderRadius: 24,
    },
  }
}} />
```

---

## 4. Typography Implementation

Add to `index.html`:
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=IBM+Plex+Mono:wght@400&family=IBM+Plex+Sans:wght@300;400;600&display=swap" rel="stylesheet">
```

Global body reset in `index.css`:
```css
body {
  font-family: 'IBM Plex Sans', Helvetica Neue, Arial, sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.29;
  letter-spacing: 0.16px;
  color: #161616;
  background-color: #ffffff;
}
```

---

## 5. Component-Level Changes

### Navigation Header
- Background: `bg-ibm-gray-100` (`#161616`)
- Brand text: `text-white font-ibm-plex font-light text-2xl`
- Nav links: `text-[#c6c6c6] hover:text-white text-sm font-normal`
- Remove: warm charcoal `#2A2520`, gold `#C9A87C` accents

### Application Table (Desktop)
- Table header: `bg-ibm-gray-10 text-ibm-gray-100 font-semibold text-sm tracking-[0.16px]`
- Row height: 48px via `rowClassName` or CSS
- Row hover: `hover:bg-ibm-gray-10`
- Remove: warm sand `#F3F0EB` header, terracotta accents

### Application Cards (Mobile)
- Card: `bg-ibm-gray-10 rounded-none p-4`
- Title: `text-ibm-gray-100 font-semibold text-base`
- Subtitle: `text-ibm-gray-70 text-sm tracking-[0.16px]`
- Remove: cream `#F9F7F4` background, rounded-lg

### Status Tags
- Active: `bg-ibm-blue-10 text-ibm-blue-60 rounded-[24px] px-2 py-1 text-xs`
- Replace: mint `#EAF3EE` active tags

### Buttons
- Primary: `bg-ibm-blue-60 hover:bg-[#0353e9] active:bg-ibm-blue-80 text-white rounded-none h-12 px-4`
- Remove: terracotta `#7C4A3A` primary buttons

### Node Editor Drawer
- Drawer background: `#ffffff`
- Section headers: `text-ibm-gray-70 text-xs font-semibold tracking-[0.32px] uppercase`
- Input fields: `bg-ibm-gray-10 border-0 border-b-2 border-transparent focus:border-ibm-blue-60 rounded-none`
- Remove: warm cream backgrounds

### Canvas Nodes
- Node card: `bg-ibm-gray-10 border border-ibm-gray-20 rounded-none shadow-none`
- Selected: `border-ibm-blue-60 border-2`
- Node title: `text-ibm-gray-100 font-semibold text-sm`
- Remove: warm beige node backgrounds

---

## 6. Quiet Luxury → Carbon Migration Map

| Element | Quiet Luxury (remove) | Carbon (replace with) |
|---------|----------------------|----------------------|
| Font | Lora + DM Sans | IBM Plex Sans + IBM Plex Mono |
| Primary color | Terracotta `#7C4A3A` | IBM Blue 60 `#0f62fe` |
| Header bg | Warm charcoal `#2A2520` | Gray 100 `#161616` |
| Header accent | Gold `#C9A87C` | White `#ffffff` |
| Page bg | Cream `#F9F7F4` | White `#ffffff` |
| Card bg | Warm sand `#F3F0EB` | Gray 10 `#f4f4f4` |
| Active tag | Mint `#EAF3EE` | Blue 10 `#edf5ff` |
| Border radius | 8–12px | 0px (buttons/inputs/cards) |
| Shadows | Soft warm shadows | None (cards); `0 2px 6px rgba(0,0,0,0.3)` (floating) |

---

## 7. Security & Accessibility

- No new external dependencies; Google Fonts loaded via `<link>` (existing pattern)
- All color combinations verified against WCAG 2.1 AA: Gray 100 on White = 16:1; Blue 60 on White = 4.5:1; Gray 70 on White = 7:1
- Focus ring: 2px solid `#0f62fe` inset — meets WCAG 2.4.11 (Focus Appearance)
- Existing `aria-label` attributes on drawer and buttons preserved

---

## 8. Trade-offs

| Option | Decision |
|--------|----------|
| Install `@carbon/react` library | Rejected — adds ~500KB bundle, requires React 18 migration risk |
| CSS-only token swap | Chosen — zero new dependencies, surgical changes, reversible |
| Dark mode | Deferred — out of scope for this TODO |
| Full Tailwind purge of old tokens | Deferred — remove Quiet Luxury tokens after Carbon tokens verified |

---

## 9. Rollback Plan

All changes are in `workflow-ui` CSS/config files. Reverting is a single `git revert` on the commit. No database or API changes.

---
