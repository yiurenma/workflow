import { ApiError } from './types';

/** Base path or full URL; paths should not end with /. */
export const OPERATION_API_BASE =
  import.meta.env.VITE_OPERATION_API_BASE || '/api/proxy/operation';

export const ONLINE_API_BASE =
  import.meta.env.VITE_ONLINE_API_BASE || '/api/proxy/online';

export const joinApiUrl = (base: string, path: string): string => {
  const b = base.replace(/\/$/, '');
  const p = path.startsWith('/') ? path : `/${path}`;
  return `${b}${p}`;
};

export const defaultHeaders = {
  'Content-Type': 'application/json',
};

export class ApiErrorImpl extends Error implements ApiError {
  constructor(
    message: string,
    public code: string,
    public status: number,
    public details?: Record<string, unknown>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export const handleApiError = async (response: Response): Promise<never> => {
  let message = 'An unexpected error occurred';
  let code = 'UNKNOWN_ERROR';
  let details: Record<string, unknown> | undefined;

  const text = await response.text();
  if (text) {
    try {
      const error = JSON.parse(text) as Record<string, unknown>;
      message =
        (typeof error.message === 'string' && error.message) ||
        (typeof error.error === 'string' && error.error) ||
        message;
      code =
        (typeof error.code === 'string' && error.code) ||
        (typeof error.error === 'string' ? String(error.error) : code);
      if (error.details && typeof error.details === 'object') {
        details = error.details as Record<string, unknown>;
      }
    } catch {
      message = text.slice(0, 500);
    }
  }

  throw new ApiErrorImpl(message, code, response.status, details);
};

export const createQueryKey = (base: string, params?: Record<string, unknown>) => {
  return params ? [base, params] : [base];
};
