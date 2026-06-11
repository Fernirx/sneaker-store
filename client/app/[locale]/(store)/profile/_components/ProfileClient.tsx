'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useTranslations } from 'next-intl';
import MembershipCard from './MembershipCard';
import ProfileForm from './ProfileForm';
import AddressSection from './AddressSection';
import ChangePasswordForm from './ChangePasswordForm';

export interface Profile {
  id: number;
  email: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  dateOfBirth?: string;
  avatarPublicId?: string;
  emailVerified: boolean;
  hasPassword: boolean;
  createdAt: string;
}

export interface Customer {
  membershipTier: 'BRONZE' | 'SILVER' | 'GOLD' | 'PLATINUM';
  loyaltyPoints: number;
  totalSpent: number;
}

type Tab = 'info' | 'address' | 'security';

export default function ProfileClient({
  initialProfile,
  initialCustomer,
}: {
  initialProfile: Profile | null;
  initialCustomer: Customer | null;
}) {
  const t = useTranslations('profile');
  const router = useRouter();
  const [tab, setTab] = useState<Tab>('info');
  const [profile, setProfile] = useState<Profile | null>(initialProfile);

  function handleProfileUpdated(updated: Profile) {
    setProfile(updated);
    router.refresh();
  }

  const TABS: { key: Tab; label: string }[] = [
    { key: 'info', label: t('tabInfo') },
    { key: 'address', label: t('tabAddress') },
    { key: 'security', label: t('tabSecurity') },
  ];

  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-8">
      <h1 className="font-display font-black text-2xl uppercase tracking-tight">{t('title')}</h1>

      {initialCustomer && profile && (
        <MembershipCard
          customer={initialCustomer}
          firstName={profile.firstName}
          lastName={profile.lastName}
          email={profile.email}
          createdAt={profile.createdAt}
        />
      )}

      {/* Tabs */}
      <div className="flex border-b border-line">
        {TABS.map(({ key, label }) => (
          <button key={key} type="button"
            onClick={() => setTab(key)}
            className={`px-5 py-3 font-display font-bold text-[13px] uppercase tracking-wider border-b-2 transition-colors -mb-px ${
              tab === key
                ? 'border-accent text-accent'
                : 'border-transparent text-muted hover:text-ink'
            }`}>
            {label}
          </button>
        ))}
      </div>

      <div>
        {tab === 'info' && profile && (
          <ProfileForm profile={profile} onUpdated={handleProfileUpdated} />
        )}
        {tab === 'address' && <AddressSection />}
        {tab === 'security' && <ChangePasswordForm hasPassword={profile?.hasPassword ?? false} />}
      </div>
    </div>
  );
}
