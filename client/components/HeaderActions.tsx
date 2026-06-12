'use client';

import { useEffect, useState, useTransition } from 'react';
import { Link } from '@/i18n/routing';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import { avatarUrl } from '@/lib/cloudinaryUrl';

interface Props {
  isLoggedIn: boolean;
  firstName?: string;
  avatarPublicId?: string;
}

export default function HeaderActions({ isLoggedIn, firstName, avatarPublicId }: Props) {
  const t = useTranslations('header');
  const [pending, start] = useTransition();
  const [localFirstName, setLocalFirstName] = useState(firstName);
  const [localAvatarPublicId, setLocalAvatarPublicId] = useState(avatarPublicId);

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
    <div className="flex items-center gap-1">
      {isLoggedIn ? (
        <>
          <Link href="/orders"
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm text-ink-2 hover:text-ink hover:bg-paper transition-colors">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
            <span className="hidden sm:inline text-xs font-semibold tracking-wide">{t('orders')}</span>
          </Link>

          <Link href="/wishlist"
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm text-ink-2 hover:text-ink hover:bg-paper transition-colors">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <span className="hidden sm:inline text-xs font-semibold tracking-wide">{t('wishlist')}</span>
          </Link>

          <div className="w-px h-4 bg-line mx-1" />

          <Link href="/profile"
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm font-semibold text-ink hover:bg-paper transition-colors">
            {localAvatarPublicId ? (
              <img
                src={avatarUrl(localAvatarPublicId, 48)}
                alt="avatar"
                className="w-6 h-6 rounded-full object-cover ring-1 ring-line"
              />
            ) : (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
              </svg>
            )}
            <span className="hidden sm:inline text-xs">{localFirstName ?? t('account')}</span>
          </Link>

          <button onClick={handleLogout} disabled={pending}
            className="flex items-center gap-1.5 px-3 py-2 rounded text-sm text-ink-2 hover:text-danger hover:bg-danger-bg transition-colors disabled:opacity-40">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
            <span className="hidden sm:inline text-xs font-semibold tracking-wide">{t('logout')}</span>
          </button>
        </>
      ) : (
        <Link href="/login"
          className="font-display font-bold text-[13px] uppercase tracking-wider bg-ink text-white px-4 py-2 rounded hover:bg-ink/80 transition-colors">
          {t('login')}
        </Link>
      )}
    </div>
  );
}
