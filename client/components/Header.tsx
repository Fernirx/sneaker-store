import { Link } from '@/i18n/routing';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import HeaderActions from './HeaderActions';

interface ProfileResponse {
  firstName: string;
  lastName?: string;
  avatarPublicId?: string;
}

async function getProfile(): Promise<ProfileResponse | null> {
  try {
    const api = await createServerAxios();
    const { data } = await api.get('/me');
    return data.data as ProfileResponse;
  } catch {
    return null;
  }
}

export default async function Header() {
  const session = await getSession();
  const profile = session ? await getProfile() : null;

  return (
    <header className="sticky top-0 z-40 bg-white/90 backdrop-blur border-b border-line">
      <div className="max-w-7xl mx-auto px-6 h-14 flex items-center gap-4">

        {/* Logo */}
        <Link href="/" className="flex items-center gap-2 shrink-0">
          <span className="w-2.5 h-2.5 bg-accent rounded-xs rotate-45" />
          <span className="font-display font-black text-lg uppercase tracking-tight">STRIDE</span>
        </Link>

        {/* Search bar — desktop */}
        <Link href="/search"
          className="flex-1 max-w-sm hidden md:flex items-center gap-2 border border-line rounded px-3 py-2 text-sm text-muted hover:border-ink transition-colors">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <span className="text-xs">Tìm kiếm sản phẩm...</span>
        </Link>

        <div className="flex-1" />

        {/* Search icon — mobile */}
        <Link href="/search" className="md:hidden p-2 text-ink-2 hover:text-ink transition-colors">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </Link>

        <HeaderActions
          isLoggedIn={!!session}
          firstName={profile?.firstName}
          avatarPublicId={profile?.avatarPublicId}
        />
      </div>
    </header>
  );
}
