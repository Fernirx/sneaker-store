import { Link } from '@/i18n/routing';

type Props = {
  searchParams: Promise<Record<string, string>>;
};

type Status = 'success' | 'expired' | 'failed';

function resolveStatus(code: string): Status {
  if (code === '00') return 'success';
  if (code === '15') return 'expired';
  return 'failed';
}

export default async function PaymentCallbackPage({ searchParams }: Props) {
  const params       = await searchParams;
  const responseCode = params.vnp_ResponseCode ?? '';
  const txnRef       = params.vnp_TxnRef ?? '';
  const amount       = Number(params.vnp_Amount ?? 0) / 100;
  const status       = resolveStatus(responseCode);

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
              Thanh toán thành công
            </h1>
            <p className="text-[13px] text-muted">
              Đơn hàng <span className="font-semibold text-ink">{txnRef}</span> đã được thanh toán.
            </p>
          </div>

          <div className="border border-line rounded-sm px-6 py-4 bg-line-2 w-full text-left space-y-2.5">
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">Mã giao dịch:</span>
              <span className="font-semibold text-ink font-mono text-[12px]">{txnRef}</span>
            </div>
            <div className="flex justify-between text-[13px]">
              <span className="text-muted">Số tiền:</span>
              <span className="font-semibold text-ink tabular-nums">
                {new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)}
              </span>
            </div>
          </div>

          <div className="flex gap-3 w-full">
            <Link
              href="/orders"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest bg-ink text-white py-3 rounded-sm hover:bg-accent transition-colors text-center"
            >
              Xem đơn hàng
            </Link>
            <Link
              href="/products"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest border border-line text-ink py-3 rounded-sm hover:bg-paper transition-colors text-center"
            >
              Tiếp tục mua sắm
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
              Phiên thanh toán hết hạn
            </h1>
            <p className="text-[13px] text-muted">
              Link thanh toán đã hết hiệu lực. Vui lòng thử lại.
            </p>
          </div>

          <Link
            href="/checkout"
            className="w-full text-[12px] font-bold uppercase tracking-widest bg-ink text-white py-3.5 rounded-sm hover:bg-accent transition-colors text-center block"
          >
            Thanh toán lại
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
              Thanh toán chưa thành công
            </h1>
            <p className="text-[13px] text-muted">
              Giao dịch không được hoàn tất.{' '}
              {responseCode && (
                <>Mã lỗi: <span className="font-mono text-ink">{responseCode}</span></>
              )}
            </p>
          </div>

          <div className="flex gap-3 w-full">
            <Link
              href="/checkout"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest bg-ink text-white py-3 rounded-sm hover:bg-accent transition-colors text-center"
            >
              Thử lại
            </Link>
            <Link
              href="/cart"
              className="flex-1 text-[12px] font-bold uppercase tracking-widest border border-line text-ink py-3 rounded-sm hover:bg-paper transition-colors text-center"
            >
              Quay lại giỏ hàng
            </Link>
          </div>
        </>
      )}
    </div>
  );
}
