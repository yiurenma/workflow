import type { APIRequestContext } from '@playwright/test';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const __dirname = dirname(fileURLToPath(import.meta.url));

/**
 * 确定性种子 —— 对本地 operation-api(H2) 播种应用，保证 API 测试可重复。
 *
 * 外部依赖说明（黑盒约束下的诚实声明）：
 * 工作流的 CONSUMER 步骤会调真实外部 HTTP。本套件**不**实跑会真正打外部
 * 的执行型断言；执行/幂等类用例需在能 stub 外部依赖或提供 keystore 的环境下跑
 * （见 docs/local-verification-report.md）。种子只覆盖"定义/查询"面，确定性强。
 */
export interface SeedOptions {
  baseUrl?: string;
  workflowFile?: string;
}

const DEFAULT_BASE = process.env.OPERATION_API_BASE ?? 'http://localhost:8080';
const SAMPLE_WORKFLOW = resolve(__dirname, '../../docs/guide/examples/payment-notify.workflow.json');

/** 确保某应用存在（幂等 upsert）。返回 applicationName。 */
export async function ensureApp(
  request: APIRequestContext,
  applicationName: string,
  opts: SeedOptions = {},
): Promise<string> {
  const base = opts.baseUrl ?? DEFAULT_BASE;
  const file = opts.workflowFile ?? SAMPLE_WORKFLOW;
  const body = readFileSync(file, 'utf-8');
  const res = await request.post(`${base}/api/workflow?applicationName=${encodeURIComponent(applicationName)}`, {
    headers: { 'Content-Type': 'application/json' },
    data: body,
  });
  if (![200, 201].includes(res.status())) {
    throw new Error(`seed ${applicationName} 失败: ${res.status()} ${await res.text()}`);
  }
  return applicationName;
}

/** 删除某应用（清理）。忽略不存在。 */
export async function deleteApp(request: APIRequestContext, applicationName: string, opts: SeedOptions = {}) {
  const base = opts.baseUrl ?? DEFAULT_BASE;
  await request.delete(`${base}/api/workflow?applicationName=${encodeURIComponent(applicationName)}`).catch(() => {});
}
