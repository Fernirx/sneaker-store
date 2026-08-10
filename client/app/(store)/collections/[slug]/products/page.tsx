import { notFound } from 'next/navigation';
import Link from 'next/link';
import { publicAxios } from '@/lib/axios/serverAxios';
import { collectionUrl } from '@/lib/cloudinaryUrl';
import RichText from '@/components/RichText';
import SlugProductsClient from '../../../_components/SlugProductsClient';
import { type PageData } from '../../../products/_components/types';

interface CollectionFull {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  imagePublicId: string | null;
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

export default async function CollectionProductsPage({ params, searchParams }: Props) {
  const { slug } = await params;
  const sp = await searchParams;
  const initialFilters = buildFiltersFromSearch(sp);

  const [collectionRes, productsRes] = await Promise.allSettled([
    publicAxios.get<{ data: CollectionFull }>(`/collections/${slug}`),
    publicAxios.get<PageData>(`/collections/${slug}/products?${filtersToQuery(initialFilters)}`),
  ]);

  if (collectionRes.status === 'rejected') notFound();

  const collection: CollectionFull = (collectionRes as PromiseFulfilledResult<{ data: { data: CollectionFull } }>).value.data.data;
  const name = collection.name;
  const description = collection.description;

  const initialData: PageData =
    productsRes.status === 'fulfilled'
      ? productsRes.value.data
      : { data: [], meta: { page: 0, size: 20, totalElements: 0, totalPages: 0, last: true } };

  return (
    <>
      <div className="border-b border-line">
        {collection.imagePublicId && (
          <div className="h-40 md:h-52 overflow-hidden">
            <img
              src={collectionUrl(collection.imagePublicId, 1200, 400)}
              alt={name}
              className="w-full h-full object-cover"
            />
          </div>
        )}
        <div className="max-w-7xl mx-auto px-4 py-6">
          <nav className="text-xs text-white/60 mb-6 flex items-center gap-1.5">
            <Link href="/" className="hover:text-white transition-colors">{"Trang chủ"}</Link>
            <span>/</span>
            <Link href="/products" className="hover:text-white transition-colors">{"Sản phẩm"}</Link>
            <span>/</span>
            <span className="text-ink font-medium">{name}</span>
          </nav>
          <h1 className="font-display font-black text-2xl uppercase tracking-tight">{name}</h1>
          {description && (
            <RichText html={description} className="text-sm text-muted mt-1 max-w-lg" />
          )}
        </div>
      </div>

      <SlugProductsClient
        baseApiUrl={`/api/collections/${slug}/products`}
        initialData={initialData}
        initialFilters={initialFilters}
      />
    </>
  );
}
