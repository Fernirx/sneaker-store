'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type BrandRow } from './types';

interface BrandOption { id: number; name: string; }

export default function DeleteBrandModal({
  brand,
  onClose,
  onDeleted,
}: {
  brand: BrandRow;
  onClose: () => void;
  onDeleted: () => void;
}) {
  const hasProducts = brand.productCount > 0;
  const [targetId, setTargetId] = useState('');
  const [options, setOptions] = useState<BrandOption[]>([]);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!hasProducts) return;
    clientAxios.get('/api/admin/brands?page=0&size=200&sort=name,asc')
      .then(res => setOptions((res.data.data ?? []).filter((b: BrandOption) => b.id !== brand.id)))
      .catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function handleDelete() {
    if (hasProducts && !targetId) {
      setError('Vui lòng chọn thương hiệu để chuyển sản phẩm sang.');
      return;
    }
    setDeleting(true);
    setError('');
    try {
      const query = hasProducts ? `?reassignTo=${targetId}` : '';
      await clientAxios.delete(`/api/admin/brands/${brand.id}${query}`);
      onDeleted();
    } catch (err) {
      setError(parseApiError(err).general);
      setDeleting(false);
    }
  }

  return (
    <Modal title="Xóa thương hiệu" onClose={onClose}>
      <div className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}
        <p className="text-sm">
          Bạn có chắc muốn xóa thương hiệu <span className="font-bold">{brand.name}</span>?
        </p>

        {hasProducts ? (
          <div className="space-y-1.5">
            <p className="text-xs text-danger">
              Thương hiệu này đang có <span className="font-bold">{brand.productCount}</span> sản phẩm. Thương hiệu là trường bắt buộc của sản phẩm nên bạn phải chọn thương hiệu khác để chuyển toàn bộ sản phẩm sang trước khi xóa:
            </p>
            <select
              value={targetId}
              onChange={e => setTargetId(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:border-ink"
            >
              <option value="">— Chọn thương hiệu —</option>
              {options.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
            </select>
          </div>
        ) : (
          <p className="text-xs text-muted">Thương hiệu này chưa có sản phẩm nào liên kết.</p>
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
