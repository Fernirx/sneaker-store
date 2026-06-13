'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import {
  type ProductRow,
  GENDER_OPTIONS, CLOSURE_OPTIONS, SHAFT_OPTIONS,
  formatDate,
} from '../../_components/types';

export default function BasicInfoTab({
  product,
  isAdmin,
}: {
  product: ProductRow;
  isAdmin: boolean;
}) {
  const [form, setForm] = useState({
    name:          product.name,
    code:          product.code,
    styleCode:     product.styleCode ?? '',
    gender:        product.gender,
    description:   product.description ?? '',
    upperMaterial: product.upperMaterial ?? '',
    soleType:      product.soleType ?? '',
    closureType:   product.closureType ?? '',
    shaftStyle:    product.shaftStyle ?? '',
    basePrice:     String(product.basePrice),
    originalPrice: product.originalPrice != null ? String(product.originalPrice) : '',
    costPrice:     product.costPrice     != null ? String(product.costPrice)     : '',
    newArrival:    product.newArrival ?? false,
    onSale:        product.onSale    ?? false,
    active:        product.active    ?? true,
  });
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function set(key: string, value: string | boolean) {
    setForm(f => ({ ...f, [key]: value }));
    setSuccess('');
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    setFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/products/${product.id}`, {
        name:          form.name          || null,
        code:          form.code          || null,
        styleCode:     form.styleCode     || null,
        gender:        form.gender        || null,
        description:   form.description   || null,
        upperMaterial: form.upperMaterial || null,
        soleType:      form.soleType      || null,
        closureType:   form.closureType   || null,
        shaftStyle:    form.shaftStyle    || null,
        basePrice:     form.basePrice     ? Number(form.basePrice)     : null,
        originalPrice: form.originalPrice ? Number(form.originalPrice) : null,
        costPrice:     form.costPrice     ? Number(form.costPrice)     : null,
        newArrival:    form.newArrival,
        onSale:        form.onSale,
        active:        form.active,
      });
      setSuccess('Đã lưu thành công.');
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  const ro = !isAdmin;

  return (
    <form onSubmit={handleSubmit} className="max-w-2xl space-y-5">
      {error   && <p className="text-danger text-sm">{error}</p>}
      {success && <p className="text-ok text-sm">{success}</p>}

      <p className="font-mono text-xs text-muted">
        slug: {product.slug} &nbsp;·&nbsp; brand: {product.brand.name}
      </p>

      {/* Name */}
      <div>
        <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
          Tên sản phẩm <span className="text-danger">*</span>
        </label>
        <input value={form.name} onChange={e => set('name', e.target.value)} disabled={ro}
          className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink disabled:bg-paper" />
        {fieldErrors.name && <p className="text-danger text-xs mt-1">{fieldErrors.name}</p>}
      </div>

      {/* Code + Style code */}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Mã sản phẩm <span className="text-danger">*</span>
          </label>
          <input value={form.code} onChange={e => set('code', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink disabled:bg-paper" />
          {fieldErrors.code && <p className="text-danger text-xs mt-1">{fieldErrors.code}</p>}
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mã style</label>
          <input value={form.styleCode} onChange={e => set('styleCode', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink disabled:bg-paper" />
        </div>
      </div>

      {/* Gender */}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Giới tính <span className="text-danger">*</span>
          </label>
          <select value={form.gender} onChange={e => set('gender', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white disabled:bg-paper">
            {GENDER_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
      </div>

      {/* Description */}
      <div>
        <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mô tả</label>
        <textarea value={form.description} onChange={e => set('description', e.target.value)} disabled={ro}
          rows={4} className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none disabled:bg-paper" />
      </div>

      {/* Upper material + Sole type */}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Chất liệu mũi</label>
          <input value={form.upperMaterial} onChange={e => set('upperMaterial', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink disabled:bg-paper" />
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Loại đế</label>
          <input value={form.soleType} onChange={e => set('soleType', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink disabled:bg-paper" />
        </div>
      </div>

      {/* Closure + Shaft */}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Kiểu khóa</label>
          <select value={form.closureType} onChange={e => set('closureType', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white disabled:bg-paper">
            <option value="">— Không chọn —</option>
            {CLOSURE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Kiểu cổ</label>
          <select value={form.shaftStyle} onChange={e => set('shaftStyle', e.target.value)} disabled={ro}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white disabled:bg-paper">
            <option value="">— Không chọn —</option>
            {SHAFT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
      </div>

      {/* Prices */}
      <div className="grid grid-cols-3 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Giá bán <span className="text-danger">*</span>
          </label>
          <input type="number" value={form.basePrice} onChange={e => set('basePrice', e.target.value)} disabled={ro} min="0"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink disabled:bg-paper" />
          {fieldErrors.basePrice && <p className="text-danger text-xs mt-1">{fieldErrors.basePrice}</p>}
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Giá niêm yết</label>
          <input type="number" value={form.originalPrice} onChange={e => set('originalPrice', e.target.value)} disabled={ro} min="0"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink disabled:bg-paper" />
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Giá vốn</label>
          <input type="number" value={form.costPrice} onChange={e => set('costPrice', e.target.value)} disabled={ro} min="0"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink disabled:bg-paper" />
        </div>
      </div>

      {/* Flags + Active */}
      <div className="flex gap-6">
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={form.newArrival} onChange={e => set('newArrival', e.target.checked)} disabled={ro} className="accent-accent" />
          <span className="text-sm">Mới về</span>
        </label>
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={form.onSale} onChange={e => set('onSale', e.target.checked)} disabled={ro} className="accent-accent" />
          <span className="text-sm">Đang sale</span>
        </label>
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={form.active} onChange={e => set('active', e.target.checked)} disabled={ro} className="accent-accent" />
          <span className="text-sm">Hoạt động</span>
        </label>
      </div>

      {/* Stats */}
      <div className="flex gap-6 text-xs text-muted font-mono border-t border-line pt-4">
        <span>Đã bán: {product.soldCount}</span>
        <span>Lượt xem: {product.viewCount}</span>
        <span>Tạo: {formatDate(product.createdAt)}</span>
        <span>Cập nhật: {formatDate(product.updatedAt)}</span>
      </div>

      {isAdmin && (
        <div className="pt-1">
          <button type="submit" disabled={saving}
            className="px-5 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors">
            {saving ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>
      )}
    </form>
  );
}
