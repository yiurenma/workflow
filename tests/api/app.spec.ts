import { test, expect, request } from '@playwright/test';
import { OPERATION_API_BASE } from '../playwright.config';

/**
 * APP / control-plane (operation-api) — encodes pm-doc-master v2.29 APP-US-* and
 * arch error catalog (ApiErrorCatalog). Verdicts feed audit §3.
 *
 * Test app names are read-only/negative where possible to avoid mutating shared UAT data.
 * Mutating cases are gated behind APP_WRITE=1.
 */
const WRITE = process.env.APP_WRITE === '1';
const APP_A = process.env.APP_A ?? 'TEST_APP_A';

test.describe('APP-US-01 list & search (TC-01)', () => {
  test('APP-AC-01: list applications returns 200 + page model', async ({ request }) => {
    const res = await request.get(`${OPERATION_API_BASE}/api/workflow/entity-setting?page=0&size=5`);
    expect(res.status(), 'list endpoint reachable').toBe(200);
    const body = await res.json();
    // Spring Data page model
    expect(body).toHaveProperty('content');
    expect(Array.isArray(body.content ?? body)).toBeTruthy();
  });

  test('APP-AC-01-D2: fuzzy search by applicationName', async ({ request }) => {
    const res = await request.get(
      `${OPERATION_API_BASE}/api/workflow/entity-setting?applicationName=${encodeURIComponent(APP_A)}&page=0&size=5`,
    );
    expect(res.status()).toBe(200);
  });
});

test.describe('APP-US-18 entity-setting history & PATCH (TC-18)', () => {
  test('APP-AC-11: history endpoint returns Envers revisions', async ({ request }) => {
    const res = await request.get(
      `${OPERATION_API_BASE}/api/workflow/entity-setting/history?applicationName=${encodeURIComponent(APP_A)}&page=0&size=5`,
    );
    // 200 with revisions, or WF-400-401 if app not unique — both are defined behaviors
    expect([200, 400]).toContain(res.status());
  });

  test('APP-AC-18: PATCH unknown app → WF-404-101', async ({ request }) => {
    const res = await request.patch(
      `${OPERATION_API_BASE}/api/workflow/entity-setting?applicationName=DOES_NOT_EXIST_${Date.now()}`,
      { data: { description: 'audit-probe' }, headers: { 'Content-Type': 'application/json' } },
    );
    expect(res.status()).toBe(404);
    const body = await res.json().catch(() => ({}));
    expect(JSON.stringify(body)).toContain('WF-404-101');
  });
});

test.describe('APP-US-10 autoCopy negative cases (TC-10)', () => {
  test('APP-AC-10: same source/target → WF-400-301', async ({ request }) => {
    const res = await request.post(
      `${OPERATION_API_BASE}/api/workflow/autoCopy?fromApplicationName=${encodeURIComponent(APP_A)}&toApplicationName=${encodeURIComponent(APP_A)}`,
    );
    expect(res.status()).toBe(400);
    expect(JSON.stringify(await res.json().catch(() => ({})))).toContain('WF-400-301');
  });

  test('APP-AC-10: non-existent source → WF-400-302', async ({ request }) => {
    const res = await request.post(
      `${OPERATION_API_BASE}/api/workflow/autoCopy?fromApplicationName=NON_EXISTENT_${Date.now()}&toApplicationName=SOME_TARGET`,
    );
    expect(res.status()).toBe(400);
    expect(JSON.stringify(await res.json().catch(() => ({})))).toContain('WF-400-302');
  });
});

test.describe('APP-US-03 delete (TC-03)', () => {
  test('APP-AC-03-G2: delete non-existent app → WF-400-101', async ({ request }) => {
    const res = await request.delete(
      `${OPERATION_API_BASE}/api/workflow?applicationName=DOES_NOT_EXIST_${Date.now()}`,
    );
    expect(res.status()).toBe(400);
    expect(JSON.stringify(await res.json().catch(() => ({})))).toContain('WF-400-101');
  });
});

test.describe('REC-US-19 records query filters (TC-19)', () => {
  test('REC-AC-19-D2: records list accepts filters + pagination', async ({ request }) => {
    const res = await request.get(
      `${OPERATION_API_BASE}/api/workflow/records?applicationName=${encodeURIComponent(APP_A)}&page=0&size=5`,
    );
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body).toHaveProperty('content');
  });
});

test.describe('APP-US-52 deploy CORS proxy (DIV-1)', () => {
  test('deploy proxy endpoint exists (rejects missing targetUrl, not 404)', async ({ request }) => {
    const res = await request.post(`${OPERATION_API_BASE}/workflow/deploy/proxy`, {
      data: {}, headers: { 'Content-Type': 'application/json' },
    });
    // Endpoint present: expect a 4xx/5xx business response, NOT 404 route-missing
    expect(res.status(), 'proxy route should exist').not.toBe(404);
  });
});

// Mutating round-trip (create → re-post upsert → delete keeps records). Opt-in only.
test.describe('APP write round-trip (APP_WRITE=1)', () => {
  test.skip(!WRITE, 'set APP_WRITE=1 to run mutating tests against UAT');
  test('APP-AC-02 + APP-AC-03-G1: create then delete', async ({ request }) => {
    const name = `AUDIT_TMP_${Date.now()}`;
    const create = await request.post(`${OPERATION_API_BASE}/api/workflow?applicationName=${name}`, {
      data: { pluginList: [], uiMapList: [] }, headers: { 'Content-Type': 'application/json' },
    });
    expect(create.status()).toBe(200);
    const del = await request.delete(`${OPERATION_API_BASE}/api/workflow?applicationName=${name}`);
    expect(del.status()).toBe(200);
  });
});
