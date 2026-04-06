# Test Doc — Workflow Platform
**Version**: 1.0  
**Date**: 2026-04-05  
**Status**: **Approved** — 2026-04-05  
**Inputs**: PM Doc v1.2, Architect Doc v1.1

---

## 1. Test Plan

### 1.1 Scope

This Test Doc covers all user stories in PM Doc v1.2. Priority is given to:
- **New endpoints** introduced in Arch Doc v1.1 (PATCH entity settings, GET records, revised DELETE)
- **Changed behaviour** (delete no longer blocked by execution records; CONSUMERWITHOUTERROR error suppression; rollback restores workflow definition only)
- **Core execution pipeline** (rules AND-gate, two-phase enrichment → dispatch)

### 1.2 Test Approach

| Layer | Tool / Method | Owner |
|-------|--------------|-------|
| API tests | HTTP client (Postman / REST Assured / equivalent); automated scripts | API Test Engineer |
| UI tests | Playwright / Cypress; manual exploratory where noted | UI Test Engineer |
| Unit / integration | Maven (`mvn test` / `mvn -Pintegration verify`) per repo | Backend developers (TDD gate) |

### 1.3 Environments

| Environment | operation-api | online-api | workflow-ui | DB |
|-------------|--------------|-----------|-------------|-----|
| Local dev | `localhost:8080` | `localhost:8081` | `localhost:5173` | Neon (dev schema) |
| Integration | As per CI env vars | As per CI env vars | Built artifact | Neon (test schema) |

### 1.4 Entry Criteria

- PM Doc v1.2 and Architect Doc v1.1 both approved by human
- Both backends build and pass unit tests (`mvn clean verify`)
- UI builds without errors (`npm run build`)
- Test DB seeded with prerequisite data (see Section 2)

### 1.5 Exit Criteria

- All TC marked **Must Pass** pass without open defects
- No severity-1 (blocking) defects open
- API test suite runs green in CI
- Test Manager issues deployment clearance

---

## 2. Environment & Data Prerequisites

### 2.1 Seed Data Required Before Testing

| Data item | Purpose | How to create |
|-----------|---------|--------------|
| Application `TEST_APP_A` with a complete workflow (≥2 enrichment steps, ≥1 MESSAGE step) | Base app for most tests | POST /api/workflow (operation-api) |
| Application `TEST_APP_B` empty (no workflow steps) | Baseline for copy / rollback targets | POST /api/workflow (empty body) |
| Application `TEST_APP_DELETE` with WORKFLOW_RECORD rows | Verify delete proceeds despite execution records | POST /api/workflow + POST /api/workflow (online) |
| At least 1 saved revision for `TEST_APP_A` | Rollback tests | Save workflow, modify it, save again |
| `TEST_APP_ASYNC` with `asyncMode=true` and `TEST_APP_SYNC` with `asyncMode=false` | Execution mode tests | PATCH /api/workflow/entity-setting |

### 2.2 Environment Variables

```
IT_WORKFLOW_APPLICATION_NAME=TEST_APP_A   # integration profile
SPRING_DATASOURCE_URL, _USERNAME, _PASSWORD  # test DB
VITE_OPERATION_API_BASE, VITE_ONLINE_API_BASE  # UI config
```

---

## 3. Test Cases

### 3.1 Application Management

#### TC-01 — List applications (US-01)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-01-1 | GET `/api/workflow/entity-setting` with no filters | Returns paginated list; default page size 5 | API |
| TC-01-2 | GET with `applicationName=TEST` (fuzzy) | Returns all apps whose name contains "TEST" | API |
| TC-01-3 | Open `/workflows` in UI | Shows `applicationName`, `enabled` badge, description, `lastModifiedDateTime` for each row | UI |
| TC-01-4 | Search for partial name in UI search box | List filters to matching apps only | UI |
| TC-01-5 | Navigate to a page with no apps | Empty state message shown | UI |

#### TC-02 — Create application (US-02)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-02-1 | Click Create, enter unique `applicationName`, submit | Application created; UI navigates to canvas editor | UI |
| TC-02-2 | Submit with empty `applicationName` | Validation error shown; form stays open | UI |
| TC-02-3 | Submit with duplicate `applicationName` | Error shown (WF-400-101); form stays open | UI / API |
| TC-02-4 | POST `/api/workflow?applicationName=NEW_APP` with empty body | 200; empty workflow created in DB | API |

#### TC-03 — Delete application (US-03)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-03-1 | Delete `TEST_APP_DELETE` (which has WORKFLOW_RECORD rows) | **Deletion succeeds** — no WF-409-201 error; app removed from list | UI / API |
| TC-03-2 | After TC-03-1, query `WORKFLOW_RECORD` for `TEST_APP_DELETE` | Records still exist as orphans (`applicationName` preserved) | API / DB |
| TC-03-3 | DELETE app that does not exist | 404 or appropriate error | API |
| TC-03-4 | Delete shows confirmation modal before proceeding | Modal appears; cancel aborts deletion | UI |

#### TC-18 — Edit application settings (US-18)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-18-1 | PATCH `/api/workflow/entity-setting?applicationName=TEST_APP_A` with `{"enabled": false}` | 200; `enabled` updated; other fields unchanged; new Envers revision created | API |
| TC-18-2 | PATCH with `{"asyncMode": false}` | 200; `asyncMode` updated to false | API |
| TC-18-3 | PATCH with full `retryProperties` block | 200; retry error codes and max attempts updated | API |
| TC-18-4 | PATCH with unknown applicationName | 404 WF-404-101 | API |
| TC-18-5 | Open Settings form in UI for `TEST_APP_A` | Form pre-filled with all current values including `asyncMode`, `retryProperties` | UI |
| TC-18-6 | Toggle `enabled` off in UI and save | Application list badge updates immediately to disabled | UI |
| TC-18-7 | Verify PATCH does not touch workflow definition | After PATCH, GET `/api/workflow?applicationName=TEST_APP_A` returns unchanged workflow | API |

#### TC-10 — Copy workflow (US-10)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-10-1 | POST `/api/workflow/autoCopy?fromApplicationName=TEST_APP_A&toApplicationName=TEST_APP_B` | 200; `TEST_APP_B` now has same workflow definition AND same entity settings as `TEST_APP_A` | API |
| TC-10-2 | Verify entity settings copied: GET entity-setting for `TEST_APP_B` | Fields (enabled, retry, region, asyncMode, retryProperties…) match `TEST_APP_A` | API |
| TC-10-3 | Verify workflow definition copied: GET workflow for `TEST_APP_B` | `pluginList` matches `TEST_APP_A` | API |
| TC-10-4 | autoCopy where source = target | Error returned | API |
| TC-10-5 | autoCopy where source does not exist | Error WF-400-302 | API |
| TC-10-6 | Copy affordance in UI pre-fills source app name when opened from a row | Source field shows `TEST_APP_A` | UI |

#### TC-11 — Configuration history & rollback (US-11)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-11-1 | GET `/api/workflow/entity-setting/history?applicationName=TEST_APP_A` | Returns paginated revision list with rev number, timestamp, change type, snapshot | API |
| TC-11-2 | History accessible even after WORKFLOW_RECORD rows exist | History endpoint still returns results | API |
| TC-11-3 | Click a revision in UI — view workflow detail | Full workflow step list from that revision displayed | UI |
| TC-11-4 | Click "Rollback to this version" and confirm | POST `/api/workflow?applicationName=TEST_APP_A` called with revision's workflow payload | UI / API |
| TC-11-5 | After rollback: GET workflow for `TEST_APP_A` | Workflow definition matches the rolled-back revision | API |
| TC-11-6 | After rollback: GET entity-setting for `TEST_APP_A` | Entity settings fields **unchanged** — only workflow definition was restored | API |
| TC-11-7 | After rollback: GET history for `TEST_APP_A` | New revision entry exists (rollback itself is audited) | API |
| TC-11-8 | Rollback and then cancel at confirmation modal | No change made; workflow remains as before | UI |

---

### 3.2 Workflow Editor

#### TC-04 — Open workflow in canvas (US-04)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-04-1 | Navigate to `/workflows/TEST_APP_A` | Canvas renders with correct nodes and edges from saved workflow | UI |
| TC-04-2 | All 6 node types present in sidebar | CONSUMER, CONSUMERWITHOUTERROR, MESSAGE, IFELSE, FUNCTION_V2, FUNCTION_V3 shown | UI |
| TC-04-3 | No START node present | START node not in sidebar or canvas | UI |
| TC-04-4 | Canvas supports pan and zoom | Panning and zooming work without errors | UI |

#### TC-05 — Add & connect steps (US-05, US-06)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-05-1 | Drag CONSUMER from sidebar to canvas | New CONSUMER node appears at drop position | UI |
| TC-05-2 | Drag CONSUMERWITHOUTERROR to canvas | New node appears | UI |
| TC-05-3 | Connect two nodes by dragging from output to input handle | Directed edge appears with arrow marker | UI |
| TC-05-4 | Delete a node (Backspace/Delete) | Node and its connected edges removed | UI |

#### TC-07 — Configure workflow steps — drawer (US-07)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-07-1 | Click a CONSUMER node | Drawer opens with shared skeleton (Description, Rules, Provider, Type, Remark) + HTTP fields (Method, Request Url, Internal Request Url, Headers, Body, Response Body) | UI |
| TC-07-2 | Click a CONSUMERWITHOUTERROR node | Same fields as CONSUMER | UI |
| TC-07-3 | Click a MESSAGE node | Same HTTP fields as CONSUMER | UI |
| TC-07-4 | Click an IFELSE node | Drawer shows shared skeleton + Logic JSON editor; **no HTTP fields** | UI |
| TC-07-5 | Click FUNCTION_V2 node | Drawer shows shared skeleton + Logic JSON editor; no HTTP fields | UI |
| TC-07-6 | Click FUNCTION_V3 node | Drawer shows shared skeleton + Logic JSON editor; no HTTP fields | UI |
| TC-07-7 | Add a second Rule to any node | Rules list grows; each rule has expression + remark fields | UI |
| TC-07-8 | Save drawer, close, reopen | Previously entered values are preserved | UI |
| TC-07-9 | Edit drawer fields without clicking header Save | Navigating away prompts or discards — canvas state not yet persisted to server | UI |

#### TC-09 — Save workflow (US-09)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-09-1 | Click Save in editor header | Success confirmation shown; GET workflow returns updated definition | UI / API |
| TC-09-2 | Verify `logicOrder` reflects edge connection order | Steps ordered correctly in DB | API |
| TC-09-3 | Save failure (simulate network error) | Error shown; canvas data preserved | UI |

---

### 3.3 Workflow Execution

#### TC-12 — Submit execution request (US-12)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-12-1 | POST `/api/workflow?applicationName=TEST_APP_ASYNC` with valid JSON body and correlation ID | 200 returned immediately; WORKFLOW_RECORD created with status INITIATION | API |
| TC-12-2 | POST to `TEST_APP_SYNC` (asyncMode=false) | Response returned after processing completes | API |
| TC-12-3 | POST with unknown applicationName | M0001 error | API |
| TC-12-4 | POST with XML body (`Content-Type: application/xml`) | 200; XML parsed and processed | API |
| TC-12-5 | POST with missing `X-Request-Correlation-Id` header | Error or auto-generated ID — verify behaviour | API |

#### TC-15 — Idempotency (US-15)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-15-1 | POST same `X-Request-Correlation-Id` twice to same app | Second request returns M0002; only one WORKFLOW_RECORD created | API |
| TC-15-2 | Same correlation ID to different applicationName | Both accepted (different combination) | API |

#### TC-13 — Enrichment pipeline — rules AND-gate (US-13)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-13-1 | Step with all rules matching → CONSUMER action | HTTP call made; response merged into `messageInformation` | API / Integration |
| TC-13-2 | Step where one rule does NOT match | Step skipped entirely; no HTTP call; payload unchanged | API / Integration |
| TC-13-3 | IFELSE step where rules match | Logic JSON applied to payload | API / Integration |
| TC-13-4 | IFELSE step where rules do NOT match | Step skipped; **no fallback applied** | API / Integration |
| TC-13-5 | CONSUMER step — downstream HTTP returns error | `overallStatus` set to `GI_FAIL`; enrichment halts; no dispatch | API / Integration |
| TC-13-6 | CONSUMERWITHOUTERROR step — downstream HTTP returns error | Error suppressed; execution continues to next step; `overallStatus` not set to GI_FAIL by this step alone | API / Integration |
| TC-13-7 | FUNCTION_V2 step with rules matching | Java method invoked; output wrapped in `WorkflowRuntimePayload.reference()` and available downstream | API / Integration |
| TC-13-8 | FUNCTION_V3 step with rules matching | Executes same as FUNCTION_V2 (current implementation) | API / Integration |

#### TC-14 — Message dispatch (US-14)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-14-1 | MESSAGE step with rules matching | HTTP call made to dispatch URL; child WORKFLOW_RECORD created with channel status and trackingNumber | API / Integration |
| TC-14-2 | MESSAGE step rules do NOT match | Step skipped; no child record created | API / Integration |
| TC-14-3 | MESSAGE HTTP call fails | Child record status = SM_FAIL; parent record continues; other MESSAGE steps still execute | API / Integration |
| TC-14-4 | After all MESSAGE steps | Parent WORKFLOW_RECORD `overallStatus` reflects combined result (FB_ALL_SUCCESS / FB_PARTIAL_SUCCESS / FB_ALL_FAIL) | API |
| TC-14-5 | Tracking number extraction | `trackingNumber` in child record matches value extracted by `trackingNumberSchemaInHttpResponse` | API / Integration |

#### TC-16 — Retry on failure (US-16)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-16-1 | Step returns HTTP error code in `retryErrorCodes` list | New WORKFLOW_RECORD created with `originWorkflowRecordId` pointing to original | API / Integration |
| TC-16-2 | Retry exhausted (all attempts fail) | `overallStatus` = RETRY_ALL_FAIL | API / Integration |
| TC-16-3 | Retry re-submission with `isSelfRequest=true` | Duplicate check bypassed; retry processed | API |

---

### 3.4 Execution Records

#### TC-19 — Browse execution records (US-19)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-19-1 | GET `/api/workflow/records?applicationName=TEST_APP_A&page=0&size=20` | Returns paginated list of WORKFLOW_RECORD rows for TEST_APP_A | API |
| TC-19-2 | Filter by `overallStatus=GI_FAIL` | Only GI_FAIL records returned | API |
| TC-19-3 | Filter by `transactionConfirmationNumber=XYZ` | Exact match on confirmation number | API |
| TC-19-4 | Filter by date range (`from` + `to`) | Only records within range returned | API |
| TC-19-5 | Filter by `trackingNumber` | Exact match | API |
| TC-19-6 | Filter by `customerId` | Exact match | API |
| TC-19-7 | Combine multiple filters | AND logic — only records matching all filters returned | API |
| TC-19-8 | GET `/api/workflow/records/{id}` for a parent record | Returns parent record + child records (MESSAGE steps + retry chain) in `children` array | API |
| TC-19-9 | GET record ID that does not exist | 404 | API |
| TC-19-10 | Open Execution Records page in UI | Table shows all columns: applicationName, confirmationNumber, overallStatus, channel statuses, trackingNumber, customerId, retryTimes, dates | UI |
| TC-19-11 | Click a record row in UI | Detail view shows parent + child record hierarchy | UI |
| TC-19-12 | Apply filters in UI | Table updates to filtered results | UI |

---

### 3.5 Test Runner

#### TC-17 — Test workflow from UI (US-17)

| # | Step | Expected Result | Type |
|---|------|----------------|------|
| TC-17-1 | Click Run in editor header | Modal opens with JSON editor | UI |
| TC-17-2 | Submit valid JSON payload | POST sent to online-api; response shown (status + body ≤8000 chars) | UI |
| TC-17-3 | Submit with invalid JSON | Error shown before submit | UI |
| TC-17-4 | Network error during execution | Error message shown clearly in modal | UI |

---

## 4. Negative & Boundary Cases

| TC | Scenario | Expected Result |
|----|----------|----------------|
| TC-N-01 | POST /api/workflow with empty `pluginList` | Empty workflow saved; no error |
| TC-N-02 | PATCH entity-setting with all fields omitted (empty body) | 200; no fields changed (partial update, all optional) |
| TC-N-03 | GET /api/workflow/records with no filters | Returns all records paginated (no crash on missing filters) |
| TC-N-04 | GET /api/workflow/records with `size=0` | Error or default size applied — verify boundary |
| TC-N-05 | GET /api/workflow/records with `size=101` (above max 100) | Capped at 100 or validation error |
| TC-N-06 | Rollback to revision of a workflow that is currently being executed | Rollback saves new definition; in-flight execution uses snapshot at dispatch time — no conflict expected |
| TC-N-07 | autoCopy to an applicationName that already has a workflow | Existing workflow overwritten (create-or-update semantics) — verify this is intentional |
| TC-N-08 | Submit execution to app with `asyncMode=false` and a slow enrichment step | Response delayed until pipeline completes; no timeout on client side |
| TC-N-09 | CONSUMER step with `<<<...>>>` placeholder pointing to missing JSONPath | Verify error handling — does it halt enrichment or produce empty substitution? |
| TC-N-10 | Drawer: save IFELSE Logic field with invalid JSON | Validation error shown; save blocked |
| TC-N-11 | Delete application twice (second call on already-deleted app) | 404 or idempotent 200 — verify |
| TC-N-12 | History endpoint for app with no revisions yet | Returns empty list, not 404 |
| TC-N-13 | Pagination beyond last page (`page=9999`) | Returns empty content list, not error |

---

## 5. Traceability Matrix

| Requirement | Test Cases |
|------------|-----------|
| US-01 List applications | TC-01-1 to TC-01-5 |
| US-02 Create application | TC-02-1 to TC-02-4 |
| US-03 Delete application (no record check) | TC-03-1 to TC-03-4 |
| US-04 Open canvas | TC-04-1 to TC-04-4 |
| US-05 Add step | TC-05-1 to TC-05-2 |
| US-06 Connect steps | TC-05-3 |
| US-07 Configure step (drawer) | TC-07-1 to TC-07-9 |
| US-08 Delete step | TC-05-4 |
| US-09 Save workflow | TC-09-1 to TC-09-3 |
| US-10 Copy workflow | TC-10-1 to TC-10-6 |
| US-11 History + rollback | TC-11-1 to TC-11-8 |
| US-12 Submit execution | TC-12-1 to TC-12-5 |
| US-13 Enrichment pipeline | TC-13-1 to TC-13-8 |
| US-14 Message dispatch | TC-14-1 to TC-14-5 |
| US-15 Idempotency | TC-15-1 to TC-15-2 |
| US-16 Retry | TC-16-1 to TC-16-3 |
| US-17 Test runner | TC-17-1 to TC-17-4 |
| US-18 Edit entity settings | TC-18-1 to TC-18-7 |
| US-19 Browse execution records | TC-19-1 to TC-19-12 |
| Negative / boundary | TC-N-01 to TC-N-13 |

---

## 6. Must-Pass Tests (Deployment Gate)

The following TCs are **blocking** — all must pass before any deployment is approved:

| TC | Reason |
|----|--------|
| TC-03-1 | Changed behaviour — delete must not be blocked by execution records |
| TC-13-5 | CONSUMER failure must halt enrichment (GI_FAIL) |
| TC-13-6 | CONSUMERWITHOUTERROR must suppress error and continue |
| TC-13-4 | IFELSE no-match must skip entirely — no fallback |
| TC-15-1 | Idempotency is a core safety guarantee |
| TC-18-7 | PATCH must not touch workflow definition |
| TC-11-6 | Rollback must not touch entity settings |
| TC-10-2 / TC-10-3 | autoCopy must copy both entity settings and workflow definition |
| TC-19-8 | Execution record detail must return parent + child hierarchy |

---

*End of Test Doc v1.0 — please approve before Delivery Manager dispatches development.*
