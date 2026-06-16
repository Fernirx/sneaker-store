export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT';

export interface CouponRow {
  id: number;
  code: string;
  description: string | null;
  discountType: DiscountType;
  discountValue: number;
  minOrderAmount: number | null;
  maxDiscountAmount: number | null;
  usageLimit: number | null;
  usedCount: number;
  userUsageLimit: number | null;
  startDate: string | null;
  endDate: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PageData {
  data: CouponRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export function formatDate(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function formatDateTime(iso: string | null): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatPrice(n: number): string {
  return n.toLocaleString('vi-VN') + '₫';
}

export function formatDiscount(type: DiscountType, value: number): string {
  return type === 'PERCENTAGE' ? `${value}%` : formatPrice(value);
}

/** Convert Spring LocalDateTime string → datetime-local input value (YYYY-MM-DDTHH:mm) */
export function toInputDatetime(iso: string | null): string {
  if (!iso) return '';
  return iso.slice(0, 16);
}

/** Convert datetime-local input value → Spring LocalDateTime string */
export function fromInputDatetime(val: string): string | null {
  if (!val) return null;
  return val.length === 16 ? `${val}:00` : val;
}
