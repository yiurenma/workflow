# Workflow Platform — Agent Process Rules

This file is read by Claude Code at the start of every session. All rules below are **mandatory**.

## Team Roles

| Agent | Responsibility |
|---|---|
| PM | Writes user stories + acceptance criteria into `workflow-agent-teams/docs/pm-doc-*.md` |
| Architect | Reviews technical approach, writes architecture notes into `workflow-agent-teams/docs/arch-doc-*.md` |
| Test Manager | Writes test cases into `workflow-agent-teams/docs/test-doc-*.md` based on PM + Arch docs |
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
7. QA        → verify against test cases
8. Commit + push each affected repo
9. Mark TODO item as Done
```

**Step 4 is a hard gate.** Do not proceed to step 5 without the human typing "approve" or equivalent confirmation.

## Document Locations

- PM docs: `workflow-agent-teams/docs/pm-doc-*.md`
- Arch docs: `workflow-agent-teams/docs/arch-doc-*.md`
- Test docs: `workflow-agent-teams/docs/test-doc-*.md`
- TODO backlog: `workflow-agent-teams/TODO.md`

## Document Status Lifecycle

Draft → awaiting human approval → **Approved**

Only update status to **Approved** after the human explicitly confirms.

## Commit & Push Rules

- `workflow-ui`: commit on `main`, push to `origin/main`
- `workflow-operation-api`: commit on `main`, push to `origin/main`
- `workflow-online-api`: commit on `develop`, push to `origin/develop`
- `workflow-agent-teams`: commit on `main`, push to `origin/main`
- Never leave commits in detached HEAD state — always checkout the correct branch before committing

## What NOT to Do

- Do NOT write code before PM doc + Arch doc + test doc are all approved
- Do NOT mark a TODO item as Done before code is pushed
- Do NOT skip the human approval gate (step 4) for any reason
- Do NOT commit to wrong branches
