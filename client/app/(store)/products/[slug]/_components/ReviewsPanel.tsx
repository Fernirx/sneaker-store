'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { avatarUrl, reviewUrl } from '@/lib/cloudinaryUrl';
import ReviewFormModal from './ReviewFormModal';
import { formatReviewDate, type PageMeta, type ReviewItem, type ReviewSummary } from './reviewTypes';

function Stars({ value }: { value: number }) {
  return (
    <span className="text-[13px] tracking-widest leading-none">
      {[1, 2, 3, 4, 5].map(n => (
        <span key={n} className={n <= value ? 'text-accent' : 'text-line'}>★</span>
      ))}
    </span>
  );
}

function Avatar({ name, publicId }: { name: string; publicId: string | null }) {
  if (publicId) {
    return <img src={avatarUrl(publicId, 64)} alt={name} className="w-10 h-10 rounded-full object-cover ring-1 ring-line shrink-0" />;
  }
  return (
    <div className="w-10 h-10 rounded-full bg-accent/10 flex items-center justify-center shrink-0">
      <span className="font-display font-black text-sm text-accent">{name.charAt(0).toUpperCase()}</span>
    </div>
  );
}

export default function ReviewsPanel({
  slug,
  productId,
  isLoggedIn,
  currentUserId,
}: {
  slug: string;
  productId: number;
  isLoggedIn: boolean;
  currentUserId: number | null;
}) {
  const [summary, setSummary]   = useState<ReviewSummary | null>(null);
  const [reviews, setReviews]   = useState<ReviewItem[]>([]);
  const [meta, setMeta]         = useState<PageMeta | null>(null);
  const [page, setPage]         = useState(0);
  const [loading, setLoading]   = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);

  const [formOpen, setFormOpen]     = useState(false);
  const [editTarget, setEditTarget] = useState<ReviewItem | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ReviewItem | null>(null);
  const [deleting, setDeleting]     = useState(false);
  const [deleteError, setDeleteError] = useState('');

  async function fetchSummary() {
    try {
      const { data } = await clientAxios.get(`/api/products/${slug}/reviews/summary`);
      setSummary(data.data as ReviewSummary);
    } catch {
      setSummary(null);
    }
  }

  async function fetchReviews(pageNum: number, append: boolean) {
    if (append) setLoadingMore(true); else setLoading(true);
    try {
      const { data } = await clientAxios.get(
        `/api/products/${slug}/reviews?page=${pageNum}&size=10&sort=createdAt,desc`,
      );
      setReviews(prev => append ? [...prev, ...data.data] : data.data);
      setMeta(data.meta as PageMeta);
      setPage(pageNum);
    } finally {
      if (append) setLoadingMore(false); else setLoading(false);
    }
  }

  useEffect(() => {
    fetchSummary();
    fetchReviews(0, false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [slug]);

  function handleSaved() {
    setFormOpen(false);
    setEditTarget(null);
    fetchSummary();
    fetchReviews(0, false);
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    setDeleteError('');
    try {
      await clientAxios.delete(`/api/me/reviews/${deleteTarget.id}`);
      setReviews(prev => prev.filter(r => r.id !== deleteTarget.id));
      setDeleteTarget(null);
      fetchSummary();
    } catch (err) {
      setDeleteError(parseApiError(err, "Không thể xóa đánh giá").general);
    } finally {
      setDeleting(false);
    }
  }

  const totalReviews = summary?.totalReviews ?? 0;
  const avgRating = summary?.averageRating;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[300px_1fr] gap-8 items-start">
      {/* Left: Rating summary */}
      <div>
        <div className="font-display font-black text-[64px] leading-none tracking-tight">
          {avgRating != null ? avgRating.toFixed(1) : '—'}
        </div>
        <div className="text-[18px] mt-1.5">
          <Stars value={avgRating != null ? Math.round(avgRating) : 0} />
        </div>
        <p className="text-[13px] text-muted mt-1">
          {totalReviews > 0 ? `${totalReviews.toLocaleString('vi-VN')} đánh giá` : "Chưa có đánh giá"}
        </p>

        {/* Rating bars */}
        <div className="space-y-2 mt-[18px]">
          {[5, 4, 3, 2, 1].map(star => {
            const count = summary?.distribution?.[String(star)] ?? 0;
            const pct = totalReviews > 0 ? Math.round((count / totalReviews) * 100) : 0;
            return (
              <div key={star} className="flex items-center gap-2.5">
                <span className="text-[12px] w-3.5 text-right shrink-0">{star}</span>
                <div className="flex-1 h-[7px] bg-line-2 rounded-full overflow-hidden">
                  <div className="h-full bg-accent" style={{ width: `${pct}%` }} />
                </div>
                <span className="text-[11px] text-muted w-7 shrink-0">{pct}%</span>
              </div>
            );
          })}
        </div>

        {isLoggedIn ? (
          <button
            onClick={() => setFormOpen(true)}
            className="w-full mt-[18px] py-3 border-[1.5px] border-line rounded-sm bg-white text-sm font-bold hover:border-ink transition-colors"
          >
            {"Viết đánh giá"}
          </button>
        ) : (
          <Link
            href="/login"
            className="block w-full mt-[18px] py-3 border-[1.5px] border-line rounded-sm bg-white text-sm font-bold text-center hover:border-ink transition-colors"
          >
            {"Đăng nhập để đánh giá"}
          </Link>
        )}
      </div>

      {/* Right: Review list */}
      <div>
        {loading ? (
          <p className="text-sm text-muted py-16 text-center">{"Đang tải..."}</p>
        ) : reviews.length === 0 ? (
          <div className="flex items-center justify-center py-16 text-sm text-muted border-[1.5px] border-line rounded-lg">
            {"Chưa có đánh giá nào."}
          </div>
        ) : (
          <div className="space-y-5">
            {reviews.map(r => (
              <div key={r.id} className="border-b border-line pb-5 last:border-0 last:pb-0">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <Avatar name={r.user.displayName} publicId={r.user.avatarPublicId} />
                    <div>
                      <p className="font-semibold text-sm">{r.user.displayName}</p>
                      <div className="flex items-center gap-2 mt-0.5">
                        <Stars value={r.rating} />
                        <span className="text-xs text-muted">{formatReviewDate(r.createdAt)}</span>
                      </div>
                    </div>
                  </div>
                  {r.user.userId === currentUserId && (
                    <div className="flex gap-3 shrink-0">
                      <button onClick={() => setEditTarget(r)} className="text-xs font-bold text-muted hover:text-ink transition-colors">
                        {"Sửa"}
                      </button>
                      <button onClick={() => { setDeleteTarget(r); setDeleteError(''); }} className="text-xs font-bold text-danger hover:opacity-75 transition-opacity">
                        {"Xóa"}
                      </button>
                    </div>
                  )}
                </div>

                {r.title && <p className="font-semibold text-sm mt-3">{r.title}</p>}
                {r.comment && <p className="text-sm text-ink-2 mt-1 whitespace-pre-line">{r.comment}</p>}

                {r.images.length > 0 && (
                  <div className="flex gap-2 mt-3">
                    {r.images.map(img => (
                      <img
                        key={img.id}
                        src={reviewUrl(img.imagePublicId, 80, 80)}
                        alt=""
                        className="w-16 h-16 object-cover rounded-sm border border-line"
                      />
                    ))}
                  </div>
                )}
              </div>
            ))}

            {meta && !meta.last && (
              <button
                onClick={() => fetchReviews(page + 1, true)}
                disabled={loadingMore}
                className="w-full py-3 border border-line rounded-sm text-sm font-bold hover:bg-paper transition-colors disabled:opacity-50"
              >
                {loadingMore ? "Đang tải..." : "Xem thêm"}
              </button>
            )}
          </div>
        )}
      </div>

      {/* Create/Edit modal */}
      {(formOpen || editTarget) && (
        <ReviewFormModal
          productId={productId}
          initial={editTarget ?? undefined}
          onClose={() => { setFormOpen(false); setEditTarget(null); }}
          onSaved={handleSaved}
        />
      )}

      {/* Delete confirm */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={() => setDeleteTarget(null)}>
          <div className="bg-white rounded-lg shadow-xl w-full max-w-sm p-5 space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="font-display font-black text-sm uppercase tracking-wide">{"Xóa đánh giá"}</h3>
            {deleteError && <p className="text-danger text-sm">{deleteError}</p>}
            <p className="text-sm">{"Xóa đánh giá này?"}</p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">{"Hủy"}</button>
              <button onClick={handleDelete} disabled={deleting}
                className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60">
                {deleting ? "Đang xóa..." : "Xóa"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
