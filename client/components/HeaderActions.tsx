'use client';

import { useEffect, useState, useTransition } from 'react';
import Link from 'next/link';
import clientAxios from '@/lib/axios/clientAxios';
import { avatarUrl } from '@/lib/cloudinaryUrl';
import { useCart } from '@/contexts/CartContext';
import { useNotificationUnreadCount } from '@/hooks/useNotificationUnreadCount';

interface Props {
  isLoggedIn: boolean;
  firstName?: string;
  avatarPublicId?: string;
}

function NotificationBellLink() {
  const unreadCount = useNotificationUnreadCount('/api/notifications');
  return (
    <Link href="/notifications" title="Thông báo"
      className="relative p-2.5 rounded-full text-ink-2 hover:text-ink hover:bg-paper transition-colors">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      {unreadCount > 0 && (
        <span className="absolute top-1 right-1 min-w-[16px] h-4 bg-accent text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1 leading-none">
          {unreadCount > 99 ? '99+' : unreadCount}
        </span>
      )}
    </Link>
  );
}

export default function HeaderActions({ isLoggedIn, firstName, avatarPublicId }: Props) {
  const [pending, start] = useTransition();
  const [localFirstName, setLocalFirstName] = useState(firstName);
  const [localAvatarPublicId, setLocalAvatarPublicId] = useState(avatarPublicId);

  const { cart } = useCart();
  const itemCount = cart?.totalItems ?? 0;

  useEffect(() => {
    setLocalFirstName(firstName);
    setLocalAvatarPublicId(avatarPublicId);
  }, [firstName, avatarPublicId]);

  useEffect(() => {
    function handler(e: Event) {
      const detail = (e as CustomEvent).detail ?? {};
      if ('firstName' in detail) setLocalFirstName(detail.firstName as string);
      if ('avatarPublicId' in detail) setLocalAvatarPublicId(detail.avatarPublicId as string | undefined);
    }
    window.addEventListener('profile-updated', handler);
    return () => window.removeEventListener('profile-updated', handler);
  }, []);

  function handleLogout() {
    start(async () => {
      await clientAxios.post('/api/auth/logout').catch(() => {});
      window.location.href = '/';
    });
  }

  return (
    <div className="flex items-center gap-2">
      {/* Cart icon — shown for everyone */}
      <Link href="/cart" title="Giỏ hàng"
        className="relative p-2.5 rounded-full text-ink-2 hover:text-ink hover:bg-paper transition-colors">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/>
        </svg>
        {itemCount > 0 && (
          <span className="absolute top-1 right-1 min-w-[16px] h-4 bg-accent text-white text-[10px] font-bold rounded-full flex items-center justify-center px-1 leading-none">
            {itemCount > 99 ? '99+' : itemCount}
          </span>
        )}
      </Link>

      {isLoggedIn ? (
        <>
          <NotificationBellLink />

          <Link href="/orders" title="Đơn hàng"
            className="p-2.5 rounded-full text-ink-2 hover:text-ink hover:bg-paper transition-colors">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/><rect x="9" y="3" width="6" height="4" rx="1"/><path d="M9 12h6M9 16h4"/>
            </svg>
          </Link>

          <div className="w-px h-4 bg-line mx-1" />

          <Link href="/profile" title={localFirstName ?? "Tài khoản"}
            className="p-1.5 rounded-full text-ink hover:bg-paper transition-colors">
            {localAvatarPublicId ? (
              <img
                src={avatarUrl(localAvatarPublicId, 48)}
                alt="avatar"
                className="w-7 h-7 rounded-full object-cover ring-1 ring-line"
              />
            ) : (
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" className="m-1">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
              </svg>
            )}
          </Link>

          <button onClick={handleLogout} disabled={pending} title="Đăng xuất"
            className="p-2.5 rounded-full text-ink-2 hover:text-danger hover:bg-danger-bg transition-colors disabled:opacity-40 cursor-pointer">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </>
      ) : (
        <Link href="/login"
          className="font-display font-bold text-[13px] uppercase tracking-wider bg-ink text-white px-4 py-2 rounded hover:bg-ink/80 transition-colors">
          {"Đăng nhập"}
        </Link>
      )}
    </div>
  );
}
