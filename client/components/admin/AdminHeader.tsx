'use client';

import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { useTransition } from 'react';
import { useNotificationUnreadCount } from '@/hooks/useNotificationUnreadCount';

interface Props {
  firstName?: string;
}

export default function AdminHeader({ firstName }: Props) {
  const [pending, start] = useTransition();
  const unreadCount = useNotificationUnreadCount('/api/admin/notifications');

  function handleLogout() {
    start(async () => {
      await clientAxios.post('/api/auth/logout').catch(() => {});
      window.location.href = '/login';
    });
  }

  return (
    <header className="h-14 border-b border-line bg-white flex items-center justify-end gap-3 px-6">
      <Link href="/admin/notifications" title="Thông báo"
        className="relative p-2 rounded-full text-ink-2 hover:text-ink hover:bg-paper transition-colors">
        <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        {unreadCount > 0 && (
          <span className="absolute top-0.5 right-0.5 min-w-[15px] h-[15px] bg-accent text-white text-[9px] font-bold rounded-full flex items-center justify-center px-1 leading-none">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </Link>

      <div className="w-px h-4 bg-line" />

      <span className="text-sm text-ink-2">
        {firstName ?? 'Admin'}
      </span>

      <button onClick={handleLogout} disabled={pending}
        className="flex items-center gap-1.5 text-sm text-ink-2 hover:text-danger transition-colors disabled:opacity-40">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Đăng xuất
      </button>
    </header>
  );
}
