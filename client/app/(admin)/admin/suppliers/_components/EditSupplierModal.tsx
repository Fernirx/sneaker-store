'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import PhoneField from './PhoneField';
import { type SupplierRow } from './types';

export default function EditSupplierModal({
  supplier,
  onClose,
  onSaved,
}: {
  supplier: SupplierRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [code, setCode] = useState(supplier.code);
  const [name, setName] = useState(supplier.name);
  const [email, setEmail] = useState(supplier.email ?? '');
  const [phone, setPhone] = useState(supplier.phone ?? '');
  const [contactPerson, setContactPerson] = useState(supplier.contactPerson ?? '');
  const [contactPhone, setContactPhone] = useState(supplier.contactPhone ?? '');
  const [address, setAddress] = useState(supplier.address ?? '');
  const [notes, setNotes] = useState(supplier.notes ?? '');
  const [active, setActive] = useState(supplier.active);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/suppliers/${supplier.id}`, {
        code,
        name,
        email: email || null,
        phone: phone || null,
        contactPerson: contactPerson || null,
        contactPhone: contactPhone || null,
        address: address || null,
        notes: notes || null,
        active,
      });
      onSaved();
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
      document.querySelector('.overflow-y-auto')?.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Cập nhật nhà cung cấp" onClose={onClose} maxWidth="2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Mã <span className="text-danger">*</span>
            </label>
            <input
              type="text"
              value={code}
              onChange={e => setCode(e.target.value.replace(/^\s+/, '').toUpperCase())}
              required
              maxLength={50}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm font-body focus:outline-none focus:border-ink"
            />
            {fieldErrors.code && <p className="text-danger text-xs mt-1">{fieldErrors.code}</p>}
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Tên <span className="text-danger">*</span>
            </label>
            <input
              type="text"
              value={name}
              onChange={e => setName(e.target.value.replace(/^\s+/, ''))}
              required
              maxLength={200}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
            {fieldErrors.name && <p className="text-danger text-xs mt-1">{fieldErrors.name}</p>}
          </div>
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Email</label>
          <input
            type="text"
            inputMode="email"
            value={email}
            onChange={e => setEmail(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.email && <p className="text-danger text-xs mt-1">{fieldErrors.email}</p>}
        </div>

        <div className="grid grid-cols-2 gap-3">
          <PhoneField label="Số điện thoại" value={phone} onChange={setPhone} error={fieldErrors.phone} />
          <PhoneField label="SĐT người liên hệ" value={contactPhone} onChange={setContactPhone} error={fieldErrors.contactPhone} />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Người liên hệ</label>
          <input
            type="text"
            value={contactPerson}
            onChange={e => setContactPerson(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Địa chỉ</label>
          <textarea
            value={address}
            onChange={e => setAddress(e.target.value.replace(/^\s+/, ''))}
            rows={2}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
          />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Ghi chú</label>
          <textarea
            value={notes}
            onChange={e => setNotes(e.target.value.replace(/^\s+/, ''))}
            rows={2}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
          />
        </div>

        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={active} onChange={e => setActive(e.target.checked)} className="accent-accent" />
          <span className="text-[11px] font-bold uppercase tracking-wider">Đang hoạt động</span>
        </label>

        <div className="flex justify-end gap-2 pt-1">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={saving || !code.trim() || !name.trim()}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang lưu...' : 'Lưu'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
