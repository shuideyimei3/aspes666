-- 修改采购合同表，添加product_id字段
ALTER TABLE `purchase_contract` 
ADD COLUMN `product_id` bigint NOT NULL COMMENT '关联产品ID' AFTER `farmer_id`,
ADD KEY `fk_contract_product` (`product_id`),
ADD CONSTRAINT `fk_contract_product` FOREIGN KEY (`product_id`) REFERENCES `farmer_product` (`id`) ON DELETE RESTRICT;

-- 修改采购订单表，添加product_id字段
ALTER TABLE `purchase_order` 
ADD COLUMN `product_id` bigint NOT NULL COMMENT '关联产品ID' AFTER `contract_id`,
ADD KEY `fk_order_product` (`product_id`),
ADD CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `farmer_product` (`id`) ON DELETE RESTRICT;