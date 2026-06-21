import { expect, type APIRequestContext } from '@playwright/test';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';

/**
 * API 契约硬门禁 — OpenAPI（取自各服务 /v3/api-docs）+ RFC 9110 状态语义。
 * 校验：① 端点已文档化；② 实际响应状态在文档声明内；③ 响应体符合声明 schema。
 */
export interface OpenApiDoc {
  openapi?: string;
  paths: Record<string, Record<string, any>>;
  components?: { schemas?: Record<string, any> };
}

export async function loadOpenApi(request: APIRequestContext, baseUrl: string): Promise<OpenApiDoc> {
  const res = await request.get(`${baseUrl}/v3/api-docs`);
  expect(res.status(), `${baseUrl}/v3/api-docs 可达`).toBe(200);
  const doc = (await res.json()) as OpenApiDoc;
  expect(doc.openapi, 'OpenAPI 3 文档').toMatch(/^3\./);
  expect(doc.paths, 'paths 存在').toBeTruthy();
  return doc;
}

/** 找到某 method+path 的 operation（path 支持 OpenAPI 模板，如 /records/{id}）。 */
export function getOperation(doc: OpenApiDoc, method: string, path: string) {
  const op = doc.paths?.[path]?.[method.toLowerCase()];
  return op;
}

export function assertPathDocumented(doc: OpenApiDoc, method: string, path: string) {
  const op = getOperation(doc, method, path);
  expect(op, `OpenAPI 已声明 ${method.toUpperCase()} ${path}`).toBeTruthy();
  return op;
}

/** 文档声明的响应状态码列表。 */
export function documentedStatuses(doc: OpenApiDoc, method: string, path: string): string[] {
  const op = getOperation(doc, method, path);
  return op?.responses ? Object.keys(op.responses) : [];
}

/** 断言实际状态码落在文档声明内（含 2XX/4XX 通配）。 */
export function assertStatusDocumented(doc: OpenApiDoc, method: string, path: string, status: number) {
  const declared = documentedStatuses(doc, method, path);
  const ok = declared.includes(String(status)) ||
    declared.includes(`${Math.floor(status / 100)}XX`) ||
    declared.includes('default');
  expect(ok, `状态 ${status} 应在 ${method.toUpperCase()} ${path} 的声明 [${declared.join(',')}] 内`).toBeTruthy();
}

/**
 * 用 ajv 按 OpenAPI 内联 schema（含 #/components/$ref）校验响应体。
 * 把整份 doc 注册为 'openapi'，再以 $ref 指向具体 schema 解析引用。
 */
export function makeSchemaValidator(doc: OpenApiDoc) {
  const ajv = new Ajv({ strict: false, allErrors: true, validateFormats: true });
  addFormats(ajv);
  ajv.addSchema(doc as object, 'openapi');
  return (schemaRefOrObject: string | object, data: unknown) => {
    const schema = typeof schemaRefOrObject === 'string'
      ? { $ref: `openapi#/components/schemas/${schemaRefOrObject}` }
      : schemaRefOrObject;
    const validate = ajv.compile(schema);
    const valid = validate(data);
    return { valid, errors: validate.errors ?? [] };
  };
}

/** 取某 operation 指定状态码的响应体 schema（application/json）。 */
export function responseSchema(doc: OpenApiDoc, method: string, path: string, status: string) {
  const op = getOperation(doc, method, path);
  return op?.responses?.[status]?.content?.['application/json']?.schema;
}
