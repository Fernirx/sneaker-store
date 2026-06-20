import { formatPrice, type OrderInternalResponse } from '../../_components/types';

export default function ItemsTab({ order }: { order: OrderInternalResponse }) {
  return (
    <div className="bg-white border border-line rounded-sm overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-line bg-paper">
            <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Sản phẩm</th>
            <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">SKU</th>
            <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Size / Màu</th>
            <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">SL</th>
            <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Đơn giá</th>
            <th className="text-right px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted">Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          {order.items.map(item => (
            <tr key={item.id} className="border-b border-line-2 last:border-0">
              <td className="px-4 py-3 font-medium">{item.productName}</td>
              <td className="px-4 py-3 font-body text-xs text-muted">{item.variantSku}</td>
              <td className="px-4 py-3 text-xs text-muted">{item.variantColor} · {item.variantSize}</td>
              <td className="px-4 py-3 text-right tabular-nums">{item.quantity}</td>
              <td className="px-4 py-3 text-right tabular-nums">{formatPrice(item.unitPrice)}</td>
              <td className="px-4 py-3 text-right tabular-nums font-semibold">{formatPrice(item.subtotal)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
