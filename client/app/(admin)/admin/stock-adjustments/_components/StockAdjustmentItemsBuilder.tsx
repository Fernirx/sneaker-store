'use client';

import VariantPicker, { type VariantSearchResult } from '@/components/admin/VariantPicker';
import { type StockAdjustmentType } from './types';

export interface DraftAdjustmentItem {
  key: string;
  variantId: number;
  sku: string;
  productName: string;
  colorway: string;
  size: number;
  stockQuantity: number;
  sign: 1 | -1;
  quantity: number;
  note: string;
}

export default function StockAdjustmentItemsBuilder({
  type,
  items,
  onChange,
}: {
  type: StockAdjustmentType;
  items: DraftAdjustmentItem[];
  onChange: (items: DraftAdjustmentItem[]) => void;
}) {
  function handlePick(v: VariantSearchResult) {
    if (items.some(it => it.variantId === v.id)) return;
    const item: DraftAdjustmentItem = {
      key: `v${v.id}-${Date.now()}`,
      variantId: v.id,
      sku: v.sku,
      productName: v.productName,
      colorway: v.colorway,
      size: v.size,
      stockQuantity: v.stockQuantity,
      sign: type === 'EXPORT' ? -1 : 1,
      quantity: 1,
      note: '',
    };
    onChange([...items, item]);
  }

  function updateItem(key: string, patch: Partial<DraftAdjustmentItem>) {
    onChange(items.map(it => (it.key === key ? { ...it, ...patch } : it)));
  }

  function removeItem(key: string) {
    onChange(items.filter(it => it.key !== key));
  }

  return (
    <div className="space-y-3">
      <VariantPicker onSelect={handlePick} />

      {items.length === 0 ? (
        <p className="text-sm text-muted text-center py-8 border border-dashed border-line rounded-sm">
          Chưa có sản phẩm nào. Tìm và chọn variant ở trên để thêm.
        </p>
      ) : (
        <div className="border border-line rounded-sm overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-line bg-paper">
                <th className="text-left px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">SKU / Sản phẩm</th>
                <th className="text-left px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Size / Màu</th>
                <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-20">Tồn hiện tại</th>
                <th className="text-center px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-32">+ / −</th>
                <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-24">Số lượng</th>
                <th className="text-left px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Ghi chú</th>
                <th className="px-3 py-2 w-10" />
              </tr>
            </thead>
            <tbody>
              {items.map(it => (
                <tr key={it.key} className="border-b border-line-2 last:border-0">
                  <td className="px-3 py-2">
                    <div className="font-body text-xs font-bold">{it.sku}</div>
                    <div className="text-xs text-muted truncate max-w-[160px]">{it.productName}</div>
                  </td>
                  <td className="px-3 py-2 text-xs text-muted whitespace-nowrap">{it.colorway} · {it.size}</td>
                  <td className="px-3 py-2 text-right tabular-nums text-muted">{it.stockQuantity}</td>
                  <td className="px-3 py-2">
                    <div className="flex justify-center gap-1">
                      <button type="button" onClick={() => updateItem(it.key, { sign: 1 })}
                        className={`px-2.5 py-1 rounded-sm text-xs font-bold border ${it.sign === 1 ? 'bg-ok text-white border-ok' : 'border-line text-muted hover:bg-paper'}`}>
                        +
                      </button>
                      <button type="button" onClick={() => updateItem(it.key, { sign: -1 })}
                        className={`px-2.5 py-1 rounded-sm text-xs font-bold border ${it.sign === -1 ? 'bg-danger text-white border-danger' : 'border-line text-muted hover:bg-paper'}`}>
                        −
                      </button>
                    </div>
                  </td>
                  <td className="px-3 py-2">
                    <input
                      type="number" min={1}
                      value={it.quantity}
                      onChange={e => updateItem(it.key, { quantity: Math.max(1, Number(e.target.value)) })}
                      className="w-full border border-line rounded-sm px-2 py-1.5 text-sm text-right focus:outline-none focus:border-ink"
                    />
                  </td>
                  <td className="px-3 py-2">
                    <input
                      value={it.note}
                      onChange={e => updateItem(it.key, { note: e.target.value })}
                      className="w-full border border-line rounded-sm px-2 py-1.5 text-sm focus:outline-none focus:border-ink"
                    />
                  </td>
                  <td className="px-3 py-2 text-right">
                    <button type="button" onClick={() => removeItem(it.key)} className="text-danger text-xs font-bold hover:opacity-75">
                      Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
