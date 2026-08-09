'use client';

import { useState, useEffect, useCallback } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { guestHeaders, saveGuestToken } from '@/lib/guestToken';
import { formatPrice } from '../../../products/_components/types';
import {
  formatDateTime, STATUS_COLORS, STATUS_LABELS, PAYMENT_STATUS_LABELS, PAYMENT_METHOD_LABELS, SHIPMENT_STATUS_LABELS,
  type OrderResponse, type OrderStatusHistoryResponse, type OrderStatus,
} from '../../_components/types';
import ReturnRequestSection from './ReturnRequestSection';
import OrderDetailSkeleton from './OrderDetailSkeleton';

function StatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide ${STATUS_COLORS[status]}`}>
      {STATUS_LABELS[status]}
    </span>
  );
}

export default function OrderDetailClient({
  orderId,
  isLoggedIn,
  orderTokenFromUrl,
  initialOrder,
  isTrackingMode = false,
  initialHistory,
}: {
  orderId: number | string;
  isLoggedIn?: boolean;
  orderTokenFromUrl?: string | null;
  initialOrder?: OrderResponse;
  isTrackingMode?: boolean;
  initialHistory?: OrderStatusHistoryResponse[];
}) {
  useEffect(() => {
    if (orderTokenFromUrl) {
      saveGuestToken(orderTokenFromUrl);
    }
  }, [orderTokenFromUrl]);

  const [order, setOrder] = useState<OrderResponse | null>(initialOrder || null);
  const [history, setHistory] = useState<OrderStatusHistoryResponse[]>(initialHistory || []);
  const [loading, setLoading] = useState(!initialOrder);
  const [notFound, setNotFound] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [error, setError] = useState('');
  const [trackingLink, setTrackingLink] = useState('');

  useEffect(() => {
    if (!isLoggedIn && typeof window !== 'undefined' && order?.trackingToken) {
        setTrackingLink(`${window.location.origin}/tracking/${order.trackingToken}`);
    }
  }, [isLoggedIn, order?.trackingToken]);

  const load = useCallback(() => {
    if (isTrackingMode) {
      setLoading(false);
      return;
    }
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
  }, [orderId, isTrackingMode]);

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
        <p className="text-muted text-[14px]">{"Không tìm thấy đơn hàng."}</p>
        <Link
          href={isTrackingMode ? "/tracking" : "/orders"}
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {isTrackingMode ? "Quay lại trang tra cứu" : "Quay lại danh sách"}
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
        <Link href={isTrackingMode ? "/tracking" : "/orders"} className="text-muted hover:text-ink transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M11 6l-6 6 6 6"/>
          </svg>
        </Link>
        <h1 className="font-display font-black text-2xl uppercase tracking-tight">{order.code}</h1>
        <StatusBadge status={order.status} />
      </div>
      <p className="text-[12px] text-muted mb-8 ml-7">{formatDateTime(order.createdAt)}</p>

      {trackingLink && !isTrackingMode && (
        <div className="mb-6 p-4 bg-ink/5 border border-ink/10 rounded-sm">
          <p className="text-[13px] font-semibold text-ink mb-1">
            {order.status === 'PENDING' ? "Đơn hàng của bạn đã được ghi nhận!" : "Đường dẫn tra cứu đơn hàng"}
          </p>
          <p className="text-[12px] text-muted mb-3">
            {order.status === 'PENDING' 
              ? "Vui lòng lưu lại đường link dưới đây để tra cứu trạng thái đơn hàng (nhất là khi bạn không cung cấp Email):"
              : "Bạn có thể lưu lại đường link này để tiếp tục tra cứu trạng thái đơn hàng về sau:"}
          </p>
          <div className="flex gap-2">
            <input 
              readOnly 
              value={trackingLink} 
              className="flex-1 px-3 py-2 text-[12px] bg-white border border-line rounded-sm outline-none text-muted"
            />
            <button 
              onClick={() => { navigator.clipboard.writeText(trackingLink); alert("Đã copy link!"); }}
              className="px-4 py-2 bg-ink text-white text-[12px] font-bold rounded-sm hover:bg-accent transition-colors shrink-0"
            >
              {"Copy"}
            </button>
          </div>
        </div>
      )}

      <div className="space-y-6">

        {/* Items */}
        <section className="border border-line rounded-sm overflow-hidden">
          <div className="px-5 py-3.5 border-b border-line bg-line-2">
            <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Sản phẩm"}</h2>
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
              <span className="text-muted">{"Tạm tính"}:</span>
              <span className="tabular-nums text-ink">{formatPrice(order.subtotal)}</span>
            </div>
            {order.discountAmount > 0 && (
              <div className="flex justify-between text-[13px]">
                <span className="text-muted">{"Mã giảm giá"}:</span>
                <span className="tabular-nums text-ok">-{formatPrice(order.discountAmount)}</span>
              </div>
            )}
            {order.tierDiscountAmount > 0 && (
              <div className="flex justify-between text-[13px]">
                <span className="text-muted">{"Hạng thành viên"}:</span>
                <span className="tabular-nums text-ok">-{formatPrice(order.tierDiscountAmount)}</span>
              </div>
            )}
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">{"Phí giao hàng"}:</span>
              <span className="tabular-nums text-ink">{formatPrice(order.shippingFee)}</span>
            </div>
            <div className="flex justify-between text-[15px] font-bold pt-1.5 border-t border-line">
              <span className="text-ink">{"Tổng tiền"}:</span>
              <span className="tabular-nums text-ink">{formatPrice(order.totalAmount)}</span>
            </div>
          </div>
        </section>

        {/* Shipping info */}
        <section className="border border-line rounded-sm overflow-hidden">
          <div className="px-5 py-3.5 border-b border-line bg-line-2">
            <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Thông tin giao hàng"}</h2>
          </div>
          <div className="p-5 space-y-2 text-[13px]">
            <div className="flex justify-between">
              <span className="text-muted">{"Người nhận"}:</span>
              <span className="text-ink font-medium">{order.recipientName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted">{"Điện thoại"}:</span>
              <span className="text-ink">{order.recipientPhone}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-muted shrink-0">{"Địa chỉ"}:</span>
              <span className="text-ink text-right">{fullAddress}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted">{"Phương thức"}:</span>
              <span className="text-ink">{PAYMENT_METHOD_LABELS[order.paymentMethod]}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted">{"Thanh toán"}:</span>
              <span className="text-ink">{PAYMENT_STATUS_LABELS[order.paymentStatus]}</span>
            </div>
            {order.note && (
              <div className="flex justify-between gap-4">
                <span className="text-muted shrink-0">{"Ghi chú"}:</span>
                <span className="text-ink text-right">{order.note}</span>
              </div>
            )}
          </div>
        </section>

        {/* Shipment (GHN) */}
        {order.shipment && (
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Vận chuyển"}</h2>
            </div>
            <div className="p-5 space-y-2 text-[13px]">
              {order.shipment.shippingOrderCode && (
                <div className="flex justify-between">
                  <span className="text-muted">{"Mã vận đơn"}:</span>
                  <span className="text-ink font-medium font-body">{order.shipment.shippingOrderCode}</span>
                </div>
              )}
              {order.shipment.status && (
                <div className="flex justify-between">
                  <span className="text-muted">{"Trạng thái vận chuyển"}:</span>
                  <span className="text-ink">{SHIPMENT_STATUS_LABELS[order.shipment.status] ?? order.shipment.status}</span>
                </div>
              )}
              {order.shipment.expectedDeliveryAt && (
                <div className="flex justify-between">
                  <span className="text-muted">{"Dự kiến giao"}:</span>
                  <span className="text-ink font-medium">{formatDateTime(order.shipment.expectedDeliveryAt)}</span>
                </div>
              )}
            </div>
          </section>
        )}

        {/* History */}
        {history.length > 0 && (
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">{"Lịch sử trạng thái"}</h2>
            </div>
            <div className="p-5 space-y-3">
              {history.map(h => (
                <div key={h.id} className="flex items-start gap-3 text-[13px]">
                  <span className="w-1.5 h-1.5 rounded-full bg-ink mt-1.5 shrink-0" />
                  <div className="flex-1">
                    <p className="text-ink font-medium">{STATUS_LABELS[h.newStatus]}</p>
                    {h.note && <p className="text-[12px] text-muted mt-0.5">{h.note}</p>}
                    <p className="text-[11px] text-faint mt-0.5">{formatDateTime(h.createdAt)}</p>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* Return/Exchange */}
        {order.status === 'DELIVERED' && !isTrackingMode && (
          <ReturnRequestSection
            orderId={order.id}
            deliveredAt={history.find(h => h.newStatus === 'DELIVERED')?.createdAt ?? null}
            isLoggedIn={isLoggedIn}
          />
        )}

        {/* Actions (Cancel / Pay Again) */}
        {order.status === 'PENDING' && !isTrackingMode && (
          <div className="pt-2 flex flex-col items-start gap-3">
            {error && (
              <p className="text-[12px] text-danger bg-danger/5 border border-danger/20 rounded-sm px-3 py-2.5 w-full">
                {error}
              </p>
            )}

            <div className="flex gap-3">
              {order.paymentMethod === 'VNPAY' && order.paymentStatus === 'UNPAID' && (
                <button
                  onClick={async () => {
                    setError('');
                    setCancelling(true); // Tái sử dụng state loading
                    try {
                      const res = await clientAxios.post('/api/payment', { orderId: order.id }, { headers: guestHeaders() });
                      if (res.data?.data) window.location.href = res.data.data;
                    } catch (err) {
                      setError(parseApiError(err).general);
                      setCancelling(false);
                    }
                  }}
                  disabled={cancelling}
                  className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-5 py-2.5 rounded-sm hover:bg-accent transition-colors disabled:opacity-50"
                >
                  {cancelling ? "Đang xử lý..." : "Thanh toán ngay"}
                </button>
              )}

              {!confirmOpen ? (
                <button
                  onClick={() => setConfirmOpen(true)}
                  disabled={cancelling}
                  className="text-[12px] font-bold uppercase tracking-widest text-danger border border-danger/30 px-5 py-2.5 rounded-sm hover:bg-danger/5 transition-colors disabled:opacity-50"
                >
                  {"Hủy đơn hàng"}
                </button>
              ) : (
                <div className="border border-danger/30 rounded-sm p-4 bg-danger/5 space-y-3 w-full">
                  <p className="text-[13px] font-semibold text-ink">{"Hủy đơn hàng?"}</p>
                  <p className="text-[12px] text-muted">{"Bạn có chắc muốn hủy đơn hàng này? Hành động này không thể hoàn tác."}</p>
                  <div className="flex gap-2">
                    <button
                      onClick={handleCancel}
                      disabled={cancelling}
                      className="text-[12px] font-bold uppercase tracking-widest bg-danger text-white px-4 py-2 rounded-sm hover:opacity-90 transition-opacity disabled:opacity-50"
                    >
                      {cancelling ? "Đang hủy..." : "Xác nhận hủy"}
                    </button>
                    <button
                      onClick={() => setConfirmOpen(false)}
                      disabled={cancelling}
                      className="text-[12px] font-bold uppercase tracking-widest border border-line px-4 py-2 rounded-sm hover:bg-paper transition-colors"
                    >
                      {"Không"}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
