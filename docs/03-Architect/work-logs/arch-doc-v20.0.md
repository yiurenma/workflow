# Arch Doc v20.0 — Applications: Edit Application Name

**Label:** `TODO-application-table-edit-name`
**Date:** 2026-04-08

---

## Discovery: API Gap

The existing `PATCH /api/workflow/entity-setting?applicationName=X` endpoint uses `applicationName` as a **lookup key** in the URL. The `EntitySettingPatchRequest` DTO contains operational fields (retry, tracking, enabled, etc.) but **not** a rename field.

To rename the application, the backend needs a new endpoint:

```
PATCH /api/workflow/entity-setting/rename?applicationName=OLD&newName=NEW
```

or alternatively extend the PATCH DTO with an optional `newApplicationName` field and handle the rename server-side.

**Chosen approach:** Add `newApplicationName` (optional String) to `EntitySettingPatchRequest`. If present and non-blank, the service layer renames the record. This is a single-file change in the DTO + service, no new route needed.

---

## Backend Changes (`workflow-operation-api`)

| File | Change |
|------|--------|
| `EntitySettingPatchRequest.java` | Add optional `String newApplicationName` field |
| `WorkflowEntitySettingService.java` | If `newApplicationName` non-blank, check uniqueness then update `applicationName` |
| `WorkflowEntitySettingController.java` | No change (existing PATCH route reused) |

---

## Frontend Changes (`workflow-ui`)

**Settings modal** (`-components/settings-modal/index.tsx`):
- Add an "Application Name" input field pre-filled with current name
- On save, if name changed, include `newApplicationName` in PATCH payload
- Show inline error if API returns 409 (duplicate) or 400 (validation)

**React Query invalidation:** after successful rename, invalidate the applications list query so the table row updates.

---

## Validation

- Non-empty, trimmed
- Max 255 characters (match DB column length)
- Unique — enforced server-side; UI shows error on 409

---

## Risk

Medium. Renaming the primary lookup key of the entity is a sensitive operation — all linked records (workflow, history) reference `applicationName` as a string FK. **Arch decision:** rename in `workflow_entity_setting` only; dependent tables (`workflow_record` etc.) use `applicationName` as a denormalized string and do not cascade. Document this in code comments.
