import Link from 'next/link';

export const metadata = { title: 'Hỗ trợ — STRIDE' };

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="scroll-mt-20">
      <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-4">{title}</h2>
      <div className="space-y-3 text-sm text-ink-2 leading-relaxed max-w-3xl">{children}</div>
    </section>
  );
}

export default function HelpPage() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-16 space-y-14">
      <h1 className="font-display font-black text-3xl md:text-4xl uppercase tracking-tight">
        Hỗ trợ khách hàng
      </h1>

      <Section id="buying" title="Hướng dẫn mua hàng">
        <ol className="list-decimal list-inside space-y-2">
          <li>Chọn sản phẩm, size và màu phù hợp, sau đó bấm <span className="font-bold">Thêm vào giỏ</span>.</li>
          <li>Vào <Link href="/cart" className="underline hover:text-ink">Giỏ hàng</Link>, kiểm tra lại số lượng rồi bấm <span className="font-bold">Thanh toán</span>.</li>
          <li>Nhập/chọn địa chỉ giao hàng, xem phí vận chuyển và tổng tiền, chọn hình thức thanh toán (VNPay hoặc thanh toán khi nhận hàng).</li>
          <li>Xác nhận đặt hàng — bạn sẽ nhận được mã đơn hàng và có thể theo dõi tại <Link href="/orders" className="underline hover:text-ink">Đơn hàng của tôi</Link>.</li>
        </ol>
      </Section>

      <Section id="returns" title="Đổi trả & Bảo hành">
        <p>
          STRIDE hỗ trợ đổi trả miễn phí trong vòng <span className="font-bold">30 ngày kể từ khi nhận
          hàng</span>, áp dụng cho sản phẩm còn nguyên vẹn, đầy đủ hộp và phụ kiện đi kèm. Không áp dụng
          cho sản phẩm đã qua sử dụng hoặc đang trong chương trình khuyến mãi đặc biệt.
        </p>
        <p>
          Để tạo yêu cầu đổi/trả, vào <Link href="/orders" className="underline hover:text-ink">Đơn hàng
          của tôi</Link>, chọn đơn hàng đã giao thành công và bấm <span className="font-bold">Đổi/trả
          hàng</span>. Bạn có thể chọn hoàn tiền hoặc đổi sang size/màu khác còn hàng.
        </p>
      </Section>
    </div>
  );
}
