'use client';

import { useState, useEffect, useCallback } from 'react';
import { useTranslations } from 'next-intl';
import { Link } from '@/i18n/routing';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { guestHeaders } from '@/lib/guestToken';
import { formatPrice } from '../../../products/_components/types';
import {
  formatDateTime, STATUS_COLORS,
  type OrderResponse, type OrderStatusHistoryResponse, type OrderStatus,
} from '../../_components/types';

function StatusBadge({ status }: { status: OrderStatus }) {
  const t = useTranslations('orders');
  return (
    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide ${STATUS_COLORS[status]}`}>
      {t(`statusValues.${status}`)}
    </span>
  );
}

function OrderDetailSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-6">
      <div className="h-8 w-64 bg-line rounded-sm animate-pulse" />
      <div className="h-48 bg-line rounded-sm animate-pulse" />
      <div className="h-32 bg-line rounded-sm animate-pulse" />
    </div>
  );
}

export default function OrderDetailClient({ orderId }: { orderId: number }) {
  const t = useTranslations('orders');
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [history, setHistory] = useState<OrderStatusHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setLoading(true);
    setNotFound(false);
    Promise.all([
      clientAxios.get(`/api/orders/${orderId}`, { headers: guestHeaders() }),
      clientAxios.get(`/api/orders/${orderId}/history`, { headers: guestHeaders() }),
    ])
      .then(([orderRes, historyRes]) => {
        setOrder(orderRes.data.data);
        setHistory(historyRes.data.data ?? []);
      })
      .catch(() => setNotFound(true))
      .finally(() => setLoading(false));
  }, [orderId]);

  useEffect(() => { load(); }, [load]);

  async function handleCancel() {
    setError('');
    setCancelling(true);
    try {
      await clientAxios.patch(`/api/orders/${orderId}/cancel`, null, { headers: guestHeaders() });
      setConfirmOpen(false);
      load();
    } catch (err) {
      const { general } = parseApiError(err);
      setError(general);
    } finally {
      setCancelling(false);
    }
  }

  if (loading) return <OrderDetailSkeleton />;

  if (notFound || !order) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <p className="text-muted text-[14px]">{t('notFound')}</p>
        <Link
          href="/orders"
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {t('backToList')}
        </Link>
      </div>
    );
  }

  const fullAddress = [order.shippingStreet, order.shippingWard, order.shippingDistrict, order.shippingProvince]
    .filter(Boolean)
    .join(', ');

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">

      {/* Header */}
      <div className="flex items-center gap-3 mb-1">
        <Link href="/orders" className="text-muted hover:text-ink transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M11 6l-6 6 6 6"/>
          </svg>
        </Link>
        <h1 className="font-display font-black text-2xl uppercase tracking-tight">{order.code}</h1>
        <StatusBadge status={order.status} />
      </div>
      <p className="text-[12px] text-muted mb-8 ml-7">{formatDateTime(order.createdAt)}</p>

      <div className="space-y-6">

        {/* Items */}
        <section className="border border-line rounded-sm overflow-hidden">
          <div className="px-5 py-3.5 border-b border-line bg-line-2">
            <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{t('itemsTitle')}</h2>
          </div>
          <div className="px-5">
            {order.items.map(item => (
              <div key={item.id} className="flex gap-4 py-4 border-b border-line last:border-b-0">
                <div className="flex-1 min-w-0">
                  <p className="text-[13px] font-semibold text-ink leading-snug">{item.productName}</p>
                  <p className="text-[12px] text-muted mt-0.5">{item.variantColor} · Size {item.variantSize} · SL {item.quantity}</p>
                </div>
                <span className="text-[14px] font-bold tabular-nums text-ink shrink-0">{formatPrice(item.subtotal)}</span>
              </div>
            ))}
          </div>
          <div className="px-5 py-4 border-t border-line space-y-1.5 bg-line-2">
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">{t('subtotal')}:</span>
              <span className="tabular-nums text-ink">{formatPrice(order.subtotal)}</span>
            </div>
            {order.discountAmount > 0 && (
              <div className="flex justify-between text-[13px]">
                <span className="text-muted">{t('discount')}:</span>
                <span className="tabular-nums text-ok">-{formatPrice(order.discountAmount)}</span>
              </div>
            )}
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">{t('shippingFee')}:</span>
              <span className="tabular-nums text-ink">{formatPrice(order.shippingFee)}</span>
            </div>
            <div className="flex justify-between text-[15px] font-bold pt-1.5 border-t border-line">
              <span className="text-ink">{t('total')}:</span>
              <span className="tabular-nums text-ink">{formatPrice(order.totalAmount)}</span>
            </div>
          </div>
        </section>

        {/* Shipping info */}
        <section className="border border-line rounded-sm overflow-hidden">
          <div className="px-5 py-3.5 border-b border-line bg-line-2">
            <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{t('shippingTitle')}</h2>
          </div>
          <div className="p-5 space-y-2 text-[13px]">
            <div className="flex justify-between">
              <span className="text-muted">{t('recipient')}:</span>
              <span className="text-ink font-medium">{order.recipientName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted">{t('phone')}:</span>
              <span className="text-ink">{order.recipientPhone}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-muted shrink-0">{t('address')}:</span>
              <span className="text-ink text-right">{fullAddress}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted">{t('paymentMethod')}:</span>
              <span className="text-ink">{t(`paymentMethodValues.${order.paymentMethod}`)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted">{t('paymentStatus')}:</span>
              <span className="text-ink">{t(`paymentStatusValues.${order.paymentStatus}`)}</span>
            </div>
            {order.note && (
              <div className="flex justify-between gap-4">
                <span className="text-muted shrink-0">{t('note')}:</span>
                <span className="text-ink text-right">{order.note}</span>
              </div>
            )}
          </div>
        </section>

        {/* History */}
        {history.length > 0 && (
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{t('historyTitle')}</h2>
            </div>
            <div className="p-5 space-y-3">
              {history.map(h => (
                <div key={h.id} className="flex items-start gap-3 text-[13px]">
                  <span className="w-1.5 h-1.5 rounded-full bg-ink mt-1.5 shrink-0" />
                  <div className="flex-1">
                    <p className="text-ink font-medium">{t(`statusValues.${h.newStatus}`)}</p>
                    {h.note && <p className="text-[12px] text-muted mt-0.5">{h.note}</p>}
                    <p className="text-[11px] text-faint mt-0.5">{formatDateTime(h.createdAt)}</p>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Cancel */}
        {order.status === 'PENDING' && (
          <div className="pt-2">
            {error && (
              <p className="text-[12px] text-danger bg-danger/5 border border-danger/20 rounded-sm px-3 py-2.5 mb-3">
                {error}
              </p>
            )}
            {!confirmOpen ? (
              <button
                onClick={() => setConfirmOpen(true)}
                className="text-[12px] font-bold uppercase tracking-widest text-danger border border-danger/30 px-5 py-2.5 rounded-sm hover:bg-danger/5 transition-colors"
              >
                {t('cancelOrder')}
              </button>
            ) : (
              <div className="border border-danger/30 rounded-sm p-4 bg-danger/5 space-y-3">
                <p className="text-[13px] font-semibold text-ink">{t('cancelConfirmTitle')}</p>
                <p className="text-[12px] text-muted">{t('cancelConfirmBody')}</p>
                <div className="flex gap-2">
                  <button
                    onClick={handleCancel}
                    disabled={cancelling}
                    className="text-[12px] font-bold uppercase tracking-widest bg-danger text-white px-4 py-2 rounded-sm hover:opacity-90 transition-opacity disabled:opacity-50"
                  >
                    {cancelling ? t('cancelling') : t('cancelConfirmYes')}
                  </button>
                  <button
                    onClick={() => setConfirmOpen(false)}
                    disabled={cancelling}
                    className="text-[12px] font-bold uppercase tracking-widest border border-line px-4 py-2 rounded-sm hover:bg-paper transition-colors"
                  >
                    {t('cancelConfirmNo')}
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
