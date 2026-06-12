'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import AvatarUpload from '@/app/[locale]/(store)/profile/_components/AvatarUpload';
import ProfileForm from '@/app/[locale]/(store)/profile/_components/ProfileForm';
import ChangePasswordForm from '@/app/[locale]/(store)/profile/_components/ChangePasswordForm';
import type { Profile } from '@/app/[locale]/(store)/profile/_components/ProfileClient';

type Tab = 'info' | 'security';

export default function AdminProfileClient({ initialProfile }: { initialProfile: Profile }) {
  const router = useRouter();
  const [tab, setTab] = useState<Tab>('info');
  const [profile, setProfile] = useState<Profile>(initialProfile);

  function handleProfileUpdated(updated: Profile) {
    setProfile(updated);
    router.refresh();
  }

  const TABS: { key: Tab; label: string }[] = [
    { key: 'info', label: 'Thông tin' },
    { key: 'security', label: 'Bảo mật' },
  ];

  return (
    <div className="max-w-2xl space-y-6">
      <h1 className="font-display font-black text-xl uppercase tracking-tight">Hồ sơ cá nhân</h1>

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
        {tab === 'info' && (
          <div className="space-y-6">
            <AvatarUpload
              avatarPublicId={profile.avatarPublicId}
              name={profile.firstName}
              onUpdated={handleProfileUpdated}
            />
            <hr className="border-line" />
            <ProfileForm profile={profile} onUpdated={handleProfileUpdated} />
          </div>
        )}
        {tab === 'security' && (
          <ChangePasswordForm hasPassword={profile.hasPassword} />
        )}
      </div>
    </div>
  );
}
