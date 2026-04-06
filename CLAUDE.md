# Workflow Platform — Agent Process Rules

This file is read by Claude Code at the start of every session. All rules below are **mandatory**.

## Team Roles

| Agent | Responsibility |
|---|---|
| PM | Writes user stories + acceptance criteria into `workflow-agent-teams/docs/pm-doc-*.md` |
| Architect | Reviews technical approach, writes architecture notes into `workflow-agent-teams/docs/arch-doc-*.md` |
| Test Manager | Writes test cases into `workflow-agent-teams/docs/test-doc-*.md` based on PM + Arch docs; prepares UAT test script after merge; guides human through UAT steps; collects human's results and writes UAT report |
| Delivery Manager | Coordinates implementation — only after all three docs are approved |
| Frontend / Backend devs | Implement only after Test Manager doc is approved by the human |

## Mandatory Process for Every TODO Item

**NEVER jump to implementation directly.** For every task from `workflow-agent-teams/TODO.md`, follow this sequence in order:

```
1. PM        → write/update PM doc (user story + acceptance criteria)
2. Architect → write/update Arch doc (approach, data flow, security, trade-offs)
3. Test Mgr  → write test cases based on PM doc + Arch doc
4. STOP      → present all three docs to the human and wait for explicit approval
5. Only after approval: Delivery Manager dispatches implementation
6. Implement → Frontend / Backend agents write code
7. QA        → verify against test cases; write test report into
               workflow-agent-teams/docs/ui-test-report-vX.Y.md
8. Commit + push every affected repo:
               a. Commit all code changes on the working branch
               b. Merge the working branch into the target branch
                  (see Commit & Push Rules below for each repo's target branch)
               c. Push the target branch to origin
               d. Do the same for workflow-agent-teams (docs + TODO.md)
9. UAT       → Agent capability boundary: agents cannot render browser UI or
               interact with the live site. UAT works as follows:
               a. Test Manager verifies backend APIs directly where possible
                  (curl / fetch against UAT environment URLs)
               b. Test Manager produces a step-by-step UAT script for the human,
                  listing exactly what to click, what to enter, and what to observe
                  on https://workflow-ui-gamma.vercel.app
               c. Human executes the script in the browser and reports results
               d. Test Manager writes uat-report-vX.Y.md based on human's feedback:
                  - All cases PASS → proceed to step 10
                  - Any FAIL → document each failure, open a new TODO item in
                    workflow-agent-teams/TODO.md, full cycle restarts for that item
10. Mark TODO item as Done in workflow-agent-teams/TODO.md
    (only after the human confirms UAT is acceptable)
```

**Step 4 is a hard gate.** Do not proceed to step 5 without the human typing "approve" or equivalent confirmation.

**Step 8 must be fully completed** — do not stop after committing to a feature branch. Always merge to the target branch and push. Do not ask the human to merge; do it as part of the step.

**Step 9 (UAT) is a hard gate.** Do not mark a TODO as Done until the human confirms UAT is acceptable. If failures are found, do not mark Done — open new TODO items and restart the cycle for each defect.

## Document Locations

- PM docs: `workflow-agent-teams/docs/pm-doc-*.md`
- Arch docs: `workflow-agent-teams/docs/arch-doc-*.md`
- Test docs: `workflow-agent-teams/docs/test-doc-*.md`
- QA test reports: `workflow-agent-teams/docs/ui-test-report-vX.Y.md`
- UAT reports: `workflow-agent-teams/docs/uat-report-vX.Y.md`
- TODO backlog: `workflow-agent-teams/TODO.md`

## Document Status Lifecycle

Draft → awaiting human approval → **Approved**

Only update status to **Approved** after the human explicitly confirms.

## UAT Environment

| Service | URL |
|---|---|
| **Frontend (UAT)** | https://workflow-ui-gamma.vercel.app |
| **operation-api** | https://workflow-operation-api.onrender.com |
| **online-api** | https://workflow-online-api.onrender.com |

Test Manager verifies backend APIs directly against the URLs above.
For frontend UI tests, Test Manager produces a step-by-step script and the human executes it in the browser.
Backend API calls can be verified by the agent via curl/fetch.

## Commit & Push Rules

- `workflow-ui`: commit on `main`, push to `origin/main`
- `workflow-operation-api`: commit on `main`, push to `origin/main`
- `workflow-online-api`: commit on `develop`, push to `origin/develop`
- `workflow-agent-teams`: commit on `main`, push to `origin/main`
- Never leave commits in detached HEAD state — always checkout the correct branch before committing

## What NOT to Do

- Do NOT write code before PM doc + Arch doc + test doc are all approved
- Do NOT mark a TODO item as Done before code is pushed **and merged to the target branch**
- Do NOT mark a TODO item as Done before UAT is confirmed PASS by the human
- Do NOT skip the human approval gate (step 4) for any reason
- Do NOT skip the UAT gate (step 9) — Test Manager must report UAT results to human before Done
- Do NOT commit to wrong branches
- Do NOT stop at a feature branch — always merge to the target branch (main / develop) and push as the final step
- Do NOT skip the QA test report — every TODO item that involves code changes must have a `ui-test-report-vX.Y.md` committed to `workflow-agent-teams/docs/` before UAT begins
- Do NOT silently absorb UAT failures — every failed UAT case becomes a new TODO item
