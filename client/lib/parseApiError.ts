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
  
  const status = err.response?.status;
  
  const fields: Record<string, string> = {};
  d?.fields?.forEach(f => { fields[f.field] = f.message; });
  
  let generalMessage = d?.message;
  if (!generalMessage && Object.keys(fields).length === 0) {
    if (status === 404) generalMessage = 'Không tìm thấy dữ liệu';
    else if (status === 400) generalMessage = 'Yêu cầu không hợp lệ';
    else if (status === 500) generalMessage = 'Lỗi máy chủ nội bộ';
    else generalMessage = fallback;
  }
  
  return {
    general: Object.keys(fields).length > 0 ? '' : (generalMessage ?? fallback),
    fields,
  };
}
