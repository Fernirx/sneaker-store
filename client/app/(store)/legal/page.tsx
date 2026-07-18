export const metadata = { title: 'Chính sách — STRIDE' };

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="scroll-mt-20">
      <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-4">{title}</h2>
      <div className="space-y-3 text-sm text-ink-2 leading-relaxed max-w-3xl">{children}</div>
    </section>
  );
}

export default function LegalPage() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-16 space-y-14">
      <h1 className="font-display font-black text-3xl md:text-4xl uppercase tracking-tight">
        Điều khoản & Chính sách
      </h1>

      <Section id="terms" title="Điều khoản dịch vụ">
        <p>
          Khi đặt hàng trên STRIDE, bạn xác nhận thông tin cung cấp (họ tên, số điện thoại, địa chỉ giao
          hàng) là chính xác. STRIDE có quyền từ chối hoặc hủy đơn hàng nếu phát hiện thông tin không hợp
          lệ hoặc có dấu hiệu gian lận.
        </p>
        <p>
          Giá bán, khuyến mãi và tình trạng còn hàng có thể thay đổi mà không cần báo trước. Đơn hàng chỉ
          được xác nhận sau khi thanh toán thành công (VNPay) hoặc được xác nhận qua điện thoại/email đối
          với hình thức thanh toán khi nhận hàng (COD).
        </p>
      </Section>

      <Section id="privacy" title="Bảo mật thông tin">
        <p>
          STRIDE thu thập thông tin cá nhân (họ tên, số điện thoại, địa chỉ, lịch sử mua hàng) chỉ nhằm
          mục đích xử lý đơn hàng, giao vận và chăm sóc khách hàng. Thông tin thanh toán được xử lý trực
          tiếp qua cổng VNPay, STRIDE không lưu trữ số thẻ/tài khoản ngân hàng của khách hàng.
        </p>
        <p>
          Chúng tôi không chia sẻ, bán hoặc trao đổi thông tin cá nhân của khách hàng cho bên thứ ba, trừ
          trường hợp cần thiết để hoàn tất việc giao hàng (đơn vị vận chuyển) hoặc theo yêu cầu của cơ
          quan pháp luật có thẩm quyền.
        </p>
      </Section>

      <Section id="cookies" title="Chính sách Cookie">
        <p>
          Website sử dụng cookie để ghi nhớ phiên đăng nhập, giỏ hàng và cải thiện trải nghiệm duyệt web
          của bạn. Cookie không được dùng để thu thập thông tin nhạy cảm ngoài phạm vi vận hành website.
        </p>
        <p>
          Bạn có thể tắt cookie trong cài đặt trình duyệt, tuy nhiên một số chức năng (đăng nhập, giỏ
          hàng) có thể không hoạt động đúng nếu cookie bị chặn hoàn toàn.
        </p>
      </Section>
    </div>
  );
}
