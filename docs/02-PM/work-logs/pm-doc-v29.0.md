# PM Document v29.0 — Node Editor Drawer Close Button Fix

**Document version:** 29.0  
**Date:** 2026-04-12  
**Status:** Draft  
**TODO label:** `TODO-uat-e2e-node-drawer-close-button-broken`

## Problem Statement

Users cannot close the node editor drawer by clicking the close button (`.ant-drawer-close`). The drawer remains open after clicking, forcing users to click outside the drawer or press ESC to dismiss it. On mobile devices where ESC is not available, this creates a poor user experience.

## User Story

**US-29.1:** As a workflow editor user, I want to close the node editor drawer by clicking the close button, so that I can quickly return to the canvas without needing to click outside or use keyboard shortcuts.

## Acceptance Criteria

**AC-29.1:** When a user clicks the drawer close button (`.ant-drawer-close`), the drawer closes immediately and focus returns to the canvas.

**AC-29.2:** The close button works consistently on both Desktop Chrome (1280px) and Mobile Chrome (390×844).

**AC-29.3:** After closing via the close button, the canvas remains interactive and the drawer is no longer visible in the DOM or is hidden.

## Scope

- **In scope:** Fix the drawer close button click handler in `workflow-ui`
- **Out of scope:** Other drawer behaviors (ESC key, click outside, swipe gestures)

## Evidence

- Test case TC-NODE-ENHANCED-05 (Layer 5) fails in `specs/uat-report-e2e-pass3.md`
- Symptom: clicking `.ant-drawer-close` does not trigger drawer dismissal

## Investigation Hints

Potential root causes:
- Event handler not bound to close button
- Event propagation stopped before reaching handler
- Ant Design `destroyOnClose` / `open` state mismatch
- Z-index or portal overlay blocking hit target
- ReactFlow capturing pointer events
