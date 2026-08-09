export type ReturnResolutionType = 'REFUND' | 'EXCHANGE';
export type ReturnStatus = 'PENDING' | 'APPROVED' | 'RECEIVED' | 'REFUND_PENDING' | 'COMPLETED' | 'REJECTED' | 'REJECTED_AFTER_INSPECTION';

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

export interface ReturnRequestResponse {
  id: number;
  code: string;
  orderId: number;
  orderCode: string;
  resolutionType: ReturnResolutionType;
  status: ReturnStatus;
  reason: string;
  rejectReason: string | null;
  trackingCode: string | null;
  refundAmount: number | null;
  refundedAt: string | null;
  exchangeShippingOrderCode: string | null;
  exchangeExpectedDeliveryAt: string | null;
  createdAt: string;
  items: ReturnRequestItemResponse[];
  imagePublicIds: string[] | null;
}

export interface ExchangeCandidate {
  variantId: number;
  size: number;
  shoeWidth: string;
  colorway: string;
  sku: string;
  price: number;
  stockQuantity: number;
}

export interface EligibleOrderItemResponse {
  orderItemId: number;
  productName: string;
  variantSku: string;
  variantSize: number;
  variantColor: string;
  unitPrice: number;
  purchasedQuantity: number;
  maxReturnableQuantity: number;
  exchangeCandidates: ExchangeCandidate[];
}

export const RETURN_STATUS_LABELS: Record<ReturnStatus, string> = {
  PENDING: 'Chờ duyệt',
  APPROVED: 'Đã duyệt — chờ gửi hàng',
  RECEIVED: 'Đã nhận hàng — đang kiểm tra',
  REFUND_PENDING: 'Chờ hoàn tiền',
  COMPLETED: 'Hoàn tất',
  REJECTED: 'Đã từ chối',
  REJECTED_AFTER_INSPECTION: 'Không đạt kiểm tra',
};

export const RETURN_STATUS_COLORS: Record<ReturnStatus, string> = {
  PENDING: 'bg-warn-bg text-warn',
  APPROVED: 'bg-blue-100 text-blue-700',
  RECEIVED: 'bg-blue-100 text-blue-700',
  REFUND_PENDING: 'bg-purple-100 text-purple-700',
  COMPLETED: 'bg-ok-bg text-ok',
  REJECTED: 'bg-danger-bg text-danger',
  REJECTED_AFTER_INSPECTION: 'bg-danger-bg text-danger',
};

export const RESOLUTION_TYPE_LABELS: Record<ReturnResolutionType, string> = {
  REFUND: 'Hoàn tiền',
  EXCHANGE: 'Đổi size/màu',
};
