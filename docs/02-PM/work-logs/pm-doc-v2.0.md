# PM Doc — GitHub OAuth for Explain Feature

**Version**: 2.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Scope**: workflow-ui only — no backend changes required  
**Input**: TODO.md item "Explain: no GitHub token → send user to GitHub to authorize"

---

## 1. Background

The **Explain** button (US-20, shipped) lets users get an AI explanation of their workflow canvas. It currently requires the user to manually paste a GitHub personal access token (PAT) or Anthropic API key into a modal. When no token is stored in `localStorage`, the modal prompts for manual input — but there is no guidance or automated path to obtain a token.

This release (US-21) closes that gap: when the user clicks **Explain** and no valid GitHub token is stored, the UI **automatically initiates a GitHub OAuth Device Flow** so the user can authorize with GitHub without ever leaving the mental context of the workflow editor.

---

## 2. User Story

**US-21 — GitHub OAuth for Explain**

> **As an** integration engineer using the Workflow Studio,  
> **I want** clicking **Explain** to automatically guide me through GitHub OAuth authorization when I don't have a GitHub token,  
> **so that** I can use GitHub Models for workflow explanation without manually creating or copying a personal access token.

---

## 3. Acceptance Criteria

### AC-21-1 — Trigger condition

- When the user clicks **Explain** and `localStorage` contains no value for `ai_explain_token`, **or** the stored value does not begin with `ghp_`, `ghu_`, `ghs_`, or `sk-ant-`, the UI **must** present the GitHub OAuth Device Flow rather than the existing manual token modal.

### AC-21-2 — Device Flow initiation

- The UI calls GitHub's Device Flow authorization endpoint to obtain a `device_code` and `user_code`.
- The UI displays:
  - The **user code** (e.g. `ABCD-1234`) prominently so the user can copy it.
  - A **"Open GitHub to authorize"** button that opens `https://github.com/login/device` in a new browser tab.
  - A status indicator that polling is in progress.
  - A **Cancel** button.

### AC-21-3 — Polling and token acquisition

- The UI polls GitHub's token endpoint at the interval specified by the Device Flow response (`interval` seconds, default 5 s).
- When GitHub returns `access_token`:
  - The token is stored in `localStorage` under `ai_explain_token`.
  - The OAuth modal closes automatically.
  - The Explain call executes immediately using the new token — the user does not need to click Explain again.

### AC-21-4 — Error handling

| GitHub error code | User-visible behaviour |
|---|---|
| `authorization_pending` | Keep polling silently |
| `slow_down` | Increase polling interval by the `interval` delta returned by GitHub; continue |
| `expired_token` | Show "Authorization timed out — please try again." Stop polling. |
| `access_denied` | Show "Authorization was denied on GitHub. You can also paste a token manually." Stop polling. |
| Network error | Show "Network error — retrying…"; retry with exponential back-off (max 3 retries then stop) |

### AC-21-5 — Manual token fallback

- A **"Paste a token manually"** link is visible at all times during the Device Flow modal.
- Clicking it closes the Device Flow modal and opens the existing manual-token-entry modal.

### AC-21-6 — Existing token path unchanged

- If `ai_explain_token` is already present in `localStorage` and its prefix is recognised (`sk-ant-`, `ghp_`, `ghu_`, `ghs_`), clicking **Explain** proceeds directly to the AI call — no OAuth modal is shown.
- The existing **"Clear token"** button in the Explain result modal continues to work as before.

### AC-21-7 — Security & scope

- The OAuth Device Flow requests **only** the `read:user` scope (minimum required for GitHub Models inference). No write scopes are requested.
- The `client_id` used is a public GitHub OAuth App registered for this product.
- No `client_secret` is involved — Device Flow for public clients does not require one.
- The resulting token is stored in `localStorage` (same security posture as the existing PAT flow — accepted trade-off for internal tooling per Arch Doc v1.0 A.4).

### AC-21-8 — UX: no popup blockers

- GitHub authorization opens via `window.open()` triggered directly by a user gesture (the "Open GitHub to authorize" button click), not by the initial Explain click. This avoids popup-blocker issues.
- If `window.open` is blocked, the UI falls back to displaying the authorization URL as a copyable link.

### AC-21-9 — No backend changes

- The Device Flow is implemented entirely in the browser.
- No new environment variables are required beyond the public `client_id`.
- No server-side token exchange is needed.

---

## 4. Out of Scope

| Item | Rationale |
|---|---|
| GitHub Enterprise Server support | Different host URLs; out of scope for this release |
| Org-level OAuth app approval | Admin concern; documented as a known risk |
| Automatic token refresh / rotation | Future enhancement |
| Replacing Anthropic token path | Anthropic does not offer a device/browser OAuth flow; manual entry remains |
| Backend proxy for token exchange | Not needed for Device Flow (no client secret) |
| httpOnly cookie or platform secret storage | Future security hardening; out of scope |

---

## 5. Coordination

- **Product / scope confirmation**: Shuang Xia Wen — confirm OAuth App `client_id` and which GitHub org should own the app registration before implementation begins.
- **GitHub OAuth App registration**: must be done by an org admin before the feature can be tested end-to-end.

---

*End of PM Doc v2.0 — awaiting Architect review and human approval.*
