-- V20: Thêm trạng thái REFUNDED cho cột payment_status của orders

ALTER TABLE `orders` 
MODIFY COLUMN `payment_status` ENUM('UNPAID', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'UNPAID';
