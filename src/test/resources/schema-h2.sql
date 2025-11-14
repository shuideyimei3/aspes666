-- H2数据库初始化脚本
-- 处理关键字表名问题

-- 用户表（H2数据库中user是关键字，需要添加引号）
CREATE TABLE IF NOT EXISTS "user" (
    `id` bigint NOT NULL COMMENT '用户ID（雪花算法）',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '加密密码',
    `role` VARCHAR(20) NOT NULL COMMENT '角色：农户/采购方/管理员',
    `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    `is_certified` TINYINT DEFAULT 0 COMMENT '是否认证（0=未认证/1=已认证）',
    `is_delete` TINYINT DEFAULT 0 COMMENT '软删除（0=正常）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

-- 其他表（使用H2语法）
CREATE TABLE IF NOT EXISTS `origin_area` (
    `area_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '产地ID',
    `area_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '行政区划代码',
    `area_name` VARCHAR(100) NOT NULL COMMENT '产地名称（县/乡镇）',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '地级市',
    `feature` TEXT COMMENT '产地特色',
    `is_poverty_area` BOOLEAN DEFAULT 0 COMMENT '是否贫困地区（1=是）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `farmer_info` (
    `id` bigint NOT NULL COMMENT '农户信息ID（雪花算法）',
    `user_id` bigint NOT NULL UNIQUE COMMENT '关联用户表',
    `farm_name` VARCHAR(100) NOT NULL COMMENT '农场/合作社名称',
    `origin_area_id` INT NOT NULL COMMENT '主要产地',
    `production_scale` VARCHAR(200) COMMENT '生产规模',
    `certifications` VARCHAR(500) DEFAULT NULL COMMENT '认证资质',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '收款银行账号',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `audit_status` VARCHAR(20) DEFAULT 'pending' COMMENT '资质审核状态',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `purchaser_info` (
    `id` bigint NOT NULL COMMENT '采购方信息ID（雪花算法）',
    `user_id` bigint NOT NULL UNIQUE COMMENT '关联用户表',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `company_type` VARCHAR(50) COMMENT '企业类型',
    `business_license` VARCHAR(500) NOT NULL COMMENT '营业执照URL',
    `purchase_scale` VARCHAR(200) COMMENT '采购规模',
    `preferred_origin` VARCHAR(200) COMMENT '偏好产地',
    `audit_status` VARCHAR(20) DEFAULT 'pending' COMMENT '资质审核状态',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_category` (
    `id` bigint NOT NULL COMMENT '分类ID（雪花算法）',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` bigint DEFAULT NULL COMMENT '父分类ID（顶级分类为null）',
    `attribute` VARCHAR(500) DEFAULT NULL COMMENT '分类属性',
    `status` VARCHAR(20) DEFAULT 'active',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `farmer_product` (
    `id` bigint NOT NULL COMMENT '产品ID（雪花算法）',
    `farmer_id` bigint NOT NULL COMMENT '所属农户',
    `category_id` bigint NOT NULL COMMENT '产品分类',
    `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `spec` VARCHAR(100) NOT NULL COMMENT '规格',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价（元/单位）',
    `min_purchase` INT NOT NULL DEFAULT 1 COMMENT '起订量',
    `stock` INT NOT NULL COMMENT '可售数量',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期/采收日期',
    `shelf_life` VARCHAR(50) COMMENT '保质期',
    `production_method` VARCHAR(50) COMMENT '生产方式',
    `origin_area_id` INT NOT NULL COMMENT '产地',
    `description` TEXT COMMENT '产品描述',
    `status` VARCHAR(20) DEFAULT 'on_sale' COMMENT '状态：在售/下架',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `product_image` (
    `id` bigint NOT NULL COMMENT '图片ID（雪花算法）',
    `product_id` bigint NOT NULL COMMENT '关联产品',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `image_type` VARCHAR(20) NOT NULL COMMENT '类型：封面/生产场景/细节',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `purchase_demand` (
    `id` bigint NOT NULL COMMENT '需求ID（雪花算法）',
    `purchaser_id` bigint NOT NULL COMMENT '发布方',
    `category_id` bigint NOT NULL COMMENT '需求产品分类',
    `product_name` VARCHAR(100) NOT NULL COMMENT '需求产品名称',
    `spec_require` VARCHAR(200) COMMENT '规格要求',
    `quantity` INT NOT NULL COMMENT '需求数量',
    `unit` VARCHAR(20) NOT NULL COMMENT '数量单位',
    `price_range` VARCHAR(50) COMMENT '价格范围',
    `delivery_date` DATE NOT NULL COMMENT '期望交货日期',
    `delivery_address` VARCHAR(300) NOT NULL COMMENT '交货地点',
    `quality_require` TEXT COMMENT '质量要求',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：待匹配/已匹配/已关闭',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `docking_record` (
    `id` bigint NOT NULL COMMENT '对接ID（雪花算法）',
    `demand_id` bigint NOT NULL COMMENT '关联采购需求',
    `farmer_id` bigint NOT NULL COMMENT '响应的农户',
    `product_id` bigint DEFAULT NULL COMMENT '农户提供的产品（可选）',
    `quote_price` DECIMAL(10,2) NOT NULL COMMENT '报价（元/单位）',
    `can_supply` INT NOT NULL COMMENT '可供应数量',
    `supply_time` DATE COMMENT '可交货时间',
    `contact_way` VARCHAR(50) COMMENT '补充联系方式',
    `remark` TEXT COMMENT '对接备注',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '对接状态',
    `purchaser_remark` TEXT COMMENT '采购方反馈',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demand_farmer` (`demand_id`, `farmer_id`)
);

CREATE TABLE IF NOT EXISTS `purchase_contract` (
    `id` bigint NOT NULL COMMENT '合同ID（雪花算法）',
    `contract_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
    `docking_id` bigint NOT NULL COMMENT '关联对接记录',
    `purchaser_id` bigint NOT NULL COMMENT '采购方',
    `farmer_id` bigint NOT NULL COMMENT '农户',
    `product_info` TEXT NOT NULL COMMENT '产品信息',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '合同总金额',
    `payment_terms` VARCHAR(500) NOT NULL COMMENT '付款方式',
    `delivery_time` DATE NOT NULL COMMENT '交货时间',
    `delivery_address` VARCHAR(300) NOT NULL COMMENT '交货地址',
    `quality_standards` TEXT COMMENT '质量标准',
    `breach_terms` TEXT COMMENT '违约责任',
    `status` VARCHAR(20) DEFAULT 'signed' COMMENT '合同状态',
    `sign_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '签约时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` bigint NOT NULL COMMENT '订单ID（雪花算法）',
    `contract_id` bigint NOT NULL COMMENT '关联采购合同',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `purchaser_id` bigint NOT NULL COMMENT '采购方',
    `farmer_id` bigint NOT NULL COMMENT '农户',
    `product_info` TEXT NOT NULL COMMENT '产品信息',
    `quantity` INT NOT NULL COMMENT '订购数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    `delivery_time` DATE NOT NULL COMMENT '交货时间',
    `delivery_address` VARCHAR(300) NOT NULL COMMENT '交货地址',
    `actual_quantity` INT DEFAULT NULL COMMENT '实际交付数量',
    `inspection_result` TEXT COMMENT '验收结果',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);