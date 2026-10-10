'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type UserRow } from './types';

export default function RestoreUserModal({
  user,
  onClose,
  onRestored,
}: {
  user: UserRow;
  onClose: () => void;
  onRestored: () => void;
}) {
  const [restoring, setRestoring] = useState(false);
  const [error, setError] = useState('');

  async function handleRestore() {
    setRestoring(true);
    setError('');
    try {
      await clientAxios.patch(`/api/admin/users/${user.id}/restore`);
      onRestored();
    } catch (err) {
      setError(parseApiError(err).general);
      setRestoring(false);
    }
  }

  return (
    <Modal title="Khôi phục người dùng" onClose={onClose}>
      <div className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}
        <p className="text-sm">
          Bạn có chắc muốn khôi phục tài khoản <span className="font-bold">{user.email}</span>?
        </p>
        <div className="flex justify-end gap-2 pt-1">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            Hủy
          </button>
          <button
            onClick={handleRestore}
            disabled={restoring}
            className="px-4 py-2 bg-ok text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60 transition-opacity"
          >
            {restoring ? 'Đang khôi phục...' : 'Khôi phục'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
