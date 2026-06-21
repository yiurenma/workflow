# Arch Doc v21.0 — Application Name: Description Field (DB + API + UI)

**Label:** `TODO-application-name-description-db-text-ui-api`
**Date:** 2026-04-08

---

## Scope

| Repo | Files changed |
|------|---------------|
| `workflow-operation-api` | Migration SQL, entity, DTO, service (patch) |
| `workflow-ui` | Applications table column, Settings modal textarea |
| `workflow-online-api` | Verify only — no change expected |

---

## Database Migration (`workflow-operation-api`)

New Flyway migration file: `src/main/resources/db/migration/V<next>__add_description_to_entity_setting.sql`

```sql
ALTER TABLE workflow_entity_setting
  ADD COLUMN description TEXT NULL;
```

Nullable — no default needed; existing rows unaffected.

---

## Backend Changes

**Entity** (`WorkflowEntitySetting.java`):
```java
@Column(columnDefinition = "TEXT")
private String description;
```

**Response DTO** (`WorkflowEntitySettingResponse.java` or equivalent):
```java
private String description;
```

**Patch DTO** (`EntitySettingPatchRequest.java`):
```java
private String description; // null = no change; "" = clear
```

**Service** (`WorkflowEntitySettingService.java`): if `description != null`, update the field.

---

## API Verification for `workflow-online-api`

`workflow-online-api` reads entity settings to route workflow execution. It reads `applicationName`, `retry`, `enabled`, `workflow` (the Base64 payload) — **not** `description`. Adding the column is safe; no change needed in `workflow-online-api`.

---

## Frontend Changes (`workflow-ui`)

**Applications table** (`src/routes/workflows/index.tsx`):
- "Description" column: render `record.description ?? '—'` (remove the current `eimId || defaultServiceAccount || region` placeholder).

**Settings modal** (`-components/settings-modal/index.tsx`):
- Add `<Input.TextArea rows={3} maxLength={2000}>` for description, bound to form state.
- Include `description` in the PATCH payload.

**React Query / API hook**: `useWorkflowEntitySettings` and related hooks already fetch the entity settings list — confirm `description` is included in the response after backend change; no hook signature change needed.

---

## Risk

Low for DB/API (additive nullable column). Medium for UI (replaces placeholder logic in the Description column — coordinate with v20.0 Settings modal changes to avoid conflicts).
