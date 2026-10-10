import { notFound } from 'next/navigation';
import Link from 'next/link';
import { publicAxios } from '@/lib/axios/serverAxios';
import { brandUrl } from '@/lib/cloudinaryUrl';
import RichText from '@/components/RichText';
import SlugProductsClient from '../../../_components/SlugProductsClient';
import { type PageData } from '../../../products/_components/types';

interface BrandFull {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  logoPublicId: string | null;
}

type Props = {
  params: Promise<{ slug: string }>;
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
};

function buildFiltersFromSearch(sp: { [key: string]: string | string[] | undefined }) {
  const str = (v: string | string[] | undefined) => (typeof v === 'string' ? v : '');
  const arr = (v: string | string[] | undefined): string[] => {
    if (!v) return [];
    return Array.isArray(v) ? v : [v];
  };
  return {
    search:        str(sp.search),
    gender:        str(sp.gender),
    brandSlugs:    arr(sp.brandSlugs),
    sizes:         arr(sp.sizes).map(Number).filter(n => !isNaN(n)),
    minPrice:      str(sp.minPrice),
    maxPrice:      str(sp.maxPrice),
    newArrival:    sp.newArrival === 'true',
    onSale:        sp.onSale === 'true',
  };
}

function filtersToQuery(f: any): string {
  const p = new URLSearchParams();
  p.set('page', '0'); p.set('size', '20'); p.set('sort', 'createdAt,desc');
  if (f.search)     p.set('search', f.search);
  if (f.gender)     p.set('gender', f.gender);
  if (f.minPrice)   p.set('minPrice', f.minPrice);
  if (f.maxPrice)   p.set('maxPrice', f.maxPrice);
  if (f.newArrival) p.set('newArrival', 'true');
  if (f.onSale)     p.set('onSale', 'true');
  f.brandSlugs?.forEach((s: string) => p.append('brandSlugs', s));
  f.sizes?.forEach((s: number) => p.append('sizes', String(s)));
  return p.toString();
}

export default async function BrandProductsPage({ params, searchParams }: Props) {
  const { slug } = await params;
  const sp = await searchParams;
  const initialFilters = buildFiltersFromSearch(sp);

  const [brandRes, productsRes] = await Promise.allSettled([
    publicAxios.get<{ data: BrandFull }>(`/brands/${slug}`),
    publicAxios.get<PageData>(`/brands/${slug}/products?${filtersToQuery(initialFilters)}`),
  ]);

  if (brandRes.status === 'rejected') notFound();

  const brand: BrandFull = (brandRes as PromiseFulfilledResult<{ data: { data: BrandFull } }>).value.data.data;

  const initialData: PageData =
    productsRes.status === 'fulfilled'
      ? productsRes.value.data
      : { data: [], meta: { page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } };

  return (
    <>
      <div className="border-b border-line bg-paper">
        <div className="max-w-7xl mx-auto px-4 py-8">
          <nav className="text-xs text-muted mb-4 flex items-center gap-1.5">
            <Link href="/" className="hover:text-ink transition-colors">{"Trang chủ"}</Link>
            <span>/</span>
            <Link href="/products" className="hover:text-ink transition-colors">{"Sản phẩm"}</Link>
            <span>/</span>
            <span className="text-ink font-medium">{brand.name}</span>
          </nav>
          <div className="flex items-center gap-6">
            {brand.logoPublicId && (
              <div className="flex-shrink-0 w-24 h-16 flex items-center justify-center border border-line rounded-sm bg-white p-2">
                <img
                  src={brandUrl(brand.logoPublicId, 160, 80)}
                  alt={brand.name}
                  className="max-h-full max-w-full object-contain"
                />
              </div>
            )}
            <div>
              <h1 className="font-display font-black text-2xl uppercase tracking-tight">{brand.name}</h1>
              {brand.description && (
                <RichText html={brand.description} className="text-sm text-muted mt-1 max-w-lg" />
              )}
            </div>
          </div>
        </div>
      </div>

      <SlugProductsClient
        baseApiUrl={`/api/brands/${slug}/products`}
        initialData={initialData}
        initialFilters={initialFilters}
      />
    </>
  );
}
