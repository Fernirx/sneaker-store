'use client';

import { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useCart, type CartItemData } from '@/contexts/CartContext';
import { productUrl } from '@/lib/cloudinaryUrl';
import { parseApiError } from '@/lib/parseApiError';
import { guestHeaders } from '@/lib/guestToken';
import { formatPrice } from '../../products/_components/types';
import clientAxios from '@/lib/axios/clientAxios';
import AddressSelector from '@/components/AddressSelector';
import ImageUnavailable from '@/components/ImageUnavailable';

const WEEKDAY_LABELS = ['Chủ nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];

function formatExpectedDeliveryHeading(iso: string): string {
  const d = new Date(iso);
  return `${WEEKDAY_LABELS[d.getDay()]}, ngày ${d.getDate()} tháng ${d.getMonth() + 1}`;
}

type ShippingForm = {
  recipientName: string;
  recipientPhone: string;
  shippingStreet: string;
  shippingWard: string;
  shippingWardCode: number | null;
  shippingDistrict: string;
  shippingDistrictCode: number | null;
  shippingProvince: string;
  shippingProvinceCode: number | null;
  note: string;
};

interface Address {
  id: number;
  name: string;
  phone: string;
  street: string;
  ward?: string;
  wardCode?: number;
  district?: string;
  districtCode?: number;
  province: string;
  provinceCode?: number;
  postalCode?: string;
  defaultAddress: boolean;
}

type PaymentMethod = 'VNPAY' | 'COD';

const EMPTY_FORM: ShippingForm = {
  recipientName: '',
  recipientPhone: '',
  shippingStreet: '',
  shippingWard: '',
  shippingWardCode: null,
  shippingDistrict: '',
  shippingDistrictCode: null,
  shippingProvince: '',
  shippingProvinceCode: null,
  note: '',
};

function FieldError({ msg }: { msg?: string }) {
  return msg ? <p className="text-[11px] text-danger mt-1">{msg}</p> : null;
}

// ── Floating-label field (label sits on the border once focused/filled) ───────

const FLOATING_LABEL_FLOATED =
  "peer-focus:top-0 peer-focus:text-[10px] peer-focus:text-muted peer-focus:font-semibold peer-focus:uppercase peer-focus:tracking-wide " +
  "peer-[:not(:placeholder-shown)]:top-0 peer-[:not(:placeholder-shown)]:text-[10px] peer-[:not(:placeholder-shown)]:text-muted peer-[:not(:placeholder-shown)]:font-semibold peer-[:not(:placeholder-shown)]:uppercase peer-[:not(:placeholder-shown)]:tracking-wide";

interface FloatingInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  id: string;
  label: string;
  required?: boolean;
  prefix?: string;
  error?: string;
}

function FloatingInput({ id, label, required = true, prefix, error, className = '', ...inputProps }: FloatingInputProps) {
  return (
    <div className="relative">
      {prefix && (
        <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[13px] text-ink z-10 font-medium">
          {prefix}
        </span>
      )}
      <input
        id={id}
        placeholder=" "
        {...inputProps}
        className={`peer w-full h-12 px-3 border rounded-sm text-[13px] text-ink placeholder-transparent bg-white focus:outline-none transition-colors ${
          prefix ? 'pl-10' : ''
        } ${error ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'} ${className}`}
      />
      <label
        htmlFor={id}
        className={`absolute ${prefix ? 'left-10' : 'left-3'} top-1/2 -translate-y-1/2 text-[13px] text-faint pointer-events-none transition-all duration-150 bg-white px-1 ${FLOATING_LABEL_FLOATED}`}
      >
        {label} {required && <span className="text-danger">*</span>}
      </label>
      <FieldError msg={error} />
    </div>
  );
}

function FloatingTextarea({ id, label, ...textareaProps }: { id: string; label: string } & React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <div className="relative">
      <textarea
        id={id}
        placeholder=" "
        {...textareaProps}
        className="peer w-full px-3 py-3 border border-line rounded-sm text-[13px] text-ink placeholder-transparent bg-white focus:outline-none focus:border-ink transition-colors resize-none"
      />
      <label
        htmlFor={id}
        className={`absolute left-3 top-3 text-[13px] text-faint pointer-events-none transition-all duration-150 bg-white px-1 ${FLOATING_LABEL_FLOATED}`}
      >
        {label}
      </label>
    </div>
  );
}

// ── Read-only item row ────────────────────────────────────────────────────────

function CheckoutItem({ item }: { item: CartItemData }) {
  const unit = Number(item.unitPrice);
  const orig = item.originalPrice ? Number(item.originalPrice) : null;
  const hasDiscount = orig != null && orig > unit;
  const pct  = hasDiscount ? Math.round(((orig! - unit) / orig!) * 100) : 0;

  return (
    <div className="flex gap-4 py-4 border-b border-line last:border-b-0">
      <div className="shrink-0 w-[72px] h-[72px] bg-transparent overflow-hidden">
        {item.primaryImagePublicId ? (
          <img
            src={productUrl(item.primaryImagePublicId, 144, 144)}
            alt={item.productName}
            className="w-full h-full object-contain"
          />
        ) : (
          <ImageUnavailable className="w-6 h-6" />
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
        {hasDiscount && (
          <span className="text-[10px] font-bold text-white bg-accent px-1 py-0.5 rounded-sm leading-none">
            -{pct}%
          </span>
        )}
        <span className={`text-[14px] font-bold tabular-nums ${hasDiscount ? 'text-accent' : 'text-ink'}`}>
          {formatPrice(unit * item.quantity)}
        </span>
        {hasDiscount && (
          <span className="text-[11px] text-faint line-through tabular-nums">
            {formatPrice(orig! * item.quantity)}
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

export default function CheckoutClient({ isLoggedIn, userEmail }: { isLoggedIn: boolean; userEmail?: string }) {
  const router = useRouter();
  const { cart, loading, clearCart } = useCart();

  // 1 UUID/phiên checkout, gửi lại y hệt ở mọi lần submit (kể cả bấm lại sau lỗi mạng) để BE nhận diện
  // và trả lại đúng đơn cũ thay vì tạo trùng khi double-click/replay request.
  const idempotencyKeyRef = useRef<string | null>(null);
  if (idempotencyKeyRef.current === null) {
    idempotencyKeyRef.current = crypto.randomUUID();
  }

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
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [savedAddrOpen, setSavedAddrOpen] = useState(false);
  const savedAddrRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (savedAddrRef.current && !savedAddrRef.current.contains(e.target as Node)) {
        setSavedAddrOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const [couponCode, setCouponCode] = useState('');
  const [couponInput, setCouponInput] = useState('');
  const [couponDiscount, setCouponDiscount] = useState<number>(0);
  const [couponError, setCouponError] = useState('');
  const [loadingCoupon, setLoadingCoupon] = useState(false);

  const [shippingFee, setShippingFee] = useState<number | null>(null);
  const [expectedDeliveryTime, setExpectedDeliveryTime] = useState<string | null>(null);
  const [loadingShippingFee, setLoadingShippingFee] = useState(false);

  const [freeShipThreshold, setFreeShipThreshold] = useState<number | null>(null);
  const [summaryOpen, setSummaryOpen] = useState(false);

  const selectedItems = (cart?.items ?? []).filter(i => i.selected && !i.outOfStock);
  const totalAmount   = Number(cart?.totalAmount ?? 0);

  useEffect(() => {
    clientAxios.get('/api/settings/free-ship-threshold')
      .then(({ data }) => setFreeShipThreshold(Number(data.data)))
      .catch(() => {});
  }, []);

  const isFreeShip = freeShipThreshold !== null && totalAmount >= freeShipThreshold;

  useEffect(() => {
    if (!form.shippingStreet.trim() || !form.shippingWard || !form.shippingDistrict || !form.shippingProvince
        || selectedItems.length === 0) {
      setShippingFee(null);
      setExpectedDeliveryTime(null);
      return;
    }

    let cancelled = false;
    setLoadingShippingFee(true);

    const itemsPayload = selectedItems.map(item => ({
      variantId: item.variantId,
      quantity: item.quantity,
    }));

    clientAxios.post('/api/shipping/preview', {
      shippingStreet: form.shippingStreet.trim(),
      shippingWard: form.shippingWard,
      shippingDistrict: form.shippingDistrict,
      shippingProvince: form.shippingProvince,
      items: itemsPayload,
    })
      .then(({ data }) => {
        if (!cancelled && data?.data) {
          setShippingFee(isFreeShip ? 0 : Number(data.data.fee ?? 0));
          setExpectedDeliveryTime(data.data.expectedDeliveryTime ?? null);
        }
      })
      .catch((err) => {
        console.error("Error fetching shipping fee:", err);
        if (!cancelled) {
          setShippingFee(0);
          setExpectedDeliveryTime(null);
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoadingShippingFee(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [
    form.shippingWard,
    form.shippingDistrict,
    form.shippingProvince,
    form.shippingStreet,
    JSON.stringify(selectedItems.map(i => ({ v: i.variantId, q: i.quantity }))),
    isFreeShip
  ]);

  useEffect(() => {
    if (!isLoggedIn) return;
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
            shippingWardCode: def.wardCode ?? null,
            shippingDistrict: def.district ?? '',
            shippingDistrictCode: def.districtCode ?? null,
            shippingProvince: def.province,
            shippingProvinceCode: def.provinceCode ?? null,
          }));
          setSelectedAddressId(def.id);
        }
      })
      .catch(() => {});
  }, [isLoggedIn]);

  function handleSelectAddress(addr: Address) {
    setForm(prev => ({
      ...prev,
      recipientName: addr.name,
      recipientPhone: addr.phone,
      shippingStreet: addr.street,
      shippingWard: addr.ward ?? '',
      shippingWardCode: addr.wardCode ?? null,
      shippingDistrict: addr.district ?? '',
      shippingDistrictCode: addr.districtCode ?? null,
      shippingProvince: addr.province,
      shippingProvinceCode: addr.provinceCode ?? null,
    }));
    setSelectedAddressId(addr.id);
    setFieldErrors({});
  }

  if (loading) return <CheckoutSkeleton />;

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
        // Có email/phone thì BE check luôn giới hạn dùng theo khách hàng ở bước preview - tránh hiện "áp
        // dụng thành công" rồi bị từ chối lúc bấm Đặt hàng thật. Có thể chưa có nếu khách guest chưa nhập.
        email: isLoggedIn ? userEmail : (guestEmail.trim() || undefined),
        phone: form.recipientPhone.trim() || undefined,
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
      !form.shippingWard.trim() ||
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
          shippingWard: form.shippingWard.trim(),
          shippingDistrict: form.shippingDistrict.trim(),
          shippingProvince: form.shippingProvince.trim(),
          paymentMethod,
          couponCode: couponCode || undefined,
          note: form.note.trim() || undefined,
          ...(isLoggedIn ? {} : { guestEmail: guestEmail.trim(), otpCode: otpCode.trim() }),
        },
        { headers: { ...guestHeaders(), 'Idempotency-Key': idempotencyKeyRef.current! } },
      );
      const order = orderRes.data;

      await clearCart();

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
  const finalTotal = Math.max(0, totalAmount - couponDiscount + (shippingFee ?? 0));

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
            <section>
              <h2 className="text-[16px] font-bold text-ink mb-3">
                {"Thông tin liên hệ"}
              </h2>
              <div className="space-y-4">
                <div className="flex gap-2 items-start">
                  <div className="flex-1">
                    <FloatingInput
                      id="guestEmail"
                      label="Email"
                      type="email"
                      value={guestEmail}
                      onChange={e => setGuestEmail(e.target.value)}
                      error={fieldErrors.guestEmail}
                    />
                  </div>
                  <button
                    onClick={handleSendOtp}
                    disabled={sendingOtp || resendSeconds > 0 || !guestEmail.trim()}
                    className="shrink-0 px-4 h-12 text-[11px] font-bold uppercase tracking-wide border border-line rounded-sm hover:bg-paper transition-colors disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
                  >
                    {sendingOtp
                      ? "Đang gửi..."
                      : resendSeconds > 0
                        ? `Gửi lại sau ${resendSeconds}s`
                        : otpSent ? "Gửi lại mã" : "Gửi mã xác nhận"}
                  </button>
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

          {/* Thông tin giao hàng */}
          <section>
            <h2 className="text-[16px] font-bold text-ink mb-3">
              {"Thông tin giao hàng"}
            </h2>

            {isLoggedIn && savedAddresses.length > 0 && (
              <div className="relative mb-4" ref={savedAddrRef}>
                <div
                  onClick={() => setSavedAddrOpen(o => !o)}
                  className="relative w-full min-h-12 pl-3 pr-9 py-3 border border-line rounded-sm bg-white cursor-pointer flex items-center transition-colors hover:border-ink"
                >
                  <label className="absolute left-3 -top-0 -translate-y-1/2 text-[10px] text-muted font-semibold uppercase tracking-wide bg-white px-1 pointer-events-none">
                    {"Địa chỉ đã lưu trữ"}
                  </label>
                  {selectedAddressId !== null ? (() => {
                    const addr = savedAddresses.find(a => a.id === selectedAddressId);
                    return addr ? (
                      <div className="text-[13px] pr-2 flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-2">
                          <p className="text-ink truncate">
                            {addr.name} <span className="text-muted font-normal">({addr.phone})</span>
                          </p>
                          {addr.defaultAddress && (
                            <span className="shrink-0 text-accent text-[11px] font-medium">{"Mặc định"}</span>
                          )}
                        </div>
                        <p className="text-muted mt-0.5">
                          {[addr.street, addr.ward, addr.district, addr.province].filter(Boolean).join(', ')}
                        </p>
                      </div>
                    ) : null;
                  })() : null}
                  <svg
                    className={`absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted transition-transform ${savedAddrOpen ? 'rotate-180' : ''}`}
                    viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
                  >
                    <path d="M6 9l6 6 6-6"/>
                  </svg>
                </div>

                {savedAddrOpen && (
                  <div className="absolute z-50 mt-1 w-full bg-white border border-line rounded-sm shadow-lg max-h-72 overflow-y-auto">
                    {savedAddresses.map(addr => {
                      const isSelected = addr.id === selectedAddressId;
                      return (
                        <div
                          key={addr.id}
                          onClick={() => {
                            handleSelectAddress(addr);
                            setSavedAddrOpen(false);
                          }}
                          className={`px-3 py-2.5 cursor-pointer border-b border-line last:border-b-0 transition-colors ${
                            isSelected ? 'bg-paper' : 'hover:bg-paper/50'
                          }`}
                        >
                          <p className="text-[13px] font-semibold text-ink flex items-center gap-2">
                            {addr.name} <span className="text-muted font-normal">· {addr.phone}</span>
                            {addr.defaultAddress && (
                              <span className="px-1.5 py-0.5 text-[9px] bg-accent text-white uppercase rounded-sm tracking-wider">
                                {"Mặc định"}
                              </span>
                            )}
                          </p>
                          <p className="text-[12px] text-muted mt-0.5 truncate">
                            {[addr.street, addr.ward, addr.district, addr.province].filter(Boolean).join(', ')}
                          </p>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            )}

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-4">
              <FloatingInput
                id="recipientName"
                label="Họ và tên"
                type="text"
                value={form.recipientName}
                onChange={handleField('recipientName')}
                error={fieldErrors.recipientName}
              />
              <FloatingInput
                id="recipientPhone"
                label="Số điện thoại"
                type="tel"
                prefix="+84"
                value={form.recipientPhone.startsWith('+84') ? form.recipientPhone.slice(3) : form.recipientPhone}
                onChange={(e) => {
                  let val = e.target.value.replace(/\D/g, '');
                  if (val.startsWith('84') && val.length >= 10) val = val.substring(2);
                  if (val.startsWith('0')) val = val.substring(1);
                  setForm(prev => ({ ...prev, recipientPhone: val ? `+84${val}` : '' }));
                }}
                error={fieldErrors.recipientPhone}
              />
            </div>

            <div className="space-y-4">
              <FloatingInput
                id="shippingStreet"
                label="Địa chỉ (số nhà, đường)"
                type="text"
                value={form.shippingStreet}
                onChange={handleField('shippingStreet')}
                error={fieldErrors.shippingStreet}
              />

              <AddressSelector
                province={form.shippingProvince}
                provinceCode={form.shippingProvinceCode}
                district={form.shippingDistrict}
                districtCode={form.shippingDistrictCode}
                ward={form.shippingWard}
                wardCode={form.shippingWardCode}
                onChange={({ province, provinceCode, district, districtCode, ward, wardCode }) => {
                  setForm(f => ({
                    ...f,
                    shippingProvince: province, shippingProvinceCode: provinceCode,
                    shippingDistrict: district, shippingDistrictCode: districtCode,
                    shippingWard: ward, shippingWardCode: wardCode,
                  }));
                  setFieldErrors(e => {
                    const next = { ...e };
                    delete next.shippingProvince;
                    delete next.shippingDistrict;
                    delete next.shippingWard;
                    return next;
                  });
                }}
                provinceError={fieldErrors.shippingProvince}
                districtError={fieldErrors.shippingDistrict}
                wardError={fieldErrors.shippingWard}
              />
            </div>

            <div className="mt-4">
              <FloatingTextarea
                id="note"
                label="Ghi chú"
                value={form.note}
                onChange={handleField('note')}
                rows={3}
              />
            </div>
          </section>

          {/* Payment method */}
          <section>
            <h2 className="text-[16px] font-bold text-ink mb-3">
              {"Phương thức thanh toán"}
            </h2>
            <div className="space-y-3">
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
        </div>

        {/* ── RIGHT ── */}
        <aside className="lg:sticky lg:top-[84px] space-y-3">

          {/* Summary */}
          <div className="rounded-sm overflow-hidden bg-line-2">
            <button
              type="button"
              onClick={() => setSummaryOpen(o => !o)}
              className="w-full flex items-center justify-between gap-3 px-5 py-3.5 text-left lg:pointer-events-none"
            >
              <h2 className="text-[16px] font-bold text-ink shrink-0">
                {"Tóm tắt đơn hàng"}
              </h2>
              <div className="flex items-center gap-2 lg:hidden">
                <span className="text-[12px] font-semibold tabular-nums text-ink">
                  {formatPrice(finalTotal)} ({selectedItems.length} sp)
                </span>
                <svg
                  width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"
                  className={`shrink-0 transition-transform ${summaryOpen ? 'rotate-180' : ''}`}
                >
                  <path d="M6 9l6 6 6-6"/>
                </svg>
              </div>
            </button>

            <div className="mx-5 border-b border-line" />

            <div className={`${summaryOpen ? 'flex' : 'hidden'} lg:flex flex-col`}>
              {/* Giá & tổng — trên desktop hiện trước sản phẩm, trên mobile hiện sau */}
              <div className="order-2 lg:order-1">
                <div className="mx-5 border-t border-line lg:hidden" />
                <div className="px-5 pt-3 pb-3 space-y-2.5">
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
                  <div className="flex justify-between items-baseline text-[13px]">
                    <span className="text-muted">{"Vận chuyển (Tạm tính)"}:</span>
                    <span className="tabular-nums text-ink">
                      {isFreeShip ? (
                        "-"
                      ) : loadingShippingFee ? (
                        <span className="text-muted animate-pulse">{"Đang tính..."}</span>
                      ) : shippingFee !== null ? (
                        formatPrice(shippingFee)
                      ) : (
                        <span className="text-faint">{"Chưa chọn địa chỉ"}</span>
                      )}
                    </span>
                  </div>

                  {freeShipThreshold !== null && freeShipThreshold > 0 && (
                    totalAmount >= freeShipThreshold ? (
                      <p className="text-[11px] text-ok font-medium">{"Đơn hàng của bạn được miễn phí vận chuyển!"}</p>
                    ) : (
                      <div className="pt-0.5">
                        <p className="text-[11px] text-muted">
                          {"Mua thêm "}
                          <span className="font-semibold text-ink">{formatPrice(freeShipThreshold - totalAmount)}</span>
                          {" để được miễn phí vận chuyển!"}
                        </p>
                        <div className="mt-1.5 h-1 w-full bg-line rounded-full overflow-hidden">
                          <div
                            className="h-full bg-ok rounded-full transition-all"
                            style={{ width: `${Math.min(100, (totalAmount / freeShipThreshold) * 100)}%` }}
                          />
                        </div>
                      </div>
                    )
                  )}

                  {/* Coupon Input */}
                  <div className="pt-2 space-y-2">
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
                </div>

                <div className="mx-5 border-t border-line" />

                <div className="px-5 pt-3 flex justify-between items-center">
                  <span className="text-[13px] font-bold text-ink">{"Tổng cộng"}:</span>
                  <span className="text-[20px] font-bold tabular-nums text-ink">{formatPrice(finalTotal)}</span>
                </div>

                <div className="mx-5 border-b border-line hidden lg:block mt-3" />
              </div>

              {/* Sản phẩm — trên mobile hiện trước giá, trên desktop hiện sau */}
              <div className="order-1 lg:order-2 px-5 pt-4 pb-1">
                <h3 className="text-[13px] font-semibold text-ink mb-2">
                  {expectedDeliveryTime
                    ? `Hàng sẽ đến vào ${formatExpectedDeliveryHeading(expectedDeliveryTime)}`
                    : `${selectedItems.length} sản phẩm`}
                </h3>
                <div className="max-h-72 overflow-y-auto">
                  {selectedItems.map(item => (
                    <CheckoutItem key={item.id} item={item} />
                  ))}
                </div>
              </div>
            </div>
          </div>
          {shippingFee === null && (
            <p className="text-[11px] text-muted leading-relaxed px-1">
              {"Phí giao hàng sẽ được hiển thị sau khi bạn chọn địa chỉ giao hàng."}
            </p>
          )}
        </aside>
      </div>
    </div>
  );
}
