'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDateTime, STATUS_LABELS, type OrderStatusHistoryResponse } from '../../_components/types';

export default function HistoryTab({ orderId }: { orderId: number }) {
  const [history, setHistory] = useState<OrderStatusHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    clientAxios
      .get(`/api/admin/orders/${orderId}/history`)
      .then(({ data }) => setHistory(data.data ?? []))
      .finally(() => setLoading(false));
  }, [orderId]);

  if (loading) return <div className="h-32 bg-line rounded-sm animate-pulse" />;

  return (
    <div className="bg-white border border-line rounded-sm p-5">
      {history.length === 0 ? (
        <p className="text-sm text-muted text-center py-8">Chưa có lịch sử.</p>
      ) : (
        <div className="space-y-4">
          {history.map(h => (
            <div key={h.id} className="flex items-start gap-3 text-sm">
              <span className="w-1.5 h-1.5 rounded-full bg-ink mt-1.5 shrink-0" />
              <div className="flex-1">
                <p className="font-medium">
                  {h.oldStatus ? `${STATUS_LABELS[h.oldStatus]} → ${STATUS_LABELS[h.newStatus]}` : STATUS_LABELS[h.newStatus]}
                </p>
                {h.note && <p className="text-xs text-muted mt-0.5">{h.note}</p>}
                <p className="text-xs text-faint mt-0.5">{formatDateTime(h.createdAt)}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
