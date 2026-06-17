'use client';

import { useState } from 'react';
import { Link } from '@/i18n/routing';
import { useCart, type CartItemData } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { parseApiError } from '@/lib/parseApiError';
import { formatPrice } from '../../products/_components/types';
import clientAxios from '@/lib/axios/clientAxios';

type ShippingForm = {
  fullName: string;
  phone: string;
  address: string;
  note: string;
};

// ── Read-only item row ────────────────────────────────────────────────────────

function CheckoutItem({ item }: { item: CartItemData }) {
  const unit = Number(item.unitPrice);
  const orig = item.originalPrice ? Number(item.originalPrice) : null;
  const pct  = orig ? Math.round((1 - unit / orig) * 100) : 0;

  return (
    <div className="flex gap-4 py-4 border-b border-line last:border-b-0">
      <div className="shrink-0 w-[72px] h-[72px] rounded-sm border border-line bg-paper overflow-hidden">
        {item.primaryImagePublicId ? (
          <img
            src={productUrl(item.primaryImagePublicId, 144, 144)}
            alt={item.productName}
            className="w-full h-full object-contain p-1"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-faint text-xs">—</div>
        )}
      </div>

      <div className="flex-1 min-w-0">
        <p className="text-[13px] font-semibold text-ink leading-snug line-clamp-2">
          {item.productName}
        </p>
        <p className="text-[12px] text-muted mt-0.5">{item.colorway} · Size {item.size}</p>
        <p className="text-[12px] text-muted mt-0.5">Số lượng: {item.quantity}</p>
      </div>

      <div className="shrink-0 flex flex-col items-end gap-0.5 pt-0.5">
        {orig && (
          <span className="text-[10px] font-bold text-white bg-accent px-1 py-0.5 rounded-sm leading-none">
            -{pct}%
          </span>
        )}
        <span className={`text-[14px] font-bold tabular-nums ${orig ? 'text-accent' : 'text-ink'}`}>
          {formatPrice(unit * item.quantity)}
        </span>
        {orig && (
          <span className="text-[11px] text-faint line-through tabular-nums">
            {formatPrice(orig * item.quantity)}
          </span>
        )}
      </div>
    </div>
  );
}

// ── Skeleton ──────────────────────────────────────────────────────────────────

function CheckoutSkeleton() {
  return (
    <div className="max-w-5xl mx-auto px-4 py-10">
      <div className="h-9 w-48 bg-line rounded-sm mb-8 animate-pulse" />
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-10">
        <div className="space-y-6">
          <div className="h-56 bg-line rounded-sm animate-pulse" />
          <div className="h-64 bg-line rounded-sm animate-pulse" />
        </div>
        <div className="h-64 bg-line rounded-sm animate-pulse" />
      </div>
    </div>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function CheckoutClient() {
  const { cart, loading } = useCart();
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState<ShippingForm>({
    fullName: '',
    phone: '',
    address: '',
    note: '',
  });

  if (loading) return <CheckoutSkeleton />;

  const selectedItems = (cart?.items ?? []).filter(i => i.selected && !i.outOfStock);
  const totalAmount   = Number(cart?.totalAmount ?? 0);

  if (selectedItems.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <p className="text-muted text-[14px]">Không có sản phẩm nào được chọn để thanh toán.</p>
        <Link
          href="/cart"
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          Quay lại giỏ hàng
        </Link>
      </div>
    );
  }

  function handleField(field: keyof ShippingForm) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setForm(prev => ({ ...prev, [field]: e.target.value }));
    };
  }

  async function handlePayment() {
    setError('');
    if (!form.fullName.trim() || !form.phone.trim() || !form.address.trim()) {
      setError('Vui lòng điền đầy đủ họ tên, số điện thoại và địa chỉ.');
      return;
    }

    setPaying(true);
    try {
      // TODO: Thay bằng order creation endpoint khi order module sẵn sàng
      // const { data: order } = await clientAxios.post('/api/orders', {
      //   items: selectedItems.map(i => ({ cartItemId: i.id })),
      //   shipping: form,
      // });
      // const { orderId, orderCode } = order.data;
      const orderId    = 0;
      const orderCode  = 'TEST-ORDER';

      const { data } = await clientAxios.post('/api/payment', {
        orderId,
        orderCode,
        amount: totalAmount,
      });

      window.location.href = data.data; // redirect sang VNPay
    } catch (err) {
      const { general } = parseApiError(err, 'Không thể khởi tạo thanh toán. Vui lòng thử lại.');
      setError(general);
      setPaying(false);
    }
  }

  const subtotalOriginal = selectedItems.reduce((sum, i) => {
    const price = i.originalPrice ? Number(i.originalPrice) : Number(i.unitPrice);
    return sum + price * i.quantity;
  }, 0);
  const totalDiscount = subtotalOriginal - totalAmount;

  return (
    <div className="max-w-5xl mx-auto px-4 py-10">

      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <Link href="/cart" className="text-muted hover:text-ink transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M11 6l-6 6 6 6"/>
          </svg>
        </Link>
        <h1 className="font-display font-black text-3xl uppercase tracking-tight">Thanh toán</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-10 items-start">

        {/* ── LEFT ── */}
        <div className="space-y-6">

          {/* Shipping form */}
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                Thông tin giao hàng
              </h2>
            </div>
            <div className="p-5 space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    Họ và tên <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    value={form.fullName}
                    onChange={handleField('fullName')}
                    placeholder="Nguyễn Văn A"
                    className="w-full h-10 px-3 border border-line rounded-sm text-[13px] text-ink placeholder:text-faint bg-white focus:outline-none focus:border-ink transition-colors"
                  />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    Số điện thoại <span className="text-danger">*</span>
                  </label>
                  <input
                    type="tel"
                    value={form.phone}
                    onChange={handleField('phone')}
                    placeholder="0901234567"
                    className="w-full h-10 px-3 border border-line rounded-sm text-[13px] text-ink placeholder:text-faint bg-white focus:outline-none focus:border-ink transition-colors"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                  Địa chỉ <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  value={form.address}
                  onChange={handleField('address')}
                  placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố"
                  className="w-full h-10 px-3 border border-line rounded-sm text-[13px] text-ink placeholder:text-faint bg-white focus:outline-none focus:border-ink transition-colors"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                  Ghi chú
                </label>
                <textarea
                  value={form.note}
                  onChange={handleField('note')}
                  placeholder="Ghi chú cho đơn hàng (không bắt buộc)"
                  rows={3}
                  className="w-full px-3 py-2.5 border border-line rounded-sm text-[13px] text-ink placeholder:text-faint bg-white focus:outline-none focus:border-ink transition-colors resize-none"
                />
              </div>
            </div>
          </section>

          {/* Items list */}
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                Sản phẩm ({selectedItems.length})
              </h2>
            </div>
            <div className="px-5">
              {selectedItems.map(item => (
                <CheckoutItem key={item.id} item={item} />
              ))}
            </div>
          </section>
        </div>

        {/* ── RIGHT ── */}
        <aside className="lg:sticky lg:top-[84px] space-y-3">

          {/* Summary */}
          <div className="border border-line rounded-sm overflow-hidden bg-line-2">
            <div className="px-5 py-3.5 border-b border-line">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                Tóm tắt đơn hàng
              </h2>
            </div>
            <div className="px-5 pt-4 pb-3 space-y-2.5">
              <div className="flex justify-between items-baseline text-[13px]">
                <span className="text-muted">Tạm tính:</span>
                <span className="tabular-nums text-ink">{formatPrice(subtotalOriginal)}</span>
              </div>
              {totalDiscount > 0 && (
                <div className="flex justify-between items-baseline text-[13px]">
                  <span className="text-muted">Giảm giá:</span>
                  <span className="tabular-nums text-ok">-{formatPrice(totalDiscount)}</span>
                </div>
              )}
              <div className="flex justify-between items-baseline text-[13px]">
                <span className="text-muted">Phí giao hàng:</span>
                <span className="tabular-nums text-ok">Miễn phí</span>
              </div>
            </div>
            <div className="border-t border-line px-5 py-4 flex justify-between items-center">
              <span className="text-[12px] font-bold uppercase tracking-widest text-ink">Tổng cộng:</span>
              <span className="text-[20px] font-bold tabular-nums text-ink">{formatPrice(totalAmount)}</span>
            </div>
          </div>

          {error && (
            <p className="text-[12px] text-danger bg-danger/5 border border-danger/20 rounded-sm px-3 py-2.5">
              {error}
            </p>
          )}

          <button
            onClick={handlePayment}
            disabled={paying}
            className="w-full bg-ink text-white text-[12px] font-bold uppercase tracking-widest py-3.5 rounded-sm hover:bg-accent transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {paying ? (
              <>
                <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                Đang xử lý...
              </>
            ) : (
              'Thanh toán qua VNPay'
            )}
          </button>

          <p className="text-[11px] text-muted text-center leading-relaxed">
            Bạn sẽ được chuyển đến cổng thanh toán VNPay để hoàn tất giao dịch.
          </p>
        </aside>
      </div>
    </div>
  );
}
