'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import ImageUpload from '@/components/admin/ImageUpload';
import TiptapEditor from '@/components/admin/TiptapEditor';
import { type CollectionRow } from './types';

export default function EditCollectionModal({
  collection,
  onClose,
  onSaved,
}: {
  collection: CollectionRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [name, setName] = useState(collection.name);
  const [description, setDescription] = useState(collection.description ?? '');
  const [imagePublicId, setImagePublicId] = useState(collection.imagePublicId ?? '');
  const [launchDate, setLaunchDate] = useState(collection.launchDate ?? '');
  const [endDate, setEndDate] = useState(collection.endDate ?? '');
  const [active, setActive] = useState(collection.active);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/collections/${collection.id}`, {
        name,
        description: description || null,
        imagePublicId: imagePublicId || null,
        launchDate: launchDate || null,
        endDate: endDate || null,
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
    <Modal title="Cập nhật bộ sưu tập" onClose={onClose} maxWidth="2xl">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}

        <p className="text-xs text-muted">slug: {collection.slug}</p>

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

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mô tả</label>
          <TiptapEditor value={description} onChange={setDescription} />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Ảnh</label>
          <ImageUpload value={imagePublicId} folder="collections" onChange={setImagePublicId} />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Ngày ra mắt
            </label>
            <input
              type="date"
              value={launchDate}
              max={endDate || undefined}
              onChange={e => setLaunchDate(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
            {fieldErrors.launchDate && <p className="text-danger text-xs mt-1">{fieldErrors.launchDate}</p>}
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Ngày kết thúc
            </label>
            <input
              type="date"
              value={endDate}
              min={launchDate || undefined}
              onChange={e => setEndDate(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
            {fieldErrors.endDate && <p className="text-danger text-xs mt-1">{fieldErrors.endDate}</p>}
          </div>
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
            disabled={saving || !name.trim()}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang lưu...' : 'Lưu'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
