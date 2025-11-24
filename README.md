# 农副产品电商对接平台

## 项目简介

农副产品电商对接平台是一个基于Spring Boot开发的B2B/B2C农副产品交易系统，旨在连接农户与采购方，提供从产品发布、需求对接、合同签订到物流配送的全流程服务。平台支持贫困地区农副产品直销，助力乡村振兴。

## 技术栈

- **后端框架**: Spring Boot 3.2.1
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM框架**: MyBatis Plus 3.5.5
- **安全框架**: Spring Security + JWT
- **API文档**: SpringDoc OpenAPI 3
- **文件存储**: 阿里云OSS
- **工具库**: Hutool、ModelMapper、Lombok
- **测试框架**: JUnit 5、RestAssured、H2 Database

## 系统架构

### 核心模块

1. **用户管理模块**
   - 农户认证与管理
   - 采购方认证与管理
   - 管理员权限控制

2. **产品管理模块**
   - 农户产品发布与展示
   - 产品分类管理
   - 产品图片管理

3. **采购需求模块**
   - 采购方需求发布
   - 需求匹配与对接
   - 对接记录管理

4. **交易管理模块**
   - 合同生成与签署
   - 订单管理
   - 支付记录管理

5. **物流管理模块**
   - 物流记录跟踪
   - 物流轨迹查询

6. **评价系统**
   - 双方互评机制
   - 信用评价体系

## 数据库设计

系统包含17张核心数据表，主要表结构如下：

- `user`: 用户基础信息表
- `farmer_info`: 农户详细信息表
- `purchaser_info`: 采购方详细信息表
- `origin_area`: 产地信息表（含贫困地区标识）
- `product_category`: 产品分类表
- `farmer_product`: 农户产品表
- `product_image`: 产品图片表
- `purchase_demand`: 采购需求表
- `docking_record`: 对接记录表
- `purchase_contract`: 采购合同表
- `purchase_order`: 采购订单表
- `payment_record`: 支付记录表
- `logistics_record`: 物流记录表
- `logistics_trace`: 物流轨迹表
- `cooperation_review`: 合作评价表
- `stock_reservation`: 库存预留表
- `user_certification_apply`: 用户认证申请表

## 快速开始

### 环境要求

- JDK 17
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. 克隆项目到本地
```bash
git clone [项目地址]
cd aspes666
```

2. 创建数据库并执行初始化脚本
```bash
mysql -u root -p
CREATE DATABASE aspes;
USE aspes;
source init.sql;
```

3. 修改配置文件
编辑 `src/main/resources/application.yml`，配置数据库连接、Redis连接等信息。

4. 安装依赖并启动项目
```bash
mvn clean install
mvn spring-boot:run
```

5. 访问应用
- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## API接口

平台提供RESTful API接口，主要接口分类如下：

- **通用接口** (`/api/common`)
  - 用户认证 (`/auth`)
  - 合作评价 (`/cooperation-review`)

- **B2C接口** (`/api/b2c`)
  - 农户信息 (`/farmer-info`)
  - 产品管理 (`/products`)
  - 物流管理 (`/logistics`)

- **B2B接口** (`/api/b2b`)
  - 采购方信息 (`/purchaser-info`)
  - 采购需求 (`/demands`)
  - 采购合同 (`/purchase-contract`)
  - 采购订单 (`/purchase-order`)
  - 支付管理 (`/payment`)

- **管理员接口** (`/api/admin`)
  - 用户管理 (`/admin-user`)
  - 产品图片管理 (`/product-image`)
  - 统计分析 (`/statistics`)
  - 支付管理 (`/payment`)
  - 合作审核 (`/cooperation`)

- **共享接口** (`/api/shared`)
  - 对接记录 (`/docking-record`)
  - 产地信息 (`/origin-area`)
  - 产品分类 (`/product-category`)
  - 库存预留 (`/stock-reservation`)

## 项目特色

1. **贫困地区支持**: 专门标识贫困地区农副产品，助力扶贫攻坚
2. **全流程追溯**: 从产地到餐桌的完整产品溯源体系
3. **灵活对接**: 支持农户主动响应采购需求，提高对接效率
4. **库存预留**: 防止超卖机制，保障交易可靠性
5. **分阶段支付**: 支持预付款+尾款的灵活支付方式
6. **物流跟踪**: 完整的物流轨迹记录，实时掌握货物状态

## 开发规范

1. 代码风格遵循阿里巴巴Java开发手册
2. 统一使用RESTful API设计风格
3. 所有接口返回统一格式的Result对象
4. 使用JWT进行身份认证
5. 敏感操作需要相应角色权限

## 测试

项目包含完整的单元测试和集成测试：

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ProductServiceTest
```

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: shuideyimei
- 邮箱: shuideyimei@gmail.com
- 项目地址: https://github.com/shuideyimei3/aspes666

## 更新日志

### v1.0.0 (2025-11-18)
- 初始版本发布
- 实现基础的用户管理、产品管理、采购需求管理功能
- 完成对接记录、合同管理、订单管理核心流程
- 添加物流跟踪和评价系统
