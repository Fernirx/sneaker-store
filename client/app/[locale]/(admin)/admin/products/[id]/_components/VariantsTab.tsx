'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { SHOE_WIDTH_OPTIONS } from '../../_components/types';

interface VariantRow {
  id: number;
  size: number;
  shoeWidth: string;
  sku: string;
  price: number | null;
  stockQuantity: number;
  minStockLevel: number;
  displayOrder: number;
  active: boolean;
}

interface ColorGroup {
  colorway: string;
  colorwayCode: string | null;
  colorHex: string | null;
  variants: VariantRow[];
}

type VForm = {
  colorway: string; colorwayCode: string; colorHex: string;
  size: string; shoeWidth: string; sku: string;
  price: string; stockQuantity: string; minStockLevel: string; displayOrder: string;
  active: boolean;
};

const EMPTY: VForm = {
  colorway: '', colorwayCode: '', colorHex: '#000000',
  size: '', shoeWidth: 'REGULAR', sku: '',
  price: '', stockQuantity: '0', minStockLevel: '5', displayOrder: '0',
  active: true,
};

function InlineModal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-lg shadow-xl w-full max-w-lg max-h-[85vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-5 py-4 border-b border-line sticky top-0 bg-white">
          <h3 className="font-display font-black text-sm uppercase tracking-wide">{title}</h3>
          <button onClick={onClose} className="text-muted hover:text-ink text-xl leading-none">&times;</button>
        </div>
        <div className="px-5 py-4">{children}</div>
      </div>
    </div>
  );
}

function VariantForm({
  form, setForm, error, fieldErrors, saving, onSubmit, onClose, isEdit,
}: {
  form: VForm;
  setForm: React.Dispatch<React.SetStateAction<VForm>>;
  error: string; fieldErrors: Record<string, string>;
  saving: boolean; isEdit: boolean;
  onSubmit: (e: React.FormEvent) => void;
  onClose: () => void;
}) {
  function s(key: keyof VForm, val: string | boolean) {
    setForm(f => ({ ...f, [key]: val }));
  }

  return (
    <form onSubmit={onSubmit} className="space-y-4">
      {error && <p className="text-danger text-sm">{error}</p>}

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Colorway <span className="text-danger">*</span></label>
          <input value={form.colorway} onChange={e => s('colorway', e.target.value)} required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          {fieldErrors.colorway && <p className="text-danger text-xs mt-1">{fieldErrors.colorway}</p>}
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mã colorway</label>
          <input value={form.colorwayCode} onChange={e => s('colorwayCode', e.target.value)}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink" />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Màu HEX</label>
          <div className="flex items-center gap-2">
            <input type="color" value={form.colorHex} onChange={e => s('colorHex', e.target.value)}
              className="w-10 h-9 border border-line rounded-sm cursor-pointer p-0.5" />
            <input value={form.colorHex} onChange={e => s('colorHex', e.target.value)} maxLength={7}
              className="flex-1 border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink" />
          </div>
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Độ rộng <span className="text-danger">*</span></label>
          <select value={form.shoeWidth} onChange={e => s('shoeWidth', e.target.value)} required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white">
            {SHOE_WIDTH_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Size <span className="text-danger">*</span></label>
          <input type="number" value={form.size} onChange={e => s('size', e.target.value)} required min="1"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
          {fieldErrors.size && <p className="text-danger text-xs mt-1">{fieldErrors.size}</p>}
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">SKU <span className="text-danger">*</span></label>
          <input value={form.sku} onChange={e => s('sku', e.target.value)} required
            className="w-full border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink" />
          {fieldErrors.sku && <p className="text-danger text-xs mt-1">{fieldErrors.sku}</p>}
        </div>
      </div>

      <div className="grid grid-cols-3 gap-3">
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Tồn kho <span className="text-danger">*</span></label>
          <input type="number" value={form.stockQuantity} onChange={e => s('stockQuantity', e.target.value)} required min="0"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Cảnh báo tồn</label>
          <input type="number" value={form.minStockLevel} onChange={e => s('minStockLevel', e.target.value)} min="0"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
        </div>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Thứ tự</label>
          <input type="number" value={form.displayOrder} onChange={e => s('displayOrder', e.target.value)} min="0"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
        </div>
      </div>

      <div>
        <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Giá riêng <span className="text-xs font-normal normal-case">(để trống = dùng giá sản phẩm)</span></label>
        <input type="number" value={form.price} onChange={e => s('price', e.target.value)} min="0"
          className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
      </div>

      {isEdit && (
        <label className="flex items-center gap-2 cursor-pointer select-none">
          <input type="checkbox" checked={form.active} onChange={e => s('active', e.target.checked)} className="accent-accent" />
          <span className="text-sm font-medium">Hoạt động</span>
        </label>
      )}

      <div className="flex justify-end gap-2 pt-1">
        <button type="button" onClick={onClose}
          className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">Hủy</button>
        <button type="submit" disabled={saving}
          className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60">
          {saving ? 'Đang lưu...' : 'Lưu'}
        </button>
      </div>
    </form>
  );
}

export default function VariantsTab({ productId, isAdmin }: { productId: number; isAdmin: boolean }) {
  const [groups, setGroups] = useState<ColorGroup[]>([]);
  const [loading, setLoading] = useState(true);

  const [addOpen, setAddOpen] = useState(false);
  const [addForm, setAddForm] = useState<VForm>({ ...EMPTY });
  const [addSaving, setAddSaving] = useState(false);
  const [addError, setAddError] = useState('');
  const [addFieldErrors, setAddFieldErrors] = useState<Record<string, string>>({});

  const [editTarget, setEditTarget] = useState<VariantRow | null>(null);
  const [editForm, setEditForm] = useState<VForm>({ ...EMPTY });
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState('');
  const [editFieldErrors, setEditFieldErrors] = useState<Record<string, string>>({});

  const [deleteTarget, setDeleteTarget] = useState<VariantRow | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  async function load() {
    setLoading(true);
    try {
      const { data } = await clientAxios.get(`/api/admin/products/${productId}/variants`);
      setGroups(data.data ?? []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [productId]);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setAddSaving(true); setAddError(''); setAddFieldErrors({});
    try {
      await clientAxios.post(`/api/admin/products/${productId}/variants`, {
        colorway:      addForm.colorway,
        colorwayCode:  addForm.colorwayCode  || null,
        colorHex:      addForm.colorHex      || null,
        size:          Number(addForm.size),
        shoeWidth:     addForm.shoeWidth,
        sku:           addForm.sku,
        price:         addForm.price ? Number(addForm.price) : null,
        stockQuantity: Number(addForm.stockQuantity),
        minStockLevel: Number(addForm.minStockLevel),
        displayOrder:  Number(addForm.displayOrder),
      });
      setAddOpen(false);
      setAddForm({ ...EMPTY });
      load();
    } catch (err) {
      const p = parseApiError(err);
      setAddError(p.general);
      setAddFieldErrors(p.fields);
    } finally {
      setAddSaving(false);
    }
  }

  function openEdit(v: VariantRow, g: ColorGroup) {
    setEditTarget(v);
    setEditForm({
      colorway:      g.colorway,
      colorwayCode:  g.colorwayCode  ?? '',
      colorHex:      g.colorHex      ?? '#000000',
      size:          String(v.size),
      shoeWidth:     v.shoeWidth,
      sku:           v.sku,
      price:         v.price != null ? String(v.price) : '',
      stockQuantity: String(v.stockQuantity),
      minStockLevel: String(v.minStockLevel),
      displayOrder:  String(v.displayOrder),
      active:        v.active,
    });
    setEditError(''); setEditFieldErrors({});
  }

  async function handleEdit(e: React.FormEvent) {
    e.preventDefault();
    if (!editTarget) return;
    setEditSaving(true); setEditError(''); setEditFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/products/${productId}/variants/${editTarget.id}`, {
        colorway:      editForm.colorway      || null,
        colorwayCode:  editForm.colorwayCode  || null,
        colorHex:      editForm.colorHex      || null,
        size:          editForm.size          ? Number(editForm.size)          : null,
        shoeWidth:     editForm.shoeWidth     || null,
        sku:           editForm.sku           || null,
        price:         editForm.price         ? Number(editForm.price)         : null,
        stockQuantity: editForm.stockQuantity ? Number(editForm.stockQuantity) : null,
        minStockLevel: editForm.minStockLevel ? Number(editForm.minStockLevel) : null,
        displayOrder:  editForm.displayOrder  ? Number(editForm.displayOrder)  : null,
        active:        editForm.active,
      });
      setEditTarget(null);
      load();
    } catch (err) {
      const p = parseApiError(err);
      setEditError(p.general);
      setEditFieldErrors(p.fields);
    } finally {
      setEditSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true); setDeleteError('');
    try {
      await clientAxios.delete(`/api/admin/products/${productId}/variants/${deleteTarget.id}`);
      setDeleteTarget(null);
      load();
    } catch (err) {
      setDeleteError(parseApiError(err).general);
    } finally {
      setDeleting(false);
    }
  }

  if (loading) return <p className="text-muted text-sm py-10 text-center">Đang tải...</p>;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="font-display font-bold text-sm uppercase tracking-wide text-muted">
          {groups.reduce((s, g) => s + g.variants.length, 0)} variant · {groups.length} màu
        </h2>
        {isAdmin && (
          <button onClick={() => { setAddOpen(true); setAddForm({ ...EMPTY }); }}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors">
            Thêm variant
          </button>
        )}
      </div>

      {groups.length === 0 ? (
        <p className="text-muted text-sm py-10 text-center border border-dashed border-line rounded-sm">
          Chưa có variant nào.
        </p>
      ) : (
        groups.map(g => (
          <div key={g.colorway} className="border border-line rounded-sm overflow-hidden">
            <div className="bg-paper px-4 py-2.5 flex items-center gap-3 border-b border-line">
              {g.colorHex && (
                <span className="w-4 h-4 rounded-full border border-line flex-shrink-0" style={{ backgroundColor: g.colorHex }} />
              )}
              <span className="font-bold text-sm">{g.colorway}</span>
              {g.colorwayCode && <span className="font-mono text-xs text-muted">{g.colorwayCode}</span>}
              {isAdmin && (
                <button
                  onClick={() => {
                    setAddForm({
                      ...EMPTY,
                      colorway: g.colorway,
                      colorwayCode: g.colorwayCode ?? '',
                      colorHex: g.colorHex ?? '#000000',
                    });
                    setAddOpen(true);
                  }}
                  className="ml-auto text-[11px] font-bold text-accent hover:underline"
                >
                  + Thêm size
                </button>
              )}
            </div>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-line-2 text-[11px] font-bold uppercase tracking-wide text-muted bg-white">
                  <th className="px-4 py-2 text-left">Size</th>
                  <th className="px-4 py-2 text-left">Rộng</th>
                  <th className="px-4 py-2 text-left">SKU</th>
                  <th className="px-4 py-2 text-left">Giá riêng</th>
                  <th className="px-4 py-2 text-left">Tồn</th>
                  <th className="px-4 py-2 text-left">Trạng thái</th>
                  {isAdmin && <th className="px-4 py-2 w-24" />}
                </tr>
              </thead>
              <tbody>
                {g.variants.map(v => (
                  <tr key={v.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50">
                    <td className="px-4 py-2.5 font-bold">{v.size}</td>
                    <td className="px-4 py-2.5 text-muted text-xs">
                      {SHOE_WIDTH_OPTIONS.find(o => o.value === v.shoeWidth)?.label ?? v.shoeWidth}
                    </td>
                    <td className="px-4 py-2.5 font-mono text-xs">{v.sku}</td>
                    <td className="px-4 py-2.5 text-muted">
                      {v.price != null ? new Intl.NumberFormat('vi-VN').format(v.price) : '—'}
                    </td>
                    <td className="px-4 py-2.5">
                      <span className={v.stockQuantity <= v.minStockLevel ? 'text-danger font-bold' : ''}>
                        {v.stockQuantity}
                      </span>
                    </td>
                    <td className="px-4 py-2.5">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${v.active ? 'bg-ok-bg text-ok' : 'bg-danger-bg text-danger'}`}>
                        {v.active ? 'On' : 'Off'}
                      </span>
                    </td>
                    {isAdmin && (
                      <td className="px-4 py-2.5">
                        <div className="flex gap-3 justify-end">
                          <button onClick={() => openEdit(v, g)} className="text-xs font-bold text-muted hover:text-ink">Sửa</button>
                          <button onClick={() => { setDeleteTarget(v); setDeleteError(''); }} className="text-xs font-bold text-danger hover:opacity-75">Xóa</button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))
      )}

      {addOpen && (
        <InlineModal title="Thêm variant" onClose={() => setAddOpen(false)}>
          <VariantForm form={addForm} setForm={setAddForm} error={addError} fieldErrors={addFieldErrors}
            saving={addSaving} isEdit={false} onSubmit={handleAdd} onClose={() => setAddOpen(false)} />
        </InlineModal>
      )}

      {editTarget && (
        <InlineModal title="Cập nhật variant" onClose={() => setEditTarget(null)}>
          <VariantForm form={editForm} setForm={setEditForm} error={editError} fieldErrors={editFieldErrors}
            saving={editSaving} isEdit={true} onSubmit={handleEdit} onClose={() => setEditTarget(null)} />
        </InlineModal>
      )}

      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
          onClick={() => setDeleteTarget(null)}>
          <div className="bg-white rounded-lg shadow-xl w-full max-w-sm p-5 space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="font-display font-black text-sm uppercase tracking-wide">Xóa variant</h3>
            {deleteError && <p className="text-danger text-sm">{deleteError}</p>}
            <p className="text-sm">Xóa variant <span className="font-bold font-mono">{deleteTarget.sku}</span> (size {deleteTarget.size})?</p>
            <div className="flex justify-end gap-2">
              <button onClick={() => setDeleteTarget(null)} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">Hủy</button>
              <button onClick={handleDelete} disabled={deleting}
                className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60">
                {deleting ? 'Đang xóa...' : 'Xóa'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
