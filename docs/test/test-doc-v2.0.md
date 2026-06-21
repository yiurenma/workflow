# Test Doc — GitHub OAuth Device Flow for Explain Feature

**Version**: 2.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc v2.0 (US-21) + Arch Doc v2.0  
**Scope**: workflow-ui — `useGitHubDeviceFlow` hook + `WorkflowHeader` component

---

## 1. Test Strategy

| Layer | Approach |
|---|---|
| Unit — hook logic | Vitest + `@testing-library/react` renderHook; mock `fetch` with `vi.fn()` |
| Unit — component | Vitest + `@testing-library/react`; mock the hook |
| Integration (manual) | Real GitHub OAuth App (dev registration); verify end-to-end flow in browser |
| Regression | Run existing Explain unit tests; confirm no breakage |

---

## 2. Test Cases — `useGitHubDeviceFlow` hook

### TC-21-H-01 — Happy path: token acquired

**Precondition**: `fetch` returns valid `device_code` response, then `authorization_pending` once, then `access_token`.  
**Steps**:
1. Call `start()` on the hook.
2. Verify state transitions: `requesting` → `awaiting_user` → `polling`.
3. After simulated `access_token` response, verify state = `success`.
4. Verify `onSuccess` callback called with the token string.
5. Verify token is written to `localStorage["ai_explain_token"]`.

**Pass**: All assertions pass; `onSuccess` called exactly once.

---

### TC-21-H-02 — `authorization_pending` loop then success

**Precondition**: `fetch` for polling returns `authorization_pending` three times, then `access_token`.  
**Steps**:
1. Call `start()`.
2. Fast-forward timers through 3 polling intervals.
3. On 4th poll, return `access_token`.

**Pass**: State = `success`; `onSuccess` called once after 4th poll.

---

### TC-21-H-03 — `slow_down`: interval increases

**Precondition**: First poll returns `slow_down` with `interval: 10`; second returns `access_token`.  
**Steps**:
1. Call `start()` with initial interval 5 s.
2. Advance timer 5 s → poll returns `slow_down`.
3. Verify next poll fires after 10 s (not 5 s).
4. Advance timer 10 s → poll returns `access_token`.

**Pass**: State = `success`; polling interval increased correctly.

---

### TC-21-H-04 — `expired_token`: state transitions to `expired`

**Precondition**: Poll returns `expired_token`.  
**Steps**:
1. Call `start()`.
2. Advance timer to first poll → returns `expired_token`.

**Pass**: State = `expired`; polling stops; `onSuccess` not called.

---

### TC-21-H-05 — `access_denied`: state transitions to `denied`

**Precondition**: Poll returns `access_denied`.  
**Steps**: Same as TC-21-H-04 but with `access_denied`.

**Pass**: State = `denied`; polling stops; `onSuccess` not called.

---

### TC-21-H-06 — Network error with retry then success

**Precondition**: `fetch` throws network error twice, then returns `access_token`.  
**Steps**:
1. Call `start()`.
2. First two polls throw `TypeError: Failed to fetch`.
3. Third poll returns `access_token`.

**Pass**: State = `success` after 3rd attempt; `onSuccess` called.

---

### TC-21-H-07 — Network error exhausted: state = `error`

**Precondition**: `fetch` throws network error 4 times (exceeds max 3 retries).  
**Steps**: Advance timers through 4 polls, all throw.

**Pass**: State = `error` with descriptive message; polling stops.

---

### TC-21-H-08 — Cancel during polling

**Precondition**: Device Flow in `polling` state.  
**Steps**:
1. Call `start()`.
2. Advance to `polling` state.
3. Call `cancel()`.

**Pass**: State = `idle`; polling interval cleared; no further `fetch` calls after cancel.

---

### TC-21-H-09 — Cancel during `awaiting_user`

**Precondition**: Device Flow in `awaiting_user` state (waiting for user to open GitHub).  
**Steps**:
1. Call `start()` → state = `awaiting_user`.
2. Call `cancel()` before first poll.

**Pass**: State = `idle`; no polling starts.

---

### TC-21-H-10 — Hook cleanup on unmount

**Precondition**: Device Flow in `polling` state; component unmounted.  
**Steps**:
1. Advance to `polling`.
2. Unmount the component/hook.
3. Advance timers further.

**Pass**: No `fetch` calls occur after unmount; no React state-update-on-unmounted warnings.

---

## 3. Test Cases — `WorkflowHeader` component

### TC-21-C-01 — No token: OAuth modal shown instead of manual modal

**Precondition**: `localStorage["ai_explain_token"]` is empty.  
**Steps**:
1. Render `WorkflowHeader`.
2. Click **Explain** button.

**Pass**: OAuth Device Flow modal appears (showing "Authorise with GitHub" heading); manual token input modal does NOT appear.

---

### TC-21-C-02 — Valid token present: no OAuth modal

**Precondition**: `localStorage["ai_explain_token"] = "ghp_testtoken"`.  
**Steps**:
1. Render `WorkflowHeader`.
2. Click **Explain** button.

**Pass**: OAuth modal does NOT appear; AI call fires directly (hook mocked to return explanation).

---

### TC-21-C-03 — OAuth modal shows user_code and "Open GitHub" button

**Precondition**: Hook mocked to state `awaiting_user` with `userCode = "ABCD-1234"`.  
**Steps**:
1. Render component with mocked hook in `awaiting_user` state.

**Pass**: Modal contains text `ABCD-1234`; "Open GitHub to authorize" button is visible and enabled.

---

### TC-21-C-04 — "Open GitHub" button calls `window.open`

**Precondition**: Hook in `awaiting_user` state; `window.open` mocked.  
**Steps**:
1. Click "Open GitHub to authorize" button.

**Pass**: `window.open` called with `https://github.com/login/device`; second argument = `_blank`.

---

### TC-21-C-05 — "Paste a token manually" link opens manual modal

**Precondition**: OAuth modal open.  
**Steps**:
1. Click "Paste a token manually" link.

**Pass**: OAuth modal closes (or hides); manual token `Input.Password` field appears.

---

### TC-21-C-06 — Cancel button returns to idle

**Precondition**: OAuth modal open; hook in `polling` state.  
**Steps**:
1. Click **Cancel** button on OAuth modal.

**Pass**: Modal closes; `cancel()` called on hook; state returns to `idle`.

---

### TC-21-C-07 — `expired` state shows correct error message

**Precondition**: Hook mocked to state `expired`.  
**Steps**:
1. Render component with hook in `expired` state.

**Pass**: Modal shows "Authorization timed out — please try again." No polling spinner visible.

---

### TC-21-C-08 — `denied` state shows correct error + fallback hint

**Precondition**: Hook mocked to state `denied`.  
**Steps**:
1. Render component with hook in `denied` state.

**Pass**: Modal shows "Authorization was denied on GitHub. You can also paste a token manually."

---

### TC-21-C-09 — On success: modal closes and Explain result modal opens

**Precondition**: Hook transitions to `success`; AI API mocked to return explanation text.  
**Steps**:
1. Hook fires `onSuccess("ghp_newtoken")`.

**Pass**: OAuth modal closes; Explain result modal opens showing AI explanation; no second Explain click required.

---

### TC-21-C-10 — Existing "Clear token" button still works

**Precondition**: Token present in localStorage; Explain result modal open.  
**Steps**:
1. Click **Clear token** button.

**Pass**: `localStorage["ai_explain_token"]` is removed; message "AI token cleared" shown.

---

## 4. Test Cases — Regression

### TC-21-R-01 — Anthropic token path unchanged

**Precondition**: `localStorage["ai_explain_token"] = "sk-ant-testkey"`.  
**Steps**:
1. Click **Explain**.

**Pass**: OAuth modal does NOT appear; `fetch` call goes to `api.anthropic.com` (not GitHub Device Flow endpoints).

---

### TC-21-R-02 — Unknown token prefix still shows error

**Precondition**: `localStorage["ai_explain_token"] = "invalidtoken"`.  
**Steps**:
1. Click **Explain**.

**Pass**: Error message "Unrecognised token format" shown; no OAuth flow initiated.

---

### TC-21-R-03 — Run button unaffected

**Steps**:
1. Click **Run** button with no token set.

**Pass**: Run modal opens as before; no OAuth modal.

---

### TC-21-R-04 — Save button unaffected

**Steps**:
1. Click **Save** with no token set.

**Pass**: Save proceeds normally; no OAuth modal.

---

## 5. Manual End-to-End Test (browser)

**Environment**: Local dev (`npm run dev`); real GitHub OAuth App registered with callback `http://localhost:5173`.

| Step | Action | Expected |
|---|---|---|
| 1 | Clear `localStorage["ai_explain_token"]` | — |
| 2 | Navigate to any workflow canvas | Canvas loads |
| 3 | Click **Explain** | OAuth modal appears with a user code |
| 4 | Click "Open GitHub to authorize" | New tab opens at `github.com/login/device` |
| 5 | Enter user code on GitHub; approve app | GitHub confirms authorization |
| 6 | Return to workflow tab | Modal shows "Authorized!" or closes; Explain result appears |
| 7 | Check `localStorage` | `ai_explain_token` key present with `ghp_` token |
| 8 | Click **Explain** again | No OAuth modal; result appears immediately |
| 9 | Click "Clear token" | Token removed from localStorage |
| 10 | Repeat from step 3 | OAuth flow restarts correctly |

---

## 6. Test Exclusions

| Scenario | Reason excluded |
|---|---|
| GitHub Enterprise Server | Out of scope per PM Doc v2.0 |
| Org admin OAuth app approval | Infrastructure concern; cannot be automated |
| httpOnly cookie token storage | Out of scope; future hardening |

---

*End of Test Doc v2.0 — awaiting human approval before implementation.*
