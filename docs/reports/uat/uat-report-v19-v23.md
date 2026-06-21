# UAT Report — v19.0 to v23.0

**Date:** 2026-04-08  
**UAT Method:** Playwright E2E (automated, simulating real user interactions)  
**Tester:** Claude (automated)

---

## UAT Scope

Five features released in this sprint:

| Version | Feature |
|---------|---------|
| v19.0 | Straighten button in canvas |
| v20.0 | Rename workflow from Settings modal |
| v21.0 | Workflow description field |
| v22.0 | Mobile node editor opens as bottom sheet |
| v23.0 | AI workflow generator modal |

---

## UAT Results

### v19.0 — Straighten Button

**Acceptance Criteria:**
- AC1: A "Straighten" button is visible in the canvas on all viewports ✅
- AC2: Clicking it reorders nodes vertically and calls fitView ✅ (verified via TC-CANVAS-05)

**Result: ACCEPTED**

---

### v20.0 — Rename Workflow

**Acceptance Criteria:**
- AC1: Settings modal shows an "Application Name (rename)" input pre-filled with current name ✅
- AC2: Editing and saving the name sends `newApplicationName` in PATCH body ✅
- AC3: Backend validates no collision before renaming ✅ (code review verified)

**Result: ACCEPTED**

---

### v21.0 — Workflow Description

**Acceptance Criteria:**
- AC1: Flyway migration adds nullable `description` column ✅
- AC2: Description visible in desktop table and mobile card subtitle ✅
- AC3: Settings modal includes Description textarea, editable and saved via PATCH ✅

**Result: ACCEPTED**

---

### v22.0 — Mobile Bottom Drawer

**Acceptance Criteria:**
- AC1: On mobile, tapping a canvas node opens a bottom sheet drawer ✅ (TC-CANVAS-MOB-09)
- AC2: Drawer respects `80dvh` max height and iOS safe area insets ✅ (CSS verified)
- AC3: Desktop drawer (right placement, 420px wide) unchanged ✅ (TC-NODE-01/02 pass)

**Result: ACCEPTED**

---

### v23.0 — AI Workflow Generator

**Acceptance Criteria:**
- AC1: "Generate" button visible in desktop header ✅ (TC-GENERATOR-01)
- AC2: "Generate" accessible via ⋯ overflow menu on mobile ✅ (TC-GENERATOR-01 mobile branch)
- AC3: Modal opens with a description textarea and Generate button ✅ (TC-GENERATOR-02)
- AC4: `WORKFLOW_GENERATOR_SYSTEM_PROMPT` constant defined in `WorkflowGeneratorModal.tsx` ✅
- AC5: AI called with `max_tokens: 4096`; result parsed as `WorkFlow` JSON ✅ (code review)
- AC6: On token unavailability, user is redirected to GitHub OAuth flow ✅ (code review)

**Result: ACCEPTED**

---

## Overall UAT Verdict

| Feature | Result |
|---------|--------|
| v19.0 Straighten button | ✅ ACCEPTED |
| v20.0 Rename workflow | ✅ ACCEPTED |
| v21.0 Workflow description | ✅ ACCEPTED |
| v22.0 Mobile bottom drawer | ✅ ACCEPTED |
| v23.0 AI generator | ✅ ACCEPTED |

**All features ACCEPTED. Sprint v19.0–v23.0 is ready for production merge.**

---

## Sign-off

UAT Reviewer: Claude (automated)  
Date: 2026-04-08  
Status: **APPROVED**
