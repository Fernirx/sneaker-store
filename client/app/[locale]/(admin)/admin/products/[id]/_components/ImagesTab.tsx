'use client';

import { useState, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { productUrl } from '@/lib/cloudinaryUrl';
import ImageUpload from '@/components/admin/ImageUpload';

interface ImageRow {
  id: number;
  publicId: string;
  primaryImage: boolean;
  displayOrder: number;
}

interface ImgGroup {
  colorway: string;
  colorHex: string | null;
  images: ImageRow[];
}

type AddForm = {
  colorway: string;
  colorHex: string;
  imagePublicId: string;
  primaryImage: boolean;
  displayOrder: string;
};

const EMPTY_ADD: AddForm = {
  colorway: '', colorHex: '#000000', imagePublicId: '',
  primaryImage: false, displayOrder: '0',
};

function InlineModal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-5 py-4 border-b border-line">
          <h3 className="font-display font-black text-sm uppercase tracking-wide">{title}</h3>
          <button onClick={onClose} className="text-muted hover:text-ink text-xl leading-none">&times;</button>
        </div>
        <div className="px-5 py-4">{children}</div>
      </div>
    </div>
  );
}

export default function ImagesTab({ productId, isAdmin }: { productId: number; isAdmin: boolean }) {
  const [groups, setGroups] = useState<ImgGroup[]>([]);
  const [loading, setLoading] = useState(true);

  const [addOpen, setAddOpen] = useState(false);
  const [addForm, setAddForm] = useState<AddForm>({ ...EMPTY_ADD });
  const [addSaving, setAddSaving] = useState(false);
  const [addError, setAddError] = useState('');
  const [addFieldErrors, setAddFieldErrors] = useState<Record<string, string>>({});

  const [editTarget, setEditTarget] = useState<ImageRow | null>(null);
  const [editPrimary, setEditPrimary] = useState(false);
  const [editOrder, setEditOrder] = useState('0');
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState('');

  const [deleteTarget, setDeleteTarget] = useState<ImageRow | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  async function load() {
    setLoading(true);
    try {
      const { data } = await clientAxios.get(`/api/admin/products/${productId}/images`);
      setGroups(data.data ?? []);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [productId]);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    if (!addForm.imagePublicId) { setAddError('Vui lòng chọn ảnh.'); return; }
    setAddSaving(true); setAddError(''); setAddFieldErrors({});
    try {
      await clientAxios.post(`/api/admin/products/${productId}/images`, {
        colorway:      addForm.colorway,
        colorHex:      addForm.colorHex      || null,
        imagePublicId: addForm.imagePublicId,
        primaryImage:  addForm.primaryImage,
        displayOrder:  Number(addForm.displayOrder),
      });
      setAddOpen(false);
      setAddForm({ ...EMPTY_ADD });
      load();
    } catch (err) {
      const p = parseApiError(err);
      setAddError(p.general);
      setAddFieldErrors(p.fields);
    } finally {
      setAddSaving(false);
    }
  }

  function openEdit(img: ImageRow) {
    setEditTarget(img);
    setEditPrimary(img.primaryImage);
    setEditOrder(String(img.displayOrder));
    setEditError('');
  }

  async function handleEdit(e: React.FormEvent) {
    e.preventDefault();
    if (!editTarget) return;
    setEditSaving(true); setEditError('');
    try {
      await clientAxios.patch(`/api/admin/products/${productId}/images/${editTarget.id}`, {
        primaryImage: editPrimary,
        displayOrder: Number(editOrder),
      });
      setEditTarget(null);
      load();
    } catch (err) {
      setEditError(parseApiError(err).general);
    } finally {
      setEditSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true); setDeleteError('');
    try {
      await clientAxios.delete(`/api/admin/products/${productId}/images/${deleteTarget.id}`);
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
          {groups.reduce((s, g) => s + g.images.length, 0)} ảnh · {groups.length} màu
        </h2>
        {isAdmin && (
          <button onClick={() => { setAddOpen(true); setAddForm({ ...EMPTY_ADD }); }}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors">
            Thêm ảnh
          </button>
        )}
      </div>

      {groups.length === 0 ? (
        <p className="text-muted text-sm py-10 text-center border border-dashed border-line rounded-sm">
          Chưa có ảnh nào.
        </p>
      ) : (
        groups.map(g => (
          <div key={g.colorway} className="border border-line rounded-sm overflow-hidden">
            <div className="bg-paper px-4 py-2.5 flex items-center gap-3 border-b border-line">
              {g.colorHex && (
                <span className="w-4 h-4 rounded-full border border-line flex-shrink-0" style={{ backgroundColor: g.colorHex }} />
              )}
              <span className="font-bold text-sm">{g.colorway}</span>
            </div>
            <div className="p-4 flex flex-wrap gap-3">
              {g.images.map(img => (
                <div key={img.id} className="relative group">
                  <div className={`border-2 rounded-sm overflow-hidden ${img.primaryImage ? 'border-accent' : 'border-line'}`}>
                    <img
                      src={productUrl(img.publicId, 100, 100)}
                      alt=""
                      className="w-24 h-24 object-contain bg-paper"
                    />
                  </div>
                  {img.primaryImage && (
                    <span className="absolute top-1 left-1 bg-accent text-white text-[9px] font-bold px-1 py-0.5 rounded">
                      CHÍNH
                    </span>
                  )}
                  <p className="text-[10px] text-muted text-center mt-1">#{img.displayOrder}</p>
                  {isAdmin && (
                    <div className="flex gap-2 justify-center mt-1">
                      <button onClick={() => openEdit(img)} className="text-[10px] font-bold text-muted hover:text-ink">Sửa</button>
                      <button onClick={() => { setDeleteTarget(img); setDeleteError(''); }} className="text-[10px] font-bold text-danger hover:opacity-75">Xóa</button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        ))
      )}

      {/* Add modal */}
      {addOpen && (
        <InlineModal title="Thêm ảnh" onClose={() => setAddOpen(false)}>
          <form onSubmit={handleAdd} className="space-y-4">
            {addError && <p className="text-danger text-sm">{addError}</p>}

            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
                Ảnh <span className="text-danger">*</span>
              </label>
              <ImageUpload
                value={addForm.imagePublicId}
                folder="products"
                onChange={v => setAddForm(f => ({ ...f, imagePublicId: v }))}
              />
            </div>

            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
                Colorway <span className="text-danger">*</span>
              </label>
              <input value={addForm.colorway} onChange={e => setAddForm(f => ({ ...f, colorway: e.target.value }))} required
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
              {addFieldErrors.colorway && <p className="text-danger text-xs mt-1">{addFieldErrors.colorway}</p>}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Màu HEX</label>
                <div className="flex items-center gap-2">
                  <input type="color" value={addForm.colorHex}
                    onChange={e => setAddForm(f => ({ ...f, colorHex: e.target.value }))}
                    className="w-10 h-9 border border-line rounded-sm cursor-pointer p-0.5" />
                  <input value={addForm.colorHex} maxLength={7}
                    onChange={e => setAddForm(f => ({ ...f, colorHex: e.target.value }))}
                    className="flex-1 border border-line rounded-sm px-3 py-2 text-sm font-mono focus:outline-none focus:border-ink" />
                </div>
              </div>
              <div>
                <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Thứ tự</label>
                <input type="number" value={addForm.displayOrder} min="0"
                  onChange={e => setAddForm(f => ({ ...f, displayOrder: e.target.value }))}
                  className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
              </div>
            </div>

            <label className="flex items-center gap-2 cursor-pointer select-none">
              <input type="checkbox" checked={addForm.primaryImage}
                onChange={e => setAddForm(f => ({ ...f, primaryImage: e.target.checked }))} className="accent-accent" />
              <span className="text-sm">Ảnh chính của màu này</span>
            </label>

            <div className="flex justify-end gap-2 pt-1">
              <button type="button" onClick={() => setAddOpen(false)} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">Hủy</button>
              <button type="submit" disabled={addSaving}
                className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60">
                {addSaving ? 'Đang lưu...' : 'Lưu'}
              </button>
            </div>
          </form>
        </InlineModal>
      )}

      {/* Edit modal */}
      {editTarget && (
        <InlineModal title="Cập nhật ảnh" onClose={() => setEditTarget(null)}>
          <form onSubmit={handleEdit} className="space-y-4">
            {editError && <p className="text-danger text-sm">{editError}</p>}
            <img src={productUrl(editTarget.publicId, 160, 160)} alt="" className="w-40 h-40 object-contain bg-paper border border-line rounded-sm mx-auto" />
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Thứ tự hiển thị</label>
              <input type="number" value={editOrder} onChange={e => setEditOrder(e.target.value)} min="0"
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink" />
            </div>
            <label className="flex items-center gap-2 cursor-pointer select-none">
              <input type="checkbox" checked={editPrimary} onChange={e => setEditPrimary(e.target.checked)} className="accent-accent" />
              <span className="text-sm">Ảnh chính của màu này</span>
            </label>
            <div className="flex justify-end gap-2 pt-1">
              <button type="button" onClick={() => setEditTarget(null)} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper">Hủy</button>
              <button type="submit" disabled={editSaving}
                className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60">
                {editSaving ? 'Đang lưu...' : 'Lưu'}
              </button>
            </div>
          </form>
        </InlineModal>
      )}

      {/* Delete confirm */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
          onClick={() => setDeleteTarget(null)}>
          <div className="bg-white rounded-lg shadow-xl w-full max-w-sm p-5 space-y-4" onClick={e => e.stopPropagation()}>
            <h3 className="font-display font-black text-sm uppercase tracking-wide">Xóa ảnh</h3>
            {deleteError && <p className="text-danger text-sm">{deleteError}</p>}
            <p className="text-sm">Xóa ảnh này khỏi sản phẩm?</p>
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
