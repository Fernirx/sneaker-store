import { notFound } from 'next/navigation';
import { cookies } from 'next/headers';
import { getTranslations } from 'next-intl/server';
import { Link } from '@/i18n/routing';
import { publicAxios } from '@/lib/axios/serverAxios';
import { collectionUrl } from '@/lib/cloudinaryUrl';
import SlugProductsClient from '../../../_components/SlugProductsClient';
import { type PageData } from '../../../products/_components/types';

interface CollectionTranslation {
  locale: string;
  name: string;
  description: string | null;
}

interface CollectionFull {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  imagePublicId: string | null;
  translations: CollectionTranslation[];
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

export default async function CollectionProductsPage({ params, searchParams }: Props) {
  const { slug } = await params;
  const sp = await searchParams;
  const t = await getTranslations('products');
  const store = await cookies();
  const locale = store.get('NEXT_LOCALE')?.value ?? 'vi';

  const [collectionRes, productsRes] = await Promise.allSettled([
    publicAxios.get<{ data: CollectionFull }>(`/collections/${slug}`),
    publicAxios.get<PageData>(`/collections/${slug}/products?${buildQuery(sp)}`),
  ]);

  if (collectionRes.status === 'rejected') notFound();

  const collection: CollectionFull = (collectionRes as PromiseFulfilledResult<{ data: { data: CollectionFull } }>).value.data.data;
  const translated = collection.translations?.find(tr => tr.locale === locale);
  const name = translated?.name ?? collection.name;
  const description = translated?.description ?? collection.description;

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
          <nav className="text-xs text-muted mb-3 flex items-center gap-1.5">
            <Link href="/" className="hover:text-ink transition-colors">{t('breadcrumbHome')}</Link>
            <span>/</span>
            <Link href="/products" className="hover:text-ink transition-colors">{t('breadcrumbProducts')}</Link>
            <span>/</span>
            <span className="text-ink font-medium">{name}</span>
          </nav>
          <h1 className="font-display font-black text-2xl uppercase tracking-tight">{name}</h1>
          {description && (
            <p className="text-sm text-muted mt-1 max-w-lg">{description}</p>
          )}
        </div>
      </div>

      <SlugProductsClient
        baseApiUrl={`/api/collections/${slug}/products`}
        initialData={initialData}
      />
    </>
  );
}
