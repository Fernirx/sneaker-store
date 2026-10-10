'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { ALL_ROLES, formatRole, formatDate, roleBadgeClass, type UserRow, type PageData } from './types';
import RestoreUserModal from './RestoreUserModal';

export default function DeletedUsersClient() {
  const [pageData, setPageData] = useState<PageData | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [restoreTarget, setRestoreTarget] = useState<UserRow | null>(null);

  const mounted = useRef(false);

  useEffect(() => {
    if (!mounted.current) {
      mounted.current = true;
      return;
    }
    fetchData(currentPage);
  }, [currentPage]);

  async function fetchData(page: number) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'deleted_at,desc');
      const { data } = await clientAxios.get(`/api/admin/users/deleted?${params}`);
      setPageData(data);
    } finally {
      setLoading(false);
    }
  }

  // Initial fetch triggers on mount if we change the condition, but let's just trigger it manually once
  useEffect(() => {
    fetchData(0);
  }, []);

  return (
    <div className="space-y-5 mt-4">
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Thùng rác</h1>
      </div>

      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                Email
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                Vai trò
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                Trạng thái
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                Ngày xóa
              </th>
              <th className="px-4 py-3 w-24" />
            </tr>
          </thead>
          <tbody>
            {!pageData || pageData.data.length === 0 ? (
              <tr>
                <td colSpan={5} className="text-center py-14 text-muted text-sm">
                  Thùng rác trống.
                </td>
              </tr>
            ) : (
              pageData.data.map(user => (
                <tr
                  key={user.id}
                  className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors"
                >
                  <td className="px-4 py-3 text-xs">{user.email}</td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-1">
                      {user.roles.map(r => (
                        <span
                          key={r}
                          className={`px-1.5 py-0.5 rounded text-[10px] font-bold ${roleBadgeClass(r)}`}
                        >
                          {formatRole(r)}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-danger-bg text-danger">
                      Đã xóa
                    </span>
                  </td>
                  <td className="px-4 py-3 text-muted text-xs">{formatDate(user.deletedAt || user.createdAt)}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-3 justify-end">
                      <button
                        onClick={() => setRestoreTarget(user)}
                        className="text-xs font-bold text-ok hover:opacity-75 transition-opacity"
                      >
                        Khôi phục
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {pageData && pageData.meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">
            {pageData.meta.totalElements} người dùng đã xóa
          </span>
          <div className="flex items-center gap-2">
            <button
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Trước
            </button>
            <span className="px-2 text-xs text-muted">
              {currentPage + 1} / {pageData.meta.totalPages}
            </span>
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

      {restoreTarget && (
        <RestoreUserModal
          user={restoreTarget}
          onClose={() => setRestoreTarget(null)}
          onRestored={() => {
            setRestoreTarget(null);
            const safePage = pageData && pageData.data.length === 1 && currentPage > 0
              ? currentPage - 1
              : currentPage;
            setCurrentPage(safePage);
            fetchData(safePage);
          }}
        />
      )}
    </div>
  );
}
