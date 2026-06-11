'use client';

import { useState, useEffect, useCallback } from 'react';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import AddressModal, { type AddressForm } from './AddressModal';

interface Address {
  id: number;
  name: string;
  phone: string;
  street: string;
  ward?: string;
  district: string;
  province: string;
  postalCode?: string;
  defaultAddress: boolean;
}

function MapPinIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/>
    </svg>
  );
}

export default function AddressSection() {
  const t = useTranslations('profile');
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState<{ open: boolean; editing?: Address }>({ open: false });
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const fetchAddresses = useCallback(async () => {
    try {
      const { data } = await clientAxios.get('/api/me/addresses');
      setAddresses(data.data ?? []);
    } catch {
      setAddresses([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAddresses(); }, [fetchAddresses]);

  async function handleSave(form: AddressForm) {
    if (modal.editing) {
      await clientAxios.patch(`/api/me/addresses/${modal.editing.id}`, form);
    } else {
      await clientAxios.post('/api/me/addresses', form);
    }
    setModal({ open: false });
    fetchAddresses();
  }

  async function handleDelete(id: number) {
    if (!window.confirm(t('confirmDelete'))) return;
    setDeletingId(id);
    try {
      await clientAxios.delete(`/api/me/addresses/${id}`);
      setAddresses(prev => prev.filter(a => a.id !== id));
    } finally {
      setDeletingId(null);
    }
  }

  function formatAddress(a: Address) {
    return [a.street, a.ward, a.district, a.province].filter(Boolean).join(', ');
  }

  if (loading) {
    return (
      <div className="space-y-3">
        {[1, 2].map(i => (
          <div key={i} className="border border-line rounded-lg p-4 animate-pulse">
            <div className="h-4 bg-paper rounded w-1/3 mb-2" />
            <div className="h-3 bg-paper rounded w-2/3" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <p className="text-sm text-muted">{addresses.length} địa chỉ</p>
        <button onClick={() => setModal({ open: true })}
          className="flex items-center gap-2 bg-accent hover:bg-accent-700 text-white font-display font-bold text-[13px] uppercase tracking-wider px-4 py-2 rounded transition-colors">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          {t('addAddress')}
        </button>
      </div>

      {addresses.length === 0 ? (
        <div className="border border-dashed border-line rounded-lg p-10 text-center">
          <div className="text-faint mb-3 flex justify-center">
            <MapPinIcon />
          </div>
          <p className="text-sm text-muted">{t('noAddresses')}</p>
        </div>
      ) : (
        <div className="space-y-3">
          {[...addresses].sort((a, b) => (b.defaultAddress ? 1 : 0) - (a.defaultAddress ? 1 : 0)).map(addr => (
            <div key={addr.id}
              className={`border rounded-lg p-4 transition-colors ${
                addr.defaultAddress ? 'border-accent/40 bg-accent-tint' : 'border-line bg-white'
              }`}>
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0 space-y-0.5">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-semibold text-sm text-ink">{addr.name}</span>
                    <span className="text-muted text-sm">·</span>
                    <span className="text-sm text-ink-2">{addr.phone}</span>
                    {addr.defaultAddress && (
                      <span className="font-mono text-[10px] font-semibold tracking-wider uppercase bg-accent text-white px-2 py-0.5 rounded-sm">
                        {t('defaultBadge')}
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-muted leading-snug">{formatAddress(addr)}</p>
                  {addr.postalCode && (
                    <p className="text-xs text-faint">Mã bưu chính: {addr.postalCode}</p>
                  )}
                </div>
                <div className="flex gap-2 shrink-0">
                  <button
                    onClick={() => setModal({ open: true, editing: addr })}
                    className="text-xs font-semibold text-ink-2 hover:text-ink border border-line hover:border-ink rounded px-2.5 py-1.5 transition-colors">
                    {t('editAddress')}
                  </button>
                  <button
                    onClick={() => handleDelete(addr.id)}
                    disabled={deletingId === addr.id}
                    className="text-xs font-semibold text-danger hover:text-danger border border-danger/30 hover:border-danger rounded px-2.5 py-1.5 transition-colors disabled:opacity-40">
                    {deletingId === addr.id ? '...' : t('deleteAddress')}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {modal.open && (
        <AddressModal
          initial={modal.editing}
          onClose={() => setModal({ open: false })}
          onSave={handleSave}
        />
      )}
    </div>
  );
}
