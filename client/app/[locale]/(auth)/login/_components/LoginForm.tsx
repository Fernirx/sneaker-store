'use client';

import { useState, useTransition, useEffect } from 'react';
import { useTranslations } from 'next-intl';
import { useRouter } from '@/i18n/routing';
import { useSearchParams } from 'next/navigation';
import axios from 'axios';
import { isStaffRole } from '@/lib/constants';
import LanguageSwitcher from '@/components/LanguageSwitcher';

const OAUTH2_ERROR_MESSAGES: Record<string, string> = {
  oauth2_failed: 'Đăng nhập bằng mạng xã hội thất bại.',
  ACCOUNT_UNAVAILABLE: 'Tài khoản của bạn đã bị vô hiệu hóa.',
  UNAUTHORIZED: 'Không có quyền truy cập.',
};

type Tab = 'login' | 'register' | 'otp';
type FieldErrors = Record<string, string>;

interface ApiError {
  code?: string;
  message?: string;
  fields?: { field: string; message: string }[];
}

function parseError(err: unknown): { code: string; general: string; fields: FieldErrors } {
  if (!axios.isAxiosError(err)) return { code: '', general: 'Lỗi kết nối', fields: {} };
  const data = err.response?.data as ApiError | undefined;
  const fields: FieldErrors = {};
  data?.fields?.forEach(f => { fields[f.field] = f.message; });
  const general = Object.keys(fields).length === 0
    ? (data?.message ?? 'Đã có lỗi xảy ra')
    : '';
  return { code: data?.code ?? '', general, fields };
}

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

export default function LoginForm() {
  const t = useTranslations('auth');
  const router = useRouter();
  const searchParams = useSearchParams();
  const [tab, setTab] = useState<Tab>('login');
  const [pending, start] = useTransition();

  const [loginEmail, setLoginEmail] = useState('');
  const [loginPass, setLoginPass] = useState('');

  const [regEmail, setRegEmail] = useState('');
  const [regFirst, setRegFirst] = useState('');
  const [regLast, setRegLast] = useState('');
  const [regPass, setRegPass] = useState('');
  const [regConfirm, setRegConfirm] = useState('');

  const [otpEmail, setOtpEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [resendSeconds, setResendSeconds] = useState(0);

  const [generalError, setGeneralError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({});

  useEffect(() => {
    const error = searchParams.get('error');
    if (!error) return;
    const msg = OAUTH2_ERROR_MESSAGES[error] ?? 'Đăng nhập thất bại, vui lòng thử lại.';
    setGeneralError(msg);
    const url = new URL(window.location.href);
    url.searchParams.delete('error');
    window.history.replaceState({}, '', url.toString());
  }, [searchParams]);

  const [showLoginPass, setShowLoginPass] = useState(false);
  const [showRegPass, setShowRegPass] = useState(false);
  const [showRegConfirm, setShowRegConfirm] = useState(false);

  function clearErrors() { setGeneralError(''); setFieldErrors({}); }

  function startResendTimer() {
    setResendSeconds(60);
    const iv = setInterval(() => {
      setResendSeconds(s => { if (s <= 1) { clearInterval(iv); return 0; } return s - 1; });
    }, 1000);
  }

  function handleLogin() {
    clearErrors();
    start(async () => {
      try {
        const { data } = await axios.post('/api/auth/login', { email: loginEmail, password: loginPass });
        const roles: string[] = data.roles ?? [];
        router.replace(isStaffRole(roles) ? '/admin' : '/');
      } catch (err) {
        const { code, general, fields } = parseError(err);
        if (code === 'EMAIL_NOT_VERIFIED') {
          setOtpEmail(loginEmail);
          await axios.post('/api/auth/resend-otp', { email: loginEmail, purpose: 'REGISTER' }).catch(() => {});
          startResendTimer();
          setTab('otp');
          return;
        }
        setGeneralError(general);
        setFieldErrors(fields);
      }
    });
  }

  function handleRegister() {
    clearErrors();
    start(async () => {
      try {
        await axios.post('/api/auth/register', {
          email: regEmail,
          password: regPass,
          confirmPassword: regConfirm,
          firstName: regFirst || undefined,
          lastName: regLast || undefined,
        });
        setOtpEmail(regEmail);
        startResendTimer();
        setTab('otp');
      } catch (err) {
        const { general, fields } = parseError(err);
        setGeneralError(general);
        setFieldErrors(fields);
      }
    });
  }

  function handleVerifyOtp() {
    clearErrors();
    start(async () => {
      try {
        const { data } = await axios.post('/api/auth/verify-otp', { email: otpEmail, otp });
        const roles: string[] = data.roles ?? [];
        router.replace(isStaffRole(roles) ? '/admin' : '/');
      } catch (err) {
        const { general } = parseError(err);
        setGeneralError(general);
      }
    });
  }

  async function handleResend() {
    if (resendSeconds > 0) return;
    try {
      await axios.post('/api/auth/resend-otp', { email: otpEmail, purpose: 'REGISTER' });
      startResendTimer();
    } catch { /* ignore */ }
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
          <span className="inline-block font-mono text-[10px] font-semibold tracking-[0.12em] uppercase bg-accent px-2.5 py-1 rounded-sm">
            {t('memberBadge')}
          </span>
          <h1 className="font-display font-black text-[clamp(3rem,5vw,4.5rem)] uppercase leading-[0.9] tracking-tight whitespace-pre-line">
            {t('heroCopy')}
          </h1>
          <p className="text-faint text-[15px] leading-relaxed max-w-xs">{t('heroSub')}</p>
        </div>
      </div>

      {/* Form side */}
      <div className="relative flex items-center justify-center min-h-screen md:min-h-0 p-8 bg-white">
        <div className="absolute top-6 right-6">
          <LanguageSwitcher />
        </div>
        <div className="w-full max-w-sm">

          {tab === 'otp' ? (
            <div className="space-y-5">
              <div>
                <h2 className="font-display font-black text-2xl uppercase tracking-tight">{t('otpTitle')}</h2>
                <p className="text-muted text-sm mt-1.5">
                  {t('otpSub')} <span className="font-semibold text-ink">{otpEmail}</span>
                </p>
              </div>
              <div>
                <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('otpCode')}</label>
                <input
                  value={otp}
                  onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  placeholder="000000"
                  className="w-full border border-line rounded px-3 py-2.5 font-mono text-center text-xl tracking-[0.4em] focus:outline-none focus:border-ink transition-colors"
                />
              </div>
              {generalError && <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2">{generalError}</p>}
              <button onClick={handleVerifyOtp} disabled={pending || otp.length !== 6}
                className="w-full bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-3 rounded transition-colors">
                {pending ? t('verifying') : t('verifyBtn')}
              </button>
              <button onClick={handleResend} disabled={resendSeconds > 0}
                className="w-full text-sm text-muted hover:text-ink disabled:opacity-40 transition-colors">
                {resendSeconds > 0 ? `${t('resendIn')} ${resendSeconds}s` : t('resend')}
              </button>
            </div>
          ) : (
            <>
              {/* Tab toggle */}
              <div className="flex border border-line rounded overflow-hidden mb-6">
                {(['login', 'register'] as const).map(v => (
                  <button key={v} type="button"
                    onClick={() => { setTab(v); clearErrors(); }}
                    className={`flex-1 py-3 font-display font-bold text-[13px] uppercase tracking-wider transition-colors ${
                      tab === v ? 'bg-ink text-white' : 'bg-white text-muted hover:text-ink'
                    }`}>
                    {t(v === 'login' ? 'loginTab' : 'registerTab')}
                  </button>
                ))}
              </div>

              <div className="mb-6">
                <h2 className="font-display font-black text-2xl uppercase tracking-tight">
                  {tab === 'login' ? t('welcomeBack') : t('createAccount')}
                </h2>
                <p className="text-muted text-sm mt-1.5">
                  {tab === 'login' ? t('loginSub') : t('registerSub')}
                </p>
              </div>

              {/* OAuth */}
              <div className="grid grid-cols-2 gap-2.5 mb-5">
                {[
                  {
                    label: 'Google',
                    provider: 'google',
                    icon: <svg width="16" height="16" viewBox="0 0 24 24"><path fill="#EA4335" d="M12 11v3.6h5.1c-.2 1.3-1.6 3.9-5.1 3.9-3 0-5.5-2.5-5.5-5.5S9 7.5 12 7.5c1.7 0 2.9.7 3.6 1.4l2.5-2.4C16.5 4.9 14.5 4 12 4 7.6 4 4 7.6 4 12s3.6 8 8 8c4.6 0 7.7-3.2 7.7-7.8 0-.5 0-.9-.1-1.2H12z"/></svg>,
                  },
                  {
                    label: 'Facebook',
                    provider: 'facebook',
                    icon: <svg width="16" height="16" viewBox="0 0 24 24" fill="#1877F2"><path d="M22 12a10 10 0 1 0-11.6 9.9v-7H7.9V12h2.5V9.8c0-2.5 1.5-3.9 3.8-3.9 1.1 0 2.2.2 2.2.2v2.5h-1.2c-1.2 0-1.6.8-1.6 1.6V12h2.7l-.4 2.9h-2.3v7A10 10 0 0 0 22 12z"/></svg>,
                  },
                ].map(({ label, provider, icon }) => (
                  <button key={label} type="button"
                    onClick={() => { window.location.href = `/api/auth/oauth2?provider=${provider}`; }}
                    className="flex items-center justify-center gap-2 border border-line rounded py-2.5 text-[13px] font-semibold text-ink-2 hover:border-ink-2 hover:text-ink transition-colors">
                    {icon}{label}
                  </button>
                ))}
              </div>

              <div className="flex items-center gap-3 mb-5">
                <div className="flex-1 h-px bg-line" />
                <span className="font-mono text-[11px] uppercase tracking-widest text-faint">{t('orWith')}</span>
                <div className="flex-1 h-px bg-line" />
              </div>

              {generalError && (
                <p className="text-sm text-danger bg-danger-bg border border-danger/20 rounded px-3 py-2 mb-4">
                  {generalError}
                </p>
              )}

              {tab === 'login' ? (
                <div className="space-y-3.5">
                  <div>
                    <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('email')}</label>
                    <input type="email" value={loginEmail} onChange={e => setLoginEmail(e.target.value)}
                      placeholder="ban@email.com" className={inputCls('email')} />
                    <FieldError msg={fieldErrors['email']} />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('password')}</label>
                    <div className="relative">
                      <input type={showLoginPass ? 'text' : 'password'} value={loginPass} onChange={e => setLoginPass(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && handleLogin()}
                        className={`${inputCls('password')} pr-10`} />
                      <button type="button" onClick={() => setShowLoginPass(v => !v)}
                        className="absolute inset-y-0 right-3 flex items-center text-muted hover:text-ink transition-colors">
                        <EyeIcon open={showLoginPass} />
                      </button>
                    </div>
                    <FieldError msg={fieldErrors['password']} />
                  </div>
                  <div className="flex justify-end text-sm">
                    <button type="button" className="font-mono text-[11px] uppercase tracking-wider text-accent hover:underline">
                      {t('forgotPassword')}
                    </button>
                  </div>
                  <button onClick={handleLogin} disabled={pending || !loginEmail || !loginPass}
                    className="w-full bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-3 rounded transition-colors">
                    {pending ? t('loggingIn') : t('loginBtn')}
                  </button>
                </div>
              ) : (
                <div className="space-y-3.5">
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('firstName')}</label>
                      <input value={regFirst} onChange={e => setRegFirst(e.target.value)}
                        placeholder="Nguyễn" className={inputCls('firstName')} />
                      <FieldError msg={fieldErrors['firstName']} />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('lastName')}</label>
                      <input value={regLast} onChange={e => setRegLast(e.target.value)}
                        placeholder="An" className={inputCls('lastName')} />
                      <FieldError msg={fieldErrors['lastName']} />
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('email')}</label>
                    <input type="email" value={regEmail} onChange={e => setRegEmail(e.target.value)}
                      placeholder="ban@email.com" className={inputCls('email')} />
                    <FieldError msg={fieldErrors['email']} />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('password')}</label>
                    <div className="relative">
                      <input type={showRegPass ? 'text' : 'password'} value={regPass} onChange={e => setRegPass(e.target.value)}
                        className={`${inputCls('password')} pr-10`} />
                      <button type="button" onClick={() => setShowRegPass(v => !v)}
                        className="absolute inset-y-0 right-3 flex items-center text-muted hover:text-ink transition-colors">
                        <EyeIcon open={showRegPass} />
                      </button>
                    </div>
                    <FieldError msg={fieldErrors['password']} />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-ink-2 tracking-wide mb-1.5">{t('confirmPassword')}</label>
                    <div className="relative">
                      <input type={showRegConfirm ? 'text' : 'password'} value={regConfirm} onChange={e => setRegConfirm(e.target.value)}
                        onKeyDown={e => e.key === 'Enter' && handleRegister()}
                        className={`${inputCls('confirmPassword')} pr-10`} />
                      <button type="button" onClick={() => setShowRegConfirm(v => !v)}
                        className="absolute inset-y-0 right-3 flex items-center text-muted hover:text-ink transition-colors">
                        <EyeIcon open={showRegConfirm} />
                      </button>
                    </div>
                    <FieldError msg={fieldErrors['confirmPassword']} />
                  </div>
                  <button onClick={handleRegister} disabled={pending || !regEmail || !regPass}
                    className="w-full bg-accent hover:bg-accent-700 disabled:opacity-40 text-white font-display font-bold text-sm uppercase tracking-wider py-3 rounded transition-colors">
                    {pending ? t('registering') : t('registerBtn')}
                  </button>
                </div>
              )}

              <p className="text-center text-sm text-muted mt-5">
                {tab === 'login' ? t('noAccount') : t('hasAccount')}{' '}
                <button type="button" onClick={() => { setTab(tab === 'login' ? 'register' : 'login'); clearErrors(); }}
                  className="text-ink font-semibold underline underline-offset-2">
                  {tab === 'login' ? t('signUpLink') : t('signInLink')}
                </button>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
