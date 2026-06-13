'use client';

import { useState, useRef } from 'react';
import { useTranslations } from 'next-intl';
import { Link } from '@/i18n/routing';

interface BrandItem {
  id: number;
  name: string;
  slug: string;
}

interface CategoryItem {
  id: number;
  name: string;
  slug: string;
  parentId: number | null;
}

function ChevronDown() {
  return (
    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="6 9 12 15 18 9" />
    </svg>
  );
}

export default function HeaderNav({ brands, categories }: { brands: BrandItem[]; categories: CategoryItem[] }) {
  const t = useTranslations('header');
  const [open, setOpen] = useState<'brands' | 'categories' | null>(null);
  const closeTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  function enter(menu: 'brands' | 'categories') {
    if (closeTimer.current) clearTimeout(closeTimer.current);
    setOpen(menu);
  }

  function leave() {
    closeTimer.current = setTimeout(() => setOpen(null), 120);
  }

  // Build category tree: parents first, children grouped under their parent
  const parents = categories.filter(c => c.parentId == null);
  const childrenOf = (parentId: number) => categories.filter(c => c.parentId === parentId);

  return (
    <nav className="hidden md:flex items-center gap-1">
      <Link
        href="/products"
        className="px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors rounded"
      >
        {t('navProducts')}
      </Link>

      {/* Brands dropdown */}
      {brands.length > 0 && (
        <div className="relative" onMouseEnter={() => enter('brands')} onMouseLeave={leave}>
          <button className="flex items-center gap-1 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors rounded">
            {t('navBrands')}
            <ChevronDown />
          </button>

          {open === 'brands' && (
            <div className="absolute top-full left-0 mt-1 bg-white border border-line shadow-lg rounded-sm min-w-[160px] py-1.5 z-50">
              {brands.map(b => (
                <Link
                  key={b.id}
                  href={`/brands/${b.slug}/products` as never}
                  className="block px-4 py-2 text-sm font-medium text-muted hover:text-ink hover:bg-paper transition-colors"
                  onClick={() => setOpen(null)}
                >
                  {b.name}
                </Link>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Categories dropdown — hierarchical */}
      {categories.length > 0 && (
        <div className="relative" onMouseEnter={() => enter('categories')} onMouseLeave={leave}>
          <button className="flex items-center gap-1 px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider text-muted hover:text-ink transition-colors rounded">
            {t('navCategories')}
            <ChevronDown />
          </button>

          {open === 'categories' && (
            <div className="absolute top-full left-0 mt-1 bg-white border border-line shadow-lg rounded-sm min-w-[200px] py-2 z-50">
              {parents.map((parent, i) => {
                const children = childrenOf(parent.id);
                return (
                  <div key={parent.id}>
                    {i > 0 && <div className="my-1 mx-3 border-t border-line" />}
                    <Link
                      href={`/categories/${parent.slug}/products` as never}
                      className="block px-4 py-1.5 text-[11px] font-black uppercase tracking-wider text-ink hover:text-accent transition-colors"
                      onClick={() => setOpen(null)}
                    >
                      {parent.name}
                    </Link>
                    {children.map(child => (
                      <Link
                        key={child.id}
                        href={`/categories/${child.slug}/products` as never}
                        className="block pl-7 pr-4 py-1.5 text-sm text-muted hover:text-ink hover:bg-paper transition-colors"
                        onClick={() => setOpen(null)}
                      >
                        {child.name}
                      </Link>
                    ))}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </nav>
  );
}
