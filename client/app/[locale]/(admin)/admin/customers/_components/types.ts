export interface CustomerRow {
  id: number;
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  membershipTier: string;
  loyaltyPoints: number;
  totalSpent: number;
  createdAt: string;
}

export interface PageData {
  data: CustomerRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const TIERS = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM'] as const;

export function tierBadgeClass(tier: string) {
  const map: Record<string, string> = {
    BRONZE: 'bg-[#fdf0e8] text-[#9a6030]',
    SILVER: 'bg-line text-muted',
    GOLD: 'bg-warn-bg text-warn',
    PLATINUM: 'bg-accent-tint text-accent',
  };
  return map[tier] ?? 'bg-line text-muted';
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function formatCurrency(amount: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(amount);
}

export function fullName(c: CustomerRow) {
  return [c.firstName, c.lastName].filter(Boolean).join(' ');
}
