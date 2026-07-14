export interface DashboardSummary {
  revenueToday: number;
  revenueThisMonth: number;
  orderCountByStatus: Record<string, number>;
  revenueByDay: { date: string; revenue: number }[];
  lowStockCount: number;
  outOfStockCount: number;
  lowStockVariants: {
    variantId: number;
    productId: number;
    productName: string;
    sku: string;
    colorway: string;
    size: number;
    stockQuantity: number;
    minStockLevel: number;
  }[];
  topProducts: { productCode: string; productName: string; totalQuantity: number }[];
  newCustomersThisMonth: number;
  pendingPurchases: number;
  pendingStockAdjustments: number;
}

export const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING: 'Chờ xác nhận',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  CANCELLED: 'Đã hủy',
};

// Riêng cho chart: SHIPPING dùng indigo (khác CONFIRMED) để 2 cột phân biệt được trên biểu đồ,
// dù badge trạng thái đơn ở trang Orders dùng chung 1 màu xanh cho cả 2 (xem orders/_components/types.ts STATUS_COLORS).
export const ORDER_STATUS_CHART_COLOR: Record<string, string> = {
  PENDING: '#d97706',
  CONFIRMED: '#1d4ed8',
  SHIPPING: '#4338ca',
  DELIVERED: '#16a34a',
  CANCELLED: '#dc2626',
};

export const ORDER_STATUS_ORDER = ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED'] as const;

export function formatPrice(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
}

export function formatCompactPrice(value: number) {
  return new Intl.NumberFormat('vi-VN', { notation: 'compact', maximumFractionDigits: 1 }).format(value);
}

export function formatShortDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
}
