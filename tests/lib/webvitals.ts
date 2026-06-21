import { expect, type Page } from '@playwright/test';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

// 官方 web-vitals IIFE 构建（exports 字段限制 require.resolve，故按相对路径直取 dist）
const WEB_VITALS_IIFE = resolve(
  dirname(fileURLToPath(import.meta.url)),
  '../node_modules/web-vitals/dist/web-vitals.iife.js',
);

/** Google Core Web Vitals "good" 阈值。 */
export const CWV_THRESHOLDS = { LCP: 2500, CLS: 0.1, INP: 200 } as const;

export type WebVitals = Partial<Record<'LCP' | 'CLS' | 'INP', number>>;

/**
 * 采集 Core Web Vitals（性能体验硬门禁）。
 * 用 reportAllChanges=true 持续上报，避免依赖 visibilitychange flush。
 * INP 需要交互才会产生，故先做一次点击/移动触发。
 */
export async function collectWebVitals(page: Page, settleMs = 2500): Promise<WebVitals> {
  await page.addScriptTag({ path: WEB_VITALS_IIFE });
  await page.evaluate(() => {
    (window as Window & { __cwv?: WebVitals }).__cwv = {};
    const wv = (window as unknown as { webVitals?: any }).webVitals;
    if (!wv) return;
    const store = (window as Window & { __cwv?: any }).__cwv;
    const opt = { reportAllChanges: true };
    wv.onLCP((m: any) => (store.LCP = m.value), opt);
    wv.onCLS((m: any) => (store.CLS = m.value), opt);
    wv.onINP((m: any) => (store.INP = m.value), opt);
  });
  // 触发交互以产出 INP，并让 LCP/CLS 收敛
  await page.mouse.move(20, 20);
  await page.mouse.click(20, 20).catch(() => {});
  await page.waitForTimeout(settleMs);
  return await page.evaluate(() => (window as Window & { __cwv?: WebVitals }).__cwv || {});
}

/**
 * 断言已采集到的 CWV 在预算内（只对采到的指标断言；INP 在无交互页面可能缺省）。
 */
export async function assertWebVitalsWithinBudget(page: Page): Promise<WebVitals> {
  const v = await collectWebVitals(page);
  if (v.LCP !== undefined) expect(v.LCP, `LCP=${Math.round(v.LCP)}ms (预算 ≤${CWV_THRESHOLDS.LCP})`).toBeLessThanOrEqual(CWV_THRESHOLDS.LCP);
  if (v.CLS !== undefined) expect(v.CLS, `CLS=${v.CLS.toFixed(3)} (预算 ≤${CWV_THRESHOLDS.CLS})`).toBeLessThanOrEqual(CWV_THRESHOLDS.CLS);
  if (v.INP !== undefined) expect(v.INP, `INP=${Math.round(v.INP)}ms (预算 ≤${CWV_THRESHOLDS.INP})`).toBeLessThanOrEqual(CWV_THRESHOLDS.INP);
  return v;
}
