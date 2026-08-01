'use client';

import { useState, useTransition } from 'react';
import { useRouter } from 'next/navigation';
import clientAxios from '@/lib/axios/clientAxios';
import { parseApiError } from '@/lib/parseApiError';
import Link from "next/link";
import { Home } from 'lucide-react';

type Step = 'email' | 'otp' | 'reset';
type FieldErrors = Record<string, string>;

function FieldError({ msg }: { msg?: string }) {
  return msg ? <p className="text-xs text-danger mt-1">{msg}</p> : null;
}

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

export default function ForgotPasswordForm() {
  const router = useRouter();
  const [step, setStep] = useState<Step>('email');
  const [pending, start] = useTransition();

  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [resendSeconds, setResendSeconds] = useState(0);
  
  const [resetToken, setResetToken] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [generalError, setGeneralError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  function clearErrors() { setGeneralError(''); setFieldErrors({}); }

  function startResendTimer() {
    setResendSeconds(60);
    const iv = setInterval(() => {
      setResendSeconds(s => { if (s <= 1) { clearInterval(iv); return 0; } return s - 1; });
    }, 1000);
  }

  function handleSendOtp() {
    clearErrors();
    start(async () => {
      try {
        await clientAxios.post('/api/auth/forgot-password', { email });
        startResendTimer();
        setStep('otp');
      } catch (err) {
        const { general, fields } = parseApiError(err, "Lỗi kết nối");
        setGeneralError(general);
        setFieldErrors(fields);
      }
    });
  }

  function handleVerifyOtp() {
    clearErrors();
    start(async () => {
      try {
        const { data } = await clientAxios.post('/api/auth/forgot-password/verify-otp', { email, otp });
        if (data.resetPasswordToken) {
          setResetToken(data.resetPasswordToken);
          setStep('reset');
        } else {
          setGeneralError("Không nhận được mã xác thực để đổi mật khẩu.");
        }
      } catch (err) {
        const { general } = parseApiError(err, "Lỗi xác minh");
        setGeneralError(general);
      }
    });
  }

  async function handleResend() {
    if (resendSeconds > 0) return;
    try {
      await clientAxios.post('/api/auth/resend-otp', { email, purpose: 'FORGOT_PASSWORD' });
      startResendTimer();
    } catch { /* ignore */ }
  }

  function handleResetPassword() {
    clearErrors();
    start(async () => {
      try {
        await clientAxios.post('/api/auth/forgot-password/reset', { 
          resetToken, 
          password: newPassword,
          confirmPassword: confirmPassword
        });
        router.replace('/login?reset_success=1');
      } catch (err) {
        const { general, fields } = parseApiError(err, "Lỗi kết nối");
        setGeneralError(general);
        setFieldErrors(fields);
      }
    });
  }

  function inputCls(field: string) {
    return `w-full border rounded px-3 py-2.5 text-sm focus:outline-none transition-colors ${
      fieldErrors[field] ? 'border-danger focus:border-danger' : 'border-line focus:border-ink'
    }`;
  }

  return (
    <div className="min-h-screen grid grid-cols-1 md:grid-cols-2">

      {/* Promo side */}
      <div className="hidden md:flex flex-col bg-ink text-white p-14 relative overflow-hidden select-none">
        <div className="absolute inset-0 opacity-[0.04]"
          style={{ backgroundImage: 'repeating-linear-gradient(135deg,#fff 0 2px,transparent 2px 11px)' }} />
        <div className="relative z-10 flex items-center gap-2">
          <span className="w-2.5 h-2.5 bg-accent rounded-xs rotate-45 shrink-0" />
          <span className="font-display font-black text-xl uppercase tracking-tight">STRIDE</span>
        </div>
        <div className="flex-1" />
        <div className="relative z-10 space-y-5">
          <span className="inline-block text-[10px] font-semibold tracking-[0.12em] uppercase bg-accent px-2.5 py-1 rounded-sm">
            {"Bảo mật tài khoản"}
          </span>
          <h1 className="font-display font-black text-[clamp(3rem,5vw,4.5rem)] uppercase leading-[0.9] tracking-tight whitespace-pre-line">
            {`KHÔI PHỤC\nMẬT KHẨU.`}
          </h1>
          <p className="text-faint text-[15px] leading-relaxed max-w-xs">{"Lấy lại quyền truy cập vào tài khoản của bạn chỉ với vài bước đơn giản."}</p>
        </div>
      </div>

      {/* Form side */}
      <div className="relative flex items-center justify-center min-h-screen md:min-h-0 p-8 bg-white">
        <Link
            href="/"
            className="absolute top-6 left-6 w-10 h-10 rounded-full border border-line flex items-center justify-center hover:bg-gray-50 transition-colors"
        >
          <Home size={18} />
        </Link>
        <div className="w-full max-w-sm">

          {step === 'email' && (
            <div className="space-y-4">
              <div className="mb-6">
                <h2 className="font-display font-black text-2xl uppercase tracking-tight">{"Quên mật khẩu"}</h2>
                <p className="text-muted text-sm mt-1.5">
                  {"Nhập email đã đăng ký, chúng tôi sẽ gửi mã OTP để bạn lấy lại mật khẩu."}
                </p>
              </div>

              {generalError && <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>}

              <div>
                <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Email"}</label>
                <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleSendOtp()}
                  placeholder={"ban@email.com"} className={inputCls('email')} />
                <FieldError msg={fieldErrors['email']} />
              </div>

              <button onClick={handleSendOtp} disabled={pending || !email}
                className="w-full bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-3 rounded transition-colors mt-2">
                {pending ? "Đang gửi..." : "Gửi mã xác minh"}
              </button>

              <div className="text-center mt-6">
                <Link href="/login" className="text-sm text-ink font-semibold hover:underline underline-offset-2 transition-colors">
                  {"Quay lại đăng nhập"}
                </Link>
              </div>
            </div>
          )}

          {step === 'otp' && (
            <div className="space-y-5">
              <div>
                <h2 className="font-display font-black text-2xl uppercase tracking-tight">{"Xác minh email"}</h2>
                <p className="text-muted text-sm mt-1.5">
                  {"Nhập mã OTP đã gửi đến"} <span className="font-semibold text-ink">{email}</span>
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Mã OTP (6 số)"}</label>
                <input
                  value={otp}
                  onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  onKeyDown={e => e.key === 'Enter' && otp.length === 6 && handleVerifyOtp()}
                  placeholder="000000"
                  className="w-full border border-line rounded px-3 py-2.5 font-body text-center text-xl tracking-[0.4em] focus:outline-none focus:border-ink transition-colors"
                />
              </div>

              {generalError && <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>}

              <button onClick={handleVerifyOtp} disabled={pending || otp.length !== 6}
                className="w-full bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-3 rounded transition-colors">
                {pending ? "Đang xác minh..." : "Xác minh"}
              </button>

              <button onClick={handleResend} disabled={resendSeconds > 0}
                className="w-full text-sm text-muted hover:text-ink disabled:opacity-40 transition-colors">
                {resendSeconds > 0 ? `${"Gửi lại sau"} ${resendSeconds}s` : "Gửi lại mã"}
              </button>
            </div>
          )}

          {step === 'reset' && (
            <div className="space-y-4">
              <div className="mb-6">
                <h2 className="font-display font-black text-2xl uppercase tracking-tight">{"Tạo mật khẩu mới"}</h2>
                <p className="text-muted text-sm mt-1.5">
                  {"Vui lòng nhập mật khẩu mới cho tài khoản của bạn."}
                </p>
              </div>

              {generalError && <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>}

              <div>
                <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Mật khẩu mới"}</label>
                <div className="relative">
                  <input type={showPass ? 'text' : 'password'} value={newPassword} onChange={e => setNewPassword(e.target.value)}
                    className={`${inputCls('password')} pr-10`} />
                  <button type="button" onClick={() => setShowPass(v => !v)}
                    className="absolute inset-y-0 right-3 flex items-center text-muted hover:text-ink transition-colors">
                    <EyeIcon open={showPass} />
                  </button>
                </div>
                <FieldError msg={fieldErrors['password']} />
              </div>

              <div>
                <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{"Xác nhận mật khẩu"}</label>
                <div className="relative">
                  <input type={showConfirm ? 'text' : 'password'} value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && handleResetPassword()}
                    className={`${inputCls('confirmPassword')} pr-10`} />
                  <button type="button" onClick={() => setShowConfirm(v => !v)}
                    className="absolute inset-y-0 right-3 flex items-center text-muted hover:text-ink transition-colors">
                    <EyeIcon open={showConfirm} />
                  </button>
                </div>
                <FieldError msg={fieldErrors['confirmPassword']} />
              </div>

              <button onClick={handleResetPassword} disabled={pending || !newPassword || !confirmPassword}
                className="w-full bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-3 rounded transition-colors mt-2">
                {pending ? "Đang xử lý..." : "Đổi mật khẩu"}
              </button>
            </div>
          )}

        </div>
      </div>
    </div>
  );
}
