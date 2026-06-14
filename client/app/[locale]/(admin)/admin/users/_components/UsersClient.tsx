'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { ALL_ROLES, formatRole, formatDate, roleBadgeClass, type UserRow, type PageData } from './types';
import CreateUserModal from './CreateUserModal';
import EditUserModal from './EditUserModal';
import DeleteUserModal from './DeleteUserModal';

type Filters = { search: string; role: string; active: string };

export default function UsersClient({
  initialData,
  isAdmin,
  currentUserId,
}: {
  initialData: PageData;
  isAdmin: boolean;
  currentUserId: number;
}) {
  const [pageData, setPageData] = useState<PageData>(initialData);
  const [filters, setFilters] = useState<Filters>({ search: '', role: '', active: '' });
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<UserRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<UserRow | null>(null);

  const mounted = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!mounted.current) {
      mounted.current = true;
      return;
    }
    fetchData(currentPage, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, filters.search, filters.role, filters.active]);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'createdAt,desc');
      if (f.search) params.set('search', f.search);
      if (f.role) params.set('role', f.role);
      if (f.active !== '') params.set('active', f.active);
      const { data } = await clientAxios.get(`/api/admin/users?${params}`);
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

  function handleFilterChange(key: 'role' | 'active', val: string) {
    setFilters(f => ({ ...f, [key]: val }));
    setCurrentPage(0);
  }

  const colCount = isAdmin ? 6 : 5;

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Quản lý người dùng</h1>
        {isAdmin && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            Tạo người dùng
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <input
          type="text"
          placeholder="Tìm theo email..."
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm w-64 focus:outline-none focus:border-ink"
        />
        <select
          value={filters.role}
          onChange={e => handleFilterChange('role', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả vai trò</option>
          {ALL_ROLES.map(r => (
            <option key={r} value={r}>{formatRole(r)}</option>
          ))}
        </select>
        <select
          value={filters.active}
          onChange={e => handleFilterChange('active', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">Tất cả trạng thái</option>
          <option value="true">Hoạt động</option>
          <option value="false">Vô hiệu</option>
        </select>
      </div>

      {/* Table */}
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
                Xác minh email
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                Ngày tạo
              </th>
              {isAdmin && <th className="px-4 py-3 w-24" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  Không có người dùng nào.
                </td>
              </tr>
            ) : (
              pageData.data.map(user => (
                <tr
                  key={user.id}
                  className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors"
                >
                  <td className="px-4 py-3 font-mono text-xs">{user.email}</td>
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
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        user.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'
                      }`}
                    >
                      {user.active ? 'Hoạt động' : 'Vô hiệu'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        user.emailVerified ? 'bg-ok-bg text-ok' : 'bg-warn-bg text-warn'
                      }`}
                    >
                      {user.emailVerified ? 'Đã xác minh' : 'Chưa xác minh'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-muted text-xs">{formatDate(user.createdAt)}</td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex gap-3 justify-end">
                        <button
                          onClick={() => setEditTarget(user)}
                          className="text-xs font-bold text-muted hover:text-ink transition-colors"
                        >
                          Sửa
                        </button>
                        {user.id !== currentUserId && (
                          <button
                            onClick={() => setDeleteTarget(user)}
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

      {/* Pagination */}
      {pageData.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">
            {pageData.totalElements} người dùng
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
              {currentPage + 1} / {pageData.totalPages}
            </span>
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

      {/* Modals */}
      {createOpen && (
        <CreateUserModal
          onClose={() => setCreateOpen(false)}
          onCreated={() => {
            setCreateOpen(false);
            setCurrentPage(0);
            fetchData(0, filters);
          }}
        />
      )}
      {editTarget && (
        <EditUserModal
          user={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={() => {
            setEditTarget(null);
            fetchData(currentPage, filters);
          }}
        />
      )}
      {deleteTarget && (
        <DeleteUserModal
          user={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onDeleted={() => {
            setDeleteTarget(null);
            const safePage = pageData.data.length === 1 && currentPage > 0
              ? currentPage - 1
              : currentPage;
            setCurrentPage(safePage);
            fetchData(safePage, filters);
          }}
        />
      )}
    </div>
  );
}
