# Test Cases — Workflow Studio Unified Program Part 6: Home & Branding (v32.0)

**Document version:** 32.0 Part 6  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Home screen introduction + snail favicon  
**Repository:** `workflow-ui`

---

## 1. Test Strategy

### 1.1 Test Levels

- **Unit tests:** Not required (content and assets)
- **Integration tests:** Not required (no backend changes)
- **E2E tests (Playwright):** Optional for home screen visibility
- **Manual testing:** Primary verification method

### 1.2 Test Environments

- **Desktop:** Chrome 1280px, Safari, Firefox
- **Mobile:** Chrome 390×844, Safari iOS
- **UAT:** https://workflow-ui-gamma.vercel.app

---

## 2. Home Screen Introduction Test Cases

### TC-HOME-01 — Introduction visible on first load

**Preconditions:**
- Clear browser cache and localStorage
- Navigate to `https://workflow-ui-gamma.vercel.app`

**Steps:**
1. Load home screen (applications list)
2. Observe page content

**Expected:**
- Introduction section visible above or beside applications list
- Section includes:
  - **Heading:** "Workflow Studio" or similar
  - **What it is:** Brief description of the product
  - **Who it's for:** Target audience
  - **Benefits:** 2-3 key benefits listed
  - **Features:** Bullet list of main features

**Layer validation:**
- Layer 1 (Exist): Introduction section present in DOM
- Layer 3 (Viewport): Section visible without scrolling (or clearly above fold)
- Layer 5 (Effect): Text meets WCAG AA contrast requirements

---

### TC-HOME-02 — Content accuracy and completeness

**Preconditions:**
- Open home screen

**Steps:**
1. Read introduction content
2. Verify against requirements

**Expected:**
- **What the product is:** "Workflow Studio enables configurable message enrichment, conditional logic, and multi-channel delivery without writing custom backend code"
- **Who it's for:** Integration engineers, application developers, QA/operations teams, business analysts
- **Benefits include:**
  - Speed vs code-only integration
  - Shared business/engineering view
  - Rules as guardrails
  - Records as observability
- **Features include:**
  - Applications (manage configurations)
  - Canvas (visual editor)
  - Run (test workflows)
  - Records (execution history)
  - Explain (AI documentation)
  - Generate (AI creation)
  - JsonPath tool
  - Mobile experience

---

### TC-HOME-03 — Accessible contrast (WCAG AA)

**Preconditions:**
- Open home screen
- Use browser DevTools or contrast checker tool

**Steps:**
1. Inspect introduction text colors
2. Measure contrast ratios against background

**Expected:**
- All text meets WCAG AA contrast requirements:
  - Normal text (< 18pt): ≥ 4.5:1
  - Large text (≥ 18pt): ≥ 3:1
- Carbon design system tokens used (e.g., `--cds-text-primary`, `--cds-text-secondary`)

**Verification:**
- Use WebAIM Contrast Checker: https://webaim.org/resources/contrastchecker/
- Or browser DevTools accessibility panel

---

### TC-HOME-04 — Dismissible introduction (optional)

**Preconditions:**
- Open home screen (first visit)

**Steps:**
1. Locate dismiss control (e.g., "×" button, "Got it" button)
2. Click dismiss
3. Reload page

**Expected:**
- Introduction hidden after dismiss
- Preference persists in localStorage
- Applications list remains visible
- Optional: "Show intro" link to restore

**Verification:**
- Check localStorage: `localStorage.getItem('workflow_intro_dismissed')` === `'true'`

---

### TC-HOME-05 — Introduction on mobile

**Preconditions:**
- Open home screen on Mobile Chrome 390×844

**Steps:**
1. Load home screen
2. Observe introduction section

**Expected:**
- Introduction visible and readable on mobile
- Text reflows appropriately for narrow viewport
- No horizontal scrolling required
- Touch targets (dismiss button) ≥ 44×44px

---

### TC-HOME-06 — Introduction does not block navigation

**Preconditions:**
- Open home screen with introduction visible

**Steps:**
1. Scroll down to applications list
2. Click on an application
3. Navigate to workflow canvas

**Expected:**
- Introduction does not block or interfere with navigation
- User can access all features normally
- Introduction is informational only, not a modal blocker

---

## 3. Favicon Test Cases

### TC-FAVICON-01 — Favicon visible in browser tab (Desktop Chrome)

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app` in Desktop Chrome 1280px

**Steps:**
1. Observe browser tab
2. Check favicon

**Expected:**
- Snail icon visible in tab
- Icon readable at small size (16×16 or 32×32)
- Icon matches brand (Carbon neutrals or documented exception)

---

### TC-FAVICON-02 — Favicon visible in browser tab (Safari)

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app` in Safari (macOS)

**Steps:**
1. Observe browser tab
2. Check favicon

**Expected:**
- Snail icon visible in tab
- Icon readable at small size

---

### TC-FAVICON-03 — Favicon visible in browser tab (Firefox)

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app` in Firefox

**Steps:**
1. Observe browser tab
2. Check favicon

**Expected:**
- Snail icon visible in tab
- Icon readable at small size

---

### TC-FAVICON-04 — Favicon visible on mobile (Chrome)

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app` on Mobile Chrome 390×844

**Steps:**
1. Observe browser tab/address bar
2. Check favicon

**Expected:**
- Snail icon visible (if browser shows favicons on mobile)
- Icon readable at small size

---

### TC-FAVICON-05 — Favicon visible on mobile (Safari iOS)

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app` on Safari iOS

**Steps:**
1. Observe browser tab
2. Check favicon

**Expected:**
- Snail icon visible (if browser shows favicons on mobile)
- Icon readable at small size

---

### TC-FAVICON-06 — Apple touch icon (iOS home screen)

**Preconditions:**
- Open `https://workflow-ui-gamma.vercel.app` on Safari iOS

**Steps:**
1. Tap Share button
2. Select "Add to Home Screen"
3. Observe icon preview
4. Add to home screen
5. Check home screen icon

**Expected:**
- Snail icon shown in preview (180×180)
- Icon added to home screen with snail graphic
- Icon readable and recognizable

---

### TC-FAVICON-07 — PWA manifest icons (if applicable)

**Preconditions:**
- Check if app has `manifest.json`
- Open `https://workflow-ui-gamma.vercel.app`

**Steps:**
1. Open browser DevTools
2. Navigate to Application > Manifest
3. Check icons array

**Expected:**
- If manifest exists:
  - Icons array includes 192×192 and 512×512 PNG
  - Icons point to valid snail icon files
  - Icons load successfully

---

### TC-FAVICON-08 — Favicon files exist and are valid

**Preconditions:**
- Access to `workflow-ui` repository

**Steps:**
1. Check `public/` or `static/` directory for favicon files
2. Verify files exist:
   - `favicon.svg` (vector)
   - `favicon.ico` (16×16, 32×32)
   - `favicon-16×16.png`
   - `favicon-32×32.png`
   - `apple-touch-icon.png` (180×180)
   - `icon-192×192.png` (for PWA)
   - `icon-512×512.png` (for PWA)
3. Open each file in image viewer

**Expected:**
- All files exist
- All files are valid images (not corrupted)
- All files show snail icon
- Icons readable at their respective sizes

---

### TC-FAVICON-09 — Favicon wired in index.html

**Preconditions:**
- Access to `workflow-ui` repository

**Steps:**
1. Open `index.html` or equivalent entry point
2. Check `<head>` section for favicon links

**Expected:**
- Links present:
```html
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<link rel="icon" type="image/png" sizes="32×32" href="/favicon-32×32.png">
<link rel="icon" type="image/png" sizes="16×16" href="/favicon-16×16.png">
<link rel="apple-touch-icon" sizes="180×180" href="/apple-touch-icon.png">
```
- Paths correct (files exist at specified paths)

---

### TC-FAVICON-10 — Favicon color alignment

**Preconditions:**
- Open favicon files in image editor or viewer

**Steps:**
1. Inspect favicon colors
2. Compare to Carbon design system neutrals

**Expected:**
- Colors align with Carbon neutrals (grays) OR
- Documented intentional brand exception (e.g., terracotta snail from Quiet Luxury era)
- Colors readable against typical browser tab backgrounds (light and dark modes)

---

## 4. Regression Tests

### TC-HOME-REG-01 — Applications list still visible and functional

**Preconditions:**
- Open home screen with introduction

**Steps:**
1. Scroll to applications list
2. Verify list visible
3. Click on an application
4. Navigate to workflow canvas

**Expected:**
- Applications list not obscured by introduction
- All existing functionality works
- No regressions in navigation

---

### TC-HOME-REG-02 — Existing E2E tests pass

**Preconditions:**
- Run full Playwright suite

**Steps:**
1. Run TC-NAV-01 (navigation to applications)
2. Run TC-APP-DESK-01 through TC-APP-DESK-10 (applications tests)

**Expected:**
- All existing tests pass
- No regressions from introduction or favicon changes

---

## 5. Acceptance Criteria Mapping

| AC | Test Cases |
|----|------------|
| AC-HOME-01-01 | TC-HOME-01 |
| AC-HOME-01-02 | TC-HOME-02 |
| AC-HOME-01-03 | TC-HOME-02 |
| AC-HOME-01-04 | TC-HOME-03 |
| AC-HOME-01-05 | TC-HOME-04 |
| AC-BRAND-01-01 | TC-FAVICON-08 |
| AC-BRAND-01-02 | TC-FAVICON-09 |
| AC-BRAND-01-03 | TC-FAVICON-07 |
| AC-BRAND-01-04 | TC-FAVICON-01 through TC-FAVICON-06 |
| AC-BRAND-01-05 | TC-FAVICON-10 |

---

## 6. Test Execution Plan

### Phase 1: Home Screen Introduction (after implementation)
- TC-HOME-01 through TC-HOME-06
- TC-HOME-REG-01

### Phase 2: Favicon (after implementation)
- TC-FAVICON-08, TC-FAVICON-09 (verify files and wiring)
- TC-FAVICON-01 through TC-FAVICON-07 (visual verification)
- TC-FAVICON-10 (color alignment)

### Phase 3: Regression (after all features)
- TC-HOME-REG-02
- Full E2E suite run

---

## 7. Success Criteria

- Home screen introduction visible on first load
- Introduction content complete and accurate
- Introduction meets WCAG AA contrast requirements
- Snail favicon visible in browser tabs on all tested browsers
- Apple touch icon works on iOS
- PWA manifest icons present (if applicable)
- No regressions in existing functionality

---

## 8. Manual Testing Checklist

### Home Screen Introduction
- [ ] Introduction visible on Desktop Chrome
- [ ] Introduction visible on Mobile Chrome
- [ ] Content complete (what/who/benefits/features)
- [ ] Contrast meets WCAG AA
- [ ] Dismissible (if implemented)
- [ ] Does not block navigation

### Favicon
- [ ] Visible in Desktop Chrome tab
- [ ] Visible in Safari tab
- [ ] Visible in Firefox tab
- [ ] Visible on Mobile Chrome
- [ ] Visible on Mobile Safari
- [ ] Apple touch icon works (iOS home screen)
- [ ] PWA manifest icons present (if applicable)
- [ ] All favicon files exist and are valid
- [ ] Favicon wired in index.html
- [ ] Colors align with brand

---

## 9. References

- `pm-doc-v32.0-part6-home-branding.md` — Product requirements
- `arch-doc-v32.0-part6-home-branding.md` — Technical design
- Favicon best practices: https://evilmartians.com/chronicles/how-to-favicon-in-2021
- WCAG contrast checker: https://webaim.org/resources/contrastchecker/
- Carbon Design System: https://carbondesignsystem.com/
