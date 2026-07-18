-- ROLE_TECHNICIAN chưa từng được gắn @PreAuthorize ở bất kỳ endpoint nào (đã rà toàn bộ codebase) -
-- role rỗng hoàn toàn, không có nghiệp vụ thực tế. Xóa dữ liệu tham chiếu (nếu có) trước khi thu hẹp ENUM.

DELETE FROM `user_roles` WHERE `role` = 'ROLE_TECHNICIAN';
UPDATE `notifications` SET `target_role` = NULL WHERE `target_role` = 'ROLE_TECHNICIAN';

ALTER TABLE `user_roles`
  MODIFY COLUMN `role` ENUM('ROLE_USER', 'ROLE_SALE', 'ROLE_WAREHOUSE', 'ROLE_MARKETING', 'ROLE_ADMIN') NOT NULL;

ALTER TABLE `notifications`
  MODIFY COLUMN `target_role` ENUM('ROLE_USER', 'ROLE_SALE', 'ROLE_WAREHOUSE', 'ROLE_MARKETING', 'ROLE_ADMIN') NULL DEFAULT NULL;
