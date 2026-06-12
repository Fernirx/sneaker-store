export interface UserRow {
  id: number;
  email: string;
  active: boolean;
  emailVerified: boolean;
  roles: string[];
  createdAt: string;
}

export interface PageData {
  data: UserRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const ALL_ROLES = [
  'ROLE_USER',
  'ROLE_ADMIN',
  'ROLE_SALE',
  'ROLE_WAREHOUSE',
  'ROLE_MARKETING',
  'ROLE_TECHNICIAN',
] as const;

export function formatRole(role: string) {
  return role.replace('ROLE_', '');
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function roleBadgeClass(role: string) {
  if (role === 'ROLE_ADMIN') return 'bg-danger-bg text-danger';
  if (role === 'ROLE_SALE') return 'bg-ok-bg text-ok';
  if (role === 'ROLE_USER') return 'bg-line-2 text-muted';
  return 'bg-warn-bg text-warn';
}
