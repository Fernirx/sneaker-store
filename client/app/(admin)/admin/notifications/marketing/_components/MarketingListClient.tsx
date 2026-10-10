'use client';

import { useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDateTime, TYPE_LABELS, TARGET_TYPE_LABELS, type NotificationInternalResponse, type PageResult } from '../../_components/types';
import MarketingComposer from './MarketingComposer';

export default function MarketingListClient({ initialData }: { initialData: PageResult<NotificationInternalResponse> }) {
  const [result, setResult] = useState(initialData);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  async function fetchData(page: number) {
    setLoading(true);
    try {
      const { data } = await clientAxios.get(`/api/admin/notifications/marketing?page=${page}&size=20&sort=createdAt,desc`);
      setResult(data);
    } finally {
      setLoading(false);
    }
  }

  async function toggleActive(n: NotificationInternalResponse) {
    setResult(r => ({ ...r, data: r.data.map(x => x.id === n.id ? { ...x, active: !x.active } : x) }));
    await clientAxios.patch(`/api/admin/notifications/marketing/${n.id}/active?active=${!n.active}`).catch(() => {
      setResult(r => ({ ...r, data: r.data.map(x => x.id === n.id ? { ...x, active: n.active } : x) }));
    });
  }

  const notifications = result.data;
  const meta = result.meta;

  return (
    <div className="space-y-6">
      <MarketingComposer />

      <div className="space-y-3">
        <h2 className="font-display font-bold text-sm uppercase tracking-wide text-ink">Lịch sử đã gửi</h2>

        <div className={`bg-white border border-line rounded-sm overflow-x-auto transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
          <table className="w-full text-sm table-fixed">
            <thead>
              <tr className="border-b border-line bg-paper">
                <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap w-[35%]">Tiêu đề</th>
                <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap w-[15%]">Loại</th>
                <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap w-[15%]">Đối tượng</th>
                <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap w-[20%]">Thời gian gửi</th>
                <th className="text-left px-4 py-3 font-display font-bold text-[11px] uppercase tracking-wide text-muted whitespace-nowrap w-[15%]">Trạng thái</th>
                <th className="px-4 py-3 w-24" />
              </tr>
            </thead>
            <tbody>
              {notifications.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-14 text-muted text-sm">Chưa gửi thông báo nào.</td></tr>
              ) : (
                notifications.map(n => (
                  <tr key={n.id} className="border-b border-line-2 last:border-0 hover:bg-paper/50 transition-colors">
                    <td className="px-4 py-3 font-medium text-sm truncate">{n.title}</td>
                    <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">{TYPE_LABELS[n.type]}</td>
                    <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">{TARGET_TYPE_LABELS[n.targetType]}</td>
                    <td className="px-4 py-3 text-xs text-muted whitespace-nowrap">{formatDateTime(n.createdAt)}</td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${n.active ? 'bg-ok-bg text-ok' : 'bg-line-2 text-muted'}`}>
                        {n.active ? 'Đang hiển thị' : 'Đã ẩn'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => toggleActive(n)}
                        className="text-xs font-bold text-muted hover:text-ink transition-colors"
                      >
                        {n.active ? 'Ẩn' : 'Hiện'}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {meta.totalPages > 1 && (
          <div className="flex items-center justify-between text-sm">
            <span className="text-muted text-xs">{meta.totalElements} thông báo</span>
            <div className="flex items-center gap-2">
              <button
                disabled={currentPage === 0}
                onClick={() => { setCurrentPage(p => p - 1); fetchData(currentPage - 1); }}
                className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
              >
                Trước
              </button>
              <span className="px-2 text-xs text-muted">{currentPage + 1} / {meta.totalPages}</span>
              <button
                disabled={meta.last}
                onClick={() => { setCurrentPage(p => p + 1); fetchData(currentPage + 1); }}
                className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
              >
                Tiếp
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
