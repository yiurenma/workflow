# PM Doc v28.0 — IBM Design Language UI Refactoring

**Version:** 28.0  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Label:** `TODO-ui-design-ibm-design-language-refactoring`

---

## Overview

Refactor the Workflow Studio UI from the current Quiet Luxury design system to IBM Carbon Design Language. The goal is a systematic, token-driven visual overhaul that replaces the warm terracotta/cream palette with IBM's monochromatic + Blue 60 system, adopts IBM Plex Sans/Mono typography, and applies Carbon's rectangular component aesthetic throughout. All existing functionality must remain intact.

---

## User Stories & Acceptance Criteria

### CV-US-36 — IBM Carbon Design Language Visual System

> **As a** Workflow Studio user, **I want** the interface to adopt IBM Carbon Design Language, **so that** the platform communicates enterprise authority, systematic precision, and professional credibility.

**Acceptance Criteria:**

**CV-AC-36-1 — Typography System**
- Font family: `IBM Plex Sans` (primary), `IBM Plex Mono` (code/technical), with appropriate system fallbacks
- Display headings (42px+): weight 300 (Light), line-height 1.17–1.19, letter-spacing 0
- Body text (16px): weight 400, line-height 1.50, letter-spacing 0
- Compact body (14px): weight 400, line-height 1.29, letter-spacing 0.16px
- Captions (12px): weight 400, line-height 1.33, letter-spacing 0.32px
- UI labels / emphasis: weight 600 (Semibold) — weight 700 (Bold) is NOT used
- All text meets WCAG 2.1 AA contrast (4.5:1 for body, 3:1 for large text)

**CV-AC-36-2 — Color System**
- Background: White `#ffffff`
- Primary text: Gray 100 `#161616`
- Secondary text: Gray 70 `#525252`
- Surface / card background: Gray 10 `#f4f4f4`
- Border / divider: Gray 30 `#c6c6c6`
- Sole accent / interactive: IBM Blue 60 `#0f62fe`
- Link hover: Blue 70 `#0043ce`
- Focus ring: Blue 60 `#0f62fe` (2px inset)
- Error: Red 60 `#da1e28`
- Success: Green 50 `#24a148`
- Warning: Yellow 30 `#f1c21b`
- Navigation bar background: Gray 100 `#161616`
- No additional accent colors introduced; no gradients

**CV-AC-36-3 — Component Shapes & Borders**
- Buttons: 0px border-radius (sharp rectangle)
- Inputs: 0px border-radius; bottom-border only (2px solid, no side/top borders)
- Cards / tiles: 0px border-radius; no box-shadow; depth via background-color layering
- Tags / pills: 24px border-radius (sole rounded exception)
- Avatars / icon containers: 50% (circle)

**CV-AC-36-4 — Button Styles**
- Primary: `#0f62fe` background, `#ffffff` text, 48px height, 0px radius, hover `#0353e9`, active `#002d9c`
- Secondary: `#393939` background, `#ffffff` text, same dimensions
- Tertiary / Ghost: transparent background, `#0f62fe` text, 1px solid `#0f62fe` border
- Danger: `#da1e28` background, `#ffffff` text
- All buttons: asymmetric padding `14px 63px 14px 15px` (room for trailing icon)

**CV-AC-36-5 — Form Inputs**
- Background: `#f4f4f4` (Gray 10)
- Height: 40px (default), 48px (large)
- Horizontal padding: 16px
- Bottom-border: 2px solid transparent (default) → 2px solid `#0f62fe` (focus) → 2px solid `#da1e28` (error)
- Label: 12px, weight 400, letter-spacing 0.32px, color `#525252`
- Placeholder: `#6f6f6f` (Gray 60)

**CV-AC-36-6 — Navigation Bar**
- Background: `#161616` (Gray 100), height 48px
- Logo / brand text: white, left-aligned
- Nav links: 14px IBM Plex Sans weight 400, color `#c6c6c6`; hover `#ffffff`; active `#ffffff` + 2px bottom indicator
- Mobile: hamburger with slide-out panel

**CV-AC-36-7 — Cards & Tables**
- Card background: `#f4f4f4`; hover: `#e8e8e8`; 0px radius; 16px padding; no shadow
- Table header: `#f4f4f4` background, 14px weight 600, color `#161616`
- Table rows: white background, 48px row height, bottom-border `1px solid #e0e0e0`
- Row hover: `#f4f4f4` background

**CV-AC-36-8 — Tags / Status Labels**
- Active / running: Blue 10 `#edf5ff` background, Blue 60 `#0f62fe` text
- Success: Green 10 background, Green 50 `#24a148` text
- Error: Red 10 background, Red 60 `#da1e28` text
- Padding: 4px 8px; border-radius: 24px; font: 12px weight 400

**CV-AC-36-9 — Spacing & Layout**
- Base unit: 8px; all spacing values are multiples of 8px (with 2px/4px for micro-adjustments)
- Component padding: 16px standard
- Section spacing: 48px major, 32px minor, 16px mobile
- No arbitrary spacing values

**CV-AC-36-10 — Depth & Elevation**
- Cards / tiles: no shadow — depth via background-color layering (white → `#f4f4f4` → `#e0e0e0`)
- Dropdowns / tooltips / overflow menus: `0 2px 6px rgba(0,0,0,0.3)`
- Modal dialogs / drawers: `0 2px 6px rgba(0,0,0,0.3)` + dark scrim overlay
- Focus: 2px solid `#0f62fe` inset + 1px solid `#ffffff` inner ring

**CV-AC-36-11 — Transitions & Motion**
- Fast interactions (button hover, tag toggle): 150ms ease-out
- Medium interactions (dropdown open, tooltip): 200ms ease-out
- Slow interactions (drawer/modal open): 300ms ease-out
- No spring or bounce animations

**CV-AC-36-12 — Functional Regression**
- All existing features (application CRUD, canvas editing, node editor, records, explain, generator) remain fully functional
- No API contract changes
- Mobile responsive behavior preserved
- Accessibility: all WCAG 2.1 AA requirements maintained (contrast, focus, aria-labels)

---

## Scope

**In scope:**
- Global CSS variables / design tokens (replacing Quiet Luxury tokens)
- Navigation header
- Application list (desktop table + mobile cards)
- Canvas toolbar and node cards
- Node editor drawer
- Records list
- Modals (settings, confirm, explain, generator, JsonPath)
- Buttons, inputs, tags, pagination throughout

**Out of scope:**
- Backend API changes
- New features or functionality
- Dark mode implementation (deferred)
- IBM Carbon React component library installation (implement visually equivalent styles in existing Tailwind/CSS)

---
