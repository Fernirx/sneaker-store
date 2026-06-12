'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

const ICONS = {
  dashboard:   <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>,
  profile:     <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>,
  users:       <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>,
  customers:   <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><line x1="12" y1="14" x2="12" y2="21"/></svg>,
  brands:      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/><line x1="7" y1="7" x2="7.01" y2="7"/></svg>,
  categories:  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><path d="M3 14h7v7H3z"/></svg>,
  collections: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></svg>,
};

function NavItem({ href, label, icon, active }: { href: string; label: string; icon: React.ReactNode; active: boolean }) {
  return (
    <Link href={href}
      className={`flex items-center gap-2.5 px-3 py-2 rounded text-sm transition-colors ${
        active ? 'bg-accent text-white font-semibold' : 'text-ink-2 hover:bg-paper hover:text-ink'
      }`}>
      {icon}{label}
    </Link>
  );
}

export default function Sidebar() {
  const pathname = usePathname();
  function isActive(href: string) {
    return pathname === href || pathname.startsWith(href + '/');
  }

  return (
    <aside className="w-56 shrink-0 border-r border-line bg-white min-h-screen flex flex-col">
      <div className="h-14 flex items-center gap-2 px-5 border-b border-line">
        <span className="w-2.5 h-2.5 bg-accent rounded-xs rotate-45 shrink-0" />
        <span className="font-display font-black text-lg uppercase tracking-tight">STRIDE</span>
        <span className="ml-auto font-mono text-[9px] tracking-widest uppercase text-muted">Admin</span>
      </div>

      <nav className="flex-1 p-3 space-y-0.5">
        <NavItem href="/admin/dashboard" label="Tổng quan"   icon={ICONS.dashboard} active={isActive('/admin/dashboard')} />
        <NavItem href="/admin/profile"   label="Trang cá nhân" icon={ICONS.profile} active={isActive('/admin/profile')} />

        <div className="pt-4">
          <p className="font-mono text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
            Tài khoản
          </p>
          <div className="space-y-0.5">
            <NavItem href="/admin/users"     label="Người dùng" icon={ICONS.users}     active={isActive('/admin/users')} />
            <NavItem href="/admin/customers" label="Khách hàng" icon={ICONS.customers} active={isActive('/admin/customers')} />
          </div>
        </div>

        <div className="pt-4">
          <p className="font-mono text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
            Catalog
          </p>
          <div className="space-y-0.5">
            <NavItem href="/admin/brands"      label="Thương hiệu"   icon={ICONS.brands}      active={isActive('/admin/brands')} />
            <NavItem href="/admin/categories"  label="Danh mục"      icon={ICONS.categories}  active={isActive('/admin/categories')} />
            <NavItem href="/admin/collections" label="Bộ sưu tập"    icon={ICONS.collections} active={isActive('/admin/collections')} />
          </div>
        </div>
      </nav>
    </aside>
  );
}
