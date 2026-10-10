'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import StockAdjustmentItemsBuilder, { type DraftAdjustmentItem } from '../../_components/StockAdjustmentItemsBuilder';
import { TYPE_OPTIONS, type StockAdjustmentResponse, type StockAdjustmentType } from '../../_components/types';

export default function EditStockAdjustmentForm({
  adjustment,
  onSaved,
  onCancel,
}: {
  adjustment: StockAdjustmentResponse;
  onSaved: (adjustment: StockAdjustmentResponse) => void;
  onCancel: () => void;
}) {
  const [type, setType] = useState<StockAdjustmentType>(adjustment.type);
  const [reason, setReason] = useState(adjustment.reason);
  const [items, setItems] = useState<DraftAdjustmentItem[]>(
    adjustment.items.map(it => ({
      key: `item-${it.id}`,
      variantId: it.variantId,
      sku: it.sku,
      productName: it.productName,
      colorway: it.colorway,
      size: it.size,
      stockQuantity: it.quantityBefore,
      sign: it.quantityChange < 0 ? -1 : 1,
      quantity: Math.abs(it.quantityChange),
      note: it.note ?? '',
    })),
  );

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (items.length === 0) {
      setError('Cần có ít nhất một sản phẩm.');
      return;
    }
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      const { data } = await clientAxios.patch(`/api/admin/stock-adjustments/${adjustment.id}`, {
        type,
        reason,
        items: items.map(it => ({
          variantId: it.variantId,
          quantityChange: it.sign * it.quantity,
          note: it.note || null,
        })),
      });
      onSaved(data.data);
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      {error && <p className="text-danger text-sm">{error}</p>}

      <div className="bg-white border border-line rounded-sm p-5 space-y-4">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted">Thông tin chung</h3>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Loại phiếu <span className="text-danger">*</span>
          </label>
          <div className="flex gap-2">
            {TYPE_OPTIONS.map(o => (
              <button
                key={o.value}
                type="button"
                onClick={() => setType(o.value)}
                className={`px-3 py-2 rounded-sm text-sm font-bold border transition-colors ${
                  type === o.value ? 'bg-accent text-white border-accent' : 'border-line text-muted hover:bg-paper'
                }`}
              >
                {o.label}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Lý do <span className="text-danger">*</span>
          </label>
          <textarea
            value={reason}
            onChange={e => setReason(e.target.value.replace(/^\s+/, ''))}
            required
            maxLength={255}
            rows={2}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
          />
          {fieldErrors.reason && <p className="text-danger text-xs mt-1">{fieldErrors.reason}</p>}
        </div>
      </div>

      <div className="bg-white border border-line rounded-sm p-5 space-y-4">
        <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted">Sản phẩm</h3>
        <StockAdjustmentItemsBuilder type={type} items={items} onChange={setItems} />
      </div>

      <div className="flex justify-end gap-2">
        <button type="button" onClick={onCancel} className="px-4 py-2.5 border border-line text-sm rounded-sm hover:bg-paper transition-colors">
          Hủy
        </button>
        <button
          type="submit"
          disabled={saving || !reason.trim()}
          className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-5 py-2.5 rounded-sm hover:bg-accent-700 transition-colors disabled:opacity-50"
        >
          {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
        </button>
      </div>
    </form>
  );
}
