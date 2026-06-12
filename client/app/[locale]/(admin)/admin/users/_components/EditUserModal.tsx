'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { ALL_ROLES, formatRole, type UserRow } from './types';

type TFunc = ReturnType<typeof useTranslations>;

export default function EditUserModal({
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
