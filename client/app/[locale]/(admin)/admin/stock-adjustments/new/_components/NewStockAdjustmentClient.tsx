'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import StockAdjustmentItemsBuilder, { type DraftAdjustmentItem } from '../../_components/StockAdjustmentItemsBuilder';
import { TYPE_OPTIONS, type StockAdjustmentType } from '../../_components/types';

export default function NewStockAdjustmentClient() {
  const router = useRouter();

  const [type, setType] = useState<StockAdjustmentType>('EXPORT');
  const [reason, setReason] = useState('');
  const [items, setItems] = useState<DraftAdjustmentItem[]>([]);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function handleTypeChange(next: StockAdjustmentType) {
    setType(next);
    setItems(items.map(it => ({ ...it, sign: next === 'EXPORT' ? -1 : 1 })));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (items.length === 0) {
      setError('Cần thêm ít nhất một sản phẩm.');
      return;
    }
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      const { data } = await clientAxios.post('/api/admin/stock-adjustments', {
        type,
        reason,
        items: items.map(it => ({
          variantId: it.variantId,
          quantityChange: it.sign * it.quantity,
          note: it.note || null,
        })),
      });
      router.push(`/admin/stock-adjustments/${data.data.id}`);
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-5 max-w-4xl">
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/stock-adjustments" className="text-muted hover:text-ink transition-colors">
          Điều chỉnh kho
        </Link>
        <span className="text-muted">/</span>
        <span className="font-bold">Tạo mới</span>
      </div>

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
                  onClick={() => handleTypeChange(o.value)}
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
              onChange={e => setReason(e.target.value)}
              required
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
          <Link href="/admin/stock-adjustments" className="px-4 py-2.5 border border-line text-sm rounded-sm hover:bg-paper transition-colors">
            Hủy
          </Link>
          <button
            type="submit"
            disabled={saving}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-5 py-2.5 rounded-sm hover:bg-accent-700 transition-colors disabled:opacity-50"
          >
            {saving ? 'Đang lưu...' : 'Tạo phiếu (Nháp)'}
          </button>
        </div>
      </form>
    </div>
  );
}
