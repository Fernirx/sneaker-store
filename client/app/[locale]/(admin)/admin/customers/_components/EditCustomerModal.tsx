'use client';

import { useState } from 'react';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { TIERS, fullName, type CustomerRow } from './types';

type TFunc = ReturnType<typeof useTranslations>;

export default function EditCustomerModal({
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
