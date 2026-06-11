'use client';

import { useRouter } from '@/i18n/routing';
import axios from 'axios';
import { useTransition } from 'react';
import { useTranslations } from 'next-intl';
import LanguageSwitcher from '@/components/LanguageSwitcher';

interface Props {
  firstName?: string;
}

export default function AdminHeader({ firstName }: Props) {
  const router = useRouter();
  const t = useTranslations('admin');
  const [pending, start] = useTransition();

  function handleLogout() {
    start(async () => {
      await axios.post('/api/auth/logout').catch(() => {});
      router.replace('/login');
    });
  }

  return (
    <header className="h-14 border-b border-line bg-white flex items-center justify-end gap-3 px-6">
      <LanguageSwitcher />

      <div className="w-px h-4 bg-line" />

      <span className="text-sm text-ink-2">
        {firstName ?? 'Admin'}
      </span>

      <button onClick={handleLogout} disabled={pending}
        className="flex items-center gap-1.5 text-sm text-ink-2 hover:text-danger transition-colors disabled:opacity-40">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        {t('logout')}
      </button>
    </header>
  );
}
