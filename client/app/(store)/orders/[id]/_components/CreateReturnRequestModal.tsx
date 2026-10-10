'use client';

import { useEffect, useRef, useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { formatPrice } from '../../../products/_components/types';
import type { EligibleOrderItemResponse, ReturnRequestResponse, ReturnResolutionType } from '../../_components/returnTypes';

const MAX_IMAGES = 5;

type SelectedItem = {
  checked: boolean;
  quantity: number;
  exchangeVariantId: number | null;
};

export default function CreateReturnRequestModal({
  orderId,
  onClose,
  onCreated,
}: {
  orderId: number;
  onClose: () => void;
  onCreated: (returnRequest: ReturnRequestResponse) => void;
}) {
  const [eligibleItems, setEligibleItems] = useState<EligibleOrderItemResponse[]>([]);
  const [loadingItems, setLoadingItems] = useState(true);
  const [loadError, setLoadError] = useState('');

  const [resolutionType, setResolutionType] = useState<ReturnResolutionType>('REFUND');
  const [reason, setReason] = useState('');
  const [selected, setSelected] = useState<Record<number, SelectedItem>>({});

  const [pendingFiles, setPendingFiles] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const fileRef = useRef<HTMLInputElement>(null);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    clientAxios.get(`/api/me/returns/eligible-items?orderId=${orderId}`)
      .then(({ data }) => setEligibleItems((data.data ?? []) as EligibleOrderItemResponse[]))
      .catch(err => setLoadError(parseApiError(err, 'Không thể tải danh sách sản phẩm').general))
      .finally(() => setLoadingItems(false));
  }, [orderId]);

  useEffect(() => {
    return () => { previews.forEach(URL.revokeObjectURL); };
  }, [previews]);

  function toggleItem(orderItemId: number, maxQuantity: number) {
    setSelected(prev => {
      const current = prev[orderItemId];
      if (current?.checked) {
        const { [orderItemId]: _removed, ...rest } = prev;
        return rest;
      }
      return { ...prev, [orderItemId]: { checked: true, quantity: Math.min(1, maxQuantity), exchangeVariantId: null } };
    });
  }

  function updateQuantity(orderItemId: number, quantity: number, maxQuantity: number) {
    const clamped = Math.max(1, Math.min(quantity, maxQuantity));
    setSelected(prev => ({ ...prev, [orderItemId]: { ...prev[orderItemId], checked: true, quantity: clamped } }));
  }

  function updateExchangeVariant(orderItemId: number, exchangeVariantId: number) {
    setSelected(prev => ({ ...prev, [orderItemId]: { ...prev[orderItemId], checked: true, exchangeVariantId } }));
  }

  function handleFilesChange(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    e.target.value = '';
    if (!files.length) return;
    const room = MAX_IMAGES - pendingFiles.length;
    const accepted = files.slice(0, Math.max(0, room));
    setPendingFiles(prev => [...prev, ...accepted]);
    setPreviews(prev => [...prev, ...accepted.map(f => URL.createObjectURL(f))]);
  }

  function removePending(idx: number) {
    URL.revokeObjectURL(previews[idx]);
    setPendingFiles(prev => prev.filter((_, i) => i !== idx));
    setPreviews(prev => prev.filter((_, i) => i !== idx));
  }

  const selectedEntries = Object.entries(selected).filter(([, v]) => v.checked);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');

    if (selectedEntries.length === 0) {
      setError('Chọn ít nhất 1 sản phẩm muốn đổi/trả.');
      return;
    }
    if (!reason.trim()) {
      setError('Vui lòng nhập lý do đổi/trả.');
      return;
    }
    if (resolutionType === 'EXCHANGE' && selectedEntries.some(([, v]) => !v.exchangeVariantId)) {
      setError('Vui lòng chọn size/màu muốn đổi cho từng sản phẩm.');
      return;
    }

    setSaving(true);
    try {
      const uploadedIds: string[] = [];
      for (const file of pendingFiles) {
        const form = new FormData();
        form.append('file', file);
        form.append('folder', 'returns');
        const { data } = await clientAxios.post('/api/upload', form);
        uploadedIds.push(data.publicId);
      }

      const body = {
        orderId,
        resolutionType,
        reason: reason.trim(),
        items: selectedEntries.map(([orderItemId, v]) => ({
          orderItemId: Number(orderItemId),
          quantity: v.quantity,
          exchangeVariantId: resolutionType === 'EXCHANGE' ? v.exchangeVariantId : null,
        })),
        imagePublicIds: uploadedIds,
      };

      const { data } = await clientAxios.post('/api/me/returns', body);
      onCreated(data.data as ReturnRequestResponse);
    } catch (err) {
      setError(parseApiError(err, 'Không thể tạo yêu cầu đổi/trả').general);
      document.querySelector('.overflow-y-auto')?.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm" onClick={onClose}>
      <div className="bg-white rounded-lg shadow-xl w-full max-w-xl max-h-[90vh] overflow-y-auto" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-line">
          <h3 className="font-display font-black text-base uppercase tracking-tight">Yêu cầu đổi/trả hàng</h3>
          <button onClick={onClose} className="text-muted hover:text-ink transition-colors p-1">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18" /><line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{error}</p>
          )}

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">Hình thức xử lý</label>
            <div className="flex gap-2">
              <button type="button" onClick={() => setResolutionType('REFUND')}
                className={`flex-1 border rounded-sm py-2 text-sm font-semibold transition-colors ${
                  resolutionType === 'REFUND' ? 'border-ink bg-ink text-white' : 'border-line text-ink-2 hover:border-ink'
                }`}>
                Hoàn tiền
              </button>
              <button type="button" onClick={() => setResolutionType('EXCHANGE')}
                className={`flex-1 border rounded-sm py-2 text-sm font-semibold transition-colors ${
                  resolutionType === 'EXCHANGE' ? 'border-ink bg-ink text-white' : 'border-line text-ink-2 hover:border-ink'
                }`}>
                Đổi size/màu
              </button>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
              Chọn sản phẩm <span className="text-danger">*</span>
            </label>
            {loadingItems ? (
              <p className="text-sm text-muted">Đang tải...</p>
            ) : loadError ? (
              <p className="text-sm text-danger">{loadError}</p>
            ) : eligibleItems.length === 0 ? (
              <p className="text-sm text-muted">Không có sản phẩm nào còn có thể đổi/trả trong đơn này.</p>
            ) : (
              <div className="space-y-2">
                {eligibleItems.map(item => {
                  const sel = selected[item.orderItemId];
                  const disabled = item.maxReturnableQuantity <= 0;
                  return (
                    <div key={item.orderItemId}
                      className={`border rounded-sm p-3 ${sel?.checked ? 'border-ink' : 'border-line'} ${disabled ? 'opacity-50' : ''}`}>
                      <label className="flex items-start gap-3 cursor-pointer">
                        <input type="checkbox" className="mt-1" disabled={disabled}
                          checked={!!sel?.checked}
                          onChange={() => toggleItem(item.orderItemId, item.maxReturnableQuantity)} />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-semibold text-ink">{item.productName}</p>
                          <p className="text-xs text-muted mt-0.5">
                            {item.variantColor} · Size {item.variantSize} · Đã mua {item.purchasedQuantity} · {formatPrice(item.unitPrice)}
                          </p>
                          {disabled && (
                            <p className="text-xs text-danger mt-1">Đã yêu cầu đổi/trả hết số lượng của sản phẩm này.</p>
                          )}
                        </div>
                      </label>

                      {sel?.checked && !disabled && (
                        <div className="mt-3 pl-7 space-y-2">
                          <div className="flex items-center gap-2">
                            <span className="text-xs text-muted">Số lượng:</span>
                            <input type="number" min={1} max={item.maxReturnableQuantity} value={sel.quantity}
                              onChange={e => updateQuantity(item.orderItemId, Number(e.target.value), item.maxReturnableQuantity)}
                              className="w-16 border border-line rounded-sm px-2 py-1 text-sm text-center" />
                            <span className="text-xs text-muted">/ tối đa {item.maxReturnableQuantity}</span>
                          </div>

                          {resolutionType === 'EXCHANGE' && (
                            item.exchangeCandidates.length === 0 ? (
                              <p className="text-xs text-danger">Không có size/màu khác cùng giá để đổi cho sản phẩm này.</p>
                            ) : (
                              <select value={sel.exchangeVariantId ?? ''}
                                onChange={e => updateExchangeVariant(item.orderItemId, Number(e.target.value))}
                                className="w-full border border-line rounded-sm px-2 py-1.5 text-sm">
                                <option value="">— Chọn size/màu muốn đổi —</option>
                                {item.exchangeCandidates.map(c => (
                                  <option key={c.variantId} value={c.variantId} disabled={c.stockQuantity <= 0}>
                                    {c.colorway} · Size {c.size}{c.stockQuantity <= 0 ? ' (hết hàng)' : ''}
                                  </option>
                                ))}
                              </select>
                            )
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
              Lý do đổi/trả <span className="text-danger">*</span>
            </label>
            <textarea value={reason} onChange={e => setReason(e.target.value.replace(/^\s+/, ''))} rows={3}
              placeholder="Vd: sản phẩm không vừa size, giao sai màu..."
              className="w-full border border-line rounded px-3 py-2.5 text-sm focus:outline-none focus:border-ink transition-colors resize-none" />
          </div>

          <div>
            <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
              Ảnh minh chứng ({pendingFiles.length}/{MAX_IMAGES})
            </label>
            <div className="flex flex-wrap gap-2">
              {previews.map((src, i) => (
                <div key={i} className="relative">
                  <img src={src} alt="" className="w-16 h-16 object-cover rounded-sm border border-line" />
                  <button type="button" onClick={() => removePending(i)}
                    className="absolute -top-1.5 -right-1.5 w-4 h-4 bg-danger text-white rounded-full text-[10px] font-bold flex items-center justify-center leading-none hover:opacity-80">
                    ×
                  </button>
                </div>
              ))}
              {pendingFiles.length < MAX_IMAGES && (
                <button type="button" onClick={() => fileRef.current?.click()}
                  className="w-16 h-16 border border-dashed border-line rounded-sm flex items-center justify-center text-muted hover:border-ink hover:text-ink transition-colors text-xl">
                  +
                </button>
              )}
            </div>
            <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp" multiple className="hidden"
              onChange={handleFilesChange} />
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} disabled={saving}
              className="flex-1 border border-line rounded py-2.5 text-sm font-semibold text-ink-2 hover:border-ink hover:text-ink transition-colors disabled:opacity-40">
              Hủy
            </button>
            <button type="submit" disabled={saving || loadingItems || selectedEntries.length === 0 || !reason.trim() || (resolutionType === 'EXCHANGE' && selectedEntries.some(([, v]) => !v.exchangeVariantId))}
              className="flex-1 bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-2.5 rounded transition-colors">
              {saving ? '...' : 'Gửi yêu cầu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
