import { notFound } from 'next/navigation';
import { Link } from '@/i18n/routing';
import { publicAxios } from '@/lib/axios/serverAxios';
import { categoryUrl } from '@/lib/cloudinaryUrl';
import SlugProductsClient from '../../../_components/SlugProductsClient';
import { type PageData } from '../../../products/_components/types';

interface CategoryFull {
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

export default async function CategoryProductsPage({ params, searchParams }: Props) {
  const { slug } = await params;
  const sp = await searchParams;

  const [categoryRes, productsRes, brandsRes] = await Promise.allSettled([
    publicAxios.get<{ data: CategoryFull }>(`/categories/${slug}`),
    publicAxios.get<PageData>(`/categories/${slug}/products?${buildQuery(sp)}`),
    publicAxios.get('/brands?page=0&size=100&sort=name,asc'),
  ]);

  if (categoryRes.status === 'rejected') notFound();

  const category: CategoryFull = (categoryRes as PromiseFulfilledResult<{ data: { data: CategoryFull } }>).value.data.data;

  const initialData: PageData =
    productsRes.status === 'fulfilled'
      ? productsRes.value.data
      : { data: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };

  const brands = brandsRes.status === 'fulfilled' ? (brandsRes.value.data.data ?? []) : [];

  return (
    <>
      <div className="border-b border-line">
        {category.imagePublicId && (
          <div className="h-40 md:h-52 overflow-hidden">
            <img
              src={categoryUrl(category.imagePublicId, 1200, 400)}
              alt={category.name}
              className="w-full h-full object-cover"
            />
          </div>
        )}
        <div className="max-w-7xl mx-auto px-4 py-6">
          <nav className="text-xs text-muted mb-3 flex items-center gap-1.5">
            <Link href="/" className="hover:text-ink transition-colors">Home</Link>
            <span>/</span>
            <Link href="/products" className="hover:text-ink transition-colors">Sản phẩm</Link>
            <span>/</span>
            <span className="text-ink font-medium">{category.name}</span>
          </nav>
          <h1 className="font-display font-black text-2xl uppercase tracking-tight">{category.name}</h1>
          {category.description && (
            <p className="text-sm text-muted mt-1 max-w-lg">{category.description}</p>
          )}
        </div>
      </div>

      <SlugProductsClient
        baseApiUrl={`/api/categories/${slug}/products`}
        initialData={initialData}
        brands={brands}
      />
    </>
  );
}
