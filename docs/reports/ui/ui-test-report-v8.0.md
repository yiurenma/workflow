# UI Test Report v8.0 — Hosting: DigitalOcean URLs

**Feature:** `TODO-hosting-digitalocean-urls`  
**Date:** 2026-04-07  
**Tester:** QA Agent

---

## Source Checks

| TC-ID | Check | Result |
|-------|-------|--------|
| TC-38-L01 | `vercel.json` operation destination | ✅ PASS — `workflow-operation-api-n9sbp.ondigitalocean.app/api/$1` |
| TC-38-L02 | `vercel.json` online destination | ✅ PASS — `workflow-online-api-nr3e4.ondigitalocean.app/api/$1` |
| TC-38-L03 | `vite.config.ts` DO_OPERATION constant | ✅ PASS |
| TC-38-L04 | `vite.config.ts` DO_ONLINE constant | ✅ PASS |
| TC-38-L05 | `CLAUDE.md` UAT table | ✅ PASS — both rows updated to ondigitalocean.app |
| TC-38-L06 | `AGENTS.md` no onrender.com | ✅ PASS — section updated to DigitalOcean |
| TC-38-L07 | No onrender.com in touched files | ✅ PASS |

## API Smoke Tests

| TC-ID | Check | Result |
|-------|-------|--------|
| TC-38-A01 | `GET .../actuator/health` operation-api | ✅ PASS — HTTP 403 (server live, endpoint auth-gated) |
| TC-38-A02 | `GET .../actuator/health` online-api | ✅ PASS — HTTP 403 (server live, endpoint auth-gated) |

**Source + smoke: 9/9 PASS**

## Browser UAT — pending human execution

*Open UAT app, confirm Applications list loads and Run modal works.*

---
