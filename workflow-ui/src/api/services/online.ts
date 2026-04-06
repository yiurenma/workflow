import {
  defaultHeaders,
  handleApiError,
  joinApiUrl,
  ONLINE_API_BASE,
} from '../config';

export type OnlineWorkflowRequest = {
  applicationName: string;
  confirmationNumber?: string;
  channelKind?: string;
  body: string;
  contentType?: string;
};

function randomCorrelationId(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `corr-${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

export const onlineApi = {
  postWorkflow: async (req: OnlineWorkflowRequest): Promise<Response> => {
    const sp = new URLSearchParams({ applicationName: req.applicationName });
    if (req.confirmationNumber) sp.set('confirmationNumber', req.confirmationNumber);
    if (req.channelKind) sp.set('channelKind', req.channelKind);

    const headers: Record<string, string> = {
      ...defaultHeaders,
      'X-Request-Correlation-Id': randomCorrelationId(),
      ...(req.contentType ? { 'Content-Type': req.contentType } : {}),
    };

    const response = await fetch(
      joinApiUrl(ONLINE_API_BASE, `/workflow?${sp.toString()}`),
      {
        method: 'POST',
        headers,
        body: req.body,
      }
    );

    if (!response.ok) {
      return handleApiError(response);
    }
    return response;
  },
};
