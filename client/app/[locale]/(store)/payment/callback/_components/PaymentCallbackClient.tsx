'use client';

import { useEffect, useState } from 'react';
import { useTranslations } from 'next-intl';
import { Link } from '@/i18n/routing';
import clientAxios from '@/lib/axios/clientAxios';
import { guestHeaders } from '@/lib/guestToken';

type Status = 'loading' | 'success' | 'expired' | 'failed';

function resolveStatusFromCode(code: string): Status {
  if (code === '00') return 'success';
  if (code === '15') return 'expired';
  return 'failed';
}

function parseOrderId(txnRef: string): number | null {
  const id = Number(txnRef.split('_')[0]);
  return Number.isFinite(id) && id > 0 ? id : null;
}

export default function PaymentCallbackClient({
  responseCode,
  txnRef,
  amount,
}: {
  responseCode: string;
  txnRef: string;
  amount: number;
}) {
  const t = useTranslations('payment');
  const [status, setStatus]   = useState<Status>('loading');
  const [orderId, setOrderId] = useState<number | null>(null);
  const [orderCode, setOrderCode] = useState(txnRef);
  const [displayAmount, setDisplayAmount] = useState(amount);

  useEffect(() => {
    const id = parseOrderId(txnRef);
    setOrderId(id);

    if (!id) {
      setStatus(resolveStatusFromCode(responseCode));
      return;
    }

    let cancelled = false;
    clientAxios
      .get(`/api/orders/${id}`, { headers: guestHeaders() })
      .then(({ data }) => {
        if (cancelled) return;
        const order = data.data;
        setOrderCode(order.code ?? txnRef);
        setDisplayAmount(Number(order.totalAmount ?? amount));
        if (order.paymentStatus === 'PAID') {
          setStatus('success');
        } else if (order.status === 'CANCELLED') {
          setStatus(responseCode === '15' ? 'expired' : 'failed');
        } else {
          setStatus(resolveStatusFromCode(responseCode));
        }
      })
      .catch(() => {
        if (!cancelled) setStatus(resolveStatusFromCode(responseCode));
      });

    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (status === 'loading') {
    return (
      <div className="max-w-lg mx-auto px-4 py-28 flex justify-center">
        <svg className="animate-spin w-8 h-8 text-muted" viewBox="0 0 24 24" fill="none">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
      </div>
    );
  }

  const orderDetailHref = orderId ? `/orders/${orderId}` : '/orders';

  return (
    <div className="max-w-lg mx-auto px-4 py-24 flex flex-col items-center gap-6 text-center">

      {/* ── Success ── */}
      {status === 'success' && (
        <>
          <div className="w-16 h-16 rounded-full bg-ok/10 flex items-center justify-center">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="text-ok">
              <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7"/>
            </svg>
          </div>

          <div>
            <h1 className="font-display font-black text-2xl uppercase tracking-tight text-ink mb-2">
              {t('successTitle')}
            </h1>
            <p className="text-[13px] text-muted">
              {t('successBody', { code: orderCode })}
            </p>
          </div>

          <div className="border border-line rounded-sm px-6 py-4 bg-line-2 w-full text-left space-y-2.5">
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">{t('orderCodeLabel')}</span>
              <span className="font-semibold text-ink font-mono text-[12px]">{orderCode}</span>
            </div>
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">{t('amountLabel')}</span>
              <span className="font-semibold text-ink tabular-nums">
                {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(displayAmount)}
              </span>
            </div>
          </div>

          <div className="flex gap-3 w-full">
            <Link
              href={orderDetailHref}
              className="flex-1 text-[12px] font-bold uppercase tracking-widest bg-ink text-white py-3 rounded-sm hover:bg-accent transition-colors text-center"
            >
              {t('viewOrder')}
            </Link>
            <Link
              href="/products"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest border border-line text-ink py-3 rounded-sm hover:bg-paper transition-colors text-center"
            >
              {t('continueShopping')}
            </Link>
          </div>
        </>
      )}

      {/* ── Expired ── */}
      {status === 'expired' && (
        <>
          <div className="w-16 h-16 rounded-full bg-warn/10 flex items-center justify-center">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="text-warn">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>

          <div>
            <h1 className="font-display font-black text-2xl uppercase tracking-tight text-ink mb-2">
              {t('expiredTitle')}
            </h1>
            <p className="text-[13px] text-muted">
              {t('expiredBody')}
            </p>
          </div>

          <Link
            href="/checkout"
            className="w-full text-[12px] font-bold uppercase tracking-widest bg-ink text-white py-3.5 rounded-sm hover:bg-accent transition-colors text-center block"
          >
            {t('retryPayment')}
          </Link>
        </>
      )}

      {/* ── Failed ── */}
      {status === 'failed' && (
        <>
          <div className="w-16 h-16 rounded-full bg-danger/10 flex items-center justify-center">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="text-danger">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>

          <div>
            <h1 className="font-display font-black text-2xl uppercase tracking-tight text-ink mb-2">
              {t('failedTitle')}
            </h1>
            <p className="text-[13px] text-muted">
              {t('failedBody')}{' '}
              {responseCode && (
                <>{t('errorCodeLabel')} <span className="font-mono text-ink">{responseCode}</span></>
              )}
            </p>
          </div>

          <div className="flex gap-3 w-full">
            <Link
              href="/checkout"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest bg-ink text-white py-3 rounded-sm hover:bg-accent transition-colors text-center"
            >
              {t('retry')}
            </Link>
            <Link
              href="/cart"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest border border-line text-ink py-3 rounded-sm hover:bg-paper transition-colors text-center"
            >
              {t('backToCart')}
            </Link>
          </div>
        </>
      )}
    </div>
  );
}
