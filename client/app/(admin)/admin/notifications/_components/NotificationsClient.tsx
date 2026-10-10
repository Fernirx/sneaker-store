'use client';

import { useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDateTime, TYPE_LABELS, type NotificationResponse, type PageResult } from './types';

export default function NotificationsClient({ initialData }: { initialData: PageResult<NotificationResponse> }) {
  const [result, setResult] = useState(initialData);
  const [currentPage, setCurrentPage] = useState(0);
  const [loading, setLoading] = useState(false);

  async function fetchData(page: number) {
    setLoading(true);
    try {
      const { data } = await clientAxios.get(`/api/admin/notifications?page=${page}&size=20&sort=createdAt,desc`);
      setResult(data);
    } finally {
      setLoading(false);
    }
  }

  function goToPage(page: number) {
    setCurrentPage(page);
    fetchData(page);
  }

  async function markAsRead(notification: NotificationResponse) {
    if (notification.read) return;
    setResult(r => ({ ...r, data: r.data.map(n => n.id === notification.id ? { ...n, read: true } : n) }));
    await clientAxios.patch(`/api/admin/notifications/${notification.id}/read`).catch(() => {});
    window.dispatchEvent(new Event('notification-read'));
  }

  const notifications = result.data;
  const meta = result.meta;

  return (
    <div className="space-y-5">
      <h1 className="font-display font-black text-xl uppercase tracking-tight">Thông báo</h1>

      <div className={`bg-white border border-line rounded-sm overflow-hidden transition-opacity duration-150 ${loading ? 'opacity-60 pointer-events-none' : ''}`}>
        {notifications.length === 0 ? (
          <p className="text-center py-14 text-muted text-sm">Không có thông báo nào.</p>
        ) : (
          notifications.map(n => {
            const content = (
              <div className={`px-4 py-3.5 border-b border-line-2 last:border-0 flex items-start gap-3 hover:bg-paper/50 transition-colors ${!n.read ? 'bg-accent/5' : ''}`}>
                <span className={`w-1.5 h-1.5 rounded-full mt-1.5 shrink-0 ${!n.read ? 'bg-accent' : 'bg-transparent'}`} />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-line-2 text-ink-2">{TYPE_LABELS[n.type]}</span>
                    <p className={`text-sm ${!n.read ? 'font-bold text-ink' : 'text-ink-2'}`}>{n.title}</p>
                  </div>
                  <p className="text-xs text-muted mt-1">{formatDateTime(n.createdAt)}</p>
                </div>
              </div>
            );
            return n.link ? (
              <Link key={n.id} href={n.link} onClick={() => markAsRead(n)}>{content}</Link>
            ) : (
              <div key={n.id} onClick={() => markAsRead(n)} className="cursor-pointer">{content}</div>
            );
          })
        )}
      </div>

      {meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted text-xs">{meta.totalElements} thông báo</span>
          <div className="flex items-center gap-2">
            <button
              disabled={currentPage === 0}
              onClick={() => goToPage(currentPage - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Trước
            </button>
            <span className="px-2 text-xs text-muted">{currentPage + 1} / {meta.totalPages}</span>
            <button
              disabled={meta.last}
              onClick={() => goToPage(currentPage + 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              Tiếp
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
