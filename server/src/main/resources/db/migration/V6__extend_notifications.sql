-- Bảng notifications/notification_recipients (V1) chưa có Entity/Service nào dùng (0 dòng dữ liệu) -> an toàn để redefine ENUM.
-- Bỏ 'ADMIN' (dư thừa, đã có ROLE + target_role=ROLE_ADMIN) và không thêm CUSTOMER_TIER (đã quyết định bỏ khái niệm "nhóm khách hàng").
ALTER TABLE `notifications`
  MODIFY COLUMN `target_type` ENUM('USER','ROLE','ALL') NOT NULL;

-- Thêm PRODUCT để tách "sản phẩm mới/giảm giá" khỏi PROMOTION (coupon/campaign).
ALTER TABLE `notifications`
  MODIFY COLUMN `type` ENUM('ORDER','PAYMENT','PROMOTION','SYSTEM','REVIEW','INVENTORY','PRODUCT') NOT NULL;
