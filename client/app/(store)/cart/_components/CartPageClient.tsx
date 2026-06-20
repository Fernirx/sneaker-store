'use client';

import { useState, useTransition } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useCart, type CartItemData } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { formatPrice } from '../../products/_components/types';
import { parseApiError } from '@/lib/parseApiError';


// ── Item row ──────────────────────────────────────────────────────────────────

function CartItemRow({ item }: { item: CartItemData }) {
  const { updateItem, removeItem, toggleSelection } = useCart();
  const [qty, setQty] = useState(item.quantity);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState('');

  async function handleQty(next: number) {
    if (next < 1 || next === qty) return;
    setError('');
    setUpdating(true);
    try {
      setQty(next);
      await updateItem(item.id, next);
    } catch (err) {
      const { general } = parseApiError(err, "Lỗi cập nhật");
      setError(general);
      setQty(item.quantity);
    } finally {
      setUpdating(false);
    }
  }

  async function handleRemove() {
    setUpdating(true);
    try { await removeItem(item.id); } catch { setUpdating(false); }
  }

  async function handleToggleSelection(selected: boolean) {
    setUpdating(true);
    try { await toggleSelection(item.id, selected); } catch { /* ignore */ }
    finally { setUpdating(false); }
  }

  const lowStock = !item.outOfStock && item.stockQuantity > 0 && item.stockQuantity <= 3;
  const orig     = item.originalPrice ? Number(item.originalPrice) : null;
  const unit     = Number(item.unitPrice);
  const pct      = orig ? Math.round((1 - unit / orig) * 100) : 0;

  return (
    <div
      className={`flex gap-4 py-5 border-b border-line transition-opacity ${
        updating ? 'opacity-40 pointer-events-none' : ''
      } ${!item.selected ? 'opacity-50' : ''}`}
    >
      {/* Checkbox */}
      <div className="pt-1 shrink-0">
        <input
          type="checkbox"
          checked={item.selected}
          onChange={(e) => handleToggleSelection(e.target.checked)}
          className="w-4 h-4 rounded-sm cursor-pointer accent-ink"
        />
      </div>

      {/* Image */}
      <Link
        href={`/products/${item.productSlug}`}
        className="shrink-0 w-[88px] h-[88px] rounded-sm border border-line bg-paper overflow-hidden"
      >
        {item.primaryImagePublicId ? (
          <img
            src={productUrl(item.primaryImagePublicId, 176, 176)}
            alt={item.productName}
            className="w-full h-full object-contain p-1.5"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-faint text-xs">—</div>
        )}
      </Link>

      {/* Info */}
      <div className="flex-1 min-w-0 flex flex-col justify-between gap-2">
        <div>
          <Link
            href={`/products/${item.productSlug}`}
            className="text-[13px] font-semibold leading-snug text-ink hover:text-accent transition-colors line-clamp-2"
          >
            {item.productName}
          </Link>

          {/* Màu · Size */}
          <p className="text-[12px] text-muted mt-1 leading-tight">
            {item.colorway} &nbsp;·&nbsp; {"Size"} {item.size}
          </p>

          {/* Out of stock / low stock */}
          <div className="flex flex-wrap gap-1.5 mt-1.5">
            {item.outOfStock && (
              <span className="text-[10px] font-black uppercase tracking-widest text-danger">
                {"Hết hàng"}
              </span>
            )}
            {lowStock && (
              <span className="text-[10px] text-warn">
                {`Chỉ còn ${item.stockQuantity}`}
              </span>
            )}
          </div>

          {error && <p className="text-xs text-danger mt-1">{error}</p>}
        </div>

        {/* Stepper + Remove */}
        <div className="flex items-center gap-4">
          <div className="inline-flex items-center border border-line rounded-sm overflow-hidden divide-x divide-line h-8">
            <button
              onClick={() => handleQty(qty - 1)}
              disabled={updating || qty <= 1}
              className="w-8 h-8 flex items-center justify-center text-ink text-base hover:bg-paper disabled:opacity-30 disabled:cursor-not-allowed transition-colors select-none"
            >
              −
            </button>
            <span className="w-9 h-8 flex items-center justify-center text-[13px] font-semibold tabular-nums text-ink">
              {qty}
            </span>
            <button
              onClick={() => handleQty(qty + 1)}
              disabled={updating || item.outOfStock || qty >= item.stockQuantity}
              className="w-8 h-8 flex items-center justify-center text-ink text-base hover:bg-paper disabled:opacity-30 disabled:cursor-not-allowed transition-colors select-none"
            >
              +
            </button>
          </div>

          <button
            onClick={handleRemove}
            disabled={updating}
            className="text-[12px] text-muted hover:text-danger transition-colors disabled:opacity-40"
          >
            {"Xóa"}
          </button>
        </div>
      </div>

      {/* Price */}
      <div className="shrink-0 flex flex-col items-end gap-0.5 pt-0.5 min-w-[80px]">
        {orig && (
          <span className="text-[10px] font-bold text-white bg-accent px-1.5 py-0.5 rounded-sm tabular-nums leading-none">
            -{pct}%
          </span>
        )}
        <span className={`text-[15px] font-bold tabular-nums leading-tight ${orig ? 'text-accent' : 'text-ink'}`}>
          {formatPrice(unit * qty)}
        </span>
        {orig && (
          <span className="text-[11px] text-faint line-through tabular-nums leading-none">
            {formatPrice(orig * qty)}
          </span>
        )}
        {qty > 1 && (
          <span className="text-[11px] text-muted tabular-nums leading-none mt-0.5">
            {formatPrice(unit)} × {qty}
          </span>
        )}
      </div>
    </div>
  );
}

// ── FAQ accordion item ────────────────────────────────────────────────────────

function FaqItem({ title, body }: { title: string; body: string }) {
  const [open, setOpen] = useState(false);
  return (
    <div className="border-b border-line last:border-b-0">
      <button
        onClick={() => setOpen(v => !v)}
        className="w-full flex justify-between items-center px-5 py-3.5 text-left text-[13px] font-medium text-ink hover:bg-paper transition-colors"
      >
        <span>{title}</span>
        <svg
          width="12" height="12" viewBox="0 0 24 24" fill="none"
          stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"
          className={`text-muted shrink-0 transition-transform ${open ? 'rotate-180' : ''}`}
        >
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </button>
      {open && (
        <p className="px-5 pb-4 text-[12px] text-muted leading-relaxed">
          {body}
        </p>
      )}
    </div>
  );
}

// ── Order summary sidebar ─────────────────────────────────────────────────────

function OrderSummary({
  items,
  totalAmount,
  onCheckout,
  onClear,
  clearing,
}: {
  items: CartItemData[];
  totalAmount: number;
  onCheckout: () => void;
  onClear: () => void;
  clearing: boolean;
}) {
  const selectedItems         = items.filter(i => i.selected);
  const subtotalOriginal      = selectedItems.reduce((sum, i) => {
    const price = i.originalPrice ? Number(i.originalPrice) : Number(i.unitPrice);
    return sum + price * i.quantity;
  }, 0);
  const totalDiscount         = subtotalOriginal - totalAmount;
  const hasOutOfStockSelected = selectedItems.some(i => i.outOfStock);

  return (
    <aside className="lg:sticky lg:top-[84px] space-y-3">

      {/* ── Price block ── */}
      <div className="border border-line rounded-sm overflow-hidden bg-line-2">

        {/* Header */}
        <div className="px-5 py-3.5 border-b border-line">
          <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
            {"Tạm tính"}
          </h2>
        </div>

        {/* Price rows */}
        <div className="px-5 pt-4 pb-3 space-y-2.5">
          <div className="flex justify-between items-baseline text-[13px]">
            <span className="text-muted">{"Giá bán"}:</span>
            <span className="tabular-nums text-ink">{formatPrice(subtotalOriginal)}</span>
          </div>
          {totalDiscount > 0 && (
            <div className="flex justify-between items-baseline text-[13px]">
              <span className="text-muted">{"Giảm giá"}:</span>
              <span className="tabular-nums text-ok">-{formatPrice(totalDiscount)}</span>
            </div>
          )}
          <div className="flex justify-between items-baseline text-[13px]">
            <span className="text-muted">{"Đơn hàng"}:</span>
            <span className="tabular-nums text-ink">{formatPrice(totalAmount)}</span>
          </div>
        </div>

        {/* Final total */}
        <div className="border-t border-line px-5 py-4 flex justify-between items-center bg-line-2">
          <span className="text-[12px] font-bold uppercase tracking-widest text-ink">
            {"Tạm tính"}:
          </span>
          <span className="text-[20px] font-bold tabular-nums text-ink">
            {formatPrice(totalAmount)}
          </span>
        </div>
      </div>

      {/* ── Checkout ── */}
      <button
        onClick={onCheckout}
        disabled={hasOutOfStockSelected || selectedItems.length === 0}
        className="w-full bg-ink text-white text-[12px] font-bold uppercase tracking-widest py-3.5 rounded-sm hover:bg-accent transition-colors disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-ink"
      >
        {"Tiến hành thanh toán"}
      </button>
      {hasOutOfStockSelected && (
        <p className="text-[11px] text-danger text-center leading-snug -mt-1">
          {"Giỏ hàng có sản phẩm hết hàng, vui lòng bỏ chọn hoặc xóa để tiếp tục."}
        </p>
      )}

      {/* ── Policy accordion ── */}
      <div className="border border-line rounded-sm overflow-hidden bg-white">
        <FaqItem title={"Chính sách bảo hành"} body={"Sản phẩm chính hãng 100%, được bảo hành theo chính sách của nhà sản xuất. Liên hệ STRIDE để được hỗ trợ trong vòng 30 ngày kể từ ngày nhận hàng."} />
        <FaqItem title={"Chính sách đổi trả"}   body={"Đổi trả miễn phí trong 30 ngày với sản phẩm còn nguyên vẹn, đầy đủ hộp và phụ kiện. Không áp dụng cho sản phẩm đã qua sử dụng hoặc đang khuyến mãi."} />
      </div>

      {/* ── Clear cart ── */}
      <button
        onClick={onClear}
        disabled={clearing}
        className="w-full text-[12px] text-muted hover:text-danger transition-colors disabled:opacity-40 text-center py-1"
      >
        {clearing ? "Đang xóa..." : "Xóa giỏ hàng"}
      </button>
    </aside>
  );
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

function CartSkeleton() {
  return (
    <div className="max-w-5xl mx-auto px-4 py-12">
      <div className="h-8 w-36 bg-line rounded-sm mb-2 animate-pulse" />
      <div className="h-4 w-24 bg-line rounded-sm mb-10 animate-pulse" />
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-8 items-start">
        <div>
          {[1, 2, 3].map(i => (
            <div key={i} className="flex gap-4 py-5 border-b border-line">
              <div className="w-[88px] h-[88px] bg-line rounded-sm animate-pulse shrink-0" />
              <div className="flex-1 space-y-2 pt-1">
                <div className="h-4 w-3/4 bg-line rounded-sm animate-pulse" />
                <div className="h-3 w-2/5 bg-line rounded-sm animate-pulse" />
                <div className="h-8 w-24 bg-line rounded-sm animate-pulse mt-4" />
              </div>
            </div>
          ))}
        </div>
        <div className="h-52 bg-line rounded-sm animate-pulse" />
      </div>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function CartPageClient() {
  const router = useRouter();
  const { cart, loading, clearCart } = useCart();
  const [clearing, startClearing] = useTransition();

  function handleClear() {
    startClearing(async () => { await clearCart().catch(() => {}); });
  }

  if (loading) return <CartSkeleton />;

  const items = cart?.items ?? [];

  if (items.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" className="text-line">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
          <line x1="3" y1="6" x2="21" y2="6"/>
          <path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        <p className="text-muted text-[14px]">{"Giỏ hàng của bạn đang trống."}</p>
        <Link
          href="/products"
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {"Tiếp tục mua sắm"}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-10">
      <h1 className="font-display font-black text-3xl uppercase tracking-tight mb-1">
        {"Giỏ hàng"}
      </h1>
      <p className="text-[13px] text-muted mb-8">
        {`${cart?.totalItems ?? 0} sản phẩm`}
      </p>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-10 items-start">
        {/* Item list */}
        <section>
          {items.map(item => (
            <CartItemRow key={item.id} item={item} />
          ))}

          <Link
            href="/products"
            className="inline-flex items-center gap-1.5 mt-6 text-[12px] text-muted hover:text-ink transition-colors"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M19 12H5M11 6l-6 6 6 6"/>
            </svg>
            {"Tiếp tục mua sắm"}
          </Link>
        </section>

        {/* Summary */}
        <OrderSummary
          items={items}
          totalAmount={Number(cart?.totalAmount ?? 0)}
          onCheckout={() => router.push('/checkout')}
          onClear={handleClear}
          clearing={clearing}
        />
      </div>
    </div>
  );
}
