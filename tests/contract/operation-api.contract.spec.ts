import { test, expect } from '@playwright/test';
import { OPERATION_API_BASE } from '../playwright.config';
import { loadOpenApi, assertPathDocumented, assertStatusDocumented } from '../lib/contract';

/**
 * @gate 契约硬门禁 — operation-api（控制面）。
 * OpenAPI(/v3/api-docs) 为 oracle：端点已声明 + 实际响应状态在声明内。
 * 防回归：后端改了路径/状态码/响应结构 → 这里立刻红。
 */
test.describe('@gate operation-api OpenAPI 契约', () => {
  test('Given OpenAPI doc, Then 核心端点均已声明', async ({ request }) => {
    const doc = await loadOpenApi(request, OPERATION_API_BASE);
    assertPathDocumented(doc, 'get', '/api/workflow/entity-setting');
    assertPathDocumented(doc, 'post', '/api/workflow');
    assertPathDocumented(doc, 'delete', '/api/workflow');
    assertPathDocumented(doc, 'post', '/api/workflow/autoCopy');
    assertPathDocumented(doc, 'patch', '/api/workflow/entity-setting');
    assertPathDocumented(doc, 'get', '/api/workflow/entity-setting/history');
    assertPathDocumented(doc, 'get', '/api/workflow/records');
    assertPathDocumented(doc, 'get', '/api/workflow/records/{id}');
    assertPathDocumented(doc, 'post', '/workflow/deploy/proxy');
  });

  test('When 列应用, Then 200 且状态在声明内、响应是分页模型', async ({ request }) => {
    const doc = await loadOpenApi(request, OPERATION_API_BASE);
    const res = await request.get(`${OPERATION_API_BASE}/api/workflow/entity-setting?page=0&size=5`);
    expect(res.status()).toBe(200);
    assertStatusDocumented(doc, 'get', '/api/workflow/entity-setting', 200);
    const body = await res.json();
    expect(body, 'Spring 分页模型字段').toMatchObject({
      content: expect.any(Array),
      totalElements: expect.any(Number),
    });
  });

  test('When 删除未知应用, Then 400 且状态在声明内', async ({ request }) => {
    const doc = await loadOpenApi(request, OPERATION_API_BASE);
    const res = await request.delete(`${OPERATION_API_BASE}/api/workflow?applicationName=NOPE_${Date.now()}`);
    expect(res.status()).toBe(400);
    assertStatusDocumented(doc, 'delete', '/api/workflow', 400);
  });
});
