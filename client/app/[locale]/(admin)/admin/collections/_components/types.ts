export interface TranslationRow {
  locale: string;
  name: string;
  description: string | null;
}

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
  translations: TranslationRow[];
}

export interface PageData {
  data: CollectionRow[];
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
