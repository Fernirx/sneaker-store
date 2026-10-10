export interface CategoryRow {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  imagePublicId: string | null;
  displayOrder: number;
  active: boolean;
  parentId: number | null;
  parentName: string | null;
  productCount: number;
  childrenCount: number;
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
  data: CategoryRow[];
  meta: PageMeta;
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
