import { notFound } from 'next/navigation';
import { Link } from '@/i18n/routing';
import { publicAxios } from '@/lib/axios/serverAxios';
import { brandUrl } from '@/lib/cloudinaryUrl';
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

function buildQuery(sp: { [key: string]: string | string[] | undefined }) {
  const str = (v: string | string[] | undefined) => (typeof v === 'string' ? v : '');
  const p = new URLSearchParams();
  p.set('page', '0'); p.set('size', '20'); p.set('sort', 'createdAt,desc');
  const search = str(sp.search); if (search) p.set('search', search);
  const gender = str(sp.gender); if (gender) p.set('gender', gender);
  const min = str(sp.minPrice); if (min) p.set('minPrice', min);
  const max = str(sp.maxPrice); if (max) p.set('maxPrice', max);
  if (sp.newArrival === 'true') p.set('newArrival', 'true');
  if (sp.onSale === 'true') p.set('onSale', 'true');
  return p.toString();
}

export default async function BrandProductsPage({ params, searchParams }: Props) {
  const { slug } = await params;
  const sp = await searchParams;

  const [brandRes, productsRes] = await Promise.allSettled([
    publicAxios.get<{ data: BrandFull }>(`/brands/${slug}`),
    publicAxios.get<PageData>(`/brands/${slug}/products?${buildQuery(sp)}`),
  ]);

  if (brandRes.status === 'rejected') notFound();

  const brand: BrandFull = (brandRes as PromiseFulfilledResult<{ data: { data: BrandFull } }>).value.data.data;

  const initialData: PageData =
    productsRes.status === 'fulfilled'
      ? productsRes.value.data
      : { data: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

  return (
    <>
      <div className="border-b border-line bg-paper">
        <div className="max-w-7xl mx-auto px-4 py-8">
          <nav className="text-xs text-muted mb-4 flex items-center gap-1.5">
            <Link href="/" className="hover:text-ink transition-colors">Home</Link>
            <span>/</span>
            <Link href="/products" className="hover:text-ink transition-colors">Sản phẩm</Link>
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
                <p className="text-sm text-muted mt-1 max-w-lg">{brand.description}</p>
              )}
            </div>
          </div>
        </div>
      </div>

      <SlugProductsClient
        baseApiUrl={`/api/brands/${slug}/products`}
        initialData={initialData}
      />
    </>
  );
}
