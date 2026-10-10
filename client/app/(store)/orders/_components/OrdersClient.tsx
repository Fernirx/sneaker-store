'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { getGuestToken, guestHeaders } from '@/lib/guestToken';
import { formatPrice } from '../../products/_components/types';
import { formatDateTime, STATUS_COLORS, STATUS_LABELS, type OrderResponse, type OrderStatus, type PageMeta } from './types';

function StatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide ${STATUS_COLORS[status]}`}>
      {STATUS_LABELS[status]}
    </span>
  );
}

function OrdersSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="h-8 w-48 bg-line rounded-sm animate-pulse mb-8" />
      <div className="space-y-3">
        {[1, 2, 3].map(i => (
          <div key={i} className="h-24 bg-line rounded-sm animate-pulse" />
        ))}
      </div>
    </div>
  );
}

export default function OrdersClient({ isLoggedIn }: { isLoggedIn: boolean }) {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [meta, setMeta] = useState<PageMeta | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn && !getGuestToken()) {
      setOrders([]);
      setMeta(null);
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    clientAxios
      .get(`/api/orders?page=${page}&size=10&sort=createdAt,desc`, { headers: guestHeaders() })
      .then(({ data }) => {
        if (cancelled) return;
        setOrders(data.data ?? []);
        setMeta(data.meta ?? null);
      })
      .catch(() => {
        if (!cancelled) { setOrders([]); setMeta(null); }
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [isLoggedIn, page]);

  if (loading) return <OrdersSkeleton />;

  if (orders.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" className="text-line">
          <path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        <p className="text-muted text-[14px]">{"Bạn chưa có đơn hàng nào."}</p>
        <Link
          href="/products"
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {"Tiếp tục mua sắm"}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-display font-black text-3xl uppercase tracking-tight mb-8">{"Đơn hàng của tôi"}</h1>

      <div className="space-y-3">
        {orders.map(order => (
          <Link
            key={order.id}
            href={`/orders/${order.id}`}
            className="block border border-line rounded-sm p-4 hover:border-ink transition-colors"
          >
            <div className="flex items-center justify-between gap-3 mb-2">
              <span className="font-body font-bold text-[13px] text-ink">{order.code}</span>
              <StatusBadge status={order.status} />
            </div>
            <div className="flex items-center justify-between text-[12px] text-muted">
              <span>{formatDateTime(order.createdAt)} · {order.items.length} sản phẩm</span>
              <span className="text-[14px] font-bold text-ink tabular-nums">{formatPrice(order.totalAmount)}</span>
            </div>
            {order.shipment?.expectedDeliveryAt && (
              <p className="text-[11px] text-muted mt-1">Dự kiến giao: {formatDateTime(order.shipment.expectedDeliveryAt)}</p>
            )}
          </Link>
        ))}
      </div>

      {meta && meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm mt-6">
          <span className="text-muted text-xs">{meta.totalElements}</span>
          <div className="flex items-center gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              ←
            </button>
            <span className="px-2 text-xs text-muted">{page + 1} / {meta.totalPages}</span>
            <button
              disabled={meta.last}
              onClick={() => setPage(p => p + 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
