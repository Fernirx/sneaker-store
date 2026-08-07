'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';

export interface StoreSetting {
  id: number;
  pointsPerAmount: number;
  freeShipThreshold: number;
  silverThreshold: number;
  goldThreshold: number;
  platinumThreshold: number;
  silverDiscountRate: number;
  goldDiscountRate: number;
  platinumDiscountRate: number;
  updatedAt: string;
}

interface StoreSettingForm {
  pointsPerAmount: string;
  freeShipThreshold: string;
  silverThreshold: string;
  goldThreshold: string;
  platinumThreshold: string;
  silverDiscountRate: string;
  goldDiscountRate: string;
  platinumDiscountRate: string;
}

function toForm(data: StoreSetting | null): StoreSettingForm {
  if (!data) {
    return { pointsPerAmount: '', freeShipThreshold: '', silverThreshold: '', goldThreshold: '', platinumThreshold: '', silverDiscountRate: '0', goldDiscountRate: '0', platinumDiscountRate: '0' };
  }
  return {
    pointsPerAmount: String(data.pointsPerAmount),
    freeShipThreshold: String(data.freeShipThreshold),
    silverThreshold: String(data.silverThreshold),
    goldThreshold: String(data.goldThreshold),
    platinumThreshold: String(data.platinumThreshold),
    silverDiscountRate: String(data.silverDiscountRate ?? 0),
    goldDiscountRate: String(data.goldDiscountRate ?? 0),
    platinumDiscountRate: String(data.platinumDiscountRate ?? 0),
  };
}

function Field({ label, hint, children }: { label: string; hint?: string; children: React.ReactNode }) {
  return (
    <div>
      <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{label}</label>
      {children}
      {hint && <p className="text-xs text-muted mt-1">{hint}</p>}
    </div>
  );
}

function inputCls(err?: string) {
  return `w-full border rounded px-3 py-2.5 text-sm focus:outline-none transition-colors ${
    err ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'
  }`;
}

export default function StoreSettingClient({ initialData }: { initialData: StoreSetting | null }) {
  const router = useRouter();
  const isCreate = initialData === null;

  const [form, setForm] = useState<StoreSettingForm>(toForm(initialData));
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [pending, setPending] = useState(false);
  const [generalError, setGeneralError] = useState('');
  const [success, setSuccess] = useState('');

  function set(key: keyof StoreSettingForm, value: string) {
    setForm(f => ({ ...f, [key]: value }));
    setErrors(e => { const next = { ...e }; delete next[key]; return next; });
  }

  function validate() {
    const e: Record<string, string> = {};
    (Object.keys(form) as (keyof StoreSettingForm)[]).forEach(key => {
      const value = Number(form[key]);
      if (!form[key].trim() || Number.isNaN(value) || value <= 0) {
        e[key] = "Phải là số dương";
      }
    });
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSave() {
    if (!validate()) return;
    setPending(true);
    setGeneralError('');
    setSuccess('');
    try {
      const payload = {
        pointsPerAmount: Number(form.pointsPerAmount),
        freeShipThreshold: Number(form.freeShipThreshold),
        silverThreshold: Number(form.silverThreshold),
        goldThreshold: Number(form.goldThreshold),
        platinumThreshold: Number(form.platinumThreshold),
        silverDiscountRate: Number(form.silverDiscountRate),
        goldDiscountRate: Number(form.goldDiscountRate),
        platinumDiscountRate: Number(form.platinumDiscountRate),
      };
      if (isCreate) {
        await clientAxios.post('/api/admin/settings/store', payload);
      } else {
        await clientAxios.patch('/api/admin/settings/store', payload);
      }
      setSuccess("Đã lưu chính sách giá/ưu đãi.");
      router.refresh();
    } catch (err: unknown) {
      const { general, fields } = parseApiError(err);
      if (Object.keys(fields).length > 0) {
        setErrors(prev => ({ ...prev, ...fields }));
      } else {
        setGeneralError(general);
      }
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="max-w-2xl space-y-6">
      <div>
        <h1 className="font-display font-black text-xl uppercase tracking-tight">Chính sách giá / ưu đãi</h1>
        <p className="text-sm text-muted mt-1">
          Ngưỡng tích điểm, miễn phí vận chuyển và hạng thành viên áp dụng cho toàn hệ thống.
        </p>
      </div>

      {isCreate && (
        <p className="text-sm text-warn bg-warn-bg border border-warn/20 rounded px-3 py-2">
          Chưa cấu hình chính sách giá/ưu đãi. Vui lòng nhập đầy đủ thông tin bên dưới.
        </p>
      )}

      <div className="space-y-4">
        <Field label="Số tiền (VND) cần chi tiêu để được 1 điểm tích lũy">
          <input type="number" min="1" value={form.pointsPerAmount}
            onChange={e => set('pointsPerAmount', e.target.value)}
            className={inputCls(errors.pointsPerAmount)} />
          {errors.pointsPerAmount && <p className="text-xs text-danger mt-1">{errors.pointsPerAmount}</p>}
        </Field>

        <Field label="Ngưỡng đơn hàng được miễn phí vận chuyển (VND)">
          <input type="number" min="1" value={form.freeShipThreshold}
            onChange={e => set('freeShipThreshold', e.target.value)}
            className={inputCls(errors.freeShipThreshold)} />
          {errors.freeShipThreshold && <p className="text-xs text-danger mt-1">{errors.freeShipThreshold}</p>}
        </Field>

        <div className="grid grid-cols-3 gap-3">
          <Field label="Ngưỡng hạng Bạc (VND)">
            <input type="number" min="1" value={form.silverThreshold}
              onChange={e => set('silverThreshold', e.target.value)}
              className={inputCls(errors.silverThreshold)} />
            {errors.silverThreshold && <p className="text-xs text-danger mt-1">{errors.silverThreshold}</p>}
          </Field>
          <Field label="Ngưỡng hạng Vàng (VND)">
            <input type="number" min="1" value={form.goldThreshold}
              onChange={e => set('goldThreshold', e.target.value)}
              className={inputCls(errors.goldThreshold)} />
            {errors.goldThreshold && <p className="text-xs text-danger mt-1">{errors.goldThreshold}</p>}
          </Field>
          <Field label="Ngưỡng hạng Bạch kim (VND)">
            <input type="number" min="1" value={form.platinumThreshold}
              onChange={e => set('platinumThreshold', e.target.value)}
              className={inputCls(errors.platinumThreshold)} />
            {errors.platinumThreshold && <p className="text-xs text-danger mt-1">{errors.platinumThreshold}</p>}
          </Field>
        </div>

        <div className="grid grid-cols-3 gap-3">
          <Field label="Giảm giá hạng Bạc (%)">
            <input type="number" min="0" max="100" value={form.silverDiscountRate}
              onChange={e => set('silverDiscountRate', e.target.value)}
              className={inputCls(errors.silverDiscountRate)} />
            {errors.silverDiscountRate && <p className="text-xs text-danger mt-1">{errors.silverDiscountRate}</p>}
          </Field>
          <Field label="Giảm giá hạng Vàng (%)">
            <input type="number" min="0" max="100" value={form.goldDiscountRate}
              onChange={e => set('goldDiscountRate', e.target.value)}
              className={inputCls(errors.goldDiscountRate)} />
            {errors.goldDiscountRate && <p className="text-xs text-danger mt-1">{errors.goldDiscountRate}</p>}
          </Field>
          <Field label="Giảm giá hạng Bạch kim (%)">
            <input type="number" min="0" max="100" value={form.platinumDiscountRate}
              onChange={e => set('platinumDiscountRate', e.target.value)}
              className={inputCls(errors.platinumDiscountRate)} />
            {errors.platinumDiscountRate && <p className="text-xs text-danger mt-1">{errors.platinumDiscountRate}</p>}
          </Field>
        </div>

        {generalError && (
          <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>
        )}
        {success && (
          <p className="text-sm text-ok bg-ok-bg border border-ok/20 rounded px-3 py-2">{success}</p>
        )}

        <button onClick={handleSave} disabled={pending}
          className="bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider px-6 py-2.5 rounded transition-colors">
          {pending ? "Đang lưu..." : isCreate ? "Khởi tạo cấu hình" : "Lưu thay đổi"}
        </button>
      </div>
    </div>
  );
}
