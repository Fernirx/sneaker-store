export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';
export type OrderPaymentStatus = 'UNPAID' | 'PAID';
export type PaymentMethod = 'VNPAY' | 'COD';

export interface OrderItemResponse {
  id: number;
  variantId: number;
  productCode: string;
  productName: string;
  variantSku: string;
  variantSize: number;
  variantColor: string;
  quantity: number;
  originalPrice: number | null;
  unitPrice: number;
  subtotal: number;
}

export interface OrderResponse {
  id: number;
  code: string;
  status: OrderStatus;
  paymentStatus: OrderPaymentStatus;
  paymentMethod: PaymentMethod;
  recipientName: string;
  recipientPhone: string;
  shippingStreet: string;
  shippingWard: string;
  shippingProvince: string;
  subtotal: number;
  shippingFee: number;
  discountAmount: number;
  totalAmount: number;
  couponCode: string | null;
  note: string | null;
  expiredAt: string;
  createdAt: string;
  items: OrderItemResponse[];
}

export interface OrderStatusHistoryResponse {
  id: number;
  oldStatus: OrderStatus | null;
  newStatus: OrderStatus;
  note: string | null;
  createdAt: string;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING: 'bg-warn-bg text-warn',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  SHIPPING: 'bg-blue-100 text-blue-700',
  DELIVERED: 'bg-ok-bg text-ok',
  CANCELLED: 'bg-danger-bg text-danger',
};

export const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
};

export const PAYMENT_STATUS_LABELS: Record<OrderPaymentStatus, string> = {
  UNPAID: 'Chưa thanh toán',
  PAID: 'Đã thanh toán',
};

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  VNPAY: 'VNPay',
  COD: 'Thanh toán khi nhận hàng',
};

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
