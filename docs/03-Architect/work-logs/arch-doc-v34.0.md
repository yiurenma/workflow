# Architecture Document v34.0

**Document Version:** 34.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Label:** `TODO-e2e-fix-test-failures-post-v30`

---

## 1. Test Failure Root Causes

### 1.1 Read-First Mode (6 failures)

v31.0 changed drawer to open in read-only NodeView component. Tests expect editable inputs immediately.

**Fix pattern:**
```typescript
await page.locator('.ant-drawer button:has-text("Edit")').click();
await page.waitForTimeout(300);
```

### 1.2 Table Viewport (1 failure)

Table not scrolled into view before assertion.

**Fix:**
```typescript
await table.scrollIntoViewIfNeeded();
```

### 1.3 FAB Size (1 failure)

Test expects 56px, Carbon standard is 48px.

**Fix:** Change expectation to 48px.

---

## 2. Implementation

**Files modified:**
- `e2e/drawer-close-v29.spec.ts` (2 tests)
- `e2e/node-editor-enhanced.spec.ts` (2 tests)
- `e2e/carbon-design-desktop.spec.ts` (1 test)
- `e2e/canvas-mobile.spec.ts` (1 test)

**No production code changes required.**
