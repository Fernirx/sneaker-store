export interface CommentRow {
  id: number;
  productId: number;
  productName: string;
  userId: number;
  userEmail: string;
  parentId: number | null;
  content: string;
  approved: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: CommentRow[];
  meta: PageMeta;
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
