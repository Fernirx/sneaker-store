'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import RichText from '@/components/RichText';
import { formatDateTime, TYPE_LABELS, type NotificationResponse } from '../../_components/types';

function DetailSkeleton() {
  return (
    <div className="max-w-2xl mx-auto px-4 py-10 space-y-4">
      <div className="h-6 w-40 bg-line rounded-sm animate-pulse" />
      <div className="h-64 bg-line rounded-sm animate-pulse" />
    </div>
  );
}

export default function NotificationDetailClient({ notificationId }: { notificationId: number }) {
  const [notification, setNotification] = useState<NotificationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let cancelled = false;
    clientAxios.get(`/api/notifications/${notificationId}`)
      .then(({ data }) => {
        if (cancelled) return;
        setNotification(data.data);
        clientAxios.patch(`/api/notifications/${notificationId}/read`).catch(() => {});
      })
      .catch(() => { if (!cancelled) setNotFound(true); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [notificationId]);

  if (loading) return <DetailSkeleton />;

  if (notFound || !notification) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-28 flex flex-col items-center gap-5">
        <p className="text-muted text-[14px]">{"Không tìm thấy thông báo."}</p>
        <Link href="/notifications" className="text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors">
          {"Quay lại danh sách"}
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <div className="flex items-center gap-3 mb-1">
        <Link href="/notifications" className="text-muted hover:text-ink transition-colors">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M11 6l-6 6 6 6"/>
          </svg>
        </Link>
        <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide bg-line-2 text-ink-2">
          {TYPE_LABELS[notification.type]}
        </span>
      </div>
      <p className="text-[12px] text-muted mb-4 ml-7">{formatDateTime(notification.createdAt)}</p>

      <h1 className="font-display font-black text-2xl uppercase tracking-tight mb-6">{notification.title}</h1>

      {/* Server sanitize title+message cho mọi nguồn (NotificationServiceImpl.create()) trước khi lưu DB. */}
      <RichText html={notification.message} className="text-[14px] leading-relaxed text-ink-2" />

      {notification.link && (
        <Link
          href={notification.link}
          className="inline-block mt-6 text-[12px] font-bold uppercase tracking-widest bg-ink text-white px-6 py-3 rounded-sm hover:bg-accent transition-colors"
        >
          {"Xem chi tiết"}
        </Link>
      )}
    </div>
  );
}
