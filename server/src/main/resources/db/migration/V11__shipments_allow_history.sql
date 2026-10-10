-- Trước đây mỗi đơn hàng chỉ giữ tối đa 1 dòng shipment (unique order_id) - khi hủy vận đơn, dòng này bị
-- xóa cứng để có thể tạo vận đơn mới cho cùng đơn hàng, làm mất toàn bộ dữ liệu vận đơn cũ (mã vận đơn,
-- thời điểm tạo, trạng thái đồng bộ GHN...). Bỏ ràng buộc unique để mỗi lần tạo vận đơn là 1 dòng lịch sử
-- riêng, dòng cũ khi bị hủy chỉ đổi status = 'cancel', không xóa.
ALTER TABLE `shipments` DROP FOREIGN KEY `fk_shipments_order`;
ALTER TABLE `shipments` DROP INDEX `order_id_UNIQUE`;
ALTER TABLE `shipments` ADD INDEX `idx_shipments_order_id` (`order_id`);
ALTER TABLE `shipments` ADD CONSTRAINT `fk_shipments_order` FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
