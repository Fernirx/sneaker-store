import { getTranslations } from 'next-intl/server';
import { Link } from '@/i18n/routing';

const SOCIALS = [
  {
    label: 'Facebook',
    href: '#',
    icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M22 12a10 10 0 1 0-11.6 9.9v-7H7.9V12h2.5V9.8c0-2.5 1.5-3.9 3.8-3.9 1.1 0 2.2.2 2.2.2v2.5h-1.2c-1.2 0-1.6.8-1.6 1.6V12h2.7l-.4 2.9h-2.3v7A10 10 0 0 0 22 12z"/></svg>,
  },
  {
    label: 'Instagram',
    href: '#',
    icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="2" width="20" height="20" rx="5"/><circle cx="12" cy="12" r="4"/><circle cx="17.5" cy="6.5" r="1" fill="currentColor" stroke="none"/></svg>,
  },
  {
    label: 'TikTok',
    href: '#',
    icon: <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M19.6 3h-3v9.6a2.4 2.4 0 1 1-2.4-2.4c.22 0 .43.03.63.08V7.1a5.8 5.8 0 1 0 5.17 5.7V8.2a8.6 8.6 0 0 0 5 1.58V6.4a5.62 5.62 0 0 1-5.4-3.4z"/></svg>,
  },
];

export default async function Footer() {
  const t = await getTranslations('footer');

  const LINKS = [
    {
      title: t('colBrand'),
      items: [
        { label: t('about'), href: '/about' },
        { label: t('careers'), href: '/careers' },
        { label: t('news'), href: '/news' },
      ],
    },
    {
      title: t('colSupport'),
      items: [
        { label: t('buyingGuide'), href: '/help/buying' },
        { label: t('returns'), href: '/help/returns' },
        { label: t('tracking'), href: '/help/tracking' },
        { label: t('contact'), href: '/contact' },
      ],
    },
    {
      title: t('colLegal'),
      items: [
        { label: t('terms'), href: '/legal/terms' },
        { label: t('privacy'), href: '/legal/privacy' },
        { label: t('cookies'), href: '/legal/cookies' },
      ],
    },
  ];

  return (
    <footer className="bg-ink text-white">
      <div className="max-w-7xl mx-auto px-6 pt-14 pb-8">

        {/* Top */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-10 pb-12 border-b border-white/10">

          {/* Brand */}
          <div className="col-span-2 md:col-span-1 space-y-4">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 bg-accent rounded-xs rotate-45 shrink-0" />
              <span className="font-display font-black text-xl uppercase tracking-tight">STRIDE</span>
            </div>
            <p className="text-white/50 text-sm leading-relaxed max-w-xs">
              {t('tagline')}
            </p>
            <div className="flex items-center gap-3">
              {SOCIALS.map(s => (
                <a key={s.label} href={s.href} aria-label={s.label}
                  className="text-white/40 hover:text-white transition-colors">
                  {s.icon}
                </a>
              ))}
            </div>
          </div>

          {/* Link columns */}
          {LINKS.map(({ title, items }) => (
            <div key={title}>
              <p className="font-mono text-[10px] font-semibold tracking-[0.14em] uppercase text-white/40 mb-4">
                {title}
              </p>
              <ul className="space-y-2.5">
                {items.map(item => (
                  <li key={item.href}>
                    <Link href={item.href}
                      className="text-sm text-white/60 hover:text-white transition-colors">
                      {item.label}
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        {/* Bottom */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-6 text-white/30 text-xs">
          <span>© {new Date().getFullYear()} STRIDE. All rights reserved.</span>
          <span className="font-mono tracking-wider">MADE IN VIETNAM</span>
        </div>
      </div>
    </footer>
  );
}
