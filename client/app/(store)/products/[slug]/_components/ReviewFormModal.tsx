'use client';

import { useEffect, useRef, useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { reviewUrl } from '@/lib/cloudinaryUrl';
import type { ReviewImageItem, ReviewItem } from './reviewTypes';

const MAX_IMAGES = 5;

function StarPicker({ value, onChange }: { value: number; onChange: (v: number) => void }) {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map(n => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          className={`text-2xl leading-none transition-colors ${n <= value ? 'text-accent' : 'text-line'}`}
          aria-label={`${n} sao`}
        >
          ★
        </button>
      ))}
    </div>
  );
}

export default function ReviewFormModal({
  productId,
  initial,
  onClose,
  onSaved,
}: {
  productId: number;
  initial?: ReviewItem;
  onClose: () => void;
  onSaved: (review: ReviewItem) => void;
}) {
  const isEdit = !!initial;

  const [rating, setRating]   = useState(initial?.rating ?? 5);
  const [title, setTitle]     = useState(initial?.title ?? '');
  const [comment, setComment] = useState(initial?.comment ?? '');

  const [existingImages, setExistingImages] = useState<ReviewImageItem[]>(initial?.images ?? []);
  const [pendingFiles, setPendingFiles]      = useState<File[]>([]);
  const [previews, setPreviews]              = useState<string[]>([]);

  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');
  const fileRef = useRef<HTMLInputElement>(null);

  const totalImageCount = existingImages.length + pendingFiles.length;

  useEffect(() => {
    return () => { previews.forEach(URL.revokeObjectURL); };
  }, [previews]);

  function handleFilesChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    e.target.value = '';
    if (!files.length) return;
    const room = MAX_IMAGES - totalImageCount;
    const accepted = files.slice(0, Math.max(0, room));
    setPendingFiles(prev => [...prev, ...accepted]);
    setPreviews(prev => [...prev, ...accepted.map(f => URL.createObjectURL(f))]);
  }

  function removeExisting(id: number) {
    setExistingImages(prev => prev.filter(img => img.id !== id));
  }

  function removePending(idx: number) {
    URL.revokeObjectURL(previews[idx]);
    setPendingFiles(prev => prev.filter((_, i) => i !== idx));
    setPreviews(prev => prev.filter((_, i) => i !== idx));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const uploadedIds: string[] = [];
      for (const file of pendingFiles) {
        const form = new FormData();
        form.append('file', file);
        form.append('folder', 'reviews');
        const { data } = await clientAxios.post('/api/upload', form);
        uploadedIds.push(data.publicId);
      }

      const body = {
        rating,
        title: title.trim() || null,
        comment: comment.trim() || null,
        imagePublicIds: [...existingImages.map(img => img.imagePublicId), ...uploadedIds],
      };

      const { data } = isEdit
        ? await clientAxios.patch(`/api/me/reviews/${initial!.id}`, body)
        : await clientAxios.post('/api/me/reviews', { productId, ...body });

      onSaved(data.data as ReviewItem);
    } catch (err) {
      setError(parseApiError(err, "Không thể lưu đánh giá").general);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm" onClick={onClose}>
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-line">
          <h3 className="font-display font-black text-base uppercase tracking-tight">
            {isEdit ? "Sửa đánh giá" : "Viết đánh giá"}
          </h3>
          <button onClick={onClose} className="text-muted hover:text-ink transition-colors p-1">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{error}</p>
          )}

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">Đánh giá của bạn</label>
            <StarPicker value={rating} onChange={setRating} />
          </div>

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">Tiêu đề (tùy chọn)</label>
            <input
              value={title}
              onChange={e => setTitle(e.target.value)}
              maxLength={255}
              className="w-full border border-line rounded px-3 py-2.5 text-sm focus:outline-none focus:border-ink transition-colors"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">Nội dung (tùy chọn)</label>
            <textarea
              value={comment}
              onChange={e => setComment(e.target.value)}
              rows={4}
              className="w-full border border-line rounded px-3 py-2.5 text-sm focus:outline-none focus:border-ink transition-colors resize-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
              Ảnh ({totalImageCount}/{MAX_IMAGES})
            </label>
            <div className="flex flex-wrap gap-2">
              {existingImages.map(img => (
                <div key={img.id} className="relative">
                  <img src={reviewUrl(img.imagePublicId, 72, 72)} alt="" className="w-16 h-16 object-cover rounded-sm border border-line" />
                  <button
                    type="button"
                    onClick={() => removeExisting(img.id)}
                    className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-danger text-white rounded-full text-[10px] font-bold flex items-center justify-center leading-none hover:opacity-80"
                  >
                    ×
                  </button>
                </div>
              ))}
              {previews.map((src, i) => (
                <div key={i} className="relative">
                  <img src={src} alt="" className="w-16 h-16 object-cover rounded-sm border border-line" />
                  <button
                    type="button"
                    onClick={() => removePending(i)}
                    className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-danger text-white rounded-full text-[10px] font-bold flex items-center justify-center leading-none hover:opacity-80"
                  >
                    ×
                  </button>
                </div>
              ))}
              {totalImageCount < MAX_IMAGES && (
                <button
                  type="button"
                  onClick={() => fileRef.current?.click()}
                  className="w-16 h-16 border border-dashed border-line rounded-sm flex items-center justify-center text-muted hover:border-ink hover:text-ink transition-colors text-xl"
                >
                  +
                </button>
              )}
            </div>
            <input
              ref={fileRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              className="hidden"
              onChange={handleFilesChange}
            />
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} disabled={saving}
              className="flex-1 border border-line rounded py-2.5 text-sm font-semibold text-ink-2 hover:border-ink hover:text-ink transition-colors disabled:opacity-40">
              Hủy
            </button>
            <button type="submit" disabled={saving}
              className="flex-1 bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-2.5 rounded transition-colors">
              {saving ? '...' : "Lưu"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
