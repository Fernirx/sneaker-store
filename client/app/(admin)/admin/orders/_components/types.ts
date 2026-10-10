export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'DELIVERED' | 'CANCELLED';
export type OrderPaymentStatus = 'UNPAID' | 'PAID' | 'REFUNDED';
export type PaymentMethod = 'VNPAY' | 'COD';
export type PaymentTxnStatus = 'SUCCESS' | 'FAILED';

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

export interface OrderInternalResponse {
  id: number;
  code: string;
  status: OrderStatus;
  paymentStatus: OrderPaymentStatus;
  paymentMethod: PaymentMethod;
  customerEmail: string | null;
  guestToken: string | null;
  trackingToken: string | null;
  recipientName: string;
  recipientPhone: string;
  shippingStreet: string;
  shippingWard: string;
  shippingDistrict: string;
  shippingProvince: string;
  subtotal: number;
  shippingFee: number;
  discountAmount: number;
  tierDiscountAmount: number;
  totalAmount: number;
  couponCode: string | null;
  note: string | null;
  adminNote: string | null;
  assignedToId: number | null;
  expiredAt: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemResponse[];
  shipment: ShipmentResponse | null;
}

export interface ShipmentResponse {
  shippingOrderCode: string | null;
  status: string | null;
  expectedDeliveryAt: string | null;
  syncedAt: string | null;
}

export const SHIPMENT_STATUS_LABELS: Record<string, string> = {
  ready_to_pick: 'Đã tạo đơn giao hàng',
  picking: 'Đang lấy hàng',
  delivering: 'Đang giao hàng',
  delivered: 'Đã giao hàng',
  cancel: 'Đã hủy',
};

export interface OrderStatusHistoryResponse {
  id: number;
  oldStatus: OrderStatus | null;
  newStatus: OrderStatus;
  note: string | null;
  createdAt: string;
}

export interface PaymentInternalResponse {
  id: number;
  orderId: number;
  orderCode: string;
  amount: number;
  status: PaymentTxnStatus;
  transactionId: string | null;
  responseCode: string | null;
  createdAt: string;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageResult {
  data: OrderInternalResponse[];
  meta: PageMeta;
}

export const STATUS_OPTIONS = [
  { value: 'PENDING',   label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPING',  label: 'Đang giao' },
  { value: 'DELIVERED', label: 'Đã giao' },
  { value: 'CANCELLED', label: 'Đã hủy' },
] as const;

export const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
};

export const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING: 'bg-warn-bg text-warn',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  SHIPPING: 'bg-blue-100 text-blue-700',
  DELIVERED: 'bg-ok-bg text-ok',
  CANCELLED: 'bg-danger-bg text-danger',
};

export const PAYMENT_STATUS_OPTIONS = [
  { value: 'UNPAID', label: 'Chưa thanh toán' },
  { value: 'PAID',   label: 'Đã thanh toán' },
  { value: 'REFUNDED', label: 'Đã hoàn tiền' },
] as const;

export const PAYMENT_STATUS_LABELS: Record<OrderPaymentStatus, string> = {
  UNPAID: 'Chưa thanh toán',
  PAID: 'Đã thanh toán',
  REFUNDED: 'Đã hoàn tiền',
};

export const PAYMENT_STATUS_COLORS: Record<OrderPaymentStatus, string> = {
  UNPAID: 'bg-warn-bg text-warn',
  PAID: 'bg-ok-bg text-ok',
  REFUNDED: 'bg-purple-100 text-purple-700',
};

export const PAYMENT_METHOD_OPTIONS = [
  { value: 'VNPAY', label: 'VNPay' },
  { value: 'COD',   label: 'Thanh toán khi nhận hàng' },
] as const;

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  VNPAY: 'VNPay',
  COD: 'COD',
};

export function formatPrice(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
}

export function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
