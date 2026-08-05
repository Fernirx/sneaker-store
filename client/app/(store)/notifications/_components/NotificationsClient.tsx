'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { formatDateTime, TYPE_LABELS, type NotificationResponse, type PageMeta } from './types';

function NotificationsSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="h-8 w-48 bg-line rounded-sm animate-pulse mb-8" />
      <div className="space-y-3">
        {[1, 2, 3].map(i => (
          <div key={i} className="h-20 bg-line rounded-sm animate-pulse" />
        ))}
      </div>
    </div>
  );
}

export default function NotificationsClient({ isLoggedIn }: { isLoggedIn: boolean }) {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [meta, setMeta] = useState<PageMeta | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn) {
      setNotifications([]);
      setMeta(null);
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    clientAxios
      .get(`/api/notifications?page=${page}&size=10&sort=createdAt,desc`)
      .then(({ data }) => {
        if (cancelled) return;
        setNotifications(data.data ?? []);
        setMeta(data.meta ?? null);
      })
      .catch(() => {
        if (!cancelled) { setNotifications([]); setMeta(null); }
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [isLoggedIn, page]);

  async function markAsRead(id: number) {
    setNotifications(list => list.map(n => n.id === id ? { ...n, read: true } : n));
    await clientAxios.patch(`/api/notifications/${id}/read`).catch(() => {});
    window.dispatchEvent(new Event('notification-read'));
  }

  if (loading) return <NotificationsSkeleton />;

  if (notifications.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2" className="text-line">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        <p className="text-muted text-[14px]">{"Bạn chưa có thông báo nào."}</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <h1 className="font-display font-black text-3xl uppercase tracking-tight mb-8">{"Thông báo"}</h1>

      <div className="space-y-3">
        {notifications.map(n => (
          <Link
            key={n.id}
            href={`/notifications/${n.id}`}
            onClick={() => markAsRead(n.id)}
            className={`block border rounded-sm p-4 hover:border-ink transition-colors ${!n.read ? 'border-accent/30 bg-accent/5' : 'border-line'}`}
          >
            <div className="flex items-center justify-between gap-3 mb-2">
              <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide bg-line-2 text-ink-2">
                {TYPE_LABELS[n.type]}
              </span>
              {!n.read && <span className="w-1.5 h-1.5 rounded-full bg-accent shrink-0" />}
            </div>
            <p className={`text-[14px] ${!n.read ? 'font-bold text-ink' : 'text-ink-2'}`}>{n.title}</p>
            <p className="text-[12px] text-muted mt-1">{formatDateTime(n.createdAt)}</p>
          </Link>
        ))}
      </div>

      {meta && meta.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm mt-6">
          <span className="text-muted text-xs">{meta.totalElements}</span>
          <div className="flex items-center gap-2">
            <button
              disabled={page === 0}
              onClick={() => setPage(p => p - 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              ←
            </button>
            <span className="px-2 text-xs text-muted">{page + 1} / {meta.totalPages}</span>
            <button
              disabled={meta.last}
              onClick={() => setPage(p => p + 1)}
              className="px-3 py-1.5 border border-line rounded-sm text-xs font-bold disabled:opacity-40 hover:bg-paper transition-colors"
            >
              →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
