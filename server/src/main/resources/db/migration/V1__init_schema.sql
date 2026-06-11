-- Model: Sneaker Store E-commerce Database
-- Initial database schema for Sneaker Store e-commerce

-- =====================================================
-- MODULE: user
-- =====================================================

CREATE TABLE IF NOT EXISTS `users` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `email`       VARCHAR(100)     NOT NULL,
  `password`    VARCHAR(255)     NULL DEFAULT NULL,
  `active`      BOOLEAN          NOT NULL DEFAULT TRUE,
  `verified_at` DATETIME         NULL DEFAULT NULL,
  `deleted_at`  DATETIME         NULL DEFAULT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Tài khoản - xác thực và phân quyền';


CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id`    BIGINT UNSIGNED  NOT NULL,
  `role`       ENUM('ROLE_USER', 'ROLE_SALE', 'ROLE_WAREHOUSE', 'ROLE_MARKETING', 'ROLE_TECHNICIAN', 'ROLE_ADMIN') NOT NULL,
  `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `role`),
  INDEX `idx_user_roles_role` (`role`),
  CONSTRAINT `fk_user_roles_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Vai trò của người dùng - hỗ trợ kiêm nhiệm nhiều vai trò';


CREATE TABLE IF NOT EXISTS `user_oauth` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED  NOT NULL,
  `provider`    ENUM('GOOGLE', 'FACEBOOK') NOT NULL,
  `provider_id` VARCHAR(255)     NOT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `provider_id_UNIQUE` (`provider`, `provider_id`),
  INDEX `idx_oauth_user` (`user_id`),
  CONSTRAINT `fk_user_oauth_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'OAuth provider - 1 user có thể link nhiều provider';


CREATE TABLE IF NOT EXISTS `user_profiles` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`          BIGINT UNSIGNED  NOT NULL,
  `first_name`       VARCHAR(100)     NOT NULL,
  `last_name`        VARCHAR(100)     NULL DEFAULT NULL,
  `phone`            VARCHAR(15)      NULL DEFAULT NULL,
  `date_of_birth`    DATE             NULL DEFAULT NULL,
  `avatar_url`       VARCHAR(500)     NULL DEFAULT NULL,
  `avatar_public_id` VARCHAR(255)     NULL DEFAULT NULL,
  `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_id_UNIQUE` (`user_id`),
  INDEX `idx_profiles_name` (`last_name`, `first_name`),
  CONSTRAINT `fk_user_profiles_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Thông tin cá nhân - identity';


CREATE TABLE IF NOT EXISTS `customers` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT UNSIGNED  NOT NULL,
  `loyalty_points`  INT UNSIGNED     NOT NULL DEFAULT 0,
  `total_spent`     DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `membership_tier` ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') NOT NULL DEFAULT 'BRONZE',
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_id_UNIQUE` (`user_id`),
  CONSTRAINT `fk_customers_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Thông tin mua hàng - tạo khi đặt hàng lần đầu';


CREATE TABLE IF NOT EXISTS `addresses` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `customer_id` BIGINT UNSIGNED  NOT NULL,
  `name`        VARCHAR(200)     NOT NULL,
  `phone`       VARCHAR(15)      NOT NULL,
  `street`      VARCHAR(255)     NOT NULL,
  `ward`        VARCHAR(100)     NULL DEFAULT NULL,
  `district`    VARCHAR(100)     NOT NULL,
  `province`    VARCHAR(100)     NOT NULL,
  `postal_code` VARCHAR(20)      NULL DEFAULT NULL,
  `default_address` BOOLEAN       NOT NULL DEFAULT FALSE,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_addresses_customer` (`customer_id`),
  INDEX `idx_addresses_phone` (`phone`),
  CONSTRAINT `fk_addresses_customer`
    FOREIGN KEY (`customer_id`) REFERENCES `customers`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Địa chỉ giao hàng - nhiều địa chỉ per customer';


-- =====================================================
-- MODULE: catalog
-- =====================================================

CREATE TABLE IF NOT EXISTS `categories` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `parent_id`       BIGINT UNSIGNED  NULL DEFAULT NULL,
  `name`            VARCHAR(100)     NOT NULL,
  `slug`            VARCHAR(100)     NOT NULL,
  `description`     TEXT             NULL DEFAULT NULL,
  `image_url`       VARCHAR(500)     NULL DEFAULT NULL,
  `image_public_id` VARCHAR(255)     NULL DEFAULT NULL,
  `display_order`   INT              NOT NULL DEFAULT 0,
  `active`          BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name`),
  UNIQUE INDEX `slug_UNIQUE` (`slug`),
  INDEX `idx_categories_parent_active` (`parent_id`, `active`),
  CONSTRAINT `fk_categories_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `categories`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Danh mục sản phẩm - hỗ trợ phân cấp đa tầng';


CREATE TABLE IF NOT EXISTS `brands` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(100)     NOT NULL,
  `slug`            VARCHAR(100)     NOT NULL,
  `description`     TEXT             NULL DEFAULT NULL,
  `logo_url`        VARCHAR(500)     NULL DEFAULT NULL,
  `logo_public_id`  VARCHAR(255)     NULL DEFAULT NULL,
  `active`          BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name`),
  UNIQUE INDEX `slug_UNIQUE` (`slug`),
  INDEX `idx_brands_active` (`active`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Thương hiệu sneaker';


CREATE TABLE IF NOT EXISTS `collections` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(100)     NOT NULL,
  `slug`            VARCHAR(100)     NOT NULL,
  `description`     TEXT             NULL DEFAULT NULL,
  `image_url`       VARCHAR(500)     NULL DEFAULT NULL,
  `image_public_id` VARCHAR(255)     NULL DEFAULT NULL,
  `launch_date`     DATE             NULL DEFAULT NULL,
  `end_date`        DATE             NULL DEFAULT NULL,
  `active`          BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name`),
  UNIQUE INDEX `slug_UNIQUE` (`slug`),
  INDEX `idx_collections_active_dates` (`active`, `launch_date`, `end_date`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Bộ sưu tập sneaker';


-- =====================================================
-- MODULE: product
-- =====================================================

CREATE TABLE IF NOT EXISTS `products` (
  `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `brand_id`       BIGINT UNSIGNED  NOT NULL,
  `code`           VARCHAR(50)      NOT NULL,
  `style_code`     VARCHAR(50)      NULL DEFAULT NULL,
  `name`           VARCHAR(255)     NOT NULL,
  `slug`           VARCHAR(255)     NOT NULL,
  `description`    TEXT             NULL DEFAULT NULL,
  `gender`         ENUM('MEN', 'WOMEN', 'UNISEX', 'KIDS') NOT NULL DEFAULT 'UNISEX',
  `upper_material` VARCHAR(100)     NULL DEFAULT NULL,
  `sole_type`      VARCHAR(100)     NULL DEFAULT NULL,
  `closure_type`   ENUM('LACE', 'SLIP_ON', 'VELCRO', 'ZIPPER') NULL DEFAULT NULL,
  `shaft_style`    ENUM('LOW', 'MID', 'HIGH') NULL DEFAULT NULL,
  `base_price`     DECIMAL(15,2)    NOT NULL,
  `original_price` DECIMAL(15,2)    NULL DEFAULT NULL,
  `cost_price`     DECIMAL(15,2)    NULL DEFAULT NULL,
  `new_arrival`    BOOLEAN          NOT NULL DEFAULT FALSE,
  `on_sale`        BOOLEAN          NOT NULL DEFAULT FALSE,
  `active`         BOOLEAN          NOT NULL DEFAULT TRUE,
  `sold_count`     INT UNSIGNED     NOT NULL DEFAULT 0,
  `view_count`     INT UNSIGNED     NOT NULL DEFAULT 0,
  `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code`),
  UNIQUE INDEX `slug_UNIQUE` (`slug`),
  INDEX `idx_products_style_code` (`style_code`),
  INDEX `idx_products_brand_active` (`brand_id`, `active`),
  INDEX `idx_products_gender` (`gender`),
  INDEX `idx_products_badges` (`new_arrival`, `on_sale`),
  CONSTRAINT `fk_products_brand`
    FOREIGN KEY (`brand_id`) REFERENCES `brands`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Sản phẩm sneaker';


CREATE TABLE IF NOT EXISTS `product_variants` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `product_id`      BIGINT UNSIGNED  NOT NULL,
  `size`            TINYINT UNSIGNED NOT NULL,
  `shoe_width`      ENUM('NARROW', 'REGULAR', 'WIDE') NOT NULL DEFAULT 'REGULAR',
  `colorway`        VARCHAR(100)     NOT NULL,
  `colorway_code`   VARCHAR(50)      NULL DEFAULT NULL,
  `color_hex`       VARCHAR(7)       NULL DEFAULT NULL,
  `price`           DECIMAL(15,2)    NULL DEFAULT NULL,
  `sku`             VARCHAR(100)     NOT NULL,
  `stock_quantity`  INT              NOT NULL DEFAULT 0,
  `min_stock_level` INT              NOT NULL DEFAULT 5,
  `display_order`   INT              NOT NULL DEFAULT 0,
  `active`          BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `sku_UNIQUE` (`sku`),
  UNIQUE INDEX `variant_UNIQUE` (`product_id`, `size`, `colorway`, `shoe_width`),
  INDEX `idx_variants_product_order` (`product_id`, `display_order`),
  INDEX `idx_variants_product_active` (`product_id`, `active`),
  INDEX `idx_variants_stock` (`stock_quantity`),
  CONSTRAINT `fk_variants_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Biến thể sneaker';


CREATE TABLE IF NOT EXISTS `product_images` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `product_id`      BIGINT UNSIGNED  NOT NULL,
  `colorway`        VARCHAR(100)     NOT NULL,
  `color_hex`       VARCHAR(7)       NULL DEFAULT NULL,
  `image_url`       VARCHAR(500)     NOT NULL,
  `image_public_id` VARCHAR(255)     NOT NULL,
  `primary_image`   BOOLEAN          NOT NULL DEFAULT FALSE,
  `display_order`   INT              NOT NULL DEFAULT 0,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `image_public_id_UNIQUE` (`image_public_id`),
  INDEX `idx_images_product_colorway` (`product_id`, `colorway`),
  INDEX `idx_images_primary` (`product_id`, `colorway`, `primary_image`),
  CONSTRAINT `fk_images_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Ảnh sneaker';


CREATE TABLE IF NOT EXISTS `product_categories` (
  `product_id`  BIGINT UNSIGNED  NOT NULL,
  `category_id` BIGINT UNSIGNED  NOT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`, `category_id`),
  INDEX `idx_product_categories_category` (`category_id`),
  CONSTRAINT `fk_product_categories_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_product_categories_category`
    FOREIGN KEY (`category_id`) REFERENCES `categories`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Quan hệ nhiều-nhiều sản phẩm - danh mục';


CREATE TABLE IF NOT EXISTS `product_collections` (
  `product_id`    BIGINT UNSIGNED  NOT NULL,
  `collection_id` BIGINT UNSIGNED  NOT NULL,
  `created_at`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`, `collection_id`),
  INDEX `idx_product_collections_collection` (`collection_id`),
  CONSTRAINT `fk_product_collections_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_product_collections_collection`
    FOREIGN KEY (`collection_id`) REFERENCES `collections`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Quan hệ nhiều-nhiều sản phẩm - bộ sưu tập';


-- =====================================================
-- MODULE: inventory
-- =====================================================

CREATE TABLE IF NOT EXISTS `suppliers` (
  `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `code`           VARCHAR(50)      NOT NULL,
  `name`           VARCHAR(200)     NOT NULL,
  `email`          VARCHAR(100)     NULL DEFAULT NULL,
  `phone`          VARCHAR(20)      NULL DEFAULT NULL,
  `contact_person` VARCHAR(100)     NULL DEFAULT NULL,
  `contact_phone`  VARCHAR(20)      NULL DEFAULT NULL,
  `address`        TEXT             NULL DEFAULT NULL,
  `notes`          TEXT             NULL DEFAULT NULL,
  `active`         BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name`),
  UNIQUE INDEX `code_UNIQUE` (`code`),
  INDEX `idx_suppliers_active` (`active`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Nhà cung cấp sneaker';


CREATE TABLE IF NOT EXISTS `purchases` (
  `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `supplier_id`         BIGINT UNSIGNED  NOT NULL,
  `created_by`          BIGINT UNSIGNED  NULL DEFAULT NULL,
  `received_by`         BIGINT UNSIGNED  NULL DEFAULT NULL,
  `purchase_code`       VARCHAR(50)      NOT NULL,
  `supplier_invoice_no` VARCHAR(100)     NULL DEFAULT NULL,
  `subtotal`            DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `discount_amount`     DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `tax_amount`          DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `shipping_cost`       DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `total_cost`          DECIMAL(15,2)    NOT NULL,
  `payment_status`      ENUM('PENDING', 'PAID') NOT NULL DEFAULT 'PENDING',
  `status`              ENUM('DRAFT', 'CONFIRMED', 'RECEIVED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
  `notes`               TEXT             NULL DEFAULT NULL,
  `confirmed_at`        DATETIME         NULL DEFAULT NULL,
  `received_at`         DATETIME         NULL DEFAULT NULL,
  `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `purchase_code_UNIQUE` (`purchase_code`),
  INDEX `idx_purchases_supplier` (`supplier_id`),
  INDEX `idx_purchases_status` (`status`),
  INDEX `idx_purchases_payment_status` (`payment_status`),
  CONSTRAINT `fk_purchases_supplier`
    FOREIGN KEY (`supplier_id`) REFERENCES `suppliers`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_purchases_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_purchases_received_by`
    FOREIGN KEY (`received_by`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Phiếu nhập hàng';


CREATE TABLE IF NOT EXISTS `purchase_items` (
  `id`                BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `purchase_id`       BIGINT UNSIGNED  NOT NULL,
  `variant_id`        BIGINT UNSIGNED  NOT NULL,
  `quantity_ordered`  INT UNSIGNED     NOT NULL,
  `quantity_received` INT UNSIGNED     NOT NULL DEFAULT 0,
  `unit_cost`         DECIMAL(15,2)    NOT NULL,
  `line_total`        DECIMAL(15,2)    NOT NULL,
  `quality_status`    ENUM('PENDING', 'OK', 'DEFECTIVE', 'PARTIALLY_DEFECTIVE', 'RETURNED') NOT NULL DEFAULT 'PENDING',
  `defective_qty`     INT UNSIGNED     NOT NULL DEFAULT 0,
  `received_date`     DATETIME         NULL DEFAULT NULL,
  `notes`             TEXT             NULL DEFAULT NULL,
  `created_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_purchase_items_purchase` (`purchase_id`),
  INDEX `idx_purchase_items_variant` (`variant_id`),
  INDEX `idx_purchase_items_quality_status` (`quality_status`),
  CONSTRAINT `fk_purchase_items_purchase`
    FOREIGN KEY (`purchase_id`) REFERENCES `purchases`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_purchase_items_variant`
    FOREIGN KEY (`variant_id`) REFERENCES `product_variants`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Chi tiết phiếu nhập hàng';


CREATE TABLE IF NOT EXISTS `inventory_transactions` (
  `id`             BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `variant_id`     BIGINT UNSIGNED  NOT NULL,
  `created_by`     BIGINT UNSIGNED  NULL DEFAULT NULL,
  `type`           ENUM('IN', 'OUT', 'ADJUST') NOT NULL,
  `quantity`       INT              NOT NULL,
  `old_stock`      INT              NOT NULL,
  `new_stock`      INT              NOT NULL,
  `reference_type` ENUM('ORDER', 'PURCHASE_ITEM', 'RETURN', 'STOCK_ADJUSTMENT') NULL DEFAULT NULL,
  `reference_id`   BIGINT UNSIGNED  NULL DEFAULT NULL,
  `note`           TEXT             NULL DEFAULT NULL,
  `created_at`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_inventory_variant_date` (`variant_id`, `created_at`),
  INDEX `idx_inventory_type` (`type`),
  INDEX `idx_inventory_reference` (`reference_type`, `reference_id`),
  INDEX `idx_inventory_created_by` (`created_by`),
  CONSTRAINT `fk_inventory_variant`
    FOREIGN KEY (`variant_id`) REFERENCES `product_variants`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_inventory_user`
    FOREIGN KEY (`created_by`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Lịch sử xuất nhập kho - chỉ thêm mới, không sửa xóa';


CREATE TABLE IF NOT EXISTS `stock_adjustments` (
  `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `created_by`   BIGINT UNSIGNED  NULL DEFAULT NULL,
  `approved_by`  BIGINT UNSIGNED  NULL DEFAULT NULL,
  `code`         VARCHAR(50)      NOT NULL,
  `type`         ENUM('EXPORT', 'STOCKTAKE') NOT NULL,
  `status`       ENUM('DRAFT', 'CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
  `reason`       VARCHAR(255)     NOT NULL,
  `notes`        TEXT             NULL DEFAULT NULL,
  `approved_at`  DATETIME         NULL DEFAULT NULL,
  `confirmed_at` DATETIME         NULL DEFAULT NULL,
  `created_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code`),
  INDEX `idx_adjustments_type_status` (`type`, `status`),
  CONSTRAINT `fk_adjustments_user`
    FOREIGN KEY (`created_by`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_adjustments_approved_by`
    FOREIGN KEY (`approved_by`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Phiếu điều chỉnh kho';


CREATE TABLE IF NOT EXISTS `stock_adjustment_items` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `adjustment_id`   BIGINT UNSIGNED  NOT NULL,
  `variant_id`      BIGINT UNSIGNED  NOT NULL,
  `quantity_change` INT              NOT NULL,
  `quantity_before` INT              NOT NULL,
  `quantity_after`  INT              NOT NULL,
  `note`            VARCHAR(255)     NULL DEFAULT NULL,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_adj_items_adjustment` (`adjustment_id`),
  INDEX `idx_adj_items_variant` (`variant_id`),
  CONSTRAINT `fk_adj_items_adjustment`
    FOREIGN KEY (`adjustment_id`) REFERENCES `stock_adjustments`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_adj_items_variant`
    FOREIGN KEY (`variant_id`) REFERENCES `product_variants`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Chi tiết phiếu điều chỉnh kho';


-- =====================================================
-- MODULE: order and cart
-- =====================================================

CREATE TABLE IF NOT EXISTS `carts` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED  NULL DEFAULT NULL,
  `guest_token` VARCHAR(64)      NULL DEFAULT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_cart_user_UNIQUE` (`user_id`),
  UNIQUE INDEX `user_cart_guest_UNIQUE` (`guest_token`),
  CONSTRAINT `fk_carts_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Giỏ hàng';


CREATE TABLE IF NOT EXISTS `cart_items` (
  `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `cart_id`    BIGINT UNSIGNED  NOT NULL,
  `variant_id` BIGINT UNSIGNED  NOT NULL,
  `quantity`   INT              NOT NULL DEFAULT 1,
  `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `cart_variant_UNIQUE` (`cart_id`, `variant_id`),
  INDEX `idx_cart_items_variant` (`variant_id`),
  CONSTRAINT `fk_cart_items_cart`
    FOREIGN KEY (`cart_id`) REFERENCES `carts`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_items_variant`
    FOREIGN KEY (`variant_id`) REFERENCES `product_variants`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Sản phẩm trong giỏ hàng';


CREATE TABLE IF NOT EXISTS `orders` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`          BIGINT UNSIGNED  NULL DEFAULT NULL,
  `guest_token`      VARCHAR(64)      NULL DEFAULT NULL,
  `code`             VARCHAR(50)      NOT NULL,
  `status`           ENUM('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED','REFUNDED') NOT NULL DEFAULT 'PENDING',
  `payment_status`   ENUM('UNPAID','PAID','FAILED','REFUNDED','CANCELLED','EXPIRED') NOT NULL DEFAULT 'UNPAID',
  `payment_method`   ENUM('VNPAY','COD') NOT NULL,
  `recipient_name`   VARCHAR(200)     NOT NULL,
  `recipient_phone`  VARCHAR(15)      NOT NULL,
  `shipping_street`  VARCHAR(255)     NOT NULL,
  `shipping_ward`    VARCHAR(100)     NULL DEFAULT NULL,
  `shipping_district` VARCHAR(100)    NOT NULL,
  `shipping_province` VARCHAR(100)    NOT NULL,
  `subtotal`         DECIMAL(15,2)    NOT NULL,
  `shipping_fee`     DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `discount_amount`  DECIMAL(15,2)    NOT NULL DEFAULT 0,
  `total_amount`     DECIMAL(15,2)    NOT NULL,
  `coupon_code`      VARCHAR(50)      NULL DEFAULT NULL,
  `note`             TEXT             NULL DEFAULT NULL,
  `admin_note`       TEXT             NULL DEFAULT NULL,
  `assigned_to`      BIGINT UNSIGNED  NULL DEFAULT NULL,
  `expired_at`       DATETIME         NOT NULL,
  `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code`),
  INDEX `idx_orders_user_status` (`user_id`, `status`),
  INDEX `idx_orders_guest` (`guest_token`),
  INDEX `idx_orders_payment_status` (`payment_status`),
  INDEX `idx_orders_created` (`created_at`),
  INDEX `idx_orders_assigned` (`assigned_to`),
  CONSTRAINT `fk_orders_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_assigned`
    FOREIGN KEY (`assigned_to`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Đơn hàng';


CREATE TABLE IF NOT EXISTS `order_items` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `order_id`        BIGINT UNSIGNED  NOT NULL,
  `variant_id`      BIGINT UNSIGNED  NOT NULL,
  `product_code`    VARCHAR(50)      NOT NULL,
  `product_name`    VARCHAR(255)     NOT NULL,
  `variant_sku`     VARCHAR(100)     NOT NULL,
  `variant_size`    TINYINT UNSIGNED NOT NULL,
  `variant_color`   VARCHAR(100)     NOT NULL,
  `quantity`        INT              NOT NULL,
  `original_price`  DECIMAL(15,2)    NULL DEFAULT NULL,
  `unit_price`      DECIMAL(15,2)    NOT NULL,
  `subtotal`        DECIMAL(15,2)    NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_order_items_order` (`order_id`),
  INDEX `idx_order_items_variant` (`variant_id`),
  CONSTRAINT `fk_order_items_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_order_items_variant`
    FOREIGN KEY (`variant_id`) REFERENCES `product_variants`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Chi tiết đơn hàng - snapshot tại thời điểm đặt';


CREATE TABLE IF NOT EXISTS `order_status_history` (
  `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT UNSIGNED  NOT NULL,
  `changed_by` BIGINT UNSIGNED  NULL DEFAULT NULL,
  `old_status` ENUM('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED','REFUNDED') NULL DEFAULT NULL,
  `new_status` ENUM('PENDING','CONFIRMED','PROCESSING','SHIPPING','DELIVERED','CANCELLED','REFUNDED') NOT NULL,
  `note`       TEXT             NULL DEFAULT NULL,
  `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_status_history_order_created` (`order_id`, `created_at`),
  INDEX `idx_status_history_changed_by` (`changed_by`),
  CONSTRAINT `fk_status_history_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_status_history_user`
    FOREIGN KEY (`changed_by`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Lịch sử trạng thái đơn hàng - chỉ thêm mới, không sửa xóa';


CREATE TABLE IF NOT EXISTS `payments` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `order_id`         BIGINT UNSIGNED  NOT NULL,
  `amount`          DECIMAL(15,2)    NOT NULL,
  `status`          ENUM('SUCCESS','FAILED','EXPIRED') NOT NULL,
  `transaction_id`  VARCHAR(255)     NULL DEFAULT NULL,
  `response_code`   VARCHAR(10)      NULL DEFAULT NULL,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_payments_order` (`order_id`),
  INDEX `idx_payments_transaction` (`transaction_id`),
  INDEX `idx_payments_status` (`status`),
  CONSTRAINT `fk_payments_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Giao dịch thanh toán';


CREATE TABLE IF NOT EXISTS `coupons` (
  `id`                  BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `code`                VARCHAR(50)      NOT NULL,
  `description`         TEXT             NULL DEFAULT NULL,
  `discount_type`       ENUM('PERCENTAGE','FIXED_AMOUNT') NOT NULL,
  `discount_value`      DECIMAL(15,2)    NOT NULL,
  `min_order_amount`    DECIMAL(15,2)    NULL DEFAULT NULL,
  `max_discount_amount` DECIMAL(15,2)    NULL DEFAULT NULL,
  `usage_limit`         INT              NULL DEFAULT NULL,
  `used_count`          INT              NOT NULL DEFAULT 0,
  `user_usage_limit`    INT              NULL DEFAULT NULL,
  `start_date`          DATETIME         NOT NULL,
  `end_date`            DATETIME         NOT NULL,
  `active`              BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `code_UNIQUE` (`code`),
  INDEX `idx_coupons_active_dates` (`active`, `start_date`, `end_date`)
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Mã giảm giá';


CREATE TABLE IF NOT EXISTS `coupon_usages` (
  `id`        BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `coupon_id` BIGINT UNSIGNED  NOT NULL,
  `order_id`  BIGINT UNSIGNED  NOT NULL,
  `email`     VARCHAR(100)     NULL DEFAULT NULL,
  `phone`     VARCHAR(15)      NULL DEFAULT NULL,
  `used_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `order_id_UNIQUE` (`order_id`),
  INDEX `idx_coupon_usages_coupon_email` (`coupon_id`, `email`),
  INDEX `idx_coupon_usages_coupon_phone` (`coupon_id`, `phone`),
  CONSTRAINT `fk_coupon_usages_coupon`
    FOREIGN KEY (`coupon_id`) REFERENCES `coupons`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_coupon_usages_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Lịch sử sử dụng mã giảm giá - anti-spam';


-- =====================================================
-- MODULE: shipping
-- =====================================================

CREATE TABLE IF NOT EXISTS `shipments` (
  `id`                   BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `order_id`             BIGINT UNSIGNED  NOT NULL,
  `provider`             ENUM('GHN') NOT NULL DEFAULT 'GHN',
  `service`              VARCHAR(50)      NULL DEFAULT NULL,
  `shipping_order_code`  VARCHAR(50)      NULL DEFAULT NULL,
  `tracking_number`      VARCHAR(100)     NULL DEFAULT NULL,
  `status`               VARCHAR(50)      NULL DEFAULT NULL,
  `expected_delivery_at` DATETIME         NULL DEFAULT NULL,
  `picked_up_at`         DATETIME         NULL DEFAULT NULL,
  `delivered_at`         DATETIME         NULL DEFAULT NULL,
  `synced_at`            DATETIME         NULL DEFAULT NULL,
  `created_at`           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `order_id_UNIQUE` (`order_id`),
  INDEX `idx_shipments_tracking` (`tracking_number`),
  INDEX `idx_shipments_order_code` (`shipping_order_code`),
  CONSTRAINT `fk_shipments_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Thông tin vận chuyển - tích hợp GHN';


CREATE TABLE IF NOT EXISTS `shipment_tracking` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `shipment_id` BIGINT UNSIGNED  NOT NULL,
  `status`      VARCHAR(50)      NOT NULL,
  `location`    VARCHAR(255)     NULL DEFAULT NULL,
  `message`     TEXT             NULL DEFAULT NULL,
  `occurred_at` DATETIME         NOT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_tracking_shipment` (`shipment_id`, `occurred_at`),
  CONSTRAINT `fk_tracking_shipment`
    FOREIGN KEY (`shipment_id`) REFERENCES `shipments`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Lịch sử tracking vận chuyển - webhook từ GHN';


-- =====================================================
-- MODULE: engagement
-- =====================================================

CREATE TABLE IF NOT EXISTS `product_reviews` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `product_id`  BIGINT UNSIGNED  NOT NULL,
  `user_id`     BIGINT UNSIGNED  NOT NULL,
  `order_id`    BIGINT UNSIGNED  NOT NULL,
  `rating`      TINYINT UNSIGNED NOT NULL,
  `title`       VARCHAR(255)     NULL DEFAULT NULL,
  `comment`     TEXT             NULL DEFAULT NULL,
  `approved`    BOOLEAN          NOT NULL DEFAULT FALSE,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_product_UNIQUE` (`user_id`, `product_id`),
  INDEX `idx_reviews_product_approved` (`product_id`, `approved`),
  INDEX `idx_reviews_product_rating` (`product_id`, `rating`),
  INDEX `idx_reviews_user` (`user_id`),
  INDEX `idx_reviews_order` (`order_id`),
  CONSTRAINT `fk_reviews_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reviews_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_reviews_order`
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Đánh giá sản phẩm - chỉ user đã mua, 1 review/sản phẩm/user';


CREATE TABLE IF NOT EXISTS `review_images` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `review_id`        BIGINT UNSIGNED  NOT NULL,
  `image_url`        VARCHAR(500)     NOT NULL,
  `image_public_id`  VARCHAR(255)     NOT NULL,
  `display_order`    INT              NOT NULL DEFAULT 0,
  `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `image_public_id_UNIQUE` (`image_public_id`),
  INDEX `idx_review_images_review` (`review_id`),
  CONSTRAINT `fk_review_images_review`
    FOREIGN KEY (`review_id`) REFERENCES `product_reviews`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Ảnh đính kèm review';


CREATE TABLE IF NOT EXISTS `product_comments` (
  `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT UNSIGNED  NOT NULL,
  `user_id`    BIGINT UNSIGNED  NOT NULL,
  `parent_id`  BIGINT UNSIGNED  NULL DEFAULT NULL,
  `content`    TEXT             NOT NULL,
  `approved`   BOOLEAN          NOT NULL DEFAULT FALSE,
  `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_comments_product_approved` (`product_id`, `approved`),
  INDEX `idx_comments_parent` (`parent_id`),
  INDEX `idx_comments_user` (`user_id`),
  CONSTRAINT `fk_comments_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_comments_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `product_comments`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Bình luận sản phẩm - hỗ trợ reply lồng nhau';


CREATE TABLE IF NOT EXISTS `wishlists` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED  NOT NULL,
  `product_id`  BIGINT UNSIGNED  NOT NULL,
  `variant_id`  BIGINT UNSIGNED  NULL DEFAULT NULL,
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_product_UNIQUE` (`user_id`, `product_id`, `variant_id`),
  INDEX `idx_wishlists_user` (`user_id`),
  INDEX `idx_wishlists_variant` (`variant_id`),
  CONSTRAINT `fk_wishlists_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wishlists_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_wishlists_variant`
    FOREIGN KEY (`variant_id`) REFERENCES `product_variants`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Danh sách yêu thích';


-- =====================================================
-- MODULE: notification
-- =====================================================

CREATE TABLE IF NOT EXISTS `notifications` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `target_type`      ENUM('USER','ADMIN','ROLE','ALL') NOT NULL,
  `target_user_id`   BIGINT UNSIGNED  NULL DEFAULT NULL,
  `target_role`      ENUM('ROLE_USER','ROLE_SALE','ROLE_WAREHOUSE','ROLE_MARKETING','ROLE_TECHNICIAN','ROLE_ADMIN') NULL DEFAULT NULL,
  `type`             ENUM('ORDER','PAYMENT','PROMOTION','SYSTEM','REVIEW','INVENTORY') NOT NULL,
  `title`            VARCHAR(255)     NOT NULL,
  `message`          TEXT             NOT NULL,
  `image_url`        VARCHAR(500)     NULL DEFAULT NULL,
  `image_public_id`  VARCHAR(255)     NULL DEFAULT NULL,
  `link`             VARCHAR(500)     NULL DEFAULT NULL,
  `active`           BOOLEAN          NOT NULL DEFAULT TRUE,
  `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_notifications_target_user` (`target_type`, `target_user_id`),
  INDEX `idx_notifications_target_role` (`target_type`, `target_role`),
  INDEX `idx_notifications_type` (`type`),
  INDEX `idx_notifications_created` (`created_at`),
  CONSTRAINT `fk_notifications_target_user`
    FOREIGN KEY (`target_user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Thông báo hệ thống - cá nhân và broadcast';


CREATE TABLE IF NOT EXISTS `notification_recipients` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `notification_id` BIGINT UNSIGNED  NOT NULL,
  `user_id`         BIGINT UNSIGNED  NOT NULL,
  `read_at`         DATETIME         NULL DEFAULT NULL,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `user_notification_UNIQUE` (`user_id`, `notification_id`),
  INDEX `idx_recipients_notification` (`notification_id`),
  INDEX `idx_recipients_user_read_at` (`user_id`, `read_at`),
  CONSTRAINT `fk_recipients_notification`
    FOREIGN KEY (`notification_id`) REFERENCES `notifications`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_recipients_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Trạng thái đọc thông báo của người nhận';


-- =====================================================
-- MODULE: chat
-- =====================================================

CREATE TABLE IF NOT EXISTS `conversations` (
  `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED  NULL DEFAULT NULL,
  `guest_token` VARCHAR(64)      NULL DEFAULT NULL,
  `sale_id`     BIGINT UNSIGNED  NULL DEFAULT NULL,
  `status`      ENUM('OPEN','CLOSED') NOT NULL DEFAULT 'OPEN',
  `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_conversations_user` (`user_id`),
  INDEX `idx_conversations_guest` (`guest_token`),
  INDEX `idx_conversations_sale` (`sale_id`),
  INDEX `idx_conversations_status` (`status`),
  CONSTRAINT `fk_conversations_user`
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_conversations_sale`
    FOREIGN KEY (`sale_id`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Cuộc hội thoại giữa khách và sale';


CREATE TABLE IF NOT EXISTS `messages` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `conversation_id` BIGINT UNSIGNED  NOT NULL,
  `sender_id`       BIGINT UNSIGNED  NULL DEFAULT NULL,
  `guest_token`     VARCHAR(64)      NULL DEFAULT NULL,
  `sender_type`     ENUM('USER','GUEST','SALE') NOT NULL,
  `type`            ENUM('TEXT','IMAGE') NOT NULL DEFAULT 'TEXT',
  `content`         TEXT             NULL DEFAULT NULL,
  `seen_at`         DATETIME         NULL DEFAULT NULL,
  `created_at`      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_messages_conversation` (`conversation_id`, `created_at`),
  INDEX `idx_messages_sender` (`sender_id`),
  CONSTRAINT `fk_messages_conversation`
    FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_messages_sender`
    FOREIGN KEY (`sender_id`) REFERENCES `users`(`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Tin nhắn trong cuộc hội thoại';


CREATE TABLE IF NOT EXISTS `message_attachments` (
  `id`               BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
  `message_id`       BIGINT UNSIGNED  NOT NULL,
  `image_url`        VARCHAR(500)     NOT NULL,
  `image_public_id`  VARCHAR(255)     NOT NULL,
  `created_at`       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `image_public_id_UNIQUE` (`image_public_id`),
  INDEX `idx_attachments_message` (`message_id`),
  CONSTRAINT `fk_attachments_message`
    FOREIGN KEY (`message_id`) REFERENCES `messages`(`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_520_ci
  COMMENT = 'Ảnh đính kèm trong tin nhắn';