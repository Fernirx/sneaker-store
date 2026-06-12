export interface TranslationRow {
  locale: string;
  name: string;
  description: string | null;
}

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
  createdAt: string;
  translations: TranslationRow[];
}

export interface PageData {
  data: CategoryRow[];
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
