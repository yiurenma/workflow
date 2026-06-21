# Test Doc — Post-mortem: Explain + GitHub OAuth Device Flow (CORS / Same-Origin Proxy)

**Label**: `TODO-postmortem-explain-github-oauth-cors-uat`  
**Version**: 1.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc postmortem-cors-oauth v1.0 + Arch Doc postmortem-cors-oauth v1.0

---

## 1. Purpose

This test doc defines:
1. Verification test cases that the same-origin proxy implementation resolves the CORS issue.
2. A standing UAT rule for the Test Manager covering any feature with browser cross-origin fetch.
3. A network-tab verification checklist to be added to `TEST_CASES_MASTER.md`.

---

## 2. Regression Verification Test Cases

### TC-CORS-01 — Device Flow: device code request succeeds in real browser on Vercel

| Field | Value |
|---|---|
| **ID** | TC-CORS-01 |
| **Feature** | GitHub OAuth Device Flow — Step 1 (device code request) |
| **Environment** | Real browser on Vercel preview URL (NOT localhost) |
| **Precondition** | No `ai_explain_token` in `localStorage`. Vercel preview deployed. |
| **Steps** | 1. Open Vercel preview URL in browser. 2. Click **Explain** (no token). 3. Open DevTools → Network tab. 4. Observe the request for device code. |
| **Expected result** | Network tab shows a request to `/api/github/device-code` (same-origin path). No CORS error in console. Response contains `device_code` and `user_code`. |
| **Pass criteria** | No `CORS policy` error in browser console. Request path is same-origin. |
| **Priority** | P1 — Critical |

---

### TC-CORS-02 — Device Flow: polling request succeeds in real browser on Vercel

| Field | Value |
|---|---|
| **ID** | TC-CORS-02 |
| **Feature** | GitHub OAuth Device Flow — Step 3 (polling for access token) |
| **Environment** | Real browser on Vercel preview URL |
| **Precondition** | Device code request succeeded (TC-CORS-01 passed). |
| **Steps** | 1. Observe Network tab while the UI polls for authorization. |
| **Expected result** | Polling requests go to `/api/github/access-token` (same-origin). No CORS error. |
| **Pass criteria** | No `CORS policy` error. Path is same-origin proxy. |
| **Priority** | P1 — Critical |

---

### TC-CORS-03 — Device Flow does NOT directly call github.com from the browser

| Field | Value |
|---|---|
| **ID** | TC-CORS-03 |
| **Feature** | Same-origin proxy — negative test |
| **Environment** | Real browser on Vercel preview URL |
| **Steps** | 1. Open DevTools → Network tab. 2. Click Explain. 3. Filter requests by `github.com`. |
| **Expected result** | **Zero** requests to `github.com` appear in the browser Network tab. All Device Flow calls go through the same-origin proxy. |
| **Pass criteria** | No direct `github.com` fetch from the browser. |
| **Priority** | P1 — Critical |

---

### TC-CORS-04 — Full end-to-end on Vercel: Device Flow → token → Explain

| Field | Value |
|---|---|
| **ID** | TC-CORS-04 |
| **Feature** | Full Device Flow + Explain on production-like host |
| **Environment** | Real browser on Vercel preview URL |
| **Precondition** | No token. Vercel preview deployed. GitHub OAuth App registered. |
| **Steps** | 1. Click Explain. 2. Complete Device Flow (enter code on GitHub, approve). 3. Return to app. |
| **Expected result** | Token received (`gho_`). Explain call executes. Explanation displayed. No CORS errors at any step. |
| **Priority** | P1 — Critical |

---

### TC-CORS-05 — Proxy: invalid / missing client_id is rejected by proxy (not forwarded)

| Field | Value |
|---|---|
| **ID** | TC-CORS-05 |
| **Feature** | Proxy security — input validation |
| **Steps** | 1. POST `/api/github/device-code` with no `client_id` in body. |
| **Expected result** | Proxy returns `400 Bad Request`. Does not forward to `github.com`. |
| **Priority** | P2 — High |

---

## 3. Standing UAT Rule (to be added to `TEST_CASES_MASTER.md`)

**UAT Rule for Cross-Origin Fetch Features**

> For any feature that involves `fetch` from the browser SPA to a third-party host (OAuth endpoints, external APIs, etc.), the following must be completed **before** Test Manager sign-off:
>
> 1. **Staging URL required**: Delivery Manager provides a Vercel preview URL (or equivalent) to the Test Manager during test planning — before acceptance testing begins.
> 2. **Real-browser acceptance**: Test Manager runs acceptance tests in a real browser on the Vercel preview URL, not `localhost` or mock environments.
> 3. **Network tab verification**: DevTools Network tab is open; tester confirms:
>    - Requests go to same-origin proxy paths (e.g. `/api/github/device-code`)
>    - No requests go directly to the third-party host
>    - No `CORS policy` errors in the browser console
> 4. **Sign-off blocked** if any of the above checks fail.

---

## 4. Network-Tab Verification Checklist

Add to `TEST_CASES_MASTER.md` — Cross-Origin / Proxy Feature QA Checklist:

- [ ] DevTools → Network tab open during full acceptance run.
- [ ] Third-party API calls appear as **same-origin proxy paths** (not the third-party host directly).
- [ ] No `Access to fetch at '...' from origin '...' has been blocked by CORS policy` in browser console.
- [ ] Vercel function logs (or equivalent) confirm the proxy received and forwarded the request.
- [ ] Test environment is **Vercel preview URL** (or production-equivalent), NOT `localhost` / `vite dev`.

---

## 5. Test Manager Planning Checklist (for future features)

When a feature involves `fetch` to any third-party host:

- [ ] Identify all `fetch` / `XMLHttpRequest` / `axios` calls to non-same-origin hosts.
- [ ] Confirm a same-origin proxy is planned (Vercel function / backend route).
- [ ] Request Vercel preview URL from Delivery Manager before writing test cases.
- [ ] Add TC-CORS-01 / TC-CORS-03 equivalent tests (device code request + negative direct-call test) to the feature test doc.
- [ ] Include the network-tab checklist in the QA sign-off.

---

## 6. `TEST_CASES_MASTER.md` Update Instructions

1. Add rows TC-CORS-01 through TC-CORS-05 to a new "CORS / Same-Origin Proxy" section.
2. Add the "UAT Rule for Cross-Origin Fetch Features" from section 3 as a standing rule.
3. Add the Network-Tab Verification Checklist from section 4.
4. Add the Test Manager Planning Checklist from section 5 to the "Feature Test Planning" section.

---

*End of Test Doc — Post-mortem CORS/OAuth — Draft, awaiting approval.*
