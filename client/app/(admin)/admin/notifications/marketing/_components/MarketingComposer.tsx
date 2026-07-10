'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import TiptapEditor from './TiptapEditor';
import CustomerPicker from './CustomerPicker';
import type { NotificationType } from '../../_components/types';

const TYPE_OPTIONS: { value: NotificationType; label: string }[] = [
  { value: 'PROMOTION', label: 'Khuyến mãi / Mã giảm giá' },
  { value: 'PRODUCT', label: 'Sản phẩm mới / Giảm giá' },
  { value: 'SYSTEM', label: 'Thông báo chung' },
];

interface CustomerOption { id: number; email: string; firstName: string; lastName?: string; }

export default function MarketingComposer() {
  const router = useRouter();
  const [type, setType] = useState<NotificationType>('PROMOTION');
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [link, setLink] = useState('');
  const [targetType, setTargetType] = useState<'ALL' | 'USER'>('ALL');
  const [customers, setCustomers] = useState<CustomerOption[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!title.trim()) { setError('Vui lòng nhập tiêu đề.'); return; }
    if (!message.trim()) { setError('Vui lòng nhập nội dung.'); return; }
    if (targetType === 'USER' && customers.length === 0) { setError('Vui lòng chọn ít nhất 1 khách hàng.'); return; }

    setSubmitting(true);
    try {
      await clientAxios.post('/api/admin/notifications/marketing', {
        type,
        title: title.trim(),
        message,
        targetType,
        targetCustomerIds: targetType === 'USER' ? customers.map(c => c.id) : undefined,
        link: link.trim() || undefined,
      });
      setSuccess('Đã gửi thông báo thành công.');
      setTitle('');
      setMessage('');
      setLink('');
      setCustomers([]);
      router.refresh();
    } catch (err) {
      const { general } = parseApiError(err);
      setError(general);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 bg-white border border-line rounded-sm p-5">
      <h2 className="font-display font-bold text-sm uppercase tracking-wide text-ink">Soạn thông báo mới</h2>

      {error && <p className="text-[12px] text-danger bg-danger/5 border border-danger/20 rounded-sm px-3 py-2.5">{error}</p>}
      {success && <p className="text-[12px] text-ok bg-ok-bg border border-ok/20 rounded-sm px-3 py-2.5">{success}</p>}

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-xs font-bold text-ink-2 mb-1.5">Loại thông báo</label>
          <select
            value={type}
            onChange={e => setType(e.target.value as NotificationType)}
            className="w-full border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          >
            {TYPE_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
        </div>
        <div>
          <label className="block text-xs font-bold text-ink-2 mb-1.5">Đối tượng nhận</label>
          <select
            value={targetType}
            onChange={e => setTargetType(e.target.value as 'ALL' | 'USER')}
            className="w-full border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
          >
            <option value="ALL">Toàn bộ khách hàng</option>
            <option value="USER">Khách hàng cụ thể</option>
          </select>
        </div>
      </div>

      {targetType === 'USER' && (
        <div>
          <label className="block text-xs font-bold text-ink-2 mb-1.5">Chọn khách hàng</label>
          <CustomerPicker selected={customers} onChange={setCustomers} />
        </div>
      )}

      <div>
        <label className="block text-xs font-bold text-ink-2 mb-1.5">Tiêu đề</label>
        <input
          type="text"
          value={title}
          onChange={e => setTitle(e.target.value)}
          maxLength={255}
          className="w-full border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        />
      </div>

      <div>
        <label className="block text-xs font-bold text-ink-2 mb-1.5">Nội dung</label>
        <TiptapEditor value={message} onChange={setMessage} />
      </div>

      <div>
        <label className="block text-xs font-bold text-ink-2 mb-1.5">Liên kết (tùy chọn)</label>
        <input
          type="text"
          value={link}
          onChange={e => setLink(e.target.value)}
          placeholder="/products/air-max-1 hoặc /coupons"
          maxLength={500}
          className="w-full border border-line bg-white rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
        />
      </div>

      <button
        type="submit"
        disabled={submitting}
        className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors disabled:opacity-50"
      >
        {submitting ? 'Đang gửi...' : 'Gửi thông báo'}
      </button>
    </form>
  );
}
