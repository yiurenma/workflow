# PM Doc v8.0 — Hosting: Point APIs to DigitalOcean URLs

**Feature:** `TODO-hosting-digitalocean-urls`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Problem Statement

Both backend APIs have migrated from **Render** to **DigitalOcean App Platform**:

| API | Old (Render) | New (DigitalOcean) |
|-----|-------------|---------------------|
| operation-api | `https://workflow-operation-api.onrender.com` | `https://workflow-operation-api-n9sbp.ondigitalocean.app` |
| online-api | `https://workflow-online-api.onrender.com` | `https://workflow-online-api-nr3e4.ondigitalocean.app` |

Several files in multiple repos still reference the old Render URLs. Until updated, Vercel's production proxy rewrites point at Render (which may be decommissioned) and local dev proxies do too.

---

## User Stories

### US-38 — Update All Render URL References to DigitalOcean

**As a** developer or ops engineer,  
**I want** all config, doc, and proxy files to reference the DigitalOcean URLs,  
**So that** UAT and production traffic routes to the live DigitalOcean services correctly.

**Acceptance Criteria:**
- AC-38-1: `workflow-ui/vercel.json` proxy rewrites point at DigitalOcean (with `/api` path prefix preserved).
- AC-38-2: `workflow-ui/vite.config.ts` dev proxy constants updated to DigitalOcean.
- AC-38-3: `workflow/CLAUDE.md` UAT environment table updated to DigitalOcean URLs.
- AC-38-4: `workflow-operation-api/AGENTS.md` references updated.
- AC-38-5: No `*.onrender.com` URL remains in any touched file.
- AC-38-6: CORS / same-origin proxy pattern unchanged (rewrites still go through `/api/proxy/operation/` and `/api/proxy/online/`).

---

*Status: Draft*
