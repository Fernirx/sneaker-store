export interface BannerRow {
  id: number;
  title: string;
  imagePublicId: string | null;
  linkUrl: string | null;
  displayOrder: number;
  active: boolean;
  startAt: string | null;
  endAt: string | null;
  createdAt: string;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: BannerRow[];
  meta: PageMeta;
}

export function formatDateTime(iso: string | null) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
