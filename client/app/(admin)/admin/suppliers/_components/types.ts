export interface SupplierRow {
  id: number;
  code: string;
  name: string;
  email: string | null;
  phone: string | null;
  contactPerson: string | null;
  contactPhone: string | null;
  address: string | null;
  notes: string | null;
  active: boolean;
  createdAt: string;
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: SupplierRow[];
  meta: PageMeta;
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
