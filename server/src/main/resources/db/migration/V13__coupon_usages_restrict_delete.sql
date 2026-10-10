-- Coupon vẫn cho xóa cứng (là cấu hình chiến dịch, ADMIN/MARKETING có thể tạo/xóa tự do khi chưa phát
-- sinh nghiệp vụ), nhưng coupon_id trên coupon_usages trước đây là CASCADE - xóa 1 coupon đã từng được
-- dùng sẽ âm thầm xóa luôn lịch sử áp dụng giảm giá gắn với các đơn hàng cũ (dữ liệu đối soát tài chính).
-- Đổi sang RESTRICT để khớp cùng pattern đã dùng cho Brand/Supplier (chặn xóa bằng pre-check ở service,
-- đồng thời để DB tự chặn thêm 1 lớp nếu vì lý do gì đó pre-check bị bỏ sót).
ALTER TABLE `coupon_usages` DROP FOREIGN KEY `fk_coupon_usages_coupon`;
ALTER TABLE `coupon_usages` ADD CONSTRAINT `fk_coupon_usages_coupon`
  FOREIGN KEY (`coupon_id`) REFERENCES `coupons`(`id`)
  ON DELETE RESTRICT ON UPDATE CASCADE;
