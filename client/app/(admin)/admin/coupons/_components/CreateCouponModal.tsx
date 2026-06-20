'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type DiscountType, fromInputDatetime } from './types';

export default function CreateCouponModal({
  onClose,
  onCreated,
}: {
  onClose: () => void;
  onCreated: () => void;
}) {
  const [code, setCode] = useState('');
  const [description, setDescription] = useState('');
  const [discountType, setDiscountType] = useState<DiscountType>('PERCENTAGE');
  const [discountValue, setDiscountValue] = useState('');
  const [minOrderAmount, setMinOrderAmount] = useState('');
  const [maxDiscountAmount, setMaxDiscountAmount] = useState('');
  const [usageLimit, setUsageLimit] = useState('');
  const [userUsageLimit, setUserUsageLimit] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});
    try {
      await clientAxios.post('/api/admin/coupons', {
        code: code.toUpperCase(),
        description: description || null,
        discountType,
        discountValue: Number(discountValue),
        minOrderAmount: minOrderAmount ? Number(minOrderAmount) : null,
        maxDiscountAmount: maxDiscountAmount ? Number(maxDiscountAmount) : null,
        usageLimit: usageLimit ? Number(usageLimit) : null,
        userUsageLimit: userUsageLimit ? Number(userUsageLimit) : null,
        startDate: fromInputDatetime(startDate),
        endDate: fromInputDatetime(endDate),
      });
      onCreated();
    } catch (err) {
      const parsed = parseApiError(err);
      setError(parsed.general);
      setFieldErrors(parsed.fields);
    } finally {
      setSaving(false);
    }
  }

  return (
    <Modal title="Tạo coupon mới" onClose={onClose}>
      <form onSubmit={handleSubmit} className="space-y-3 max-h-[70vh] overflow-y-auto pr-1">
        {error && <p className="text-danger text-sm">{error}</p>}

        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
            Mã coupon <span className="text-danger">*</span>
          </label>
          <input
            type="text"
            value={code}
            onChange={e => setCode(e.target.value.toUpperCase())}
            required
            placeholder="VD: SUMMER30"
            className="w-full border border-line rounded-sm px-3 py-2 text-sm font-body focus:outline-none focus:border-ink"
          />
          {fieldErrors.code && <p className="text-danger text-xs mt-1">{fieldErrors.code}</p>}
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
              Loại giảm <span className="text-danger">*</span>
            </label>
            <select
              value={discountType}
              onChange={e => setDiscountType(e.target.value as DiscountType)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink bg-white"
            >
              <option value="PERCENTAGE">Phần trăm (%)</option>
              <option value="FIXED_AMOUNT">Số tiền cố định</option>
            </select>
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">
              Giá trị <span className="text-danger">*</span>
              <span className="ml-1 normal-case font-normal">
                {discountType === 'PERCENTAGE' ? '(%)' : '(₫)'}
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
        </div>

        <div className="grid grid-cols-2 gap-3">
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
              placeholder={discountType === 'PERCENTAGE' ? 'Cap giảm giá' : 'Không giới hạn'}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
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
            {saving ? 'Đang lưu...' : 'Tạo coupon'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
