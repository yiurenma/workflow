# Architecture Document v30.0

**Document version:** 30.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-uat-e2e-retrofit-5layer-validation`

## Overview

Technical approach for retrofitting Playwright E2E test suite with five-layer validation framework to catch UX regressions that simple visibility checks miss.

## Five-Layer Validation Framework

### Layer 1: Exist (Current baseline)
**What:** Element is present in DOM  
**Playwright API:** `locator.count()`, `expect(locator).toBeAttached()`  
**Example:**
```typescript
const drawer = page.locator('.ant-drawer');
await expect(drawer).toBeAttached();
```

### Layer 2: Size (NEW)
**What:** Element dimensions meet specification  
**Playwright API:** `locator.boundingBox()`, custom assertions  
**Example:**
```typescript
const drawer = page.locator('.ant-drawer');
const box = await drawer.boundingBox();
const viewportHeight = page.viewportSize()!.height;
expect(box!.height).toBeGreaterThan(viewportHeight * 0.35);
```

**Priority targets:**
- Mobile drawers: node editor (>35% viewport height)
- Mobile modals: Delete confirm, Explain, Generate (>30% viewport height)
- Form inputs: minimum touch target 44×44px on mobile

### Layer 3: Viewport (NEW)
**What:** Element is visible within viewport (not clipped/scrolled out)  
**Playwright API:** `expect(locator).toBeInViewport()`  
**Example:**
```typescript
const submitButton = page.getByRole('button', { name: 'Submit' });
await expect(submitButton).toBeInViewport();
```

### Layer 4: Interact (Partial coverage exists)
**What:** Element responds to user interaction  
**Playwright API:** Real `click()`, `fill()`, `press()`  

### Layer 5: Effect (NEW — highest value)
**What:** Post-interaction state + computed styles match spec  
**Playwright API:** `evaluate()` for computed styles, `toHaveScreenshot()` for visual regression  
**Example:**
```typescript
const color = await element.evaluate(el => 
  window.getComputedStyle(el).color
);
expect(color).toBe('rgb(15, 98, 254)');
```

## Implementation Strategy

### Phase 1: Upgrade existing specs
Target files:
1. `apps-mobile.spec.ts` — add layer 2 (card height, overflow menu)
2. `node-editor.spec.ts` — add layer 2 (drawer height), layer 5 (form styles)
3. `canvas-mobile.spec.ts` — add layer 2 (Add Node sheet), layer 3 (FAB)

### Phase 2: Run full suite and document
- Generate `e2e-test-report-v30.0.md` with layer coverage summary
- Include representative examples

## Success Criteria

- At least 10 test cases with layer 2 assertions
- At least 5 test cases with layer 5 assertions
- Zero regression in existing pass rate
- Suite runtime <5 minutes
