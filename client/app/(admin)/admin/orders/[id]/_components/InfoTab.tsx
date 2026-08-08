'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import {
  formatPrice, formatDateTime, STATUS_OPTIONS, PAYMENT_STATUS_COLORS, PAYMENT_STATUS_LABELS, SHIPMENT_STATUS_LABELS,
  type OrderInternalResponse, type OrderStatus,
} from '../../_components/types';

function Row({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className="flex justify-between gap-3 text-sm">
      <span className="text-muted shrink-0">{label}:</span>
      <span className={`text-ink text-right ${mono ? 'font-body text-xs break-all' : ''}`}>{value}</span>
    </div>
  );
}

export default function InfoTab({
  order,
  onUpdated,
  roles,
}: {
  order: OrderInternalResponse;
  onUpdated: (order: OrderInternalResponse) => void;
  roles: string[];
}) {
  const canManageShipment = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_WAREHOUSE');
  const canConfirmOrder   = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SALE');
  const canForceDeliver   = roles.includes('ROLE_ADMIN');
  const canUpdateStatus   = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SALE');
  // Xác nhận đơn (PENDING -> CONFIRMED) là quyết định CSKH, WAREHOUSE không có quyền này (BE chặn) -
  // ẩn luôn option để không cho chọn 1 hành động chắc chắn sẽ bị từ chối. Tương tự, đánh dấu DELIVERED thủ
  // công qua dropdown này chỉ dành cho ADMIN (lối thoát hiếm khi GHN lỗi) - đường chính đạo là nút "Làm mới
  // trạng thái GHN" (canManageShipment), SALE/WAREHOUSE không được tự set DELIVERED không qua GHN xác nhận.
  // Hủy đơn PENDING cũng là quyết định CSKH (đơn còn chưa bàn giao cho kho xử lý) - dùng chung
  // canConfirmOrder vì đúng cùng bộ role (ADMIN/SALE) được thao tác trên đơn PENDING.
  // State Machine chuẩn cho Frontend (chỉ hiển thị những tuỳ chọn thủ công được phép)
  const statusOptions = STATUS_OPTIONS.filter(s => {
    if (s.value === order.status) return true; // Luôn hiển thị trạng thái hiện tại để User có thể Cập nhật mỗi Ghi chú
    if (s.value === 'SHIPPING') return false;  // Không bao giờ cho chọn thủ công

    if (order.status === 'PENDING') {
      if (s.value === 'CONFIRMED' && canConfirmOrder) return true;
      if (s.value === 'CANCELLED' && canConfirmOrder) return true;
      return false;
    }

    if (order.status === 'CONFIRMED') {
      // Đã xác nhận thì chỉ có thể Hủy (còn sang Đang giao là tự động qua GHN)
      if (s.value === 'CANCELLED' && canConfirmOrder) return true;
      return false;
    }

    if (order.status === 'SHIPPING') {
      // Đang giao thì chỉ Admin mới được ép Hoàn thành thủ công, hoặc ép Hủy (nếu mất hàng)
      if (s.value === 'DELIVERED' && canForceDeliver) return true;
      if (s.value === 'CANCELLED' && canForceDeliver) return true;
      return false;
    }

    // Nếu đã DELIVERED hoặc CANCELLED thì không cho đổi sang bất kỳ trạng thái nào khác
    return false;
  });

  const [status, setStatus] = useState<OrderStatus>(order.status);
  const [note, setNote]     = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError]   = useState('');

  // order.status có thể đổi từ bên ngoài (tạo/hủy/đồng bộ vận đơn) — dropdown phải theo kịp, không chỉ đọc 1 lần lúc mount
  useEffect(() => {
    setStatus(order.status);
  }, [order.status]);

  const [creatingShipment, setCreatingShipment] = useState(false);
  const [cancelingShipment, setCancelingShipment] = useState(false);
  const [syncingShipment, setSyncingShipment]   = useState(false);
  const [shipmentError, setShipmentError]       = useState('');

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

  async function handleCreateShipment() {
    setShipmentError('');
    setCreatingShipment(true);
    try {
      const { data } = await clientAxios.post(`/api/admin/orders/${order.id}/shipment`);
      onUpdated(data.data);
    } catch (err) {
      const { general } = parseApiError(err);
      setShipmentError(general);
    } finally {
      setCreatingShipment(false);
    }
  }

  async function handleCancelShipment() {
    if (!window.confirm('Hủy vận đơn GHN hiện tại? Đơn sẽ quay lại trạng thái "Đã xác nhận" và có thể tạo vận đơn mới sau đó.')) {
      return;
    }
    setShipmentError('');
    setCancelingShipment(true);
    try {
      const { data } = await clientAxios.delete(`/api/admin/orders/${order.id}/shipment`);
      onUpdated(data.data);
    } catch (err) {
      const { general } = parseApiError(err);
      setShipmentError(general);
    } finally {
      setCancelingShipment(false);
    }
  }

  async function handleSyncShipment() {
    setShipmentError('');
    setSyncingShipment(true);
    try {
      const { data } = await clientAxios.post(`/api/admin/orders/${order.id}/shipment/sync`);
      onUpdated(data.data);
    } catch (err) {
      const { general } = parseApiError(err);
      setShipmentError(general);
    } finally {
      setSyncingShipment(false);
    }
  }

  const fullAddress = [order.shippingStreet, order.shippingWard, order.shippingDistrict, order.shippingProvince]
    .filter(Boolean)
    .join(', ');

  // Vận đơn đã hủy (status='cancel') là dữ liệu lịch sử, không phải vận đơn đang áp dụng - vẫn hiện read-only
  // để đối soát nhưng không coi là "đã có vận đơn" (BE giờ giữ lại row thay vì xóa khi hủy).
  const isShipmentActive = !!order.shipment && order.shipment.status !== 'cancel';

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
        {order.discountAmount > 0 && <Row label="Mã giảm giá" value={`-${formatPrice(order.discountAmount)}`} />}
        {order.tierDiscountAmount > 0 && <Row label="Hạng thành viên" value={`-${formatPrice(order.tierDiscountAmount)}`} />}
        <Row label="Tổng tiền" value={<span className="font-bold">{formatPrice(order.totalAmount)}</span>} />
        <Row label="Hết hạn TT" value={formatDateTime(order.expiredAt)} />
        <Row label="Ngày tạo" value={formatDateTime(order.createdAt)} />
      </div>

      <div className="bg-white border border-line rounded-sm p-5 space-y-3 lg:col-span-2">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">
          Vận chuyển (GHN)
        </h3>
        {isShipmentActive ? (
          <>
            <Row label="Mã vận đơn" value={order.shipment!.shippingOrderCode ?? '—'} mono />
            {order.shipment!.status && (
              <Row label="Trạng thái GHN" value={SHIPMENT_STATUS_LABELS[order.shipment!.status] ?? order.shipment!.status} />
            )}
            {order.shipment!.expectedDeliveryAt && (
              <Row label="Dự kiến giao" value={formatDateTime(order.shipment!.expectedDeliveryAt)} />
            )}
            <Row
              label="Đồng bộ lần cuối"
              value={order.shipment!.syncedAt ? formatDateTime(order.shipment!.syncedAt) : 'Chưa đồng bộ'}
            />
            {canManageShipment && (
              <div className="flex items-center gap-3 pt-1">
                {(order.status === 'SHIPPING' || order.status === 'CONFIRMED') && (
                  <button
                    onClick={handleSyncShipment}
                    disabled={syncingShipment}
                    className="border border-line text-ink font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2.5 rounded-sm hover:bg-bg-subtle transition-colors disabled:opacity-40"
                  >
                    {syncingShipment ? 'Đang đồng bộ...' : 'Đồng bộ trạng thái GHN'}
                  </button>
                )}
                {(order.status === 'SHIPPING' || order.status === 'CONFIRMED') && (
                  <button
                    onClick={handleCancelShipment}
                    disabled={cancelingShipment}
                    className="border border-danger text-danger font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2.5 rounded-sm hover:bg-danger-bg transition-colors disabled:opacity-40"
                  >
                    {cancelingShipment ? 'Đang hủy...' : 'Hủy vận đơn'}
                  </button>
                )}
              </div>
            )}
          </>
        ) : (
          <>
            {order.shipment && (
              <div className="pb-1 space-y-3 opacity-70">
                <Row label="Vận đơn trước đó" value={order.shipment.shippingOrderCode ?? '—'} mono />
                <Row label="Trạng thái GHN" value={SHIPMENT_STATUS_LABELS[order.shipment.status ?? ''] ?? order.shipment.status ?? '—'} />
              </div>
            )}
            {canManageShipment ? (
              <div className="flex items-center gap-3">
                <button
                  onClick={handleCreateShipment}
                  disabled={creatingShipment || order.status !== 'CONFIRMED'}
                  className="bg-ink text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2.5 rounded-sm hover:bg-ink/80 transition-colors disabled:opacity-40"
                >
                  {creatingShipment ? 'Đang tạo...' : 'Tạo vận đơn GHN'}
                </button>
                {order.status !== 'CONFIRMED' && (
                  <span className="text-xs text-muted">Chỉ tạo được khi đơn đã &quot;Đã xác nhận&quot;.</span>
                )}
              </div>
            ) : (
              !order.shipment && <p className="text-xs text-muted">Chưa có vận đơn.</p>
            )}
          </>
        )}
        {shipmentError && <p className="text-xs text-danger">{shipmentError}</p>}
      </div>

      {canUpdateStatus && (
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
                {statusOptions.map(s => <option key={s.value} value={s.value}>{s.label}</option>)}
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
      )}
    </div>
  );
}
