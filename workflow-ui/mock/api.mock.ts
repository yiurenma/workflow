import { defineMock } from 'vite-plugin-mock-dev-server';
import type { SpringPage, WorkflowEntitySettingRow, WorkFlow } from '../src/api/types';

const mockRows: WorkflowEntitySettingRow[] = [
  {
    id: 1,
    applicationName: 'DEMO_APP',
    enabled: true,
    lastModifiedDateTime: new Date().toISOString(),
  },
];

const emptyWorkFlow = (): WorkFlow => ({ pluginList: [], uiMapList: [] });

export default defineMock([
  {
    url: '/api/proxy/operation/workflow/entity-setting',
    method: 'GET',
    body: {
      content: mockRows,
      totalElements: mockRows.length,
      totalPages: 1,
      size: 20,
      number: 0,
    } satisfies SpringPage<WorkflowEntitySettingRow>,
  },
  {
    url: '/api/proxy/operation/workflow',
    method: 'GET',
    body: (req) => {
      const url = new URL(req.url || '', 'http://local');
      const name = url.searchParams.get('applicationName');
      if (!name || name === 'MISSING') {
        return new Response(JSON.stringify({ message: 'Not found', code: 'NF' }), {
          status: 404,
          headers: { 'Content-Type': 'application/json' },
        });
      }
      return emptyWorkFlow();
    },
  },
  {
    url: '/api/proxy/operation/workflow',
    method: 'POST',
    body: (req) => {
      const url = new URL(req.url || '', 'http://local');
      const name = url.searchParams.get('applicationName');
      const body = (req.body || {}) as WorkFlow;
      if (!name) {
        return new Response(JSON.stringify({ message: 'applicationName required' }), {
          status: 400,
        });
      }
      return body.pluginList ? body : emptyWorkFlow();
    },
  },
  {
    url: '/api/proxy/operation/workflow',
    method: 'DELETE',
    body: { ok: true },
  },
]);
