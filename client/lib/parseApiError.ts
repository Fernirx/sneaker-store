import axios from 'axios';

export interface ParsedError {
  general: string;
  fields: Record<string, string>;
}

export function parseApiError(err: unknown, fallback = 'Đã có lỗi xảy ra'): ParsedError {
  if (!axios.isAxiosError(err)) {
    return { general: err instanceof Error ? err.message : fallback, fields: {} };
  }
  const d = err.response?.data as
    | { fields?: { field: string; message: string }[]; message?: string }
    | undefined;
  const fields: Record<string, string> = {};
  d?.fields?.forEach(f => { fields[f.field] = f.message; });
  return {
    general: Object.keys(fields).length > 0 ? '' : (d?.message ?? fallback),
    fields,
  };
}
