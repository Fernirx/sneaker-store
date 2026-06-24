export interface ReviewerInfo {
  userId: number;
  displayName: string;
  avatarPublicId: string | null;
}

export interface ReviewImageItem {
  id: number;
  imagePublicId: string;
  displayOrder: number;
}

export interface ReviewItem {
  id: number;
  rating: number;
  title: string | null;
  comment: string | null;
  user: ReviewerInfo;
  images: ReviewImageItem[];
  createdAt: string;
  updatedAt: string;
}

export interface ReviewSummary {
  averageRating: number | null;
  totalReviews: number;
  distribution: Record<string, number>;
}

export interface CommentItem {
  id: number;
  content: string;
  user: ReviewerInfo;
  createdAt: string;
  updatedAt: string;
  replies: CommentItem[];
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export function formatReviewDate(iso: string): string {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
