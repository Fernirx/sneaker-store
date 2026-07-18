'use client';

import { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import {
  formatDateTime,
  RESOLUTION_TYPE_LABELS, STATUS_OPTIONS, STATUS_LABELS, STATUS_COLORS,
  type PageData,
} from './types';

type Filters = {
  search: string;
  resolutionType: string;
  status: string;
  fromDate: string;
  toDate: string;
};

const EMPTY_FILTERS: Filters = { search: '', resolutionType: '', status: '', fromDate: '', toDate: '' };

export default function ReturnsClient({ initialData }: { initialData: PageData }) {
  const [result, setResult] = useState<PageData>(initialData);
  const [filters, setFilters] = useState<Filters>(EMPTY_FILTERS);
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const mounted = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'createdAt,desc');
      if (f.search) params.set('search', f.search);
      if (f.resolutionType) params.set('resolutionType', f.resolutionType);
      if (f.status) params.set('status', f.status);
      if (f.fromDate) params.set('fromDate', `${f.fromDate}T00:00:00`);
      if (f.toDate) params.set('toDate', `${f.toDate}T23:59:59`);
      const { data } = await clientAxios.get(`/api/admin/returns?${params}`);
      setResult(data);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!mounted.current) { mounted.current = true; return; }
    fetchData(currentPage, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, filters.search, filters.resolutionType, filters.status, filters.fromDate, filters.toDate]);

  function handleSearchChange(val: string) {
    setPendingSearch(val);
    if (searchTimer.current) clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => {
      setFilters(f => ({ ...f, search: val }));
      setCurrentPage(0);
    }, 350);
  }

  function updateFilter(key: keyof Filters, value: string) {
    setFilters(f => ({ ...f, [key]: value }));
    setCurrentPage(0);
  }

  const returns = result.data;
  const meta = result.meta;

  return (
    <div className="space-y-5">
      <h1 className="font-display font-black text-xl uppercase tracking-tight">Đổi/trả hàng</h1>

      <div className="flex gap-3 flex-wrap items-center">
        <input
          type="text"
          placeholder="Tìm theo mã yêu cầu/đơn hàng..."
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm w-60 focus:outline-none focus:border-ink"
        />
        <select
          value={filters.resolutionType}
          onChange={e => updateFilter('resolutionType', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả hình thức</option>
          <option value="REFUND">Hoàn tiền</option>
          <option value="EXCHANGE">Đổi size/màu</option>
        </select>
        <select
          value={filters.status}
          onChange={e => updateFilter('status', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả trạng thái</option>
          {STATUS_OPTIONS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
        </select>
        <div className="flex items-center gap-1.5">
          <input
            type="date"
            value={filters.fromDate}
            onChange={e => updateFilter('fromDate', e.target.value)}
            className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          <span className="text-muted text-sm">—</span>
          <input
            type="date"
            value={filters.toDate}
            onChange={e => updateFilter('toDate', e.target.value)}
            className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
        </div>
      </div>

      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Mã yêu cầu</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Đơn hàng</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Khách hàng</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Hình thức</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Trạng thái</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ngày tạo</th>
              <th className="px-4 py-3 w-16" />
            </tr>
          </thead>
          <tbody>
            {returns.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center py-14 text-muted text-sm">
                  Không có yêu cầu đổi/trả nào.
                </td>
              </tr>
            ) : (
              returns.map(r => (
                <tr key={r.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
                  <td className="px-4 py-3 font-body font-bold text-sm">{r.code}</td>
                  <td className="px-4 py-3 text-sm">{r.orderCode}</td>
                  <td className="px-4 py-3 text-xs text-muted">{r.customerEmail}</td>
                  <td className="px-4 py-3 text-sm">{RESOLUTION_TYPE_LABELS[r.resolutionType]}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${STATUS_COLORS[r.status]}`}>
                      {STATUS_LABELS[r.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-muted">{formatDateTime(r.createdAt)}</td>
                  <td className="px-4 py-3 text-right">
                    <Link href={`/admin/returns/${r.id}`} className="text-xs font-bold text-muted hover:text-ink transition-colors">
                      Xem
                    </Link>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{meta.totalElements} yêu cầu</span>
          <div className="flex items-center gap-2">
            <button
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Trước
            </button>
            <span className="px-2 text-xs text-muted">{currentPage + 1} / {meta.totalPages}</span>
            <button
              disabled={meta.last}
              onClick={() => setCurrentPage(p => p + 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Tiếp
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
