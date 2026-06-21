# Test Cases — Workflow Studio Unified Program Part 1: E2E Quality (v32.0)

**Document version:** 32.0 Part 1  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** E2E drawer close fix + 5-layer validation retrofit  
**Test environment:** UAT (https://workflow-ui-gamma.vercel.app)

---

## 1. Test Strategy

### 1.1 Scope

- **Drawer close fix:** Manual + Playwright validation on Desktop + Mobile
- **Five-layer retrofit:** Upgrade 10-15 existing test cases with Layer 2/3/5 assertions

### 1.2 Test Levels

- **Unit:** Not applicable (UI interaction defect)
- **Integration:** Not applicable
- **E2E:** Primary focus — Playwright tests on Desktop Chrome 1280px + Mobile Chrome 390×844

### 1.3 Test Data

- **Application:** `E2E_TEST_CANVAS` (existing test app with multiple node types)
- **Nodes:** HTTP fetch, Safe HTTP fetch, Logic nodes

---

## 2. Test Cases

### 2.1 Drawer Close Fix

#### TC-DRAWER-CLOSE-01: Desktop drawer close button

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Canvas loaded with at least one node

**Steps:**
1. Click on any node on canvas
2. Wait for node editor drawer to open
3. Verify drawer visible (Layer 1)
4. Click `.ant-drawer-close` button
5. Wait for drawer to close

**Expected Results:**
- **Layer 1 (Exist):** Drawer initially visible
- **Layer 4 (Interact):** Close button clickable
- **Layer 5 (Effect):** After close click:
  - Drawer removed from DOM OR `visibility: hidden`
  - Canvas interactive (can click nodes)
  - No console errors

**Playwright assertion example:**
```typescript
await page.locator('.react-flow__node').first().click();
await expect(page.locator('.ant-drawer')).toBeVisible();

await page.locator('.ant-drawer-close').click();
await expect(page.locator('.ant-drawer')).not.toBeVisible();

// Verify canvas interactive
await page.locator('.react-flow__node').nth(1).click();
await expect(page.locator('.ant-drawer')).toBeVisible(); // Can open again
```

---

#### TC-DRAWER-CLOSE-02: Mobile drawer close button

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Canvas loaded with at least one node

**Steps:**
1. Tap on any node on canvas
2. Wait for node editor drawer to open (bottom placement on mobile)
3. Verify drawer visible (Layer 1)
4. Verify drawer height >35% viewport (Layer 2)
5. Tap `.ant-drawer-close` button
6. Wait for drawer to close

**Expected Results:**
- **Layer 1 (Exist):** Drawer initially visible
- **Layer 2 (Size):** Drawer height >35% of 844px viewport (~295px minimum)
- **Layer 4 (Interact):** Close button tappable (44px touch target)
- **Layer 5 (Effect):** After close tap:
  - Drawer removed from DOM OR `visibility: hidden`
  - Canvas interactive (can tap nodes)
  - No console errors

**Playwright assertion example:**
```typescript
await page.locator('.react-flow__node').first().tap();
const drawer = page.locator('.ant-drawer');
await expect(drawer).toBeVisible();

const box = await drawer.boundingBox();
expect(box.height).toBeGreaterThan(844 * 0.35); // Layer 2

await page.locator('.ant-drawer-close').tap();
await expect(drawer).not.toBeVisible(); // Layer 5

// Verify canvas interactive
await page.locator('.react-flow__node').nth(1).tap();
await expect(drawer).toBeVisible(); // Can open again
```

---

#### TC-DRAWER-CLOSE-03: Close button works across node types

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Canvas has HTTP fetch, Safe HTTP fetch, and Logic nodes

**Steps:**
1. For each node type (HTTP fetch, Safe HTTP fetch, Logic):
   - Click/tap node
   - Verify drawer opens
   - Click/tap close button
   - Verify drawer closes

**Expected Results:**
- Close button works consistently across all node types
- No node type exhibits drawer close defect

---

### 2.2 Five-Layer Validation Retrofit

#### TC-NODE-ENHANCED-01: Node editor drawer 5-layer validation

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`
- Canvas loaded with at least one node

**Steps:**
1. Click/tap on node
2. Validate Layer 1: Drawer visible
3. Validate Layer 2: Drawer dimensions meet spec
4. Validate Layer 3: Drawer in viewport
5. Validate Layer 4: Drawer interactive (can type in fields)
6. Validate Layer 5: Drawer content loaded with correct styles

**Expected Results:**

**Layer 1 (Exist):**
- `.ant-drawer` present in DOM
- `role="dialog"` attribute present

**Layer 2 (Size):**
- Desktop: Drawer width 400-600px
- Mobile: Drawer height >35% viewport

**Layer 3 (Viewport):**
- Drawer fully visible in viewport
- No content clipped outside viewport

**Layer 4 (Interact):**
- Can type in "Node Description" field
- Can click "Rules" section to expand
- Can click "Action" section to expand

**Layer 5 (Effect):**
- Header text "NODE CONFIGURATION" visible with correct color (text-zinc-600, rgb(82, 82, 91))
- Description field has correct placeholder
- Computed styles match Carbon tokens (if applicable)

**Playwright assertion example:**
```typescript
await page.locator('.react-flow__node').first().click();
const drawer = page.locator('.ant-drawer');

// Layer 1: Exist
await expect(drawer).toBeVisible();
await expect(drawer).toHaveAttribute('role', 'dialog');

// Layer 2: Size
const box = await drawer.boundingBox();
if (isMobile) {
  expect(box.height).toBeGreaterThan(viewport.height * 0.35);
} else {
  expect(box.width).toBeGreaterThanOrEqual(400);
  expect(box.width).toBeLessThanOrEqual(600);
}

// Layer 3: Viewport
await expect(drawer).toBeInViewport();

// Layer 4: Interact
const descField = drawer.locator('textarea[placeholder*="description"]');
await descField.fill('Test description');
await expect(descField).toHaveValue('Test description');

// Layer 5: Effect
const header = drawer.locator('text=NODE CONFIGURATION');
await expect(header).toBeVisible();
const headerColor = await header.evaluate(el => getComputedStyle(el).color);
expect(headerColor).toBe('rgb(82, 82, 91)'); // text-zinc-600
```

---

#### TC-APP-MOB-ENHANCED-01: Mobile app overflow menu 5-layer validation

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Mobile Chrome 390×844

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows`
- At least one application in list

**Steps:**
1. Tap ⋯ button on application card
2. Validate Layer 1: Dropdown menu visible
3. Validate Layer 2: Menu items meet touch target size (44px)
4. Validate Layer 3: Menu in viewport
5. Validate Layer 4: Can tap "History" option
6. Validate Layer 5: History modal opens with correct content

**Expected Results:**

**Layer 1 (Exist):**
- `.ant-dropdown-menu` visible

**Layer 2 (Size):**
- Each menu item height ≥44px (touch target)

**Layer 3 (Viewport):**
- Menu fully visible in viewport
- Not clipped at bottom edge

**Layer 4 (Interact):**
- Can tap "History" option
- Can tap "Copy" option
- Can tap "Delete" option

**Layer 5 (Effect):**
- Tapping "History" opens modal with `role="dialog"`
- Modal shows configuration history list
- Modal has correct header text

---

#### TC-JSONPATH-ENHANCED-01: JsonPath modal 5-layer validation

**Priority:** MEDIUM  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px

**Preconditions:**
- Navigate to `https://workflow-ui-gamma.vercel.app/workflows/E2E_TEST_CANVAS`

**Steps:**
1. Click "JsonPath" button in toolbar
2. Validate Layer 1: Modal visible
3. Validate Layer 2: Modal dimensions appropriate
4. Validate Layer 3: Modal in viewport
5. Validate Layer 4: Can type in path and JSON fields
6. Validate Layer 5: Validation result displays correctly

**Expected Results:**

**Layer 1 (Exist):**
- `.jsonpath-modal` visible
- `role="dialog"` attribute present

**Layer 2 (Size):**
- Modal width 600-800px
- Modal height fits content

**Layer 3 (Viewport):**
- Modal centered in viewport
- Not clipped at edges

**Layer 4 (Interact):**
- Can type JSONPath expression in path field
- Can type JSON in data field
- Can click "Validate" button

**Layer 5 (Effect):**
- Valid path shows green success message
- Invalid path shows red error message
- Result preview displays extracted value

---

### 2.3 Regression Tests

#### TC-REGRESSION-01: Existing E2E suite still passes

**Priority:** HIGH  
**Type:** E2E (Playwright)  
**Viewport:** Desktop Chrome 1280px + Mobile Chrome 390×844

**Preconditions:**
- All code changes merged

**Steps:**
1. Run full Playwright suite: `npx playwright test`
2. Review results

**Expected Results:**
- Pass rate ≥82/84 (baseline from v28.0)
- No new failures introduced by drawer close fix or 5-layer retrofit
- TC-NODE-ENHANCED-05 (drawer close) now passes (was failing before)

---

## 3. Test Execution Plan

### 3.1 Phase 1: Drawer Close Fix

1. Manual test TC-DRAWER-CLOSE-01 on Desktop
2. Manual test TC-DRAWER-CLOSE-02 on Mobile
3. Implement Playwright tests TC-DRAWER-CLOSE-01/02/03
4. Run full E2E suite; verify no regressions

### 3.2 Phase 2: Five-Layer Retrofit

1. Upgrade TC-NODE-ENHANCED-01 with 5-layer assertions
2. Upgrade TC-APP-MOB-ENHANCED-01 with 5-layer assertions
3. Upgrade TC-JSONPATH-ENHANCED-01 with 5-layer assertions
4. Upgrade 7-10 additional existing test cases (TBD based on audit)
5. Run full suite; document results in `e2e-test-report-v32.0-part1.md`

---

## 4. Exit Criteria

- TC-DRAWER-CLOSE-01/02/03 pass on Desktop + Mobile
- At least 10 test cases upgraded with 5-layer assertions
- Full E2E suite pass rate ≥82/84 (no regressions)
- `e2e-test-report-v32.0-part1.md` documents coverage and gaps

---

## 5. References

- `specs/uat-report-e2e-pass3.md` — UAT findings
- `docs/e2e-testing-guide.md` — E2E conventions
- `docs/test-doc-v28.0.md` — Current E2E baseline
