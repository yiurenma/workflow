# Test Doc v28.0 — IBM Design Language UI Refactoring

**Version:** 28.0  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Label:** `TODO-ui-design-ibm-design-language-refactoring`  
**Refs:** `pm-doc-v28.0.md` (CV-US-36), `arch-doc-v28.0.md`

---

## Test Scope

Visual and functional regression tests for the IBM Carbon Design Language refactoring. All tests run against the UAT environment: https://workflow-ui-gamma.vercel.app

**5-Layer UX Validation Framework:**
- Layer 1 (Exist): element is present in DOM
- Layer 2 (Size): dimensions meet spec
- Layer 3 (Viewport): content not clipped
- Layer 4 (Interact): clicks, inputs, navigation work
- Layer 5 (Effect): computed styles match Carbon spec

---

## Test Cases

### TC-IBM-01 — IBM Plex Sans font loaded (Typography)

**Layer:** 1 + 5  
**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/workflows
2. Inspect `document.body` computed font-family

**Expected:** `font-family` includes `IBM Plex Sans`  
**Not expected:** `Lora`, `DM Sans`, `Crimson Pro`

---

### TC-IBM-02 — IBM Plex Mono loaded for code/technical content

**Layer:** 1 + 5  
**Steps:**
1. Open node editor drawer on any node
2. Inspect JSON input field computed font-family

**Expected:** `font-family` includes `IBM Plex Mono`

---

### TC-IBM-03 — Navigation bar background color

**Layer:** 1 + 5  
**Steps:**
1. Navigate to any page
2. Inspect `.workflow-header` or `nav` element computed `background-color`

**Expected:** `rgb(22, 22, 22)` (`#161616`)  
**Not expected:** `rgb(42, 37, 32)` (`#2A2520`, Quiet Luxury charcoal)

---

### TC-IBM-04 — Navigation bar link color

**Layer:** 5  
**Steps:**
1. Inspect nav link elements computed `color`

**Expected:** `rgb(198, 198, 198)` (`#c6c6c6`)  
**Hover expected:** `rgb(255, 255, 255)` (`#ffffff`)

---

### TC-IBM-05 — Page background color

**Layer:** 5  
**Steps:**
1. Inspect `document.body` computed `background-color`

**Expected:** `rgb(255, 255, 255)` (`#ffffff`)  
**Not expected:** `rgb(249, 247, 244)` (`#F9F7F4`, Quiet Luxury cream)

---

### TC-IBM-06 — Primary button color (Blue 60)

**Layer:** 2 + 5  
**Steps:**
1. Locate any primary action button (e.g., "New Application", "Save")
2. Inspect computed `background-color`
3. Measure button height

**Expected:** background `rgb(15, 98, 254)` (`#0f62fe`); height ≥ 40px  
**Not expected:** terracotta `rgb(124, 74, 58)` (`#7C4A3A`)

---

### TC-IBM-07 — Primary button border-radius = 0

**Layer:** 5  
**Steps:**
1. Inspect primary button computed `border-radius`

**Expected:** `0px`  
**Not expected:** any value > 0

---

### TC-IBM-08 — Application table header background

**Layer:** 5  
**Steps:**
1. Navigate to /workflows (desktop viewport 1280px)
2. Inspect table header row `background-color`

**Expected:** `rgb(244, 244, 244)` (`#f4f4f4`)  
**Not expected:** `rgb(243, 240, 235)` (`#F3F0EB`, Quiet Luxury warm sand)

---

### TC-IBM-09 — Application table row height

**Layer:** 2  
**Steps:**
1. Measure table row `height` via `getBoundingClientRect()`

**Expected:** height ≥ 48px

---

### TC-IBM-10 — Application table row hover

**Layer:** 4 + 5  
**Steps:**
1. Hover over a table row
2. Inspect computed `background-color`

**Expected:** `rgb(244, 244, 244)` (`#f4f4f4`)

---

### TC-IBM-11 — Mobile application card background

**Layer:** 5  
**Steps:**
1. Set viewport to 390×844 (mobile)
2. Navigate to /workflows
3. Inspect card container `background-color`

**Expected:** `rgb(244, 244, 244)` (`#f4f4f4`)  
**Not expected:** cream `#F9F7F4`

---

### TC-IBM-12 — Mobile application card border-radius

**Layer:** 5  
**Steps:**
1. Mobile viewport, inspect card `border-radius`

**Expected:** `0px`  
**Not expected:** `8px`, `12px` (Quiet Luxury rounded)

---

### TC-IBM-13 — Status tag color (active/running)

**Layer:** 5  
**Steps:**
1. Find an application with active/running status tag
2. Inspect tag `background-color` and `color`

**Expected:** background `rgb(237, 245, 255)` (`#edf5ff`); text `rgb(15, 98, 254)` (`#0f62fe`)  
**Not expected:** mint `#EAF3EE` background

---

### TC-IBM-14 — Status tag border-radius (pill)

**Layer:** 5  
**Steps:**
1. Inspect status tag `border-radius`

**Expected:** `24px`

---

### TC-IBM-15 — Form input bottom-border only

**Layer:** 5  
**Steps:**
1. Open node editor drawer
2. Inspect any text input `border-top`, `border-left`, `border-right`

**Expected:** all three are `0px` or `none`  
**Expected:** `border-bottom` is `2px solid`

---

### TC-IBM-16 — Form input focus state (Blue 60 bottom-border)

**Layer:** 4 + 5  
**Steps:**
1. Click into a text input in the node editor
2. Inspect `border-bottom-color`

**Expected:** `rgb(15, 98, 254)` (`#0f62fe`)

---

### TC-IBM-17 — Form input background (Gray 10)

**Layer:** 5  
**Steps:**
1. Inspect text input `background-color`

**Expected:** `rgb(244, 244, 244)` (`#f4f4f4`)

---

### TC-IBM-18 — Card / tile no box-shadow

**Layer:** 5  
**Steps:**
1. Inspect application card or canvas node card `box-shadow`

**Expected:** `none` or `0px 0px 0px 0px`

---

### TC-IBM-19 — Canvas node card background

**Layer:** 5  
**Steps:**
1. Open canvas for any application
2. Inspect a node card `background-color`

**Expected:** `rgb(244, 244, 244)` (`#f4f4f4`) or `rgb(255, 255, 255)` (`#ffffff`)  
**Not expected:** warm beige tones

---

### TC-IBM-20 — Canvas node selected state (Blue 60 border)

**Layer:** 4 + 5  
**Steps:**
1. Click a node on the canvas
2. Inspect node card `border-color`

**Expected:** `rgb(15, 98, 254)` (`#0f62fe`)

---

### TC-IBM-21 — Secondary text color (Gray 70)

**Layer:** 5  
**Steps:**
1. Inspect helper text / description text in node editor
2. Check computed `color`

**Expected:** `rgb(82, 82, 82)` (`#525252`)  
**Not expected:** zinc-400 `#a1a1aa`

---

### TC-IBM-22 — Body text letter-spacing at 14px

**Layer:** 5  
**Steps:**
1. Inspect 14px body text `letter-spacing`

**Expected:** `0.16px`

---

### TC-IBM-23 — Caption text letter-spacing at 12px

**Layer:** 5  
**Steps:**
1. Inspect 12px caption/label text `letter-spacing`

**Expected:** `0.32px`

---

### TC-IBM-24 — Modal border-radius = 0

**Layer:** 5  
**Steps:**
1. Open any modal (settings, confirm, explain)
2. Inspect modal container `border-radius`

**Expected:** `0px`

---

### TC-IBM-25 — Drawer background (white)

**Layer:** 5  
**Steps:**
1. Open node editor drawer
2. Inspect drawer panel `background-color`

**Expected:** `rgb(255, 255, 255)` (`#ffffff`)

---

### TC-IBM-26 — Focus ring on interactive elements

**Layer:** 4 + 5  
**Steps:**
1. Tab to a button or input
2. Inspect `outline` or `box-shadow` on focused element

**Expected:** `2px solid #0f62fe` (or equivalent focus indicator)

---

### TC-IBM-27 — WCAG contrast: primary text on white

**Layer:** 5  
**Steps:**
1. Compute contrast ratio of `#161616` on `#ffffff`

**Expected:** ≥ 4.5:1 (actual: 16:1 — PASS)

---

### TC-IBM-28 — WCAG contrast: Blue 60 on white (buttons/links)

**Layer:** 5  
**Steps:**
1. Compute contrast ratio of `#0f62fe` on `#ffffff`

**Expected:** ≥ 4.5:1 (actual: 4.56:1 — PASS)

---

### TC-IBM-29 — Functional regression: application CRUD

**Layer:** 4  
**Steps:**
1. Create a new application
2. Edit application name
3. Delete application

**Expected:** all operations succeed; no JS errors in console

---

### TC-IBM-30 — Functional regression: canvas and node editor

**Layer:** 4  
**Steps:**
1. Open canvas for an application
2. Click a node to open editor
3. Edit node fields and save

**Expected:** changes persist; drawer opens and closes correctly

---

### TC-IBM-31 — Functional regression: records list

**Layer:** 4  
**Steps:**
1. Navigate to records
2. Paginate through results

**Expected:** pagination works; records display correctly

---

### TC-IBM-32 — Functional regression: mobile viewport

**Layer:** 3 + 4  
**Steps:**
1. Set viewport to 390×844
2. Navigate through applications, open canvas, open node editor

**Expected:** no content clipped outside viewport; all interactions work

---

### TC-IBM-33 — No Quiet Luxury colors remain

**Layer:** 5  
**Steps:**
1. Run automated scan for color values: `#7C4A3A`, `#C9A87C`, `#F9F7F4`, `#F3F0EB`, `#EAF3EE`, `#2A2520`

**Expected:** none of these colors appear in computed styles of any visible element

---

### TC-IBM-34 — No Lora / DM Sans fonts remain

**Layer:** 5  
**Steps:**
1. Inspect all text elements for `font-family`

**Expected:** no element uses `Lora`, `DM Sans`, or `Crimson Pro`

---

## Regression Suite

All existing test cases from `test-doc-v27.0.md` (TC-APP-*, TC-CANVAS-*, TC-NODE-*, TC-REC-*, TC-GENERATOR-*, TC-JSONPATH-*) must continue to pass after the IBM Design refactoring.

---
