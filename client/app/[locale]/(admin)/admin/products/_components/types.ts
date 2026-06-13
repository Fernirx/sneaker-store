export interface BrandBrief {
  id: number;
  name: string;
  slug: string;
  logoPublicId: string | null;
}

export interface ProductRow {
  id: number;
  slug: string;
  code: string;
  styleCode: string | null;
  name: string;
  gender: string;
  description: string | null;
  upperMaterial: string | null;
  soleType: string | null;
  closureType: string | null;
  shaftStyle: string | null;
  basePrice: number;
  originalPrice: number | null;
  costPrice: number | null;
  newArrival: boolean;
  onSale: boolean;
  active: boolean;
  soldCount: number;
  viewCount: number;
  brand: BrandBrief;
  primaryImagePublicId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PageData {
  data: ProductRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const GENDER_OPTIONS = [
  { value: 'MEN',   label: 'Nam' },
  { value: 'WOMEN', label: 'Nữ' },
  { value: 'UNISEX', label: 'Unisex' },
  { value: 'KIDS',  label: 'Trẻ em' },
] as const;

export const CLOSURE_OPTIONS = [
  { value: 'LACE',    label: 'Dây buộc' },
  { value: 'SLIP_ON', label: 'Không dây' },
  { value: 'VELCRO',  label: 'Velcro' },
  { value: 'ZIPPER',  label: 'Kéo khóa' },
] as const;

export const SHAFT_OPTIONS = [
  { value: 'LOW',  label: 'Cổ thấp' },
  { value: 'MID',  label: 'Cổ giữa' },
  { value: 'HIGH', label: 'Cổ cao' },
] as const;

export const SHOE_WIDTH_OPTIONS = [
  { value: 'NARROW',  label: 'Hẹp' },
  { value: 'REGULAR', label: 'Thường' },
  { value: 'WIDE',    label: 'Rộng' },
] as const;

export function formatPrice(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
}

export function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}
