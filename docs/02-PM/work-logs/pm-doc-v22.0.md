# PM Doc v22.0 — Mobile: Node Editor Blank Space Below Content

**Status:** Awaiting approval
**Label:** `TODO-mobile-node-editor-blank-space-below`
**Date:** 2026-04-08

---

## Problem Statement

On iPhone X-class viewports, after tapping a node on the canvas, the node editor drawer opens with ~80% blank space below the actual form fields. Users must scroll down to reach content that should be immediately visible. Reported frequency: ~80% of opens on mobile.

## User Story

As a **mobile user editing a workflow node**,
I want the node editor drawer to show content immediately at the top without a large empty region,
So that I can edit node properties without unnecessary scrolling.

## Acceptance Criteria

1. When the node editor opens on mobile (viewport width < 768 px), the drawer body shows form content starting at the top — no blank region larger than standard padding.
2. The fix applies to **iPhone X-class** viewports (375–390 px wide, ~812–844 px tall with Safari chrome).
3. The drawer remains scrollable when content exceeds the available height (e.g. many rule rows).
4. Desktop drawer behavior (420 px wide, right-placed) is **unchanged**.
5. The fix must account for iOS safe-area insets (`env(safe-area-inset-bottom)`) so the bottom of the drawer is not obscured by the home indicator.
