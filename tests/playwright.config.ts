import { defineConfig, devices } from '@playwright/test';

/**
 * Test-driven documentation audit — Workflow platform.
 *
 * Targets the UAT environment (CLAUDE.md → UAT Environment). Override via env vars.
 * NOTE: if the sandbox egress policy blocks these hosts, the suite cannot run here
 * (see docs/doc-implementation-audit-v1.0.md §0). Authoring is complete; runnable
 * wherever the three UAT hosts are reachable.
 */
export const UI_BASE = process.env.UI_BASE ?? 'https://workflow-ui-gamma.vercel.app';
export const OPERATION_API_BASE =
  process.env.OPERATION_API_BASE ?? 'https://workflow-operation-api-n9sbp.ondigitalocean.app';
export const ONLINE_API_BASE =
  process.env.ONLINE_API_BASE ?? 'https://workflow-online-api-nr3e4.ondigitalocean.app';

export default defineConfig({
  testDir: '.',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },
  projects: [
    {
      name: 'api',
      testDir: './api',
      use: { baseURL: OPERATION_API_BASE },
    },
    {
      name: 'desktop-chrome',
      testDir: './e2e',
      use: { ...devices['Desktop Chrome'], viewport: { width: 1280, height: 1024 }, baseURL: UI_BASE },
    },
    {
      name: 'mobile-chrome',
      testDir: './e2e',
      use: { ...devices['Pixel 5'], viewport: { width: 390, height: 844 }, baseURL: UI_BASE },
    },
  ],
});
