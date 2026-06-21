# UI Test Report v7.0 — Q2 Mobile: App Card Actions + Desktop View Entry

**Feature:** `TODO-q2-mobile-apps-actions-desktop-entry-constraints`  
**Date:** 2026-04-07  
**Tester:** QA Agent

---

## Source Checks

| TC-ID | Check | Result |
|-------|-------|--------|
| TC-37-L01 | `EllipsisOutlined` Dropdown on mobile cards | ✅ PASS — `<Dropdown menu={cardMenu(record)}>` with `<EllipsisOutlined />` in card action area |
| TC-37-L02 | Dropdown items: Copy, History, Delete | ✅ PASS — `items` array has keys `copy`, `history`, `delete` |
| TC-37-L03 | Copy calls `setCopySource` | ✅ PASS |
| TC-37-L04 | Delete calls `confirmDelete` (Modal.confirm) | ✅ PASS — `confirmDelete` helper extracted and reused by both desktop and mobile |
| TC-37-L05 | `desktopOverride` state + `DESKTOP_VIEW_KEY` localStorage | ✅ PASS — `useState(loadDesktopOverride)` + `localStorage.setItem(DESKTOP_VIEW_KEY, ...)` |
| TC-37-L06 | "Desktop view" / "Mobile view" toggle button inside `{isMobile && ...}` context | ✅ PASS — toggle rendered conditionally inside both layout branches |
| TC-37-L07 | `showDesktop = !isMobile \|\| desktopOverride` | ✅ PASS |
| TC-37-L08 | Layout branches use `!showDesktop` | ✅ PASS — all `isMobile ?` replaced with `!showDesktop ?` |
| TC-37-L09 | Canvas file not modified | ✅ PASS |

**Source: 9/9 PASS**

---

## Browser UAT — pending human execution

*Script in test-doc-v7.0.md*

---
