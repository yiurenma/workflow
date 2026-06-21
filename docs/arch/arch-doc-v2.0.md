# Architect Doc — GitHub OAuth Device Flow for Explain Feature

**Version**: 2.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc v2.0 (US-21)

---

## 1. Scope

This document covers the architectural approach for US-21: adding a GitHub OAuth Device Flow to the **Explain** button in `workflow-ui` when no valid token is stored.

**Affected component**: `workflow-ui` only.  
**No backend changes** — Device Flow is a pure browser-to-GitHub interaction. No new API endpoints, no schema changes, no new environment variables in operation-api or online-api.

---

## 2. GitHub OAuth Device Flow — Protocol Summary

GitHub's Device Flow (RFC 8628) allows public clients (no `client_secret`) to obtain OAuth tokens from a browser without a redirect URI:

```
Step 1: Browser → POST https://github.com/login/device/code
        Body: client_id=<CLIENT_ID>&scope=read:user
        Response: { device_code, user_code, verification_uri,
                    expires_in, interval }

Step 2: UI displays user_code + opens https://github.com/login/device
        User enters user_code on GitHub, approves the app.

Step 3: Browser polls → POST https://github.com/oauth/access_token
        Body: client_id=<CLIENT_ID>&device_code=<device_code>
              &grant_type=urn:ietf:params:oauth:grant-type:device_code
        Accept: application/json
        Responses:
          { error: "authorization_pending" }  → keep polling
          { error: "slow_down", interval: N } → increase interval
          { error: "expired_token" }          → abort
          { error: "access_denied" }          → abort
          { access_token, token_type, scope } → success
```

No redirect URI, no `client_secret`, no backend involvement.

---

## 3. Component Architecture

### 3.1 Affected files in workflow-ui

```
src/routes/workflows/-components/workflow-header/
  index.tsx                ← primary change: orchestrate OAuth flow
  useGitHubDeviceFlow.ts   ← new custom hook: all Device Flow logic
```

All OAuth logic is isolated in `useGitHubDeviceFlow.ts`. The header component calls the hook and renders UI state.

### 3.2 State machine for the Device Flow hook

```
IDLE
  │─ user clicks "Explain" with no valid token
  ▼
REQUESTING_CODE        (POST /login/device/code)
  │─ success
  ▼
AWAITING_USER          (display user_code; user authorizes on GitHub)
  │─ poll interval
  ▼
POLLING                (POST /oauth/access_token repeatedly)
  │─ authorization_pending → stay in POLLING
  │─ slow_down           → increase interval, stay in POLLING
  │─ access_token        → store in localStorage → SUCCESS
  │─ expired_token       → EXPIRED
  │─ access_denied       → DENIED
  │─ network error       → NETWORK_ERROR (retry with back-off, then ERROR)
  │─ user cancels        → IDLE
  ▼
SUCCESS / EXPIRED / DENIED / ERROR
```

### 3.3 Data flow

```mermaid
sequenceDiagram
  participant U as User (Browser)
  participant H as WorkflowHeader
  participant GH_DEV as github.com/login/device/code
  participant GH_POLL as github.com/oauth/access_token
  participant GH_AUTH as github.com/login/device (browser tab)
  participant LS as localStorage

  U->>H: Click "Explain" (no token in localStorage)
  H->>GH_DEV: POST /login/device/code<br/>client_id=..., scope=read:user
  GH_DEV-->>H: { device_code, user_code, interval, expires_in }
  H-->>U: Show OAuth modal:<br/>  - user_code (ABCD-1234)<br/>  - "Open GitHub" button<br/>  - polling spinner

  U->>GH_AUTH: Click "Open GitHub to authorize"<br/>(window.open triggered by gesture)
  GH_AUTH-->>U: User enters user_code, approves OAuth App

  loop Poll every interval seconds
    H->>GH_POLL: POST /oauth/access_token<br/>client_id=..., device_code=..., grant_type=device_code
    GH_POLL-->>H: { error: "authorization_pending" } or access_token
  end

  H->>LS: Store access_token as ai_explain_token
  H->>H: Close OAuth modal; call AI Explain immediately
  H-->>U: Render explanation result
```

ASCII fallback:
```
Browser
  │
  ├─[no token]──► POST github.com/login/device/code → device_code + user_code
  │
  ├─ Display modal: user_code + "Open GitHub" button
  │
  ├─[user gesture]──► window.open(github.com/login/device)
  │
  └─[poll loop]──► POST github.com/oauth/access_token
                      │─ pending → keep polling
                      └─ access_token → store localStorage → run Explain
```

---

## 4. Security Analysis

| Concern | Detail | Decision |
|---|---|---|
| **No client_secret** | Device Flow for public clients requires only `client_id` — no secret. `client_id` is safe to embed in frontend code (public identifier only). | Accepted — by protocol design |
| **Token in localStorage** | Same as existing PAT flow. XSS on the same origin can read it. Accepted trade-off for internal operator tooling. | Accepted — consistent with Arch Doc v1.0 A.4 |
| **Scope** | Request only `read:user` — minimum for GitHub Models. Broader write scopes are never requested. | Comply with AC-21-7 |
| **CSRF / state** | Device Flow has no redirect callback, so there is no CSRF risk — no `state` parameter needed. The `device_code` is bound server-side to the session on GitHub. | N/A |
| **Popup blockers** | `window.open` called from a direct user gesture (button click) on the modal, not from the initial Explain click. Browsers allow popups from direct gestures. | AC-21-8 complied |
| **Token scope leakage** | `ghp_` PATs created via Device Flow carry scopes explicitly approved by the user on GitHub's consent screen — no more than requested. | Complied |

---

## 5. Implementation Plan

### 5.1 New file: `useGitHubDeviceFlow.ts`

```typescript
// Responsibilities:
// - POST /login/device/code to get device_code + user_code
// - Poll /oauth/access_token at correct interval
// - Handle all error codes: authorization_pending, slow_down,
//   expired_token, access_denied, network errors
// - Store token in localStorage on success
// - Return state machine state + user_code + callbacks (cancel, openGitHub)
// - Clean up polling interval on unmount / cancel
```

Key exports:
```typescript
type DeviceFlowState =
  | { status: "idle" }
  | { status: "requesting" }
  | { status: "awaiting_user"; userCode: string; verificationUri: string }
  | { status: "polling"; userCode: string; verificationUri: string }
  | { status: "success" }
  | { status: "expired" }
  | { status: "denied" }
  | { status: "error"; message: string };

function useGitHubDeviceFlow(clientId: string, onSuccess: (token: string) => void): {
  state: DeviceFlowState;
  start: () => void;
  cancel: () => void;
};
```

### 5.2 Changes to `workflow-header/index.tsx`

1. Import `useGitHubDeviceFlow`
2. On Explain click: if no valid token → call `deviceFlow.start()` instead of showing manual modal
3. Add Device Flow modal rendering (user_code display, "Open GitHub" button, status, cancel, "Paste token manually" fallback)
4. On `deviceFlow.onSuccess`: token already stored by hook → call `explainWithToken(token)` immediately
5. Existing manual token modal unchanged — reachable via "Paste a token manually" link

### 5.3 GitHub client_id

- Stored as a compile-time constant `GITHUB_OAUTH_CLIENT_ID` in the hook file.
- Value provided by the team admin who registers the OAuth App (per AC-21 coordination note in PM doc).
- **Not** a secret — safe to commit.

---

## 6. Trade-offs

| Trade-off | Rationale |
|---|---|
| **No backend proxy** | Device Flow needs no `client_secret` → zero backend risk. Full UX achievable in browser only. |
| **localStorage persistence** | Consistent with existing behaviour. Production hardening (httpOnly cookies) is future work. |
| **`read:user` scope only** | Minimum viable scope for GitHub Models. If GitHub requires a different scope in future, update the constant. |
| **No token refresh** | GitHub PATs from Device Flow don't expire unless the app is revoked or token TTL is set. Manual re-auth required only after revocation. |
| **Anthropic path unchanged** | Anthropic has no equivalent browser OAuth flow; manual entry is the correct path for Anthropic keys. |

---

## 7. Architectural Decisions

| # | Decision | Resolution |
|---|---|---|
| AD-21-1 | Backend vs browser-only for Device Flow | **Browser-only** — Device Flow requires no client_secret; no backend involvement needed |
| AD-21-2 | OAuth App vs GitHub App | **OAuth App** — simpler registration, no installation required, sufficient for GitHub Models |
| AD-21-3 | Token storage | **localStorage** — consistent with existing Explain token storage; accepted risk for internal tooling |
| AD-21-4 | Scope requested | **`read:user`** — minimum viable for GitHub Models API |
| AD-21-5 | State management | **Custom React hook** (`useGitHubDeviceFlow`) — keeps polling/timer logic out of the component |

---

## 8. What is NOT Changed

- `workflow-operation-api` — no changes
- `workflow-online-api` — no changes
- Database — no migrations
- Environment variables (`.env`) — no new variables (client_id is a public constant, not a secret)
- Existing manual token entry modal — preserved as fallback (AC-21-5)
- Anthropic token path — unchanged

---

*End of Arch Doc v2.0 — awaiting Test Manager review and human approval.*
