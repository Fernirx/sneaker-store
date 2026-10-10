import Link from 'next/link';
import { publicAxios } from '@/lib/axios/serverAxios';
import { brandUrl, collectionUrl, bannerUrl } from '@/lib/cloudinaryUrl';
import ProductCard from './products/_components/ProductCard';
import { type PageData, type ProductResponse } from './products/_components/types';

interface HomeBrand {
  id: number;
  name: string;
  slug: string;
  logoPublicId: string | null;
}

interface HomeCollection {
  id: number;
  name: string;
  slug: string;
  imagePublicId: string | null;
}

interface HomeBanner {
  id: number;
  title: string;
  imagePublicId: string | null;
  linkUrl: string | null;
}

function SectionHeader({ title, href, viewAllLabel }: { title: string; href: string; viewAllLabel: string }) {
  return (
    <div className="flex items-center justify-between mb-5">
      <h2 className="font-display font-black text-xl md:text-2xl uppercase tracking-tight">{title}</h2>
      <Link href={href} className="text-xs font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors">
        {viewAllLabel} →
      </Link>
    </div>
  );
}

export default async function HomePage() {
  const [newArrivalsRes, onSaleRes, collectionsRes, brandsRes, bannersRes] = await Promise.allSettled([
    publicAxios.get<PageData>('/products?newArrival=true&size=8&sort=createdAt,desc'),
    publicAxios.get<PageData>('/products?onSale=true&size=8&sort=createdAt,desc'),
    publicAxios.get<{ data: HomeCollection[] }>('/collections?page=0&size=4&sort=launchDate,desc'),
    publicAxios.get<{ data: HomeBrand[] }>('/brands?page=0&size=10&sort=name,asc'),
    publicAxios.get<{ data: HomeBanner[] }>('/banners'),
  ]);

  const newArrivals: ProductResponse[] =
    newArrivalsRes.status === 'fulfilled' ? newArrivalsRes.value.data.data : [];
  const onSale: ProductResponse[] =
    onSaleRes.status === 'fulfilled' ? onSaleRes.value.data.data : [];
  const collections: HomeCollection[] =
    collectionsRes.status === 'fulfilled' ? collectionsRes.value.data.data : [];
  const brands: HomeBrand[] =
    brandsRes.status === 'fulfilled' ? brandsRes.value.data.data : [];
  const banners: HomeBanner[] =
    bannersRes.status === 'fulfilled' ? bannersRes.value.data.data : [];

  return (
    <>
      {/* Hero */}
      <section className="bg-ink text-white">
        <div className="max-w-7xl mx-auto px-6 py-20 md:py-28">
          <h1 className="font-display font-black text-4xl md:text-6xl uppercase tracking-tight leading-[0.95] whitespace-pre-line">
            {`SẴN SÀNG
BỨT TỐC.`}
          </h1>
          <p className="text-white/60 text-sm md:text-base mt-5 max-w-md">{"Khám phá những đôi sneaker mới nhất, chính hãng 100%, giao nhanh toàn quốc."}</p>
          <div className="flex items-center gap-5 mt-8">
            <Link
              href="/products"
              className="bg-accent hover:bg-accent-700 text-white font-display font-bold text-sm uppercase tracking-wider px-6 py-3.5 rounded-sm transition-colors"
            >
              {"Mua ngay"}
            </Link>
            <Link href="/products?newArrival=true" className="text-sm text-white/70 hover:text-white underline transition-colors">
              {"Xem hàng mới về"}
            </Link>
          </div>
        </div>
      </section>

      {/* Banners */}
      {banners.length > 0 && (
        <section className="max-w-7xl mx-auto px-6 pt-8">
          <div className="flex gap-4 overflow-x-auto snap-x snap-mandatory pb-1">
            {banners.map(b => (
              <Link
                key={b.id}
                href={b.linkUrl || '#'}
                className="group relative shrink-0 w-full snap-start aspect-[16/5] rounded-sm overflow-hidden bg-paper border border-line block"
              >
                {b.imagePublicId ? (
                  <img
                    src={bannerUrl(b.imagePublicId, 1600, 500)}
                    alt={b.title}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-muted text-sm">{b.title}</div>
                )}
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* New Arrivals */}
      {newArrivals.length > 0 && (
        <section className="max-w-7xl mx-auto px-6 py-12">
          <SectionHeader title={"Hàng mới về"} href="/products?newArrival=true" viewAllLabel={"Xem tất cả"} />
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {newArrivals.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}

      {/* On Sale */}
      {onSale.length > 0 && (
        <section className="max-w-7xl mx-auto px-6 py-12">
          <SectionHeader title={"Đang giảm giá"} href="/products?onSale=true" viewAllLabel={"Xem tất cả"} />
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {onSale.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        </section>
      )}

      {/* Featured Collections */}
      {collections.length > 0 && (
        <section className="max-w-7xl mx-auto px-6 py-12">
          <SectionHeader title={"Bộ sưu tập nổi bật"} href="/products" viewAllLabel={"Xem tất cả"} />
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {collections.map(c => (
              <Link
                key={c.id}
                href={`/collections/${c.slug}/products`}
                className="group relative aspect-[16/9] rounded-sm overflow-hidden bg-paper border border-line block"
              >
                {c.imagePublicId ? (
                  <img
                    src={collectionUrl(c.imagePublicId, 800, 450)}
                    alt={c.name}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-muted text-sm">—</div>
                )}
                <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/10 to-transparent" />
                <div className="absolute bottom-0 left-0 p-5">
                  <span className="font-display font-black text-lg md:text-xl uppercase tracking-tight text-white">
                    {c.name}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* Shop by Brand */}
      {brands.length > 0 && (
        <section className="bg-paper border-t border-line">
          <div className="max-w-7xl mx-auto px-6 py-12">
            <SectionHeader title={"Thương hiệu"} href="/products" viewAllLabel={"Xem tất cả"} />
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4">
              {brands.map(b => (
                <Link
                  key={b.id}
                  href={`/brands/${b.slug}/products`}
                  className="flex items-center justify-center h-20 border border-line rounded-sm bg-white p-4 hover:border-ink transition-colors"
                >
                  {b.logoPublicId ? (
                    <img
                      src={brandUrl(b.logoPublicId, 160, 64)}
                      alt={b.name}
                      className="max-h-full max-w-full object-contain"
                    />
                  ) : (
                    <span className="font-display font-bold text-sm uppercase tracking-wide text-ink-2">{b.name}</span>
                  )}
                </Link>
              ))}
            </div>
          </div>
        </section>
      )}
    </>
  );
}
