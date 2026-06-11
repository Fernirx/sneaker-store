'use client';

import { useState, useTransition } from 'react';
import { useTranslations } from 'next-intl';
import axios from 'axios';
import clientAxios from '@/lib/axios/clientAxios';

function EyeIcon({ open }: { open: boolean }) {
  return open ? (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7z"/><circle cx="12" cy="12" r="3"/>
    </svg>
  ) : (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-10-8-10-8a18.45 18.45 0 0 1 5.06-5.94"/><path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 10 8 10 8a18.5 18.5 0 0 1-2.16 3.19"/><line x1="1" y1="1" x2="23" y2="23"/>
    </svg>
  );
}

function PasswordField({
  label, value, onChange, show, onToggle, error,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  show: boolean;
  onToggle: () => void;
  error?: string;
}) {
  return (
    <div>
      <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{label}</label>
      <div className="relative">
        <input
          type={show ? 'text' : 'password'}
          value={value}
          onChange={e => onChange(e.target.value)}
          className={`w-full border rounded px-3 py-2.5 pr-10 text-sm focus:outline-none transition-colors ${
            error ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'
          }`}
        />
        <button type="button" onClick={onToggle}
          className="absolute inset-y-0 right-3 flex items-center text-muted hover:text-ink transition-colors">
          <EyeIcon open={show} />
        </button>
      </div>
      {error && <p className="text-xs text-danger mt-1">{error}</p>}
    </div>
  );
}

function parseError(err: unknown) {
  if (!axios.isAxiosError(err)) return { general: 'Lỗi kết nối', fields: {} };
  const d = err.response?.data as { fields?: { field: string; message: string }[]; message?: string } | undefined;
  const fields: Record<string, string> = {};
  d?.fields?.forEach(f => { fields[f.field] = f.message; });
  return {
    general: Object.keys(fields).length === 0 ? (d?.message ?? 'Đã có lỗi xảy ra') : '',
    fields,
  };
}

export default function ChangePasswordForm({ hasPassword }: { hasPassword: boolean }) {
  const t = useTranslations('profile');
  const [pending, start] = useTransition();

  const [currentPassword, setCurrentPassword] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState('');
  const [generalError, setGeneralError] = useState('');

  function reset() {
    setCurrentPassword('');
    setPassword('');
    setConfirmPassword('');
    setFieldErrors({});
    setGeneralError('');
  }

  function handleSubmit() {
    setFieldErrors({});
    setSuccess('');
    setGeneralError('');

    start(async () => {
      try {
        if (hasPassword) {
          await clientAxios.post('/api/me/change-password', { currentPassword, password, confirmPassword });
        } else {
          await clientAxios.post('/api/me/set-password', { password, confirmPassword });
        }
        setSuccess(hasPassword ? 'Đổi mật khẩu thành công' : 'Đặt mật khẩu thành công');
        reset();
      } catch (err) {
        const { general, fields } = parseError(err);
        setFieldErrors(fields);
        setGeneralError(general);
      }
    });
  }

  const canSubmit = hasPassword
    ? !!(currentPassword && password && confirmPassword)
    : !!(password && confirmPassword);

  return (
    <div className="space-y-5 max-w-md">
      {!hasPassword && (
        <p className="text-sm text-ink-2 bg-warn-bg border border-warn/20 rounded px-3 py-2.5">
          Tài khoản của bạn chưa có mật khẩu. Đặt mật khẩu để có thể đăng nhập bằng email.
        </p>
      )}

      {hasPassword && (
        <PasswordField
          label={t('currentPwd')}
          value={currentPassword}
          onChange={setCurrentPassword}
          show={showCurrent}
          onToggle={() => setShowCurrent(v => !v)}
          error={fieldErrors['currentPassword']}
        />
      )}

      <PasswordField
        label={t('newPwd')}
        value={password}
        onChange={setPassword}
        show={showNew}
        onToggle={() => setShowNew(v => !v)}
        error={fieldErrors['password']}
      />

      <PasswordField
        label={t('confirmPwd')}
        value={confirmPassword}
        onChange={setConfirmPassword}
        show={showConfirm}
        onToggle={() => setShowConfirm(v => !v)}
        error={fieldErrors['confirmPassword']}
      />

      {generalError && (
        <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>
      )}
      {success && (
        <p className="text-sm text-ok bg-ok-bg border border-ok/20 rounded px-3 py-2">{success}</p>
      )}

      <button
        onClick={handleSubmit}
        disabled={pending || !canSubmit}
        className="bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider px-6 py-2.5 rounded transition-colors">
        {pending ? t('changing') : (hasPassword ? t('changePwdBtn') : 'Đặt mật khẩu')}
      </button>
    </div>
  );
}
