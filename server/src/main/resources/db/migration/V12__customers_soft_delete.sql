-- Customer đổi sang soft-delete giống User: hồ sơ khách hàng gắn trực tiếp với lịch sử đơn hàng/điểm
-- thưởng (giá trị tài chính/audit), không nên mất dấu khi "xóa" - trước đây hard-delete sẽ bị chặn cứng
-- (RESTRICT) hoặc mất lịch sử âm thầm (CASCADE PointTransaction) nếu khách đã phát sinh nghiệp vụ.
ALTER TABLE `customers` ADD COLUMN `deleted_at` DATETIME NULL DEFAULT NULL AFTER `updated_at`;
