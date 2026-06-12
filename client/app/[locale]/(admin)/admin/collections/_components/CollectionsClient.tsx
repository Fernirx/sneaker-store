'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDate, type CollectionRow, type PageData } from './types';
import CreateCollectionModal from './CreateCollectionModal';
import EditCollectionModal from './EditCollectionModal';
import DeleteCollectionModal from './DeleteCollectionModal';

type Filters = { search: string; active: string };

export default function CollectionsClient({
  initialData,
  isAdmin,
}: {
  initialData: PageData;
  isAdmin: boolean;
}) {
  const [pageData, setPageData] = useState<PageData>(initialData);
  const [filters, setFilters] = useState<Filters>({ search: '', active: '' });
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<CollectionRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<CollectionRow | null>(null);

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
      params.set('sort', 'createdAt,desc');
      if (f.search) params.set('search', f.search);
      if (f.active !== '') params.set('active', f.active);
      const { data } = await clientAxios.get(`/api/admin/collections?${params}`);
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

  const colCount = isAdmin ? 7 : 6;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Bộ sưu tập</h1>
        {isAdmin && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            Tạo bộ sưu tập
          </button>
        )}
      </div>

      <div className="flex gap-3 flex-wrap">
        <input
          type="text"
          placeholder="Tìm theo tên..."
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
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Tên</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Slug</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ra mắt</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Kết thúc</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Trạng thái</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ngày tạo</th>
              {isAdmin && <th className="px-4 py-3 w-24" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  Không có bộ sưu tập nào.
                </td>
              </tr>
            ) : (
              pageData.data.map(col => (
                <tr key={col.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
                  <td className="px-4 py-3 font-bold text-sm">{col.name}</td>
                  <td className="px-4 py-3 font-mono text-xs text-muted">{col.slug}</td>
                  <td className="px-4 py-3 text-xs text-muted">{col.launchDate ?? '—'}</td>
                  <td className="px-4 py-3 text-xs text-muted">{col.endDate ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${col.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'}`}>
                      {col.active ? 'Hoạt động' : 'Ẩn'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-muted text-xs">{formatDate(col.createdAt)}</td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex gap-3 justify-end">
                        <button
                          onClick={() => setEditTarget(col)}
                          className="text-xs font-bold text-muted hover:text-ink transition-colors"
                        >
                          Sửa
                        </button>
                        <button
                          onClick={() => setDeleteTarget(col)}
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

      {pageData.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{pageData.totalElements} bộ sưu tập</span>
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
        <CreateCollectionModal
          onClose={() => setCreateOpen(false)}
          onCreated={() => { setCreateOpen(false); setCurrentPage(0); fetchData(0, filters); }}
        />
      )}
      {editTarget && (
        <EditCollectionModal
          collection={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={() => { setEditTarget(null); fetchData(currentPage, filters); }}
        />
      )}
      {deleteTarget && (
        <DeleteCollectionModal
          collection={deleteTarget}
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
