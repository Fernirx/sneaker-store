export type NotificationType = 'ORDER' | 'PAYMENT' | 'PROMOTION' | 'SYSTEM' | 'REVIEW' | 'INVENTORY' | 'PRODUCT' | 'RETURN';
export type NotificationTargetType = 'USER' | 'ROLE' | 'ALL';

export interface NotificationResponse {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  imagePublicId: string | null;
  link: string | null;
  read: boolean;
  createdAt: string;
}

export interface NotificationInternalResponse {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  imagePublicId: string | null;
  link: string | null;
  targetType: NotificationTargetType;
  targetRole: string | null;
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

export interface PageResult<T> {
  data: T[];
  meta: PageMeta;
}

export const TYPE_LABELS: Record<NotificationType, string> = {
  ORDER: 'Đơn hàng',
  PAYMENT: 'Thanh toán',
  PROMOTION: 'Khuyến mãi',
  SYSTEM: 'Hệ thống',
  REVIEW: 'Đánh giá',
  INVENTORY: 'Tồn kho',
  PRODUCT: 'Sản phẩm',
  RETURN: 'Đổi/trả hàng',
};

export const TARGET_TYPE_LABELS: Record<NotificationTargetType, string> = {
  USER: 'Khách hàng cụ thể',
  ROLE: 'Theo vai trò',
  ALL: 'Toàn bộ khách hàng',
};

export function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
