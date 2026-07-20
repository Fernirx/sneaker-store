-- Chống double-submit khi tạo đơn hàng (double-click / retry sau timeout / replay request):
-- client tạo 1 UUID cho mỗi phiên checkout, gửi qua header `Idempotency-Key`. Cột này là lớp
-- an toàn cuối cùng ở tầng DB cho trường hợp 2 request đua nhau lọt qua check ở tầng Service.
ALTER TABLE `orders` ADD COLUMN `idempotency_key` VARCHAR(36) NULL;
ALTER TABLE `orders` ADD CONSTRAINT `uq_orders_idempotency_key` UNIQUE (`idempotency_key`);
