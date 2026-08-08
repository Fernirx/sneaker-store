'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import ImageUpload from '@/components/admin/ImageUpload';
import { type BannerRow } from './types';

function toLocalInput(iso: string | null) {
  if (!iso) return '';
  return iso.slice(0, 16);
}

export default function EditBannerModal({
  banner,
  onClose,
  onSaved,
}: {
  banner: BannerRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [title, setTitle] = useState(banner.title);
  const [imagePublicId, setImagePublicId] = useState(banner.imagePublicId ?? '');
  const [linkUrl, setLinkUrl] = useState(banner.linkUrl ?? '');
  const [displayOrder, setDisplayOrder] = useState(String(banner.displayOrder));
  const [startAt, setStartAt] = useState(toLocalInput(banner.startAt));
  const [endAt, setEndAt] = useState(toLocalInput(banner.endAt));
  const [active, setActive] = useState(banner.active);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/banners/${banner.id}`, {
        title,
        imagePublicId: imagePublicId || null,
        linkUrl: linkUrl || null,
        displayOrder: displayOrder ? Number(displayOrder) : 0,
        startAt: startAt || null,
        endAt: endAt || null,
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
    <Modal title="Cập nhật banner" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Tiêu đề <span className="text-danger">*</span>
          </label>
          <input
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value.replace(/^\s+/, ''))}
            required
            maxLength={150}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.title && <p className="text-danger text-xs mt-1">{fieldErrors.title}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Ảnh <span className="text-danger">*</span>
          </label>
          <ImageUpload value={imagePublicId} folder="banners" onChange={setImagePublicId} />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Liên kết khi bấm vào
          </label>
          <input
            type="text"
            value={linkUrl}
            onChange={e => setLinkUrl(e.target.value.replace(/^\s+/, ''))}
            maxLength={500}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Thứ tự hiển thị
          </label>
          <input
            type="number"
            value={displayOrder}
            onChange={e => setDisplayOrder(e.target.value)}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Bắt đầu hiển thị
            </label>
            <input
              type="datetime-local"
              value={startAt}
              max={endAt || undefined}
              onChange={e => setStartAt(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Kết thúc hiển thị
            </label>
            <input
              type="datetime-local"
              value={endAt}
              min={startAt || undefined}
              onChange={e => setEndAt(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
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
            disabled={saving || !title.trim() || !imagePublicId.trim()}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang lưu...' : 'Lưu'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
