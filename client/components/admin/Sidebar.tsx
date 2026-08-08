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
  products:    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>,
  coupons:     <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><line x1="19" y1="5" x2="5" y2="19"/><circle cx="6.5" cy="6.5" r="2.5"/><circle cx="17.5" cy="17.5" r="2.5"/></svg>,
  orders:      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M9 2 2 6.5V21a1 1 0 0 0 1 1h18a1 1 0 0 0 1-1V6.5L15 2z"/><path d="M2 6.5h20"/><path d="M9 11v4"/><path d="M15 11v4"/></svg>,
  reviews:     <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>,
  comments:    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>,
  suppliers:   <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="1" y="3" width="15" height="13"/><path d="M16 8h4l3 3v5h-7V8z"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/></svg>,
  purchases:   <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1"/><path d="M9 13l2 2 4-4"/></svg>,
  adjustments: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><line x1="4" y1="21" x2="4" y2="14"/><line x1="4" y1="10" x2="4" y2="3"/><line x1="12" y1="21" x2="12" y2="12"/><line x1="12" y1="8" x2="12" y2="3"/><line x1="20" y1="21" x2="20" y2="16"/><line x1="20" y1="12" x2="20" y2="3"/><line x1="1" y1="14" x2="7" y2="14"/><line x1="9" y1="8" x2="15" y2="8"/><line x1="17" y1="16" x2="23" y2="16"/></svg>,
  pricingSetting: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>,
  notifications: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>,
  marketingCompose: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.12 2.12 0 0 1 3 3L12 15l-4 1 1-4z"/></svg>,
  returns: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>,
  banners: <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><rect x="2" y="5" width="20" height="14" rx="1"/><circle cx="8.5" cy="10.5" r="1.5"/><path d="M2 16l5-5 4 4 3-3 6 6"/></svg>,
};

function hasAnyRole(roles: string[], required: string[]) {
  return required.some(r => roles.includes(r));
}

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

export default function Sidebar({ roles }: { roles: string[] }) {
  const pathname = usePathname();
  function isActive(href: string) {
    return pathname === href || pathname.startsWith(href + '/');
  }

  return (
    <aside className="w-48 shrink-0 border-r border-line bg-white min-h-screen flex flex-col">
      <div className="h-14 flex items-center gap-2 px-5 border-b border-line">
        <span className="w-2.5 h-2.5 bg-accent rounded-xs rotate-45 shrink-0" />
        <span className="font-display font-black text-lg uppercase tracking-tight">STRIDE</span>
        <span className="ml-auto font-display text-[9px] tracking-widest uppercase text-muted">Admin</span>
      </div>

      <nav className="flex-1 p-3 space-y-0.5">
        <NavItem href="/admin/dashboard" label="Tổng quan"   icon={ICONS.dashboard} active={isActive('/admin/dashboard')} />
        <NavItem href="/admin/notifications" label="Thông báo" icon={ICONS.notifications} active={isActive('/admin/notifications') && !isActive('/admin/notifications/marketing')} />
        <NavItem href="/admin/profile"   label="Trang cá nhân" icon={ICONS.profile} active={isActive('/admin/profile')} />

        {hasAnyRole(roles, ['ROLE_ADMIN', 'ROLE_SALE']) && (
          <div className="pt-4">
            <p className="font-display text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
              Tài khoản
            </p>
            <div className="space-y-0.5">
              {hasAnyRole(roles, ['ROLE_ADMIN']) && (
                <NavItem href="/admin/users" label="Người dùng" icon={ICONS.users} active={isActive('/admin/users')} />
              )}
              <NavItem href="/admin/customers" label="Khách hàng" icon={ICONS.customers} active={isActive('/admin/customers')} />
            </div>
          </div>
        )}

        <div className="pt-4">
          <p className="font-display text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
            Catalog
          </p>
          <div className="space-y-0.5">
            <NavItem href="/admin/brands"      label="Thương hiệu"   icon={ICONS.brands}      active={isActive('/admin/brands')} />
            <NavItem href="/admin/categories"  label="Danh mục"      icon={ICONS.categories}  active={isActive('/admin/categories')} />
            {hasAnyRole(roles, ['ROLE_ADMIN', 'ROLE_SALE']) && (
              <NavItem href="/admin/collections" label="Bộ sưu tập"    icon={ICONS.collections} active={isActive('/admin/collections')} />
            )}
            <NavItem href="/admin/products"    label="Sản phẩm"      icon={ICONS.products}    active={isActive('/admin/products')} />
          </div>
        </div>

        {hasAnyRole(roles, ['ROLE_ADMIN']) && (
          <div className="pt-4">
            <p className="font-display text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
              Tương tác
            </p>
            <div className="space-y-0.5">
              <NavItem href="/admin/reviews" label="Đánh giá" icon={ICONS.reviews} active={isActive('/admin/reviews')} />
              <NavItem href="/admin/comments" label="Bình luận" icon={ICONS.comments} active={isActive('/admin/comments')} />
            </div>
          </div>
        )}

        {hasAnyRole(roles, ['ROLE_ADMIN', 'ROLE_SALE', 'ROLE_WAREHOUSE']) && (
          <div className="pt-4">
            <p className="font-display text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
              Bán hàng
            </p>
            <div className="space-y-0.5">
              <NavItem href="/admin/orders" label="Đơn hàng" icon={ICONS.orders} active={isActive('/admin/orders')} />
              <NavItem href="/admin/returns" label="Đổi/trả hàng" icon={ICONS.returns} active={isActive('/admin/returns')} />
            </div>
          </div>
        )}

        {hasAnyRole(roles, ['ROLE_ADMIN', 'ROLE_WAREHOUSE']) && (
          <div className="pt-4">
            <p className="font-display text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
              Kho
            </p>
            <div className="space-y-0.5">
              <NavItem href="/admin/suppliers" label="Nhà cung cấp" icon={ICONS.suppliers} active={isActive('/admin/suppliers')} />
              <NavItem href="/admin/purchases" label="Phiếu nhập hàng" icon={ICONS.purchases} active={isActive('/admin/purchases')} />
              <NavItem href="/admin/stock-adjustments" label="Điều chỉnh kho" icon={ICONS.adjustments} active={isActive('/admin/stock-adjustments')} />
            </div>
          </div>
        )}

        {hasAnyRole(roles, ['ROLE_ADMIN', 'ROLE_SALE']) && (
          <div className="pt-4">
            <p className="font-display text-[9px] font-semibold tracking-[0.14em] uppercase text-muted px-3 mb-1.5">
              Marketing
            </p>
            <div className="space-y-0.5">
              <NavItem href="/admin/coupons" label="Coupon" icon={ICONS.coupons} active={isActive('/admin/coupons')} />
              <NavItem href="/admin/banners" label="Banner" icon={ICONS.banners} active={isActive('/admin/banners')} />
              {hasAnyRole(roles, ['ROLE_ADMIN']) && (
                <NavItem href="/admin/notifications/marketing" label="Soạn thông báo" icon={ICONS.marketingCompose} active={isActive('/admin/notifications/marketing')} />
              )}
              {hasAnyRole(roles, ['ROLE_ADMIN']) && (
                <NavItem href="/admin/settings/store" label="Chính sách giá" icon={ICONS.pricingSetting} active={isActive('/admin/settings/store')} />
              )}
            </div>
          </div>
        )}
      </nav>
    </aside>
  );
}
