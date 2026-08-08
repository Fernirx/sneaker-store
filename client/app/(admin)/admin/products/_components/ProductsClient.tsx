'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { productUrl } from '@/lib/cloudinaryUrl';
import {
  type ProductRow, type PageData,
  type VariantGroup,
  GENDER_OPTIONS, formatPrice, formatDate,
} from './types';
import { Fragment } from 'react';
import CreateProductModal from './CreateProductModal';
import DeleteProductModal from './DeleteProductModal';

interface BrandOption { id: number; name: string; }
type Filters = { search: string; brandId: string; gender: string; active: string; };

export default function ProductsClient({
  initialData,
  isAdmin,
}: {
  initialData: PageData;
  isAdmin: boolean;
}) {
  const router = useRouter();
  const [pageData, setPageData] = useState<PageData>(initialData);
  const [filters, setFilters] = useState<Filters>({ search: '', brandId: '', gender: '', active: '' });
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [brands, setBrands] = useState<BrandOption[]>([]);

  const [createOpen, setCreateOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<ProductRow | null>(null);

  const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());
  const [variantsCache, setVariantsCache] = useState<Record<number, VariantGroup[]>>({});
  const [loadingVariants, setLoadingVariants] = useState<Record<number, boolean>>({});

  const mounted = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    clientAxios.get('/api/admin/brands?page=0&size=100&sort=name,asc')
      .then(res => setBrands(res.data.data ?? []))
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!mounted.current) { mounted.current = true; return; }
    fetchData(currentPage, filters);
  }, [currentPage, filters.search, filters.brandId, filters.gender, filters.active]);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const p = new URLSearchParams();
      p.set('page', String(page));
      p.set('size', '20');
      p.set('sort', 'createdAt,desc');
      if (f.search)  p.set('search', f.search);
      if (f.brandId) p.set('brandId', f.brandId);
      if (f.gender)  p.set('gender', f.gender);
      if (f.active !== '') p.set('active', f.active);
      const { data } = await clientAxios.get(`/api/admin/products?${p}`);
      setPageData(data);
    } finally {
      setLoading(false);
    }
  }

  function handleSearchChange(val: string) {
    setPendingSearch(val);
    if (searchTimer.current) clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => {
      setFilters(f => ({ ...f, search: val }));
      setCurrentPage(0);
    }, 350);
  }

  function setFilter(key: keyof Filters, val: string) {
    setFilters(f => ({ ...f, [key]: val }));
    setCurrentPage(0);
  }

  async function toggleRow(productId: number) {
    const newSet = new Set(expandedRows);
    if (newSet.has(productId)) {
      newSet.delete(productId);
      setExpandedRows(newSet);
      return;
    }
    
    newSet.add(productId);
    setExpandedRows(newSet);

    if (!variantsCache[productId]) {
      setLoadingVariants(prev => ({ ...prev, [productId]: true }));
      try {
        const { data } = await clientAxios.get(`/api/admin/products/${productId}/variants`);
        setVariantsCache(prev => ({ ...prev, [productId]: data.data || [] }));
      } catch (error) {
        console.error(error);
      } finally {
        setLoadingVariants(prev => ({ ...prev, [productId]: false }));
      }
    }
  }

  const colCount = isAdmin ? 9 : 8;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Sản phẩm</h1>
        {isAdmin && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            Thêm sản phẩm
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <input
          type="text"
          placeholder="Tìm theo tên, mã..."
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm w-56 focus:outline-none focus:border-ink"
        />
        <select
          value={filters.brandId}
          onChange={e => setFilter('brandId', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả thương hiệu</option>
          {brands.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
        </select>
        <select
          value={filters.gender}
          onChange={e => setFilter('gender', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả giới tính</option>
          {GENDER_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
        <select
          value={filters.active}
          onChange={e => setFilter('active', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="true">Hoạt động</option>
          <option value="false">Ẩn</option>
        </select>
      </div>

      {/* Table */}
      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="w-8 px-2 py-3"></th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-16">Ảnh</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted max-w-[250px]">Tên</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Mã SP</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted max-w-[200px]">Slug</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-40 whitespace-nowrap">Khoảng giá</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-28 whitespace-nowrap">Trạng thái</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Ngày tạo</th>
              {isAdmin && <th className="px-4 py-3 w-28" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  Không có sản phẩm nào.
                </td>
              </tr>
            ) : (
              pageData.data.map(p => (
                <Fragment key={p.id}>
                  <tr className="border-b border-line-2 hover:bg-paper/50 transition-colors">
                    <td className="px-2 py-3 text-center">
                      <button onClick={() => toggleRow(p.id)} className="text-muted hover:text-ink w-6 h-6 flex items-center justify-center rounded bg-paper border border-line-2">
                        {expandedRows.has(p.id) ? (
                          <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7"></path></svg>
                        ) : (
                          <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24" strokeWidth="2"><path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7"></path></svg>
                        )}
                      </button>
                    </td>
                    <td className="px-4 py-3">
                    {p.primaryImagePublicId ? (
                      <img
                        src={productUrl(p.primaryImagePublicId, 56, 56)}
                        alt={p.name}
                        className="w-14 h-14 object-contain rounded-sm border border-line bg-paper"
                      />
                    ) : (
                      <div className="w-14 h-14 rounded-sm border border-line bg-paper flex items-center justify-center">
                        <span className="text-muted text-[10px]">—</span>
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-3 max-w-[250px]">
                    <p className="font-bold text-sm leading-snug truncate" title={p.name}>{p.name}</p>
                    <div className="flex gap-1 mt-1.5">
                      {p.newArrival && (
                        <span className="px-1.5 py-0.5 bg-accent/10 text-accent text-[9px] font-bold rounded">MỚI</span>
                      )}
                      {p.onSale && (
                        <span className="px-1.5 py-0.5 bg-danger/10 text-danger text-[9px] font-bold rounded">SALE</span>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm font-bold">{p.code}</td>
                  <td className="px-4 py-3 text-xs text-muted font-mono max-w-[200px] break-all">{p.slug}</td>
                  <td className="px-4 py-3 text-sm font-medium whitespace-nowrap">
                    {p.minPrice != null 
                      ? (p.maxPrice != null && p.maxPrice !== p.minPrice ? `${formatPrice(p.minPrice)} - ${formatPrice(p.maxPrice)}` : formatPrice(p.minPrice))
                      : <span className="text-muted font-normal text-xs">Chưa có giá</span>}
                  </td>
                  <td className="px-4 py-3 whitespace-nowrap">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${p.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'}`}>
                      {p.active ? 'Hoạt động' : 'Ẩn'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">
                    {formatDate(p.createdAt)}
                  </td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex gap-3 justify-end">
                        <button
                          onClick={() => router.push(`/admin/products/${p.id}`)}
                          className="text-xs font-bold text-muted hover:text-ink transition-colors"
                        >
                          Sửa
                        </button>
                        <button
                          onClick={() => setDeleteTarget(p)}
                          className="text-xs font-bold text-danger hover:opacity-75 transition-opacity"
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  )}
                  </tr>
                  
                  {expandedRows.has(p.id) && (
                    <tr className="bg-paper/30 border-b border-line-2">
                      <td colSpan={colCount} className="p-0">
                        <div className="p-4 pl-12">
                          {loadingVariants[p.id] ? (
                            <div className="text-xs text-muted flex items-center gap-2">
                              <svg className="animate-spin h-3 w-3 text-ink" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                              Đang tải dữ liệu...
                            </div>
                          ) : variantsCache[p.id]?.length > 0 ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                              {variantsCache[p.id].map(group => (
                                <div key={group.colorwayCode || group.colorway} className="bg-white border border-line rounded p-3 shadow-sm">
                                  <div className="flex items-center gap-2 mb-2">
                                    <span className="w-3 h-3 rounded-full border border-line" style={{ backgroundColor: group.colorHex || '#ccc' }}></span>
                                    <span className="text-xs font-bold">{group.colorway}</span>
                                  </div>
                                  <table className="w-full text-xs">
                                    <thead>
                                      <tr className="text-muted text-[10px] uppercase border-b border-line-2">
                                        <th className="text-left pb-1 font-medium">Size</th>
                                        <th className="text-right pb-1 font-medium">SKU</th>
                                        <th className="text-right pb-1 font-medium">Tồn</th>
                                        <th className="text-right pb-1 font-medium">Giá</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      {group.variants.map(v => (
                                        <tr key={v.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
                                          <td className="py-1.5">{v.size}</td>
                                          <td className="py-1.5 text-right text-muted">{v.sku}</td>
                                          <td className="py-1.5 text-right">
                                            <span className={v.stockQuantity > 0 ? 'text-ok font-medium' : 'text-danger font-medium'}>
                                              {v.stockQuantity}
                                            </span>
                                          </td>
                                          <td className="py-1.5 text-right font-medium">{formatPrice(v.price)}</td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <div className="text-xs text-muted">Sản phẩm này chưa có mẫu mã nào.</div>
                          )}
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {pageData.meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{pageData.meta.totalElements} sản phẩm</span>
          <div className="flex items-center gap-2">
            <button
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Trước
            </button>
            <span className="px-2 text-xs text-muted">{currentPage + 1} / {pageData.meta.totalPages}</span>
            <button
              disabled={pageData.meta.last}
              onClick={() => setCurrentPage(p => p + 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Tiếp
            </button>
          </div>
        </div>
      )}

      {createOpen && (
        <CreateProductModal
          onClose={() => setCreateOpen(false)}
          onCreated={(id) => router.push(`/admin/products/${id}`)}
        />
      )}
      {deleteTarget && (
        <DeleteProductModal
          product={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onDeleted={() => {
            setDeleteTarget(null);
            const safePage = pageData.data.length === 1 && currentPage > 0 ? currentPage - 1 : currentPage;
            setCurrentPage(safePage);
            fetchData(safePage, filters);
          }}
        />
      )}
    </div>
  );
}
