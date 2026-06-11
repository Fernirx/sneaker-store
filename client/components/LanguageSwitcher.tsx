'use client';

import { useLocale } from 'next-intl';
import { useRouter, usePathname } from '@/i18n/routing';
import { useState, useTransition, useRef, useEffect } from 'react';

const LOCALES = [
  { code: 'vi', label: 'Tiếng Việt', short: 'VI' },
  { code: 'en', label: 'English', short: 'EN' },
] as const;

export default function LanguageSwitcher() {
  const locale = useLocale();
  const router = useRouter();
  const pathname = usePathname();
  const [pending, start] = useTransition();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const current = LOCALES.find(l => l.code === locale) ?? LOCALES[0];

  function select(code: string) {
    setOpen(false);
    if (code === locale) return;
    start(() => router.replace(pathname, { locale: code as 'vi' | 'en' }));
  }

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(v => !v)}
        disabled={pending}
        className="flex items-center gap-1.5 font-mono text-[11px] font-semibold tracking-widest uppercase border border-line rounded px-2.5 py-1 text-ink-2 hover:border-ink hover:text-ink transition-colors disabled:opacity-50"
      >
        {current.short}
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
          className={`transition-transform ${open ? 'rotate-180' : ''}`}>
          <path d="M6 9l6 6 6-6"/>
        </svg>
      </button>

      {open && (
        <div className="absolute right-0 mt-1.5 w-36 bg-white border border-line rounded shadow-lg overflow-hidden z-50">
          {LOCALES.map(l => (
            <button key={l.code} onClick={() => select(l.code)}
              className={`w-full flex items-center gap-2.5 px-3 py-2 text-[13px] hover:bg-paper transition-colors ${
                l.code === locale ? 'font-semibold text-ink' : 'text-ink-2'
              }`}>
              <span className="font-mono text-[10px] tracking-widest uppercase w-5">{l.short}</span>
              <span>{l.label}</span>
              {l.code === locale && (
                <svg className="ml-auto" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M20 6L9 17l-5-5"/>
                </svg>
              )}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
