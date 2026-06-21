# PM Doc — Post-mortem: `gho_` Device-Flow Token Rejected as "Unrecognised token format"

**Label**: `TODO-postmortem-explain-github-gho-token-validation`  
**Version**: 1.0  
**Date**: 2026-04-06  
**Status**: Approved  
**Scope**: Post-mortem write-up + process improvements (no new feature code)  
**Reference**: [yiurenma/workflow#10](https://github.com/yiurenma/workflow/pull/10), [workflow-ui@263b483](https://github.com/yiurenma/workflow-ui/commit/263b483c0b29671d947add279957b791644cedec)

---

## 1. Background

After the GitHub OAuth Device Flow feature (US-21) was shipped, users who completed the Device Flow authorization received a `gho_…` (GitHub OAuth user access token) — a token format that was rejected at the local validation layer with "Unrecognised token format." The fix was shipped in `workflow-ui@263b483`, adding `gho_` to the allowed prefix list in both `isValidToken` and `callAI`. This post-mortem documents the root cause, why it was not caught, and mandates process improvements to prevent recurrence.

---

## 2. User Story (Retrospective)

**PM-PM1 — Token validation regression post-mortem**

> **As a** development team member,  
> **I want** a documented post-mortem of the `gho_` token validation gap,  
> **so that** we have a shared record of what went wrong, why, and what process changes prevent recurrence.

---

## 3. Scope of This Deliverable

The output of this TODO item is **a post-mortem document** in `workflow-agent-teams/docs/` plus concrete checklist additions to the Test Manager process. No new production code is shipped as part of this item.

### 3.1 Required inclusions in the post-mortem

| # | Required section | Description |
|---|---|---|
| PM1-1 | Root cause | Exactly which code change introduced the gap and why `gho_` was omitted |
| PM1-2 | Detection failure analysis | Why automated tests, manual QA, and code review all missed it |
| PM1-3 | Duplicate validation sites | How having `isValidToken` and `callAI` validate separately created a review blind spot |
| PM1-4 | Proposed test improvements | Concrete new test cases / rows to add to the master test matrix |
| PM1-5 | Proposed process improvements | Checklist additions for frontend dev + code review + Test Manager |

---

## 4. Acceptance Criteria

### AC-PM1-1 — Root cause documented
- The post-mortem names the two code locations (`isValidToken` and `callAI` in `workflow-header/index.tsx`) and the exact missing prefix (`gho_`) that caused the failure.
- It explains why `gho_` is issued by GitHub OAuth Device Flow while `ghp_`/`ghu_`/`ghs_` are issued by other paths.

### AC-PM1-2 — Detection gaps documented  
- At least three distinct failure points are identified (e.g. no device-flow end-to-end automated test; manual QA used only PAT path; code review checked one validation site not both).
- Each gap maps to a concrete improvement action.

### AC-PM1-3 — Duplicate logic identified and consolidation proposed
- The post-mortem recommends consolidating token-type detection into **one helper** function shared by `isValidToken` and `callAI` (already shipped in the fix; this documents the rationale).

### AC-PM1-4 — Test matrix additions defined
- A new test matrix row is defined: "OAuth Device Flow token (`gho_`) must be accepted for GitHub Models path."
- A negative row is defined: "Local prefix validation must never reject a token that GitHub has issued."
- These rows are to be added to `TEST_CASES_MASTER.md`.

### AC-PM1-5 — Code review checklist updated
- A checklist item is added for auth-related feature PRs: "Diff must show ONE token-validation code path, or tests explicitly covering both call sites."

### AC-PM1-6 — Ownership assigned
- Each improvement action names the responsible role: **Test Manager**, **Frontend dev**, **Code reviewer**.

---

## 5. Out of Scope

| Item | Rationale |
|---|---|
| New production code | The fix is already shipped; this is documentation only |
| Retroactive blame | Post-mortem is blameless |
| Other token types (e.g. GitHub Apps `ghs_`) | Covered by existing prefix list; not the focus of this regression |

---

## 6. Coordination

- **Test Manager**: update `TEST_CASES_MASTER.md` with the new matrix rows as part of this item.
- **Architect**: confirm the consolidated-helper recommendation is consistent with current code (see Arch Doc for this post-mortem).

---

*End of PM Doc — Post-mortem gho_ token — Draft, awaiting approval.*
