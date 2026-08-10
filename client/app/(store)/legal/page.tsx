export const metadata = { title: 'Chính sách — STRIDE' };

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="scroll-mt-24">
      <h2 className="font-display font-black text-2xl uppercase tracking-tight mb-6 pb-2 border-b border-line">{title}</h2>
      <div className="space-y-4 text-sm md:text-base text-ink-2 leading-relaxed max-w-4xl">{children}</div>
    </section>
  );
}

export default function LegalPage() {
  return (
    <div className="max-w-5xl mx-auto px-6 py-16 space-y-16">
      <h1 className="font-display font-black text-3xl md:text-4xl uppercase tracking-tight">
        Điều khoản & Chính sách
      </h1>

      <Section id="terms" title="1. Điều khoản dịch vụ">
        <h3 className="font-bold text-lg text-ink mt-2">1.1 Trách nhiệm của khách hàng</h3>
        <p>
          Khi truy cập và mua sắm trên website STRIDE, khách hàng đồng ý tuân thủ các quy định và hướng dẫn của chúng tôi. Bạn có trách nhiệm cung cấp thông tin cá nhân (họ tên, số điện thoại, địa chỉ giao hàng) chính xác và cập nhật để đảm bảo quá trình giao nhận diễn ra thuận lợi.
        </p>
        <p>
          STRIDE có quyền từ chối phục vụ, hủy đơn hàng, hoặc khóa tài khoản vĩnh viễn nếu phát hiện có hành vi gian lận, lạm dụng hệ thống khuyến mãi, hoặc sử dụng website cho các mục đích trái pháp luật.
        </p>

        <h3 className="font-bold text-lg text-ink mt-6">1.2 Giá cả và Sản phẩm</h3>
        <p>
          Mọi thông tin về giá bán, khuyến mãi và tình trạng còn hàng (Stock) trên website có thể được thay đổi mà không cần thông báo trước. Dù chúng tôi luôn cố gắng đảm bảo thông tin hiển thị chính xác nhất, trong một số trường hợp hiếm hoi (lỗi hệ thống, biến động kho), giá trị đơn hàng có thể bị sai lệch. Khi đó, STRIDE sẽ chủ động liên hệ để thông báo và xin ý kiến khách hàng trước khi tiến hành hủy hoặc điều chỉnh đơn.
        </p>

        <h3 className="font-bold text-lg text-ink mt-6">1.3 Trách nhiệm của STRIDE</h3>
        <p>
          Chúng tôi cam kết mang đến những sản phẩm chính hãng với chất lượng đúng như mô tả. Đội ngũ CSKH có trách nhiệm giải quyết mọi khiếu nại của khách hàng trong thời gian sớm nhất, dựa trên tinh thần hợp tác và đảm bảo quyền lợi tối đa cho người mua.
        </p>
      </Section>

      <Section id="privacy" title="2. Chính sách Bảo mật thông tin">
        <h3 className="font-bold text-lg text-ink mt-2">2.1 Mục đích thu thập</h3>
        <p>
          STRIDE thu thập thông tin cá nhân (họ tên, số điện thoại, địa chỉ email, địa chỉ nhận hàng, và lịch sử mua sắm) nhằm các mục đích cốt lõi: 
          (1) Xử lý đơn đặt hàng và bàn giao cho đơn vị vận chuyển.
          (2) Cung cấp các thông tin liên quan đến dịch vụ khách hàng (thay đổi trạng thái đơn, giải quyết khiếu nại).
          (3) Gửi email thông báo về các ưu đãi đặc biệt (nếu khách hàng đồng ý nhận).
        </p>

        <h3 className="font-bold text-lg text-ink mt-6">2.2 Bảo vệ thông tin</h3>
        <p>
          Thông tin của bạn được lưu trữ an toàn trên máy chủ của chúng tôi với các biện pháp mã hóa tiêu chuẩn hiện đại. Đối với thanh toán trực tuyến, mọi dữ liệu thẻ/tài khoản ngân hàng của bạn được xử lý trực tiếp bởi hệ thống bảo mật của cổng thanh toán VNPay. <strong>STRIDE tuyệt đối không lưu trữ bất kỳ thông tin số thẻ hay mật khẩu ngân hàng nào của bạn.</strong>
        </p>

        <h3 className="font-bold text-lg text-ink mt-6">2.3 Chia sẻ thông tin</h3>
        <p>
          Chúng tôi cam kết không bán, trao đổi hoặc chia sẻ thông tin cá nhân của bạn cho bất kỳ bên thứ ba nào vì mục đích thương mại. Thông tin chỉ được chia sẻ trong 2 trường hợp: (1) Cung cấp cho đối tác vận chuyển để thực hiện việc giao hàng; (2) Khi có yêu cầu hợp lệ từ các cơ quan pháp luật có thẩm quyền.
        </p>
      </Section>

      <Section id="cookies" title="3. Chính sách Cookie">
        <p>
          Cookie là các tập tin văn bản nhỏ được lưu trữ trên thiết bị của bạn khi bạn truy cập website. STRIDE sử dụng Cookie để:
        </p>
        <ul className="list-disc list-inside space-y-2 ml-2">
          <li>Ghi nhớ trạng thái đăng nhập, giúp bạn không phải nhập lại mật khẩu liên tục.</li>
          <li>Ghi nhớ các sản phẩm trong giỏ hàng và các tùy chọn cá nhân hóa của bạn.</li>
          <li>Thu thập dữ liệu thống kê (ẩn danh) qua Google Analytics để đo lường lượng truy cập và cải thiện trải nghiệm giao diện.</li>
        </ul>
        <p className="mt-4">
          Bạn hoàn toàn có quyền chủ động quản lý hoặc từ chối Cookie trong phần Cài đặt của trình duyệt web. Tuy nhiên, việc tắt toàn bộ Cookie có thể khiến một số tính năng cốt lõi (như giỏ hàng, đăng nhập) hoạt động không ổn định.
        </p>
      </Section>

      <div className="bg-paper p-6 mt-8 rounded-sm">
        <p className="text-sm text-muted italic">
          Bản quyền thuộc về STRIDE. Nội dung các điều khoản và chính sách trên có hiệu lực từ ngày 01/01/2026. Lần cập nhật gần nhất: 10/08/2026.
        </p>
      </div>
    </div>
  );
}
