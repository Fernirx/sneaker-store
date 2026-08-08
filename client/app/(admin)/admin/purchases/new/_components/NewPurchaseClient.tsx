'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import PurchaseItemsBuilder, { type DraftPurchaseItem } from '../../_components/PurchaseItemsBuilder';
import { formatPrice, type SupplierBrief } from '../../_components/types';

export default function NewPurchaseClient({ suppliers }: { suppliers: SupplierBrief[] }) {
  const router = useRouter();

  const [supplierId, setSupplierId] = useState('');
  const [supplierInvoiceNo, setSupplierInvoiceNo] = useState('');
  const [discountAmount, setDiscountAmount] = useState('0');
  const [taxAmount, setTaxAmount] = useState('0');
  const [shippingCost, setShippingCost] = useState('0');
  const [notes, setNotes] = useState('');
  const [items, setItems] = useState<DraftPurchaseItem[]>([]);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const subtotal = items.reduce((s, it) => s + it.quantityOrdered * it.unitCost, 0);
  const totalPreview = subtotal - Number(discountAmount || 0) + Number(taxAmount || 0) + Number(shippingCost || 0);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (items.length === 0) {
      setError('Cần thêm ít nhất một sản phẩm.');
      return;
    }
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      const { data } = await clientAxios.post('/api/admin/purchases', {
        supplierId: Number(supplierId),
        supplierInvoiceNo: supplierInvoiceNo || null,
        discountAmount: Number(discountAmount || 0),
        taxAmount: Number(taxAmount || 0),
        shippingCost: Number(shippingCost || 0),
        notes: notes || null,
        items: items.map(it => ({
          variantId: it.variantId,
          quantityOrdered: it.quantityOrdered,
          unitCost: it.unitCost,
        })),
      });
      router.push(`/admin/purchases/${data.data.id}`);
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-5 max-w-4xl">
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/purchases" className="text-muted hover:text-ink transition-colors">
          Phiếu nhập hàng
        </Link>
        <span className="text-muted">/</span>
        <span className="font-bold">Tạo mới</span>
      </div>

      <form onSubmit={handleSubmit} className="space-y-5">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div className="bg-white border border-line rounded-sm p-5 space-y-4">
          <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted">Thông tin chung</h3>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
                Nhà cung cấp <span className="text-danger">*</span>
              </label>
              <select
                value={supplierId}
                onChange={e => setSupplierId(e.target.value)}
                required
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white"
              >
                <option value="">-- Chọn nhà cung cấp --</option>
                {suppliers.map(s => <option key={s.id} value={s.id}>{s.code} — {s.name}</option>)}
              </select>
              {fieldErrors.supplierId && <p className="text-danger text-xs mt-1">{fieldErrors.supplierId}</p>}
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Số hóa đơn NCC</label>
              <input
                value={supplierInvoiceNo}
                onChange={e => setSupplierInvoiceNo(e.target.value.replace(/^\s+/, ''))}
                maxLength={100}
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Giảm giá</label>
              <input
                type="number" min={0} value={discountAmount}
                onChange={e => setDiscountAmount(e.target.value)}
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Thuế</label>
              <input
                type="number" min={0} value={taxAmount}
                onChange={e => setTaxAmount(e.target.value)}
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
              />
            </div>
            <div>
              <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Phí vận chuyển</label>
              <input
                type="number" min={0} value={shippingCost}
                onChange={e => setShippingCost(e.target.value)}
                className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
              />
            </div>
          </div>

          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Ghi chú</label>
            <textarea
              value={notes}
              onChange={e => setNotes(e.target.value.replace(/^\s+/, ''))}
              rows={2}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
            />
          </div>
        </div>

        <div className="bg-white border border-line rounded-sm p-5 space-y-4">
          <h3 className="font-display font-bold text-xs uppercase tracking-wide text-muted">Sản phẩm</h3>
          <PurchaseItemsBuilder items={items} onChange={setItems} />
        </div>

        <div className="bg-white border border-line rounded-sm p-5 flex items-center justify-between">
          <span className="text-sm text-muted">Tổng tiền dự kiến</span>
          <span className="font-display font-black text-lg">{formatPrice(Math.max(0, totalPreview))}</span>
        </div>

        <div className="flex justify-end gap-2">
          <Link href="/admin/purchases" className="px-4 py-2.5 border border-line text-sm rounded-sm hover:bg-paper transition-colors">
            Hủy
          </Link>
          <button
            type="submit"
            disabled={saving || !supplierId}
            className="bg-accent text-white font-display font-bold text-[11px] uppercase tracking-wider px-5 py-2.5 rounded-sm hover:bg-accent-700 transition-colors disabled:opacity-50"
          >
            {saving ? 'Đang thêm...' : 'Thêm phiếu nhập (Nháp)'}
          </button>
        </div>
      </form>
    </div>
  );
}
