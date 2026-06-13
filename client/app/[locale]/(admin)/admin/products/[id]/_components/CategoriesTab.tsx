'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';

interface CategoryOption {
  id: number;
  name: string;
  slug: string;
  parentName: string | null;
}

interface CategoryBrief {
  id: number;
  name: string;
  slug: string;
}

export default function CategoriesTab({ productId, isAdmin }: { productId: number; isAdmin: boolean }) {
  const [allCategories, setAllCategories] = useState<CategoryOption[]>([]);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      clientAxios.get('/api/admin/categories?page=0&size=200&sort=displayOrder,asc'),
      clientAxios.get(`/api/admin/products/${productId}/categories`),
    ]).then(([allRes, assignedRes]) => {
      setAllCategories(allRes.data.data ?? []);
      const assigned: CategoryBrief[] = assignedRes.data.data ?? [];
      setSelected(new Set(assigned.map(c => c.id)));
    }).catch(() => {
      setError('Không tải được danh sách danh mục.');
    }).finally(() => {
      setLoading(false);
    });
  }, [productId]);

  function toggle(id: number) {
    setSelected(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
    setSuccess('');
  }

  async function handleSave() {
    setSaving(true); setError(''); setSuccess('');
    try {
      await clientAxios.put(`/api/admin/products/${productId}/categories`, {
        categoryIds: Array.from(selected),
      });
      setSuccess('Đã lưu danh mục.');
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p className="text-muted text-sm py-10 text-center">Đang tải...</p>;

  return (
    <div className="max-w-lg space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="font-display font-bold text-sm uppercase tracking-wide text-muted">
          Đã chọn: {selected.size} danh mục
        </h2>
        {isAdmin && (
          <button onClick={handleSave} disabled={saving}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors">
            {saving ? 'Đang lưu...' : 'Lưu danh mục'}
          </button>
        )}
      </div>

      {error   && <p className="text-danger text-sm">{error}</p>}
      {success && <p className="text-ok text-sm">{success}</p>}

      {allCategories.length === 0 ? (
        <p className="text-muted text-sm py-8 text-center border border-dashed border-line rounded-sm">
          Chưa có danh mục nào.
        </p>
      ) : (
        <div className="border border-line rounded-sm divide-y divide-line">
          {allCategories.map(c => (
            <label key={c.id} className="flex items-center gap-3 px-4 py-2.5 hover:bg-paper/50 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={selected.has(c.id)}
                onChange={() => isAdmin && toggle(c.id)}
                disabled={!isAdmin}
                className="accent-accent"
              />
              <span className="flex-1 text-sm">{c.name}</span>
              {c.parentName && (
                <span className="text-xs text-muted">{c.parentName}</span>
              )}
            </label>
          ))}
        </div>
      )}
    </div>
  );
}
