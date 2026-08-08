'use client';

import { useState, useTransition } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import type { Profile } from './ProfileClient';

function FieldError({ msg }: { msg?: string }) {
  return msg ? <p className="text-xs text-danger mt-1">{msg}</p> : null;
}

function inputCls(error?: string) {
  return `w-full border rounded px-3 py-2.5 text-sm focus:outline-none transition-colors ${
    error ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'
  }`;
}

export default function ProfileForm({
  profile,
  onUpdated,
}: {
  profile: Profile;
  onUpdated: (p: Profile) => void;
}) {
  const [pending, start] = useTransition();

  const [firstName, setFirstName] = useState(profile.firstName);
  const [lastName, setLastName] = useState(profile.lastName ?? '');
  const [phone, setPhone] = useState(profile.phone ?? '');
  const [dateOfBirth, setDateOfBirth] = useState(profile.dateOfBirth ?? '');

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState('');
  const [generalError, setGeneralError] = useState('');

  function handleSave() {
    setFieldErrors({});
    setSuccess('');
    setGeneralError('');

    start(async () => {
      try {
        const { data } = await clientAxios.patch('/api/me', {
          firstName: firstName.trim() || undefined,
          lastName: lastName.trim(),
          phone: phone.trim(),
          dateOfBirth: dateOfBirth || undefined,
        });
        onUpdated(data.data as Profile);
        setSuccess("Đã cập nhật thành công");
      } catch (err) {
        const { general, fields } = parseApiError(err, "Lỗi kết nối");
        setFieldErrors(fields);
        setGeneralError(general);
      }
    });
  }

  return (
    <div className="space-y-5 max-w-md">
      {/* Email - read only */}
      <div>
        <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Email"}</label>
        <div className="flex items-center gap-2">
          <input readOnly value={profile.email}
            className="flex-1 border border-line rounded px-3 py-2.5 text-sm bg-paper text-muted cursor-not-allowed" />
          <span className={`shrink-0 text-[11px] font-semibold px-2 py-1 rounded ${
            profile.emailVerified ? 'bg-ok-bg text-ok' : 'bg-warn-bg text-warn'
          }`}>
            {profile.emailVerified ? "Đã xác minh" : "Chưa xác minh"}
          </span>
        </div>
      </div>

      {/* Name */}
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">
            {"Họ"}
            <span className="text-danger ml-1">*</span>
          </label>
          <input value={firstName} onChange={e => setFirstName(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            className={inputCls(fieldErrors['firstName'])} />
          <FieldError msg={fieldErrors['firstName']} />
        </div>
        <div>
          <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Tên"}</label>
          <input value={lastName} onChange={e => setLastName(e.target.value.replace(/^\s+/, ''))}
            maxLength={100}
            className={inputCls(fieldErrors['lastName'])} />
          <FieldError msg={fieldErrors['lastName']} />
        </div>
      </div>

      {/* Phone */}
      <div>
        <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Số điện thoại"}</label>
        <div className="flex relative">
          <span className="absolute left-3 top-1/2 -translate-y-1/2 text-[13px] text-ink z-10 font-medium">
            +84
          </span>
          <input
            type="tel"
            maxLength={15}
            value={phone.startsWith('+84') ? phone.slice(3) : phone}
            onChange={e => {
              const val = e.target.value.replace(/\D/g, '');
              setPhone(val ? `+84${val}` : '');
            }}
            placeholder="912345678"
            className={`${inputCls(fieldErrors['phone'])} pl-10`}
          />
        </div>
        <FieldError msg={fieldErrors['phone']} />
      </div>

      {/* Date of birth */}
      <div>
        <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Ngày sinh"}</label>
        <input type="date" value={dateOfBirth} onChange={e => setDateOfBirth(e.target.value)}
          max={new Date().toISOString().split('T')[0]}
          className={inputCls(fieldErrors['dateOfBirth'])} />
        <FieldError msg={fieldErrors['dateOfBirth']} />
      </div>

      {/* Feedback */}
      {generalError && (
        <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>
      )}
      {success && (
        <p className="text-sm text-ok bg-ok-bg border border-ok/20 rounded px-3 py-2">{success}</p>
      )}

      <button onClick={handleSave} disabled={pending || !firstName.trim()}
        className="bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider px-6 py-2.5 rounded transition-colors">
        {pending ? "Đang lưu..." : "Lưu thay đổi"}
      </button>
    </div>
  );
}
