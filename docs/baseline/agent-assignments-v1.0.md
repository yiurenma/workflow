# Agent Team Assignments — Workflow Platform (2026-04-07)

**Status**: Active  
**Covers**: HOT-01 (repo fix) + Q2 Mobile doc cycle  
**Inputs**: TODO.md (open items), CLAUDE.md (mandatory process)

---

## Active Open Items Summary

| Label | Type | Status | Assigned Agent |
|-------|------|--------|---------------|
| HOT-01-online-api-restore-staged-deletion | Hotfix (no doc cycle) | Open — BLOCKER | Backend Dev / Ops |
| Q2-DOC-mobile-pm-arch-test-docs | Doc cycle gate | Open | PM → Architect → Test Manager |
| TODO-q2-mobile-apps-actions-desktop-entry-constraints | Feature (blocked on Q2-DOC) | Waiting for docs | Frontend Dev |
| TODO-mobile-app-overflow-history-copy-modal-not-navigation | Bug fix (blocked on Q2-DOC) | Waiting for docs | Frontend Dev |
| TODO-mobile-add-application-fab-draggable | Feature (blocked on Q2-DOC) | Waiting for docs | Frontend Dev |

---

## HOT-01 — Online API Restore (No doc cycle required)

**Agent**: Backend Dev / Ops  
**Priority**: P0 — must be resolved first; does not require PM/Arch/Test docs because it is a git state restore with zero logic change.

**What happened**: All 94 files in `workflow-online-api` are staged for deletion (likely `git rm --cached -r .` was run). HEAD is intact.

**Steps**:
```bash
cd workflow-online-api
git restore --staged .
git status          # must show: working tree clean
mvn clean verify    # must pass
```

**Done when**: `git status` clean, `mvn clean verify` green, report result back.

**Do NOT**: `git checkout -- .` (risky if working-tree changes exist). Use `restore --staged` only.

---

## Q2-DOC — Doc Cycle for Q2 Mobile Items

Per CLAUDE.md, no implementation until PM doc + Arch doc + Test doc are approved by the human.

### Step 1 — PM (writes pm-doc-v11.0.md)

Cover all three mobile items as distinct user stories:

**US-Q2-01 — Mobile application actions (copy + delete)**
- AC: overflow/swipe menu on mobile application cards includes Copy and Delete; tapping Copy opens the copy modal; tapping Delete runs confirm + action path; existing desktop flows unchanged.

**US-Q2-02 — Desktop-view entry point on mobile**
- AC: one visible affordance (button/link/toggle) on the mobile applications list enables "desktop view" (full breakpoint shell, or URL param); one tap switches; default mobile remains minimal.

**US-Q2-03 — Fix mobile overflow menu navigation bug**
- AC: on mobile, History and Copy in the overflow menu open their respective modals in-place (not navigate to Workflow screen); Delete runs confirm + action without navigation; matches existing desktop behavior.

**US-Q2-04 — Mobile add-application FAB draggable**
- AC: FAB is repositionable by drag on mobile; snap to edges optional; single tap still opens add flow; desktop layout unchanged.

### Step 2 — Architect (writes arch-doc-v11.0.md)

Cover:
- Which components in `workflow-ui` are touched per US
- `stopPropagation` / `preventDefault` fix for US-Q2-03 overflow navigation bug
- Drag implementation for FAB (pointer events, snap logic)
- How to conditionally render desktop breakpoint shell
- Regression guard: canvas pan/zoom must not be affected

### Step 3 — Test Manager (writes test-doc-v11.0.md)

Write TC for each AC, including:
- Mobile viewport (375px) and desktop viewport (1280px) cases
- Overflow menu → History/Copy → modal opened (not navigation)
- Overflow menu → Delete → confirm modal → action
- FAB drag-and-drop positions; tap still opens add flow
- Desktop-view entry: renders full desktop UI

### Step 4 — HARD GATE

Present all three docs to the human. **Do not proceed** until explicit approval received.

### Step 5 — Implementation (after approval)

**Frontend Dev** implements:
- `workflow-ui` mobile applications list / card / overflow menu fixes
- Desktop-view entry point
- FAB drag logic

**UI Test** executes test doc against implementation.

**Test Manager** reviews report.

---

## Escalation

| Conflict | Escalate to |
|----------|------------|
| HOT-01 build failure | Backend Dev → Delivery Manager |
| Q2-DOC ambiguity during PM writing | PM → Human |
| Architecture trade-off (drag impl, breakpoint strategy) | Architect → Human |
| Test TC / AC mismatch | Test Manager → PM |
