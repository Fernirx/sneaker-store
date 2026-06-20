export interface CollectionRow {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  imagePublicId: string | null;
  launchDate: string | null;
  endDate: string | null;
  active: boolean;
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
  data: CollectionRow[];
  meta: PageMeta;
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
