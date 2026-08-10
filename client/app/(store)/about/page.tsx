export const metadata = { title: 'Giới thiệu & Liên hệ — STRIDE' };

export default function AboutPage() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-16 space-y-16">
      {/* Giới thiệu */}
      <section>
        <h1 className="font-display font-black text-3xl md:text-4xl uppercase tracking-tight mb-8">
          Về STRIDE
        </h1>
        <div className="space-y-6 text-sm md:text-base text-ink-2 leading-relaxed max-w-4xl">
          <p>
            Được thành lập từ năm 2026, <strong>STRIDE</strong> không chỉ là một nền tảng bán lẻ trực tuyến chuyên về sneaker, mà còn là điểm đến lý tưởng dành cho những người đam mê văn hóa đường phố (streetwear) tại Việt Nam. Chúng tôi tin rằng mỗi đôi giày không chỉ là một phụ kiện thời trang, mà còn là một câu chuyện, một cách để thể hiện cá tính và bản sắc riêng của mỗi cá nhân.
          </p>
          <p>
            Tại STRIDE, chúng tôi tự hào mang đến cho khách hàng bộ sưu tập sneaker đa dạng, từ những dòng giày huyền thoại vượt thời gian của Nike, adidas, New Balance, đến những phiên bản collab giới hạn (limited edition) hiếm có khó tìm.
          </p>
        </div>
      </section>

      {/* Tầm nhìn & Sứ mệnh */}
      <section>
        <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-6">
          Tầm nhìn & Sứ mệnh
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-paper p-8 rounded-sm">
            <h3 className="font-bold text-lg mb-3">Tầm nhìn</h3>
            <p className="text-sm text-ink-2 leading-relaxed">
              Trở thành nền tảng phân phối sneaker và thời trang đường phố uy tín số 1 Việt Nam, là cầu nối vững chắc đưa văn hóa sneaker toàn cầu đến gần hơn với giới trẻ Việt.
            </p>
          </div>
          <div className="bg-paper p-8 rounded-sm">
            <h3 className="font-bold text-lg mb-3">Sứ mệnh</h3>
            <p className="text-sm text-ink-2 leading-relaxed">
              Cung cấp những sản phẩm chính hãng 100% với chất lượng hoàn hảo. Đi kèm với đó là trải nghiệm mua sắm tuyệt vời, dịch vụ khách hàng tận tâm và các chính sách hậu mãi minh bạch.
            </p>
          </div>
        </div>
      </section>

      {/* Cam kết */}
      <section>
        <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-6">
          Cam kết của chúng tôi
        </h2>
        <ul className="space-y-4 text-sm md:text-base text-ink-2 leading-relaxed max-w-4xl list-disc pl-5">
          <li><strong>Chính hãng 100%:</strong> Mọi sản phẩm tại STRIDE đều trải qua quá trình kiểm định nghiêm ngặt (legit check) trước khi đến tay khách hàng. Khách hàng được hoàn tiền 100% nếu phát hiện hàng giả, hàng nhái.</li>
          <li><strong>Dịch vụ tận tâm:</strong> Đội ngũ tư vấn viên am hiểu sâu sắc về sneaker luôn sẵn sàng hỗ trợ bạn tìm được đôi giày vừa vặn và ưng ý nhất.</li>
          <li><strong>Giao hàng hỏa tốc:</strong> Đối tác vận chuyển chuyên nghiệp đảm bảo sản phẩm luôn đến tay bạn trong tình trạng nguyên vẹn và nhanh chóng nhất.</li>
        </ul>
      </section>

      {/* Liên hệ */}
      <section id="contact">
        <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-6">
          Thông tin liên hệ
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <div className="border border-line rounded-sm p-6 bg-white hover:border-ink transition-colors">
            <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">Hotline Hỗ Trợ</p>
            <p className="text-lg font-bold text-ink">1900 6750</p>
            <p className="text-sm text-muted mt-2">Từ 8:00 đến 21:00 (Thứ 2 - Chủ Nhật)</p>
          </div>
          <div className="border border-line rounded-sm p-6 bg-white hover:border-ink transition-colors">
            <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-2">Email Hỗ Trợ</p>
            <p className="text-lg font-bold text-ink">hotro@stride.vn</p>
            <p className="text-sm text-muted mt-2">Chúng tôi sẽ phản hồi trong vòng 24h làm việc</p>
          </div>
        </div>
      </section>
    </div>
  );
}
