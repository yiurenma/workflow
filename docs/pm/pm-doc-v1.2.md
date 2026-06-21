# PM Doc — Workflow Platform

**Version**: 1.2  
**Date**: 2026-04-05  
**Status**: Approved

**Changes from v1.1**:

- Added node types `CONSUMERWITHOUTERROR` and `FUNCTION_V3` throughout (planned backend additions; documented as product intent)
- US-07 rewritten: drawer configuration described as two distinct groups (HTTP-call group vs Logic/branching group) with shared skeleton fields
- AC-13-1 updated: CONSUMERWITHOUTERROR and FUNCTION_V3 added to enrichment step types
- AC-13-2 clarified: response body is merged into `messageInformation` of runtime payload
- AC-13-3 corrected: IFELSE `Logic` applies when rules **match** (not as a fallback when they don't); no-match always means skip
- Section 3.2: **Rules mechanic** paragraph added before Node Type Reference — explains JSONPath AND-gating
- Section 3.2: **Node Type Reference** table updated with On Failure column; CONSUMER vs CONSUMERWITHOUTERROR guidance added
- Node Type Reference — CONSUMER: failure behavior added (halts enrichment, sets GI_FAIL)
- Node Type Reference — IFELSE: no-else-branch clarification added
- US-12: two-phase model (enrichment then dispatch) called out explicitly
- AC-10-4: autoCopy clarified — copies both entity settings AND workflow definition
- AC-04-2 / AC-05-1: START node removed (UI visual only, no backend execution meaning)
- AC-11-8: rollback clarified — restores workflow definition only, not entity settings; reuses existing save endpoint
- AC-18-2: async/sync execution mode added to editable entity setting fields
- Section 5 Out of Scope: payload encryption/decryption (Base64) noted as future enhancement
- Section 3.2: CONSUMER vs CONSUMERWITHOUTERROR decision guidance added
- AC-19-2: parent/child WORKFLOW_RECORD structure noted (MESSAGE creates child records)
- AC-19-3: `transactionConfirmationNumber` highlighted as the primary business-side lookup key
- Decision Log OQ-2 corrected: FUNCTION_V2 and FUNCTION_V3 both supported; FUNCTION v1 dropped

**Changes from v1.0**:

- Section 1: Execution model corrected to async/sync (configurable)
- Section 3.1: US-10 (Copy) and US-11 (History) consolidated into Application Management; both explicitly surfaced from the `/workflows` list, not the canvas
- Section 3.1: Added **US-18 — Edit application settings** (was OQ-3 + OQ-5): all `WorkflowEntitySetting` fields editable from UI
- Section 3.3: Added **US-19 — Browse execution records** (was OQ-1): filterable `WORKFLOW_RECORD` table
- Section 3.2: FUNCTION v1 removed; FUNCTION_V2 and FUNCTION_V3 are the supported function variants (was OQ-2)
- Section 4 NFR: Async/sync row and audit trail row updated
- Section 5 Out of Scope: Execution records view removed (now in scope)
- Section 6 Open Questions: All resolved; section replaced with Decision Log

---

## 1. Product Overview

The **Workflow Platform** is a rule-driven, async/sync processing and dispatch system targeted at enterprise applications (financial services, telecom, e-commerce) that need to enrich, evaluate, and route transactional messages to multiple downstream channels (SMS, Email, Push, etc.) without hardcoding logic per integration.

**One-line value proposition**: Configure once in the UI, dispatch anywhere at runtime — with full audit trail and retry safety.

**Three-component architecture**:


| Component                | What it is                                                                                                                                                     |
| ------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `workflow-ui`            | **Application management** (list, create, delete, edit settings, workflow copy, configuration history, execution records), visual workflow editor, test runner |
| `workflow-operation-api` | Management (control) plane — CRUD for workflow definitions, entity settings, audit history                                                                     |
| `workflow-online-api`    | Execution (online) plane — ingress endpoint, enrichment pipeline, dispatch                                                                                     |


Both backend services share one PostgreSQL database; the online API never mutates schema definitions.

---

## 2. Users & Goals


| User                      | Goal                                                                                                                                    |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **Integration engineer**  | Define a workflow for their application: configure enrichment steps, branching rules, and dispatch actions without writing backend code |
| **Application developer** | Submit a transaction payload at runtime and have it enriched + dispatched automatically, with idempotency guarantees                    |
| **Operations / QA**       | Test a workflow end-to-end directly from the UI, browse execution records with filters, and review configuration audit history          |


---

## 3. User Stories & Acceptance Criteria

### 3.1 Application Management

Application Management covers everything a user does on the **applications list** (`/workflows`): browsing applications, registering or removing them, **editing application settings**, **copying a workflow from one application to another**, and **reviewing configuration audit history**. It is distinct from the **Workflow Editor** (canvas), which is reserved for editing a single application's visual definition.

#### US-01 — List applications

> As an integration engineer, I want to see all registered workflow applications so I can navigate to any of them.

**Acceptance criteria**:

- AC-01-1: Page `/workflows` lists all applications with `applicationName`, `enabled` status badge, description and `lastModifiedDateTime`.
- AC-01-2: List is paginated (default as 5) and supports fuzzy search by `applicationName`.
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
- AC-03-3: Confirming deletion removes the application (`WorkflowEntitySetting`) and all its associated workflow definitions (rules, types, step mappings). No check is performed against execution records — deletion always proceeds.

#### US-18 — Edit application settings

> As an integration engineer, I want to edit all settings of an application (enabled, retry, tracking, region, retry configuration, etc.) from Application Management so I never need to modify the database directly.

**Acceptance criteria**:

- AC-18-1: An "Edit" / "Settings" affordance is available per application row on the `/workflows` list.
- AC-18-2: Opening it presents a form pre-filled with the current `WorkflowEntitySetting` values for all editable fields:
  - `enabled` (boolean toggle)
  - `retry` (boolean toggle)
  - `tracking` (boolean toggle)
  - `region` (text)
  - `eimId` (text)
  - `defaultServiceAccount` (text)
  - `trackingServiceProviderActionId` and `trackingServiceProviderActionId2` (text)
  - `retryProperties` — including retry error codes and maximum retry attempts
  - `async` / `sync` execution mode — whether enrichment and dispatch run asynchronously (return `200` immediately) or synchronously (return after processing completes)
- AC-18-3: Saving submits the updated fields to the Operation API and shows a success confirmation.
- AC-18-4: Validation errors (e.g. required fields missing) are shown inline without closing the form.
- AC-18-5: Changes are reflected immediately in the application list (e.g. `enabled` badge updates).

#### US-10 — Copy workflow from one application to another

> As an integration engineer, I want to replicate a workflow definition to another application from Application Management so I avoid rebuilding identical configurations without entering the canvas editor.

**Acceptance criteria**:

- AC-10-1: `POST /api/workflow/autoCopy` with `fromApplicationName` and `toApplicationName` performs the copy (Operation API).
- AC-10-2: Source and target must be different applications; error returned if same.
- AC-10-3: Source application must exist and be unique; error returned otherwise.
- AC-10-4: The copy duplicates **both** the entity settings fields (enabled, retry, tracking, region, eimId, retryProperties, async/sync mode, etc.) **and** the full workflow definition (rules, types, step mappings, canvas layout) from the source to the target application.
- AC-10-5: **UI**: A **Copy workflow** / **Auto copy** affordance is available from Application Management — on the `/workflows` list (per-row and/or toolbar), with the source application pre-filled when opened from a row. This is **not** exclusive to the workflow canvas.

#### US-11 — View configuration history and rollback

> As an operations engineer, I want to see the change history of an application's workflow configuration and optionally roll back to a previous version, so I can audit changes and recover from unintended edits without opening the canvas editor.

**Acceptance criteria**:

- AC-11-1: `GET /api/workflow/entity-setting/history?applicationName=APP` returns a paginated list of revisions (Operation API).
- AC-11-2: Each revision includes: revision number, timestamp, change type (ADD / MOD / DEL), and the snapshot of `WorkflowEntitySetting` at that revision (including the workflow payload as exposed by the API).
- AC-11-3: History is available for the application's entity setting regardless of whether execution records exist in `WORKFLOW_RECORD`.
- AC-11-4: **UI**: A **History** / **Audit** affordance is available from Application Management — on the `/workflows` list (per-row action or equivalent). Viewing history is **not** exclusive to the workflow canvas.
- AC-11-5: Clicking a revision in the history list shows the full workflow details of that version.
- AC-11-6: From the revision detail view, the user is presented with a **Rollback to this version** option.
- AC-11-7: Choosing rollback triggers a confirmation step before proceeding.
- AC-11-8: On confirmation, the **workflow definition only** (rules, types, step mappings, and canvas layout) is restored to the selected revision's snapshot. Entity setting fields (enabled, retry, region, etc.) are **not** affected. The UI extracts the workflow payload from the revision and calls the existing `POST /api/workflow?applicationName=APP` endpoint — no new API is required. The resulting save is recorded as a new audit revision.
- AC-11-9: After a successful rollback, the application's workflow in the canvas editor reflects the restored version.

---

### 3.2 Workflow Editor (Canvas)

#### How Rules Work

Every step type — regardless of group — uses **Rules** as an AND-gated execution precondition. Each rule is a JSONPath expression evaluated against the current runtime payload (`messageInformation`, `messageContactInfo`, `messageStatus`). A rule "matches" when the JSONPath returns a non-empty result. **All rules in a step must match** for the step to execute. If any rule fails to match, the entire step is skipped — no HTTP call is made, no Logic is applied, and no fallback runs. Rules are therefore both the condition and the guard; designing them correctly is the primary configuration task for every step.

#### Node Type Reference

The table below defines every draggable step type — its execution phase, runtime behaviour, failure behaviour, and intended use case. Types marked *(planned)* are defined as product requirements; backend implementation is pending.


| Type                     | Group             | Execution Phase | Runtime Behaviour                                                                                                                                                                                                                                         | On Failure                                                                                                            | Typical Use Case                                                                                                                      |
| ------------------------ | ----------------- | --------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| **CONSUMER**             | HTTP-call         | Enrichment      | If all rules match, makes an HTTP call to the configured URL and merges the full response body into `messageInformation` of the runtime payload. If rules don't match, step is skipped.                                                                   | HTTP call failure halts enrichment and sets `overallStatus` to `GI_FAIL`.                                             | Fetch customer profile, account data, or any enrichment data from an external service — where that data is **mandatory** for dispatch |
| **CONSUMERWITHOUTERROR** | HTTP-call         | Enrichment      | Same as CONSUMER. If all rules match, makes an HTTP call and merges the response into `messageInformation`. *(planned)*                                                                                                                                   | HTTP call failure is **silently suppressed** — execution continues to the next step rather than failing the workflow. | Optional enrichment where missing data is acceptable and must not block the workflow                                                  |
| **MESSAGE**              | HTTP-call         | Dispatch        | If all rules match, makes an HTTP call to the configured dispatch URL with the enriched payload. Creates a **child `WORKFLOW_RECORD`** capturing channel status (SMS / Email / Push), tracking number extracted from the response, and provider response. | Call failure sets the child record status to `SM_FAIL` but does not halt other dispatch steps.                        | Send a notification to an SMS, email, or push channel                                                                                 |
| **IFELSE**               | Logic / branching | Enrichment      | If all rules match, applies the `Logic` JSON to the runtime payload (conditional assignment or transformation). If rules don't match, the step is skipped — **there is no else branch or automatic fallback**.                                            | N/A — no HTTP call; skip on no-match is the only outcome.                                                             | Conditional payload transformation — e.g. set contact type, routing flag, or default value based on customer attributes               |
| **FUNCTION_V2**          | Logic / branching | Enrichment      | If all rules match, invokes a configured Java class method via reflection. Output is wrapped in `WorkflowRuntimePayload.reference()` so downstream steps receive full workflow context alongside the function result. (FUNCTION v1 is not supported.)     | Reflection or invocation failure throws a runtime exception and halts the workflow.                                   | Date calculation, format conversion, or any reusable business logic function                                                          |
| **FUNCTION_V3**          | Logic / branching | Enrichment      | Extended function execution with enhanced parameter handling and invocation model over FUNCTION_V2. *(planned)*                                                                                                                                           | Same as FUNCTION_V2.                                                                                                  | Advanced transformation or multi-step function composition requiring capabilities beyond FUNCTION_V2                                  |

#### Choosing Between CONSUMER and CONSUMERWITHOUTERROR

This is one of the most important design decisions when configuring a workflow step. The rule is simple:

> **Use CONSUMER when the enrichment data is mandatory** — if the call fails, the workflow must stop because downstream steps depend on the data.  
> **Use CONSUMERWITHOUTERROR when the enrichment data is optional** — if the call fails, execution continues and downstream steps work with whatever data is already in the payload.

| Scenario | Recommended type |
|----------|-----------------|
| Fetching customer profile needed to determine the dispatch channel | CONSUMER — without it, dispatch cannot proceed |
| Fetching a preferred name for personalising a message | CONSUMERWITHOUTERROR — message can still send with a generic greeting |
| Calling an account balance API required for fraud check | CONSUMER — failure must block the workflow |
| Calling a secondary analytics service to enrich audit data | CONSUMERWITHOUTERROR — failure should not affect the main dispatch path |

#### US-04 — Open workflow in visual editor

> As an integration engineer, I want to open an application and see its workflow as a visual diagram so I can understand and edit it.

**Acceptance criteria**:

- AC-04-1: Navigating to `/workflows/$applicationName` loads the current workflow definition and renders it as a directed graph (nodes + edges).
- AC-04-2: Supported node types displayed on canvas: CONSUMER, CONSUMERWITHOUTERROR, MESSAGE, IFELSE, FUNCTION_V2, FUNCTION_V3. There is no START node — the canvas entry point is implicit.
- AC-04-3: Edges between nodes show animated directional flow.
- AC-04-4: Canvas supports pan and zoom.

#### US-05 — Add a workflow step

> As an integration engineer, I want to drag a step type from the sidebar onto the canvas so I can add it to the workflow.

**Acceptance criteria**:

- AC-05-1: Sidebar lists all available step types (CONSUMER, CONSUMERWITHOUTERROR, MESSAGE, IFELSE, FUNCTION_V2, FUNCTION_V3) with name and icon. No START node is included.
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

All six draggable node types share a **common skeleton** of fields, then split into two groups based on whether the step makes an outbound HTTP call or expresses logic/branching internally.

**Shared skeleton (all node types)**:


| Field         | Description                                                                                          |
| ------------- | ---------------------------------------------------------------------------------------------------- |
| `Description` | Free-text purpose note for this step                                                                 |
| `Rules`       | Rule list: one or more JSONPath expressions, each with a text remark; supports add / remove per rule |
| `Provider`    | Source system label (e.g. SYSTEM)                                                                    |
| `Type`        | Fixed to the node's type (read-only)                                                                 |
| `Remark`      | Additional notes                                                                                     |


**Group 1 — HTTP-call nodes: CONSUMER, CONSUMERWITHOUTERROR, MESSAGE**

These steps make outbound HTTP calls. In addition to the shared skeleton they expose:


| Field                  | Description                                                                       |
| ---------------------- | --------------------------------------------------------------------------------- |
| `Method`               | HTTP method dropdown (e.g. POST)                                                  |
| `Request Url`          | External / business-side URL template; supports `<<<...>>>` JSONPath placeholders |
| `Internal Request Url` | Internal routing URL (parallel to Request Url; same template convention)          |
| `Request Headers`      | JSON object — e.g. `X-*-Correlation-Id`, `Content-Type`                           |
| `Request Body`         | Request body template (e.g. `<<<$.messageInformation...>>>`)                      |
| `Response Body`        | Response mapping / extraction config (JSON; `{}` means passthrough)               |


> CONSUMERWITHOUTERROR behaves like CONSUMER but suppresses error propagation when the downstream call fails. MESSAGE additionally persists a `trackingNumber` extracted from the response via `trackingNumberSchemaInHttpResponse`.

**Group 2 — Logic/branching nodes: IFELSE, FUNCTION_V2, FUNCTION_V3**

These steps express conditional branching or internal function execution. They use the shared skeleton **without** Group 1 HTTP fields, and instead expose:


| Field   | Description                                                                                                                                                                                                                |
| ------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Logic` | Large JSON block. For IFELSE: branch result / assignment JSON applied when rules match. For FUNCTION_V2 / FUNCTION_V3: function configuration (e.g. `className` pointing to a Java class, parameters, transformation spec) |


> FUNCTION v1 is not supported. FUNCTION_V2 and FUNCTION_V3 are the only supported function variants.

**Acceptance criteria**:

- AC-07-1: Clicking a node opens a right-side drawer with a form for that node's type.
- AC-07-2: All node drawers render the shared skeleton: Description, Rules (add/remove per rule), Provider, Type (read-only), Remark.
- AC-07-3: Group 1 drawers (CONSUMER, CONSUMERWITHOUTERROR, MESSAGE) additionally render: Method, Request Url, Internal Request Url, Request Headers, Request Body, Response Body. No Logic field is shown.
- AC-07-4: Group 2 drawers (IFELSE, FUNCTION_V2, FUNCTION_V3) additionally render: Logic (JSON editor). No HTTP call fields (Method, URLs, Headers, Bodies) are shown.
- AC-07-5: Drawer shows the current node's saved values when reopened.
- AC-07-6: Saving the drawer form updates the node's data in the canvas state; unsaved drawer changes are not committed to the workflow until the user clicks the header Save button.

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

### 3.3 Workflow Execution (Online API) & Records

#### US-12 — Submit a workflow execution request

> As an application developer, I want to POST a payload to the Online API so the system enriches and dispatches it according to the configured workflow. Processing runs in two sequential phases: **enrichment** (CONSUMER / CONSUMERWITHOUTERROR / IFELSE / FUNCTION steps run in `logicOrder`, building up the runtime payload) followed by **dispatch** (MESSAGE steps send the enriched payload to downstream channels).

**Acceptance criteria**:

- AC-12-1: `POST /api/workflow?applicationName=APP&confirmationNumber=XYZ` with `X-Request-Correlation-Id` header accepted.
- AC-12-2: JSON and XML content types both accepted.
- AC-12-3: If the application is configured for **async** execution, `200 OK` is returned immediately and processing continues in the background. If configured for **sync**, the response is returned after processing completes.
- AC-12-4: If `applicationName` does not exist → `M0001` error.
- AC-12-5: If `X-Request-Correlation-Id` already exists for this application → `M0002` duplicate error.

#### US-13 — Enrichment pipeline execution

> As a system operator, I want the platform to call backend services and merge their responses into the runtime payload so downstream dispatch steps have full context.

**Acceptance criteria**:

- AC-13-1: Steps with type CONSUMER, CONSUMERWITHOUTERROR, IFELSE, FUNCTION_V2, or FUNCTION_V3 execute in `logicOrder` sequence before any dispatch step.
- AC-13-2: For each enrichment step: all Rules (JSONPath expressions) are evaluated against the current runtime payload; if all rules match, the configured action executes (HTTP call for CONSUMER / CONSUMERWITHOUTERROR; Logic JSON applied for IFELSE; Java method invoked for FUNCTION_V2 / FUNCTION_V3) and the result is merged into `messageInformation` of the runtime payload. A single rule failing to match causes the entire step to be skipped.
- AC-13-3: If no rules match for a given step, that step is skipped — no action or fallback is applied. For IFELSE steps specifically, the `Logic` JSON is applied to the payload only when rules **do** match; there is no automatic fallback when they don't.
- AC-13-4: Enrichment outcome updates `WORKFLOW_RECORD.overallStatus` to `GI_SUCCESS` or `GI_FAIL`.

#### US-14 — Message dispatch

> As a system operator, I want enriched payloads to be sent to the configured downstream channels after enrichment completes.

**Acceptance criteria**:

- AC-14-1: Steps with type MESSAGE/DISPATCH execute after all enrichment steps.
- AC-14-2: Each dispatch step makes an HTTP call to its configured URL with the enriched payload.
- AC-14-3: Tracking numbers are extracted from the dispatch response per `trackingNumberSchemaInHttpResponse` and persisted.
- AC-14-4: Channel-specific statuses (smsStatus, emailStatus, pushNotificationStatus) are updated in `WORKFLOW_RECORD`.
- AC-14-5: Final `overallStatus` reflects the combined result: `FB_ALL_SUCCESS`, `FB_PARTIAL_SUCCESS`, `FB_ALL_FAIL`, etc.
- AC-14-6: The channel status model is designed to be extensible for future channel types beyond SMS/Email/Push without breaking existing records.

#### US-15 — Idempotent request handling

> As an application developer, I want duplicate submissions with the same correlation ID to be safely rejected so I don't trigger double dispatch.

**Acceptance criteria**:

- AC-15-1: The combination of `X-Request-Correlation-Id` + `applicationName` is unique-indexed in `WORKFLOW_RECORD`.
- AC-15-2: A second identical request returns `M0002` without creating a new record or triggering dispatch.
- AC-15-3: Retry re-submissions (with `isSelfRequest=true`) bypass the duplicate check.

#### US-16 — Retry on failure

> As a system operator, I want failed workflow executions to be retried automatically based on configured error codes so transient failures are recovered without manual intervention.

**Acceptance criteria**:

- AC-16-1: If a step returns an HTTP error code listed in the application's `retryErrorCodes` (configured per application in `WorkflowEntitySetting.retryProperties`), a retry workflow record is created with `originWorkflowRecordId` pointing to the original.
- AC-16-2: Retry attempts are tracked via `retryTimes` on `WORKFLOW_RECORD`.
- AC-16-3: If all retries are exhausted, `overallStatus` is set to `RETRY_ALL_FAIL`.

#### US-19 — Browse execution records

> As an operations engineer, I want to search and filter `WORKFLOW_RECORD` entries for an application so I can monitor execution outcomes, trace failures, and look up individual transactions.

**Acceptance criteria**:

- AC-19-1: A dedicated **Execution Records** / **Runs** page (or tab per application) is accessible from the UI.
- AC-19-2: The table displays per-record columns including: `applicationName`, `transactionConfirmationNumber`, `requestCorrelationId`, `overallStatus`, `smsStatus`, `emailStatus`, `pushNotificationStatus`, `trackingNumber`, `customerId`, `retryTimes`, `createdDateTime`, `lastModifiedDateTime`. Records may have a parent record (`originWorkflowRecordId`): MESSAGE steps each create a **child record** linked to the parent ingestion record; retry attempts also link back to the original via `originWorkflowRecordId`. The UI must reflect this parent/child hierarchy (e.g. expandable rows or a linked detail view).
- AC-19-3: The table supports filtering by any combination of: `applicationName`, `overallStatus`, date range (`createdDateTime`), `transactionConfirmationNumber` (**primary business-side lookup key** — how operators trace a specific transaction), `trackingNumber`, `customerId`.
- AC-19-4: Results are paginated.
- AC-19-5: Selecting a record shows its detail view, including `workflowProvider` and `providerDescription`.
- AC-19-6: The Online API exposes a read endpoint (`GET /api/workflow/records` or equivalent) that the UI calls; filtering maps to indexed columns in `WORKFLOW_RECORD`.

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


| Area                      | Requirement                                                                                                                                                                                                                       |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Idempotency**           | Online API must reject duplicate correlation IDs per application                                                                                                                                                                  |
| **Async/Sync execution**  | Enrichment and dispatch mode (async or sync) is configurable per application in `WorkflowEntitySetting`. In async mode, `200 OK` is returned before processing; in sync mode, the response is returned after processing completes |
| **Audit trail**           | Every change to `WorkflowEntitySetting` and its associated Workflow payload must be tracked via Envers revision history                                                                                                           |
| **Encrypted storage**     | Runtime payloads in `WORKFLOW_RECORD.workflowTransactionDetails` must be encrypted at rest                                                                                                                                        |
| **Schema ownership**      | `workflow-online-api` never mutates the shared schema (`ddl-auto=none`); only `workflow-operation-api` owns DDL                                                                                                                   |
| **Payload formats**       | Online API must accept both `application/json` and `application/xml`                                                                                                                                                              |
| **Error codes**           | Operation API uses structured `WF-4xx-xxx` codes; Online API uses `M000x` codes                                                                                                                                                   |
| **Base URL isolation**    | UI configures Operation and Online API base URLs independently (`VITE_OPERATION_API_BASE`, `VITE_ONLINE_API_BASE`)                                                                                                                |
| **Channel extensibility** | Channel status model in `WORKFLOW_RECORD` must accommodate future channel types beyond SMS/Email/Push without breaking existing records                                                                                           |


---

## 5. Out of Scope (v1.0)

- User authentication / RBAC on the UI or APIs
- Bulk workflow execution / batch ingestion
- Visual execution trace (replay of enrichment steps in canvas)
- Workflow versioning (multiple live versions per application)
- Real-time execution status push (WebSocket / SSE)

**Future enhancements (not in current scope, to be revisited):**

- **Payload encryption / decryption**: `WORKFLOW_RECORD.workflowTransactionDetails` is currently encrypted using a proprietary mechanism. The target approach is Base64 encoding/decoding for simplicity and interoperability. The exact encryption strategy is not yet finalised — this is deferred as a future enhancement and should not block current delivery.

---

## 6. Decision Log (resolved from v1.0 open questions)


| #    | Decision                                                                                                                                                                                                                |
| ---- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| OQ-1 | **IN SCOPE**: UI surfaces a filterable/searchable execution records table backed by `WORKFLOW_RECORD` → captured as US-19                                                                                               |
| OQ-2 | **FUNCTION_V2 and FUNCTION_V3 supported**: FUNCTION v1 is not supported going forward; removed from drawer forms and backend type handling. FUNCTION_V2 and FUNCTION_V3 are both valid variants                         |
| OQ-3 | **Full settings edit in UI**: All `WorkflowEntitySetting` fields (enabled, retry, tracking, region, eimId, retryProperties, etc.) must be editable from Application Management — no direct DB edits → captured as US-18 |
| OQ-4 | **Channel extensibility**: New channel types planned but not in current scope; model must be extensible → reflected in AC-14-6 and NFR                                                                                  |
| OQ-5 | **Retry config is per application** (same as OQ-3): retry error codes and max attempts are part of `WorkflowEntitySetting.retryProperties` and editable via US-18                                                       |


---

*End of PM Doc v1.1 — please review and confirm before this is handed to the Delivery Manager.*

---

## Canvas AI Explainer (v1.3 addendum)

**Date**: 2026-04-06  
**Status**: Implemented — pending PM sign-off

This section documents the Canvas AI Explainer feature added to the workflow editor header. It is appended as an addendum to v1.2 and does not alter any prior acceptance criteria.

---

### 3.5 Canvas AI Explainer

#### US-20 — Explain the current workflow using AI

> As an integration engineer, I want to click an "Explain" button in the canvas header so I can get a plain-language summary of the current workflow's steps, rules, and logic without having to read raw JSON configuration.

**Acceptance criteria**:

- AC-20-1: An **Explain** button is present in the workflow canvas header, positioned to the left of the existing **Run** button.
- AC-20-2: On first click (or whenever no AI token is stored), the user is prompted to supply a personal AI token before any API call is made. Accepted token formats are:
  - Anthropic Claude API key (`sk-ant-…`)
  - GitHub Models token (`ghp_…`, `ghu_…`, or `ghs_…`)
- AC-20-3: The supplied token is stored in the browser's `localStorage` under the key `ai_explain_token`. It is **never** transmitted to the platform's own backend services (`workflow-operation-api` or `workflow-online-api`).
- AC-20-4: Once a valid token is present, clicking Explain reads the current workflow's `pluginList` from canvas state — capturing, for each step: `type`, `description`, `rules`, `linkingId`, and `URL` — and assembles a structured prompt describing the workflow.
- AC-20-5: The prompt is sent directly from the browser to the appropriate AI backend:
  - **Anthropic token** → Anthropic Claude API (`api.anthropic.com`)
  - **GitHub token** → GitHub Models Azure inference endpoint
- AC-20-6: The AI-generated explanation is displayed in a modal dialog. The modal renders the response as formatted text.
- AC-20-7: The modal footer includes a **Clear token** action that removes `ai_explain_token` from `localStorage`. After clearing, the next Explain click re-triggers the token prompt (AC-20-2).
- AC-20-8: If the AI API call fails (network error, invalid token, rate limit), a clear error message is shown inside the modal; no crash or unhandled exception occurs.
- AC-20-9: The Explain feature operates entirely client-side. No workflow data or AI token leaves the browser except via the direct call to the chosen AI API endpoint.

**Out of scope for this feature**:

- Persisting or syncing the AI token across devices or users — `localStorage` is per-browser only.
- Server-side proxying of AI API requests.
- Saving or exporting the generated explanation.
- Streaming / typewriter rendering of the AI response.
- Support for AI providers beyond Anthropic Claude and GitHub Models.
- Role-based gating — the Explain button is visible to all users with access to the canvas editor.