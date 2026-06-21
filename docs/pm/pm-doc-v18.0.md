# PM Doc v18.0 — Canvas Mobile Header: Save button inaccessible (overflow)

**Status:** Awaiting approval
**Label:** `TODO-canvas-mobile-header-save-overflow`
**Date:** 2026-04-08

---

## Problem Statement

On the workflow canvas in mobile / narrow-phone layout (≤ 390 px), the header toolbar shows four buttons: **Explain**, **JsonPath**, **Run**, **Save**. The row overflows horizontally and the **Save** button is clipped off-screen, making it unreachable. This regression was introduced in v16.0 when the "JsonPath" button was added without a mobile layout adjustment.

## Root Cause

`WorkflowHeader` renders all four action buttons in a flat `<Space>` regardless of viewport width. On a 390 px screen there is insufficient horizontal room after the back arrow and app name.

## User Story

As a **mobile user editing a workflow**,
I want the **Save** button to always be visible and tappable,
So that I can save my changes without switching to a desktop.

## Acceptance Criteria

1. On mobile viewport (≤ 767 px), the toolbar shows **Save** as the only always-visible button.
2. **Explain**, **JsonPath**, and **Run** are accessible via a single **`⋯` overflow dropdown** next to Save.
3. All three secondary actions (Explain, JsonPath, Run) remain fully functional from the dropdown.
4. Desktop layout (≥ 768 px) is **unchanged** — all four buttons remain visible in a row.
5. New E2E test: `Save` button is visible on Mobile Chrome canvas.
