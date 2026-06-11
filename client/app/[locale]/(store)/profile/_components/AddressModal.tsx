'use client';

import { useState, useEffect } from 'react';
import { useTranslations } from 'next-intl';

export interface AddressForm {
  name: string;
  phone: string;
  street: string;
  ward: string;
  district: string;
  province: string;
  postalCode: string;
  defaultAddress: boolean;
}

const EMPTY: AddressForm = {
  name: '', phone: '', street: '', ward: '',
  district: '', province: '', postalCode: '', defaultAddress: false,
};

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{label}</label>
      {children}
    </div>
  );
}

function inputCls(err?: string) {
  return `w-full border rounded px-3 py-2.5 text-sm focus:outline-none transition-colors ${
    err ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'
  }`;
}

export default function AddressModal({
  initial,
  onClose,
  onSave,
}: {
  initial?: Partial<AddressForm>;
  onClose: () => void;
  onSave: (form: AddressForm) => Promise<void>;
}) {
  const t = useTranslations('profile');
  const [form, setForm] = useState<AddressForm>({ ...EMPTY, ...initial });
  const [errors, setErrors] = useState<Partial<AddressForm>>({});
  const [pending, setPending] = useState(false);
  const [generalError, setGeneralError] = useState('');

  useEffect(() => {
    setForm({ ...EMPTY, ...initial });
  }, [initial]);

  function set(key: keyof AddressForm, value: string | boolean) {
    setForm(f => ({ ...f, [key]: value }));
    setErrors(e => ({ ...e, [key]: undefined }));
  }

  function validate() {
    const e: Partial<Record<keyof AddressForm, string>> = {};
    if (!form.name.trim()) e.name = 'Bắt buộc';
    if (!form.phone.trim()) e.phone = 'Bắt buộc';
    if (!form.street.trim()) e.street = 'Bắt buộc';
    if (!form.district.trim()) e.district = 'Bắt buộc';
    if (!form.province.trim()) e.province = 'Bắt buộc';
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSave() {
    if (!validate()) return;
    setPending(true);
    setGeneralError('');
    try {
      await onSave({
        ...form,
        ward: form.ward.trim() || '',
        postalCode: form.postalCode.trim() || '',
      });
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setGeneralError(msg ?? 'Đã có lỗi xảy ra');
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 py-4 border-b border-line">
          <h3 className="font-display font-black text-base uppercase tracking-tight">
            {initial?.name ? t('editAddress') : t('addAddress')}
          </h3>
          <button onClick={onClose} className="text-muted hover:text-ink transition-colors p-1">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Field label={t('addrName')}>
              <input value={form.name} onChange={e => set('name', e.target.value)}
                className={inputCls(errors.name)} />
              {errors.name && <p className="text-xs text-danger mt-1">{errors.name}</p>}
            </Field>
            <Field label={t('addrPhone')}>
              <input type="tel" value={form.phone} onChange={e => set('phone', e.target.value)}
                className={inputCls(errors.phone)} />
              {errors.phone && <p className="text-xs text-danger mt-1">{errors.phone}</p>}
            </Field>
          </div>

          <Field label={t('addrStreet')}>
            <input value={form.street} onChange={e => set('street', e.target.value)}
              placeholder="123 Đường ABC" className={inputCls(errors.street)} />
            {errors.street && <p className="text-xs text-danger mt-1">{errors.street}</p>}
          </Field>

          <div className="grid grid-cols-2 gap-3">
            <Field label={`${t('addrWard')} (tùy chọn)`}>
              <input value={form.ward} onChange={e => set('ward', e.target.value)}
                className={inputCls()} />
            </Field>
            <Field label={t('addrDistrict')}>
              <input value={form.district} onChange={e => set('district', e.target.value)}
                className={inputCls(errors.district)} />
              {errors.district && <p className="text-xs text-danger mt-1">{errors.district}</p>}
            </Field>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Field label={t('addrProvince')}>
              <input value={form.province} onChange={e => set('province', e.target.value)}
                className={inputCls(errors.province)} />
              {errors.province && <p className="text-xs text-danger mt-1">{errors.province}</p>}
            </Field>
            <Field label={`${t('addrPostal')} (tùy chọn)`}>
              <input value={form.postalCode} onChange={e => set('postalCode', e.target.value)}
                className={inputCls()} />
            </Field>
          </div>

          <label className="flex items-center gap-3 cursor-pointer select-none">
            <input type="checkbox" checked={form.defaultAddress}
              onChange={e => set('defaultAddress', e.target.checked)}
              className="w-4 h-4 accent-accent" />
            <span className="text-sm text-ink-2">{t('addrDefault')}</span>
          </label>

          {generalError && (
            <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>
          )}
        </div>

        <div className="flex gap-3 px-6 py-4 border-t border-line">
          <button onClick={onClose} disabled={pending}
            className="flex-1 border border-line rounded py-2.5 text-sm font-semibold text-ink-2 hover:border-ink hover:text-ink transition-colors disabled:opacity-40">
            {t('cancel')}
          </button>
          <button onClick={handleSave} disabled={pending}
            className="flex-1 bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-2.5 rounded transition-colors">
            {pending ? '...' : t('save')}
          </button>
        </div>
      </div>
    </div>
  );
}
