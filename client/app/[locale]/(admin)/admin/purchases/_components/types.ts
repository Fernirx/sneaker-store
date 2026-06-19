export type PurchaseStatus = 'DRAFT' | 'CONFIRMED' | 'RECEIVED' | 'CANCELLED';
export type PurchasePaymentStatus = 'UNPAID' | 'PAID';

export interface PurchaseItemResponse {
  id: number;
  variantId: number;
  sku: string;
  productName: string;
  size: number;
  colorway: string;
  quantityOrdered: number;
  quantityReceived: number;
  defectiveQty: number;
  unitCost: number;
  lineTotal: number;
  notes: string | null;
}

export interface PurchaseResponse {
  id: number;
  supplierId: number;
  supplierName: string;
  purchaseCode: string;
  supplierInvoiceNo: string | null;
  subtotal: number;
  discountAmount: number;
  taxAmount: number;
  shippingCost: number;
  totalCost: number;
  paymentStatus: PurchasePaymentStatus;
  status: PurchaseStatus;
  notes: string | null;
  createdById: number | null;
  createdByEmail: string | null;
  receivedById: number | null;
  receivedByEmail: string | null;
  confirmedAt: string | null;
  receivedAt: string | null;
  createdAt: string;
  items: PurchaseItemResponse[];
}

export interface SupplierBrief {
  id: number;
  code: string;
  name: string;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: PurchaseResponse[];
  meta: PageMeta;
}

export const STATUS_OPTIONS = [
  { value: 'DRAFT',     label: 'Nháp' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'RECEIVED',  label: 'Đã nhận hàng' },
  { value: 'CANCELLED', label: 'Đã hủy' },
] as const;

export const STATUS_LABELS: Record<PurchaseStatus, string> = {
  DRAFT: 'Nháp',
  CONFIRMED: 'Đã xác nhận',
  RECEIVED: 'Đã nhận hàng',
  CANCELLED: 'Đã hủy',
};

export const STATUS_COLORS: Record<PurchaseStatus, string> = {
  DRAFT: 'bg-line-2 text-muted',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  RECEIVED: 'bg-ok-bg text-ok',
  CANCELLED: 'bg-danger-bg text-danger',
};

export const PAYMENT_STATUS_OPTIONS = [
  { value: 'UNPAID', label: 'Chưa thanh toán' },
  { value: 'PAID',   label: 'Đã thanh toán' },
] as const;

export const PAYMENT_STATUS_LABELS: Record<PurchasePaymentStatus, string> = {
  UNPAID: 'Chưa thanh toán',
  PAID: 'Đã thanh toán',
};

export const PAYMENT_STATUS_COLORS: Record<PurchasePaymentStatus, string> = {
  UNPAID: 'bg-warn-bg text-warn',
  PAID: 'bg-ok-bg text-ok',
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
