-- Yêu cầu đổi/trả hàng: đơn đã DELIVERED, khách chọn hoàn tiền hoặc đổi size/màu cùng sản phẩm.
-- Order.status KHÔNG đổi khi có yêu cầu đổi trả — toàn bộ vòng đời chạy độc lập trong return_requests.status.

CREATE TABLE IF NOT EXISTS `return_requests` (
  `id`                            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `order_id`                      BIGINT UNSIGNED  NOT NULL,
  `customer_id`                   BIGINT UNSIGNED  NOT NULL,
  `code`                          VARCHAR(50)      NOT NULL,
  `resolution_type`               ENUM('REFUND', 'EXCHANGE') NOT NULL,
  `status`                        ENUM('PENDING', 'APPROVED', 'RECEIVED', 'COMPLETED', 'REJECTED', 'REJECTED_AFTER_INSPECTION')
                                   NOT NULL DEFAULT 'PENDING',
  `reason`                        TEXT             NOT NULL,
  `reject_reason`                 TEXT             NULL DEFAULT NULL,
  `tracking_code`                 VARCHAR(100)     NULL DEFAULT NULL,
  `refund_amount`                 DECIMAL(15, 2)   NULL DEFAULT NULL,
  `refunded_at`                   DATETIME         NULL DEFAULT NULL,
  `exchange_shipping_order_code`  VARCHAR(50)      NULL DEFAULT NULL,
  `exchange_expected_delivery_at` DATETIME         NULL DEFAULT NULL,
  `approved_by`                   BIGINT UNSIGNED  NULL DEFAULT NULL,
  `approved_at`                   DATETIME         NULL DEFAULT NULL,
  `received_at`                   DATETIME         NULL DEFAULT NULL,
  `completed_at`                  DATETIME         NULL DEFAULT NULL,
  `admin_note`                    TEXT             NULL DEFAULT NULL,
  `created_at`                    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`                    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code`),
  INDEX `idx_return_requests_order` (`order_id`),
  INDEX `idx_return_requests_customer_status` (`customer_id`, `status`),
  CONSTRAINT `fk_return_requests_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_return_requests_customer`
    FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_return_requests_approved_by`
    FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Yêu cầu đổi/trả hàng của khách';

CREATE TABLE IF NOT EXISTS `return_request_items` (
  `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `return_request_id`   BIGINT UNSIGNED  NOT NULL,
  `order_item_id`       BIGINT UNSIGNED  NOT NULL,
  `quantity`             INT             NOT NULL,
  `exchange_variant_id` BIGINT UNSIGNED  NULL DEFAULT NULL,
  `refund_amount`       DECIMAL(15, 2)   NOT NULL,
  `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_return_items_request` (`return_request_id`),
  INDEX `idx_return_items_order_item` (`order_item_id`),
  CONSTRAINT `fk_return_items_request`
    FOREIGN KEY (`return_request_id`) REFERENCES `return_requests` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_return_items_order_item`
    FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_return_items_exchange_variant`
    FOREIGN KEY (`exchange_variant_id`) REFERENCES `product_variants` (`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Chi tiết sản phẩm trong yêu cầu đổi/trả';

CREATE TABLE IF NOT EXISTS `return_request_images` (
  `id`                 BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `return_request_id`  BIGINT UNSIGNED  NOT NULL,
  `image_public_id`    VARCHAR(255)     NOT NULL,
  `created_at`         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `image_public_id_UNIQUE` (`image_public_id`),
  INDEX `idx_return_images_request` (`return_request_id`),
  CONSTRAINT `fk_return_images_request`
    FOREIGN KEY (`return_request_id`) REFERENCES `return_requests` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Ảnh minh chứng đính kèm yêu cầu đổi/trả';
