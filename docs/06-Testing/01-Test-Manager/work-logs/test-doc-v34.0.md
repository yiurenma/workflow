# Test Document v34.0

**Document Version:** 34.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Label:** `TODO-e2e-fix-test-failures-post-v30`

---

## Test Cases

### TC-E2E-FIX-01: Node Editor Read-First Mode Tests

**Objective:** Verify test specs correctly handle v31.0 read-first mode.

**Steps:**
1. Run TC-DRAWER-CLOSE-08, TC-DRAWER-CLOSE-10
2. Run TC-NODE-ENHANCED-01, TC-NODE-ENHANCED-04
3. Verify all 4 tests pass

**Expected:** All tests click Edit button before asserting editability.

### TC-E2E-FIX-02: Table Viewport Test

**Objective:** Verify table viewport assertion works.

**Steps:**
1. Run TC-CARBON-DESK-06
2. Verify test passes

**Expected:** Table scrolled into view before assertion.

### TC-E2E-FIX-03: FAB Size Test

**Objective:** Verify FAB size expectation matches Carbon standard.

**Steps:**
1. Run TC-CANVAS-MOB-01
2. Verify test passes

**Expected:** FAB size ≥48px (not ≥56px).

### TC-E2E-FIX-04: Full Suite Pass

**Objective:** Verify all 107 tests pass.

**Steps:**
1. Run `npx playwright test`
2. Check HTML report

**Expected:** 107/107 pass, 0 flaky.
