'use client';

import { useState, useEffect } from 'react';
import { useTranslations } from 'next-intl';
import { Link, useRouter } from '@/i18n/routing';
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
  const t = useTranslations('checkout');
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
        <p className="text-[12px] text-muted mt-0.5">{item.colorway} · {t('sizeLabel')} {item.size}</p>
        <p className="text-[12px] text-muted mt-0.5">{t('quantity')}: {item.quantity}</p>
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
  const t = useTranslations('checkout');
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
        <p className="text-muted text-[14px]">{t('emptyCart')}</p>
        <Link
          href="/cart"
          className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {t('backToCart')}
        </Link>
      </div>
    );
  }

  function handleField(field: keyof ShippingForm) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      setForm(prev => ({ ...prev, [field]: e.target.value }));
    };
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
      setError(t('errorRequiredOtp'));
      return;
    }
    setSendingOtp(true);
    try {
      await clientAxios.post('/api/orders/guest-otp', { email: guestEmail.trim() });
      setOtpSent(true);
      startResendTimer();
    } catch (err) {
      const { general } = parseApiError(err, t('errorGeneric'));
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
      setError(t('errorRequiredFields'));
      return;
    }
    if (!isLoggedIn && (!guestEmail.trim() || !otpCode.trim())) {
      setError(t('errorRequiredOtp'));
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
          note: form.note.trim() || undefined,
          ...(isLoggedIn ? {} : { guestEmail: guestEmail.trim(), otpCode: otpCode.trim() }),
        },
        { headers: guestHeaders() },
      );
      const order = orderRes.data;

      if (paymentMethod === 'VNPAY') {
        const { data: payRes } = await clientAxios.post('/api/payment', { orderId: order.id });
        window.location.href = payRes.data;
      } else {
        router.push(`/orders/${order.id}`);
      }
    } catch (err) {
      const { general, fields } = parseApiError(err, t('errorGeneric'));
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
        <h1 className="font-display font-black text-3xl uppercase tracking-tight">{t('title')}</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-10 items-start">

        {/* ── LEFT ── */}
        <div className="space-y-6">

          {/* Guest contact */}
          {!isLoggedIn && (
            <section className="border border-line rounded-sm overflow-hidden">
              <div className="px-5 py-3.5 border-b border-line bg-line-2">
                <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                  {t('guestSectionTitle')}
                </h2>
              </div>
              <div className="p-5 space-y-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {t('guestEmail')} <span className="text-danger">*</span>
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="email"
                      value={guestEmail}
                      onChange={e => setGuestEmail(e.target.value)}
                      placeholder={t('guestEmailPlaceholder')}
                      className={fieldCls('guestEmail')}
                    />
                    <button
                      onClick={handleSendOtp}
                      disabled={sendingOtp || resendSeconds > 0 || !guestEmail.trim()}
                      className="shrink-0 px-4 h-10 text-[11px] font-bold uppercase tracking-wide border border-line rounded-sm hover:bg-paper transition-colors disabled:opacity-40 disabled:cursor-not-allowed whitespace-nowrap"
                    >
                      {sendingOtp
                        ? t('sendingOtp')
                        : resendSeconds > 0
                          ? t('resendIn', { n: resendSeconds })
                          : otpSent ? t('resendOtp') : t('sendOtp')}
                    </button>
                  </div>
                  <FieldError msg={fieldErrors.guestEmail} />
                </div>

                {otpSent && (
                  <div>
                    <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                      {t('otpCode')}
                    </label>
                    <p className="text-[12px] text-muted mb-1.5">
                      {t('otpSentTo', { email: guestEmail })}
                    </p>
                    <input
                      value={otpCode}
                      onChange={e => setOtpCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      placeholder="000000"
                      className={`${fieldCls('otpCode')} font-mono text-center tracking-[0.3em]`}
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
                  {t('savedAddressesTitle')}
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
                                {t('defaultBadge')}
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
                  <span className="text-[10px] uppercase tracking-widest text-faint">{t('orNewAddress')}</span>
                  <div className="flex-1 h-px bg-line" />
                </div>
              </div>
            </section>
          )}

          {/* Shipping form */}
          <section className="border border-line rounded-sm overflow-hidden">
            <div className="px-5 py-3.5 border-b border-line bg-line-2">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-ink">
                {t('shippingSectionTitle')}
              </h2>
            </div>
            <div className="p-5 space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {t('recipientName')} <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    value={form.recipientName}
                    onChange={handleField('recipientName')}
                    placeholder={t('recipientNamePlaceholder')}
                    className={fieldCls('recipientName')}
                  />
                  <FieldError msg={fieldErrors.recipientName} />
                </div>
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {t('recipientPhone')} <span className="text-danger">*</span>
                  </label>
                  <div className="flex relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[13px] text-ink z-10 font-medium">
                      +84
                    </span>
                    <input
                      type="tel"
                      value={form.recipientPhone.startsWith('+84') ? form.recipientPhone.slice(3) : form.recipientPhone}
                      onChange={(e) => {
                        const val = e.target.value.replace(/\D/g, '');
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
                  {t('shippingStreet')} <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  value={form.shippingStreet}
                  onChange={handleField('shippingStreet')}
                  placeholder={t('shippingStreetPlaceholder')}
                  className={fieldCls('shippingStreet')}
                />
                <FieldError msg={fieldErrors.shippingStreet} />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[11px] font-semibold uppercase tracking-wide text-muted mb-1.5">
                    {t('shippingWard')}
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
                    {t('shippingDistrict')} <span className="text-danger">*</span>
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
                  {t('shippingProvince')} <span className="text-danger">*</span>
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
                  {t('note')}
                </label>
                <textarea
                  value={form.note}
                  onChange={handleField('note')}
                  placeholder={t('notePlaceholder')}
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
                {t('paymentMethodTitle')}
              </h2>
            </div>
            <div className="p-5 space-y-3">
              {([
                { value: 'VNPAY' as const, title: t('paymentMethodVnpay'), desc: t('paymentMethodVnpayDesc') },
                { value: 'COD' as const,   title: t('paymentMethodCod'),   desc: t('paymentMethodCodDesc') },
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
                {t('itemsSectionTitle', { count: selectedItems.length })}
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
                {t('summaryTitle')}
              </h2>
            </div>
            <div className="px-5 pt-4 pb-3 space-y-2.5">
              <div className="flex justify-between items-baseline text-[13px]">
                <span className="text-muted">{t('subtotal')}:</span>
                <span className="tabular-nums text-ink">{formatPrice(subtotalOriginal)}</span>
              </div>
              {totalDiscount > 0 && (
                <div className="flex justify-between items-baseline text-[13px]">
                  <span className="text-muted">{t('discount')}:</span>
                  <span className="tabular-nums text-ok">-{formatPrice(totalDiscount)}</span>
                </div>
              )}
            </div>
            <div className="border-t border-line px-5 py-4 flex justify-between items-center">
              <span className="text-[12px] font-bold uppercase tracking-widest text-ink">{t('total')}:</span>
              <span className="text-[20px] font-bold tabular-nums text-ink">{formatPrice(totalAmount)}</span>
            </div>
          </div>
          <p className="text-[11px] text-muted leading-relaxed px-1">
            {t('shippingFeeNote')}
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
                {t('processing')}
              </>
            ) : (
              paymentMethod === 'VNPAY' ? t('submitVnpay') : t('submitCod')
            )}
          </button>

          {paymentMethod === 'VNPAY' && (
            <p className="text-[11px] text-muted text-center leading-relaxed">
              {t('hintVnpay')}
            </p>
          )}
        </aside>
      </div>
    </div>
  );
}
