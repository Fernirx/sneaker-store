'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { useTranslations } from 'next-intl';
import { useRouter, usePathname } from '@/i18n/routing';
import clientAxios from '@/lib/axios/clientAxios';
import ProductCard from '../products/_components/ProductCard';
import { type PageData } from '../products/_components/types';

interface BrandOption {
  id: number;
  name: string;
  slug: string;
}

interface Filters {
  search: string;
  gender: string;
  brandSlugs: string[];
  minPrice: string;
  maxPrice: string;
  newArrival: boolean;
  onSale: boolean;
}

const EMPTY: Filters = { search: '', gender: '', brandSlugs: [], minPrice: '', maxPrice: '', newArrival: false, onSale: false };

const GENDERS: { value: string; labelKey: 'genderMen' | 'genderWomen' | 'genderUnisex' | 'genderKids' }[] = [
  { value: 'MEN',    labelKey: 'genderMen' },
  { value: 'WOMEN',  labelKey: 'genderWomen' },
  { value: 'UNISEX', labelKey: 'genderUnisex' },
  { value: 'KIDS',   labelKey: 'genderKids' },
];

export default function SlugProductsClient({
  baseApiUrl,
  initialData,
  initialFilters,
  brands,
}: {
  baseApiUrl: string;
  initialData: PageData;
  initialFilters?: Partial<Filters>;
  brands?: BrandOption[];
}) {
  const t = useTranslations('products');
  const router = useRouter();
  const pathname = usePathname();

  const [filters, setFilters] = useState<Filters>({ ...EMPTY, ...initialFilters });
  const [pendingSearch, setPendingSearch] = useState(initialFilters?.search ?? '');
  const [pageData, setPageData] = useState<PageData>(initialData);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const mounted = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  const buildQuery = useCallback((f: Filters, page: number) => {
    const p = new URLSearchParams();
    p.set('page', String(page));
    p.set('size', '20');
    p.set('sort', 'createdAt,desc');
    if (f.search)     p.set('search', f.search);
    if (f.gender)     p.set('gender', f.gender);
    f.brandSlugs.forEach(s => p.append('brandSlugs', s));
    if (f.minPrice)   p.set('minPrice', f.minPrice);
    if (f.maxPrice)   p.set('maxPrice', f.maxPrice);
    if (f.newArrival) p.set('newArrival', 'true');
    if (f.onSale)     p.set('onSale', 'true');
    return p;
  }, []);

  const syncUrl = useCallback((f: Filters) => {
    const p = new URLSearchParams();
    if (f.search)     p.set('search', f.search);
    if (f.gender)     p.set('gender', f.gender);
    f.brandSlugs.forEach(s => p.append('brandSlugs', s));
    if (f.minPrice)   p.set('minPrice', f.minPrice);
    if (f.maxPrice)   p.set('maxPrice', f.maxPrice);
    if (f.newArrival) p.set('newArrival', 'true');
    if (f.onSale)     p.set('onSale', 'true');
    const qs = p.toString();
    router.replace(`${pathname}${qs ? `?${qs}` : ''}` as never, { scroll: false });
  }, [router, pathname]);

  async function fetchProducts(f: Filters, page: number) {
    setLoading(true);
    try {
      const { data } = await clientAxios.get(`${baseApiUrl}?${buildQuery(f, page)}`);
      setPageData(data);
    } catch {
      // keep current data on error
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!mounted.current) { mounted.current = true; return; }
    setCurrentPage(0);
    syncUrl(filters);
    fetchProducts(filters, 0);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  useEffect(() => {
    if (!mounted.current) return;
    fetchProducts(filters, currentPage);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  function handleSearchChange(val: string) {
    setPendingSearch(val);
    if (searchTimer.current) clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => setFilters(f => ({ ...f, search: val })), 350);
  }

  const isFiltered = filters.search || filters.gender || filters.brandSlugs.length || filters.minPrice || filters.maxPrice || filters.newArrival || filters.onSale;

  const FilterPanel = (
    <div className="space-y-6">
      {/* Brand filter — only shown on category pages */}
      {brands && brands.length > 0 && (
        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterBrand')}</p>
          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
            {brands.map(b => (
              <label key={b.id} className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={filters.brandSlugs.includes(b.slug)}
                  onChange={() => setFilters(f => ({
                    ...f,
                    brandSlugs: f.brandSlugs.includes(b.slug)
                      ? f.brandSlugs.filter(s => s !== b.slug)
                      : [...f.brandSlugs, b.slug],
                  }))}
                  className="accent-accent w-3.5 h-3.5"
                />
                <span className="text-sm">{b.name}</span>
              </label>
            ))}
          </div>
        </div>
      )}

      <div>
        <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterGender')}</p>
        <div className="flex flex-wrap gap-1.5">
          {GENDERS.map(g => (
            <button
              key={g.value}
              onClick={() => setFilters(f => ({ ...f, gender: f.gender === g.value ? '' : g.value }))}
              className={`px-3 py-1 text-xs font-bold rounded-sm border transition-colors ${
                filters.gender === g.value ? 'bg-ink text-white border-ink' : 'border-line text-muted hover:border-ink hover:text-ink'
              }`}
            >
              {t(g.labelKey)}
            </button>
          ))}
        </div>
      </div>

      <div>
        <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterPrice')}</p>
        <div className="flex gap-2 items-center">
          <input type="number" placeholder={t('priceMin')} value={filters.minPrice} min={0}
            onChange={e => setFilters(f => ({ ...f, minPrice: e.target.value }))}
            className="w-full border border-line rounded-sm px-2 py-1.5 text-sm focus:outline-none focus:border-ink" />
          <span className="text-muted text-sm flex-shrink-0">—</span>
          <input type="number" placeholder={t('priceMax')} value={filters.maxPrice} min={0}
            onChange={e => setFilters(f => ({ ...f, maxPrice: e.target.value }))}
            className="w-full border border-line rounded-sm px-2 py-1.5 text-sm focus:outline-none focus:border-ink" />
        </div>
      </div>

      <div className="space-y-2">
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={filters.newArrival} className="accent-accent w-3.5 h-3.5"
            onChange={e => setFilters(f => ({ ...f, newArrival: e.target.checked }))} />
          <span className="text-sm">{t('filterNewArrival')}</span>
        </label>
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={filters.onSale} className="accent-accent w-3.5 h-3.5"
            onChange={e => setFilters(f => ({ ...f, onSale: e.target.checked }))} />
          <span className="text-sm">{t('filterOnSale')}</span>
        </label>
      </div>

      {isFiltered && (
        <button onClick={() => { setPendingSearch(''); setFilters({ ...EMPTY }); }}
          className="text-xs font-bold text-accent hover:underline">
          {t('clearFilter')}
        </button>
      )}
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 py-6">
      <div className="flex items-center justify-between mb-6">
        <p className="text-xs text-muted">{t('totalItems', { count: pageData.totalElements })}</p>
        <div className="flex items-center gap-3">
          <input type="text" value={pendingSearch} onChange={e => handleSearchChange(e.target.value)}
            placeholder="Search..."
            className="hidden md:block border border-line rounded-sm px-3 py-2 text-sm w-52 focus:outline-none focus:border-ink" />
          <button
            className="md:hidden flex items-center gap-1.5 border border-line rounded-sm px-3 py-2 text-sm font-bold"
            onClick={() => setSidebarOpen(v => !v)}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h18M7 8h10M11 12h2" />
            </svg>
            {t('filter')}
            {isFiltered && <span className="w-1.5 h-1.5 rounded-full bg-accent" />}
          </button>
        </div>
      </div>

      <div className="md:hidden mb-4">
        <input type="text" value={pendingSearch} onChange={e => handleSearchChange(e.target.value)}
          placeholder="Search..."
          className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
      </div>

      <div className="flex gap-8">
        <aside className="hidden md:block w-52 flex-shrink-0">{FilterPanel}</aside>

        {sidebarOpen && (
          <div className="md:hidden fixed inset-0 z-40 flex">
            <div className="absolute inset-0 bg-black/40" onClick={() => setSidebarOpen(false)} />
            <div className="relative ml-auto w-72 max-w-full bg-white h-full overflow-y-auto p-5">
              <div className="flex items-center justify-between mb-4">
                <span className="font-display font-black text-sm uppercase tracking-wide">{t('filter')}</span>
                <button onClick={() => setSidebarOpen(false)} className="text-xl text-muted leading-none">&times;</button>
              </div>
              {FilterPanel}
            </div>
          </div>
        )}

        <div className="flex-1 min-w-0">
          {pageData.data.length === 0 ? (
            <div className="py-20 text-center"><p className="text-muted">{t('noProducts')}</p></div>
          ) : (
            <div className={`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 transition-opacity duration-150 ${loading ? 'opacity-50 pointer-events-none' : ''}`}>
              {pageData.data.map(p => <ProductCard key={p.id} product={p} />)}
            </div>
          )}

          {pageData.totalPages > 1 && (
            <div className="mt-10 flex items-center justify-center gap-3">
              <button disabled={currentPage === 0} onClick={() => setCurrentPage(p => p - 1)}
                className="px-4 py-2 border border-line rounded-sm text-sm font-bold disabled:opacity-40 hover:bg-paper transition-colors">
                {t('prev')}
              </button>
              <span className="text-sm text-muted">{currentPage + 1} / {pageData.totalPages}</span>
              <button disabled={currentPage >= pageData.totalPages - 1} onClick={() => setCurrentPage(p => p + 1)}
                className="px-4 py-2 border border-line rounded-sm text-sm font-bold disabled:opacity-40 hover:bg-paper transition-colors">
                {t('next')}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
