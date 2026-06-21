# Test Doc v9.0 — CI/CD: Remove Render Deploy GitHub Actions

**Feature:** `TODO-cicd-remove-render-deploy`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Source Checks

| TC-ID | Check | Expected |
|-------|-------|----------|
| TC-39-L01 | `workflow-operation-api/.github/workflows/render-deploy.yml` deleted | File does not exist |
| TC-39-L02 | `workflow-online-api/.github/workflows/render-deploy.yml` deleted | File does not exist |
| TC-39-L03 | `workflow-operation-api/.github/workflows/ci.yml` still exists | File exists, content unchanged |
| TC-39-L04 | No `RENDER_DEPLOY_HOOK_URL` in any `.github/workflows/*.yml` | `grep -r RENDER_DEPLOY_HOOK_URL .github/` returns empty in both repos |
| TC-39-L05 | `AGENTS.md` CI section updated | References DigitalOcean, no mention of Render hooks |
| TC-39-L06 | `docs/deploy-render.md` archived or deleted | Either does not exist or has ARCHIVED header |

## UAT

No browser UAT needed for this task — it is a pure CI/CD config change. Verification is source-only.

---

*Status: Draft*
