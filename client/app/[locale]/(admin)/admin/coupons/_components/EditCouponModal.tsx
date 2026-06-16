'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type CouponRow, type DiscountType, toInputDatetime, fromInputDatetime } from './types';

export default function EditCouponModal({
  coupon,
  onClose,
  onSaved,
}: {
  coupon: CouponRow;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [description, setDescription] = useState(coupon.description ?? '');
  const [discountValue, setDiscountValue] = useState(String(coupon.discountValue));
  const [minOrderAmount, setMinOrderAmount] = useState(coupon.minOrderAmount != null ? String(coupon.minOrderAmount) : '');
  const [maxDiscountAmount, setMaxDiscountAmount] = useState(coupon.maxDiscountAmount != null ? String(coupon.maxDiscountAmount) : '');
  const [usageLimit, setUsageLimit] = useState(coupon.usageLimit != null ? String(coupon.usageLimit) : '');
  const [userUsageLimit, setUserUsageLimit] = useState(coupon.userUsageLimit != null ? String(coupon.userUsageLimit) : '');
  const [startDate, setStartDate] = useState(toInputDatetime(coupon.startDate));
  const [endDate, setEndDate] = useState(toInputDatetime(coupon.endDate));
  const [active, setActive] = useState(coupon.active);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.patch(`/api/admin/coupons/${coupon.id}`, {
        description: description || null,
        discountValue: Number(discountValue),
        minOrderAmount: minOrderAmount ? Number(minOrderAmount) : null,
        maxDiscountAmount: maxDiscountAmount ? Number(maxDiscountAmount) : null,
        usageLimit: usageLimit ? Number(usageLimit) : null,
        userUsageLimit: userUsageLimit ? Number(userUsageLimit) : null,
        startDate: fromInputDatetime(startDate),
        endDate: fromInputDatetime(endDate),
        active,
      });
      onSaved();
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Cập nhật coupon" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-3 max-h-[70vh] overflow-y-auto pr-1">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div className="flex items-center gap-3 py-1 px-3 bg-paper rounded-sm border border-line">
          <span className="font-mono font-bold text-sm">{coupon.code}</span>
          <span className={`ml-auto text-[10px] font-bold px-2 py-0.5 rounded ${
            coupon.discountType === 'PERCENTAGE' ? 'bg-blue-100 text-blue-700' : 'bg-green-100 text-green-700'
          }`}>
            {coupon.discountType === 'PERCENTAGE' ? 'Phần trăm' : 'Cố định'}
          </span>
        </div>

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Mô tả
          </label>
          <textarea
            value={description}
            onChange={e => setDescription(e.target.value)}
            rows={2}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giá trị <span className="text-danger">*</span>
              <span className="ml-1 normal-case font-normal">
                {coupon.discountType === 'PERCENTAGE' ? '(%)' : '(₫)'}
              </span>
            </label>
            <input
              type="number"
              value={discountValue}
              onChange={e => setDiscountValue(e.target.value)}
              required
              min="0"
              step="any"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
            {fieldErrors.discountValue && <p className="text-danger text-xs mt-1">{fieldErrors.discountValue}</p>}
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Đơn tối thiểu (₫)
            </label>
            <input
              type="number"
              value={minOrderAmount}
              onChange={e => setMinOrderAmount(e.target.value)}
              min="0"
              step="any"
              placeholder="Không giới hạn"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giảm tối đa (₫)
            </label>
            <input
              type="number"
              value={maxDiscountAmount}
              onChange={e => setMaxDiscountAmount(e.target.value)}
              min="0"
              step="any"
              placeholder="Không giới hạn"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giới hạn lượt dùng
            </label>
            <input
              type="number"
              value={usageLimit}
              onChange={e => setUsageLimit(e.target.value)}
              min="1"
              step="1"
              placeholder="Không giới hạn"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giới hạn / user
            </label>
            <input
              type="number"
              value={userUsageLimit}
              onChange={e => setUserUsageLimit(e.target.value)}
              min="1"
              step="1"
              placeholder="Không giới hạn"
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
          <div />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Ngày bắt đầu
            </label>
            <input
              type="datetime-local"
              value={startDate}
              onChange={e => setStartDate(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Ngày kết thúc
            </label>
            <input
              type="datetime-local"
              value={endDate}
              onChange={e => setEndDate(e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
        </div>

        <div>
          <label className="flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={active}
              onChange={e => setActive(e.target.checked)}
              className="accent-accent"
            />
            <span className="text-[11px] font-bold uppercase tracking-wider">Đang hoạt động</span>
          </label>
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-4 py-2 bg-accent text-white text-sm font-bold rounded-sm hover:bg-accent-700 disabled:opacity-60 transition-colors"
          >
            {saving ? 'Đang lưu...' : 'Lưu'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
