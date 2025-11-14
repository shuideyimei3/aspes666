-- 测试数据初始化脚本

-- 用户表测试数据
INSERT INTO "user" (id, username, password, role, contact_person, contact_phone, contact_email, is_certified, is_delete, create_time, update_time) VALUES
(1, 'farmer1', '$2a$10$example_hash', 'farmer', '张三', '13800138001', 'zhangsan@example.com', 1, 0, '2023-01-01 10:00:00', '2023-01-01 10:00:00'),
(2, 'farmer2', '$2a$10$example_hash', 'farmer', '李四', '13800138002', 'lisi@example.com', 1, 0, '2023-01-02 10:00:00', '2023-01-02 10:00:00'),
(3, 'purchaser1', '$2a$10$example_hash', 'purchaser', '王五', '13800138003', 'wangwu@example.com', 1, 0, '2023-01-03 10:00:00', '2023-01-03 10:00:00'),
(4, 'purchaser2', '$2a$10$example_hash', 'purchaser', '赵六', '13800138004', 'zhaoliu@example.com', 1, 0, '2023-01-04 10:00:00', '2023-01-04 10:00:00'),
(5, 'admin', '$2a$10$example_hash', 'admin', '管理员', '13800138005', 'admin@example.com', 1, 0, '2023-01-05 10:00:00', '2023-01-05 10:00:00');

-- 产地区域表
INSERT INTO `origin_area` (area_code, area_name, province, city, feature, is_poverty_area, create_time, update_time) 
VALUES ('510124', '郫都区', '四川省', '成都市', '特色农产品产区', 0, '2023-01-01 00:00:00', '2023-01-01 00:00:00');

-- 插入农户信息（直接使用area_id）
INSERT INTO `farmer_info` (id, user_id, farm_name, origin_area_id, production_scale, certifications, bank_account, bank_name, audit_status, audit_remark, create_time, update_time) VALUES
(1, 2, '测试农场', 1, '中小型', '有机认证', '6222021234567890', '中国农业银行', 'APPROVED', '审核通过', '2023-01-01 10:00:00', '2023-01-01 10:00:00'),
(2, 1, '测试农场2', 1, '小型', '绿色认证', '6222021234567891', '中国工商银行', 'APPROVED', '审核通过', '2023-01-01 10:00:00', '2023-01-01 10:00:00');

-- 插入采购方信息
INSERT INTO `purchaser_info` (id, user_id, company_name, company_type, business_license, purchase_scale, preferred_origin, audit_status, audit_remark, create_time, update_time) VALUES
(1, 3, '测试采购公司', '企业', 'http://example.com/license.jpg', '大型', '四川省', 'APPROVED', '审核通过', '2023-01-01 10:00:00', '2023-01-01 10:00:00');

-- 插入产品分类
INSERT INTO `product_category` (id, name, parent_id, attribute, status, create_time) VALUES
(2, '蔬菜', 0, '新鲜蔬菜', 'active', '2023-01-01 10:00:00'),
(3, '叶菜类', 2, '叶菜类蔬菜', 'active', '2023-01-01 10:00:00');

-- 插入农产品
INSERT INTO `farmer_product` (id, farmer_id, category_id, name, spec, unit, price, min_purchase, stock, production_date, shelf_life, production_method, origin_area_id, description, status, create_time, update_time) VALUES
(1, 1, 3, '白菜', '新鲜', '斤', 2.50, 10, 100, '2023-01-01', '7天', '有机种植', 1, '新鲜有机白菜', 'on_sale', '2023-01-01 10:00:00', '2023-01-01 10:00:00');

-- 插入产品图片
INSERT INTO `product_image` (id, product_id, image_url, image_type, sort, create_time) VALUES
(1, 1, 'http://example.com/cabbage.jpg', 'MAIN', 1, '2023-01-01 10:00:00');

-- 插入采购需求
INSERT INTO `purchase_demand` (id, purchaser_id, category_id, product_name, spec_require, quantity, unit, price_range, delivery_date, delivery_address, quality_require, status, create_time, update_time) VALUES
(1, 1, 2, '白菜', '新鲜有机', 50, '斤', '2.0-3.0', '2023-01-08', '四川省成都市', '新鲜有机蔬菜', 'pending', '2023-01-01 10:00:00', '2023-01-01 10:00:00');

-- 插入对接记录
INSERT INTO `docking_record` (id, demand_id, farmer_id, product_id, quote_price, can_supply, supply_time, contact_way, remark, status, purchaser_remark, create_time, update_time) VALUES
(1, 1, 1, 1, 2.80, 30, '2023-01-04', '13800000001', '优质有机白菜', 'pending', NULL, '2023-01-01 10:00:00', '2023-01-01 10:00:00');

-- 插入库存预留
INSERT INTO `stock_reservation` (id, product_id, order_id, reserved_quantity, status, release_reason, expired_time, create_time, update_time) VALUES
(1, 1, 1, 30, 'reserved', NULL, '2023-01-01 11:00:00', '2023-01-01 10:00:00', '2023-01-01 10:00:00');

-- 插入用户认证申请
INSERT INTO `user_certification_apply` (id, user_id, apply_type, id_number, id_card_front_url, id_card_back_url, business_license_url, legal_representative, apply_reason, status, admin_remark, approved_time, create_time, update_time) VALUES
(1, 4, 'purchaser', '510124199001011234', 'http://example.com/id_front.jpg', 'http://example.com/id_back.jpg', 'http://example.com/business_license.jpg', '王五', '申请采购方认证', 'pending', NULL, NULL, '2023-01-01 10:00:00', '2023-01-01 10:00:00'),
(2, 1, 'farmer', '510124199002022345', 'http://example.com/id_front2.jpg', 'http://example.com/id_back2.jpg', NULL, NULL, '申请农户认证', 'pending', NULL, NULL, '2023-01-01 10:00:00', '2023-01-01 10:00:00');