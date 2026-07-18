'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import ImageUpload from '@/components/admin/ImageUpload';

export default function CreateBannerModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: () => void;
}) {
  const [title, setTitle] = useState('');
  const [imagePublicId, setImagePublicId] = useState('');
  const [linkUrl, setLinkUrl] = useState('');
  const [displayOrder, setDisplayOrder] = useState('0');
  const [startAt, setStartAt] = useState('');
  const [endAt, setEndAt] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.post('/api/admin/banners', {
        title,
        imagePublicId: imagePublicId || null,
        linkUrl: linkUrl || null,
        displayOrder: displayOrder ? Number(displayOrder) : 0,
        startAt: startAt || null,
        endAt: endAt || null,
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
    <Modal title="Tạo banner mới" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Tiêu đề <span className="text-danger">*</span>
          </label>
          <input
            type="text"
            value={title}
            onChange={e => setTitle(e.target.value)}
            required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.title && <p className="text-danger text-xs mt-1">{fieldErrors.title}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Ảnh</label>
          <ImageUpload value={imagePublicId} folder="banners" onChange={setImagePublicId} />
          {fieldErrors.imagePublicId && <p className="text-danger text-xs mt-1">{fieldErrors.imagePublicId}</p>}
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Liên kết khi bấm vào
          </label>
          <input
            type="text"
            placeholder="/collections/xyz hoặc /products?onSale=true"
            value={linkUrl}
            onChange={e => setLinkUrl(e.target.value)}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          />
          {fieldErrors.linkUrl && <p className="text-danger text-xs mt-1">{fieldErrors.linkUrl}</p>}
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
              onChange={e => setEndAt(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
        </div>
        <p className="text-xs text-muted -mt-2">Để trống nếu muốn hiển thị ngay và không giới hạn thời gian.</p>

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
