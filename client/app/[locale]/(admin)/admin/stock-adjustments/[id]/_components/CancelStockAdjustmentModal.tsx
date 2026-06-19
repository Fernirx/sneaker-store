'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Modal from '@/components/admin/Modal';
import { type StockAdjustmentResponse } from '../../_components/types';

export default function CancelStockAdjustmentModal({
  adjustmentId,
  onClose,
  onCancelled,
}: {
  adjustmentId: number;
  onClose: () => void;
  onCancelled: (adjustment: StockAdjustmentResponse) => void;
}) {
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function handleConfirm() {
    setSaving(true);
    setError('');
    try {
      const { data } = await clientAxios.patch(`/api/admin/stock-adjustments/${adjustmentId}/cancel`, {
        reason: reason.trim() || null,
      });
      onCancelled(data.data);
    } catch (err) {
      setError(parseApiError(err).general);
      setSaving(false);
    }
  }

  return (
    <Modal title="Hủy phiếu điều chỉnh kho" onClose={onClose}>
      <div className="space-y-4">
        {error && <p className="text-danger text-sm">{error}</p>}
        <p className="text-sm">Bạn có chắc muốn hủy phiếu điều chỉnh kho này?</p>
        <div>
          <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Lý do (không bắt buộc)</label>
          <textarea
            value={reason}
            onChange={e => setReason(e.target.value)}
            rows={3}
            className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
          />
        </div>
        <div className="flex justify-end gap-2">
          <button onClick={onClose} className="px-4 py-2 border border-line text-sm rounded-sm hover:bg-paper transition-colors">
            Đóng
          </button>
          <button
            onClick={handleConfirm}
            disabled={saving}
            className="px-4 py-2 bg-danger text-white text-sm font-bold rounded-sm hover:opacity-90 disabled:opacity-60 transition-opacity"
          >
            {saving ? 'Đang hủy...' : 'Xác nhận hủy'}
          </button>
        </div>
      </div>
    </Modal>
  );
}
