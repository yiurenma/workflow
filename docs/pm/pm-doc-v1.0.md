# PM Doc — Workflow Platform

**Version**: 1.0  
**Date**: 2026-04-05  
**Status**: Draft — awaiting human approval

---

## 1. Product Overview

The **Workflow Platform** is a rule-driven, async/sync processing and dispatch system targeted at enterprise applications (financial services, telecom, e-commerce) that need to enrich, evaluate, and route transactional messages to multiple downstream channels (SMS, Email, Push, etc.) without hardcoding logic per integration.

**One-line value proposition**: Configure once in the UI, dispatch anywhere at runtime — with full audit trail and retry safety.

**Three-component architecture**:


| Component                | What it is                                                                                                                           |
| ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------ |
| `workflow-ui`            | **Application management** (list, create, delete, **workflow copy**, **configuration history**), visual workflow editor, test runner |
| `workflow-operation-api` | Management (control) plane — CRUD for workflow definitions, audit history                                                            |
| `workflow-online-api`    | Execution (online) plane — ingress endpoint, enrichment pipeline, dispatch                                                           |


Both backend services share one PostgreSQL database; the online API never mutates schema definitions.

---

## 2. Users & Goals


| User                      | Goal                                                                                                                                    |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **Integration engineer**  | Define a workflow for their application: configure enrichment steps, branching rules, and dispatch actions without writing backend code |
| **Application developer** | Submit a transaction payload at runtime and have it enriched + dispatched automatically, with idempotency guarantees                    |
| **Operations / QA**       | Test a workflow end-to-end directly from the UI, inspect execution status, and review audit history                                     |


---

## 3. User Stories & Acceptance Criteria

### 3.1 Application Management

Application Management covers everything a user does on the **applications list** (`/workflows`): browsing applications, registering or removing them, **copying a workflow from one application to another**, and **reviewing configuration audit history**. It is distinct from the **Workflow Editor** (canvas), which is reserved for editing a single application’s definition.

#### US-01 — List applications

> As an integration engineer, I want to see all registered workflow applications so I can navigate to any of them.

**Acceptance criteria**:

- AC-01-1: Page `/workflows` lists all applications with `applicationName`, `enabled` status badge, and `lastModifiedDateTime`.
- AC-01-2: List is paginated and supports fuzzy search by `applicationName`.
- AC-01-3: Empty state shown when no applications exist.

#### US-02 — Create application

> As an integration engineer, I want to register a new application so I can start building its workflow.

**Acceptance criteria**:

- AC-02-1: A "Create" action is available on the list page.
- AC-02-2: Input: `applicationName` (required, unique). Submitting creates an empty workflow and navigates to the editor.
- AC-02-3: Duplicate `applicationName` returns a clear error; user stays on the creation form.

#### US-03 — Delete application

> As an integration engineer, I want to delete an application I no longer need.

**Acceptance criteria**:

- AC-03-1: A "Delete" action is available per application row.
- AC-03-2: A confirmation modal is shown before deletion.
- AC-03-3: Deletion is blocked if execution records exist for the application; error message explains why.

#### US-10 — Copy workflow from one application to another

> As an integration engineer, I want to replicate a workflow definition to another application from **Application Management** so I avoid rebuilding identical configurations without entering the canvas editor.

**Acceptance criteria**:

- AC-10-1: `POST /api/workflow/autoCopy` with `fromApplicationName` and `toApplicationName` performs the copy (Operation API).
- AC-10-2: Source and target must be different applications; error returned if same.
- AC-10-3: Source application must exist and be unique; error returned otherwise.
- AC-10-4: The copy creates or updates the target `WorkflowEntitySetting` with rules, types, and step order duplicated from the source (per API semantics).
- AC-10-5: **UI**: A copy affordance (e.g. **Copy workflow** / **Auto copy**) is available from **Application Management** — on the `/workflows` list (per-row and/or toolbar), with the source application pre-filled when opened from a row. This is **not** exclusive to the workflow canvas.

#### US-11 — View configuration change history

> As an operations engineer, I want to see the change history of an application’s workflow configuration from **Application Management** so I can audit what changed and when without opening the editor.

**Acceptance criteria**:

- AC-11-1: `GET /api/workflow/entity-setting/history?applicationName=APP` returns a paginated list of revisions (Operation API).
- AC-11-2: Each revision includes: revision number, timestamp, change type (ADD / MOD / DEL), and the snapshot of `WorkflowEntitySetting` at that revision (including workflow payload as exposed by the API).
- AC-11-3: History is available for the application’s entity setting regardless of whether execution records exist in `WORKFLOW_RECORD`.
- AC-11-4: **UI**: A **History** / **Audit** affordance is available from **Application Management** — on the `/workflows` list (per-row action or equivalent). Viewing history is **not** exclusive to the workflow canvas.

---

### 3.2 Workflow Editor (Canvas)

#### US-04 — Open workflow in visual editor

> As an integration engineer, I want to open an application and see its workflow as a visual diagram so I can understand and edit it.

**Acceptance criteria**:

- AC-04-1: Navigating to `/workflows/$applicationName` loads the current workflow definition and renders it as a directed graph (nodes + edges).
- AC-04-2: Supported node types displayed on canvas: START, CONSUMER, MESSAGE, IF_ELSE, FUNCTION_V2.
- AC-04-3: Edges between nodes show animated directional flow.
- AC-04-4: Canvas supports pan and zoom.

#### US-05 — Add a workflow step

> As an integration engineer, I want to drag a step type from the sidebar onto the canvas so I can add it to the workflow.

**Acceptance criteria**:

- AC-05-1: Sidebar lists all available step types with name and icon.
- AC-05-2: Dragging a step type to the canvas creates a new node at the drop position.
- AC-05-3: New node is immediately selectable and configurable.

#### US-06 — Connect workflow steps

> As an integration engineer, I want to draw edges between nodes to define the execution sequence.

**Acceptance criteria**:

- AC-06-1: Dragging from a node's output handle to another node's input handle creates a directed edge.
- AC-06-2: Existing edges are displayed with directional arrow markers.
- AC-06-3: The logical order of steps is derived from the edge connections and persisted as `logicOrder`.

#### US-07 — Configure a workflow step

> As an integration engineer, I want to click a node to open a configuration panel so I can define its rules, action URL, headers, and body.

**Acceptance criteria**:

- AC-07-1: Clicking a node opens a right-side drawer with a form specific to that node's type.
- AC-07-2: CONSUMER node form: `provider` (label), `httpRequestMethod`, `httpRequestUrlWithQueryParameter`, `httpRequestHeaders`, `httpRequestBody`, `remark`.
- AC-07-3: MESSAGE node form: same HTTP fields plus `trackingNumberSchemaInHttpResponse`.
- AC-07-4: IF_ELSE node form: `ruleList` (one or more JSONPath expressions with remarks), `elseLogic` (fallback JSON payload).
- AC-07-5: FUNCTION_V2 node form: transformation configuration.
- AC-07-6: Drawer shows the current node's saved values when reopened.

#### US-08 — Delete a workflow step

> As an integration engineer, I want to remove a step from the canvas.

**Acceptance criteria**:

- AC-08-1: Selecting a node and pressing Backspace or Delete removes it from the canvas.
- AC-08-2: Edges connected to the deleted node are also removed.

#### US-09 — Save workflow

> As an integration engineer, I want to save the current canvas state so it is persisted as the workflow definition.

**Acceptance criteria**:

- AC-09-1: Clicking "Save" in the editor header merges canvas nodes and edges into the workflow payload and POSTs to the Operation API.
- AC-09-2: Success confirmation is shown; the editor remains open with the latest saved state.
- AC-09-3: Save failure shows an error message; no data is lost from the canvas.

---

### 3.3 Workflow Execution (Online API)

#### US-12 — Submit a workflow execution request

> As an application developer, I want to POST a payload to the Online API so the system enriches and dispatches it according to the configured workflow.

**Acceptance criteria**:

- AC-12-1: `POST /api/workflow?applicationName=APP&confirmationNumber=XYZ` with `X-Request-Correlation-Id` header accepted.
- AC-12-2: JSON and XML content types both accepted.
- AC-12-3: Response is returned immediately (async processing; `200 OK` does not mean dispatch is complete).
- AC-12-4: If `applicationName` does not exist → `M0001` error.
- AC-12-5: If `X-Request-Correlation-Id` already exists for this application → `M0002` duplicate error.

#### US-13 — Enrichment pipeline execution

> As a system operator, I want the platform to call backend services and merge their responses into the runtime payload so downstream dispatch steps have full context.

**Acceptance criteria**:

- AC-13-1: Steps with type CONSUMER, IFELSE, or FUNCTION_V2 execute in `logicOrder` sequence before any dispatch step.
- AC-13-2: For each enrichment step: JSONPath rules are evaluated against the current runtime payload; if matched, the configured HTTP action is called; result is merged into the payload.
- AC-13-3: If no rule matches, `elseLogic` JSON is applied as the fallback enrichment value.
- AC-13-4: Enrichment outcome updates `WORKFLOW_RECORD.overallStatus` to `GI_SUCCESS` or `GI_FAIL`.

#### US-14 — Message dispatch

> As a system operator, I want enriched payloads to be sent to the configured downstream channels after enrichment completes.

**Acceptance criteria**:

- AC-14-1: Steps with type MESSAGE/DISPATCH execute after all enrichment steps.
- AC-14-2: Each dispatch step makes an HTTP call to its configured URL with the enriched payload.
- AC-14-3: Tracking numbers are extracted from the dispatch response per `trackingNumberSchemaInHttpResponse` and persisted.
- AC-14-4: Channel-specific statuses (smsStatus, emailStatus, pushNotificationStatus) are updated in `WORKFLOW_RECORD`.
- AC-14-5: Final `overallStatus` reflects the combined result: `FB_ALL_SUCCESS`, `FB_PARTIAL_SUCCESS`, `FB_ALL_FAIL`, etc.

#### US-15 — Idempotent request handling

> As an application developer, I want duplicate submissions with the same correlation ID to be safely rejected so I don't trigger double dispatch.

**Acceptance criteria**:

- AC-15-1: The combination of `X-Request-Correlation-Id` + `applicationName` is unique-indexed in `WORKFLOW_RECORD`.
- AC-15-2: A second identical request returns `M0002` without creating a new record or triggering dispatch.
- AC-15-3: Retry re-submissions (with `isSelfRequest=true`) bypass the duplicate check.

#### US-16 — Retry on failure

> As a system operator, I want failed workflow executions to be retried automatically based on configured error codes so transient failures are recovered without manual intervention.

**Acceptance criteria**:

- AC-16-1: If a step returns an HTTP error code listed in `retryErrorCodes`, a retry workflow record is created with `originWorkflowRecordId` pointing to the original.
- AC-16-2: Retry attempts are tracked via `retryTimes` on `WORKFLOW_RECORD`.
- AC-16-3: If all retries are exhausted, `overallStatus` is set to `RETRY_ALL_FAIL`.

---

### 3.4 Test Runner (UI)

#### US-17 — Test a workflow from the UI

> As an integration engineer, I want to submit a test payload directly from the editor so I can verify the workflow behaves correctly without a separate tool.

**Acceptance criteria**:

- AC-17-1: A "Run" button in the editor header opens a modal with a JSON editor pre-populated with example input.
- AC-17-2: Clicking "Execute" sends the payload to the Online API for the current application with an auto-generated correlation ID and test confirmation number.
- AC-17-3: The modal displays the HTTP response (status code + body, up to 8,000 characters).
- AC-17-4: Errors (network, non-2xx) are shown clearly in the modal.

---

## 4. Non-Functional Requirements


| Area                     | Requirement                                                                                                                                                                                    |
| ------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Idempotency**          | Online API must reject duplicate correlation IDs per application                                                                                                                               |
| **Async/Sync execution** | Enrichment and dispatch run asynchronously or sync could be configured in workflowEntitySetting, if async, then enrichment and dispatch after the `200` response is returned. otherwise before |
| **Audit trail**          | Every change to `WorkflowEntitySetting and Workflow` must be tracked via Envers revision history                                                                                               |
| **Encrypted storage**    | Runtime payloads in `WORKFLOW_RECORD.workflowTransactionDetails` must be encrypted at rest                                                                                                     |
| **Schema ownership**     | `workflow-online-api` never mutates the shared schema (`ddl-auto=none`); only `workflow-operation-api` owns DDL                                                                                |
| **Payload formats**      | Online API must accept both `application/json` and `application/xml`                                                                                                                           |
| **Error codes**          | Operation API uses structured `WF-4xx-xxx` codes; Online API uses `M000x` codes                                                                                                                |
| **Base URL isolation**   | UI configures Operation and Online API base URLs independently (`VITE_OPERATION_API_BASE`, `VITE_ONLINE_API_BASE`)                                                                             |


---

## 5. Out of Scope (v1.0)

- User authentication / RBAC on the UI or APIs
- Bulk workflow execution / batch ingestion
- Visual execution trace (replay of enrichment steps in canvas)

---

## 6. Open Questions (for human decision before Architect Doc)


| #    | Question                                                                                                                                                                                                                                                            | Impact                                                   |
| ---- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------- |
| OQ-1 | Should the UI surface `WORKFLOW_RECORD` execution history (a "runs" tab per application)?--User: there should have a table which could be filter by different combination of record column table. user should see the records in workflow_record after executions | Scope of Online API read endpoints; new UI page          |
| OQ-2 | Is FUNCTION_V2 the only FUNCTION variant to support going forward, or should FUNCTION (v1) remain configurable?--User: Yes                                                                                                                                        | Simplification of drawer forms and backend type handling |
| OQ-3 | Should "enabled/disabled" toggle on `WorkflowEntitySetting` be actionable from the UI list page (toggle in-place)?--User: everything in entity setting table needs to be updated in UI instead of DB directly                                                     | New UI interaction + PATCH endpoint on Operation API     |
| OQ-4 | Are there plans to add new channel types beyond SMS/Email/Push?--User: yes but not currently                                                                                                                                                                      | Enum extensibility in `WorkflowRecord` status columns    |
| OQ-5 | Should retry configuration (error codes, max attempts) be editable per step in the UI, or remain backend-only config?--User: not by step but by application so it is the same question as OQ-3                                                                    | New drawer fields + Operation API contract change        |


---

*End of PM Doc v1.0 — please review and confirm (or request changes) before this is handed to the Delivery Manager.*