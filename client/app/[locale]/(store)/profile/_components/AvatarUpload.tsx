'use client';

import { useRef, useState } from 'react';
import { useTranslations } from 'next-intl';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import { avatarUrl } from '@/lib/cloudinaryUrl';
import type { Profile } from './ProfileClient';

function CameraIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
      <circle cx="12" cy="13" r="4"/>
    </svg>
  );
}

function SpinnerIcon() {
  return (
    <svg className="animate-spin" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
    </svg>
  );
}

export default function AvatarUpload({
  avatarPublicId,
  name,
  onUpdated,
}: {
  avatarPublicId?: string;
  name: string;
  onUpdated: (p: Profile) => void;
}) {
  const t = useTranslations('profile');
  const fileRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  async function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setError('');
    setUploading(true);
    try {
      const form = new FormData();
      form.append('file', file);
      form.append('folder', 'avatars');
      if (avatarPublicId) form.append('oldPublicId', avatarPublicId);
      const { data: up } = await clientAxios.post('/api/upload', form);
      const { data: me } = await clientAxios.patch('/api/me', { avatarPublicId: up.publicId });
      onUpdated(me.data as Profile);
    } catch (err) {
      const { general } = parseApiError(err, t('uploadError'));
      setError(general);
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  }

  const src = avatarPublicId ? avatarUrl(avatarPublicId, 160) : null;
  const initials = name.charAt(0).toUpperCase();

  return (
    <div className="flex items-center gap-4">
      <div className="relative shrink-0">
        <div className="w-20 h-20 rounded-full overflow-hidden bg-accent/10 flex items-center justify-center ring-2 ring-line">
          {src ? (
            <img src={src} alt="avatar" className="w-full h-full object-cover" />
          ) : (
            <span className="font-display font-black text-2xl text-accent">{initials}</span>
          )}
          {uploading && (
            <div className="absolute inset-0 bg-black/40 rounded-full flex items-center justify-center text-white">
              <SpinnerIcon />
            </div>
          )}
        </div>
        <button
          type="button"
          onClick={() => fileRef.current?.click()}
          disabled={uploading}
          className="absolute -bottom-0.5 -right-0.5 w-7 h-7 rounded-full bg-accent text-white flex items-center justify-center hover:bg-accent-700 transition-colors shadow-sm disabled:opacity-50"
        >
          <CameraIcon />
        </button>
      </div>

      <div>
        <p className="font-semibold text-sm text-ink">{name}</p>
        <button
          type="button"
          onClick={() => fileRef.current?.click()}
          disabled={uploading}
          className="text-xs text-muted hover:text-ink transition-colors mt-0.5 disabled:opacity-40"
        >
          {uploading ? t('uploading') : t('uploadAvatar')}
        </button>
        {error && <p className="text-xs text-danger mt-1">{error}</p>}
      </div>

      <input
        ref={fileRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onChange={handleChange}
      />
    </div>
  );
}
