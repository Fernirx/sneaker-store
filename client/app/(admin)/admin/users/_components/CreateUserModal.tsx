'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { ALL_ROLES, formatRole } from './types';

export default function CreateUserModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: () => void;
}) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [firstName, setFirstName] = useState('');
  const [selectedRoles, setSelectedRoles] = useState<string[]>(['ROLE_CUSTOMER']);
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
      document.querySelector('.overflow-y-auto')?.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Thêm người dùng mới" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Email
            <span className="text-danger ml-1">*</span>
          </label>
          <input
            type="text"
            inputMode="email"
            value={email}
            onChange={e => setEmail(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.email && <p className="text-danger text-xs mt-1">{fieldErrors.email}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Mật khẩu
            <span className="text-danger ml-1">*</span>
          </label>
          <input
            type="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            maxLength={255}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.password && <p className="text-danger text-xs mt-1">{fieldErrors.password}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Họ
            <span className="text-danger ml-1">*</span>
          </label>
          <input
            type="text"
            value={firstName}
            onChange={e => setFirstName(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.firstName && <p className="text-danger text-xs mt-1">{fieldErrors.firstName}</p>}
        </div>

        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">Vai trò</p>
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
            Hủy
          </button>
          <button
            type="submit"
            disabled={saving || !email.trim() || !password || !firstName.trim()}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang thêm...' : 'Thêm'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
