'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { type PurchaseResponse } from '../../_components/types';

interface ReceiveRow {
  purchaseItemId: number;
  sku: string;
  productName: string;
  colorway: string;
  size: number;
  quantityOrdered: number;
  quantityReceived: string;
  defectiveQty: string;
  note: string;
}

export default function ReceivePurchaseModal({
  purchase,
  onClose,
  onReceived,
}: {
  purchase: PurchaseResponse;
  onClose: () => void;
  onReceived: (purchase: PurchaseResponse) => void;
}) {
  const [rows, setRows] = useState<ReceiveRow[]>(
    purchase.items.map(it => ({
      purchaseItemId: it.id,
      sku: it.sku,
      productName: it.productName,
      colorway: it.colorway,
      size: it.size,
      quantityOrdered: it.quantityOrdered,
      quantityReceived: String(it.quantityOrdered),
      defectiveQty: '0',
      note: '',
    })),
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  function updateRow(purchaseItemId: number, patch: Partial<ReceiveRow>) {
    setRows(rs => rs.map(r => (r.purchaseItemId === purchaseItemId ? { ...r, ...patch } : r)));
  }

  const invalidRow = rows.find(r => Number(r.defectiveQty || 0) > Number(r.quantityReceived || 0));

  async function handleSubmit() {
    if (invalidRow) {
      setError(`Số lượng lỗi không được vượt số lượng nhận (SKU ${invalidRow.sku}).`);
      return;
    }
    setSaving(true);
    setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/purchases/${purchase.id}/receive`, {
        items: rows.map(r => ({
          purchaseItemId: r.purchaseItemId,
          quantityReceived: Number(r.quantityReceived || 0),
          defectiveQty: Number(r.defectiveQty || 0),
          note: r.note || null,
        })),
      });
      onReceived(data.data);
    } catch (err) {
      setError(parseApiError(err).general);
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={onClose}>
      <div
        className="bg-white rounded-lg shadow-xl w-full max-w-3xl max-h-[85vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-line sticky top-0 bg-white">
          <h3 className="font-display font-black text-sm uppercase tracking-wide">Nhận hàng — {purchase.purchaseCode}</h3>
          <button onClick={onClose} className="text-muted hover:text-ink text-xl leading-none">&times;</button>
        </div>

        <div className="px-5 py-4 space-y-4">
          {error && <p className="text-danger text-sm">{error}</p>}
          <p className="text-xs text-muted">
            Bắt buộc nhập đủ tất cả sản phẩm trong phiếu. Số lượng lỗi sẽ không được cộng vào tồn kho bán được.
          </p>

          <div className="border border-line rounded-sm overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-line bg-paper">
                  <th className="text-left px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">SKU / Sản phẩm</th>
                  <th className="text-left px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Size / Màu</th>
                  <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-20">SL đặt</th>
                  <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-28">SL nhận</th>
                  <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-28">SL lỗi</th>
                  <th className="text-left px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ghi chú</th>
                </tr>
              </thead>
              <tbody>
                {rows.map(r => (
                  <tr key={r.purchaseItemId} className="border-b border-line-2 last:border-0">
                    <td className="px-3 py-2">
                      <div className="font-mono text-xs font-bold">{r.sku}</div>
                      <div className="text-xs text-muted truncate max-w-[160px]">{r.productName}</div>
                    </td>
                    <td className="px-3 py-2 text-xs text-muted whitespace-nowrap">{r.colorway} · {r.size}</td>
                    <td className="px-3 py-2 text-right tabular-nums">{r.quantityOrdered}</td>
                    <td className="px-3 py-2">
                      <input
                        type="number" min={0}
                        value={r.quantityReceived}
                        onChange={e => updateRow(r.purchaseItemId, { quantityReceived: e.target.value })}
                        className="w-full border border-line rounded-sm px-2 py-1.5 text-sm text-right focus:outline-none focus:border-ink"
                      />
                    </td>
                    <td className="px-3 py-2">
                      <input
                        type="number" min={0}
                        value={r.defectiveQty}
                        onChange={e => updateRow(r.purchaseItemId, { defectiveQty: e.target.value })}
                        className="w-full border border-line rounded-sm px-2 py-1.5 text-sm text-right focus:outline-none focus:border-ink"
                      />
                    </td>
                    <td className="px-3 py-2">
                      <input
                        value={r.note}
                        onChange={e => updateRow(r.purchaseItemId, { note: e.target.value })}
                        className="w-full border border-line rounded-sm px-2 py-1.5 text-sm focus:outline-none focus:border-ink"
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex justify-end gap-2 pt-1">
            <button onClick={onClose} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors">
              Hủy
            </button>
            <button
              onClick={handleSubmit}
              disabled={saving}
              className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
            >
              {saving ? 'Đang lưu...' : 'Xác nhận nhận hàng'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
