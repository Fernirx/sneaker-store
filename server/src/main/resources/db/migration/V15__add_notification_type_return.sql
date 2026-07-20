-- Thêm RETURN để notification vòng đời đổi/trả hàng (tạo/duyệt/từ chối/không đạt kiểm tra/hoàn tất)
-- có type riêng, tách khỏi ORDER - cùng pattern đã dùng khi thêm PRODUCT ở V6.
ALTER TABLE `notifications`
  MODIFY COLUMN `type` ENUM('ORDER','PAYMENT','PROMOTION','SYSTEM','REVIEW','INVENTORY','PRODUCT','RETURN') NOT NULL;
