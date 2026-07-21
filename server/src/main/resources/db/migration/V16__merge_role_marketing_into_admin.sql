-- MARKETING gộp vào ADMIN (chủ shop tự quản lý nội dung quảng bá/khuyến mãi thay vì tách riêng 1 vai trò
-- nội bộ - khác với SALE/WAREHOUSE vốn có ranh giới vật lý/lịch trình công việc không thể kiêm nhiệm).
-- Xóa trước dòng trùng (user đã có sẵn ADMIN) để tránh vi phạm unique (user_id, role) khi update dòng
-- MARKETING còn lại sang ADMIN.
DELETE ur1 FROM `user_roles` ur1
  INNER JOIN `user_roles` ur2 ON ur1.user_id = ur2.user_id AND ur2.role = 'ROLE_ADMIN'
  WHERE ur1.role = 'ROLE_MARKETING';

UPDATE `user_roles` SET `role` = 'ROLE_ADMIN' WHERE `role` = 'ROLE_MARKETING';
UPDATE `notifications` SET `target_role` = 'ROLE_ADMIN' WHERE `target_role` = 'ROLE_MARKETING';

ALTER TABLE `user_roles`
  MODIFY COLUMN `role` ENUM('ROLE_USER', 'ROLE_SALE', 'ROLE_WAREHOUSE', 'ROLE_ADMIN') NOT NULL;

ALTER TABLE `notifications`
  MODIFY COLUMN `target_role` ENUM('ROLE_USER', 'ROLE_SALE', 'ROLE_WAREHOUSE', 'ROLE_ADMIN') NULL DEFAULT NULL;
