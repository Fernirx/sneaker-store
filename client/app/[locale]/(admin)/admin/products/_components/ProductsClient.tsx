'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { productUrl } from '@/lib/cloudinaryUrl';
import {
  type ProductRow, type PageData,
  GENDER_OPTIONS, formatPrice,
} from './types';
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

  const colCount = isAdmin ? 7 : 6;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Sản phẩm</h1>
        {isAdmin && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            Tạo sản phẩm
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
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-16">Ảnh</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Tên / Mã</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Thương hiệu</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Giới tính</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Giá bán</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Trạng thái</th>
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
                <tr key={p.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
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
                  <td className="px-4 py-3">
                    <p className="font-bold text-sm leading-snug">{p.name}</p>
                    <p className="font-mono text-xs text-muted mt-0.5">{p.code}</p>
                    <div className="flex gap-1 mt-1">
                      {p.newArrival && (
                        <span className="px-1.5 py-0.5 bg-accent/10 text-accent text-[9px] font-bold rounded">MỚI</span>
                      )}
                      {p.onSale && (
                        <span className="px-1.5 py-0.5 bg-danger/10 text-danger text-[9px] font-bold rounded">SALE</span>
                      )}
                    </div>
                  </td>
                  <td className="px-4 py-3 text-sm">{p.brand.name}</td>
                  <td className="px-4 py-3 text-sm text-muted">
                    {GENDER_OPTIONS.find(o => o.value === p.gender)?.label ?? p.gender}
                  </td>
                  <td className="px-4 py-3 text-sm font-medium">{formatPrice(p.basePrice)}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${p.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'}`}>
                      {p.active ? 'Hoạt động' : 'Ẩn'}
                    </span>
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
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {pageData.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{pageData.totalElements} sản phẩm</span>
          <div className="flex items-center gap-2">
            <button
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Trước
            </button>
            <span className="px-2 text-xs text-muted">{currentPage + 1} / {pageData.totalPages}</span>
            <button
              disabled={currentPage >= pageData.totalPages - 1}
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
