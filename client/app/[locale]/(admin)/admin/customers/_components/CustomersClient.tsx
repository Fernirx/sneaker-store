'use client';

import { useState, useEffect, useRef } from 'react';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';

interface CustomerRow {
  id: number;
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  membershipTier: string;
  loyaltyPoints: number;
  totalSpent: number;
  createdAt: string;
}

interface PageData {
  data: CustomerRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

type TFunc = ReturnType<typeof useTranslations>;

const TIERS = ['BRONZE', 'SILVER', 'GOLD', 'PLATINUM'] as const;

function tierBadgeClass(tier: string) {
  const map: Record<string, string> = {
    BRONZE: 'bg-[#fdf0e8] text-[#9a6030]',
    SILVER: 'bg-line text-muted',
    GOLD: 'bg-warn-bg text-warn',
    PLATINUM: 'bg-accent-tint text-accent',
  };
  return map[tier] ?? 'bg-line text-muted';
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

function formatCurrency(amount: number) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(amount);
}

function fullName(c: CustomerRow) {
  return [c.firstName, c.lastName].filter(Boolean).join(' ');
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

// ── Edit customer modal ───────────────────────────────────────────────────────

function EditCustomerModal({
  t,
  customer,
  onClose,
  onSaved,
}: {
  t: TFunc;
  customer: CustomerRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [loyaltyPoints, setLoyaltyPoints] = useState(String(customer.loyaltyPoints));
  const [membershipTier, setMembershipTier] = useState(customer.membershipTier);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await clientAxios.patch(`/api/admin/customers/${customer.id}`, {
        loyaltyPoints: loyaltyPoints !== '' ? Number(loyaltyPoints) : null,
        membershipTier,
      });
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

        <div>
          <p className="font-mono text-xs text-muted">{customer.email}</p>
          <p className="text-sm font-bold mt-0.5">{fullName(customer)}</p>
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            {t('fieldPoints')}
          </label>
          <input
            type="number"
            min={0}
            value={loyaltyPoints}
            onChange={e => setLoyaltyPoints(e.target.value)}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            {t('fieldTier')}
          </label>
          <select
            value={membershipTier}
            onChange={e => setMembershipTier(e.target.value)}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white"
          >
            {TIERS.map(tier => (
              <option key={tier} value={tier}>{tier}</option>
            ))}
          </select>
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

// ── Delete customer modal ─────────────────────────────────────────────────────

function DeleteCustomerModal({
  t,
  customer,
  onClose,
  onDeleted,
}: {
  t: TFunc;
  customer: CustomerRow;
  onClose: () => void;
  onDeleted: () => void;
}) {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  async function handleDelete() {
    setDeleting(true);
    setError('');
    try {
      await clientAxios.delete(`/api/admin/customers/${customer.id}`);
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
        <p className="text-sm">{t('deleteConfirm', { email: customer.email })}</p>
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

type Filters = { search: string; tier: string };

export default function CustomersClient({
  initialData,
  isAdmin,
}: {
  initialData: PageData;
  isAdmin: boolean;
}) {
  const t = useTranslations('admin.customers');

  const [pageData, setPageData] = useState<PageData>(initialData);
  const [filters, setFilters] = useState<Filters>({ search: '', tier: '' });
  const [pendingSearch, setPendingSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const [editTarget, setEditTarget] = useState<CustomerRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<CustomerRow | null>(null);

  const mounted = useRef(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!mounted.current) {
      mounted.current = true;
      return;
    }
    fetchData(currentPage, filters);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, filters.search, filters.tier]);

  async function fetchData(page: number, f: Filters) {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.set('page', String(page));
      params.set('size', '20');
      params.set('sort', 'createdAt,desc');
      if (f.search) params.set('search', f.search);
      if (f.tier) params.set('membershipTier', f.tier);
      const { data } = await clientAxios.get(`/api/admin/customers?${params}`);
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

  function handleTierChange(val: string) {
    setFilters(f => ({ ...f, tier: val }));
    setCurrentPage(0);
  }

  const colCount = isAdmin ? 8 : 7;

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="font-display font-black text-xl uppercase tracking-tight">{t('title')}</h1>
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
          value={filters.tier}
          onChange={e => handleTierChange(e.target.value)}
          className="border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        >
          <option value="">{t('allTiers')}</option>
          {TIERS.map(tier => (
            <option key={tier} value={tier}>{tier}</option>
          ))}
        </select>
      </div>

      {/* Table */}
      <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colName')}
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                Email
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colPhone')}
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colTier')}
              </th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colPoints')}
              </th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colSpent')}
              </th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">
                {t('colJoined')}
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
              pageData.data.map(customer => (
                <tr
                  key={customer.id}
                  className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors"
                >
                  <td className="px-4 py-3 font-medium">{fullName(customer)}</td>
                  <td className="px-4 py-3 font-mono text-xs">{customer.email}</td>
                  <td className="px-4 py-3 text-sm text-muted">{customer.phone ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${tierBadgeClass(customer.membershipTier)}`}
                    >
                      {customer.membershipTier}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right text-sm tabular-nums">
                    {customer.loyaltyPoints.toLocaleString('vi-VN')}
                  </td>
                  <td className="px-4 py-3 text-right text-sm tabular-nums text-muted">
                    {formatCurrency(customer.totalSpent)}
                  </td>
                  <td className="px-4 py-3 text-muted text-xs">{formatDate(customer.createdAt)}</td>
                  {isAdmin && (
                    <td className="px-4 py-3">
                      <div className="flex gap-3 justify-end">
                        <button
                          onClick={() => setEditTarget(customer)}
                          className="text-xs font-bold text-muted hover:text-ink transition-colors"
                        >
                          {t('editBtn')}
                        </button>
                        <button
                          onClick={() => setDeleteTarget(customer)}
                          className="text-xs font-bold text-danger hover:opacity-75 transition-opacity"
                        >
                          {t('deleteBtn')}
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
      {editTarget && (
        <EditCustomerModal
          t={t}
          customer={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={() => {
            setEditTarget(null);
            fetchData(currentPage, filters);
          }}
        />
      )}
      {deleteTarget && (
        <DeleteCustomerModal
          t={t}
          customer={deleteTarget}
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
