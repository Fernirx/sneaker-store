-- V18__add_tier_discount.sql
ALTER TABLE store_settings
ADD COLUMN silver_discount_rate INT NOT NULL DEFAULT 0,
ADD COLUMN gold_discount_rate INT NOT NULL DEFAULT 0,
ADD COLUMN platinum_discount_rate INT NOT NULL DEFAULT 0;

ALTER TABLE orders
ADD COLUMN tier_discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00;
