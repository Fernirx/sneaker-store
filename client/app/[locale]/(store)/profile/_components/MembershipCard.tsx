'use client';

import { useTranslations } from 'next-intl';
import { avatarUrl } from '@/lib/cloudinaryUrl';
import type { Customer } from './ProfileClient';

const TIER_CONFIG = {
  BRONZE:   { label: 'Bronze',   gradient: 'from-amber-800 to-amber-600',   ring: 'ring-amber-500/30' },
  SILVER:   { label: 'Silver',   gradient: 'from-slate-600 to-slate-400',   ring: 'ring-slate-400/30' },
  GOLD:     { label: 'Gold',     gradient: 'from-yellow-600 to-amber-400',  ring: 'ring-yellow-400/30' },
  PLATINUM: { label: 'Platinum', gradient: 'from-indigo-700 to-purple-500', ring: 'ring-indigo-400/30' },
} as const;

const TIER_THRESHOLDS: Record<string, number | null> = {
  BRONZE: 1000,
  SILVER: 5000,
  GOLD: 20000,
  PLATINUM: null,
};

const TIER_BASE: Record<string, number> = {
  BRONZE: 0,
  SILVER: 1000,
  GOLD: 5000,
  PLATINUM: 20000,
};

export default function MembershipCard({
  customer, firstName, lastName, email, createdAt, avatarPublicId,
}: {
  customer: Customer;
  firstName: string;
  lastName?: string;
  email: string;
  createdAt: string;
  avatarPublicId?: string;
}) {
  const t = useTranslations('profile');
  const cfg = TIER_CONFIG[customer.membershipTier];
  const nextThreshold = TIER_THRESHOLDS[customer.membershipTier];
  const base = TIER_BASE[customer.membershipTier];
  const progress = nextThreshold
    ? Math.min(((customer.loyaltyPoints - base) / (nextThreshold - base)) * 100, 100)
    : 100;

  const fullName = [firstName, lastName].filter(Boolean).join(' ');
  const joinYear = new Date(createdAt).getFullYear();

  return (
    <div className={`relative rounded-lg overflow-hidden bg-gradient-to-r ${cfg.gradient} ring-2 ${cfg.ring} text-white p-6`}>
      {/* Background pattern */}
      <div className="absolute inset-0 opacity-[0.06]"
        style={{ backgroundImage: 'repeating-linear-gradient(135deg,#fff 0 2px,transparent 2px 16px)' }} />

      <div className="relative z-10 flex flex-col sm:flex-row sm:items-end gap-4">
        {/* Left: avatar + tier + name */}
        <div className="flex items-center gap-4 flex-1 min-w-0">
          <div className="shrink-0 w-14 h-14 rounded-full overflow-hidden ring-2 ring-white/30 bg-white/10 flex items-center justify-center">
            {avatarPublicId ? (
              <img src={avatarUrl(avatarPublicId, 112)} alt="avatar" className="w-full h-full object-cover" />
            ) : (
              <span className="font-display font-black text-xl text-white">{firstName.charAt(0).toUpperCase()}</span>
            )}
          </div>
          <div className="space-y-1.5 min-w-0">
            <span className="inline-block font-mono text-[10px] font-semibold tracking-[0.14em] uppercase bg-white/20 px-2.5 py-1 rounded-sm">
              {cfg.label} Member
            </span>
            <p className="font-display font-black text-xl leading-tight truncate">{fullName || email}</p>
            <p className="text-white/70 text-xs truncate">{email} · {t('memberSince')} {joinYear}</p>
          </div>
        </div>

        {/* Right: stats */}
        <div className="flex gap-6 sm:text-right shrink-0">
          <div>
            <p className="font-display font-black text-2xl leading-none">
              {customer.loyaltyPoints.toLocaleString('vi-VN')}
            </p>
            <p className="text-white/70 text-xs mt-0.5">{t('loyaltyPoints')}</p>
          </div>
          <div>
            <p className="font-display font-black text-2xl leading-none">
              {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 })
                .format(customer.totalSpent)}
            </p>
            <p className="text-white/70 text-xs mt-0.5">{t('totalSpent')}</p>
          </div>
        </div>
      </div>

      {/* Progress to next tier */}
      {nextThreshold && (
        <div className="relative z-10 mt-4 space-y-1">
          <div className="flex justify-between text-[11px] text-white/70">
            <span>{customer.loyaltyPoints.toLocaleString('vi-VN')} / {nextThreshold.toLocaleString('vi-VN')} pts → {TIER_CONFIG[
              customer.membershipTier === 'BRONZE' ? 'SILVER'
              : customer.membershipTier === 'SILVER' ? 'GOLD' : 'PLATINUM'
            ].label}</span>
            <span>{Math.round(progress)}%</span>
          </div>
          <div className="h-1.5 bg-white/20 rounded-full overflow-hidden">
            <div className="h-full bg-white/80 rounded-full transition-all" style={{ width: `${progress}%` }} />
          </div>
        </div>
      )}
    </div>
  );
}
