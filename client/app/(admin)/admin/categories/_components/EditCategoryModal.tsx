'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import ImageUpload from '@/components/admin/ImageUpload';
import TiptapEditor from '@/components/admin/TiptapEditor';
import { type CategoryRow } from './types';

interface ParentOption {
  id: number;
  name: string;
}

export default function EditCategoryModal({
  category,
  onClose,
  onSaved,
}: {
  category: CategoryRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [name, setName] = useState(category.name);
  const [description, setDescription] = useState(category.description ?? '');
  const [imagePublicId, setImagePublicId] = useState(category.imagePublicId ?? '');
  const [displayOrder, setDisplayOrder] = useState(String(category.displayOrder));
  const [active, setActive] = useState(category.active);
  const [parentId, setParentId] = useState(category.parentId ? String(category.parentId) : '');
  const [parents, setParents] = useState<ParentOption[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    clientAxios.get('/api/admin/categories?page=0&size=100&sort=displayOrder,asc')
      .then(res => setParents((res.data.data ?? []).filter((p: ParentOption) => p.id !== category.id)))
      .catch(() => {});
  }, [category.id]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/categories/${category.id}`, {
        name,
        description: description || null,
        imagePublicId: imagePublicId || null,
        displayOrder: Number(displayOrder),
        parentId: parentId ? Number(parentId) : null,
        clearParent: !parentId,
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
    <Modal title="Cập nhật danh mục" onClose={onClose} maxWidth="2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}

        <p className="text-xs text-muted">slug: {category.slug}</p>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Tên <span className="text-danger">*</span>
          </label>
          <input
            type="text"
            value={name}
            onChange={e => setName(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.name && <p className="text-danger text-xs mt-1">{fieldErrors.name}</p>}
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Thứ tự hiển thị <span className="text-danger">*</span>
            </label>
            <input
              type="number"
              value={displayOrder}
              onChange={e => setDisplayOrder(e.target.value)}
              min="0"
              required
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>

          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Danh mục cha
            </label>
            <select
              value={parentId}
              onChange={e => setParentId(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white"
            >
              <option value="">— Không có —</option>
              {parents.map(p => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mô tả</label>
          <TiptapEditor value={description} onChange={setDescription} />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Ảnh</label>
          <ImageUpload value={imagePublicId} folder="categories" onChange={setImagePublicId} />
        </div>

        <div>
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={active}
              onChange={e => setActive(e.target.checked)}
              className="accent-accent"
            />
            <span className="text-[11px] font-bold uppercase tracking-wider">Đang hoạt động</span>
          </label>
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
            disabled={saving || !name.trim() || displayOrder === ''}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang lưu...' : 'Lưu'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
