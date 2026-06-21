# Workflow Platform — Agent Process Rules

This file is read by Claude Code at the start of every session. All rules below are **mandatory**.

## Team Roles

| Agent | Responsibility |
|---|---|
| PM | Writes user stories + acceptance criteria into `workflow-agent-teams/docs/pm-doc-v*.md` (versioned slices), **and** maintains the master baseline `workflow-agent-teams/docs/pm-doc-master.md` (rules below) |
| Architect | Reviews technical approach, writes architecture notes into `workflow-agent-teams/docs/arch-doc-*.md` |
| Test Manager | Writes test cases into `workflow-agent-teams/docs/test-doc-*.md` based on PM + Arch docs; coordinates QA and E2E Tester; prepares UAT test script after E2E passes; guides human through UAT steps; collects human's results and writes UAT report |
| QA | After implementation: verify against test cases; write `workflow-agent-teams/docs/ui-test-report-vX.Y.md` |
| E2E Tester | Managed by Test Manager. After QA completes and before UAT: runs Playwright E2E tests against UAT environment (workflow-ui-gamma.vercel.app), performs 5-layer UX validation (exist/size/viewport/interact/effect), writes `workflow-agent-teams/docs/e2e-test-report-vX.Y.md`. Uses headed browser mode for visual verification. Reports failures back to Test Manager who decides whether to block UAT or file new TODOs |
| Delivery Manager | Coordinates implementation — only after all three docs are approved |
| Frontend / Backend devs | Implement only after Test Manager doc is approved by the human |

## Mandatory Process for Every TODO Item

**NEVER jump to implementation directly.** For every task from `workflow-agent-teams/TODO.md`, follow this sequence in order:

```
1. PM        → write/update `pm-doc-v*.md` (user story + acceptance criteria) **and** update `workflow-agent-teams/docs/pm-doc-master.md`: bump **Document version**, align the **Chinese** product baseline with the TODO, add a **Revision history** row (TODO label + US/AC IDs). Filename `pm-doc-master.md` is fixed — only version and content change. **Do not** put HTTP paths, methods, or error codes in the master — those belong in **Arch** docs. See **PM master baseline** below.
   - **User-facing docs (same priority as the master):** if the TODO changes **user-visible behavior**, PM also updates the **root-repo** user docs in the same change set — see **User-facing docs maintenance** below. **Every shipped TODO that a user can perceive MUST add one line to `docs/promo/CHANGELOG.md`**; update the relevant `docs/guide/*` page(s) by change type (node→`reference/nodes.md`, error code→`reference/error-codes.md`, call contract→`reference/api-call.md`, workflow JSON/validation→`reference/workflow-json.md`, rule semantics→`reference/rules-jsonpath.md`, any feature→manual/getting-started as needed).
2. Architect → write/update Arch doc (approach, data flow, security, trade-offs)
3. Test Mgr  → write test cases based on PM doc + Arch doc
4. STOP      → present all three docs to the human and wait for explicit approval
5. Only after approval: Delivery Manager dispatches implementation
6. Implement → Frontend / Backend agents write code
7. QA + E2E  → 
   a. QA: verify against test cases; write ui-test-report-vX.Y.md
   b. E2E Tester (managed by Test Manager): run Playwright E2E suite 
      against UAT environment; write e2e-test-report-vX.Y.md
      - Sync: git pull all repos + submodule update --remote
      - Run: headed mode on workflow-ui-gamma.vercel.app
      - Validate: 5-layer UX framework (exist/size/viewport/interact/effect)
      - Report: pass/fail counts, screenshots, accessibility violations
      - On failure: Test Manager reviews and decides next action
8. Commit + push every affected repo:
               a. Commit all code changes on the working branch
               b. Merge the working branch into the target branch
                  (see Commit & Push Rules below for each repo's target branch)
               c. Push the target branch to origin
               d. Do the same for workflow-agent-teams (docs + TODO.md)
9. UAT       → E2E Tester runs Playwright against UAT environment and is the
               sole verifier. UAT works as follows:
               a. E2E Tester navigates to https://workflow-ui-gamma.vercel.app
                  using headed Playwright browser (Desktop Chrome 1280px +
                  Mobile Chrome 390×844)
               b. E2E Tester executes all test cases from the test-doc using
                  the 5-layer UX validation framework:
                  - Layer 1 (Exist): elements are present in DOM
                  - Layer 2 (Size): dimensions meet spec (e.g. height > 35% viewport)
                  - Layer 3 (Viewport): content is not clipped outside viewport
                  - Layer 4 (Interact): clicks, inputs, and navigation work
                  - Layer 5 (Effect): computed styles / API responses match spec
               c. E2E Tester writes uat-report-vX.Y.md with pass/fail per case,
                  computed style values, screenshots, and overall verdict
               d. Test Manager reviews the report:
                  - All cases PASS → proceed to step 10
                  - Any FAIL → document each failure, open a new TODO item in
                    workflow-agent-teams/TODO.md, full cycle restarts for that item
               NOTE: Human confirmation is NOT required for UAT. The E2E Tester's
               Playwright run is the authoritative UAT gate. The human may still
               review the uat-report-vX.Y.md but their approval is not needed
               to proceed to step 10.
10. Mark TODO item as Done in workflow-agent-teams/TODO.md
    (only after the human confirms UAT is acceptable)
```

**Step 4 is a hard gate.** Do not proceed to step 5 without the human typing "approve" or equivalent confirmation.

**Step 8 must be fully completed** — do not stop after committing to a feature branch. Always merge to the target branch and push. Do not ask the human to merge; do it as part of the step. **Every affected submodule** must reach **(a) commit → (b) merge to target branch → (c) push** — skipping any repo or substeps 8a–8c is a process failure.

**Step 9 (UAT) is a hard gate.** Do not mark a TODO as Done until E2E Tester's Playwright UAT run reports PASS. If failures are found, do not mark Done — open new TODO items and restart the cycle for each defect.

**Step 10 must not be skipped.** Finishing implementation or push is not completion. If E2E Tester's UAT run is PASS, you **must** update `workflow-agent-teams/TODO.md` in the same working session if possible; if the session is ending, explicitly hand off: list what remains.

## Mandatory end-of-task checklist (before you stop on a TODO item)

Cloud runs often truncate context — **do not end your turn** until you have verified the following for **this** TODO item (say explicitly in your reply which items apply / are N/A):

| # | Check |
|---|--------|
| 1 | PM / Arch / Test docs updated as needed; **`pm-doc-master.md`** version + revision history updated for the TODO; human approval (step 4) obtained before any implementation |
| 2 | Code changes committed on correct branch **per repo** that was touched |
| 3 | Each touched repo: merged to **target** branch (`main` or `develop` per Commit & Push Rules) **and** `git push` to origin completed |
| 4 | `workflow-agent-teams`: docs + `TODO.md` committed and pushed to `main` when anything there changed |
| 5 | `ui-test-report-vX.Y.md` exists in `workflow-agent-teams/docs/` before E2E (when the item involved code) |
| 5.5 | `e2e-test-report-vX.Y.md` exists in `workflow-agent-teams/docs/` after E2E Tester completes Playwright run against UAT |
| 6 | E2E Tester runs Playwright UAT against https://workflow-ui-gamma.vercel.app; `uat-report-vX.Y.md` written with PASS verdict; TODO marked **Done** only after E2E Tester confirms UAT PASS |
| 7 | **User-facing docs impact assessed.** If the TODO changes user-visible behavior: `docs/promo/CHANGELOG.md` has a new line **and** the relevant `docs/guide/*` page(s) are updated and committed/pushed (root repo). If purely internal (no user-perceivable change), state **N/A** and why. |

If you cannot complete a row (e.g. waiting on human UAT), **state that blocker** instead of silently stopping.

## Document Locations

- PM versioned slices: `workflow-agent-teams/docs/pm-doc-v*.md` (exclude `pm-doc-master.md`) — single body, same style as this file
- PM master (fixed filename): `workflow-agent-teams/docs/pm-doc-master.md` — **Chinese-only** product baseline, **no API/contract detail** (APIs live in `arch-doc-*.md`); **must** be updated for every `TODO.md` item (see **PM master baseline** below)
- Arch docs: `workflow-agent-teams/docs/arch-doc-*.md`
- Test docs: `workflow-agent-teams/docs/test-doc-*.md`
- QA test reports: `workflow-agent-teams/docs/ui-test-report-vX.Y.md`
- E2E test reports: `workflow-agent-teams/docs/e2e-test-report-vX.Y.md`
- UAT reports: `workflow-agent-teams/docs/uat-report-vX.Y.md`
- TODO backlog: `workflow-agent-teams/TODO.md`
- **User-facing docs (root repo, canonical):** `docs/guide/` (product manual, getting-started, concepts, `reference/*`, `examples/*`) + `docs/promo/` (pitch, landing, demo-script, **`CHANGELOG.md`**). These are the **outward-facing / adoption** docs — kept in the **root `workflow` repo** (not the submodule). Navigated from root `DOCS.md`. See **User-facing docs maintenance** below.

## User-facing docs maintenance

These docs make the platform adoptable by outside users; they must not drift from shipped behavior.

- **Owner:** PM, as a standing deliverable (same priority as `pm-doc-master.md`).
- **When:** in the **same change set** as the TODO that changes user-visible behavior (end-of-task checklist row 7).
- **CHANGELOG is the bridge:** every shipped, user-perceivable TODO adds **one line** to `docs/promo/CHANGELOG.md` (this is what the UI "What's New" surfaces — see TODO `TODO-ui-surface-docs-whatsnew-help`).
- **Map change → page:** node type → `docs/guide/reference/nodes.md`; error code → `reference/error-codes.md`; call contract (path/header/param/response) → `reference/api-call.md`; workflow JSON / import validation → `reference/workflow-json.md`; rule semantics → `reference/rules-jsonpath.md`; new user feature → `01-product-manual.md` / `02-getting-started.md` as needed.
- **Truthfulness rule:** user docs describe **current** behavior; aspirational features go under a "路线图/Roadmap" note, never as if shipped.
- **Location note:** user docs live in the **root repo**; internal process docs (`pm-doc-*`, `TODO.md`) live in the `workflow-agent-teams` submodule. A TODO touching both updates both.

## PM master baseline (`pm-doc-master.md`)

**Canonical path:** `workflow-agent-teams/docs/pm-doc-master.md` — **filename is fixed**; only **Document version** and body change.

For **every** item from `workflow-agent-teams/TODO.md` (feature, bug fix, post-mortem, doc-only, or infra that affects product-visible behavior):

1. **PM updates** `pm-doc-master.md` in the **same change set** as the PM slice doc for that item (or as the only PM update if folded into the master).
2. **Bump Document version** in the file (e.g. `2.5` → `2.6`).
3. **Reflect the TODO** in the master: add or adjust user stories / acceptance criteria; map to **APP** (application management), **REC** (execution records / online semantics), **CV** (canvas).
4. **Revision history:** append a row — version, date, TODO label or title, **US/AC** IDs touched.

**PM master content**

- **`pm-doc-master.md`:** **Chinese only** — user-visible capabilities, stories, and acceptance criteria. **Exclude** interface specifications (URLs, verbs, status/error codes, OpenAPI). Put those in **`arch-doc-*.md`**. One continuous document (no duplicate Part 1/Part 2).
- **`pm-doc-v*.md`:** **one** body per file, **same language/style as this file** (English).
- **Arch / test / reports** under `workflow-agent-teams/docs/`: **one** body per file, same style as this file unless a doc type explicitly requires otherwise.

**PM ownership:** maintaining `pm-doc-master.md` is a standing deliverable, same priority as `pm-doc-vX.Y.md` when the process still requires a versioned slice for Architect / Test.

## Document Status Lifecycle

Draft → awaiting human approval → **Approved**

Only update status to **Approved** after the human explicitly confirms.

## UAT Environment

| Service | URL |
|---|---|
| **Frontend (UAT)** | https://workflow-ui-gamma.vercel.app |
| **operation-api** | https://workflow-operation-api-n9sbp.ondigitalocean.app |
| **online-api** | https://workflow-online-api-nr3e4.ondigitalocean.app |

E2E Tester runs Playwright (headed Desktop Chrome 1280px + Mobile Chrome 390×844) directly against the UAT frontend URL.
Backend API calls can be verified by any agent via curl/fetch against the operation-api and online-api URLs above.

## Commit & Push Rules

- `workflow-ui`: commit on `main`, push to `origin/main`
- `workflow-operation-api`: commit on `main`, push to `origin/main`
- `workflow-online-api`: commit on `develop`, push to `origin/develop`
- `workflow-agent-teams`: commit on `main`, push to `origin/main`
- Never leave commits in detached HEAD state — always checkout the correct branch before committing

## Session Start / End Protocol

**Every session must follow this protocol without exception.**

### Session Start — pull latest and verify submodule pointers

**Step 1: Pull all submodules to target branches**

```
git -C /home/user/workflow-ui            checkout main    && git -C /home/user/workflow-ui            pull origin main
git -C /home/user/workflow-agent-teams   checkout main    && git -C /home/user/workflow-agent-teams   pull origin main
git -C /home/user/workflow-operation-api checkout main    && git -C /home/user/workflow-operation-api pull origin main
git -C /home/user/workflow-online-api    checkout develop && git -C /home/user/workflow-online-api    pull origin develop
```

Do this **before reading any source files or making any changes**. If a pull fails, report the conflict to the human before proceeding.

**Step 2: CRITICAL — Verify submodule pointers in root repo point to target branches**

```
git submodule status
```

**MANDATORY CHECK:** Each submodule MUST show its target branch (main or develop), NOT:
- Detached HEAD
- Feature branches (e.g., `remotes/origin/cursor/*`)
- Old commits

**If ANY submodule points to wrong branch/commit:**

1. **STOP immediately and report to user:**
   - List which submodules have incorrect pointers
   - Show current state vs. expected state
   - Example: "⚠️ Submodule pointer check FAILED: `.claude` is at detached HEAD `44a6bf0`, expected `main` branch"

2. **Ask user for confirmation before fixing:**
   - "Should I update these submodule pointers to their target branches and commit the changes?"
   - Wait for explicit user approval

3. **Only after user approves, execute fix:**
   - Update submodule to target branch: `git -C <submodule_path> checkout <target_branch> && git -C <submodule_path> pull`
   - Stage pointer update: `git add <submodule_path>`
   - Commit: `git commit -m "Update <submodule> pointer to target branch"`
   - Push: `git push origin master`

**Do NOT proceed with any work until all submodule pointers are correct and user has been informed.**

### Session End — verify submodule pointers and push all changes

After every coding session (after step 8 in the Mandatory Process):

**Step 1: Verify all repos are clean and pushed**

```
# Confirm each repo is clean and pushed
git -C /home/user/workflow-ui            status
git -C /home/user/workflow-agent-teams   status
git -C /home/user/workflow-operation-api status
git -C /home/user/workflow-online-api    status
```

No uncommitted or un-pushed changes should remain when a session ends. If changes exist, commit and push them to the target branch before finishing.

**Step 2: CRITICAL — Final verification of submodule pointers**

```
git submodule status
git status
```

**MANDATORY CHECK:** 
1. All submodules MUST point to their target branches (main or develop)
2. Main repo MUST have NO uncommitted submodule pointer changes

**If submodules have new commits but main repo hasn't updated pointers:**

1. **STOP immediately and report to user:**
   - List which submodules have new commits
   - Show that main repo pointer is stale
   - Example: "⚠️ Submodule pointer check FAILED: `workflow-ui` has new commits but main repo still points to old SHA `528c01f`"

2. **Ask user for confirmation before fixing:**
   - "Should I update the main repo's submodule pointers to reflect these new commits and push the changes?"
   - Wait for explicit user approval

3. **Only after user approves, execute fix:**
   - Stage pointer updates: `git add <submodule_path>`
   - Commit: `git commit -m "Update submodule pointers after [work description]"`
   - Push: `git push origin master`

**Why this matters:** If main repo points to old submodule commits, other developers will check out stale code. Always keep pointers synchronized with target branches.

**Do NOT end the session until all submodule pointers are correct and user has been informed.**

## Submodule Management Rules

**CRITICAL:** Always ensure submodules point to their target branches (main or develop), not temporary development branches.

**Before starting any TODO item:**
1. Run `git submodule status` to check current state
2. If any submodule points to a non-target branch (e.g., `remotes/origin/cursor/*` or detached HEAD):
   - Update each affected submodule to its target branch
   - Commit and push the submodule pointer updates to the main repo

**After completing any TODO item (before final push):**
1. Verify all submodules still point to their target branches
2. If work in a submodule created new commits, ensure those commits are on the target branch
3. Update the main repo's submodule pointers if needed

**Why:** Prevents team members from accidentally checking out stale or abandoned feature branches. Keeps the main repository pointing to stable, authoritative code.

**Target branches:**
- `workflow-ui` → `main`
- `workflow-operation-api` → `main`
- `workflow-online-api` → `develop`
- `workflow-agent-teams` → `main`
- `.claude` → `main`

## What NOT to Do

- Do NOT write code before PM doc + Arch doc + test doc are all approved
- Do NOT mark a TODO item as Done before code is pushed **and merged to the target branch**
- Do NOT mark a TODO item as Done before E2E Tester's Playwright UAT run reports PASS
- Do NOT skip the human approval gate (step 4) for any reason
- Do NOT skip the UAT gate (step 9) — E2E Tester must run Playwright UAT and report PASS before Done
- Do NOT commit to wrong branches
- Do NOT stop at a feature branch — always merge to the target branch (main / develop) and push as the final step
- Do NOT skip the QA test report — every TODO item that involves code changes must have a `ui-test-report-vX.Y.md` committed to `workflow-agent-teams/docs/` before UAT begins
- Do NOT silently absorb UAT failures — every failed UAT case becomes a new TODO item
- Do NOT end work on a TODO item without either completing the end-of-task checklist above or explicitly listing what is still blocked (human gate, push failure, etc.)
