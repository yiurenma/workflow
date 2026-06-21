import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { ONLINE_API_BASE } from '../playwright.config';
import { loadOpenApi, assertPathDocumented, assertStatusDocumented, documentedStatuses } from '../lib/contract';

/**
 * @gate 契约硬门禁 — online-api（执行面）。
 * 必需头 X-Request-Correlation-Id（见 DIV-3）。
 */
test.describe('@gate online-api OpenAPI 契约', () => {
  test('Given OpenAPI doc, Then POST /api/workflow 已声明且含 200/400/500', async ({ request }) => {
    const doc = await loadOpenApi(request, ONLINE_API_BASE);
    assertPathDocumented(doc, 'post', '/api/workflow');
    const statuses = documentedStatuses(doc, 'post', '/api/workflow');
    for (const s of ['200', '400', '500']) expect(statuses, `声明含 ${s}`).toContain(s);
  });

  test('When 调用未知应用, Then 400(M0001) 且状态在声明内', async ({ request }) => {
    const doc = await loadOpenApi(request, ONLINE_API_BASE);
    const res = await request.post(
      `${ONLINE_API_BASE}/api/workflow?applicationName=NOPE_${Date.now()}&confirmationNumber=${randomUUID()}`,
      { headers: { 'Content-Type': 'application/json', 'X-Request-Correlation-Id': randomUUID() }, data: { data: { amount: 1 } } },
    );
    expect(res.status()).toBe(400);
    assertStatusDocumented(doc, 'post', '/api/workflow', 400);
    expect(JSON.stringify(await res.json().catch(() => ({}))), '可分类失败码 M0001').toContain('M0001');
  });
});
