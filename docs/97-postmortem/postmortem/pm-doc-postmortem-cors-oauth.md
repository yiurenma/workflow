# PM Doc — Post-mortem: Explain + GitHub OAuth Device Flow (CORS / Same-Origin Proxy)

**Label**: `TODO-postmortem-explain-github-oauth-cors-uat`  
**Version**: 1.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Scope**: Post-mortem write-up + process improvements (no new feature code)

---

## 1. Background

When the GitHub OAuth Device Flow was implemented in the browser SPA, `fetch` calls went directly to `https://github.com/login/device/code` and `https://github.com/oauth/access_token`. This works via `curl` (server-side / no CORS restrictions) but browsers block cross-origin `fetch` to `github.com` because GitHub does not set `Access-Control-Allow-Origin` headers for these endpoints. The bug is environment-specific — it passes local `vite dev` testing if tests mock the network, but fails in a real Vercel deployment. This post-mortem documents the root cause, why it was not caught, and mandates process improvements.

---

## 2. User Story (Retrospective)

**PM-PM2 — CORS/proxy gap post-mortem**

> **As a** development team member,  
> **I want** a documented post-mortem of the CORS failure in the Device Flow implementation,  
> **so that** we have a shared record of what went wrong, why, and what rules prevent future same-origin / proxy gaps from slipping through.

---

## 3. Scope of This Deliverable

The output of this TODO item is:
1. A **post-mortem write-up** documenting root cause and prevention.
2. A **standing UAT rule**: the Test Manager must receive a staging/Vercel URL before writing acceptance sign-off for any feature involving browser-to-third-party network calls.
3. Any **checklist additions** to the test planning process.

No new production code is shipped as part of this item.

### 3.1 Required inclusions

| # | Required section | Description |
|---|---|---|
| PM2-1 | Root cause | Why CORS blocks browser `fetch` to `github.com` but not terminal `curl` |
| PM2-2 | Detection failure analysis | Why the bug was not caught before the merge |
| PM2-3 | Missing UAT step | Why real-browser testing on a Vercel-like host was not done |
| PM2-4 | Proposed staging / UAT rule | Concrete standing rule for Test Manager |
| PM2-5 | Verification checklist | Network-tab check steps for any cross-origin or proxy feature |

---

## 4. Acceptance Criteria

### AC-PM2-1 — Root cause documented
- The post-mortem explains the CORS mechanism: browsers enforce `Access-Control-Allow-Origin`; CLI tools do not.
- It states that GitHub's Device Flow endpoints (`/login/device/code`, `/oauth/access_token`) do not allow arbitrary web origins.
- It explains the correct fix: route these calls through a **same-origin proxy** (e.g. Vercel rewrites or a backend route) so the browser sees requests going to the same origin.

### AC-PM2-2 — Detection gaps documented
- At least three failure points identified (e.g. local `vite dev` does not enforce CORS the same way as Vercel production; tests may have mocked `fetch`; no "real-browser on Vercel" acceptance check was done before merge).
- Each gap maps to a concrete improvement action.

### AC-PM2-3 — Standing UAT rule defined
- A standing rule is written: **"For any feature involving browser fetch to a third-party host, the Test Manager must verify acceptance in a real-browser on a Vercel preview deployment (or equivalent staging URL), checking the browser Network tab to confirm requests route through the same-origin proxy."**
- This rule must be added to the test planning checklist in `TEST_CASES_MASTER.md` or equivalent.

### AC-PM2-4 — Deployment / staging coordination documented
- The post-mortem notes that the Test Manager must receive a **Vercel preview URL and branch name** during test planning for any feature touching network architecture (auth, proxying, external API calls).
- Delivery Manager is responsible for providing this URL before Test Manager sign-off.

### AC-PM2-5 — Network-tab checklist defined
- A short checklist (3–5 items) is added for cross-origin / proxy features:
  1. Open browser DevTools → Network tab.
  2. Confirm OAuth / external requests target the **same-origin proxy path** (e.g. `/api/github/device-code`), not `github.com` directly.
  3. Confirm no `CORS error` in browser console.
  4. Confirm the proxy receives and forwards the request correctly (check server logs or mock).
  5. Run on the **production-like deployment** (Vercel preview), not only `localhost`.

### AC-PM2-6 — Ownership assigned
- Each improvement action names the responsible role: **Test Manager**, **Delivery Manager**, **Frontend dev**, **DevOps/Infra**.

---

## 5. Out of Scope

| Item | Rationale |
|---|---|
| New production code | The fix is already shipped; this is documentation only |
| Retroactive blame | Post-mortem is blameless |
| CORS policy for other third-party APIs | Out of scope for this post-mortem; each feature is assessed separately |
| GitHub Enterprise Server | Different CORS posture; addressed in a future feature |

---

## 6. Coordination

- **Test Manager**: add the standing UAT rule to `TEST_CASES_MASTER.md`.
- **Delivery Manager**: confirm the Vercel preview deployment process for future features.
- **Frontend dev**: document the same-origin proxy pattern in `arch-doc-postmortem-cors-oauth.md`.

---

*End of PM Doc — Post-mortem CORS/OAuth — Draft, awaiting approval.*
