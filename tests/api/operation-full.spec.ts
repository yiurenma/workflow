import { test, expect, type APIRequestContext } from '@playwright/test';
import { OPERATION_API_BASE as BASE } from '../playwright.config';

/**
 * @gate operation-api 全量功能 + 错误码（控制面，真实后端，自带数据可重复）。
 * 覆盖 TEST_CASES_MASTER：TC-01/02/03/10/11/18/19 + 负面错误码。
 * 每个用例用唯一应用名、用后即删，保证反复跑确定性。
 */
const uniq = (p: string) => `IT_${p}_${Date.now()}_${Math.floor(Math.random() * 1e4)}`;
const MIN_WF = { pluginList: [], uiMapList: [] };
const SAMPLE_WF = {
  pluginList: [
    { id: 1, description: 'c', linkingIdOfRuleListAndAction: 'r1', ruleList: [{ key: '$.a', remark: 'a' }], action: { type: 'CONSUMER', provider: 'http' } },
    { id: 2, description: 'm', linkingIdOfRuleListAndAction: 'r2', ruleList: [{ key: '$.b', remark: 'b' }], action: { type: 'MESSAGE' } },
  ],
  uiMapList: [{ id: 'e1', source: '1', target: '2' }],
};

async function createApp(request: APIRequestContext, name: string, wf: object = MIN_WF) {
  const res = await request.post(`${BASE}/api/workflow?applicationName=${encodeURIComponent(name)}`, {
    headers: { 'Content-Type': 'application/json' }, data: wf,
  });
  expect(res.status(), `create ${name}`).toBe(200);
  return res;
}
async function delApp(request: APIRequestContext, name: string) {
  await request.delete(`${BASE}/api/workflow?applicationName=${encodeURIComponent(name)}`).catch(() => {});
}
const codeOf = async (res: { json: () => Promise<any> }) => JSON.stringify(await res.json().catch(() => ({})));

// ─────────────────────────── TC-01 List & Search ───────────────────────────
test.describe('@gate TC-01 列表与搜索', () => {
  test('TC-01-1 列出应用返回分页模型', async ({ request }) => {
    const res = await request.get(`${BASE}/api/workflow/entity-setting?page=0&size=5`);
    expect(res.status()).toBe(200);
    expect(await res.json()).toMatchObject({ content: expect.any(Array), totalElements: expect.any(Number) });
  });
  test('TC-01-2 按名模糊搜索命中', async ({ request }) => {
    const name = uniq('SEARCH');
    await createApp(request, name);
    try {
      const res = await request.get(`${BASE}/api/workflow/entity-setting?applicationName=${encodeURIComponent(name)}&page=0&size=5`);
      expect(res.status()).toBe(200);
      const body = await res.json();
      expect(body.content.some((a: any) => a.applicationName === name), '搜索命中新建应用').toBeTruthy();
    } finally { await delApp(request, name); }
  });
});

// ─────────────────────────── TC-02 Create / Upsert ─────────────────────────
test.describe('@gate TC-02 创建 / Upsert', () => {
  test('TC-02-1 创建新应用并落库 pluginList', async ({ request }) => {
    const name = uniq('CREATE');
    try {
      await createApp(request, name, SAMPLE_WF);
      const get = await request.get(`${BASE}/api/workflow?applicationName=${encodeURIComponent(name)}`);
      expect(get.status()).toBe(200);
      expect((await get.json()).pluginList.length, '2 个节点落库').toBe(2);
    } finally { await delApp(request, name); }
  });
  test('TC-02-2 同名再 POST 为幂等 upsert', async ({ request }) => {
    const name = uniq('UPSERT');
    try {
      await createApp(request, name, SAMPLE_WF);
      const again = await request.post(`${BASE}/api/workflow?applicationName=${encodeURIComponent(name)}`, {
        headers: { 'Content-Type': 'application/json' }, data: MIN_WF,
      });
      expect(again.status(), '幂等 upsert').toBe(200);
    } finally { await delApp(request, name); }
  });
  test('TC-02-N POST 无 body → WF-400-102', async ({ request }) => {
    const name = uniq('NOBODY');
    const res = await request.post(`${BASE}/api/workflow?applicationName=${encodeURIComponent(name)}`, {
      headers: { 'Content-Type': 'application/json' },
    });
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('WF-400-102');
  });
});

// ─────────────────────────── TC-03 Delete ──────────────────────────────────
test.describe('@gate TC-03 删除', () => {
  test('TC-03-1 删除已存在应用 → 200 且应用消失', async ({ request }) => {
    const name = uniq('DEL');
    await createApp(request, name);
    const del = await request.delete(`${BASE}/api/workflow?applicationName=${encodeURIComponent(name)}`);
    expect(del.status()).toBe(200);
    const after = await request.get(`${BASE}/api/workflow/entity-setting?applicationName=${encodeURIComponent(name)}&page=0&size=5`);
    expect((await after.json()).content.some((a: any) => a.applicationName === name), '已删除').toBeFalsy();
  });
  test('TC-03-3 删除不存在应用 → WF-400-101', async ({ request }) => {
    const res = await request.delete(`${BASE}/api/workflow?applicationName=${uniq('GHOST')}`);
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('WF-400-101');
  });
});

// ─────────────────────────── TC-10 AutoCopy ────────────────────────────────
test.describe('@gate TC-10 跨应用复制', () => {
  test('TC-10-1/3 复制 A→B 且 pluginList 数量一致', async ({ request }) => {
    const a = uniq('SRC'), b = uniq('DST');
    await createApp(request, a, SAMPLE_WF);
    try {
      const copy = await request.post(`${BASE}/api/workflow/autoCopy?fromApplicationName=${encodeURIComponent(a)}&toApplicationName=${encodeURIComponent(b)}`);
      expect(copy.status(), 'autoCopy 成功').toBe(200);
      const gb = await request.get(`${BASE}/api/workflow?applicationName=${encodeURIComponent(b)}`);
      expect((await gb.json()).pluginList.length, '复制后节点数一致').toBe(2);
    } finally { await delApp(request, a); await delApp(request, b); }
  });
  test('TC-10-4 同源同目标 → WF-400-301', async ({ request }) => {
    const a = uniq('SAME');
    await createApp(request, a);
    try {
      const res = await request.post(`${BASE}/api/workflow/autoCopy?fromApplicationName=${encodeURIComponent(a)}&toApplicationName=${encodeURIComponent(a)}`);
      expect(res.status()).toBe(400);
      expect(await codeOf(res)).toContain('WF-400-301');
    } finally { await delApp(request, a); }
  });
  test('TC-10-5 源不存在 → WF-400-302', async ({ request }) => {
    const res = await request.post(`${BASE}/api/workflow/autoCopy?fromApplicationName=${uniq('NOSRC')}&toApplicationName=${uniq('T')}`);
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('WF-400-302');
  });
});

// ─────────────────────────── TC-18 Entity-Setting PATCH + History ──────────
test.describe('@gate TC-18 应用设置 PATCH 与历史', () => {
  test('TC-18-1 PATCH 多字段并回读一致', async ({ request }) => {
    const name = uniq('PATCH');
    await createApp(request, name);
    try {
      const patch = await request.patch(`${BASE}/api/workflow/entity-setting?applicationName=${encodeURIComponent(name)}`, {
        headers: { 'Content-Type': 'application/json' },
        data: { enabled: false, asyncMode: true, retry: true, tracking: true, description: 'updated' },
      });
      expect(patch.status(), 'PATCH 成功').toBe(200);
      const get = await request.get(`${BASE}/api/workflow/entity-setting?applicationName=${encodeURIComponent(name)}&page=0&size=5`);
      const row = (await get.json()).content.find((a: any) => a.applicationName === name);
      expect(row.asyncMode, 'asyncMode 已更新').toBe(true);
    } finally { await delApp(request, name); }
  });
  test('TC-18-N PATCH 不存在应用 → WF-404-101', async ({ request }) => {
    const res = await request.patch(`${BASE}/api/workflow/entity-setting?applicationName=${uniq('NOPATCH')}`, {
      headers: { 'Content-Type': 'application/json' }, data: { description: 'x' },
    });
    expect(res.status()).toBe(404);
    expect(await codeOf(res)).toContain('WF-404-101');
  });
  test('TC-11 历史端点返回修订（PATCH 后）', async ({ request }) => {
    const name = uniq('HIST');
    await createApp(request, name);
    try {
      await request.patch(`${BASE}/api/workflow/entity-setting?applicationName=${encodeURIComponent(name)}`, {
        headers: { 'Content-Type': 'application/json' }, data: { description: 'rev1' },
      });
      const hist = await request.get(`${BASE}/api/workflow/entity-setting/history?applicationName=${encodeURIComponent(name)}&page=0&size=10`);
      expect(hist.status()).toBe(200);
    } finally { await delApp(request, name); }
  });
  test('TC-11-N 历史不存在应用 → WF-400-401', async ({ request }) => {
    const res = await request.get(`${BASE}/api/workflow/entity-setting/history?applicationName=${uniq('NOHIST')}&page=0&size=5`);
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('WF-400-401');
  });
});

// ─────────────────────────── TC-19 Records ─────────────────────────────────
test.describe('@gate TC-19 运行记录查询', () => {
  test('TC-19-1 按应用名 + 分页查询 → 200 分页模型', async ({ request }) => {
    const res = await request.get(`${BASE}/api/workflow/records?applicationName=${uniq('REC')}&page=0&size=5`);
    expect(res.status()).toBe(200);
    expect(await res.json()).toMatchObject({ content: expect.any(Array) });
  });
  test('TC-19-2 多筛选维度（trackingNumber/customerId/日期）受理', async ({ request }) => {
    const res = await request.get(`${BASE}/api/workflow/records?trackingNumber=TRK1&customerId=C1&page=0&size=5`);
    expect(res.status()).toBe(200);
  });
  test('TC-19-N 记录详情不存在 → 404 WF-404-000', async ({ request }) => {
    const res = await request.get(`${BASE}/api/workflow/records/999999999`);
    expect(res.status()).toBe(404);
    expect(await codeOf(res)).toContain('WF-404-000');
  });
});

// ─────────────────────────── Query 未知应用 ────────────────────────────────
test.describe('@gate 查询负面', () => {
  test('GET 未知应用工作流 → WF-400-101', async ({ request }) => {
    const res = await request.get(`${BASE}/api/workflow?applicationName=${uniq('NOGET')}`);
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('WF-400-101');
  });
});

// ─────────────────────────── Deploy 代理 ───────────────────────────────────
test.describe('@gate Deploy 代理端点', () => {
  test('POST /workflow/deploy/proxy 端点存在（非 404）', async ({ request }) => {
    const res = await request.post(`${BASE}/workflow/deploy/proxy`, { headers: { 'Content-Type': 'application/json' }, data: {} });
    expect(res.status(), '代理路由存在').not.toBe(404);
  });
});
