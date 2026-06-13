'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import { useTranslations } from 'next-intl';
import { useRouter, usePathname } from '@/i18n/routing';
import clientAxios from '@/lib/axios/clientAxios';
import ProductCard from './ProductCard';
import {
  type PageData, type Filters, type CategoryBrief,
  EMPTY_FILTERS,
} from './types';

interface BrandOption { id: number; name: string; slug: string; }

const GENDERS: { value: string; labelKey: 'genderMen' | 'genderWomen' | 'genderUnisex' | 'genderKids' }[] = [
  { value: 'MEN',    labelKey: 'genderMen' },
  { value: 'WOMEN',  labelKey: 'genderWomen' },
  { value: 'UNISEX', labelKey: 'genderUnisex' },
  { value: 'KIDS',   labelKey: 'genderKids' },
];

export default function ProductsClient({
  initialData,
  initialFilters,
  brands,
  categories,
}: {
  initialData: PageData;
  initialFilters: Partial<Filters>;
  brands: BrandOption[];
  categories: CategoryBrief[];
}) {
  const t = useTranslations('products');
  const router = useRouter();
  const pathname = usePathname();

  const [filters, setFilters] = useState<Filters>({
    ...EMPTY_FILTERS,
    ...initialFilters,
  });
  const [pendingSearch, setPendingSearch] = useState(initialFilters.search ?? '');
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
    if (f.minPrice)   p.set('minPrice', f.minPrice);
    if (f.maxPrice)   p.set('maxPrice', f.maxPrice);
    if (f.newArrival) p.set('newArrival', 'true');
    if (f.onSale)     p.set('onSale', 'true');
    f.brandSlugs.forEach(s => p.append('brandSlugs', s));
    f.categorySlugs.forEach(s => p.append('categorySlugs', s));
    return p;
  }, []);

  const syncUrl = useCallback((f: Filters) => {
    const p = new URLSearchParams();
    if (f.search)     p.set('search', f.search);
    if (f.gender)     p.set('gender', f.gender);
    if (f.minPrice)   p.set('minPrice', f.minPrice);
    if (f.maxPrice)   p.set('maxPrice', f.maxPrice);
    if (f.newArrival) p.set('newArrival', 'true');
    if (f.onSale)     p.set('onSale', 'true');
    f.brandSlugs.forEach(s => p.append('brandSlugs', s));
    f.categorySlugs.forEach(s => p.append('categorySlugs', s));
    const qs = p.toString();
    router.replace(`${pathname}${qs ? `?${qs}` : ''}` as never, { scroll: false });
  }, [router, pathname]);

  async function fetchProducts(f: Filters, page: number) {
    setLoading(true);
    try {
      const { data } = await clientAxios.get(`/api/products?${buildQuery(f, page)}`);
      setPageData(data);
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
    searchTimer.current = setTimeout(() => {
      setFilters(f => ({ ...f, search: val }));
    }, 350);
  }

  function toggleBrand(slug: string) {
    setFilters(f => ({
      ...f,
      brandSlugs: f.brandSlugs.includes(slug)
        ? f.brandSlugs.filter(s => s !== slug)
        : [...f.brandSlugs, slug],
    }));
  }

  function toggleCategory(slug: string) {
    setFilters(f => ({
      ...f,
      categorySlugs: f.categorySlugs.includes(slug)
        ? f.categorySlugs.filter(s => s !== slug)
        : [...f.categorySlugs, slug],
    }));
  }

  function setGender(g: string) {
    setFilters(f => ({ ...f, gender: f.gender === g ? '' : g }));
  }


  function clearFilters() {
    setPendingSearch('');
    setFilters({ ...EMPTY_FILTERS });
  }

  const isFiltered =
    filters.search || filters.gender || filters.brandSlugs.length ||
    filters.categorySlugs.length || filters.minPrice || filters.maxPrice ||
    filters.newArrival || filters.onSale;

  const FilterPanel = (
    <div className="space-y-6">
      {/* Gender */}
      <div>
        <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterGender')}</p>
        <div className="flex flex-wrap gap-1.5">
          {GENDERS.map(g => (
            <button
              key={g.value}
              onClick={() => setGender(g.value)}
              className={`px-3 py-1 text-xs font-bold rounded-sm border transition-colors ${
                filters.gender === g.value
                  ? 'bg-ink text-white border-ink'
                  : 'border-line text-muted hover:border-ink hover:text-ink'
              }`}
            >
              {t(g.labelKey)}
            </button>
          ))}
        </div>
      </div>

      {/* Brands */}
      {brands.length > 0 && (
        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterBrand')}</p>
          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
            {brands.map(b => (
              <label key={b.id} className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={filters.brandSlugs.includes(b.slug)}
                  onChange={() => toggleBrand(b.slug)}
                  className="accent-accent w-3.5 h-3.5"
                />
                <span className="text-sm">{b.name}</span>
              </label>
            ))}
          </div>
        </div>
      )}

      {/* Categories */}
      {categories.length > 0 && (
        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterCategory')}</p>
          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
            {categories.map(c => (
              <label key={c.id} className="flex items-center gap-2 cursor-pointer select-none">
                <input
                  type="checkbox"
                  checked={filters.categorySlugs.includes(c.slug)}
                  onChange={() => toggleCategory(c.slug)}
                  className="accent-accent w-3.5 h-3.5"
                />
                <span className="text-sm">{c.name}</span>
              </label>
            ))}
          </div>
        </div>
      )}

      {/* Price */}
      <div>
        <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">{t('filterPrice')}</p>
        <div className="flex gap-2 items-center">
          <input
            type="number"
            placeholder={t('priceMin')}
            value={filters.minPrice}
            onChange={e => setFilters(f => ({ ...f, minPrice: e.target.value }))}
            min={0}
            className="w-full border border-line rounded-sm px-2 py-1.5 text-sm focus:outline-none focus:border-ink"
          />
          <span className="text-muted text-sm flex-shrink-0">—</span>
          <input
            type="number"
            placeholder={t('priceMax')}
            value={filters.maxPrice}
            onChange={e => setFilters(f => ({ ...f, maxPrice: e.target.value }))}
            min={0}
            className="w-full border border-line rounded-sm px-2 py-1.5 text-sm focus:outline-none focus:border-ink"
          />
        </div>
      </div>

      {/* Toggles */}
      <div className="space-y-2">
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={filters.newArrival}
            onChange={e => setFilters(f => ({ ...f, newArrival: e.target.checked }))}
            className="accent-accent w-3.5 h-3.5"
          />
          <span className="text-sm">{t('filterNewArrival')}</span>
        </label>
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input
            type="checkbox"
            checked={filters.onSale}
            onChange={e => setFilters(f => ({ ...f, onSale: e.target.checked }))}
            className="accent-accent w-3.5 h-3.5"
          />
          <span className="text-sm">{t('filterOnSale')}</span>
        </label>
      </div>

      {isFiltered && (
        <button
          onClick={clearFilters}
          className="text-xs font-bold text-accent hover:underline"
        >
          {t('clearFilter')}
        </button>
      )}
    </div>
  );

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header row */}
      <div className="flex items-center justify-between mb-6">
        <h1 className="font-display font-black text-2xl uppercase tracking-tight">
          {t('title')}
        </h1>
        <div className="flex items-center gap-3">
          {/* Search — desktop */}
          <input
            type="text"
            value={pendingSearch}
            onChange={e => handleSearchChange(e.target.value)}
            placeholder="Search..."
            className="hidden md:block border border-line rounded-sm px-3 py-2 text-sm w-52 focus:outline-none focus:border-ink"
          />
          {/* Mobile filter toggle */}
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

      {/* Search mobile */}
      <div className="md:hidden mb-4">
        <input
          type="text"
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          placeholder="Search..."
          className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        />
      </div>

      <div className="flex gap-8">
        {/* Sidebar — desktop */}
        <aside className="hidden md:block w-52 flex-shrink-0">
          {FilterPanel}
        </aside>

        {/* Mobile sidebar drawer */}
        {sidebarOpen && (
          <div className="md:hidden fixed inset-0 z-40 flex">
            <div className="absolute inset-0 bg-black/40" onClick={() => setSidebarOpen(false)} />
            <div className="relative ml-auto w-72 max-w-full bg-white h-full overflow-y-auto p-5 space-y-1">
              <div className="flex items-center justify-between mb-4">
                <span className="font-display font-black text-sm uppercase tracking-wide">{t('filter')}</span>
                <button onClick={() => setSidebarOpen(false)} className="text-xl text-muted leading-none">&times;</button>
              </div>
              {FilterPanel}
            </div>
          </div>
        )}

        {/* Main */}
        <div className="flex-1 min-w-0">
          <p className="text-xs text-muted mb-4">
            {t('totalItems', { count: pageData.totalElements })}
          </p>

          {pageData.data.length === 0 ? (
            <div className="py-20 text-center">
              <p className="text-muted">{t('noProducts')}</p>
            </div>
          ) : (
            <div
              className={`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 transition-opacity duration-150 ${loading ? 'opacity-50 pointer-events-none' : ''}`}
            >
              {pageData.data.map(p => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          )}

          {/* Pagination */}
          {pageData.totalPages > 1 && (
            <div className="mt-10 flex items-center justify-center gap-3">
              <button
                disabled={currentPage === 0}
                onClick={() => setCurrentPage(p => p - 1)}
                className="px-4 py-2 border border-line rounded-sm text-sm font-bold disabled:opacity-40 hover:bg-paper transition-colors"
              >
                {t('prev')}
              </button>
              <span className="text-sm text-muted">
                {currentPage + 1} / {pageData.totalPages}
              </span>
              <button
                disabled={currentPage >= pageData.totalPages - 1}
                onClick={() => setCurrentPage(p => p + 1)}
                className="px-4 py-2 border border-line rounded-sm text-sm font-bold disabled:opacity-40 hover:bg-paper transition-colors"
              >
                {t('next')}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
