'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import PhoneField from './PhoneField';

export default function CreateSupplierModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: () => void;
}) {
  const [code, setCode] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [contactPerson, setContactPerson] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [address, setAddress] = useState('');
  const [notes, setNotes] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.post('/api/admin/suppliers', {
        code,
        name,
        email: email || null,
        phone: phone || null,
        contactPerson: contactPerson || null,
        contactPhone: contactPhone || null,
        address: address || null,
        notes: notes || null,
      });
      onCreated();
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
    <Modal title="Thêm nhà cung cấp mới" onClose={onClose} maxWidth="2xl">
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
            {saving ? 'Đang thêm...' : 'Thêm'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
