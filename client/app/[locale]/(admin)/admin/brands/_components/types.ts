export interface BrandRow {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  logoPublicId: string | null;
  active: boolean;
  createdAt: string;
}

export interface PageData {
  data: BrandRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
