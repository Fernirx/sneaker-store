'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDateTime, type BannerRow, type PageData } from './types';
import CreateBannerModal from './CreateBannerModal';
import EditBannerModal from './EditBannerModal';
import DeleteBannerModal from './DeleteBannerModal';

type Filters = { search: string; active: string };

export default function BannersClient({
  initialData,
  canCreateDelete,
  canUpdate,
}: {
  initialData: PageData;
  canCreateDelete: boolean;
  canUpdate: boolean;
}) {
  const [pageData, setPageData] = useState<PageData>(initialData);
  const [filters, setFilters] = useState<Filters>({ search: '', active: '' });
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<BannerRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<BannerRow | null>(null);

  const mounted = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!mounted.current) {
      mounted.current = true;
      return;
    }
    fetchData(currentPage, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, filters.search, filters.active]);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'displayOrder,asc');
      if (f.search) params.set('search', f.search);
      if (f.active !== '') params.set('active', f.active);
      const { data } = await clientAxios.get(`/api/admin/banners?${params}`);
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

  function handleActiveChange(val: string) {
    setFilters(f => ({ ...f, active: val }));
    setCurrentPage(0);
  }

  const showActionCol = canCreateDelete || canUpdate;
  const colCount = showActionCol ? 6 : 5;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Banner</h1>
        {canCreateDelete && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            Thêm banner
          </button>
        )}
      </div>

      <div className="flex gap-3 flex-wrap">
        <input
          type="text"
          placeholder="Tìm theo tiêu đề..."
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm w-64 focus:outline-none focus:border-ink"
        />
        <select
          value={filters.active}
          onChange={e => handleActiveChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="true">Hoạt động</option>
          <option value="false">Ẩn</option>
        </select>
      </div>

      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="table-fixed w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="w-[40%] text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Tiêu đề</th>
              <th className="w-[10%] text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Thứ tự</th>
              <th className="w-[15%] text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Bắt đầu</th>
              <th className="w-[15%] text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Kết thúc</th>
              <th className="w-[10%] text-center px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Trạng thái</th>
              {showActionCol && <th className="w-[10%] px-4 py-3 whitespace-nowrap" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  Chưa có banner nào.
                </td>
              </tr>
            ) : (
              pageData.data.map(b => (
                <tr key={b.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
                  <td className="px-4 py-3 font-bold text-sm truncate">{b.title}</td>
                  <td className="px-4 py-3 text-xs text-muted text-right tabular-nums whitespace-nowrap">{b.displayOrder}</td>
                  <td className="px-4 py-3 text-xs text-muted text-right tabular-nums whitespace-nowrap">{formatDateTime(b.startAt)}</td>
                  <td className="px-4 py-3 text-xs text-muted text-right tabular-nums whitespace-nowrap">{formatDateTime(b.endAt)}</td>
                  <td className="px-4 py-3 text-center whitespace-nowrap">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${b.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'}`}>
                      {b.active ? 'Hoạt động' : 'Ẩn'}
                    </span>
                  </td>
                  {showActionCol && (
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="flex gap-3 justify-end">
                        {canUpdate && (
                          <button
                            onClick={() => setEditTarget(b)}
                            className="text-xs font-bold text-muted hover:text-ink transition-colors"
                          >
                            Sửa
                          </button>
                        )}
                        {canCreateDelete && (
                          <button
                            onClick={() => setDeleteTarget(b)}
                            className="text-xs font-bold text-danger hover:opacity-75 transition-opacity"
                          >
                            Xóa
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {pageData.meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{pageData.meta.totalElements} banner</span>
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
        <CreateBannerModal
          onClose={() => setCreateOpen(false)}
          onCreated={() => { setCreateOpen(false); setCurrentPage(0); fetchData(0, filters); }}
        />
      )}
      {editTarget && (
        <EditBannerModal
          banner={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={() => { setEditTarget(null); fetchData(currentPage, filters); }}
        />
      )}
      {deleteTarget && (
        <DeleteBannerModal
          banner={deleteTarget}
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
