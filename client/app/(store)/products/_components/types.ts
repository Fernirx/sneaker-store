export interface BrandBrief {
  id: number;
  name: string;
  slug: string;
  logoPublicId: string | null;
}

export interface ColorSwatch {
  colorway: string;
  colorwayCode: string | null;
  colorHex: string | null;
  primaryImagePublicId: string | null;
  price: number | null;
}

export interface ProductResponse {
  id: number;
  slug: string;
  name: string;
  gender: string;
  minPrice: number | null;
  maxPrice: number | null;
  newArrival: boolean;
  onSale: boolean;
  brand: BrandBrief;
  colors: ColorSwatch[];
}

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface PageData {
  data: ProductResponse[];
  meta: PageMeta;
}

export interface CategoryBrief {
  id: number;
  name: string;
  slug: string;
  parentId: number | null;
}

export interface ImageItem {
  publicId: string;
  primary: boolean;
}

export interface SizeItem {
  variantId: number;
  sku?: string;
  size: number;
  shoeWidth: string;
  price: number | null;
  stockQuantity: number;
}

export interface ColorDetail {
  colorway: string;
  colorwayCode: string | null;
  colorHex: string | null;
  images: ImageItem[];
  sizes: SizeItem[];
}

export interface ProductDetailResponse {
  id: number;
  slug: string;
  name: string;
  description: string | null;
  gender: string;
  upperMaterial: string | null;
  soleType: string | null;
  closureType: string | null;
  shaftStyle: string | null;
  minPrice: number | null;
  maxPrice: number | null;
  newArrival: boolean;
  onSale: boolean;
  soldCount: number;
  viewCount: number;
  brand: BrandBrief & { description: string | null };
  colors: ColorDetail[];
}

export function formatPrice(n: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(n);
}

export type Filters = {
  search: string;
  gender: string;
  brandSlugs: string[];
  categorySlugs: string[];
  minPrice: string;
  maxPrice: string;
  newArrival: boolean;
  onSale: boolean;
};

export const EMPTY_FILTERS: Filters = {
  search: '',
  gender: '',
  brandSlugs: [],
  categorySlugs: [],
  minPrice: '',
  maxPrice: '',
  newArrival: false,
  onSale: false,
};
