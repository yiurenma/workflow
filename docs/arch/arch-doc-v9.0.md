# Arch Doc v9.0 — CI/CD: Remove Render Deploy GitHub Actions

**Feature:** `TODO-cicd-remove-render-deploy`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Files to Delete / Modify

### Delete

| File | Reason |
|------|--------|
| `workflow-operation-api/.github/workflows/render-deploy.yml` | Renders Render deploy hook — no longer needed |
| `workflow-online-api/.github/workflows/render-deploy.yml` | Same |

### Modify

**`workflow-operation-api/AGENTS.md`** — update "CI / Render" section:

```markdown
### CI / DigitalOcean

- **GitHub Actions**: `.github/workflows/ci.yml` runs tests on push/PR to `main`/`master`.
- **Deploy**: DigitalOcean App Platform deploys automatically when `main` is pushed. No manual deploy hook required.
```

**`workflow-operation-api/docs/deploy-render.md`** — add a deprecation notice at the top:

```markdown
> **ARCHIVED** — This service is no longer hosted on Render. See DigitalOcean App Platform.
> This document is kept for historical reference only.
```

(Deletion is also acceptable, but keeping with a header avoids a 404 if referenced from old links.)

---

## Repos & Branches Affected

| Repo | Target branch |
|------|--------------|
| `workflow-operation-api` | `main` |
| `workflow-online-api` | `develop` |

---

## Risk

- Deleting a workflow file does not affect running workflows — GitHub only runs workflows that exist in the branch at the time of the push.
- `ci.yml` is untouched; tests still run on push/PR.
- No secrets are affected — `RENDER_DEPLOY_HOOK_URL` can remain as an unused repo secret; it harms nothing.

---

*Status: Draft*
