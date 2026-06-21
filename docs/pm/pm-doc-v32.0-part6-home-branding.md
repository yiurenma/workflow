# Product Requirements — Workflow Studio Unified Program Part 6: Home & Branding (v32.0)

**Document version:** 32.0 Part 6  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Home screen introduction + snail favicon  
**Master label:** `TODO-workflow-studio-unified-product-ux-program`  
**Part labels:** `TODO-home-workflow-studio-intro-copy`, `TODO-branding-favicon-snail-tab-icon`

---

## 1. Overview

This document covers first-run experience improvements and branding polish.

**Goals:**
1. Add Workflow Studio introduction on home screen
2. Add snail favicon for browser tab

---

## 2. User Stories

### US-HOME-01 — Workflow Studio introduction on home screen

**As a** new user,  
**I want** to see an introduction to Workflow Studio on the home screen,  
**So that** I understand what the product is, who it's for, and what I can do with it.

**Acceptance Criteria:**

**AC-HOME-01-01:** **Placement:** First / **Home** screen where users land on **applications** list.

**AC-HOME-01-02:** **Content includes:**
- **What the product is:** Workflow Studio enables configurable message enrichment, conditional logic, and multi-channel delivery without writing custom backend code
- **Who it's for:** Integration engineers, application developers, QA/operations teams, business analysts
- **Benefits:**
  - Speed vs code-only integration (visual editor, no deploy cycle for config changes)
  - Shared business/engineering view (non-technical stakeholders can understand workflows)
  - Rules as guardrails (JSONPath conditions prevent invalid data flow)
  - Records as observability (execution history for debugging and auditing)
- **Feature bullets:**
  - **Applications:** Manage workflow configurations with versioning and rollback
  - **Canvas:** Visual workflow editor with nodes, rules (JSONPath), and actions
  - **Run:** Test workflows with sample data before deploying
  - **Records:** View execution history and drill into individual runs
  - **Explain:** AI-powered workflow documentation
  - **Generate:** AI-powered workflow creation from natural language
  - **JsonPath tool:** Validate JSONPath expressions against sample data
  - **Mobile experience:** Full-featured mobile UI for on-the-go access

**AC-HOME-01-03:** **Deliverable:** Final strings + optional layout note (hero section, 2–3 benefits, feature list).

**AC-HOME-01-04:** **Accessible contrast:** Text meets WCAG AA contrast requirements (Carbon design system).

**AC-HOME-01-05:** **Dismissible (optional):** User can dismiss intro after first view; preference persists in localStorage.

---

### US-BRAND-01 — Snail favicon for browser tab

**As a** user,  
**I want** to see a snail icon in my browser tab,  
**So that** I can easily identify the Workflow Studio tab among many open tabs.

**Acceptance Criteria:**

**AC-BRAND-01-01:** **Deliverable:** Snail icon in multiple formats:
- **SVG** (vector, scalable)
- **favicon.ico** (16×16, 32×32 for legacy browsers)
- **PNG** sizes: 16×16, 32×32, 180×180 (for Apple touch icon), 192×192, 512×512 (for PWA manifest)

**AC-BRAND-01-02:** **Integration:** Wire via `index.html` `<link rel="icon" ...>` tags:
```html
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<link rel="icon" type="image/png" sizes="32×32" href="/favicon-32×32.png">
<link rel="icon" type="image/png" sizes="16×16" href="/favicon-16×16.png">
<link rel="apple-touch-icon" sizes="180×180" href="/apple-touch-icon.png">
```

**AC-BRAND-01-03:** **PWA manifest icons (if app declares manifest.json):**
```json
{
  "icons": [
    { "src": "/icon-192×192.png", "sizes": "192×192", "type": "image/png" },
    { "src": "/icon-512×512.png", "sizes": "512×512", "type": "image/png" }
  ]
}
```

**AC-BRAND-01-04:** **Quality:** Readable at **16×16** and **32×32** (small sizes).

**AC-BRAND-01-05:** **Color alignment:** Align with Carbon neutrals (grays) or document intentional brand exception (e.g., terracotta snail from Quiet Luxury era).

---

## 3. Out of Scope

- Backend API changes
- Full marketing website (this is in-app intro only)
- Animated favicon (static icon only)

---

## 4. Success Metrics

- Home screen intro visible on first load
- Intro content covers what/who/benefits/features
- Snail favicon visible in browser tab at all sizes
- Favicon works on Desktop Chrome, Safari, Firefox, Mobile Chrome, Mobile Safari

---

## 5. References

- Favicon best practices: https://evilmartians.com/chronicles/how-to-favicon-in-2021
- WCAG contrast checker: https://webaim.org/resources/contrastchecker/
