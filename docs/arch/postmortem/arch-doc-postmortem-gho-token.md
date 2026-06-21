# Arch Doc — Post-mortem: `gho_` Device-Flow Token Rejected as "Unrecognised token format"

**Label**: `TODO-postmortem-explain-github-gho-token-validation`  
**Version**: 1.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Input**: PM Doc — Post-mortem gho_ token v1.0

---

## 1. Root Cause

### 1.1 What happened

`workflow-ui/src/routes/workflows/-components/workflow-header/index.tsx` contained **two independent** token-validation sites:

1. **`isValidToken(token)`** — checked prefix to decide whether to prompt for a new token.
2. **`callAI(token, prompt)`** — checked prefix again to decide which API path (`isAnthropic` / `isGitHub`) to route through, and threw "Unrecognised token format" if neither matched.

At the time of the Device Flow feature (US-21), `isValidToken` was updated to include `gho_`, but `callAI` was left with an older prefix list that included `ghp_`, `github_pat_`, `ghu_`, `ghs_` — **omitting `gho_`**.

Result: a user who completed Device Flow authorization received a `gho_` token that passed `isValidToken` (no re-prompt), but then hit the `throw new Error("Unrecognised token format…")` inside `callAI`.

### 1.2 GitHub token prefix taxonomy

| Prefix | Issued by | Format |
|---|---|---|
| `ghp_` | Classic Personal Access Token (PAT) | Manual creation in GitHub Settings |
| `github_pat_` | Fine-grained PAT | Manual creation in GitHub Settings |
| `ghu_` | User-to-server OAuth token (GitHub Apps) | GitHub App installation flow |
| `ghs_` | Server-to-server token (GitHub Apps) | GitHub App backend |
| `gho_` | **User OAuth access token (OAuth App Device Flow)** | GitHub OAuth Device Flow, web flow |

`gho_` is the token format issued by **GitHub OAuth Apps** when a user authorizes via Device Flow or standard web OAuth flow. It was the correct expected output of US-21 but was missing from `callAI`'s prefix list.

### 1.3 Fix (shipped in 263b483)

`gho_` was added to both `isValidToken` and `callAI`. Both functions now correctly classify `gho_` as a GitHub token. Additionally, the fix consolidated the prefix-check logic: `callAI` now uses the same `isGitHub` branch for all GitHub token types including `gho_`.

---

## 2. Why It Was Not Caught

### 2.1 Happy-path coverage gap

The automated test suite (and any manual QA) likely exercised the **PAT path** (`ghp_` or `github_pat_`) for the Explain feature — not the full Device Flow end-to-end path. Because Device Flow requires a real browser authorization round-trip with GitHub, it is harder to automate and easy to skip in manual testing.

**Fix**: Test Manager must add an explicit acceptance row: "After Device Flow authorization completes, `gho_` token must be accepted by Explain and produce an AI response (or fail only on the upstream Models API, never on local prefix validation)."

### 2.2 Duplicate validation — review blind spot

Two functions performed token-type detection. A reviewer checking `isValidToken` might confirm `gho_` was present and consider the review complete, without inspecting `callAI` (a different function deeper in the file). There was no mechanism to enforce parity between the two sites.

**Fix**: Consolidate into one helper. One source of truth means any future update requires changing only one place.

### 2.3 No negative test matrix row

No test case existed with the explicit assertion: "Token from OAuth Device Flow (prefix `gho_`) must reach the GitHub Models API call, not hit the unrecognised-format error." Without this row, no one was responsible for covering it.

**Fix**: Add to `TEST_CASES_MASTER.md` (see section 4).

---

## 3. Consolidated Token Helper (Recommended Architecture)

Post-fix, the code should have (and now does have) a single source of truth:

```typescript
// One helper — one place to maintain
function isValidToken(token: string | null): boolean {
  if (!token) return false;
  return (
    token.startsWith("sk-ant-") ||
    token.startsWith("ghp_") ||
    token.startsWith("github_pat_") ||
    token.startsWith("gho_") ||   // ← Device Flow / OAuth App tokens
    token.startsWith("ghu_") ||
    token.startsWith("ghs_")
  );
}

// callAI uses the same prefix logic — no separate list
const isAnthropic = token.startsWith("sk-ant-");
const isGitHub = isValidToken(token) && !isAnthropic;
```

Or alternatively, extract a `isGitHubToken(token)` function used by both `isValidToken` and the routing inside `callAI`, making the dependency explicit.

**Architectural Decision AD-PM1-1**: Token-type detection must live in **one helper** (`isValidToken` or a shared `isGitHubToken`) called by all consumers. Any future addition of a new GitHub token prefix requires only one change.

---

## 4. Required Test Matrix Additions

To be added to `TEST_CASES_MASTER.md` (Test Manager owns this):

| ID | Scenario | Input | Expected result | Notes |
|---|---|---|---|---|
| TC-AUTH-10 | Device Flow token accepted by Explain | `gho_xxxxx` (valid Device Flow token) | `callAI` routes to GitHub Models path; no "Unrecognised token" error | Requires real or mocked Device Flow token |
| TC-AUTH-11 | Device Flow token not rejected at validation gate | `gho_xxxxx` | `isValidToken` returns `true`; no re-prompt for token | Validates parity between isValidToken and callAI |
| TC-AUTH-12 | Classic PAT still accepted | `ghp_xxxxx` | `callAI` routes to GitHub Models path | Regression guard |
| TC-AUTH-13 | Fine-grained PAT still accepted | `github_pat_xxxxx` | `callAI` routes to GitHub Models path | Regression guard |
| TC-AUTH-14 | Anthropic key still accepted | `sk-ant-xxxxx` | `callAI` routes to Anthropic path | Regression guard |
| TC-AUTH-15 | Unrecognised prefix rejected (negative) | `xyz_xxxxx` | `isValidToken` returns `false`; user prompted | Validates guard still works |

---

## 5. Code Review Checklist Addition

For any PR touching token validation or authentication logic:

- [ ] Is token-type detection performed in **one place only**? If multiple call sites exist, link to the shared helper.
- [ ] Does the PR include test coverage for **all** supported token prefixes (positive cases) and at least one unsupported prefix (negative case)?
- [ ] If a new token type is added, are **all** consumers (gate, routing, display) updated in **the same PR**?

---

## 6. Affected Files (For Reference)

| File | Change |
|---|---|
| `workflow-ui/src/routes/workflows/-components/workflow-header/index.tsx` | Fix already shipped: `gho_` added to `isValidToken` and `callAI`; no further code changes needed for this post-mortem |
| `workflow-agent-teams/docs/TEST_CASES_MASTER.md` | Add rows TC-AUTH-10 through TC-AUTH-15 |

---

*End of Arch Doc — Post-mortem gho_ token — Draft, awaiting approval.*
