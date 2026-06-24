export interface ReviewImageRow {
  id: number;
  imagePublicId: string;
  displayOrder: number;
}

export interface ReviewRow {
  id: number;
  productId: number;
  productName: string;
  userId: number;
  userEmail: string;
  orderId: number;
  orderCode: string;
  rating: number;
  title: string | null;
  comment: string | null;
  approved: boolean;
  images: ReviewImageRow[];
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
  data: ReviewRow[];
  meta: PageMeta;
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
