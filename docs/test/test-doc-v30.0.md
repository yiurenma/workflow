# Test Document v30.0

**Document version:** 30.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-uat-e2e-retrofit-5layer-validation`

## Test Scope

Retrofit existing Playwright E2E test suite with five-layer validation framework.

## Five-Layer Validation Framework

| Layer | What | Playwright API |
|-------|------|----------------|
| 1. Exist | Element in DOM | `toBeAttached()` |
| 2. Size | Dimensions meet spec | `boundingBox()` |
| 3. Viewport | Visible in viewport | `toBeInViewport()` |
| 4. Interact | Responds to input | `click()`, `fill()` |
| 5. Effect | Styles + post-state | `evaluate()`, `toHaveScreenshot()` |

## Test Cases — Upgraded with Layer Annotations

### Mobile Apps (apps-mobile.spec.ts)

**TC-APP-MOB-01: Applications list loads on mobile**
- Layer 1: `.mobile-app-card` exists
- Layer 2: Card height ≥80px
- Layer 3: First card in viewport
- Layer 5: Card background color matches Carbon Gray 10

**TC-APP-MOB-05: Overflow menu opens (History)**
- Layer 1: Dropdown menu exists
- Layer 2: Menu height ≥100px
- Layer 3: Menu items in viewport
- Layer 4: Click "History" navigates

### Node Editor (node-editor.spec.ts)

**TC-NODE-ENHANCED-01: Node editor drawer opens on mobile**
- Layer 1: `.ant-drawer` exists
- Layer 2: Drawer height >35% viewport
- Layer 3: Drawer header in viewport
- Layer 5: Header text color matches Carbon Gray 100

**TC-NODE-ENHANCED-02: Node description field**
- Layer 1: Description textarea exists
- Layer 2: Textarea height ≥60px
- Layer 3: Textarea in viewport
- Layer 4: Can type and blur triggers save

### Canvas Mobile (canvas-mobile.spec.ts)

**TC-CANVAS-MOB-01: Add Node FAB visible**
- Layer 1: FAB button exists
- Layer 2: FAB size ≥56×56px
- Layer 3: FAB in viewport
- Layer 4: Tap opens Add Node sheet
- Layer 5: FAB background color matches Carbon Blue 60

## Test Execution Plan

### Phase 1: Upgrade Priority Specs
1. `apps-mobile.spec.ts` — 4 test cases
2. `node-editor.spec.ts` — 4 test cases
3. `canvas-mobile.spec.ts` — 3 test cases

Total: 11 test cases with layer 2-5 assertions

### Phase 2: Run Full Suite
```bash
cd workflow-ui
pnpm test:e2e
```

## Success Criteria

- ✅ 11 test cases with layer 2 assertions
- ✅ 5+ test cases with layer 5 assertions
- ✅ Zero regressions
- ✅ Suite runtime <5 minutes
