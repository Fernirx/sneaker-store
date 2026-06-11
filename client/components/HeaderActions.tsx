'use client';

import { useRouter } from '@/i18n/routing';
import { Link } from '@/i18n/routing';
import axios from 'axios';
import { useTransition } from 'react';

interface Props {
  isLoggedIn: boolean;
  firstName?: string;
}

export default function HeaderActions({ isLoggedIn, firstName }: Props) {
  const router = useRouter();
  const [pending, start] = useTransition();

  function handleLogout() {
    start(async () => {
      await axios.post('/api/auth/logout').catch(() => {});
      router.replace('/');
      router.refresh();
    });
  }

  return (
    <div className="flex items-center gap-1">
      {isLoggedIn ? (
        <>
          <Link href="/orders"
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm text-ink-2 hover:text-ink hover:bg-paper transition-colors">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
            <span className="hidden sm:inline text-xs font-semibold tracking-wide">Đơn hàng</span>
          </Link>

          <Link href="/wishlist"
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm text-ink-2 hover:text-ink hover:bg-paper transition-colors">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span className="hidden sm:inline text-xs font-semibold tracking-wide">Yêu thích</span>
          </Link>

          <div className="w-px h-4 bg-line mx-1" />

          <Link href="/account"
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm font-semibold text-ink hover:bg-paper transition-colors">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
            </svg>
            <span className="hidden sm:inline text-xs">{firstName ?? 'Tài khoản'}</span>
          </Link>

          <button onClick={handleLogout} disabled={pending}
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm text-ink-2 hover:text-danger hover:bg-danger-bg transition-colors disabled:opacity-40">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <span className="hidden sm:inline text-xs font-semibold tracking-wide">Đăng xuất</span>
          </button>
        </>
      ) : (
        <Link href="/login"
          className="font-display font-bold text-[13px] uppercase tracking-wider bg-ink text-white px-4 py-2 rounded hover:bg-ink/80 transition-colors">
          Đăng nhập
        </Link>
      )}
    </div>
  );
}
