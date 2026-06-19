'use client';

import VariantPicker, { type VariantSearchResult } from '@/components/admin/VariantPicker';
import { formatPrice } from './types';

export interface DraftPurchaseItem {
  key: string;
  variantId: number;
  sku: string;
  productName: string;
  colorway: string;
  size: number;
  quantityOrdered: number;
  unitCost: number;
}

export function draftItemFromVariant(v: VariantSearchResult): DraftPurchaseItem {
  return {
    key: `v${v.id}-${Date.now()}`,
    variantId: v.id,
    sku: v.sku,
    productName: v.productName,
    colorway: v.colorway,
    size: v.size,
    quantityOrdered: 1,
    unitCost: v.price ?? v.basePrice,
  };
}

export default function PurchaseItemsBuilder({
  items,
  onChange,
}: {
  items: DraftPurchaseItem[];
  onChange: (items: DraftPurchaseItem[]) => void;
}) {
  function handlePick(v: VariantSearchResult) {
    const existing = items.find(it => it.variantId === v.id);
    if (existing) {
      onChange(items.map(it => (it.variantId === v.id ? { ...it, quantityOrdered: it.quantityOrdered + 1 } : it)));
      return;
    }
    onChange([...items, draftItemFromVariant(v)]);
  }

  function updateItem(key: string, patch: Partial<DraftPurchaseItem>) {
    onChange(items.map(it => (it.key === key ? { ...it, ...patch } : it)));
  }

  function removeItem(key: string) {
    onChange(items.filter(it => it.key !== key));
  }

  const subtotal = items.reduce((s, it) => s + it.quantityOrdered * it.unitCost, 0);

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
                <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-28">SL đặt</th>
                <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted w-36">Đơn giá</th>
                <th className="text-right px-3 py-2 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Thành tiền</th>
                <th className="px-3 py-2 w-10" />
              </tr>
            </thead>
            <tbody>
              {items.map(it => (
                <tr key={it.key} className="border-b border-line-2 last:border-0">
                  <td className="px-3 py-2">
                    <div className="font-mono text-xs font-bold">{it.sku}</div>
                    <div className="text-xs text-muted truncate max-w-[180px]">{it.productName}</div>
                  </td>
                  <td className="px-3 py-2 text-xs text-muted whitespace-nowrap">{it.colorway} · {it.size}</td>
                  <td className="px-3 py-2">
                    <input
                      type="number"
                      min={1}
                      value={it.quantityOrdered}
                      onChange={e => updateItem(it.key, { quantityOrdered: Math.max(1, Number(e.target.value)) })}
                      className="w-full border border-line rounded-sm px-2 py-1.5 text-sm text-right focus:outline-none focus:border-ink"
                    />
                  </td>
                  <td className="px-3 py-2">
                    <input
                      type="number"
                      min={0}
                      value={it.unitCost}
                      onChange={e => updateItem(it.key, { unitCost: Math.max(0, Number(e.target.value)) })}
                      className="w-full border border-line rounded-sm px-2 py-1.5 text-sm text-right focus:outline-none focus:border-ink"
                    />
                  </td>
                  <td className="px-3 py-2 text-right tabular-nums font-semibold">
                    {formatPrice(it.quantityOrdered * it.unitCost)}
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

      {items.length > 0 && (
        <div className="flex justify-end text-sm">
          <span className="text-muted mr-2">Tạm tính:</span>
          <span className="font-bold">{formatPrice(subtotal)}</span>
        </div>
      )}
    </div>
  );
}
