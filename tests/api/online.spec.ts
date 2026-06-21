import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { ONLINE_API_BASE } from '../playwright.config';

/**
 * REC / execution-plane (online-api) — encodes pm-doc-master v2.29 REC-US-12~16.
 * Verdicts feed audit §4.
 *
 * NOTE (verified locally 2026-06-21): the required correlation header is
 * `X-Request-Correlation-Id` (NOT `x-request-id` as an early audit pass assumed).
 * Full execution + idempotency need the real JKS keystore secret (SecureData);
 * the repo default `changeit` is a placeholder, so those paths are gated behind
 * ONLINE_WRITE=1 and only pass where the keystore password is provided.
 */
const WRITE = process.env.ONLINE_WRITE === '1';
const APP_A = process.env.APP_A ?? 'TEST_APP_A';
const CORR_HEADER = 'X-Request-Correlation-Id';

test.describe('REC-US-12 single-entry execution', () => {
  test('REC-AC-12-3: unknown application is rejected with classifiable M0001', async ({ request }) => {
    const res = await request.post(
      `${ONLINE_API_BASE}/api/workflow?applicationName=DOES_NOT_EXIST_${Date.now()}&confirmationNumber=${randomUUID()}`,
      { data: { data: { amount: 1 } }, headers: { 'Content-Type': 'application/json', [CORR_HEADER]: randomUUID() } },
    );
    expect(res.status(), 'unknown app should not 2xx').toBe(400);
    expect(JSON.stringify(await res.json().catch(() => ({}))), 'classifiable failure code').toContain('M0001');
  });

  test('REC-AC-12-1: missing correlation header is rejected', async ({ request }) => {
    const res = await request.post(
      `${ONLINE_API_BASE}/api/workflow?applicationName=${encodeURIComponent(APP_A)}&confirmationNumber=${randomUUID()}`,
      { data: { data: { amount: 1 } }, headers: { 'Content-Type': 'application/json' } },
    );
    expect(res.status()).toBe(400);
  });
});

test.describe('REC-US-15 idempotency (TC-16) — needs keystore secret', () => {
  test.skip(!WRITE, 'set ONLINE_WRITE=1 + a valid JKS keystore password to exercise live execution');
  test('REC-AC-15-2: duplicate correlation id → M0002 duplicate rejection', async ({ request }) => {
    const reqId = randomUUID();
    const conf = randomUUID();
    const url = `${ONLINE_API_BASE}/api/workflow?applicationName=${encodeURIComponent(APP_A)}&confirmationNumber=${conf}`;
    const headers = { 'Content-Type': 'application/json', [CORR_HEADER]: reqId };
    const first = await request.post(url, { data: { data: { amount: 1 } }, headers });
    expect(first.status(), 'first execution succeeds (requires working keystore)').toBe(200);
    const second = await request.post(url, { data: { data: { amount: 1 } }, headers });
    expect(second.status()).toBe(400);
    expect(JSON.stringify(await second.json().catch(() => ({})))).toContain('M0002');
  });
});

/**
 * GAP-1 — TODO-online-api-post-optional-sse-runtime-per-step.
 * Audit confirmed NO SSE in online-api. This test documents the unmet spec (TDD):
 * it asserts the opt-in SSE behavior the doc requires. Expected unsupported until
 * the feature ships — that failure IS the tracked gap.
 */
test.describe('GAP-1 optional SSE per-step runtime (Open TODO)', () => {
  test('REC: Accept: text/event-stream should stream per-step runtime once implemented', async ({ request }) => {
    test.fixme(true, 'SSE not implemented (audit GAP-1); enable when TODO-online-api-post-optional-sse-runtime-per-step ships');
    const res = await request.post(
      `${ONLINE_API_BASE}/api/workflow?applicationName=${encodeURIComponent(APP_A)}&confirmationNumber=${randomUUID()}`,
      {
        data: { data: { amount: 1 } },
        headers: { 'Content-Type': 'application/json', [CORR_HEADER]: randomUUID(), Accept: 'text/event-stream' },
      },
    );
    expect(res.headers()['content-type']).toContain('text/event-stream');
  });
});
