# Test Doc — Post-mortem: `gho_` Device-Flow Token Rejected as "Unrecognised token format"

**Label**: `TODO-postmortem-explain-github-gho-token-validation`  
**Version**: 1.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc postmortem-gho-token v1.0 + Arch Doc postmortem-gho-token v1.0

---

## 1. Purpose

This test doc defines:
1. Verification that the shipped fix (commit 263b483) correctly resolves the regression.
2. New test matrix rows to be added to `TEST_CASES_MASTER.md` to prevent recurrence.
3. Process checklist additions for the Test Manager and code reviewer.

---

## 2. Regression Verification Test Cases

These test cases verify the shipped fix. They may be run as manual or automated acceptance tests.

### TC-AUTH-10 — Device Flow `gho_` token accepted by Explain

| Field | Value |
|---|---|
| **ID** | TC-AUTH-10 |
| **Feature** | Explain — token validation |
| **Precondition** | `localStorage` contains `ai_explain_token` = `gho_XXXX` (a real or mocked `gho_`-prefixed value) |
| **Steps** | 1. Open workflow editor. 2. Click **Explain**. |
| **Expected result** | `isValidToken` returns `true` (no re-prompt). `callAI` routes to GitHub Models path. Explain either succeeds or fails with a GitHub Models API error — **never** with "Unrecognised token format". |
| **Pass criteria** | No "Unrecognised token format" error. |
| **Priority** | P1 — Critical |

---

### TC-AUTH-11 — `gho_` token not rejected at validation gate

| Field | Value |
|---|---|
| **ID** | TC-AUTH-11 |
| **Feature** | Explain — token validation |
| **Precondition** | `localStorage` contains `ai_explain_token` = `gho_XXXX` |
| **Steps** | 1. Open workflow editor. 2. Click **Explain**. |
| **Expected result** | The OAuth Device Flow modal does **not** open. The Explain call proceeds immediately. |
| **Pass criteria** | No Device Flow modal. No manual token prompt. |
| **Priority** | P1 — Critical |

---

### TC-AUTH-12 — Classic PAT `ghp_` still accepted (regression guard)

| Field | Value |
|---|---|
| **ID** | TC-AUTH-12 |
| **Feature** | Explain — token validation |
| **Precondition** | `localStorage` `ai_explain_token` = `ghp_XXXX` |
| **Steps** | Click Explain. |
| **Expected result** | Routes to GitHub Models path. No "Unrecognised token" error. |
| **Priority** | P2 — High |

---

### TC-AUTH-13 — Fine-grained PAT `github_pat_` accepted (regression guard)

| Field | Value |
|---|---|
| **ID** | TC-AUTH-13 |
| **Feature** | Explain — token validation |
| **Precondition** | `localStorage` `ai_explain_token` = `github_pat_XXXX` |
| **Steps** | Click Explain. |
| **Expected result** | Routes to GitHub Models path. No error. |
| **Priority** | P2 — High |

---

### TC-AUTH-14 — Anthropic key `sk-ant-` accepted (regression guard)

| Field | Value |
|---|---|
| **ID** | TC-AUTH-14 |
| **Feature** | Explain — token validation |
| **Precondition** | `localStorage` `ai_explain_token` = `sk-ant-XXXX` |
| **Steps** | Click Explain. |
| **Expected result** | Routes to Anthropic path. No "Unrecognised token" error. |
| **Priority** | P2 — High |

---

### TC-AUTH-15 — Unrecognised prefix rejected (negative test)

| Field | Value |
|---|---|
| **ID** | TC-AUTH-15 |
| **Feature** | Explain — token validation |
| **Precondition** | `localStorage` `ai_explain_token` = `xyz_XXXX` |
| **Steps** | Click Explain. |
| **Expected result** | `isValidToken` returns `false`. User is prompted (Device Flow or manual token modal). `callAI` is NOT called with this token. |
| **Priority** | P2 — High |

---

### TC-AUTH-16 — End-to-end: Device Flow authorize → Explain succeeds

| Field | Value |
|---|---|
| **ID** | TC-AUTH-16 |
| **Feature** | Explain — full Device Flow path |
| **Precondition** | No `ai_explain_token` in `localStorage`. GitHub OAuth App registered. |
| **Steps** | 1. Click Explain (no token). 2. Complete Device Flow on GitHub (enter user code, approve). 3. Observe UI after authorization. |
| **Expected result** | UI receives `gho_` token. Stores it. Calls `callAI` immediately. Explain result appears. No "Unrecognised token" error at any step. |
| **Priority** | P1 — Critical |
| **Note** | May require real GitHub account and registered OAuth App. Can be mocked in unit tests. |

---

## 3. Process Checklist Additions

### 3.1 Test Manager checklist (for future auth features)

When a feature adds or modifies token handling or OAuth:

- [ ] Define a test matrix row for **every** token type the feature accepts (positive cases).
- [ ] Define at least one **negative** row: an unrecognised token type that must be rejected.
- [ ] If Device Flow or OAuth is involved: include an end-to-end test (TC-AUTH-16 pattern) covering the full browser authorization round-trip.
- [ ] Confirm that ALL code sites performing token-type detection are listed in the test scope (not just one).

### 3.2 Code reviewer checklist (for auth PRs)

- [ ] Is token-type detection done in **one shared helper**? If not, flag for consolidation.
- [ ] Does the PR test suite cover all accepted token prefixes and at least one rejected prefix?
- [ ] If a new token prefix is introduced, is it reflected in **all** consumers (validation gate + routing logic) in the same PR?

---

## 4. `TEST_CASES_MASTER.md` Update Instructions

Add rows TC-AUTH-10 through TC-AUTH-16 to the existing token/auth section of `TEST_CASES_MASTER.md`.

Add the Test Manager checklist from section 3.1 to the "Auth Feature Test Planning" section of `TEST_CASES_MASTER.md`.

---

*End of Test Doc — Post-mortem gho_ token — Draft, awaiting approval.*
