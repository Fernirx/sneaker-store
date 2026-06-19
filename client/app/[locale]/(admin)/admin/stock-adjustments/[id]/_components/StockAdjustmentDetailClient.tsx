'use client';

import { useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import {
  formatDateTime, TYPE_LABELS, STATUS_LABELS, STATUS_COLORS,
  type StockAdjustmentResponse,
} from '../../_components/types';
import EditStockAdjustmentForm from './EditStockAdjustmentForm';
import CancelStockAdjustmentModal from './CancelStockAdjustmentModal';

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex justify-between gap-3 text-sm">
      <span className="text-muted shrink-0">{label}:</span>
      <span className="text-ink text-right">{value}</span>
    </div>
  );
}

export default function StockAdjustmentDetailClient({
  adjustment: initialAdjustment,
  isAdmin,
}: {
  adjustment: StockAdjustmentResponse;
  isAdmin: boolean;
}) {
  const [adjustment, setAdjustment] = useState(initialAdjustment);
  const [editing, setEditing] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [actionError, setActionError] = useState('');
  const [approving, setApproving] = useState(false);
  const [confirming, setConfirming] = useState(false);

  async function handleApprove() {
    setApproving(true);
    setActionError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/stock-adjustments/${adjustment.id}/approve`);
      setAdjustment(data.data);
    } catch (err) {
      setActionError(parseApiError(err).general);
    } finally {
      setApproving(false);
    }
  }

  async function handleConfirmApply() {
    setConfirming(true);
    setActionError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/stock-adjustments/${adjustment.id}/confirm`);
      setAdjustment(data.data);
    } catch (err) {
      setActionError(parseApiError(err).general);
    } finally {
      setConfirming(false);
    }
  }

  if (editing) {
    return (
      <div className="space-y-5">
        <div className="flex items-center gap-2 text-sm">
          <Link href="/admin/stock-adjustments" className="text-muted hover:text-ink transition-colors">Điều chỉnh kho</Link>
          <span className="text-muted">/</span>
          <span className="font-mono font-bold">{adjustment.code}</span>
          <span className="text-xs text-muted ml-1">— Chỉnh sửa</span>
        </div>
        <EditStockAdjustmentForm
          adjustment={adjustment}
          onSaved={a => { setAdjustment(a); setEditing(false); }}
          onCancel={() => setEditing(false)}
        />
      </div>
    );
  }

  const isTentative = adjustment.status === 'DRAFT' || adjustment.status === 'APPROVED';

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/stock-adjustments" className="text-muted hover:text-ink transition-colors">Điều chỉnh kho</Link>
        <span className="text-muted">/</span>
        <span className="font-mono font-bold">{adjustment.code}</span>
        <span className="text-xs text-muted ml-1">#{adjustment.id}</span>
      </div>

      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <span className={`px-2.5 py-1 rounded text-xs font-bold ${STATUS_COLORS[adjustment.status]}`}>
            {STATUS_LABELS[adjustment.status]}
          </span>
          <span className="px-2.5 py-1 rounded text-xs font-bold bg-line-2 text-ink">
            {TYPE_LABELS[adjustment.type]}
          </span>
        </div>

        <div className="flex gap-2 flex-wrap">
          {adjustment.status === 'DRAFT' && (
            <>
              <button onClick={() => setEditing(true)}
                className="px-4 py-2 border border-line text-sm font-bold rounded-sm hover:bg-paper transition-colors">
                Sửa
              </button>
              {isAdmin && (
                <button onClick={handleApprove} disabled={approving}
                  className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 disabled:opacity-50 transition-colors">
                  {approving ? 'Đang duyệt...' : 'Duyệt'}
                </button>
              )}
              <button onClick={() => setCancelOpen(true)}
                className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                Hủy phiếu
              </button>
            </>
          )}
          {adjustment.status === 'APPROVED' && (
            <>
              <button onClick={handleConfirmApply} disabled={confirming}
                className="bg-accent text-white text-sm font-bold px-4 py-2 rounded-sm hover:bg-accent-700 disabled:opacity-50 transition-colors">
                {confirming ? 'Đang áp dụng...' : 'Áp dụng vào kho'}
              </button>
              <button onClick={() => setCancelOpen(true)}
                className="border border-danger text-danger text-sm font-bold px-4 py-2 rounded-sm hover:bg-danger-bg transition-colors">
                Hủy phiếu
              </button>
            </>
          )}
        </div>
      </div>

      {!isAdmin && adjustment.status === 'DRAFT' && (
        <p className="text-xs text-muted bg-paper border border-line rounded-sm px-3 py-2">
          Chỉ ADMIN mới có thể duyệt phiếu này. Bạn có thể sửa hoặc hủy.
        </p>
      )}
      {actionError && <p className="text-danger text-sm">{actionError}</p>}

      <div className="bg-white border border-line rounded-sm p-5 space-y-3">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted mb-2">Thông tin</h3>
        <Row label="Lý do" value={adjustment.reason} />
        <Row label="Người tạo" value={adjustment.createdByEmail ?? '—'} />
        {adjustment.approvedByEmail && <Row label="Người duyệt" value={adjustment.approvedByEmail} />}
        <Row label="Ngày tạo" value={formatDateTime(adjustment.createdAt)} />
        {adjustment.approvedAt && <Row label="Ngày duyệt" value={formatDateTime(adjustment.approvedAt)} />}
        {adjustment.confirmedAt && <Row label="Ngày áp dụng" value={formatDateTime(adjustment.confirmedAt)} />}
      </div>

      {isTentative && (
        <p className="text-xs text-warn bg-warn-bg border border-warn rounded-sm px-3 py-2">
          Số lượng tồn trước/sau dưới đây chỉ là số tạm tính lúc soạn phiếu — số liệu thật có thể thay đổi do giao dịch khác xảy ra trước khi phiếu được áp dụng. Chỉ chính xác khi phiếu ở trạng thái &quot;Đã áp dụng&quot;.
        </p>
      )}

      <div className="bg-white border border-line rounded-sm overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-line bg-paper">
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">SKU / Sản phẩm</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Size / Màu</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Thay đổi</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Tồn trước</th>
              <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Tồn sau</th>
              <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ghi chú</th>
            </tr>
          </thead>
          <tbody>
            {adjustment.items.map(item => (
              <tr key={item.id} className="border-b border-line-2 last:border-0">
                <td className="px-4 py-3">
                  <div className="font-mono text-xs font-bold">{item.sku}</div>
                  <div className="text-xs text-muted">{item.productName}</div>
                </td>
                <td className="px-4 py-3 text-xs text-muted">{item.colorway} · {item.size}</td>
                <td className="px-4 py-3 text-right tabular-nums font-bold">
                  <span className={item.quantityChange < 0 ? 'text-danger' : 'text-ok'}>
                    {item.quantityChange > 0 ? `+${item.quantityChange}` : item.quantityChange}
                  </span>
                </td>
                <td className="px-4 py-3 text-right tabular-nums text-muted">{item.quantityBefore}</td>
                <td className="px-4 py-3 text-right tabular-nums text-muted">{item.quantityAfter}</td>
                <td className="px-4 py-3 text-xs text-muted">{item.note ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {cancelOpen && (
        <CancelStockAdjustmentModal
          adjustmentId={adjustment.id}
          onClose={() => setCancelOpen(false)}
          onCancelled={a => { setAdjustment(a); setCancelOpen(false); }}
        />
      )}
    </div>
  );
}
