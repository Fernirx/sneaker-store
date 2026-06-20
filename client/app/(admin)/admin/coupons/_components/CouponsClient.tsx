'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import {
  formatDate, formatDiscount, formatPrice,
  type CouponRow, type PageData, type DiscountType,
} from './types';
import CreateCouponModal from './CreateCouponModal';
import EditCouponModal from './EditCouponModal';
import DeleteCouponModal from './DeleteCouponModal';

type Filters = { search: string; active: string; discountType: string };

export default function CouponsClient({
  initialData,
  isAdmin,
}: {
  initialData: PageData;
  isAdmin: boolean;
}) {
  const [pageData, setPageData]         = useState<PageData>(initialData);
  const [filters, setFilters]           = useState<Filters>({ search: '', active: '', discountType: '' });
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage]   = useState(0);
  const [loading, setLoading]           = useState(false);

  const [createOpen, setCreateOpen]     = useState(false);
  const [editTarget, setEditTarget]     = useState<CouponRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<CouponRow | null>(null);

  const mounted     = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!mounted.current) { mounted.current = true; return; }
    fetchData(currentPage, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, filters.search, filters.active, filters.discountType]);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'createdAt,desc');
      if (f.search) params.set('search', f.search);
      if (f.active !== '') params.set('active', f.active);
      if (f.discountType !== '') params.set('discountType', f.discountType);
      const { data } = await clientAxios.get(`/api/admin/coupons?${params}`);
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

  const colCount = isAdmin ? 8 : 7;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Coupon</h1>
        {isAdmin && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            Tạo coupon
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <input
          type="text"
          placeholder="Tìm theo mã..."
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm w-52 focus:outline-none focus:border-ink"
        />
        <select
          value={filters.active}
          onChange={e => { setFilters(f => ({ ...f, active: e.target.value })); setCurrentPage(0); }}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="true">Hoạt động</option>
          <option value="false">Ẩn</option>
        </select>
        <select
          value={filters.discountType}
          onChange={e => { setFilters(f => ({ ...f, discountType: e.target.value })); setCurrentPage(0); }}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả loại</option>
          <option value="PERCENTAGE">Phần trăm (%)</option>
          <option value="FIXED_AMOUNT">Số tiền cố định</option>
        </select>
      </div>

      {/* Table */}
      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Mã</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Loại</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Giảm giá</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Đơn tối thiểu</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Đã dùng</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Hết hạn</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Trạng thái</th>
              {isAdmin && <th className="px-4 py-3 w-24" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  Không có coupon nào.
                </td>
              </tr>
            ) : (
              pageData.data.map(coupon => (
                <tr key={coupon.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
                  <td className="px-4 py-3 font-body font-bold text-sm">{coupon.code}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      coupon.discountType === 'PERCENTAGE'
                        ? 'bg-blue-100 text-blue-700'
                        : 'bg-green-100 text-green-700'
                    }`}>
                      {coupon.discountType === 'PERCENTAGE' ? '%' : '₫'}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-semibold text-sm">
                    {formatDiscount(coupon.discountType, coupon.discountValue)}
                    {coupon.maxDiscountAmount != null && (
                      <span className="block text-[11px] text-muted font-normal">
                        tối đa {formatPrice(coupon.maxDiscountAmount)}
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-xs text-muted">
                    {coupon.minOrderAmount != null ? formatPrice(coupon.minOrderAmount) : '—'}
                  </td>
                  <td className="px-4 py-3 text-xs text-muted tabular-nums">
                    {coupon.usedCount}
                    {coupon.usageLimit != null
                      ? ` / ${coupon.usageLimit}`
                      : ' / ∞'}
                  </td>
                  <td className="px-4 py-3 text-xs text-muted">{formatDate(coupon.endDate)}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      coupon.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'
                    }`}>
                      {coupon.active ? 'Hoạt động' : 'Ẩn'}
                    </span>
                  </td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex gap-3 justify-end">
                        <button
                          onClick={() => setEditTarget(coupon)}
                          className="text-xs font-bold text-muted hover:text-ink transition-colors"
                        >
                          Sửa
                        </button>
                        <button
                          onClick={() => setDeleteTarget(coupon)}
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
      {pageData.meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{pageData.meta.totalElements} coupon</span>
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

      {/* Modals */}
      {createOpen && (
        <CreateCouponModal
          onClose={() => setCreateOpen(false)}
          onCreated={() => { setCreateOpen(false); setCurrentPage(0); fetchData(0, filters); }}
        />
      )}
      {editTarget && (
        <EditCouponModal
          coupon={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={() => { setEditTarget(null); fetchData(currentPage, filters); }}
        />
      )}
      {deleteTarget && (
        <DeleteCouponModal
          coupon={deleteTarget}
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
