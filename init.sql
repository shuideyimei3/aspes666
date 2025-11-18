SET FOREIGN_KEY_CHECKS = 0;

USE aspes;

-- 1. 贫困县/产地信息表（强化产地溯源）
CREATE TABLE IF NOT EXISTS `origin_area` (
    `area_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '产地ID',
    `area_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '行政区划代码',
    `area_name` VARCHAR(100) NOT NULL COMMENT '产地名称（县/乡镇）',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '地级市',
    `feature` TEXT COMMENT '产地特色（如“富硒土壤”“有机种植基地”）',
    `is_poverty_area` BOOLEAN DEFAULT 0 COMMENT '是否贫困地区（1=是）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_province_city` (`province`, `city`) COMMENT '按地区筛选产地'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农副产品产地信息表（含贫困地区标识）';


-- 2. 用户表（核心角色：农户/采购方/管理员）
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL COMMENT '用户ID（雪花算法）',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
    `password` VARCHAR(100) NOT NULL COMMENT '加密密码',
    `role` ENUM('farmer','purchaser','admin') NOT NULL COMMENT '角色：农户/采购方/管理员',
    `contact_person` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    `is_certified` TINYINT DEFAULT 0 COMMENT '是否认证（0=未认证/1=已认证）',
    `is_delete` TINYINT DEFAULT 0 COMMENT '软删除（0=正常）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_role_certified` (`role`, `is_certified`) COMMENT '按角色+认证状态筛选用户'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表（农户/采购方/管理员）';


-- 3. 农户信息表（关联用户表，记录生产信息）
CREATE TABLE IF NOT EXISTS `farmer_info` (
    `id` bigint NOT NULL COMMENT '农户信息ID（雪花算法）',
    `user_id` bigint NOT NULL UNIQUE COMMENT '关联用户表（role=farmer）',
    `farm_name` VARCHAR(100) NOT NULL COMMENT '农场/合作社名称',
    `origin_area_id` INT NOT NULL COMMENT '主要产地（关联origin_area）',
    `production_scale` VARCHAR(200) COMMENT '生产规模（如“500亩耕地”“年出栏1000头”）',
    `certifications` JSON DEFAULT NULL COMMENT '认证资质（如{"有机认证":"url","绿色食品":"url"}）',
    `bank_account` VARCHAR(50) DEFAULT NULL COMMENT '收款银行账号',
    `bank_name` VARCHAR(100) DEFAULT NULL COMMENT '开户银行',
    `audit_status` ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '资质审核状态',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_farmer_origin` (`origin_area_id`),
    KEY `idx_farmer_audit` (`audit_status`),
    CONSTRAINT `fk_farmer_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_farmer_origin` FOREIGN KEY (`origin_area_id`) REFERENCES `origin_area` (`area_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农户/合作社信息表';


-- 4. 采购方信息表（关联用户表，记录企业信息）
CREATE TABLE IF NOT EXISTS `purchaser_info` (
    `id` bigint NOT NULL COMMENT '采购方信息ID（雪花算法）',
    `user_id` bigint NOT NULL UNIQUE COMMENT '关联用户表（role=purchaser）',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `company_type` VARCHAR(50) COMMENT '企业类型（如“食品加工厂”“超市连锁”“电商平台”）',
    `business_license` VARCHAR(500) NOT NULL COMMENT '营业执照URL',
    `purchase_scale` VARCHAR(200) COMMENT '采购规模（如“月采购量10吨以上”）',
    `preferred_origin` JSON DEFAULT NULL COMMENT '偏好产地（如["山东","河南"]）',
    `audit_status` ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '资质审核状态',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_purchaser_audit` (`audit_status`),
    CONSTRAINT `fk_purchaser_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购方企业信息表';


-- 5. 农副产品分类表（按品类/属性划分）
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` bigint NOT NULL COMMENT '分类ID（雪花算法）',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称（如“蔬菜”“禽蛋”“粮油”）',
    `parent_id` bigint DEFAULT NULL COMMENT '父分类ID（顶级分类为null）',
    `attribute` JSON DEFAULT NULL COMMENT '分类属性（如蔬菜：{"freshness":"新鲜度","storage":"储存方式"}）',
    `status` ENUM('active','inactive') DEFAULT 'active',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`) COMMENT '查询子分类'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农副产品分类表';


-- 6. 农户产品表（农户发布的可售产品）
CREATE TABLE IF NOT EXISTS `farmer_product` (
    `id` bigint NOT NULL COMMENT '产品ID（雪花算法）',
    `farmer_id` bigint NOT NULL COMMENT '所属农户（关联farmer_info）',
    `category_id` bigint NOT NULL COMMENT '产品分类',
    `name` VARCHAR(100) NOT NULL COMMENT '产品名称（如“山东寿光有机黄瓜”）',
    `spec` VARCHAR(100) NOT NULL COMMENT '规格（如“500g/份”“直径5-8cm”）',
    `unit` VARCHAR(20) NOT NULL COMMENT '单位（如“kg”“箱”“个”）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价（元/单位）',
    `min_purchase` INT NOT NULL DEFAULT 1 COMMENT '起订量（最低采购数量）',
    `stock` INT NOT NULL COMMENT '可售数量',
    `production_date` DATE DEFAULT NULL COMMENT '生产日期/采收日期',
    `shelf_life` VARCHAR(50) COMMENT '保质期（如“常温7天”）',
    `production_method` VARCHAR(50) COMMENT '生产方式（如“有机种植”“散养”）',
    `origin_area_id` INT NOT NULL COMMENT '产地（关联origin_area）',
    `description` TEXT COMMENT '产品描述（种植过程/品质特点）',
    `status` ENUM('on_sale','off_sale') DEFAULT 'on_sale' COMMENT '状态：在售/下架',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_product_farmer` (`farmer_id`),
    KEY `fk_product_category` (`category_id`),
    KEY `fk_product_origin` (`origin_area_id`),
    KEY `idx_product_status` (`status`),
    KEY `idx_price_minpurchase` (`price`, `min_purchase`) COMMENT '按价格和起订量筛选',
    CONSTRAINT `fk_product_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `farmer_info` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_product_origin` FOREIGN KEY (`origin_area_id`) REFERENCES `origin_area` (`area_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农户发布的农副产品表';


-- 7. 产品图片表（农户产品的展示图片）
CREATE TABLE IF NOT EXISTS `product_image` (
    `id` bigint NOT NULL COMMENT '图片ID（雪花算法）',
    `product_id` bigint NOT NULL COMMENT '关联产品',
    `image_url` VARCHAR(500) NOT NULL COMMENT '图片URL',
    `image_type` ENUM('cover','production','detail') NOT NULL COMMENT '类型：封面/生产场景/细节',
    `sort` INT DEFAULT 0 COMMENT '排序（值越小越靠前）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_image_product` (`product_id`),
    CONSTRAINT `fk_image_product` FOREIGN KEY (`product_id`) REFERENCES `farmer_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品图片表';


-- 8. 采购需求表（采购方发布的需求）
CREATE TABLE IF NOT EXISTS `purchase_demand` (
    `id` bigint NOT NULL COMMENT '需求ID（雪花算法）',
    `purchaser_id` bigint NOT NULL COMMENT '发布方（关联purchaser_info）',
    `category_id` bigint NOT NULL COMMENT '需求产品分类',
    `product_name` VARCHAR(100) NOT NULL COMMENT '需求产品名称（如“新鲜土豆”）',
    `spec_require` VARCHAR(200) COMMENT '规格要求（如“单个重100-200g”）',
    `quantity` INT NOT NULL COMMENT '需求数量',
    `unit` VARCHAR(20) NOT NULL COMMENT '数量单位（如“kg”）',
    `price_range` VARCHAR(50) COMMENT '价格范围（如“2.5-3.0元/kg”）',
    `delivery_date` DATE NOT NULL COMMENT '期望交货日期',
    `delivery_address` VARCHAR(300) NOT NULL COMMENT '交货地点',
    `quality_require` TEXT COMMENT '质量要求（如“无农残”“符合GB 2763标准”）',
    `status` ENUM('pending','matched','closed') DEFAULT 'pending' COMMENT '状态：待匹配/已匹配/已关闭',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_demand_purchaser` (`purchaser_id`),
    KEY `fk_demand_category` (`category_id`),
    KEY `idx_demand_status_date` (`status`, `delivery_date`) COMMENT '按状态和交货日期筛选',
    CONSTRAINT `fk_demand_purchaser` FOREIGN KEY (`purchaser_id`) REFERENCES `purchaser_info` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_demand_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购方需求发布表';


-- 9. 对接记录表（农户响应采购需求的对接过程）
CREATE TABLE IF NOT EXISTS `docking_record` (
    `id` bigint NOT NULL COMMENT '对接ID（雪花算法）',
    `demand_id` bigint NOT NULL COMMENT '关联采购需求',
    `farmer_id` bigint NOT NULL COMMENT '响应的农户',
    `product_id` bigint DEFAULT NULL COMMENT '农户提供的产品（可选）',
    `quote_price` DECIMAL(10,2) NOT NULL COMMENT '报价（元/单位）',
    `can_supply` INT NOT NULL COMMENT '可供应数量',
    `supply_time` DATE COMMENT '可交货时间',
    `contact_way` VARCHAR(50) COMMENT '补充联系方式',
    `remark` TEXT COMMENT '对接备注（如“可提供检测报告”）',
    `status` ENUM('pending','negotiating','agreed','rejected') DEFAULT 'pending' COMMENT '对接状态：待处理/协商中/已达成/已拒绝',
    `purchaser_remark` TEXT COMMENT '采购方反馈',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demand_farmer` (`demand_id`, `farmer_id`) COMMENT '同一需求农户仅能响应一次',
    KEY `fk_docking_demand` (`demand_id`),
    KEY `fk_docking_farmer` (`farmer_id`),
    KEY `idx_docking_status` (`status`),
    CONSTRAINT `fk_docking_demand` FOREIGN KEY (`demand_id`) REFERENCES `purchase_demand` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_docking_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `farmer_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农户与采购方对接记录表';


-- 10. 采购合同表（对接达成后的正式合同）
CREATE TABLE IF NOT EXISTS `purchase_contract` (
    `id` bigint NOT NULL COMMENT '合同ID（雪花算法）',
    `contract_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
    `docking_id` bigint NOT NULL COMMENT '关联对接记录',
    `purchaser_id` bigint NOT NULL COMMENT '采购方',
    `farmer_id` bigint NOT NULL COMMENT '农户',
    `product_info` JSON NOT NULL COMMENT '产品信息（如{"name":"土豆","spec":"100-200g","quantity":5000,"unit":"kg","price":2.8}）',
    `total_amount` DECIMAL(12,2) NOT NULL COMMENT '合同总金额',
    `payment_terms` VARCHAR(500) NOT NULL COMMENT '付款方式（如“预付30%，收货后付70%”）',
    `delivery_time` DATE NOT NULL COMMENT '交货时间',
    `delivery_address` VARCHAR(300) NOT NULL COMMENT '交货地址',
    `quality_standards` TEXT COMMENT '质量标准（如“符合GB 2763-2021”）',
    `breach_terms` TEXT COMMENT '违约责任',
    `farmer_sign_url` VARCHAR(500) DEFAULT NULL COMMENT '农户签字/盖章扫描件',
    `purchaser_sign_url` VARCHAR(500) DEFAULT NULL COMMENT '采购方签字/盖章扫描件',
    `status` ENUM('draft','signed','executing','completed','terminated') DEFAULT 'draft' COMMENT '合同状态',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_contract_docking` (`docking_id`),
    KEY `fk_contract_purchaser` (`purchaser_id`),
    KEY `fk_contract_farmer` (`farmer_id`),
    KEY `idx_contract_status` (`status`),
    KEY `idx_contract_no` (`contract_no`),
    CONSTRAINT `fk_contract_docking` FOREIGN KEY (`docking_id`) REFERENCES `docking_record` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_contract_purchaser` FOREIGN KEY (`purchaser_id`) REFERENCES `purchaser_info` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_contract_farmer` FOREIGN KEY (`farmer_id`) REFERENCES `farmer_info` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购合同表';


-- 11. 采购订单表（基于合同的执行订单）
CREATE TABLE IF NOT EXISTS `purchase_order` (
    `id` bigint NOT NULL COMMENT '订单ID（雪花算法）',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `contract_id` bigint NOT NULL COMMENT '关联合同',
    `product_info` JSON NOT NULL COMMENT '订单产品信息（同合同）',
    `actual_quantity` INT DEFAULT NULL COMMENT '实际交货数量',
    `actual_amount` DECIMAL(12,2) DEFAULT NULL COMMENT '实际结算金额',
    `status` ENUM('pending','delivered','paid','completed','cancelled') DEFAULT 'pending' COMMENT '订单状态',
    `delivery_time` DATETIME DEFAULT NULL COMMENT '实际交货时间',
    `inspection_result` TEXT COMMENT '验收结果',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_order_contract` (`contract_id`),
    KEY `idx_order_status` (`status`),
    KEY `idx_order_no` (`order_no`),
    CONSTRAINT `fk_order_contract` FOREIGN KEY (`contract_id`) REFERENCES `purchase_contract` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表（合同执行凭证）';


-- 12. 支付记录表（支持分阶段支付）
CREATE TABLE IF NOT EXISTS `payment_record` (
    `id` bigint NOT NULL COMMENT '支付ID（雪花算法）',
    `order_id` bigint NOT NULL COMMENT '关联订单',
    `payment_no` VARCHAR(100) DEFAULT NULL UNIQUE COMMENT '支付流水号',
    `payment_stage` VARCHAR(50) NOT NULL COMMENT '支付阶段（如“预付款”“尾款”）',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    `payment_method` ENUM('bank_transfer','alipay','wechat') NOT NULL COMMENT '支付方式',
    `status` ENUM('pending','success','failed') DEFAULT 'pending' COMMENT '支付状态',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `voucher_url` VARCHAR(500) DEFAULT NULL COMMENT '支付凭证URL（如转账截图）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_payment_order` (`order_id`),
    KEY `idx_payment_status` (`status`),
    CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表（支持分阶段）';


-- 13. 物流记录表（批量采购物流）
CREATE TABLE IF NOT EXISTS `logistics_record` (
    `id` bigint NOT NULL COMMENT '物流ID（雪花算法）',
    `order_id` bigint NOT NULL COMMENT '关联订单（允许一个订单多条物流记录，如分批发货）',
    `logistics_company` VARCHAR(50) NOT NULL COMMENT '物流公司',
    `tracking_no` VARCHAR(100) NOT NULL COMMENT '物流单号（同一物流公司内唯一）',
    `transport_type` VARCHAR(50) COMMENT '运输方式（如“冷链车”“普通货车”）',
    `departure_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `arrival_time` DATETIME DEFAULT NULL COMMENT '到货时间',
    `status` ENUM('pending','shipped','transit','arrived','signed') DEFAULT 'pending' COMMENT '物流状态',
    `batch_no` VARCHAR(50) DEFAULT NULL COMMENT '批次号（区分同一订单的多批物流）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`), -- 新增：加速查询订单的所有物流记录
    KEY `idx_tracking_no` (`tracking_no`),
    UNIQUE KEY `uk_tracking_company` (`logistics_company`, `tracking_no`), -- 新增：确保同一物流公司的物流单号唯一
    CONSTRAINT `fk_logistics_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE RESTRICT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流记录表（支持同一订单分批发货）';

-- 14. 物流轨迹表
CREATE TABLE IF NOT EXISTS `logistics_trace` (
    `id` bigint NOT NULL COMMENT '轨迹ID（雪花算法）',
    `logistics_id` bigint NOT NULL COMMENT '关联物流记录',
    `node_time` DATETIME NOT NULL COMMENT '节点时间',
    `node_location` VARCHAR(100) COMMENT '节点地点',
    `node_desc` VARCHAR(500) NOT NULL COMMENT '节点描述（如“【济南仓】已发出”）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_trace_logistics` (`logistics_id`),
    KEY `idx_logistics_time` (`logistics_id`, `node_time`),
    CONSTRAINT `fk_trace_logistics` FOREIGN KEY (`logistics_id`) REFERENCES `logistics_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';


-- 15. 合作评价表（双方互评）
CREATE TABLE IF NOT EXISTS `cooperation_review` (
    `id` bigint NOT NULL COMMENT '评价ID（雪花算法）',
    `order_id` bigint NOT NULL UNIQUE COMMENT '关联订单（仅完成订单可评价）',
    `review_from` ENUM('farmer','purchaser') NOT NULL COMMENT '评价方',
    `review_to` ENUM('farmer','purchaser') NOT NULL COMMENT '被评价方',
    `target_id` bigint NOT NULL COMMENT '被评价方ID（farmer_id/purchaser_id）',
    `rating` TINYINT NOT NULL COMMENT '评分（1-5星）',
    `comment` TEXT COMMENT '评价内容（如“交货及时，质量达标”）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_review_order` (`order_id`),
    KEY `idx_target_rating` (`target_id`, `rating`),
    CONSTRAINT `fk_review_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合作评价表（农户与采购方互评）';


-- 16. 库存预留表（订单创建时预留库存，防止超卖）
CREATE TABLE IF NOT EXISTS `stock_reservation` (
    `id` bigint NOT NULL COMMENT '预留ID（雪花算法）',
    `product_id` bigint NOT NULL COMMENT '关联产品',
    `order_id` bigint NOT NULL COMMENT '关联订单',
    `reserved_quantity` INT NOT NULL COMMENT '预留数量',
    `status` ENUM('reserved','released','expired') DEFAULT 'reserved' COMMENT '预留状态：已预留/已释放/已过期',
    `release_reason` VARCHAR(200) COMMENT '释放原因（取消/过期等）',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `expired_time` TIMESTAMP DEFAULT NULL COMMENT '预留过期时间（若未支付则24小时后过期）',
    PRIMARY KEY (`id`),
    KEY `fk_reservation_product` (`product_id`),
    KEY `fk_reservation_order` (`order_id`),
    KEY `idx_reservation_status` (`status`),
    UNIQUE KEY `uk_order_product` (`order_id`, `product_id`) COMMENT '同一订单同一产品仅能预留一次',
    CONSTRAINT `fk_reservation_product` FOREIGN KEY (`product_id`) REFERENCES `farmer_product` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_reservation_order` FOREIGN KEY (`order_id`) REFERENCES `purchase_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预留表';


-- 17. 用户认证申请表（农户/采购方身份认证）
CREATE TABLE IF NOT EXISTS `user_certification_apply` (
    `id` bigint NOT NULL COMMENT '申请ID（雪花算法）',
    `user_id` bigint NOT NULL COMMENT '申请用户ID',
    `apply_type` ENUM('farmer','purchaser') NOT NULL COMMENT '申请类型：农户/采购方',
    `id_number` VARCHAR(50) COMMENT '身份证号（农户）或营业执照号（采购方）',
    `id_card_front_url` VARCHAR(500) COMMENT '身份证正面照URL',
    `id_card_back_url` VARCHAR(500) COMMENT '身份证反面照URL',
    `business_license_url` VARCHAR(500) COMMENT '营业执照照片URL（采购方）',
    `legal_representative` VARCHAR(100) COMMENT '法定代表人（企业）',
    `apply_reason` TEXT COMMENT '认证申请说明',
    `status` ENUM('pending','approved','rejected') DEFAULT 'pending' COMMENT '申请状态：待审核/已批准/已拒绝',
    `admin_remark` VARCHAR(500) COMMENT '管理员备注',
    `approved_time` DATETIME DEFAULT NULL COMMENT '批准时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_apply_user` (`user_id`),
    KEY `idx_apply_status` (`status`),
    KEY `idx_apply_type` (`apply_type`),
    UNIQUE KEY `uk_user_type` (`user_id`, `apply_type`) COMMENT '同用户同类型仅能申请一次',
    CONSTRAINT `fk_apply_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证申请表';


SET FOREIGN_KEY_CHECKS = 1;

-- 执行成功提示
SELECT '农副产品对接平台SQL脚本执行成功！共包含17张核心表，支持库存预留和用户认证' AS `result`;