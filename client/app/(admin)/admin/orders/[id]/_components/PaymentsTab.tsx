'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { formatPrice, formatDateTime, type PaymentInternalResponse } from '../../_components/types';

export default function PaymentsTab({ orderId }: { orderId: number }) {
  const [payments, setPayments] = useState<PaymentInternalResponse[]>([]);
  const [loading, setLoading]   = useState(true);

  useEffect(() => {
    clientAxios
      .get(`/api/admin/payments?orderId=${orderId}&size=50`)
      .then(({ data }) => setPayments(data.data ?? []))
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) return <div className="h-32 bg-line rounded-sm animate-pulse" />;

  return (
    <div className="bg-white border border-line rounded-sm overflow-x-auto">
      {payments.length === 0 ? (
        <p className="text-sm text-muted text-center py-8">Chưa có giao dịch thanh toán nào.</p>
      ) : (
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Mã giao dịch</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Mã phản hồi</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Số tiền</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Trạng thái</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Thời gian</th>
            </tr>
          </thead>
          <tbody>
            {payments.map(p => (
              <tr key={p.id} className="border-b border-line-2 last:border-0">
                <td className="px-4 py-3 font-body text-xs">{p.transactionId ?? '—'}</td>
                <td className="px-4 py-3 font-body text-xs text-muted">{p.responseCode ?? '—'}</td>
                <td className="px-4 py-3 text-right tabular-nums">{formatPrice(p.amount)}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                    p.status === 'SUCCESS' ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'
                  }`}>
                    {p.status === 'SUCCESS' ? 'Thành công' : 'Thất bại'}
                  </span>
                </td>
                <td className="px-4 py-3 text-xs text-muted">{formatDateTime(p.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
