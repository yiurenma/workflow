# Product Requirements Document v31.0

**Document version:** 31.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-node-editor-draggable-width-readonly-default`

## Overview

Enhance node editor drawer with resizable width, read-first default mode, and explicit edit gate to improve content readability and user workflow.

## Problem Statement

Current node editor drawer has fixed width and immediately shows editable form fields, making it difficult to:
- Read long content (descriptions, JSON rules, action configs) without scrolling
- Quickly scan node configuration without accidentally editing
- Adjust drawer width for different content lengths

## User Stories

**US-31.1:** As a workflow editor user, I want to resize the node editor drawer by dragging its edge, so that I can adjust the width to fit my content and screen size.

**US-31.2:** As a workflow editor user, I want the node editor to open in read-only mode by default, so that I can quickly scan the full content without accidentally editing.

**US-31.3:** As a workflow editor user, I want an explicit "Edit" button to switch to edit mode, so that I have clear control over when I'm viewing vs editing.

**US-31.4:** As a workflow editor user, I want long content to reflow when I resize the drawer, so that I can read everything without horizontal scrolling.

## Acceptance Criteria

**AC-31.1:** Resizable drawer
- User can drag the inner vertical edge (left edge on desktop) to widen/narrow the drawer
- Minimum width: 320px (mobile-friendly)
- Maximum width: 80% viewport width
- Width persists in localStorage (optional)
- Mobile: touch drag handle OR documented deferral

**AC-31.2:** Read-first default mode
- On first open after selecting a node, drawer shows read-only view
- Read-only view displays formatted text and pretty-printed JSON
- No input boxes or textareas visible in read-only mode

**AC-31.3:** Edit gate
- Prominent "Edit" button at top of drawer in read-only mode
- Clicking "Edit" switches to existing form mode
- "Done" or "Cancel" button in edit mode
- Cancel discards unsaved changes (or keeps local state per Arch decision)

**AC-31.4:** Long content handling
- Inner drawer body scrolls vertically
- Content reflows horizontally when drawer width changes
- No silent truncation of long text
- Virtualization or "Show more" only if performance requires

## Scope

**In scope:**
- Resizable drawer width (desktop priority, mobile optional)
- Read-only default view with formatted content
- Edit/Done/Cancel controls
- Content reflow on resize

**Out of scope:**
- Changing drawer placement (still right-side on desktop, bottom on mobile)
- Adding new fields or validation
- Backend API changes

## References

- TODO.md §5: Node editor width/read-first mode/edit gate
- Prior node editor work: v29.0 (drawer close button fix)

## Success Metrics

- Users can resize drawer without errors
- Read-only mode displays all content clearly
- Edit mode preserves existing functionality
- No regressions in existing node editor tests
