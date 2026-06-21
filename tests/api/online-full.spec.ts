import { test, expect } from '@playwright/test';
import { randomUUID } from 'node:crypto';
import { ONLINE_API_BASE as BASE, OPERATION_API_BASE as OP } from '../playwright.config';

/**
 * @gate online-api 全量错误码 + 参数校验（执行面）。
 * 必需头 X-Request-Correlation-Id（DIV-3）。完整执行/幂等需真实 JKS keystore
 * 密钥（仓库默认 changeit 非真实），相关用例用 test.fixme 标记 → TODO。
 */
const CORR = 'X-Request-Correlation-Id';
const uniq = (p: string) => `IT_${p}_${Date.now()}_${Math.floor(Math.random() * 1e4)}`;
const post = (request: any, qs: string, opts: any = {}) =>
  request.post(`${BASE}/api/workflow?${qs}`, { headers: { 'Content-Type': 'application/json', ...(opts.headers || {}) }, data: opts.data ?? { data: { amount: 1 } } });
const codeOf = async (res: any) => JSON.stringify(await res.json().catch(() => ({})));

test.describe('@gate online-api 参数与错误码', () => {
  test('未知应用 → 400 M0001', async ({ request }) => {
    const res = await post(request, `applicationName=${uniq('NO')}&confirmationNumber=${randomUUID()}`, { headers: { [CORR]: randomUUID() } });
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('M0001');
  });

  test('缺 X-Request-Correlation-Id → 400 (440000)', async ({ request }) => {
    const res = await post(request, `applicationName=${uniq('NOHDR')}&confirmationNumber=${randomUUID()}`);
    expect(res.status()).toBe(400);
    expect(await codeOf(res)).toContain('440000');
  });

  test('缺 confirmationNumber → 400', async ({ request }) => {
    const res = await post(request, `applicationName=${uniq('NOCONF')}`, { headers: { [CORR]: randomUUID() } });
    expect(res.status()).toBe(400);
  });

  test('缺 applicationName → 400', async ({ request }) => {
    const res = await post(request, `confirmationNumber=${randomUUID()}`, { headers: { [CORR]: randomUUID() } });
    expect(res.status()).toBe(400);
  });

  test('非法 JSON body → 4xx（不静默接受）', async ({ request }) => {
    const res = await request.post(
      `${BASE}/api/workflow?applicationName=${uniq('BADJSON')}&confirmationNumber=${randomUUID()}`,
      { headers: { 'Content-Type': 'application/json', [CORR]: randomUUID() }, data: '{not-json' },
    );
    expect(res.status()).toBeGreaterThanOrEqual(400);
  });
});

/**
 * 需真实 keystore 密钥才能跑通完整执行的用例（首次执行须 200）。
 * 见 TODO-tests-online-api-keystore-secret-for-execution。
 */
test.describe('online-api 执行/幂等（需 keystore 密钥）', () => {
  test('M0002 重复关联 ID 幂等拒绝', async ({ request }) => {
    test.fixme(true, '需真实 JKS keystore 密钥；首次执行加密失败(500)。见 TODO-tests-online-api-keystore-secret-for-execution');
    const name = uniq('IDEMP');
    await request.post(`${OP}/api/workflow?applicationName=${name}`, { headers: { 'Content-Type': 'application/json' }, data: { pluginList: [], uiMapList: [] } });
    const corr = randomUUID(), conf = randomUUID();
    const h = { [CORR]: corr };
    const first = await post(request, `applicationName=${name}&confirmationNumber=${conf}`, { headers: h });
    expect(first.status()).toBe(200);
    const second = await post(request, `applicationName=${name}&confirmationNumber=${conf}`, { headers: h });
    expect(second.status()).toBe(400);
    expect(await codeOf(second)).toContain('M0002');
  });

  test('M0004 自请求重复重试拒绝', async ({ request }) => {
    test.fixme(true, '需真实 JKS keystore 密钥才能产生首条记录。见 TODO-tests-online-api-keystore-secret-for-execution');
    expect(true).toBe(true);
  });
});
