'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type CategoryRow } from './types';

interface CategoryOption { id: number; name: string; }

type Mode = 'reassign' | 'unlink';

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
  const [mode, setMode] = useState<Mode>(hasProducts ? 'reassign' : 'unlink');
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
    if (hasProducts && mode === 'reassign' && !targetId) {
      setError('Vui lòng chọn danh mục để chuyển sản phẩm sang.');
      return;
    }
    setDeleting(true);
    setError('');
    try {
      const query = hasProducts && mode === 'reassign' ? `?reassignTo=${targetId}` : '';
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

        {hasProducts && (
          <div className="space-y-2">
            <p className="text-xs text-danger">
              Danh mục này đang được gán cho <span className="font-bold">{category.productCount}</span> sản phẩm. Chọn cách xử lý:
            </p>

            <label className={`flex items-start gap-2.5 p-3 border rounded-sm cursor-pointer transition-colors ${mode === 'reassign' ? 'border-ink bg-paper' : 'border-line hover:bg-paper/50'}`}>
              <input type="radio" name="mode" checked={mode === 'reassign'} onChange={() => setMode('reassign')} className="mt-0.5 accent-ink" />
              <span className="flex-1 space-y-1.5">
                <span className="block text-sm font-bold">Chuyển toàn bộ sản phẩm sang danh mục khác</span>
                {mode === 'reassign' && (
                  <select
                    value={targetId}
                    onChange={e => setTargetId(e.target.value)}
                    className="w-full border border-line rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:border-ink"
                  >
                    <option value="">— Chọn danh mục —</option>
                    {options.map(o => <option key={o.id} value={o.id}>{o.name}</option>)}
                  </select>
                )}
              </span>
            </label>

            <label className={`flex items-start gap-2.5 p-3 border rounded-sm cursor-pointer transition-colors ${mode === 'unlink' ? 'border-ink bg-paper' : 'border-line hover:bg-paper/50'}`}>
              <input type="radio" name="mode" checked={mode === 'unlink'} onChange={() => setMode('unlink')} className="mt-0.5 accent-ink" />
              <span className="block text-sm font-bold">Gỡ danh mục khỏi sản phẩm (không chuyển sang đâu)</span>
            </label>
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
            disabled={deleting}
            className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60 transition-opacity"
          >
            {deleting ? 'Đang xóa...' : 'Xóa'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
