'use client';

import { useState, useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { productUrl } from '@/lib/cloudinaryUrl';

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

interface VariantColor {
  colorway: string;
  colorwayCode: string | null;
  colorHex: string | null;
}

function InlineModal({ title, onClose, children }: { title: string; onClose: () => void; children: React.ReactNode }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-5 py-4 border-b border-line sticky top-0 bg-white">
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
  const [variantColors, setVariantColors] = useState<VariantColor[]>([]);
  const [loading, setLoading] = useState(true);

  // Add state
  const [addOpen, setAddOpen] = useState(false);
  const [colorway, setColorway] = useState('');
  const [colorHex, setColorHex] = useState('#000000');
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [primaryIdx, setPrimaryIdx] = useState(0);
  const [addSaving, setAddSaving] = useState(false);
  const [addProgress, setAddProgress] = useState('');
  const [addError, setAddError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Edit state
  const [editTarget, setEditTarget] = useState<ImageRow | null>(null);
  const [editPrimary, setEditPrimary] = useState(false);
  const [editOrder, setEditOrder] = useState('0');
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState('');

  // Delete state
  const [deleteTarget, setDeleteTarget] = useState<ImageRow | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  async function load() {
    setLoading(true);
    try {
      const [imgRes, varRes] = await Promise.all([
        clientAxios.get(`/api/admin/products/${productId}/images`),
        clientAxios.get(`/api/admin/products/${productId}/variants`),
      ]);
      setGroups(imgRes.data.data ?? []);
      setVariantColors(
        (varRes.data.data ?? []).map((g: VariantColor) => ({
          colorway: g.colorway,
          colorwayCode: g.colorwayCode,
          colorHex: g.colorHex,
        }))
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, [productId]);

  // Revoke object URLs on unmount or when previews change
  useEffect(() => {
    return () => { previews.forEach(URL.revokeObjectURL); };
  }, [previews]);

  function openAdd() {
    setColorway('');
    setColorHex('#000000');
    setPendingFiles([]);
    setPreviews([]);
    setPrimaryIdx(0);
    setAddError('');
    setAddProgress('');
    setAddOpen(true);
  }

  function handleFilesChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    if (!files.length) return;
    previews.forEach(URL.revokeObjectURL);
    setPendingFiles(files);
    setPreviews(files.map(f => URL.createObjectURL(f)));
    setPrimaryIdx(0);
    setAddError('');
    e.target.value = '';
  }

  function removeFile(idx: number) {
    URL.revokeObjectURL(previews[idx]);
    const newFiles = pendingFiles.filter((_, i) => i !== idx);
    const newPreviews = previews.filter((_, i) => i !== idx);
    setPendingFiles(newFiles);
    setPreviews(newPreviews);
    if (primaryIdx >= newFiles.length) setPrimaryIdx(Math.max(0, newFiles.length - 1));
    else if (idx < primaryIdx) setPrimaryIdx(primaryIdx - 1);
  }

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    if (!colorway.trim()) { setAddError('Vui lòng chọn hoặc nhập tên màu.'); return; }
    if (pendingFiles.length === 0) { setAddError('Vui lòng chọn ít nhất 1 ảnh.'); return; }

    setAddSaving(true);
    setAddError('');

    try {
      for (let i = 0; i < pendingFiles.length; i++) {
        setAddProgress(`Đang tải ${i + 1}/${pendingFiles.length}...`);

        const fd = new FormData();
        fd.append('file', pendingFiles[i]);
        fd.append('folder', 'products');
        const { data: uploaded } = await clientAxios.post('/api/upload', fd);

        await clientAxios.post(`/api/admin/products/${productId}/images`, {
          colorway:      colorway.trim(),
          colorHex:      colorHex || null,
          imagePublicId: uploaded.publicId,
          primaryImage:  i === primaryIdx,
          displayOrder:  i,
        });
      }

      setAddOpen(false);
      previews.forEach(URL.revokeObjectURL);
      setPendingFiles([]);
      setPreviews([]);
      load();
    } catch (err) {
      setAddError(parseApiError(err).general || 'Upload thất bại, thử lại.');
    } finally {
      setAddSaving(false);
      setAddProgress('');
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
          <button
            onClick={openAdd}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-4 py-2 rounded-sm hover:bg-accent-700 transition-colors"
          >
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
          <form onSubmit={handleAdd} className="space-y-5">
            {addError && <p className="text-danger text-sm">{addError}</p>}

            {/* Color selector */}
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
                Màu <span className="text-danger">*</span>
              </label>
              {variantColors.length > 0 && (
                <div className="flex flex-wrap gap-2 mb-3">
                  {variantColors.map(c => (
                    <button
                      key={c.colorway}
                      type="button"
                      onClick={() => { setColorway(c.colorway); setColorHex(c.colorHex ?? '#000000'); }}
                      className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-sm border text-xs font-bold transition-all ${
                        colorway === c.colorway
                          ? 'border-ink bg-ink text-white'
                          : 'border-line hover:border-ink'
                      }`}
                    >
                      <span
                        className="w-3 h-3 rounded-full border border-white/30 flex-shrink-0"
                        style={{ backgroundColor: c.colorHex ?? '#ccc' }}
                      />
                      {c.colorway}
                    </button>
                  ))}
                </div>
              )}
              <div className="flex gap-2">
                <input
                  value={colorway}
                  onChange={e => setColorway(e.target.value)}
                  placeholder="Tên colorway"
                  className="flex-1 border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
                />
                <input
                  type="color"
                  value={colorHex}
                  onChange={e => setColorHex(e.target.value)}
                  className="w-10 h-9 border border-line rounded-sm cursor-pointer p-0.5"
                />
              </div>
            </div>

            {/* File picker */}
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
                Ảnh <span className="text-danger">*</span>
                {pendingFiles.length > 0 && (
                  <span className="ml-2 font-normal normal-case tracking-normal text-ink">
                    {pendingFiles.length} file đã chọn
                  </span>
                )}
              </label>
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="px-4 py-2 border border-line rounded-sm text-sm font-bold hover:bg-paper transition-colors"
              >
                Chọn ảnh (nhiều file)
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                className="hidden"
                onChange={handleFilesChange}
              />
            </div>

            {/* Preview grid */}
            {previews.length > 0 && (
              <div>
                <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">
                  Click để chọn ảnh chính
                </p>
                <div className="flex flex-wrap gap-2">
                  {previews.map((src, i) => (
                    <div key={i} className="relative">
                      <button
                        type="button"
                        onClick={() => setPrimaryIdx(i)}
                        className={`block border-2 rounded-sm overflow-hidden transition-all ${
                          i === primaryIdx ? 'border-accent' : 'border-line hover:border-muted'
                        }`}
                      >
                        <img src={src} alt="" className="w-20 h-20 object-contain bg-paper" />
                      </button>
                      {i === primaryIdx && (
                        <span className="absolute top-1 left-1 bg-accent text-white text-[9px] font-bold px-1 py-0.5 rounded pointer-events-none">
                          CHÍNH
                        </span>
                      )}
                      <button
                        type="button"
                        onClick={() => removeFile(i)}
                        className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-danger text-white rounded-full text-[10px] font-bold flex items-center justify-center leading-none hover:opacity-80"
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="flex justify-end gap-2 pt-1">
              <button
                type="button"
                onClick={() => setAddOpen(false)}
                className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper"
              >
                Hủy
              </button>
              <button
                type="submit"
                disabled={addSaving}
                className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 min-w-[100px]"
              >
                {addSaving
                  ? (addProgress || 'Đang tải...')
                  : `Thêm${pendingFiles.length > 1 ? ` ${pendingFiles.length} ảnh` : ' ảnh'}`}
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
