'use client';

import { useState, useEffect, useRef } from 'react';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';

interface UserRow {
  id: number;
  email: string;
  active: boolean;
  emailVerified: boolean;
  roles: string[];
  createdAt: string;
}

interface PageData {
  data: UserRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

type TFunc = ReturnType<typeof useTranslations>;

const ALL_ROLES = [
  'ROLE_USER',
  'ROLE_ADMIN',
  'ROLE_SALE',
  'ROLE_WAREHOUSE',
  'ROLE_MARKETING',
  'ROLE_TECHNICIAN',
] as const;

function formatRole(role: string) {
  return role.replace('ROLE_', '');
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

function roleBadgeClass(role: string) {
  if (role === 'ROLE_ADMIN') return 'bg-danger-bg text-danger';
  if (role === 'ROLE_SALE') return 'bg-ok-bg text-ok';
  if (role === 'ROLE_USER') return 'bg-line-2 text-muted';
  return 'bg-warn-bg text-warn';
}

// ── Modal shell ───────────────────────────────────────────────────────────────

function Modal({
  title,
  onClose,
  children,
}: {
  title: string;
  onClose: () => void;
  children: React.ReactNode;
}) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={onClose}
    >
      <div
        className="bg-white rounded-lg shadow-xl w-full max-w-md mx-4"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-line">
          <h2 className="font-display font-black text-sm uppercase tracking-wide">{title}</h2>
          <button
            onClick={onClose}
            className="text-muted hover:text-ink text-xl leading-none transition-colors"
          >
            &times;
          </button>
        </div>
        <div className="px-5 py-4">{children}</div>
      </div>
    </div>
  );
}

// ── Create user modal ─────────────────────────────────────────────────────────

function CreateUserModal({
  t,
  onClose,
  onCreated,
}: {
  t: TFunc;
  onClose: () => void;
  onCreated: () => void;
}) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [selectedRoles, setSelectedRoles] = useState<string[]>(['ROLE_USER']);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function toggleRole(role: string, checked: boolean) {
    setSelectedRoles(prev =>
      checked ? [...prev, role] : prev.filter(r => r !== role),
    );
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.post('/api/admin/users', { email, password, firstName, roles: selectedRoles });
      onCreated();
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title={t('createTitle')} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            {t('fieldEmail')}
          </label>
          <input
            type="email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.email && <p className="text-danger text-xs mt-1">{fieldErrors.email}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            {t('fieldPassword')}
          </label>
          <input
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.password && <p className="text-danger text-xs mt-1">{fieldErrors.password}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            {t('fieldFirstName')}
          </label>
          <input
            type="text"
            value={firstName}
            onChange={e => setFirstName(e.target.value)}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.firstName && <p className="text-danger text-xs mt-1">{fieldErrors.firstName}</p>}
        </div>

        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
            {t('fieldRoles')}
          </p>
          <div className="grid grid-cols-2 gap-1.5">
            {ALL_ROLES.map(r => (
              <label key={r} className="flex items-center gap-2 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  checked={selectedRoles.includes(r)}
                  onChange={e => toggleRole(r, e.target.checked)}
                  className="accent-accent"
                />
                {formatRole(r)}
              </label>
            ))}
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-1">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            {t('cancelBtn')}
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? t('saving') : t('saveBtn')}
          </button>
        </div>
      </form>
    </Modal>
  );
}

// ── Edit user modal ───────────────────────────────────────────────────────────

function EditUserModal({
  t,
  user,
  onClose,
  onSaved,
}: {
  t: TFunc;
  user: UserRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [active, setActive] = useState(user.active);
  const [selectedRoles, setSelectedRoles] = useState<string[]>(user.roles);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  function toggleRole(role: string, checked: boolean) {
    setSelectedRoles(prev =>
      checked ? [...prev, role] : prev.filter(r => r !== role),
    );
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await clientAxios.patch(`/api/admin/users/${user.id}`, { active, roles: selectedRoles });
      onSaved();
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title={t('editTitle')} onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}

        <p className="font-mono text-xs text-muted">{user.email}</p>

        <div>
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={active}
              onChange={e => setActive(e.target.checked)}
              className="accent-accent"
            />
            <span className="text-[11px] font-bold uppercase tracking-wider">{t('fieldActive')}</span>
          </label>
        </div>

        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
            {t('fieldRoles')}
          </p>
          <div className="grid grid-cols-2 gap-1.5">
            {ALL_ROLES.map(r => (
              <label key={r} className="flex items-center gap-2 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  checked={selectedRoles.includes(r)}
                  onChange={e => toggleRole(r, e.target.checked)}
                  className="accent-accent"
                />
                {formatRole(r)}
              </label>
            ))}
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-1">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            {t('cancelBtn')}
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? t('saving') : t('saveBtn')}
          </button>
        </div>
      </form>
    </Modal>
  );
}

// ── Delete user modal ─────────────────────────────────────────────────────────

function DeleteUserModal({
  t,
  user,
  onClose,
  onDeleted,
}: {
  t: TFunc;
  user: UserRow;
  onClose: () => void;
  onDeleted: () => void;
}) {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  async function handleDelete() {
    setDeleting(true);
    setError('');
    try {
      await clientAxios.delete(`/api/admin/users/${user.id}`);
      onDeleted();
    } catch (err) {
      setError(parseApiError(err).general);
      setDeleting(false);
    }
  }

  return (
    <Modal title={t('deleteTitle')} onClose={onClose}>
      <div className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}
        <p className="text-sm">
          {t('deleteConfirm', { email: user.email })}
        </p>
        <p className="text-xs text-muted">{t('deleteWarning')}</p>
        <div className="flex justify-end gap-2 pt-1">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            {t('cancelBtn')}
          </button>
          <button
            onClick={handleDelete}
            disabled={deleting}
            className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60 transition-opacity"
          >
            {deleting ? t('deleting') : t('deleteBtn')}
          </button>
        </div>
      </div>
    </Modal>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

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
  const t = useTranslations('admin.users');

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
        <h1 className="font-display font-black text-xl uppercase tracking-tight">{t('title')}</h1>
        {isAdmin && (
          <button
            onClick={() => setCreateOpen(true)}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
            {t('createBtn')}
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <input
          type="text"
          placeholder={t('searchPlaceholder')}
          value={pendingSearch}
          onChange={e => handleSearchChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm w-64 focus:outline-none focus:border-ink"
        />
        <select
          value={filters.role}
          onChange={e => handleFilterChange('role', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">{t('allRoles')}</option>
          {ALL_ROLES.map(r => (
            <option key={r} value={r}>{formatRole(r)}</option>
          ))}
        </select>
        <select
          value={filters.active}
          onChange={e => handleFilterChange('active', e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">{t('allStatus')}</option>
          <option value="true">{t('active')}</option>
          <option value="false">{t('inactive')}</option>
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
                {t('colRoles')}
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colActive')}
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colVerified')}
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colCreated')}
              </th>
              {isAdmin && <th className="px-4 py-3 w-24" />}
            </tr>
          </thead>
          <tbody>
            {pageData.data.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-center py-14 text-muted text-sm">
                  {t('noData')}
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
                      {user.active ? t('active') : t('inactive')}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        user.emailVerified ? 'bg-ok-bg text-ok' : 'bg-warn-bg text-warn'
                      }`}
                    >
                      {user.emailVerified ? t('verified') : t('unverified')}
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
                          {t('editBtn')}
                        </button>
                        {user.id !== currentUserId && (
                          <button
                            onClick={() => setDeleteTarget(user)}
                            className="text-xs font-bold text-danger hover:opacity-75 transition-opacity"
                          >
                            {t('deleteBtn')}
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
            {pageData.totalElements} {t('totalItems')}
          </span>
          <div className="flex items-center gap-2">
            <button
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              {t('prev')}
            </button>
            <span className="px-2 text-xs text-muted">
              {currentPage + 1} / {pageData.totalPages}
            </span>
            <button
              disabled={currentPage >= pageData.totalPages - 1}
              onClick={() => setCurrentPage(p => p + 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              {t('next')}
            </button>
          </div>
        </div>
      )}

      {/* Modals */}
      {createOpen && (
        <CreateUserModal
          t={t}
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
          t={t}
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
          t={t}
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
