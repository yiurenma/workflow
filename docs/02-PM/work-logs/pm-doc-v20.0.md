# PM Doc v20.0 — Applications: Edit Application Name

**Status:** Awaiting approval
**Label:** `TODO-application-table-edit-name`
**Date:** 2026-04-08

---

## Problem Statement

Application names are immutable in the UI after creation. Users who mistype or need to rebrand an application must delete and recreate it, losing all workflow history. The backend API is believed to support updating the record.

## User Story

As an **operations user** on the Applications list,
I want to rename an existing application in-place (via a row action or modal),
So that I can correct names without recreating workflows from scratch.

## Acceptance Criteria

1. A **"Rename"** action is available per application row — either as an additional item in the existing Settings modal or as a dedicated row action button/menu item.
2. The rename UI shows the current name pre-filled and validates: non-empty, max 255 chars, no duplicate name.
3. On submit, the frontend calls the backend update endpoint; on success the table row refreshes with the new name.
4. On API error (duplicate, validation failure, network), a clear error message is shown; the name is not changed in the UI.
5. Desktop and mobile both support the rename flow.
6. **Discovery gate:** Arch doc must confirm whether the existing `PATCH /api/workflow/entity-setting` endpoint supports renaming `applicationName`, or whether a new endpoint is needed; implementation may not proceed until confirmed.
