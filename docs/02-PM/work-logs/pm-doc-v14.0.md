# PM Doc v14.0 — Mobile Records: Pagination visible on narrow viewport

**Status:** Awaiting approval
**Label:** `TODO-mobile-records-pagination-invisible`
**Date:** 2026-04-08

## Problem Statement

On the Records list page in mobile viewport (< 768 px), the Ant Design `<Pagination>` component (`.ant-pagination`) is not rendered. Instead a custom Prev/Next button pair is shown. The custom component is functional but inconsistent with the rest of the app and less accessible (no page number display, no jump-to-page).

## User Story

As a **mobile user** browsing records,
I want visible, functional pagination controls,
So that I can navigate between result pages without confusion.

## Acceptance Criteria

1. On mobile viewport (< 768 px), the Records list shows a `<Pagination>` component (Ant Design) that is visible and functional.
2. It shows at minimum: current page, total pages or total items, Previous / Next navigation.
3. The component is `size="small"` for compact display.
4. Desktop behavior is unchanged.
5. TC-REC-03 passes on both Desktop Chrome and Mobile Chrome using the standard `.ant-pagination` selector.
