# Arch Doc — Post-mortem: Explain + GitHub OAuth Device Flow (CORS / Same-Origin Proxy)

**Label**: `TODO-postmortem-explain-github-oauth-cors-uat`  
**Version**: 1.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc — Post-mortem CORS/OAuth v1.0

---

## 1. Root Cause

### 1.1 CORS primer

Browsers enforce the **Same-Origin Policy**: a script running at `https://workflow.example.com` cannot read the response of a `fetch` request to `https://github.com` unless GitHub includes an `Access-Control-Allow-Origin` header in the response that permits the request origin.

GitHub's OAuth Device Flow endpoints:

```
POST https://github.com/login/device/code
POST https://github.com/oauth/access_token
```

…do **not** include permissive CORS headers for arbitrary web origins. Therefore, a browser SPA that `fetch`es these URLs directly will receive a **CORS error** and the request will fail before the JavaScript code can read the response body.

`curl`, Postman, and server-side code are **not subject to CORS** — they make raw TCP connections and read the response regardless of headers. This is why local development via `curl` appeared to work while the browser SPA did not.

### 1.2 Why the SPA `fetch` failed

In the US-21 implementation, `useGitHubDeviceFlow.ts` called:

```typescript
// Step 1 — directly to github.com — BLOCKED in browser:
fetch("https://github.com/login/device/code", { method: "POST", ... })

// Step 3 polling — directly to github.com — BLOCKED in browser:
fetch("https://github.com/oauth/access_token", { method: "POST", ... })
```

These succeed in `vite dev` only if:
- The dev server has a proxy configured (it did not), **or**
- The fetch is mocked (tests may have done this), **or**
- CORS is disabled in the test browser (a testing anti-pattern).

On Vercel (production), the browser enforces CORS strictly, so both calls fail.

### 1.3 Correct pattern: same-origin proxy

The fix routes these calls through a **same-origin proxy** — a Vercel serverless function or a `/api/github/…` route that forwards the request server-side:

```
Browser (SPA)
  │
  ├─ POST /api/github/device-code          ← same origin → no CORS
  │     (Vercel serverless function)
  │       │
  │       └─ POST https://github.com/login/device/code  ← server-to-server → no CORS
  │
  └─ POST /api/github/access-token         ← same origin → no CORS
        (Vercel serverless function)
          │
          └─ POST https://github.com/oauth/access_token ← server-to-server → no CORS
```

The SPA never calls `github.com` directly — only the server-side proxy does.

---

## 2. Why It Was Not Caught

### 2.1 No real-browser test on Vercel deployment

Development and testing were done with `vite dev` locally. `vite dev` may have had a dev proxy configured or the tests mocked `fetch` — masking the CORS issue. No acceptance test was run on a **Vercel preview deployment** (or equivalent staging URL that matches production network configuration).

### 2.2 `curl` used as a proxy for browser behavior

Manual testing likely used `curl` to verify the GitHub Device Flow endpoints responded correctly. `curl` does not enforce CORS, so it passed. The distinction "does this work from a browser on the production host?" was not explicitly checked.

### 2.3 No "cross-origin fetch" checklist item in test planning

The Test Manager did not have a standing rule requiring: "any feature involving `fetch` to a third-party host must be verified in a real browser on a production-equivalent host, with the browser Network tab confirming no CORS errors."

---

## 3. Correct Architecture: Same-Origin Proxy Pattern

### 3.1 Vercel serverless function approach

Add two Vercel serverless API routes:

```
workflow-ui/api/github/device-code.ts
workflow-ui/api/github/access-token.ts
```

Each function:
1. Receives the POST from the browser SPA.
2. Forwards it to the corresponding `github.com` endpoint (server-side, no CORS restrictions).
3. Returns GitHub's JSON response to the browser.

`vercel.json` rewrites map `/api/github/*` to these functions.

### 3.2 Updated `useGitHubDeviceFlow.ts` call sites

```typescript
// Before (CORS-blocked in browser):
fetch("https://github.com/login/device/code", ...)
fetch("https://github.com/oauth/access_token", ...)

// After (same-origin proxy — no CORS issue):
fetch("/api/github/device-code", ...)
fetch("/api/github/access-token", ...)
```

### 3.3 Security considerations

| Concern | Detail | Decision |
|---|---|---|
| `client_id` exposure | `client_id` is a public identifier — safe to pass through the proxy or embed in SPA | Accepted |
| No `client_secret` | Device Flow for OAuth Apps requires no `client_secret` — proxy does not introduce a secret | By design |
| Request validation | Proxy should validate that the request body contains expected parameters (client_id, device_code, grant_type) before forwarding | Recommended |
| Rate limiting | GitHub rate-limits Device Flow polling; proxy should pass through GitHub's `interval` and respect `slow_down` | Required |

---

## 4. Required UAT Rule (Standing Process Change)

The following rule must be added to the Test Manager process and `TEST_CASES_MASTER.md`:

> **UAT Rule — Cross-origin fetch features**: For any feature that involves `fetch` from the browser SPA to a third-party host (OAuth endpoints, external APIs, etc.), the Test Manager must:
> 1. Receive a **Vercel preview URL** (or equivalent production-like staging URL) from the Delivery Manager before writing acceptance sign-off.
> 2. Execute acceptance tests in a **real browser** on that URL (not localhost).
> 3. Open the browser **Network tab** and confirm that requests go to **same-origin proxy paths** (e.g. `/api/github/device-code`), not directly to the third-party host.
> 4. Confirm **no CORS error** appears in the browser console.

---

## 5. Network-Tab Verification Checklist

For cross-origin or proxy features, add the following to the QA sign-off:

- [ ] DevTools → Network tab open during acceptance run.
- [ ] OAuth / external API requests show **same-origin path** (e.g. `/api/github/device-code`), not `github.com` directly.
- [ ] No `Access to fetch at 'https://...' from origin '...' has been blocked by CORS policy` in browser console.
- [ ] Server-side proxy logs (or Vercel function logs) show the forwarded request arriving.
- [ ] Acceptance run on **Vercel preview URL**, not `localhost`.

---

## 6. Affected Files (For Reference)

| File | Nature of change |
|---|---|
| `workflow-ui/src/routes/workflows/-components/workflow-header/useGitHubDeviceFlow.ts` | Update fetch URLs to use `/api/github/device-code` and `/api/github/access-token` |
| `workflow-ui/api/github/device-code.ts` | New Vercel serverless function (proxy) |
| `workflow-ui/api/github/access-token.ts` | New Vercel serverless function (proxy) |
| `workflow-ui/vercel.json` | Add rewrites for `/api/github/*` if not already routing correctly |
| `workflow-agent-teams/docs/TEST_CASES_MASTER.md` | Add UAT rule + network-tab checklist |

Note: These are the files that **would** be touched in an implementation fix. This post-mortem is documentation only — the fix is assumed already shipped. If the fix has not been shipped, this arch doc should be converted to an implementation arch doc and routed through the full PM → Arch → Test → Approval → Implementation flow.

---

*End of Arch Doc — Post-mortem CORS/OAuth — Draft, awaiting approval.*
