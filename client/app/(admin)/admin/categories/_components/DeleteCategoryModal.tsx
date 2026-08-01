'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type CategoryRow } from './types';

interface CategoryOption { id: number; name: string; }

export default function DeleteCategoryModal({
  category,
  onClose,
  onDeleted,
}: {
  category: CategoryRow;
  onClose: () => void;
  onDeleted: () => void;
}) {
  const hasProducts = category.productCount > 0;
  const [targetId, setTargetId] = useState('');
  const [options, setOptions] = useState<CategoryOption[]>([]);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!hasProducts) return;
    clientAxios.get('/api/admin/categories?page=0&size=200&sort=name,asc')
      .then(res => setOptions((res.data.data ?? []).filter((c: CategoryOption) => c.id !== category.id)))
      .catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleDelete() {
    if (hasProducts && !targetId) {
      setError('Vui lòng chọn danh mục để chuyển sản phẩm sang.');
      return;
    }
    setDeleting(true);
    setError('');
    try {
      const query = hasProducts ? `?reassignTo=${targetId}` : '';
      await clientAxios.delete(`/api/admin/categories/${category.id}${query}`);
      onDeleted();
    } catch (err) {
      setError(parseApiError(err).general);
      setDeleting(false);
    }
  }

  return (
    <Modal title="Xóa danh mục" onClose={onClose}>
      <div className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}
        <p className="text-sm">
          Bạn có chắc muốn xóa danh mục <span className="font-bold">{category.name}</span>?
        </p>

        {hasProducts ? (
          <div className="space-y-1.5">
            <p className="text-xs text-danger">
              Danh mục này đang được gán cho <span className="font-bold">{category.productCount}</span> sản phẩm. Bạn phải chọn danh mục khác để chuyển toàn bộ sản phẩm sang trước khi xóa:
            </p>
            <select
              value={targetId}
              onChange={e => setTargetId(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:border-ink"
            >
              <option value="">— Chọn danh mục —</option>
              {options.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
            </select>
          </div>
        ) : (
          <p className="text-xs text-muted">Danh mục này chưa có sản phẩm nào liên kết.</p>
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
            disabled={deleting || (hasProducts && !targetId)}
            className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60 transition-opacity"
          >
            {deleting ? 'Đang xóa...' : 'Xóa'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
