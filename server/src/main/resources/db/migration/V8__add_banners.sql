-- Banner quảng bá hiển thị trang chủ. `active` là công tắc thủ công của admin,
-- độc lập với cửa sổ start_at/end_at (lọc chính xác theo giờ tại query-time, không dùng scheduler).

CREATE TABLE IF NOT EXISTS `banners` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title`            VARCHAR(150)    NOT NULL,
  `image_public_id`  VARCHAR(255)    NOT NULL,
  `link_url`         VARCHAR(500)    NULL DEFAULT NULL,
  `display_order`    INT             NOT NULL DEFAULT 0,
  `active`           BOOLEAN         NOT NULL DEFAULT TRUE,
  `start_at`         DATETIME        NULL DEFAULT NULL,
  `end_at`           DATETIME        NULL DEFAULT NULL,
  `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_banners_active_order` (`active`, `display_order`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Banner quảng bá hiển thị trang chủ';
