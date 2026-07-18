export type ReturnResolutionType = 'REFUND' | 'EXCHANGE';
export type ReturnStatus = 'PENDING' | 'APPROVED' | 'RECEIVED' | 'COMPLETED' | 'REJECTED' | 'REJECTED_AFTER_INSPECTION';

export interface ReturnRequestItemResponse {
  id: number;
  orderItemId: number;
  productName: string;
  variantSku: string;
  variantSize: number;
  variantColor: string;
  quantity: number;
  unitPrice: number;
  exchangeVariantId: number | null;
  exchangeVariantSku: string | null;
  exchangeVariantSize: number | null;
  exchangeVariantColorway: string | null;
  refundAmount: number;
}

export interface ReturnRequestInternalResponse {
  id: number;
  code: string;
  orderId: number;
  orderCode: string;
  customerId: number;
  customerEmail: string;
  resolutionType: ReturnResolutionType;
  status: ReturnStatus;
  reason: string;
  rejectReason: string | null;
  trackingCode: string | null;
  refundAmount: number | null;
  refundedAt: string | null;
  exchangeShippingOrderCode: string | null;
  exchangeExpectedDeliveryAt: string | null;
  approvedById: number | null;
  approvedByEmail: string | null;
  approvedAt: string | null;
  receivedAt: string | null;
  completedAt: string | null;
  adminNote: string | null;
  createdAt: string;
  items: ReturnRequestItemResponse[];
  imagePublicIds: string[] | null;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: ReturnRequestInternalResponse[];
  meta: PageMeta;
}

export const RESOLUTION_TYPE_OPTIONS = [
  { value: 'REFUND', label: 'Hoàn tiền' },
  { value: 'EXCHANGE', label: 'Đổi size/màu' },
] as const;

export const RESOLUTION_TYPE_LABELS: Record<ReturnResolutionType, string> = {
  REFUND: 'Hoàn tiền',
  EXCHANGE: 'Đổi size/màu',
};

export const STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Chờ duyệt' },
  { value: 'APPROVED', label: 'Đã duyệt' },
  { value: 'RECEIVED', label: 'Đã nhận hàng' },
  { value: 'COMPLETED', label: 'Hoàn tất' },
  { value: 'REJECTED', label: 'Đã từ chối' },
  { value: 'REJECTED_AFTER_INSPECTION', label: 'Không đạt kiểm tra' },
] as const;

export const STATUS_LABELS: Record<ReturnStatus, string> = {
  PENDING: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  RECEIVED: 'Đã nhận hàng',
  COMPLETED: 'Hoàn tất',
  REJECTED: 'Đã từ chối',
  REJECTED_AFTER_INSPECTION: 'Không đạt kiểm tra',
};

export const STATUS_COLORS: Record<ReturnStatus, string> = {
  PENDING: 'bg-warn-bg text-warn',
  APPROVED: 'bg-blue-100 text-blue-700',
  RECEIVED: 'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-ok-bg text-ok',
  REJECTED: 'bg-danger-bg text-danger',
  REJECTED_AFTER_INSPECTION: 'bg-danger-bg text-danger',
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

export function formatPrice(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
}
