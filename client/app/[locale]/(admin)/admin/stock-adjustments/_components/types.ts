export type StockAdjustmentStatus = 'DRAFT' | 'APPROVED' | 'CONFIRMED' | 'CANCELLED';
export type StockAdjustmentType = 'EXPORT' | 'STOCKTAKE';

export interface StockAdjustmentItemResponse {
  id: number;
  variantId: number;
  sku: string;
  productName: string;
  size: number;
  colorway: string;
  quantityChange: number;
  quantityBefore: number;
  quantityAfter: number;
  note: string | null;
}

export interface StockAdjustmentResponse {
  id: number;
  code: string;
  type: StockAdjustmentType;
  status: StockAdjustmentStatus;
  reason: string;
  createdById: number | null;
  createdByEmail: string | null;
  approvedById: number | null;
  approvedByEmail: string | null;
  approvedAt: string | null;
  confirmedAt: string | null;
  createdAt: string;
  items: StockAdjustmentItemResponse[];
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: StockAdjustmentResponse[];
  meta: PageMeta;
}

export const TYPE_OPTIONS = [
  { value: 'EXPORT',    label: 'Xuất kho (hỏng/mất/tặng/demo)' },
  { value: 'STOCKTAKE', label: 'Kiểm kê (phát hiện dư)' },
] as const;

export const TYPE_LABELS: Record<StockAdjustmentType, string> = {
  EXPORT: 'Xuất kho',
  STOCKTAKE: 'Kiểm kê',
};

export const STATUS_OPTIONS = [
  { value: 'DRAFT',     label: 'Nháp' },
  { value: 'APPROVED',  label: 'Đã duyệt' },
  { value: 'CONFIRMED', label: 'Đã áp dụng' },
  { value: 'CANCELLED', label: 'Đã hủy' },
] as const;

export const STATUS_LABELS: Record<StockAdjustmentStatus, string> = {
  DRAFT: 'Nháp',
  APPROVED: 'Đã duyệt',
  CONFIRMED: 'Đã áp dụng',
  CANCELLED: 'Đã hủy',
};

export const STATUS_COLORS: Record<StockAdjustmentStatus, string> = {
  DRAFT: 'bg-line-2 text-muted',
  APPROVED: 'bg-blue-100 text-blue-700',
  CONFIRMED: 'bg-ok-bg text-ok',
  CANCELLED: 'bg-danger-bg text-danger',
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
