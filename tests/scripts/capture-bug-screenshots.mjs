// Reproducible bug-evidence capture for the doc gaps (docs/TODO-doc-gaps.md §F).
// Each shot is named after the gap label so report ↔ screenshot ↔ TODO line up.
//
// Run:
//   export PLAYWRIGHT_BROWSERS_PATH=/opt/pw-browsers
//   node tests/scripts/capture-bug-screenshots.mjs
//
// Needs egress to UAT. Creates a throwaway app via operation-api, uses it as the
// canvas seed target, deletes it at the end. TLS-intercepting egress → ignoreHTTPSErrors.
import { chromium, devices } from '@playwright/test';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const UI = process.env.UI_BASE ?? 'https://workflow-ui-gamma.vercel.app';
const OP = process.env.OPERATION_API_BASE ?? 'https://workflow-operation-api-n9sbp.ondigitalocean.app';
const OUT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../docs/reports/uat/screenshots/v45');

const WF = {
  pluginList: [
    { id: 1, description: 'Fetch profile', linkingIdOfRuleListAndAction: 'r1', ruleList: [{ key: '$.customerId', remark: 'has id' }], action: { type: 'CONSUMER', provider: 'svc', httpRequestMethod: 'GET', httpRequestUrlWithQueryParameter: 'https://example.com/c?id={{customerId}}' }, uiMap: { id: 'CONSUMER_1', type: 'CONSUMER', position: { x: 120, y: 80 }, measured: { width: 160, height: 40 } } },
    { id: 2, description: 'High value?', linkingIdOfRuleListAndAction: 'r2', ruleList: [{ key: '$.amount', remark: 'has amount' }], action: { type: 'IFELSE', elseLogic: 'e30=' }, uiMap: { id: 'IFELSE_2', type: 'IFELSE', position: { x: 120, y: 220 }, measured: { width: 160, height: 40 } } },
  ],
  uiMapList: [{ id: 'e1', source: 'CONSUMER_1', target: 'IFELSE_2' }],
};

async function api(method, ep, body) {
  return fetch(`${OP}${ep}`, { method, headers: { 'Content-Type': 'application/json' }, body: body ? JSON.stringify(body) : undefined });
}
async function openCanvas(page, app) {
  page.on('dialog', (d) => d.accept().catch(() => {}));
  await page.goto(`${UI}/workflows/${app}`);
  await page.waitForLoadState('networkidle');
  await page.locator('.react-flow, [data-testid="rf__wrapper"]').first().waitFor({ state: 'visible', timeout: 15000 });
}
async function seed(page) {
  const btn = page.getByRole('button', { name: /import/i }).first();
  await btn.click();
  const ta = page.locator('.modal-box textarea').first();
  await ta.fill(JSON.stringify(WF));
  await page.getByRole('button', { name: /apply to canvas/i }).click();
  await page.locator('.react-flow__node').first().waitFor({ state: 'visible', timeout: 8000 }).catch(() => {});
}

const app = `CANVAS_SHOT_${Date.now()}`;
const shots = [];
async function shot(page, name, caption) { const f = path.join(OUT, name); await page.screenshot({ path: f }); shots.push({ name, caption }); console.log('  ✓', name); }

(async () => {
  console.log('create temp app', app, (await api('POST', `/api/workflow?applicationName=${app}`, { pluginList: [], uiMapList: [] })).status);
  const browser = await chromium.launch();

  // ── desktop ──
  const dctx = await browser.newContext({ ...devices['Desktop Chrome'], viewport: { width: 1280, height: 1024 }, ignoreHTTPSErrors: true });
  const dp = await dctx.newPage();

  // F: TODO-ui-a11y-records-form-labels — /records filter controls without labels
  await dp.goto(`${UI}/records`); await dp.waitForLoadState('networkidle');
  await shot(dp, 'records-a11y-form-labels.png', '/records 筛选输入/下拉无 <label>/aria-label（axe: label×2 + select-name×1）');

  // F: TODO-ui-a11y-canvas-color-contrast — canvas text contrast
  await openCanvas(dp, app); await seed(dp);
  await shot(dp, 'canvas-color-contrast.png', '画布文字/背景对比度 < 4.5:1（axe color-contrast×6）');

  // F: TODO-ui-modal-esc-close-wai-aria — Import modal has ×/Cancel but no Esc close
  await dp.getByRole('button', { name: /import/i }).first().click();
  await dp.locator('.modal-box').first().waitFor({ state: 'visible', timeout: 5000 }).catch(() => {});
  await shot(dp, 'modal-no-esc-close.png', 'Import 等模态有 ×/Cancel，但按 Esc 不关闭（违反 WAI-ARIA 对话框模式）');
  await dp.keyboard.press('Escape').catch(() => {});

  // F: TODO-ui-drawer-done-disable-on-rule-error — invalid JSONPath shows error but Done stays enabled
  await openCanvas(dp, app); await seed(dp);
  await dp.locator('.react-flow__node').first().click();
  const drawer = dp.locator('.drawer-panel, [class*="drawer"]').first();
  await drawer.getByRole('button', { name: /^edit$/i }).first().click().catch(() => {});
  // rule-key field is placeholder "$." (see E1); comma → two expressions → inline error on blur
  const keyField = drawer.getByPlaceholder(/^\$\./).first();
  await keyField.fill('a, b');
  await keyField.blur();
  await drawer.getByText(/single JSONPath expression/i).first().waitFor({ state: 'visible', timeout: 5000 }).catch(() => {});
  await dp.waitForTimeout(300);
  await shot(dp, 'drawer-done-not-disabled-on-rule-error.png', '规则键填非法 JSONPath「a, b」→ 显示内联红错「single JSONPath expression」，但 Done 按钮仍可点（CV-AC-50-4 要求禁用）');
  await dctx.close();

  // ── mobile ──
  const mctx = await browser.newContext({ ...devices['Pixel 5'], viewport: { width: 390, height: 844 }, ignoreHTTPSErrors: true });
  const mp = await mctx.newPage();

  // F: TODO-ui-a11y-mobile-nav-contrast — mobile tab bar active link contrast
  await mp.goto(`${UI}/workflows`); await mp.waitForLoadState('networkidle');
  await shot(mp, 'mobile-nav-contrast.png', '移动端 tab bar 激活态链接文字对比度 < 4.5:1（跨 /workflows、/、/records）');

  // F: TODO-tests-canvas-mobile-and-uat-flows — Run lives in mobile overflow menu
  await openCanvas(mp, app);
  const overflow = mp.getByRole('button', { name: /more|menu|⋮|⋯|actions/i }).first();
  if (await overflow.count()) await overflow.click().catch(() => {});
  await mp.waitForTimeout(400);
  await shot(mp, 'mobile-run-in-overflow.png', '移动端 Run/Import 等工具栏按钮在溢出菜单内（@uat G2 未先开菜单 → 超时失败）');
  await mctx.close();

  await browser.close();
  console.log('delete temp app', (await api('DELETE', `/api/workflow?applicationName=${app}`)).status);
  console.log(`\n${shots.length} screenshots → ${OUT}`);
  shots.forEach((s) => console.log(`  - ${s.name}: ${s.caption}`));
})().catch(async (e) => { console.error(e); await api('DELETE', `/api/workflow?applicationName=${app}`); process.exit(1); });
