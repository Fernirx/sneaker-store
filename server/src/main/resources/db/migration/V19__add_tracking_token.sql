ALTER TABLE orders ADD COLUMN tracking_token VARCHAR(36);
UPDATE orders SET tracking_token = UUID() WHERE tracking_token IS NULL;
ALTER TABLE orders ADD CONSTRAINT uq_orders_tracking_token UNIQUE (tracking_token);
