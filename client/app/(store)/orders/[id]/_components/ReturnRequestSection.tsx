'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDateTime } from '../../_components/types';
import {
  RETURN_STATUS_COLORS, RETURN_STATUS_LABELS, RESOLUTION_TYPE_LABELS,
  type ReturnRequestResponse,
} from '../../_components/returnTypes';
import CreateReturnRequestModal from './CreateReturnRequestModal';

const RETURN_WINDOW_DAYS = 30;

export default function ReturnRequestSection({
  orderId,
  deliveredAt,
  isLoggedIn,
}: {
  orderId: number;
  deliveredAt: string | null;
  isLoggedIn: boolean;
}) {
  const [returns, setReturns] = useState<ReturnRequestResponse[]>([]);
  const [loading, setLoading] = useState(isLoggedIn);
  const [modalOpen, setModalOpen] = useState(false);

  const load = useCallback(() => {
    if (!isLoggedIn) return;
    setLoading(true);
    clientAxios.get(`/api/me/returns?orderId=${orderId}`)
      .then(({ data }) => setReturns(data.data ?? []))
      .catch(() => setReturns([]))
      .finally(() => setLoading(false));
  }, [orderId, isLoggedIn]);

  useEffect(() => { load(); }, [load]);

  // Đổi/trả trực tuyến chỉ dành cho khách đã đăng nhập (đơn khách vãng lai không có Customer nên
  // không thể tạo return_requests - xem quyết định). Khách vãng lai muốn đổi/trả liên hệ hotline/email.
  if (!isLoggedIn) {
    return (
      <section className="border border-line rounded-sm overflow-hidden">
        <div className="px-5 py-3.5 border-b border-line bg-line-2">
          <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Đổi/trả hàng"}</h2>
        </div>
        <div className="p-5">
          <p className="text-[13px] text-muted">
            {"Để tạo yêu cầu đổi/trả cho đơn hàng đặt không cần tài khoản, vui lòng liên hệ "}
            <Link href="/help#returns" className="underline hover:text-ink">{"hotline hoặc email hỗ trợ"}</Link>
            {"."}
          </p>
        </div>
      </section>
    );
  }

  if (loading) return null;

  const withinWindow = deliveredAt
    ? (Date.now() - new Date(deliveredAt).getTime()) <= RETURN_WINDOW_DAYS * 24 * 60 * 60 * 1000
    : false;

  return (
    <section className="border border-line rounded-sm overflow-hidden">
      <div className="px-5 py-3.5 border-b border-line bg-line-2 flex items-center justify-between">
        <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Đổi/trả hàng"}</h2>
        {withinWindow && (
          <button
            onClick={() => setModalOpen(true)}
            className="text-[11px] font-bold uppercase tracking-widest text-accent hover:opacity-80 transition-opacity"
          >
            {"+ Tạo yêu cầu"}
          </button>
        )}
      </div>

      <div className="p-5">
        {returns.length === 0 ? (
          <p className="text-[13px] text-muted">
            {withinWindow
              ? "Chưa có yêu cầu đổi/trả nào cho đơn này."
              : "Đơn hàng đã quá 30 ngày kể từ khi nhận hàng, không còn đủ điều kiện đổi/trả."}
          </p>
        ) : (
          <div className="space-y-3">
            {returns.map(r => (
              <Link
                key={r.id}
                href={`/returns/${r.id}`}
                className="flex items-center justify-between gap-3 border border-line rounded-sm px-4 py-3 hover:border-ink transition-colors"
              >
                <div>
                  <p className="text-[13px] font-semibold text-ink">{r.code}</p>
                  <p className="text-[12px] text-muted mt-0.5">
                    {RESOLUTION_TYPE_LABELS[r.resolutionType]} · {formatDateTime(r.createdAt)}
                  </p>
                </div>
                <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide shrink-0 ${RETURN_STATUS_COLORS[r.status]}`}>
                  {RETURN_STATUS_LABELS[r.status]}
                </span>
              </Link>
            ))}
          </div>
        )}
      </div>

      {modalOpen && (
        <CreateReturnRequestModal
          orderId={orderId}
          onClose={() => setModalOpen(false)}
          onCreated={() => { setModalOpen(false); load(); }}
        />
      )}
    </section>
  );
}
