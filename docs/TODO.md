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

---

## Import — plugin / step type list should come from API schema (not hardcoded)

**Status:** Open (recorded only — not implemented)

**Summary:** In the **import** flow, validation uses a **wrong local list** of types. The **operation-api** does not publish an OpenAPI `enum` for step kinds: `Plugin.action` is a `WorkflowType` whose `type` field is a **plain string** in persistence and JSON (any value can be stored). The **de facto** contract is the set of values the control plane and **workflow-ui** already exchange today.

### Confirmed: `action.type` values (operation-api / saved workflow JSON)

These are the wire-format strings used end-to-end in **`workflow-ui`** (`src/api/mappers/workFlowMapper.ts`) and **`workflow-operation-api`** integration examples (`WorkflowUpdateController` OpenAPI example, `workflow-integration-test-data.json`, tests). They are the strings in **`pluginList[].action.type`** (and typically mirrored in **`pluginList[].uiMap.type`** for canvas mapping).

| `action.type` (API / saved JSON) | Typical UI node (workflow-ui `Plugin` enum) |
|----------------------------------|---------------------------------------------|
| `CONSUMER` | HTTP Fetch (`Consumer`) |
| `CONSUMERWITHOUTERROR` | Safe Fetch (`Consumer_Without_Error`) — **note:** wire string has **no** underscore between `CONSUMER` and `WITHOUT` |
| `IFELSE` | Condition (`If-Else`) |
| `MESSAGE` | Dispatch (`Message`) |
| `FUNCTION_V2` | Transform (`Function_V2`) — mapper also accepts legacy `FUNCTION` as input |
| `FUNCTION_V3` | Transform+ (`Function_V3`) |

**Not** part of that contract (today): types like `HTTP_CALL`, `LOGIC`, `DISPATCH`, `IF_ELSE` as **`action.type`** — those names appear in **product README** or **import modal copy** but do **not** match what `GET/POST /api/workflow` round-trips through `workFlowMapper.ts`.

### Known bug (import modal vs API)

**`workflow-ui/src/components/ImportWorkflowModal.tsx`** validates against `["FUNCTION_V2", "FUNCTION_V3", "HTTP_CALL", "LOGIC"]` and documents the same in UI copy. That list is **incorrect** for real saved workflows: valid imports must allow at least **`CONSUMER`**, **`CONSUMERWITHOUTERROR`**, **`IFELSE`**, and **`MESSAGE`** in addition to the function types, aligned with **`workFlowMapper.ts`** (single source of truth until the backend exposes a formal enum or catalog endpoint).

**Next steps for implementers:**

1. Align import validation (and any “allowed types” UI) with **`pluginToBackendType` / `backendTypeToPlugin`** in `workFlowMapper.ts`, or extract shared constants from that module.  
2. Optionally: add an OpenAPI `enum` or dedicated **plugin-type catalog** endpoint in operation-api so clients are not duplicated — today the schema is only “string”.  
3. Regression: import JSON exported from UAT/production containing each row in the table above; expect **pass** without false “invalid plugin type” errors.

### Test application — cover every node / `action.type`

After the import fix lands, the **test application** (the app used for automated or manual workflow regression in dev/UAT) must include **at least one step of each** supported `action.type`: **`CONSUMER`**, **`CONSUMERWITHOUTERROR`**, **`IFELSE`**, **`MESSAGE`**, **`FUNCTION_V2`**, **`FUNCTION_V3`**. Use that workflow to verify save/load, canvas rendering, drawer edits, and **import/export** for each kind so regressions cannot hide behind paths that only exercise a subset of nodes.

### Notes for PM / Arch / Test

- Likely maps to **APP** / **CV** depending on where import lives; Arch should cite the exact schema fields and endpoint(s), not PM master.  
- Test cases should assert **no drift** between saved `action.type` values and import validation after this change.

---

## Deploy — Step 1 fails on Deploy URL due to cross-reference (CORS); route via proxy API

**Status:** Open (recorded only — not implemented)

**Summary:** In the **Deploy** flow, **step 1** fails when the user enters a **Deploy URL** (target API base). The browser reports a **cross-reference error** (typically **CORS**: the Hub origin cannot call the target host directly). The first deploy step therefore errors even when the URL is otherwise valid.

**Expected direction:** Resolve cross-origin access by **not** calling the target host directly from the browser for deploy steps (or equivalent). **Theoretically, call a proxy API** (same-origin or server-side) that performs **CreateApplicationName** (and related steps) toward the user-supplied base URL, so the client talks only to the trusted proxy and avoids browser CORS blocks.

### Scope / acceptance (for implementers)

- Deploy step 1 succeeds when given a valid remote base URL that previously failed from the browser due to CORS.  
- Document in Arch: which service exposes the proxy, auth, and how the client passes the user’s base URL safely.  
- Regression: UAT and local still work; no silent credential leakage to unintended hosts.

### Notes for PM / Arch / Test

- Maps to **APP** (Deploy) and **infra** / **operation-api** (or dedicated proxy) per Arch.  
- PM master: user-visible outcome only (“Deploy works with my URL”); no HTTP detail in master.

**Recorded:** 2026-04-19 (user report — no code change in this task).
