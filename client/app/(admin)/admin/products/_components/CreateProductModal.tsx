'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { GENDER_OPTIONS, CLOSURE_OPTIONS, SHAFT_OPTIONS } from './types';

interface BrandOption { id: number; name: string; }

export default function CreateProductModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: (id: number) => void;
}) {
  const [brands, setBrands] = useState<BrandOption[]>([]);
  const [form, setForm] = useState({
    name: '', code: '', styleCode: '', brandId: '',
    gender: 'UNISEX', description: '',
    upperMaterial: '', soleType: '',
    closureType: '', shaftStyle: '',
    basePrice: '', originalPrice: '', costPrice: '',
    newArrival: false, onSale: false,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    clientAxios.get('/api/admin/brands?page=0&size=100&sort=name,asc')
      .then(res => setBrands(res.data.data ?? []))
      .catch(() => {});
  }, []);

  function set(key: string, value: string | boolean) {
    setForm(f => ({ ...f, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      const { data } = await clientAxios.post('/api/admin/products', {
        name: form.name,
        code: form.code,
        styleCode: form.styleCode || null,
        brandId: Number(form.brandId),
        gender: form.gender,
        description: form.description || null,
        upperMaterial: form.upperMaterial || null,
        soleType: form.soleType || null,
        closureType: form.closureType || null,
        shaftStyle: form.shaftStyle || null,
        basePrice: Number(form.basePrice),
        originalPrice: form.originalPrice ? Number(form.originalPrice) : null,
        costPrice: form.costPrice ? Number(form.costPrice) : null,
        newArrival: form.newArrival,
        onSale: form.onSale,
      });
      onCreated(data.data.id);
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Tạo sản phẩm mới" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-4 max-h-[70vh] overflow-y-auto pr-1">
        {error && <p className="text-danger text-sm">{error}</p>}

        {/* Name */}
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Tên sản phẩm <span className="text-danger">*</span>
          </label>
          <input value={form.name} onChange={e => set('name', e.target.value)} required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          {fieldErrors.name && <p className="text-danger text-xs mt-1">{fieldErrors.name}</p>}
        </div>

        {/* Code + Style code */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Mã SP <span className="text-danger">*</span>
            </label>
            <input value={form.code} onChange={e => set('code', e.target.value)} required
              className="w-full border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink" />
            {fieldErrors.code && <p className="text-danger text-xs mt-1">{fieldErrors.code}</p>}
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mã style</label>
            <input value={form.styleCode} onChange={e => set('styleCode', e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink" />
          </div>
        </div>

        {/* Brand + Gender */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Thương hiệu <span className="text-danger">*</span>
            </label>
            <select value={form.brandId} onChange={e => set('brandId', e.target.value)} required
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white">
              <option value="">— Chọn —</option>
              {brands.map(b => <option key={b.id} value={b.id}>{b.name}</option>)}
            </select>
            {fieldErrors.brandId && <p className="text-danger text-xs mt-1">{fieldErrors.brandId}</p>}
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giới tính <span className="text-danger">*</span>
            </label>
            <select value={form.gender} onChange={e => set('gender', e.target.value)} required
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white">
              {GENDER_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>
        </div>

        {/* Prices */}
        <div className="grid grid-cols-3 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giá bán <span className="text-danger">*</span>
            </label>
            <input type="number" value={form.basePrice} onChange={e => set('basePrice', e.target.value)} required min="0"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
            {fieldErrors.basePrice && <p className="text-danger text-xs mt-1">{fieldErrors.basePrice}</p>}
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Giá niêm yết</label>
            <input type="number" value={form.originalPrice} onChange={e => set('originalPrice', e.target.value)} min="0"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Giá vốn</label>
            <input type="number" value={form.costPrice} onChange={e => set('costPrice', e.target.value)} min="0"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          </div>
        </div>

        {/* Closure + Shaft */}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Kiểu khóa</label>
            <select value={form.closureType} onChange={e => set('closureType', e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white">
              <option value="">— Không chọn —</option>
              {CLOSURE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Kiểu cổ</label>
            <select value={form.shaftStyle} onChange={e => set('shaftStyle', e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white">
              <option value="">— Không chọn —</option>
              {SHAFT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>
        </div>

        {/* Checkboxes */}
        <div className="flex gap-5">
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input type="checkbox" checked={form.newArrival} onChange={e => set('newArrival', e.target.checked)} className="accent-accent" />
            <span className="text-sm">Mới về</span>
          </label>
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input type="checkbox" checked={form.onSale} onChange={e => set('onSale', e.target.checked)} className="accent-accent" />
            <span className="text-sm">Đang sale</span>
          </label>
        </div>

        <p className="text-xs text-muted">Sau khi tạo, bạn có thể thêm variant, ảnh và danh mục trong trang chỉnh sửa.</p>

        <div className="flex justify-end gap-2 pt-1">
          <button type="button" onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors">
            Hủy
          </button>
          <button type="submit" disabled={saving}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors">
            {saving ? 'Đang tạo...' : 'Tạo & tiếp tục'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
