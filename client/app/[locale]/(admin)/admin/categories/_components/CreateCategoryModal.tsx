'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import ImageUpload from '@/components/admin/ImageUpload';
import TranslationSection, { initTranslations, buildTranslationsPayload, type TranslationDraft } from '@/components/admin/TranslationSection';

interface ParentOption {
  id: number;
  name: string;
}

export default function CreateCategoryModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: () => void;
}) {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [imagePublicId, setImagePublicId] = useState('');
  const [displayOrder, setDisplayOrder] = useState('0');
  const [parentId, setParentId] = useState('');
  const [translations, setTranslations] = useState<TranslationDraft[]>(() => initTranslations());
  const [parents, setParents] = useState<ParentOption[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    clientAxios.get('/api/admin/categories?page=0&size=100&sort=displayOrder,asc')
      .then(res => setParents(res.data.data ?? []))
      .catch(() => {});
  }, []);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      const translationsPayload = buildTranslationsPayload(translations);
      await clientAxios.post('/api/admin/categories', {
        name,
        description: description || null,
        imagePublicId: imagePublicId || null,
        displayOrder: Number(displayOrder),
        parentId: parentId ? Number(parentId) : null,
        translations: translationsPayload.length ? translationsPayload : undefined,
      });
      onCreated();
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Tạo danh mục mới" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Tên <span className="text-danger">*</span>
          </label>
          <input
            type="text"
            value={name}
            onChange={e => setName(e.target.value)}
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
            {fieldErrors.displayOrder && <p className="text-danger text-xs mt-1">{fieldErrors.displayOrder}</p>}
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
          <textarea
            value={description}
            onChange={e => setDescription(e.target.value)}
            rows={2}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
          />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Ảnh</label>
          <ImageUpload value={imagePublicId} folder="categories" onChange={setImagePublicId} />
        </div>

        <TranslationSection value={translations} onChange={setTranslations} />

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
            disabled={saving}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang lưu...' : 'Lưu'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
