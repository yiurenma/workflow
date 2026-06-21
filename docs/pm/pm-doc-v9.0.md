# PM Doc v9.0 — CI/CD: Remove Render Deploy GitHub Actions

**Feature:** `TODO-cicd-remove-render-deploy`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Problem Statement

Both API repos contain GitHub Actions workflows that call Render deploy hooks (`RENDER_DEPLOY_HOOK_URL`). Now that services run on DigitalOcean (which deploys automatically from `main`/`develop` branch pushes), these Render workflows:

1. Are no longer needed — DigitalOcean listens to the branch directly.
2. May fail on every push if `RENDER_DEPLOY_HOOK_URL` secret is missing, causing noise in the Actions tab.
3. May double-deploy if the secret is still set.

Documentation (`AGENTS.md`, `docs/deploy-render.md`) still describes Render setup, misleading future contributors.

---

## User Stories

### US-39 — Remove Render Deploy Workflows & Update Docs

**As a** developer contributing to either API repo,  
**I want** the GitHub Actions tab to be clean with no Render-related workflow runs,  
**So that** pushes only trigger the relevant CI (tests) without spurious deploy jobs.

**Acceptance Criteria:**
- AC-39-1: `workflow-operation-api/.github/workflows/render-deploy.yml` is deleted.
- AC-39-2: `workflow-online-api/.github/workflows/render-deploy.yml` is deleted.
- AC-39-3: `workflow-operation-api/AGENTS.md` CI section updated: "DigitalOcean deploys automatically from `main`; Render removed."
- AC-39-4: `workflow-operation-api/docs/deploy-render.md` either deleted or clearly marked as "archived / no longer used".
- AC-39-5: `workflow-operation-api/.github/workflows/ci.yml` is NOT changed (keep running tests on push/PR).
- AC-39-6: No `RENDER_DEPLOY_HOOK_URL` references remain in any active workflow file.

---

*Status: Draft*
