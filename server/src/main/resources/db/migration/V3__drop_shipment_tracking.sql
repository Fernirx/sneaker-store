DROP TABLE IF EXISTS `shipment_tracking`;

ALTER TABLE `shipments` DROP COLUMN `tracking_number`;
ALTER TABLE `shipments` DROP COLUMN `service`;
ALTER TABLE `shipments` DROP COLUMN `picked_up_at`;
