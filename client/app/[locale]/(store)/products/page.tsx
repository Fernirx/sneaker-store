import { publicAxios } from '@/lib/axios/serverAxios';
import ProductsClient from './_components/ProductsClient';
import { type PageData, type Filters, type CategoryBrief, EMPTY_FILTERS } from './_components/types';

type SearchParams = Promise<{ [key: string]: string | string[] | undefined }>;

function buildFiltersFromSearch(sp: { [key: string]: string | string[] | undefined }): Partial<Filters> {
  const str = (v: string | string[] | undefined) => (typeof v === 'string' ? v : '');
  const arr = (v: string | string[] | undefined): string[] => {
    if (!v) return [];
    return Array.isArray(v) ? v : [v];
  };
  return {
    search:        str(sp.search),
    gender:        str(sp.gender),
    brandSlugs:    arr(sp.brandSlugs),
    categorySlugs: arr(sp.categorySlugs),
    minPrice:      str(sp.minPrice),
    maxPrice:      str(sp.maxPrice),
    newArrival:    sp.newArrival === 'true',
    onSale:        sp.onSale === 'true',
  };
}

function filtersToQuery(f: Partial<Filters>): string {
  const p = new URLSearchParams();
  p.set('page', '0');
  p.set('size', '20');
  p.set('sort', 'createdAt,desc');
  if (f.search)     p.set('search', f.search);
  if (f.gender)     p.set('gender', f.gender);
  if (f.minPrice)   p.set('minPrice', f.minPrice);
  if (f.maxPrice)   p.set('maxPrice', f.maxPrice);
  if (f.newArrival) p.set('newArrival', 'true');
  if (f.onSale)     p.set('onSale', 'true');
  f.brandSlugs?.forEach(s => p.append('brandSlugs', s));
  f.categorySlugs?.forEach(s => p.append('categorySlugs', s));
  return p.toString();
}

export default async function ProductsPage({ searchParams }: { searchParams: SearchParams }) {
  const sp = await searchParams;
  const initialFilters = buildFiltersFromSearch(sp);

  const [productsRes, brandsRes, categoriesRes] = await Promise.allSettled([
    publicAxios.get<PageData>(`/products?${filtersToQuery(initialFilters)}`),
    publicAxios.get('/brands?page=0&size=100&sort=name,asc'),
    publicAxios.get('/categories?page=0&size=100&sort=displayOrder,asc'),
  ]);

  const initialData: PageData =
    productsRes.status === 'fulfilled'
      ? productsRes.value.data
      : { data: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

  const brands = brandsRes.status === 'fulfilled' ? (brandsRes.value.data.data ?? []) : [];
  const categories: CategoryBrief[] =
    categoriesRes.status === 'fulfilled' ? (categoriesRes.value.data.data ?? []) : [];

  return (
    <ProductsClient
      initialData={initialData}
      initialFilters={{ ...EMPTY_FILTERS, ...initialFilters }}
      brands={brands}
      categories={categories}
    />
  );
}
