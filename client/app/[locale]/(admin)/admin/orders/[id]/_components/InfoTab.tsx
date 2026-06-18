'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import {
  formatPrice, formatDateTime, STATUS_OPTIONS, PAYMENT_STATUS_COLORS, PAYMENT_STATUS_LABELS,
  type OrderInternalResponse, type OrderStatus,
} from '../../_components/types';

function Row({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className="flex justify-between gap-3 text-sm">
      <span className="text-muted shrink-0">{label}:</span>
      <span className={`text-ink text-right ${mono ? 'font-mono text-xs break-all' : ''}`}>{value}</span>
    </div>
  );
}

export default function InfoTab({
  order,
  onUpdated,
}: {
  order: OrderInternalResponse;
  onUpdated: (order: OrderInternalResponse) => void;
}) {
  const [status, setStatus] = useState<OrderStatus>(order.status);
  const [note, setNote]     = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  async function handleUpdateStatus() {
    setError('');
    setSaving(true);
    try {
      const { data } = await clientAxios.patch(`/api/admin/orders/${order.id}/status`, {
        status,
        note: note.trim() || undefined,
      });
      onUpdated(data.data);
      setNote('');
    } catch (err) {
      const { general } = parseApiError(err);
      setError(general);
    } finally {
      setSaving(false);
    }
  }

  const fullAddress = [order.shippingStreet, order.shippingWard, order.shippingDistrict, order.shippingProvince]
    .filter(Boolean)
    .join(', ');

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      <div className="bg-white border border-line rounded-sm p-5 space-y-3">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">
          Khách hàng &amp; Giao hàng
        </h3>
        <Row label="Khách hàng" value={order.customerEmail ?? 'Khách vãng lai'} />
        {order.guestToken && <Row label="Guest token" value={order.guestToken} mono />}
        <Row label="Người nhận" value={order.recipientName} />
        <Row label="Điện thoại" value={order.recipientPhone} />
        <Row label="Địa chỉ" value={fullAddress} />
        {order.note && <Row label="Ghi chú khách" value={order.note} />}
      </div>

      <div className="bg-white border border-line rounded-sm p-5 space-y-3">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">
          Thanh toán
        </h3>
        <Row label="Phương thức" value={order.paymentMethod === 'VNPAY' ? 'VNPay' : 'COD'} />
        <Row
          label="Trạng thái TT"
          value={
            <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${PAYMENT_STATUS_COLORS[order.paymentStatus]}`}>
              {PAYMENT_STATUS_LABELS[order.paymentStatus]}
            </span>
          }
        />
        <Row label="Tạm tính" value={formatPrice(order.subtotal)} />
        <Row label="Phí giao hàng" value={formatPrice(order.shippingFee)} />
        {order.discountAmount > 0 && <Row label="Giảm giá" value={`-${formatPrice(order.discountAmount)}`} />}
        <Row label="Tổng tiền" value={<span className="font-bold">{formatPrice(order.totalAmount)}</span>} />
        <Row label="Hết hạn TT" value={formatDateTime(order.expiredAt)} />
        <Row label="Ngày tạo" value={formatDateTime(order.createdAt)} />
      </div>

      <div className="bg-white border border-line rounded-sm p-5 space-y-3 lg:col-span-2">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">
          Cập nhật trạng thái
        </h3>
        <div className="flex flex-wrap items-end gap-3">
          <div>
            <label className="block text-xs font-semibold text-muted mb-1">Trạng thái</label>
            <select
              value={status}
              onChange={e => setStatus(e.target.value as OrderStatus)}
              className="border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            >
              {STATUS_OPTIONS.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
            </select>
          </div>
          <div className="flex-1 min-w-[200px]">
            <label className="block text-xs font-semibold text-muted mb-1">Ghi chú (không bắt buộc)</label>
            <input
              value={note}
              onChange={e => setNote(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
          <button
            onClick={handleUpdateStatus}
            disabled={saving || (status === order.status && !note.trim())}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2.5 rounded-sm hover:bg-accent-700 transition-colors disabled:opacity-40"
          >
            {saving ? 'Đang lưu...' : 'Cập nhật'}
          </button>
        </div>
        {error && <p className="text-xs text-danger">{error}</p>}
      </div>
    </div>
  );
}
