export const metadata = { title: 'Giới thiệu & Liên hệ — STRIDE' };

export default function AboutPage() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-16 space-y-16">
      {/* Giới thiệu */}
      <section>
        <h1 className="font-display font-black text-3xl md:text-4xl uppercase tracking-tight mb-5">
          Giới thiệu
        </h1>
        <div className="space-y-4 text-sm md:text-base text-ink-2 leading-relaxed max-w-3xl">
          <p>
            STRIDE là điểm đến dành cho những người yêu văn hóa sneaker đường phố. Chúng tôi tuyển chọn
            các mẫu giày chính hãng 100% từ những thương hiệu thể thao và lifestyle hàng đầu, từ hàng mới
            về mỗi tuần cho tới các bản phối giới hạn.
          </p>
          <p>
            Mỗi đôi giày bán ra đều được kiểm tra kỹ trước khi giao, đóng gói cẩn thận và giao nhanh toàn
            quốc. Đội ngũ STRIDE luôn sẵn sàng hỗ trợ bạn chọn đúng size, đúng mẫu và xử lý đổi trả nhanh
            gọn nếu có vấn đề phát sinh.
          </p>
        </div>
      </section>

      {/* Liên hệ */}
      <section id="contact">
        <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-5">
          Liên hệ
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="border border-line rounded-sm p-5 bg-white">
            <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-1.5">Hotline</p>
            <p className="text-sm font-bold">1900 6750</p>
            <p className="text-xs text-muted mt-1">8:00 - 21:00, tất cả các ngày</p>
          </div>
          <div className="border border-line rounded-sm p-5 bg-white">
            <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-1.5">Email</p>
            <p className="text-sm font-bold">hotro@stride.vn</p>
            <p className="text-xs text-muted mt-1">Phản hồi trong 24 giờ làm việc</p>
          </div>
          <div className="border border-line rounded-sm p-5 bg-white">
            <p className="text-[11px] font-bold uppercase tracking-wider text-muted mb-1.5">Cửa hàng</p>
            <p className="text-sm font-bold">123 Nguyễn Trãi, Q.5, TP.HCM</p>
            <p className="text-xs text-muted mt-1">Xem hàng trực tiếp trong giờ hành chính</p>
          </div>
        </div>
      </section>
    </div>
  );
}
