import { test, expect } from '@playwright/test';

/**
 * APP-US-01/02 via the UI, 5-layer UX framework (CV-US-38):
 * Layer 1 exist · 2 size · 3 viewport · 4 interact · 5 effect (computed style).
 * Carbon selectors (.cds-*); viewport-aware (desktop "＋ New application" button is
 * .hide-mobile, mobile uses a FAB with aria-label "New application").
 */
test.describe('APP-US-01 applications list', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/workflows');
    await page.waitForLoadState('networkidle');
  });

  test('Layer 1 exist: list + search + a visible create affordance', async ({ page }) => {
    await expect(page.locator('table:visible, .cds-table:visible, .app-row:visible, [class*="card"]:visible').first()).toBeVisible();
    await expect(page.getByPlaceholder(/search/i).or(page.locator('input:visible').first())).toBeVisible();
    // desktop button OR mobile FAB — whichever is visible at this viewport
    const create = page.locator('button:visible').filter({ hasText: /New application|＋/ }).first();
    await expect(create).toBeVisible();
  });

  test('Layer 3 viewport: content not clipped horizontally', async ({ page }) => {
    const overflow = await page.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
    );
    expect(overflow, 'no horizontal clipping').toBeLessThanOrEqual(2);
  });

  test('Layer 4 interact: search input accepts text', async ({ page }) => {
    const search = page.getByPlaceholder(/search/i).or(page.locator('input:visible').first());
    await search.fill('TEST');
    await expect(search).toHaveValue('TEST');
  });
});

test.describe('CV-US-36 Carbon design tokens (Layer 5 effect)', () => {
  test('nav background rectangular (0px radius — Carbon hard constraint)', async ({ page }) => {
    await page.goto('/workflows');
    const nav = page.locator('header, nav, [class*="header"]').first();
    await expect(nav).toBeVisible();
    const radius = await nav.evaluate((el) => getComputedStyle(el).borderRadius);
    expect(radius === '0px' || radius === '').toBeTruthy();
  });
});
