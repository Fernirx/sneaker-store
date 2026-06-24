'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDate, type CommentRow, type PageData } from './types';

type Filters = { approved: string };

export default function CommentsClient({
  initialData,
  isAdmin,
}: {
  initialData: PageData;
  isAdmin: boolean;
}) {
  const [pageData, setPageData]       = useState<PageData>(initialData);
  const [filters, setFilters]         = useState<Filters>({ approved: '' });
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading]         = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<CommentRow | null>(null);
  const [deleting, setDeleting]         = useState(false);
  const [deleteError, setDeleteError]   = useState('');
  const [actionError, setActionError]   = useState('');

  const mounted = useRef(false);

  useEffect(() => {
    if (!mounted.current) { mounted.current = true; return; }
    fetchData(currentPage, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, filters.approved]);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'createdAt,desc');
      if (f.approved !== '') params.set('approved', f.approved);
      const { data } = await clientAxios.get(`/api/admin/comments?${params}`);
      setPageData(data);
    } finally {
      setLoading(false);
    }
  }

  async function toggleApprove(row: CommentRow) {
    setActionError('');
    try {
      await clientAxios.patch(`/api/admin/comments/${row.id}/approve`, { approved: !row.approved });
      setPageData(prev => ({
        ...prev,
        data: prev.data.map(c => c.id === row.id ? { ...c, approved: !c.approved } : c),
      }));
    } catch {
      setActionError('Không thể cập nhật trạng thái duyệt.');
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await clientAxios.delete(`/api/admin/comments/${deleteTarget.id}`);
      setDeleteTarget(null);
      const safePage = pageData.data.length === 1 && currentPage > 0 ? currentPage - 1 : currentPage;
      setCurrentPage(safePage);
      fetchData(safePage, filters);
    } catch {
      setDeleteError('Không thể xóa bình luận.');
    } finally {
      setDeleting(false);
    }
  }

  const colCount = isAdmin ? 6 : 5;

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Bình luận sản phẩm</h1>
      </div>

      {actionError && <p className="text-sm text-danger">{actionError}</p>}

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <select
          value={filters.approved}
          onChange={e => { setFilters(f => ({ ...f, approved: e.target.value })); setCurrentPage(0); }}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="true">Đang hiển thị</option>
          <option value="false">Đã ẩn</option>
        </select>
      </div>

      {/* Table */}
      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Người bình luận</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Sản phẩm</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Nội dung</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Trạng thái</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ngày tạo</th>
              {isAdmin && <th className="px-4 py-3 w-32" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  Không có bình luận nào.
                </td>
              </tr>
            ) : (
              pageData.data.map(row => (
                <tr key={row.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors align-top">
                  <td className="px-4 py-3 text-xs">{row.userEmail}</td>
                  <td className="px-4 py-3 text-xs font-semibold max-w-[160px] truncate">{row.productName}</td>
                  <td className="px-4 py-3 text-xs max-w-[300px]">
                    {row.parentId != null && (
                      <span className="text-[10px] font-bold text-muted">Trả lời #{row.parentId} · </span>
                    )}
                    <span className="truncate">{row.content}</span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                      row.approved ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'
                    }`}>
                      {row.approved ? 'Đang hiển thị' : 'Đã ẩn'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-xs text-muted">{formatDate(row.createdAt)}</td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex gap-3 justify-end">
                        <button
                          onClick={() => toggleApprove(row)}
                          className="text-xs font-bold text-muted hover:text-ink transition-colors"
                        >
                          {row.approved ? 'Ẩn' : 'Duyệt'}
                        </button>
                        <button
                          onClick={() => { setDeleteTarget(row); setDeleteError(''); }}
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
          <span className="text-muted text-xs">{pageData.meta.totalElements} bình luận</span>
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

      {/* Delete confirm */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={() => setDeleteTarget(null)}>
          <div className="bg-white rounded-lg shadow-xl w-full max-w-sm p-5 space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="font-display font-black text-sm uppercase tracking-wide">Xóa bình luận</h3>
            {deleteError && <p className="text-danger text-sm">{deleteError}</p>}
            <p className="text-sm">Xóa bình luận của <strong>{deleteTarget.userEmail}</strong>? Các trả lời bên dưới (nếu có) cũng sẽ bị xóa.</p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">Hủy</button>
              <button onClick={handleDelete} disabled={deleting}
                className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60">
                {deleting ? 'Đang xóa...' : 'Xóa'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
