'use client';

import { useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { returnUrl } from '@/lib/cloudinaryUrl';
import {
  formatDateTime, formatPrice,
  RESOLUTION_TYPE_LABELS, STATUS_LABELS, STATUS_COLORS,
  type ReturnRequestInternalResponse,
} from '../../_components/types';

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex justify-between gap-3 text-sm">
      <span className="text-muted shrink-0">{label}:</span>
      <span className="text-ink text-right">{value}</span>
    </div>
  );
}

export default function ReturnDetailClient({
  returnRequest: initial,
  canApprove,
  canWarehouse,
}: {
  returnRequest: ReturnRequestInternalResponse;
  canApprove: boolean;
  canWarehouse: boolean;
}) {
  const [item, setItem] = useState(initial);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  const [processOpen, setProcessOpen] = useState<'pass' | 'fail' | null>(null);
  const [processNote, setProcessNote] = useState('');
  const [processRejectReason, setProcessRejectReason] = useState('');

  async function handleApprove() {
    setBusy(true); setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/returns/${item.id}/approve`);
      setItem(data.data);
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setBusy(false);
    }
  }

  async function handleReject() {
    if (!rejectReason.trim()) return;
    setBusy(true); setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/returns/${item.id}/reject`, { rejectReason: rejectReason.trim() });
      setItem(data.data);
      setRejectOpen(false);
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setBusy(false);
    }
  }

  async function handleReceived() {
    setBusy(true); setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/returns/${item.id}/received`);
      setItem(data.data);
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setBusy(false);
    }
  }

  async function handleProcess(passed: boolean) {
    if (!passed && !processRejectReason.trim()) return;
    setBusy(true); setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/returns/${item.id}/process`, {
        passed,
        rejectReason: passed ? null : processRejectReason.trim(),
        adminNote: processNote.trim() || null,
      });
      setItem(data.data);
      setProcessOpen(null);
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setBusy(false);
    }
  }

  async function handleRetryShipment() {
    setBusy(true); setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/returns/${item.id}/retry-shipment`);
      setItem(data.data);
    } catch (err) {
      setError(parseApiError(err).general);
    } finally {
      setBusy(false);
    }
  }

  const needsShipmentRetry = item.status === 'COMPLETED' && item.resolutionType === 'EXCHANGE' && !item.exchangeShippingOrderCode;

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/returns" className="text-muted hover:text-ink transition-colors">Đổi/trả hàng</Link>
        <span className="text-muted">/</span>
        <span className="font-body font-bold">{item.code}</span>
      </div>

      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <span className={`px-2.5 py-1 rounded text-xs font-bold ${STATUS_COLORS[item.status]}`}>
            {STATUS_LABELS[item.status]}
          </span>
          <span className="px-2.5 py-1 rounded text-xs font-bold bg-line-2 text-ink">
            {RESOLUTION_TYPE_LABELS[item.resolutionType]}
          </span>
        </div>

        <div className="flex gap-2 flex-wrap">
          {item.status === 'PENDING' && canApprove && (
            <>
              <button onClick={handleApprove} disabled={busy}
                className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 disabled:opacity-50 transition-colors">
                {busy ? 'Đang xử lý...' : 'Duyệt'}
              </button>
              <button onClick={() => setRejectOpen(true)}
                className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                Từ chối
              </button>
            </>
          )}
          {item.status === 'APPROVED' && (
            <>
              {canWarehouse && (
                <button onClick={handleReceived} disabled={busy}
                  className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 disabled:opacity-50 transition-colors">
                  {busy ? 'Đang xử lý...' : 'Xác nhận đã nhận hàng'}
                </button>
              )}
              {canApprove && (
                <button onClick={() => setRejectOpen(true)}
                  className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                  Từ chối
                </button>
              )}
            </>
          )}
          {item.status === 'RECEIVED' && canWarehouse && (
            <>
              <button onClick={() => setProcessOpen('pass')}
                className="bg-ok text-white text-sm font-bold px-4 py-2 rounded-sm hover:opacity-90 transition-opacity">
                Đạt kiểm tra
              </button>
              <button onClick={() => setProcessOpen('fail')}
                className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                Không đạt
              </button>
            </>
          )}
          {needsShipmentRetry && canWarehouse && (
            <button onClick={handleRetryShipment} disabled={busy}
              className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 disabled:opacity-50 transition-colors">
              {busy ? 'Đang thử lại...' : 'Thử tạo lại vận đơn'}
            </button>
          )}
        </div>
      </div>

      {error && <p className="text-danger text-sm">{error}</p>}

      {rejectOpen && (
        <div className="border border-danger/30 rounded-sm p-4 bg-danger/5 space-y-3">
          <p className="text-sm font-semibold text-ink">Lý do từ chối</p>
          <textarea value={rejectReason} onChange={e => setRejectReason(e.target.value.replace(/^\s+/, ''))} rows={2}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          <div className="flex gap-2">
            <button onClick={handleReject} disabled={busy || !rejectReason.trim()}
              className="bg-danger text-white text-sm font-bold px-4 py-2 rounded-sm hover:opacity-90 disabled:opacity-50 transition-opacity">
              Xác nhận từ chối
            </button>
            <button onClick={() => setRejectOpen(false)} className="border border-line text-sm font-bold px-4 py-2 rounded-sm hover:bg-paper transition-colors">
              Đóng
            </button>
          </div>
        </div>
      )}

      {processOpen && (
        <div className={`border rounded-sm p-4 space-y-3 ${processOpen === 'pass' ? 'border-ok/30 bg-ok/5' : 'border-danger/30 bg-danger/5'}`}>
          <p className="text-sm font-semibold text-ink">
            {processOpen === 'pass'
              ? (item.resolutionType === 'REFUND' ? 'Xác nhận đạt kiểm tra — hoàn tiền + hoàn kho' : 'Xác nhận đạt kiểm tra — hoàn kho + gửi hàng đổi')
              : 'Xác nhận không đạt kiểm tra — trả lại hàng cho khách, không hoàn tiền/đổi'}
          </p>
          {processOpen === 'fail' && (
            <textarea value={processRejectReason} onChange={e => setProcessRejectReason(e.target.value.replace(/^\s+/, ''))} rows={2}
              placeholder="Lý do không đạt (bắt buộc)..."
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          )}
          <textarea value={processNote} onChange={e => setProcessNote(e.target.value.replace(/^\s+/, ''))} rows={2}
            placeholder="Ghi chú nội bộ (tùy chọn)..."
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          <div className="flex gap-2">
            <button onClick={() => handleProcess(processOpen === 'pass')} disabled={busy || (processOpen === 'fail' && !processRejectReason.trim())}
              className={`text-white text-sm font-bold px-4 py-2 rounded-sm disabled:opacity-50 transition-opacity ${processOpen === 'pass' ? 'bg-ok hover:opacity-90' : 'bg-danger hover:opacity-90'}`}>
              {busy ? 'Đang xử lý...' : 'Xác nhận'}
            </button>
            <button onClick={() => setProcessOpen(null)} className="border border-line text-sm font-bold px-4 py-2 rounded-sm hover:bg-paper transition-colors">
              Đóng
            </button>
          </div>
        </div>
      )}

      <div className="bg-white border border-line rounded-sm p-5 space-y-3">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">Thông tin</h3>
        <Row label="Đơn hàng" value={<Link href={`/admin/orders/${item.orderId}`} className="underline hover:text-accent">{item.orderCode}</Link>} />
        <Row label="Email khách hàng" value={item.customerEmail} />
        <Row label="Lý do" value={item.reason} />
        {item.rejectReason && <Row label="Lý do từ chối" value={<span className="text-danger">{item.rejectReason}</span>} />}
        {item.trackingCode && <Row label="Mã vận đơn khách gửi" value={item.trackingCode} />}
        {item.approvedByEmail && <Row label="Người duyệt" value={item.approvedByEmail} />}
        <Row label="Thời gian tạo" value={formatDateTime(item.createdAt)} />
        {item.approvedAt && <Row label="Ngày duyệt" value={formatDateTime(item.approvedAt)} />}
        {item.receivedAt && <Row label="Ngày nhận hàng" value={formatDateTime(item.receivedAt)} />}
        {item.completedAt && <Row label="Ngày hoàn tất" value={formatDateTime(item.completedAt)} />}
        {item.refundAmount != null && <Row label="Số tiền hoàn" value={<span className="font-bold">{formatPrice(item.refundAmount)}</span>} />}
        {item.refundedAt && <Row label="Đã hoàn tiền lúc" value={formatDateTime(item.refundedAt)} />}
        {item.exchangeShippingOrderCode && <Row label="Mã vận đơn hàng đổi" value={item.exchangeShippingOrderCode} />}
        {item.exchangeExpectedDeliveryAt && <Row label="Dự kiến giao hàng đổi" value={formatDateTime(item.exchangeExpectedDeliveryAt)} />}
        <Row label="Ghi chú nội bộ" value={item.adminNote || <span className="text-muted italic">Không có</span>} />
      </div>

      <div className="bg-white border border-line rounded-sm overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">SKU / Sản phẩm</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Size / Màu</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">SL</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Đổi sang</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap">Giá trị</th>
            </tr>
          </thead>
          <tbody>
            {item.items.map(i => (
              <tr key={i.id} className="border-b border-line-2 last:border-0">
                <td className="px-4 py-3">
                  <div className="font-body text-xs font-bold">{i.variantSku}</div>
                  <div className="text-xs text-muted">{i.productName}</div>
                </td>
                <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">{i.variantColor} · {i.variantSize}</td>
                <td className="px-4 py-3 text-right tabular-nums">{i.quantity}</td>
                <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">
                  {i.exchangeVariantId ? `${i.exchangeVariantColorway} · ${i.exchangeVariantSize}` : '—'}
                </td>
                <td className="px-4 py-3 text-right tabular-nums font-bold whitespace-nowrap">{formatPrice(i.refundAmount)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {item.imagePublicIds && item.imagePublicIds.length > 0 && (
        <div className="bg-white border border-line rounded-sm p-5">
          <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-3">Ảnh minh chứng</h3>
          <div className="flex flex-wrap gap-2">
            {item.imagePublicIds.map(publicId => (
              <img key={publicId} src={returnUrl(publicId, 96, 96)} alt="" className="w-20 h-20 object-cover rounded-sm border border-line" />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
