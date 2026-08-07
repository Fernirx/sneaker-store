'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type CollectionRow } from './types';

export default function DeleteCollectionModal({
  collection,
  onClose,
  onDeleted,
}: {
  collection: CollectionRow;
  onClose: () => void;
  onDeleted: () => void;
}) {
  const hasProducts = collection.productCount > 0;
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  async function handleDelete() {
    setDeleting(true);
    setError('');
    try {
      await clientAxios.delete(`/api/admin/collections/${collection.id}`);
      onDeleted();
    } catch (err) {
      setError(parseApiError(err).general);
      setDeleting(false);
    }
  }

  return (
    <Modal title="Xóa bộ sưu tập" onClose={onClose}>
      <div className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}
        {hasProducts ? (
          <div className="space-y-1.5">
            <p className="text-sm text-danger">
              Không thể xóa. Bộ sưu tập <span className="font-bold">{collection.name}</span> đang liên kết với <span className="font-bold">{collection.productCount}</span> sản phẩm.
            </p>
          </div>
        ) : (
          <div className="space-y-1.5">
            <p className="text-sm">
              Xác nhận xóa bộ sưu tập <span className="font-bold">{collection.name}</span>?
            </p>
          </div>
        )}

        <div className="flex justify-end gap-2 pt-1">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            Hủy
          </button>
          <button
            onClick={handleDelete}
            disabled={deleting || hasProducts}
            className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60 transition-opacity"
          >
            {deleting ? 'Đang xóa...' : 'Xóa'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
