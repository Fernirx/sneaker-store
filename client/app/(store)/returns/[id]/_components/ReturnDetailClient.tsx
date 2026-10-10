'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { returnUrl } from '@/lib/cloudinaryUrl';
import { formatDateTime } from '../../../orders/_components/types';
import { formatPrice } from '../../../products/_components/types';
import {
  RETURN_STATUS_COLORS, RETURN_STATUS_LABELS, RESOLUTION_TYPE_LABELS,
  type ReturnRequestResponse,
} from '../../../orders/_components/returnTypes';

function DetailSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-6">
      <div className="h-8 w-64 bg-line rounded-sm animate-pulse" />
      <div className="h-48 bg-line rounded-sm animate-pulse" />
    </div>
  );
}

export default function ReturnDetailClient({ returnId }: { returnId: number }) {
  const [item, setItem] = useState<ReturnRequestResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  const [trackingCode, setTrackingCode] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    setNotFound(false);
    clientAxios.get(`/api/me/returns/${returnId}`)
      .then(({ data }) => setItem(data.data))
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [returnId]);

  useEffect(() => { load(); }, [load]);

  async function handleSubmitTracking(e: React.FormEvent) {
    e.preventDefault();
    if (!trackingCode.trim()) return;
    setError('');
    setSaving(true);
    try {
      await clientAxios.patch(`/api/me/returns/${returnId}/tracking`, { trackingCode: trackingCode.trim() });
      load();
    } catch (err) {
      setError(parseApiError(err).general);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <DetailSkeleton />;

  if (notFound || !item) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <p className="text-muted text-[14px]">{"Không tìm thấy yêu cầu đổi/trả."}</p>
        <Link href="/orders" className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors">
          {"Về danh sách đơn hàng"}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="flex items-center gap-3 mb-1">
        <Link href={`/orders/${item.orderId}`} className="text-muted hover:text-ink transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M11 6l-6 6 6 6" />
          </svg>
        </Link>
        <h1 className="font-display font-black text-2xl uppercase tracking-tight">{item.code}</h1>
        <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide ${RETURN_STATUS_COLORS[item.status]}`}>
          {RETURN_STATUS_LABELS[item.status]}
        </span>
      </div>
      <p className="text-[12px] text-muted mb-8 ml-7">
        {"Đơn hàng"} <Link href={`/orders/${item.orderId}`} className="underline hover:text-ink">{item.orderCode}</Link> · {formatDateTime(item.createdAt)}
      </p>

      <div className="space-y-6">
        <section className="border border-line rounded-sm overflow-hidden">
          <div className="px-5 py-3.5 border-b border-line bg-line-2">
            <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Sản phẩm"}</h2>
          </div>
          <div className="px-5">
            {item.items.map(i => (
              <div key={i.id} className="py-4 border-b border-line last:border-b-0">
                <div className="flex justify-between gap-4">
                  <div className="flex-1 min-w-0">
                    <p className="text-[13px] font-semibold text-ink leading-snug">{i.productName}</p>
                    <p className="text-[12px] text-muted mt-0.5">{i.variantColor} · Size {i.variantSize} · SL {i.quantity}</p>
                  </div>
                  <span className="text-[14px] font-bold tabular-nums text-ink shrink-0">{formatPrice(i.refundAmount)}</span>
                </div>
                {i.exchangeVariantId && (
                  <p className="text-[12px] text-accent mt-1">
                    {"Đổi sang: "}{i.exchangeVariantColorway} · Size {i.exchangeVariantSize}
                  </p>
                )}
              </div>
            ))}
          </div>
          <div className="px-5 py-3 border-t border-line bg-line-2 flex justify-between text-[13px]">
            <span className="text-muted">{"Hình thức xử lý"}:</span>
            <span className="font-bold text-ink">{RESOLUTION_TYPE_LABELS[item.resolutionType]}</span>
          </div>
        </section>

        <section className="border border-line rounded-sm overflow-hidden">
          <div className="px-5 py-3.5 border-b border-line bg-line-2">
            <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Chi tiết yêu cầu"}</h2>
          </div>
          <div className="p-5 space-y-2 text-[13px]">
            <div className="flex justify-between gap-4">
              <span className="text-muted shrink-0">{"Lý do"}:</span>
              <span className="text-ink text-right">{item.reason}</span>
            </div>
            {item.rejectReason && (
              <div className="flex justify-between gap-4">
                <span className="text-muted shrink-0">{"Lý do từ chối"}:</span>
                <span className="text-danger text-right">{item.rejectReason}</span>
              </div>
            )}
            {item.trackingCode && (
              <div className="flex justify-between">
                <span className="text-muted">{"Mã vận đơn tự gửi"}:</span>
                <span className="text-ink font-medium font-body">{item.trackingCode}</span>
              </div>
            )}
            {item.refundAmount != null && (
              <div className="flex justify-between">
                <span className="text-muted">{"Số tiền hoàn"}:</span>
                <span className="text-ink font-bold">{formatPrice(item.refundAmount)}</span>
              </div>
            )}
            {item.refundedAt && (
              <div className="flex justify-between">
                <span className="text-muted">{"Đã hoàn tiền lúc"}:</span>
                <span className="text-ink">{formatDateTime(item.refundedAt)}</span>
              </div>
            )}
            {item.exchangeShippingOrderCode && (
              <div className="flex justify-between">
                <span className="text-muted">{"Mã vận đơn hàng đổi"}:</span>
                <span className="text-ink font-medium font-body">{item.exchangeShippingOrderCode}</span>
              </div>
            )}
            {item.exchangeExpectedDeliveryAt && (
              <div className="flex justify-between">
                <span className="text-muted">{"Dự kiến giao hàng đổi"}:</span>
                <span className="text-ink">{formatDateTime(item.exchangeExpectedDeliveryAt)}</span>
              </div>
            )}
          </div>
        </section>

        {item.imagePublicIds && item.imagePublicIds.length > 0 && (
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Ảnh minh chứng"}</h2>
            </div>
            <div className="p-5 flex flex-wrap gap-2">
              {item.imagePublicIds.map(publicId => (
                <img key={publicId} src={returnUrl(publicId, 96, 96)} alt="" className="w-20 h-20 object-cover rounded-sm border border-line" />
              ))}
            </div>
          </section>
        )}

        {item.status === 'APPROVED' && (
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Gửi hàng trả về"}</h2>
            </div>
            <div className="p-5 space-y-3">
              <p className="text-[13px] text-muted">
                {"Yêu cầu đã được duyệt. Vui lòng tự đóng gói và gửi hàng về cửa hàng qua bưu điện/đơn vị vận chuyển bất kỳ, sau đó nhập mã vận đơn bên dưới để cửa hàng đối chiếu."}
              </p>
              {error && <p className="text-[12px] text-danger">{error}</p>}
              <form onSubmit={handleSubmitTracking} className="flex gap-2">
                <input
                  value={trackingCode}
                  onChange={e => setTrackingCode(e.target.value)}
                  placeholder={item.trackingCode ?? 'Nhập mã vận đơn...'}
                  className="flex-1 border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
                />
                <button type="submit" disabled={saving}
                  className="bg-ink text-white text-[12px] font-bold uppercase tracking-widest px-5 py-2 rounded-sm hover:bg-accent transition-colors disabled:opacity-50">
                  {saving ? '...' : (item.trackingCode ? 'Cập nhật' : 'Lưu')}
                </button>
              </form>
            </div>
          </section>
        )}
      </div>
    </div>
  );
}
