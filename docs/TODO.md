# Backlog

> **Note:** In the full multi-repo setup, this item should also live in `workflow-agent-teams/TODO.md` (see `CLAUDE.md`). This copy exists in the root `docs/` folder because the `workflow-agent-teams` submodule may not be checked out in every workspace.

## Hub — Deploy action (ApplicationName)

**Status:** Open (recorded only — not implemented)

**Summary:** Add a **Deploy** action under **ApplicationName** (alongside existing actions). Behavior mirrors what **Save** in the Hub persists for the current `ApplicationName`: the deploy flow should create/update that application and carry **all workflow data** for that `ApplicationName`.

### UX

- **Placement:** Action area for **ApplicationName**.
- **Deploy click — explain first:** When the user opens/clicks **Deploy**, show **copy that describes what Deploy is for** (purpose of the action, what gets sent, and the three remote steps in plain language) before or beside the form so users know what they are doing.
- **Inputs (five fields) before Deploy:**
  1. **Deploy URL (base only)** — user enters the **host/base URL** of the target APIs (e.g. UAT control-plane base URL). The product **must not** require users to type full paths for each step.  
  2. Application name  
  3. Service account username  
  4. Service account password  
  5. Environment  
- **Automatic paths:** While deploying, the system **concatenates** the base URL with the paths that correspond to these three existing capabilities (exact path strings in Arch / OpenAPI):
  1. **CreateApplicationName**  
  2. **UpdateApplicationName**  
  3. **SaveWorkflow**  
  Together these complete deploy: create app → update app payload → save workflows for that application.

### Server-side / remote steps (visible progress)

After the user confirms **Deploy**, show **three sequential steps** with clear status, aligned with the calls above:

1. **CreateApplicationName** — create the application name on the target.  
2. **UpdateApplicationName** — push/update application data for the target `ApplicationName` (same intent as Hub **Save** for app-level data).  
3. **SaveWorkflow** — persist all workflow data for that `ApplicationName` on the target.

**Success UI:** If all three steps succeed, show a **green** success label (or badge).

**Failure:** Steps that fail should be visibly marked (e.g. error state); do not show the green “all success” label unless all three complete successfully.

### Notes for PM / Arch / Test

- Map to **APP** (application management) and **CV** (canvas / workflow definitions) in product docs.  
- Arch doc should specify HTTP contract, auth (service account), environment handling, and the **exact relative paths** appended to the user-supplied base URL for `CreateApplicationName`, `UpdateApplicationName`, and `SaveWorkflow` — not in PM master per process rules.

### Test focus (UAT)

- **Scenario:** Start from an **existing** application (user is editing / has loaded a real app in the Hub). In the Deploy form, enter a **different** application name than the one currently loaded. Use the **UAT base URL** (same environment as the operation/control APIs already used in UAT — “the URL is the UAT URL”) as the Deploy URL field.  
- **Expectation:** The three auto-built requests run in order; the target ends up with the **new** application name populated (create + update + workflows), proving the flow works end-to-end against UAT without manual path entry.  
- **Regression:** Also cover “same name as current app” if product allows it; primary acceptance path is **existing context + new name in the input box**.
