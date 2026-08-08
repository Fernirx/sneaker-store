'use client';

import { useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import {
  formatPrice, formatDateTime,
  STATUS_LABELS, STATUS_COLORS, PAYMENT_STATUS_LABELS, PAYMENT_STATUS_COLORS,
  type PurchaseResponse, type SupplierBrief,
} from '../../_components/types';
import EditPurchaseForm from './EditPurchaseForm';
import ReceivePurchaseModal from './ReceivePurchaseModal';
import CancelPurchaseModal from './CancelPurchaseModal';

function Row({ label, value, mono }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className="flex justify-between gap-3 text-sm">
      <span className="text-muted shrink-0">{label}:</span>
      <span className={`text-ink text-right ${mono ? 'font-body text-xs break-all' : ''}`}>{value}</span>
    </div>
  );
}

export default function PurchaseDetailClient({
  purchase: initialPurchase,
  suppliers,
}: {
  purchase: PurchaseResponse;
  suppliers: SupplierBrief[];
}) {
  const [purchase, setPurchase] = useState(initialPurchase);
  const [editing, setEditing] = useState(false);
  const [receiveOpen, setReceiveOpen] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [actionError, setActionError] = useState('');
  const [confirming, setConfirming] = useState(false);
  const [markingPaid, setMarkingPaid] = useState(false);

  async function handleConfirm() {
    setConfirming(true);
    setActionError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/purchases/${purchase.id}/confirm`);
      setPurchase(data.data);
    } catch (err) {
      setActionError(parseApiError(err).general);
    } finally {
      setConfirming(false);
    }
  }

  async function handleMarkAsPaid() {
    setMarkingPaid(true);
    setActionError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/purchases/${purchase.id}/mark-paid`);
      setPurchase(data.data);
    } catch (err) {
      setActionError(parseApiError(err).general);
    } finally {
      setMarkingPaid(false);
    }
  }

  if (editing) {
    return (
      <div className="space-y-5">
        <div className="flex items-center gap-2 text-sm">
          <Link href="/admin/purchases" className="text-muted hover:text-ink transition-colors">Phiếu nhập hàng</Link>
          <span className="text-muted">/</span>
          <span className="font-body font-bold">{purchase.purchaseCode}</span>
          <span className="text-xs text-muted ml-1">— Chỉnh sửa</span>
        </div>
        <EditPurchaseForm
          purchase={purchase}
          suppliers={suppliers}
          onSaved={p => { setPurchase(p); setEditing(false); }}
          onCancel={() => setEditing(false)}
        />
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/purchases" className="text-muted hover:text-ink transition-colors">Phiếu nhập hàng</Link>
        <span className="text-muted">/</span>
        <span className="font-body font-bold">{purchase.purchaseCode}</span>
        <span className="text-xs text-muted ml-1">#{purchase.id}</span>
      </div>

      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <span className={`px-2.5 py-1 rounded text-xs font-bold ${STATUS_COLORS[purchase.status]}`}>
            {STATUS_LABELS[purchase.status]}
          </span>
          <span className={`px-2.5 py-1 rounded text-xs font-bold ${PAYMENT_STATUS_COLORS[purchase.paymentStatus]}`}>
            {PAYMENT_STATUS_LABELS[purchase.paymentStatus]}
          </span>
        </div>

        <div className="flex gap-2 flex-wrap">
          {purchase.status === 'DRAFT' && (
            <>
              <button onClick={() => setEditing(true)}
                className="px-4 py-2 border border-line text-sm font-bold rounded-sm hover:bg-paper transition-colors">
                Sửa
              </button>
              <button onClick={handleConfirm} disabled={confirming}
                className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 disabled:opacity-50 transition-colors">
                {confirming ? 'Đang xác nhận...' : 'Xác nhận'}
              </button>
              <button onClick={() => setCancelOpen(true)}
                className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                Hủy phiếu
              </button>
            </>
          )}
          {purchase.status === 'CONFIRMED' && (
            <>
              <button onClick={() => setReceiveOpen(true)}
                className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors">
                Nhận hàng
              </button>
              <button onClick={() => setCancelOpen(true)}
                className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                Hủy phiếu
              </button>
            </>
          )}
          {purchase.paymentStatus === 'UNPAID' && (
            <button onClick={handleMarkAsPaid} disabled={markingPaid}
              className="border border-line text-sm font-bold px-4 py-2 rounded-sm hover:bg-paper disabled:opacity-50 transition-colors">
              {markingPaid ? 'Đang lưu...' : 'Đánh dấu đã thanh toán'}
            </button>
          )}
        </div>
      </div>

      {actionError && <p className="text-danger text-sm">{actionError}</p>}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div className="bg-white border border-line rounded-sm p-5 space-y-3">
          <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">Nhà cung cấp</h3>
          <Row label="Nhà cung cấp" value={purchase.supplierName} />
          <Row label="Số hóa đơn NCC" value={purchase.supplierInvoiceNo || <span className="text-muted italic">Không có</span>} />
          <Row label="Người tạo" value={purchase.createdByEmail ?? '—'} />
          {purchase.receivedByEmail && <Row label="Người nhận hàng" value={purchase.receivedByEmail} />}
          <Row label="Ghi chú" value={purchase.notes || <span className="text-muted italic">Không có</span>} />
        </div>

        <div className="bg-white border border-line rounded-sm p-5 space-y-3">
          <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">Chi phí</h3>
          <Row label="Tạm tính" value={formatPrice(purchase.subtotal)} />
          {purchase.discountAmount > 0 && <Row label="Giảm giá" value={`-${formatPrice(purchase.discountAmount)}`} />}
          {purchase.taxAmount > 0 && <Row label="Thuế" value={formatPrice(purchase.taxAmount)} />}
          {purchase.shippingCost > 0 && <Row label="Phí vận chuyển" value={formatPrice(purchase.shippingCost)} />}
          <Row label="Tổng chi phí" value={<span className="font-bold">{formatPrice(purchase.totalCost)}</span>} />
          <Row label="Thời gian tạo" value={formatDateTime(purchase.createdAt)} />
          {purchase.confirmedAt && <Row label="Ngày xác nhận" value={formatDateTime(purchase.confirmedAt)} />}
          {purchase.receivedAt && <Row label="Ngày nhận hàng" value={formatDateTime(purchase.receivedAt)} />}
        </div>
      </div>

      <div className="bg-white border border-line rounded-sm overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">SKU / Sản phẩm</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Size / Màu</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">SL đặt</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">SL nhận</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">SL lỗi</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Đơn giá</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            {purchase.items.map(item => (
              <tr key={item.id} className="border-b border-line-2 last:border-0">
                <td className="px-4 py-3 whitespace-nowrap">
                  <div className="font-body text-xs font-bold">{item.sku}</div>
                  <div className="text-xs text-muted">{item.productName}</div>
                </td>
                <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">{item.colorway} · {item.size}</td>
                <td className="px-4 py-3 text-right tabular-nums whitespace-nowrap">{item.quantityOrdered}</td>
                <td className="px-4 py-3 text-right tabular-nums whitespace-nowrap">{item.quantityReceived}</td>
                <td className="px-4 py-3 text-right tabular-nums whitespace-nowrap">
                  <span className={item.defectiveQty > 0 ? 'text-danger font-bold' : ''}>{item.defectiveQty}</span>
                </td>
                <td className="px-4 py-3 text-right tabular-nums whitespace-nowrap">{formatPrice(item.unitCost)}</td>
                <td className="px-4 py-3 text-right tabular-nums font-semibold whitespace-nowrap">{formatPrice(item.lineTotal)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {receiveOpen && (
        <ReceivePurchaseModal
          purchase={purchase}
          onClose={() => setReceiveOpen(false)}
          onReceived={p => { setPurchase(p); setReceiveOpen(false); }}
        />
      )}
      {cancelOpen && (
        <CancelPurchaseModal
          purchaseId={purchase.id}
          onClose={() => setCancelOpen(false)}
          onCancelled={p => { setPurchase(p); setCancelOpen(false); }}
        />
      )}
    </div>
  );
}
