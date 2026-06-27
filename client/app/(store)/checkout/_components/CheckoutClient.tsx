'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useCart, type CartItemData } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { parseApiError } from '@/lib/parseApiError';
import { guestHeaders } from '@/lib/guestToken';
import { formatPrice } from '../../products/_components/types';
import clientAxios from '@/lib/axios/clientAxios';

type ShippingForm = {
  recipientName: string;
  recipientPhone: string;
  shippingStreet: string;
  shippingWard: string;
  shippingDistrict: string;
  shippingProvince: string;
  note: string;
};

interface Address {
  id: number;
  name: string;
  phone: string;
  street: string;
  ward?: string;
  district: string;
  province: string;
  postalCode?: string;
  defaultAddress: boolean;
}

type PaymentMethod = 'VNPAY' | 'COD';

const EMPTY_FORM: ShippingForm = {
  recipientName: '',
  recipientPhone: '',
  shippingStreet: '',
  shippingWard: '',
  shippingDistrict: '',
  shippingProvince: '',
  note: '',
};

function FieldError({ msg }: { msg?: string }) {
  return msg ? <p className="text-[11px] text-danger mt-1">{msg}</p> : null;
}

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
        <p className="text-[12px] text-muted mt-0.5">{item.colorway} · {"Size"} {item.size}</p>
        <p className="text-[12px] text-muted mt-0.5">{"Số lượng"}: {item.quantity}</p>
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

export default function CheckoutClient({ isLoggedIn }: { isLoggedIn: boolean }) {
  const router = useRouter();
  const { cart, loading } = useCart();

  const [form, setForm] = useState<ShippingForm>(EMPTY_FORM);
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('VNPAY');

  const [guestEmail, setGuestEmail] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [sendingOtp, setSendingOtp] = useState(false);
  const [resendSeconds, setResendSeconds] = useState(0);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [savedAddresses, setSavedAddresses] = useState<Address[]>([]);
  const [loadingAddresses, setLoadingAddresses] = useState(false);

  const [couponCode, setCouponCode] = useState('');
  const [couponInput, setCouponInput] = useState('');
  const [couponDiscount, setCouponDiscount] = useState<number>(0);
  const [couponError, setCouponError] = useState('');
  const [loadingCoupon, setLoadingCoupon] = useState(false);

  useEffect(() => {
    if (!isLoggedIn) return;
    setLoadingAddresses(true);
    clientAxios.get('/api/me/addresses')
      .then(({ data }) => {
        const addrs = data.data ?? [];
        setSavedAddresses(addrs);
        const def = addrs.find((a: Address) => a.defaultAddress) || addrs[0];
        if (def) {
          setForm(prev => ({
            ...prev,
            recipientName: def.name,
            recipientPhone: def.phone,
            shippingStreet: def.street,
            shippingWard: def.ward ?? '',
            shippingDistrict: def.district,
            shippingProvince: def.province,
          }));
        }
      })
      .catch(() => {})
      .finally(() => setLoadingAddresses(false));
  }, [isLoggedIn]);

  function handleSelectAddress(addr: Address) {
    setForm(prev => ({
      ...prev,
      recipientName: addr.name,
      recipientPhone: addr.phone,
      shippingStreet: addr.street,
      shippingWard: addr.ward ?? '',
      shippingDistrict: addr.district,
      shippingProvince: addr.province,
    }));
    setFieldErrors({});
  }

  if (loading) return <CheckoutSkeleton />;

  const selectedItems = (cart?.items ?? []).filter(i => i.selected && !i.outOfStock);
  const totalAmount   = Number(cart?.totalAmount ?? 0);

  if (selectedItems.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <p className="text-muted text-[14px]">{"Không có sản phẩm nào được chọn để thanh toán."}</p>
        <Link
          href="/cart"
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {"Quay lại giỏ hàng"}
        </Link>
      </div>
    );
  }

  function handleField(field: keyof ShippingForm) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setForm(prev => ({ ...prev, [field]: e.target.value }));
    };
  }

  async function handleApplyCoupon() {
    if (!couponInput.trim()) return;
    setLoadingCoupon(true);
    setCouponError('');
    try {
      const { data } = await clientAxios.post('/api/coupons/preview', {
        code: couponInput.trim(),
        orderAmount: totalAmount,
      });
      setCouponCode(data.data.code);
      setCouponDiscount(data.data.discountAmount);
    } catch (err) {
      const { general } = parseApiError(err, "Mã không hợp lệ hoặc không áp dụng được.");
      setCouponError(general);
      setCouponCode('');
      setCouponDiscount(0);
    } finally {
      setLoadingCoupon(false);
    }
  }

  function handleRemoveCoupon() {
    setCouponInput('');
    setCouponCode('');
    setCouponDiscount(0);
    setCouponError('');
  }

  function startResendTimer() {
    setResendSeconds(60);
    const iv = setInterval(() => {
      setResendSeconds(s => { if (s <= 1) { clearInterval(iv); return 0; } return s - 1; });
    }, 1000);
  }

  async function handleSendOtp() {
    setError('');
    if (!guestEmail.trim()) {
      setError("Vui lòng nhập email và mã xác nhận.");
      return;
    }
    setSendingOtp(true);
    try {
      await clientAxios.post('/api/orders/guest-otp', { email: guestEmail.trim() });
      setOtpSent(true);
      startResendTimer();
    } catch (err) {
      const { general } = parseApiError(err, "Không thể đặt hàng. Vui lòng thử lại.");
      setError(general);
    } finally {
      setSendingOtp(false);
    }
  }

  async function handleSubmit() {
    setError('');
    setFieldErrors({});

    const missingRequired =
      !form.recipientName.trim() ||
      !form.recipientPhone.trim() ||
      !form.shippingStreet.trim() ||
      !form.shippingDistrict.trim() ||
      !form.shippingProvince.trim();
    if (missingRequired) {
      setError("Vui lòng điền đầy đủ các trường bắt buộc.");
      return;
    }
    if (!isLoggedIn && (!guestEmail.trim() || !otpCode.trim())) {
      setError("Vui lòng nhập email và mã xác nhận.");
      return;
    }

    setSubmitting(true);
    try {
      const { data: orderRes } = await clientAxios.post(
        '/api/orders',
        {
          recipientName: form.recipientName.trim(),
          recipientPhone: form.recipientPhone.trim(),
          shippingStreet: form.shippingStreet.trim(),
          shippingWard: form.shippingWard.trim() || undefined,
          shippingDistrict: form.shippingDistrict.trim(),
          shippingProvince: form.shippingProvince.trim(),
          paymentMethod,
          couponCode: couponCode || undefined,
          note: form.note.trim() || undefined,
          ...(isLoggedIn ? {} : { guestEmail: guestEmail.trim(), otpCode: otpCode.trim() }),
        },
        { headers: guestHeaders() },
      );
      const order = orderRes.data;

      if (paymentMethod === 'VNPAY') {
        const { data: payRes } = await clientAxios.post('/api/payment', { orderId: order.id }, { headers: guestHeaders() });
        window.location.href = payRes.data;
      } else {
        router.push(`/orders/${order.id}`);
      }
    } catch (err) {
      const { general, fields } = parseApiError(err, "Không thể đặt hàng. Vui lòng thử lại.");
      setError(general);
      setFieldErrors(fields);
      setSubmitting(false);
    }
  }

  const subtotalOriginal = selectedItems.reduce((sum, i) => {
    const price = i.originalPrice ? Number(i.originalPrice) : Number(i.unitPrice);
    return sum + price * i.quantity;
  }, 0);
  const totalDiscount = subtotalOriginal - totalAmount;
  const finalTotal = Math.max(0, totalAmount - couponDiscount);

  function fieldCls(field: string) {
    return `w-full h-10 px-3 border rounded-sm text-[13px] text-ink placeholder:text-faint bg-white focus:outline-none transition-colors ${
      fieldErrors[field] ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'
    }`;
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-10">

      {/* Header */}
      <div className="flex items-center gap-3 mb-8">
        <Link href="/cart" className="text-muted hover:text-ink transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M11 6l-6 6 6 6"/>
          </svg>
        </Link>
        <h1 className="font-display font-black text-3xl uppercase tracking-tight">{"Thanh toán"}</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-10 items-start">

        {/* ── LEFT ── */}
        <div className="space-y-6">

          {/* Guest contact */}
          {!isLoggedIn && (
            <section className="border border-line rounded-sm overflow-hidden">
              <div className="px-5 py-3.5 border-b border-line bg-line-2">
                <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                  {"Thông tin liên hệ"}
                </h2>
              </div>
              <div className="p-5 space-y-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {"Email"} <span className="text-danger">*</span>
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="email"
                      value={guestEmail}
                      onChange={e => setGuestEmail(e.target.value)}
                      placeholder={"ban@email.com"}
                      className={fieldCls('guestEmail')}
                    />
                    <button
                      onClick={handleSendOtp}
                      disabled={sendingOtp || resendSeconds > 0 || !guestEmail.trim()}
                      className="shrink-0 px-4 h-10 text-[11px] font-bold uppercase tracking-wide border border-line rounded-sm hover:bg-paper transition-colors disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
                    >
                      {sendingOtp
                        ? "Đang gửi..."
                        : resendSeconds > 0
                          ? `Gửi lại sau ${resendSeconds}s`
                          : otpSent ? "Gửi lại mã" : "Gửi mã xác nhận"}
                    </button>
                  </div>
                  <FieldError msg={fieldErrors.guestEmail} />
                </div>

                {otpSent && (
                  <div>
                    <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                      {"Mã xác nhận (OTP)"}
                    </label>
                    <p className="text-[12px] text-muted mb-1.5">
                      {`Mã xác nhận đã được gửi đến ${guestEmail}`}
                    </p>
                    <input
                      value={otpCode}
                      onChange={e => setOtpCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      placeholder="000000"
                      className={`${fieldCls('otpCode')} font-body text-center tracking-[0.3em]`}
                    />
                    <FieldError msg={fieldErrors.otpCode} />
                  </div>
                )}
              </div>
            </section>
          )}

          {/* Saved Addresses */}
          {isLoggedIn && savedAddresses.length > 0 && (
            <section className="border border-line rounded-sm overflow-hidden mb-6">
              <div className="px-5 py-3.5 border-b border-line bg-line-2 flex items-center justify-between">
                <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                  {"Địa chỉ đã lưu"}
                </h2>
              </div>
              <div className="p-5">
                <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
                  {savedAddresses.map(addr => {
                    const isSelected = form.recipientName === addr.name &&
                                       form.recipientPhone === addr.phone &&
                                       form.shippingStreet === addr.street &&
                                       form.shippingDistrict === addr.district;
                    return (
                      <label
                        key={addr.id}
                        className={`flex items-start gap-3 p-3 border rounded-sm cursor-pointer transition-colors ${
                          isSelected ? 'border-ink bg-paper' : 'border-line hover:bg-paper/50'
                        }`}
                      >
                        <input
                          type="radio"
                          name="savedAddress"
                          checked={isSelected}
                          onChange={() => handleSelectAddress(addr)}
                          className="mt-0.5 accent-ink shrink-0"
                        />
                        <div className="flex-1 min-w-0">
                          <p className="text-[13px] font-semibold text-ink">
                            {addr.name} <span className="text-muted font-normal mx-1">·</span> {addr.phone}
                            {addr.defaultAddress && (
                              <span className="ml-2 px-1.5 py-0.5 text-[9px] bg-accent text-white uppercase rounded-sm tracking-wider">
                                {"Mặc định"}
                              </span>
                            )}
                          </p>
                          <p className="text-[12px] text-muted mt-0.5 truncate">
                            {[addr.street, addr.ward, addr.district, addr.province].filter(Boolean).join(', ')}
                          </p>
                        </div>
                      </label>
                    );
                  })}
                </div>
                
                <div className="flex items-center gap-3 mt-5 mb-1">
                  <div className="flex-1 h-px bg-line" />
                  <span className="text-[10px] uppercase tracking-widest text-faint">{"Hoặc nhập địa chỉ mới"}</span>
                  <div className="flex-1 h-px bg-line" />
                </div>
              </div>
            </section>
          )}

          {/* Shipping form */}
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                {"Thông tin giao hàng"}
              </h2>
            </div>
            <div className="p-5 space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {"Họ và tên"} <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    value={form.recipientName}
                    onChange={handleField('recipientName')}
                    placeholder={"Nguyễn Văn A"}
                    className={fieldCls('recipientName')}
                  />
                  <FieldError msg={fieldErrors.recipientName} />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {"Số điện thoại"} <span className="text-danger">*</span>
                  </label>
                  <div className="flex relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[13px] text-ink z-10 font-medium">
                      +84
                    </span>
                    <input
                      type="tel"
                      value={form.recipientPhone.startsWith('+84') ? form.recipientPhone.slice(3) : form.recipientPhone}
                      onChange={(e) => {
                        let val = e.target.value.replace(/\D/g, '');
                        if (val.startsWith('84') && val.length >= 10) val = val.substring(2);
                        if (val.startsWith('0')) val = val.substring(1);
                        setForm(prev => ({ ...prev, recipientPhone: val ? `+84${val}` : '' }));
                      }}
                      placeholder="901234567"
                      className={`${fieldCls('recipientPhone')} pl-10`}
                    />
                  </div>
                  <FieldError msg={fieldErrors.recipientPhone} />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                  {"Địa chỉ (số nhà, đường)"} <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  value={form.shippingStreet}
                  onChange={handleField('shippingStreet')}
                  placeholder={"123 Lê Lợi"}
                  className={fieldCls('shippingStreet')}
                />
                <FieldError msg={fieldErrors.shippingStreet} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {"Phường/Xã"}
                  </label>
                  <input
                    type="text"
                    value={form.shippingWard}
                    onChange={handleField('shippingWard')}
                    className={fieldCls('shippingWard')}
                  />
                  <FieldError msg={fieldErrors.shippingWard} />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {"Quận/Huyện"} <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    value={form.shippingDistrict}
                    onChange={handleField('shippingDistrict')}
                    className={fieldCls('shippingDistrict')}
                  />
                  <FieldError msg={fieldErrors.shippingDistrict} />
                </div>
              </div>

              <div>
                <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                  {"Tỉnh/Thành phố"} <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  value={form.shippingProvince}
                  onChange={handleField('shippingProvince')}
                  className={fieldCls('shippingProvince')}
                />
                <FieldError msg={fieldErrors.shippingProvince} />
              </div>

              <div>
                <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                  {"Ghi chú"}
                </label>
                <textarea
                  value={form.note}
                  onChange={handleField('note')}
                  placeholder={"Ghi chú cho đơn hàng (không bắt buộc)"}
                  rows={3}
                  className="w-full px-3 py-2.5 border border-line rounded-sm text-[13px] text-ink placeholder:text-faint bg-white focus:outline-none focus:border-ink transition-colors resize-none"
                />
              </div>
            </div>
          </section>

          {/* Payment method */}
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                {"Phương thức thanh toán"}
              </h2>
            </div>
            <div className="p-5 space-y-3">
              {([
                { value: 'VNPAY' as const, title: "Thanh toán qua VNPay", desc: "Chuyển đến cổng VNPay để hoàn tất giao dịch." },
                { value: 'COD' as const,   title: "Thanh toán khi nhận hàng (COD)",   desc: "Thanh toán bằng tiền mặt khi nhận hàng." },
              ]).map(opt => (
                <label
                  key={opt.value}
                  className={`flex items-start gap-3 p-3.5 border rounded-sm cursor-pointer transition-colors ${
                    paymentMethod === opt.value ? 'border-ink bg-paper' : 'border-line hover:bg-paper/50'
                  }`}
                >
                  <input
                    type="radio"
                    name="paymentMethod"
                    checked={paymentMethod === opt.value}
                    onChange={() => setPaymentMethod(opt.value)}
                    className="mt-0.5 accent-ink"
                  />
                  <span>
                    <span className="block text-[13px] font-semibold text-ink">{opt.title}</span>
                    <span className="block text-[12px] text-muted mt-0.5">{opt.desc}</span>
                  </span>
                </label>
              ))}
            </div>
          </section>

          {/* Items list */}
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                {`Sản phẩm (${selectedItems.length})`}
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
                {"Tóm tắt đơn hàng"}
              </h2>
            </div>
            <div className="px-5 pt-4 pb-3 space-y-2.5">
              <div className="flex justify-between items-baseline text-[13px]">
                <span className="text-muted">{"Tạm tính"}:</span>
                <span className="tabular-nums text-ink">{formatPrice(subtotalOriginal)}</span>
              </div>
              {totalDiscount > 0 && (
                <div className="flex justify-between items-baseline text-[13px]">
                  <span className="text-muted">{"Giảm giá"}:</span>
                  <span className="tabular-nums text-ok">-{formatPrice(totalDiscount)}</span>
                </div>
              )}
              {couponCode && (
                <div className="flex justify-between items-baseline text-[13px]">
                  <span className="text-muted">{"Mã giảm giá"} ({couponCode}):</span>
                  <span className="tabular-nums text-ok">-{formatPrice(couponDiscount)}</span>
                </div>
              )}
            </div>
            
            {/* Coupon Input */}
            <div className="px-5 pb-4 border-b border-line space-y-2">
              <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted">
                {"Mã giảm giá"}
              </label>
              {!couponCode ? (
                <div>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={couponInput}
                      onChange={e => setCouponInput(e.target.value.toUpperCase())}
                      placeholder="Nhập mã..."
                      className="w-full h-9 px-3 border border-line rounded-sm text-[12px] text-ink placeholder:text-faint bg-white focus:outline-none focus:border-ink uppercase transition-colors"
                    />
                    <button
                      onClick={handleApplyCoupon}
                      disabled={loadingCoupon || !couponInput.trim()}
                      className="shrink-0 px-4 h-9 text-[11px] font-bold uppercase tracking-wide border border-line rounded-sm hover:bg-paper transition-colors disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
                    >
                      {loadingCoupon ? "..." : "Áp dụng"}
                    </button>
                  </div>
                  {couponError && <p className="text-[11px] text-danger mt-1.5">{couponError}</p>}
                </div>
              ) : (
                <div className="flex items-center justify-between bg-ok/10 border border-ok/20 rounded-sm px-3 py-2">
                  <div className="flex items-center gap-2 text-ok">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M20 6L9 17l-5-5"/>
                    </svg>
                    <span className="text-[12px] font-bold uppercase tracking-wide">{couponCode}</span>
                  </div>
                  <button onClick={handleRemoveCoupon} className="text-[11px] text-danger hover:underline">
                    Xóa
                  </button>
                </div>
              )}
            </div>

            <div className="px-5 py-4 flex justify-between items-center">
              <span className="text-[12px] font-bold uppercase tracking-widest text-ink">{"Tổng cộng"}:</span>
              <span className="text-[20px] font-bold tabular-nums text-ink">{formatPrice(finalTotal)}</span>
            </div>
          </div>
          <p className="text-[11px] text-muted leading-relaxed px-1">
            {"Phí giao hàng sẽ được cộng vào tổng đơn hàng khi đặt hàng thành công."}
          </p>

          {error && (
            <p className="text-[12px] text-danger bg-danger/5 border border-danger/20 rounded-sm px-3 py-2.5">
              {error}
            </p>
          )}

          <button
            onClick={handleSubmit}
            disabled={submitting}
            className="w-full bg-ink text-white text-[12px] font-bold uppercase tracking-widest py-3.5 rounded-sm hover:bg-accent transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {submitting ? (
              <>
                <svg className="animate-spin w-4 h-4" viewBox="0 0 24 24" fill="none">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                </svg>
                {"Đang xử lý..."}
              </>
            ) : (
              paymentMethod === 'VNPAY' ? "Thanh toán qua VNPay" : "Đặt hàng"
            )}
          </button>

          {paymentMethod === 'VNPAY' && (
            <p className="text-[11px] text-muted text-center leading-relaxed">
              {"Bạn sẽ được chuyển đến cổng thanh toán VNPay để hoàn tất giao dịch."}
            </p>
          )}
        </aside>
      </div>
    </div>
  );
}
