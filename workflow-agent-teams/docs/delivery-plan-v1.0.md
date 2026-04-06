# Delivery Plan — Workflow Platform
**Version**: 1.0  
**Date**: 2026-04-05  
**Status**: Active  
**Inputs**: PM Doc v1.2 ✓ · Architect Doc v1.1 ✓ · Test Doc v1.0 ✓ (all approved)

---

## 1. Execution Principles

- **TDD gate**: implementation roles write failing tests first, then implement until green
- **No task starts** until its prerequisite tasks are marked complete (see dependency column)
- **Conflict escalation**: any conflict with PM/Arch/Test Doc is escalated back to the respective role — Delivery Manager does NOT resolve unilaterally
- **Deployment gate**: UI Test + API Test reports submitted to Test Manager → Test Manager issues clearance → Ops deploys

---

## 2. Milestones

| Milestone | Definition of Done |
|-----------|-------------------|
| **M1 — Schema ready** | DB migrations applied; both backends start cleanly against new schema |
| **M2 — Backend complete** | All new/changed endpoints implemented and unit-tested; TDD green |
| **M3 — Frontend complete** | All new UI features implemented; UI builds; connects to real backends |
| **M4 — Test execution complete** | All TC-01 to TC-19 + TC-N-01 to TC-N-13 executed; report submitted to Test Manager |
| **M5 — Deployment cleared** | Test Manager issues clearance; Ops deploys to target environment |

---

## 3. Task Board

### TRACK A — Database (prerequisite for all backend work)

| Task | Description | Arch Doc ref | TC ref | Depends on | Role |
|------|-------------|-------------|--------|-----------|------|
| **DB-01** | Add `async_mode` boolean column (default `true`) to `WORKFLOW_ENTITY_SETTING` | M-01 | TC-18-2 | — | Database |
| **DB-02** | Verify `WORKFLOW_TYPE.type` varchar column accepts `CONSUMERWITHOUTERROR` and `FUNCTION_V3` values — no schema change expected, confirm and document | M-02 | TC-07-2, TC-13-6 | — | Database |
| **DB-03** | Verify `WORKFLOW_RECORD.origin_workflow_record_id` index exists; add if missing | M-04 | TC-19-8 | — | Database |
| **DB-04** | Confirm no DB-level FK constraint blocks delete of `WORKFLOW_ENTITY_SETTING` when `WORKFLOW_RECORD` rows exist; document finding | M-03 | TC-03-1 | — | Database |

> **M1 gate**: DB-01 to DB-04 all complete.

---

### TRACK B — Control Backend (`workflow-operation-api`)

| Task | Description | Arch Doc ref | AC ref | TC ref | Depends on | Role |
|------|-------------|-------------|--------|--------|-----------|------|
| **OP-01** | Add `asyncMode` field to `WorkflowEntitySetting` entity and JPA mapping | AD-4, M-01 | AC-18-2 | TC-18-2 | DB-01 | Control Backend |
| **OP-02** | Implement `PATCH /api/workflow/entity-setting?applicationName` — partial update of entity setting fields only; does NOT touch workflow definition; triggers new Envers revision | 5.1 | AC-18-1 to AC-18-5 | TC-18-1 to TC-18-7 | OP-01 | Control Backend |
| **OP-03** | Remove `WF-409-201` guard from `DELETE /api/workflow` — deletion always proceeds regardless of `WORKFLOW_RECORD` rows; `WORKFLOW_RECORD` rows retained as orphans | 5.3, AD-1 | AC-03-3 | TC-03-1 to TC-03-2 | DB-04 | Control Backend |
| **OP-04** | Implement `GET /api/workflow/records` on operation-api — paginated, filterable query of `WORKFLOW_RECORD` table; filters: applicationName, overallStatus, from/to dates, transactionConfirmationNumber, trackingNumber, customerId | 5.2, AD-2 | AC-19-1 to AC-19-6 | TC-19-1 to TC-19-7 | DB-03 | Control Backend |
| **OP-05** | Implement `GET /api/workflow/records/{id}` — single record detail with child records (`originWorkflowRecordId = id`) in `children` array | 5.2 | AC-19-5 | TC-19-8 to TC-19-9 | OP-04 | Control Backend |
| **OP-06** | Add `CONSUMERWITHOUTERROR` and `FUNCTION_V3` to `WorkflowType` type handling in operation-api (persistence, retrieval, Base64 encode/decode) | 6.1 | AC-07-2, AC-07-6 | TC-07-2, TC-07-6 | DB-02 | Control Backend |
| **OP-07** | Retain read support for `FUNCTION` (v1) type in existing DB rows — no new records created, but existing rows must still load without error | 6.1 | — | TC-N-07 | — | Control Backend |

> **M2 gate (control backend)**: OP-01 to OP-07 all complete and unit-tested.

---

### TRACK C — Online Backend (`workflow-online-api`)

| Task | Description | Arch Doc ref | AC ref | TC ref | Depends on | Role |
|------|-------------|-------------|--------|--------|-----------|------|
| **ON-01** | Add `CONSUMERWITHOUTERROR` to `Type` enum | 6.2 | AC-13-6 | TC-13-6 | DB-02 | Online Backend |
| **ON-02** | Implement `CONSUMERWITHOUTERROR` executor in `WorkflowRuleAndTypeService` — same HTTP call as CONSUMER; wrap in try/catch; swallow exception and log; execution continues to next step | 6.2 | AC-13-6 | TC-13-6 | ON-01 | Online Backend |
| **ON-03** | Add `FUNCTION_V3` to `Type` enum | 6.2 | AC-13-8 | TC-13-8 | — | Online Backend |
| **ON-04** | Implement `FUNCTION_V3` executor — identical to `FUNCTION_V2` for now; add TODO comment referencing AD-3 for future differentiation | 6.2 | AC-13-8 | TC-13-8 | ON-03 | Online Backend |
| **ON-05** | Read `asyncMode` field from `WorkflowEntitySetting` and apply to dispatch pipeline — if `asyncMode=false`, execute enrichment + dispatch synchronously before returning response | AD-4 | AC-12-3 | TC-12-1, TC-12-2 | OP-01 | Online Backend |

> **M2 gate (online backend)**: ON-01 to ON-05 all complete and unit-tested.

---

### TRACK D — Frontend (`workflow-ui`)

| Task | Description | PM Doc ref | AC ref | TC ref | Depends on | Role |
|------|-------------|-----------|--------|--------|-----------|------|
| **FE-01** | Add `CONSUMERWITHOUTERROR` and `FUNCTION_V3` node types to canvas sidebar and canvas renderer | US-04, US-05 | AC-04-2, AC-05-1 | TC-04-2, TC-05-2, TC-07-6 | OP-06 | Frontend |
| **FE-02** | Remove `START` node from sidebar and canvas | US-04 | AC-04-2, AC-05-1 | TC-04-3 | — | Frontend |
| **FE-03** | Implement Group 1 drawer (HTTP-call) for `CONSUMERWITHOUTERROR` — identical form to `CONSUMER` | US-07 | AC-07-2, AC-07-3 | TC-07-2 | FE-01 | Frontend |
| **FE-04** | Implement Group 2 drawer (Logic/branching) for `FUNCTION_V3` — identical form to `FUNCTION_V2` | US-07 | AC-07-4, AC-07-6 | TC-07-6 | FE-01 | Frontend |
| **FE-05** | Implement **Edit Settings** modal/drawer (US-18) — pre-filled form for all `WorkflowEntitySetting` fields including `asyncMode` and `retryProperties`; calls `PATCH /api/workflow/entity-setting` | US-18 | AC-18-1 to AC-18-5 | TC-18-5 to TC-18-6 | OP-02 | Frontend |
| **FE-06** | Implement **Execution Records** page (US-19) — table with all columns, filter bar, pagination; calls `GET /api/workflow/records` | US-19 | AC-19-1 to AC-19-5 | TC-19-10 to TC-19-12 | OP-04 | Frontend |
| **FE-07** | Implement record detail view with parent/child hierarchy — calls `GET /api/workflow/records/{id}`; renders child records (MESSAGE steps + retry chain) | US-19 | AC-19-5 | TC-19-11 | OP-05 | Frontend |
| **FE-08** | Implement **Rollback** in history view — "Rollback to this version" button; confirmation step; extracts workflow payload from revision and calls `POST /api/workflow` | US-11 | AC-11-5 to AC-11-9 | TC-11-3 to TC-11-8 | — | Frontend |
| **FE-09** | Add `applicationName` description column to application list table | US-01 | AC-01-1 | TC-01-3 | — | Frontend |

> **M3 gate**: FE-01 to FE-09 all complete; UI builds; all pages connect to real backends.

---

### TRACK E — API Test

| Task | Description | TC ref | Depends on |
|------|-------------|--------|-----------|
| **AT-01** | Automate TC-03 (delete without record check) | TC-03-1 to TC-03-3 | OP-03 |
| **AT-02** | Automate TC-18 (PATCH entity settings) | TC-18-1 to TC-18-7 | OP-02 |
| **AT-03** | Automate TC-10 (autoCopy copies both entity settings + workflow) | TC-10-1 to TC-10-5 | — |
| **AT-04** | Automate TC-11 API cases (history + rollback API) | TC-11-1 to TC-11-7 | — |
| **AT-05** | Automate TC-12 to TC-16 (execution pipeline: enrichment, dispatch, idempotency, retry) | TC-12 to TC-16 | ON-02, ON-04, ON-05 |
| **AT-06** | Automate TC-19 (execution records: list, filters, detail + children) | TC-19-1 to TC-19-9 | OP-04, OP-05 |
| **AT-07** | Automate TC-N-01 to TC-N-13 (negative and boundary cases) | TC-N series | M2 complete |
| **AT-08** | Run full API test suite; produce test report | All TC API cases | AT-01 to AT-07 |

---

### TRACK F — UI Test

| Task | Description | TC ref | Depends on |
|------|-------------|--------|-----------|
| **UT-01** | TC-01 to TC-03 (application list, create, delete) | TC-01 to TC-03 | FE-09, OP-03 |
| **UT-02** | TC-04 to TC-09 (canvas: nodes, edges, drawer, save) | TC-04 to TC-09 | FE-01 to FE-04 |
| **UT-03** | TC-10, TC-11 (copy, history, rollback in UI) | TC-10-6, TC-11-3 to TC-11-8 | FE-08 |
| **UT-04** | TC-17 (test runner) | TC-17-1 to TC-17-4 | — |
| **UT-05** | TC-18 (edit settings UI) | TC-18-5 to TC-18-6 | FE-05 |
| **UT-06** | TC-19 (execution records UI: table, filters, detail) | TC-19-10 to TC-19-12 | FE-06, FE-07 |
| **UT-07** | Run full UI test suite; produce test report | All TC UI cases | UT-01 to UT-06 |

---

### TRACK G — Ops (deployment — after Test Manager clearance only)

| Task | Description | Depends on |
|------|-------------|-----------|
| **OPS-01** | Apply DB migrations (DB-01 to DB-04) to target environment | M1 gate + Test Manager clearance |
| **OPS-02** | Deploy `workflow-operation-api` build artifact | M2 gate + OPS-01 |
| **OPS-03** | Deploy `workflow-online-api` build artifact | M2 gate + OPS-01 |
| **OPS-04** | Deploy `workflow-ui` build artifact | M3 gate + OPS-02 + OPS-03 |
| **OPS-05** | Health check all three services post-deploy; report result | OPS-02 to OPS-04 |

> **OPS tasks do not start until Test Manager issues deployment clearance based on AT-08 + UT-07 reports.**

---

## 4. Execution Sequence (recommended)

```
DB-01 → DB-04  (parallel, unblocked)
         ↓
OP-01 → OP-07  (sequential where dependent; OP-02 needs OP-01; OP-04/05 sequential)
ON-01 → ON-05  (sequential where dependent; can run in parallel with OP track)
         ↓
FE-01 → FE-09  (can start once target backend endpoint is ready per task)
         ↓
AT-01 → AT-08  (API tests against M2 backends)
UT-01 → UT-07  (UI tests against M3 frontend)
         ↓
Test Manager review → clearance
         ↓
OPS-01 → OPS-05
```

---

## 5. Conflict Escalation Paths

| Conflict type | Escalate to |
|--------------|-------------|
| Requirement unclear or contradicted | Product Manager (you ↔ PM role) |
| Architecture ambiguity or new technical constraint | Architect (you ↔ Architect role) |
| Test case scope dispute or missing coverage | Test Manager |
| Delivery Manager does NOT resolve any of the above unilaterally | — |

---

*End of Delivery Plan v1.0 — implementation tracks are open. Delivery Manager will track progress and escalate conflicts per Section 5.*
