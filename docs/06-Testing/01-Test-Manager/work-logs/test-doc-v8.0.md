# Test Doc v8.0 — Hosting: Point APIs to DigitalOcean URLs

**Feature:** `TODO-hosting-digitalocean-urls`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Source Checks

| TC-ID | Check | Expected |
|-------|-------|----------|
| TC-38-L01 | `vercel.json` operation-api destination | `workflow-operation-api-n9sbp.ondigitalocean.app/api/$1` |
| TC-38-L02 | `vercel.json` online-api destination | `workflow-online-api-nr3e4.ondigitalocean.app/api/$1` |
| TC-38-L03 | `vite.config.ts` operation constant | `DO_OPERATION = 'https://workflow-operation-api-n9sbp.ondigitalocean.app'` |
| TC-38-L04 | `vite.config.ts` online constant | `DO_ONLINE = 'https://workflow-online-api-nr3e4.ondigitalocean.app'` |
| TC-38-L05 | `CLAUDE.md` UAT table | Both rows show `.ondigitalocean.app` URLs |
| TC-38-L06 | `AGENTS.md` — no `onrender.com` left | `grep onrender.com AGENTS.md` returns empty |
| TC-38-L07 | No `onrender.com` anywhere in touched files | `grep -r onrender.com` returns empty across all changed files |

## API Smoke Tests (agent verifies directly)

| TC-ID | Check | Expected |
|-------|-------|----------|
| TC-38-A01 | `GET https://workflow-operation-api-n9sbp.ondigitalocean.app/actuator/health` | `{"status":"UP"}` |
| TC-38-A02 | `GET https://workflow-online-api-nr3e4.ondigitalocean.app/actuator/health` | `{"status":"UP"}` |

## UAT (human verifies in browser)

| TC-ID | Step | What to check |
|-------|------|---------------|
| TC-38-B01 | Hard-refresh UAT app; open Applications page | Applications list loads (operation-api reachable) |
| TC-38-B02 | Open any workflow → click Run → Send | Online API responds (online-api reachable) |

---

*Status: Draft*
