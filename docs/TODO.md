# Backlog

> **Note:** In the full multi-repo setup, this item should also live in `workflow-agent-teams/TODO.md` (see `CLAUDE.md`). This copy exists in the root `docs/` folder because the `workflow-agent-teams` submodule may not be checked out in every workspace.

## Hub — Deploy action (ApplicationName)

**Status:** Open (recorded only — not implemented)

**Summary:** Add a **Deploy** action under **ApplicationName** (alongside existing actions). Behavior mirrors what **Save** in the Hub persists for the current `ApplicationName`: the deploy flow should create/update that application and carry **all workflow data** for that `ApplicationName`.

### UX

- **Placement:** Action area for **ApplicationName**.
- **Inputs (five fields) before Deploy:**
  1. Deploy URL  
  2. Application name  
  3. Service account username  
  4. Service account password  
  5. Environment  
- **Deploy button:** On submit, the client calls the configured **Deploy URL** (using the supplied credentials/environment as required by the integration — exact API contract to be defined in Arch).

### Server-side / remote steps (visible progress)

After the user clicks **Deploy**, show **three sequential steps** with clear status for each:

1. **Create ApplicationName** — create the application (name) on the target.  
2. **Update ApplicationName** — push/update **all** data for the current application so the remote `ApplicationName` matches what **Save** in the Hub would persist for this app.  
3. **Update Workflow** — sync/update all workflow definitions/data stored under this `ApplicationName`.

**Success UI:** If all three steps succeed, show a **green** success label (or badge).

**Failure:** Steps that fail should be visibly marked (e.g. error state); do not show the green “all success” label unless all three complete successfully.

### Notes for PM / Arch / Test

- Map to **APP** (application management) and **CV** (canvas / workflow definitions) in product docs.  
- Arch doc should specify HTTP contract, auth (service account), and environment handling — not in PM master per process rules.
