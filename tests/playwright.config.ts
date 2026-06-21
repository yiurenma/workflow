import { defineConfig, devices } from '@playwright/test';

/**
 * 全量回归 + 可用性测试 —— 业界规范驱动、全黑盒、根仓库。
 * 规范栈见 TEST-STRATEGY.md。默认对本地栈跑，env 可切 UAT。
 *
 * 门禁：硬门禁测试标题含 @gate（CI 用 --grep @gate 阻断合并）；
 *       软提示含 @advisory（出报告、不阻断）。
 */
export const UI_BASE = process.env.UI_BASE ?? 'https://workflow-ui-gamma.vercel.app';
export const OPERATION_API_BASE =
  process.env.OPERATION_API_BASE ?? 'https://workflow-operation-api-n9sbp.ondigitalocean.app';
export const ONLINE_API_BASE =
  process.env.ONLINE_API_BASE ?? 'https://workflow-online-api-nr3e4.ondigitalocean.app';

const desktop = { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 1024 }, baseURL: UI_BASE };
const mobile = { ...devices['Pixel 5'], viewport: { width: 390, height: 844 }, baseURL: UI_BASE };

export default defineConfig({
  testDir: '.',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  // IGNORE_HTTPS_ERRORS=1 用于在做 TLS 拦截的出口代理后跑（如沙箱环境的 egress
  // gateway MITM CA），其证书系统信任但浏览器自带证书库不认。默认严格校验。
  use: {
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    ignoreHTTPSErrors: !!process.env.IGNORE_HTTPS_ERRORS,
  },
  projects: [
    // —— 功能/契约（无需浏览器，黑盒 API）——
    { name: 'api', testDir: './api', use: { baseURL: OPERATION_API_BASE } },
    { name: 'contract', testDir: './contract', use: { baseURL: OPERATION_API_BASE } },

    // —— E2E 功能（桌面 + 移动）——
    { name: 'desktop-chrome', testDir: './e2e', use: desktop },
    { name: 'mobile-chrome', testDir: './e2e', use: mobile },

    // —— 无障碍 / 可用性 / 性能 / 视觉（浏览器）——
    { name: 'a11y', testDir: './a11y', use: desktop },
    { name: 'a11y-mobile', testDir: './a11y', use: mobile },
    { name: 'ux', testDir: './ux', use: desktop },
    { name: 'ux-mobile', testDir: './ux', use: mobile },
    { name: 'perf', testDir: './perf', use: desktop },
    { name: 'visual', testDir: './visual', use: desktop },
  ],
});
