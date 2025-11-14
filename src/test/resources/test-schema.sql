-- H2数据库测试用表结构
-- 处理关键字表名问题

-- 用户表（H2数据库中user是关键字，需要添加引号）
CREATE TABLE IF NOT EXISTS "user" (
    id bigint NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    contact_person VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    contact_email VARCHAR(100) DEFAULT NULL,
    is_certified TINYINT DEFAULT 0,
    is_delete TINYINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- 其他表
CREATE TABLE IF NOT EXISTS `origin_area` (
    `area_id` INT PRIMARY KEY AUTO_INCREMENT,
    `area_code` VARCHAR(20) NOT NULL UNIQUE,
    `area_name` VARCHAR(100) NOT NULL,
    `province` VARCHAR(50) NOT NULL,
    `city` VARCHAR(50) NOT NULL,
    `feature` TEXT,
    `is_poverty_area` BOOLEAN DEFAULT 0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `farmer_info` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL UNIQUE,
    `farm_name` VARCHAR(100) NOT NULL,
    `origin_area_id` INT NOT NULL,
    `production_scale` VARCHAR(200),
    `certifications` VARCHAR(500),
    `bank_account` VARCHAR(50),
    `bank_name` VARCHAR(100),
    `audit_status` VARCHAR(20) DEFAULT 'pending',
    `audit_remark` VARCHAR(500),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `purchaser_info` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL UNIQUE,
    `company_name` VARCHAR(100) NOT NULL,
    `company_type` VARCHAR(50),
    `business_license` VARCHAR(500) NOT NULL,
    `purchase_scale` VARCHAR(200),
    `preferred_origin` VARCHAR(200),
    `audit_status` VARCHAR(20) DEFAULT 'pending',
    `audit_remark` VARCHAR(500),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_category` (
    `id` bigint NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `parent_id` bigint DEFAULT NULL,
    `attribute` VARCHAR(500),
    `status` VARCHAR(20) DEFAULT 'active',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `farmer_product` (
    `id` bigint NOT NULL,
    `farmer_id` bigint NOT NULL,
    `category_id` bigint NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `spec` VARCHAR(100) NOT NULL,
    `unit` VARCHAR(20) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `min_purchase` INT NOT NULL DEFAULT 1,
    `stock` INT NOT NULL,
    `production_date` DATE DEFAULT NULL,
    `shelf_life` VARCHAR(50),
    `production_method` VARCHAR(50),
    `origin_area_id` INT NOT NULL,
    `description` TEXT,
    `status` VARCHAR(20) DEFAULT 'on_sale',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_image` (
    `id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    `image_url` VARCHAR(500) NOT NULL,
    `image_type` VARCHAR(20) NOT NULL,
    `sort` INT DEFAULT 0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `purchase_demand` (
    `id` bigint NOT NULL,
    `purchaser_id` bigint NOT NULL,
    `category_id` bigint NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `spec_require` VARCHAR(200),
    `quantity` INT NOT NULL,
    `unit` VARCHAR(20) NOT NULL,
    `price_range` VARCHAR(50),
    `delivery_date` DATE NOT NULL,
    `delivery_address` VARCHAR(300) NOT NULL,
    `quality_require` TEXT,
    `status` VARCHAR(20) DEFAULT 'pending',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `docking_record` (
    `id` bigint NOT NULL,
    `demand_id` bigint NOT NULL,
    `farmer_id` bigint NOT NULL,
    `product_id` bigint DEFAULT NULL,
    `quote_price` DECIMAL(10,2) NOT NULL,
    `can_supply` INT NOT NULL,
    `supply_time` DATE,
    `contact_way` VARCHAR(50),
    `remark` TEXT,
    `status` VARCHAR(20) DEFAULT 'pending',
    `purchaser_remark` TEXT,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE (`demand_id`, `farmer_id`)
);

CREATE TABLE IF NOT EXISTS `purchase_contract` (
    `id` bigint NOT NULL,
    `contract_no` VARCHAR(50) NOT NULL UNIQUE,
    `docking_id` bigint NOT NULL,
    `purchaser_id` bigint NOT NULL,
    `farmer_id` bigint NOT NULL,
    `product_info` TEXT NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL,
    `payment_terms` VARCHAR(500) NOT NULL,
    `delivery_time` DATE NOT NULL,
    `delivery_address` VARCHAR(300) NOT NULL,
    `quality_standards` TEXT,
    `breach_terms` TEXT,
    `farmer_sign_url` VARCHAR(500),
    `purchaser_sign_url` VARCHAR(500),
    `status` VARCHAR(20) DEFAULT 'signed',
    `sign_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` bigint NOT NULL,
    `contract_id` bigint NOT NULL,
    `order_no` VARCHAR(50) NOT NULL UNIQUE,
    `product_info` TEXT NOT NULL,
    `actual_quantity` INT DEFAULT NULL,
    `actual_amount` DECIMAL(12,2) DEFAULT NULL,
    `delivery_time` TIMESTAMP DEFAULT NULL,
    `inspection_result` TEXT,
    `status` VARCHAR(20) DEFAULT 'pending',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `stock_reservation` (
    `id` bigint NOT NULL,
    `product_id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `reserved_quantity` int NOT NULL,
    `status` varchar(20) NOT NULL DEFAULT 'reserved',
    `release_reason` varchar(500),
    `expired_time` TIMESTAMP,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `payment_record` (
    `id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `payment_stage` varchar(50) NOT NULL,
    `amount` DECIMAL(12,2) NOT NULL,
    `payment_method` varchar(50) NOT NULL,
    `status` varchar(20) NOT NULL DEFAULT 'pending',
    `payment_no` varchar(100),
    `payment_time` TIMESTAMP,
    `voucher_url` varchar(500),
    `remark` varchar(500),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

-- 插入基础数据
INSERT INTO `product_category` (`id`, `name`, `status`) VALUES (1, '粮食作物', 'active');

-- 用户认证申请表
CREATE TABLE IF NOT EXISTS `user_certification_apply` (
    `id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `apply_type` VARCHAR(20) NOT NULL,
    `id_number` VARCHAR(50) DEFAULT NULL,
    `id_card_front_url` VARCHAR(500) DEFAULT NULL,
    `id_card_back_url` VARCHAR(500) DEFAULT NULL,
    `business_license_url` VARCHAR(500) DEFAULT NULL,
    `legal_representative` VARCHAR(50) DEFAULT NULL,
    `apply_reason` TEXT DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT 'pending',
    `admin_remark` VARCHAR(500) DEFAULT NULL,
    `approved_time` TIMESTAMP DEFAULT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

-- 物流记录表
CREATE TABLE IF NOT EXISTS `logistics_record` (
    `id` bigint NOT NULL,
    `order_id` bigint NOT NULL,
    `logistics_company` VARCHAR(100) NOT NULL,
    `tracking_no` VARCHAR(100) NOT NULL,
    `transport_type` VARCHAR(50) NOT NULL,
    `departure_time` TIMESTAMP DEFAULT NULL,
    `arrival_time` TIMESTAMP DEFAULT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

-- 物流轨迹表
CREATE TABLE IF NOT EXISTS `logistics_trace` (
    `id` bigint NOT NULL,
    `logistics_id` bigint NOT NULL,
    `node_time` TIMESTAMP NOT NULL,
    `node_location` VARCHAR(200) NOT NULL,
    `node_desc` TEXT,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);