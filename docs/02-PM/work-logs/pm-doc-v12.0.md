# PM Doc v12.0 — E2E Playwright Pass 1: Scaffold + Full Suite Run

**Status:** Draft  
**Label:** `TODO-e2e-playwright-pass1-author-full-run`  
**Date:** 2026-04-07

---

## 1. User Story

> As **QA / Test Manager**, I want a **Playwright end-to-end test suite** scaffolded in `workflow-ui`, covering all key UI function points, so that every PR can be validated automatically and regressions are caught before they reach UAT.

---

## 2. Scope

Set up Playwright from scratch in `workflow-ui`, write test cases for all key function points, execute the **full suite** once (pass 1), and report results.

---

## 3. Key UI Function Points to Cover

| Area | Function Points |
|------|----------------|
| **Navigation** | App loads; routes `/workflows/`, `/records/` resolve; 404 redirect |
| **Applications list (desktop)** | Search by name; pagination (Next/Prev); Open, Settings, History, Copy, Delete buttons visible |
| **Applications list (mobile)** | Card view renders; ⋯ menu opens History drawer / Copy modal / Delete confirm; card tap navigates |
| **Application FAB (mobile)** | FAB visible; tap opens New Application dialog; drag repositions without opening dialog |
| **Desktop view toggle** | "Desktop view" button switches mobile → table; "Mobile view" switches back |
| **New application dialog** | Open dialog; fill name; submit creates application; cancel closes without creating |
| **Canvas / Artboard** | Canvas loads for an application; nodes visible; pan/zoom gesture recognised |
| **Node editor** | Click node → drawer opens; Description / Rules / Action panels visible; save changes |
| **Workflow Explain** | Explain button visible; clicking opens token prompt or explain modal |
| **Records list** | Route `/records/` loads; table / card renders with data or empty state; pagination visible |

---

## 4. Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC-1 | `@playwright/test` installed as a dev dependency in `workflow-ui` |
| AC-2 | `playwright.config.ts` exists at the repo root with at least one browser project (Chromium) |
| AC-3 | An npm script `"test:e2e"` runs the full suite |
| AC-4 | Test files cover every function-point area listed in §3 |
| AC-5 | The **full suite is executed** (not a subset); results reported in `workflow-agent-teams/docs/ui-test-report-v12.0.md` |
| AC-6 | Results include pass/fail count, any failures, and a brief analysis |
| AC-7 | Test files are committed to `workflow-ui` (path `e2e/`) |
| AC-8 | Canvas / artboard behaviour is **not regressed** by any test setup changes |

---

## 5. Constraints

- Tests run against the live UAT environment (`https://workflow-ui-gamma.vercel.app`) — no local server spin-up needed for pass 1.
- Do **not** require a login flow if the app is publicly accessible; if auth is required, document the blocker.
- Keep tests deterministic — use stable selectors (roles, data-testid, text) over CSS-class selectors.
- Do not add to `dependencies` — only `devDependencies`.

---

## 6. Deliverables

| Deliverable | Location |
|-------------|----------|
| Playwright config | `workflow-ui/playwright.config.ts` |
| E2E test files | `workflow-ui/e2e/*.spec.ts` |
| npm script | `workflow-ui/package.json` — `"test:e2e"` |
| QA run report | `workflow-agent-teams/docs/ui-test-report-v12.0.md` |
