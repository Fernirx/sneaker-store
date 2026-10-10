-- point_transactions.reference_id trước đây dùng chung 1 cột cho 2 domain độc lập
-- (orders.id qua earnFromOrder/revokeFromOrder, return_requests.id qua revokePartial),
-- 2 sequence AUTO_INCREMENT riêng biệt nên có thể trùng số, khiến idempotency-check
-- theo (customer, reference_id, type) nhận nhầm bản ghi của domain khác.
-- Thêm cột reference_type để tách rõ 2 domain, không sửa lại V1.

ALTER TABLE `point_transactions`
  ADD COLUMN `reference_type` ENUM('ORDER', 'RETURN_REQUEST') NOT NULL DEFAULT 'ORDER' AFTER `type`;

-- Toàn bộ dữ liệu hiện có (nếu có) đều thuộc domain ORDER (revokePartial/RETURN_REQUEST
-- mới thêm ở tính năng đổi/trả hàng, chưa từng chạy thật) - default ở trên đã backfill đúng.
-- Bỏ default để buộc mọi insert sau này phải khai rõ, tránh quên.
ALTER TABLE `point_transactions`
  MODIFY COLUMN `reference_type` ENUM('ORDER', 'RETURN_REQUEST') NOT NULL;

-- Chống ghi trùng ở tầng DB (trước đây chỉ có INDEX, không có ràng buộc unique nào
-- chặn 2 request đồng thời ghi trùng 1 giao dịch điểm).
ALTER TABLE `point_transactions`
  DROP INDEX `idx_point_transactions_reference`,
  ADD UNIQUE INDEX `uq_point_transactions_customer_reference_type` (`customer_id`, `reference_type`, `reference_id`, `type`);
