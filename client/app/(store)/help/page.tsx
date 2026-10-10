import Link from 'next/link';

export const metadata = { title: 'Hỗ trợ — STRIDE' };

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="scroll-mt-24">
      <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-6 pb-2 border-b border-line">{title}</h2>
      <div className="space-y-4 text-sm md:text-base text-ink-2 leading-relaxed max-w-4xl">{children}</div>
    </section>
  );
}

export default function HelpPage() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-16 space-y-16">
      <h1 className="font-display font-black text-3xl md:text-4xl uppercase tracking-tight">
        Trung tâm hỗ trợ khách hàng
      </h1>

      <Section id="buying" title="Hướng dẫn mua hàng & Thanh toán">
        <h3 className="font-bold text-lg text-ink mt-2">1. Quy trình đặt hàng</h3>
        <ul className="list-disc list-inside space-y-2 ml-2">
          <li><strong>Bước 1:</strong> Khám phá sản phẩm qua các danh mục hoặc sử dụng thanh tìm kiếm.</li>
          <li><strong>Bước 2:</strong> Tại trang chi tiết, chọn kích cỡ (Size) phù hợp và bấm <span className="font-bold">Thêm vào giỏ</span> hoặc <span className="font-bold">Mua ngay</span>.</li>
          <li><strong>Bước 3:</strong> Chuyển đến <Link href="/cart" className="underline font-semibold hover:text-ink">Giỏ hàng</Link>, kiểm tra lại số lượng và giá tiền. Bấm <span className="font-bold">Tiến hành thanh toán</span>.</li>
          <li><strong>Bước 4:</strong> Điền đầy đủ thông tin giao hàng (Tên, SĐT, Địa chỉ).</li>
          <li><strong>Bước 5:</strong> Chọn phương thức thanh toán và hoàn tất đơn hàng. Một email xác nhận kèm mã vận đơn sẽ được gửi đến bạn.</li>
        </ul>

        <h3 className="font-bold text-lg text-ink mt-6">2. Phương thức thanh toán</h3>
        <p>STRIDE hiện hỗ trợ 2 phương thức thanh toán linh hoạt:</p>
        <ul className="list-disc list-inside space-y-2 ml-2">
          <li><strong>Thanh toán trực tuyến (VNPay):</strong> An toàn, nhanh chóng qua thẻ ATM nội địa, thẻ quốc tế (Visa/Mastercard) hoặc quét mã QR ứng dụng ngân hàng. Đơn hàng được xác nhận ngay lập tức.</li>
          <li><strong>Thanh toán khi nhận hàng (COD):</strong> Khách hàng thanh toán bằng tiền mặt cho nhân viên giao hàng khi nhận sản phẩm. Khách hàng được quyền đồng kiểm (kiểm tra ngoại quan, không thử) trước khi thanh toán.</li>
        </ul>
      </Section>

      <Section id="shipping" title="Chính sách Vận chuyển & Giao nhận">
        <p>Tất cả đơn hàng của STRIDE được vận chuyển bởi các đối tác uy tín (GHN, GHTK).</p>
        
        <h3 className="font-bold text-lg text-ink mt-4">Thời gian giao hàng dự kiến</h3>
        <ul className="list-disc list-inside space-y-2 ml-2">
          <li><strong>Nội thành TP.HCM:</strong> Giao hàng trong vòng 1-2 ngày làm việc.</li>
          <li><strong>Các tỉnh thành khác:</strong> Giao hàng trong vòng 3-5 ngày làm việc tùy khu vực.</li>
        </ul>
        <p className="text-muted text-[13px] italic">* Thời gian giao hàng không tính các ngày Lễ, Tết hoặc thời điểm có dịch bệnh, thiên tai.</p>

      </Section>

      <Section id="returns" title="Chính sách Đổi trả & Bảo hành">
        <h3 className="font-bold text-lg text-ink mt-2">1. Quy định đổi trả</h3>
        <p>
          STRIDE mang đến cho bạn sự an tâm tuyệt đối với chính sách đổi/trả linh hoạt trong vòng <span className="font-bold text-ink">30 ngày</span> kể từ ngày nhận hàng. 
        </p>
        <p><strong>Điều kiện áp dụng:</strong></p>
        <ul className="list-disc list-inside space-y-2 ml-2">
          <li>Sản phẩm chưa qua sử dụng, chưa giặt ủi, không có mùi lạ.</li>
          <li>Sản phẩm còn nguyên tem mạc, hộp đựng (box) nguyên vẹn không bị móp méo hay rách.</li>
          <li>Có đầy đủ các phụ kiện, quà tặng đi kèm (nếu có) và hóa đơn mua hàng.</li>
        </ul>
        <p className="text-danger mt-2 text-sm">* Không áp dụng đổi trả đối với các sản phẩm nằm trong chương trình sale đặc biệt hoặc xả hàng (Clearance).</p>

        <h3 className="font-bold text-lg text-ink mt-6">2. Quy trình đổi trả</h3>
        <ol className="list-decimal list-inside space-y-2 ml-2">
          <li>Truy cập vào trang <Link href="/orders" className="underline font-semibold hover:text-ink">Đơn hàng của tôi</Link> (nếu đã đăng nhập).</li>
          <li>Chọn đơn hàng cần hỗ trợ và nhấn <strong>Đổi/Trả Hàng</strong>.</li>
          <li>Điền lý do và cung cấp hình ảnh minh họa tình trạng sản phẩm hiện tại.</li>
          <li>Bộ phận CSKH sẽ liên hệ xác nhận và điều phối bưu tá đến lấy hàng tận nơi miễn phí.</li>
        </ol>
      </Section>

      <div className="bg-paper p-6 md:p-8 rounded-sm text-center">
        <h3 className="font-display font-black text-xl uppercase mb-2">Cần hỗ trợ thêm?</h3>
        <p className="text-sm text-muted mb-6">Đội ngũ CSKH của chúng tôi luôn sẵn sàng giải đáp mọi thắc mắc của bạn.</p>
        <Link href="/about#contact" className="inline-block bg-ink text-white font-bold text-sm uppercase tracking-wider px-8 py-3.5 hover:bg-accent transition-colors rounded-sm">
          Liên hệ ngay
        </Link>
      </div>
    </div>
  );
}
