# 农副产品电商对接平台 (Agricultural Trade Platform)

## 项目简介

农副产品电商对接平台是一个连接农户与采购方的B2B电商平台，旨在解决农产品销售渠道单一、供需信息不对称等问题。平台支持农户发布产品信息、采购方发布需求信息，并提供智能匹配、在线交易、物流跟踪等功能，助力农产品高效流通。

## 技术栈

- **后端框架**: Spring Boot 3.2.1
- **数据库**: MySQL 8.0
- **ORM框架**: MyBatis Plus 3.5.5
- **安全框架**: Spring Security + JWT
- **缓存**: Redis
- **文档**: SpringDoc OpenAPI (Swagger)
- **对象存储**: 阿里云OSS
- **工具库**: Hutool、ModelMapper、Thumbnailator
- **测试框架**: JUnit 5、RestAssured、H2 Database

## 核心功能

### 用户管理
- 多角色系统：农户、采购方、管理员
- 用户注册、登录、认证
- 资质审核与管理

### 农户功能
- 产品发布与管理
- 采购需求响应
- 订单管理与物流跟踪
- 资质认证申请

### 采购方功能
- 采购需求发布
- 产品搜索与筛选
- 供应商对接
- 合同管理与支付

### 平台管理
- 用户审核与管理
- 产品分类管理
- 数据统计与分析
- 系统配置管理

## 项目结构

```
src/main/java/cn/aspes/agri/trade/
├── AgriTradePlatformApplication.java  # 主启动类
├── common/                            # 公共组件
├── config/                            # 配置类
├── controller/                        # 控制器层
│   ├── admin/                         # 管理员接口
│   ├── common/                        # 通用接口
│   ├── farmer/                        # 农户接口
│   └── purchaser/                     # 采购方接口
├── converter/                         # 数据转换器
├── dto/                               # 数据传输对象
├── entity/                            # 实体类
├── enums/                             # 枚举类
├── exception/                         # 异常处理
├── filter/                            # 过滤器
├── handler/                           # 处理器
├── mapper/                            # MyBatis映射器
├── scheduled/                         # 定时任务
├── security/                          # 安全配置
├── service/                           # 服务层
├── util/                              # 工具类
└── vo/                                # 视图对象
```

## 数据库设计

系统包含以下核心数据表：

- `origin_area` - 产地信息表（含贫困地区标识）
- `user` - 用户表（农户/采购方/管理员）
- `farmer_info` - 农户信息表（含认证信息）
- `purchaser_info` - 采购方信息表（含认证信息）
- `product_category` - 农副产品分类表
- `farmer_product` - 农户产品表
- `product_image` - 产品图片表
- `purchase_demand` - 采购需求表
- `docking_record` - 对接记录表
- `purchase_contract` - 采购合同表
- `purchase_order` - 采购订单表
- `logistics_info` - 物流信息表

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. 克隆项目到本地
```bash
git clone https://github.com/your-username/agri-trade-platform.git
cd agri-trade-platform
```

2. 配置数据库
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE aspes CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入初始化SQL
mysql -u root -p aspes < init.sql
```

3. 修改配置文件
```bash
# 复制并修改配置文件
cp src/main/resources/application-example.yml src/main/resources/application-dev.yml
# 根据实际情况修改数据库连接、Redis连接等配置
```

4. 编译并运行项目
```bash
# 编译项目
mvn clean compile

# 运行项目
mvn spring-boot:run
```

5. 访问应用
- API文档: http://localhost:8080/swagger-ui.html
- 健康检查: http://localhost:8080/actuator/health

### Docker部署

```bash
# 构建镜像
docker build -t agri-trade-platform .

# 运行容器
docker run -d -p 8080:8080 --name agri-trade-platform agri-trade-platform
```

## API文档

项目使用SpringDoc OpenAPI自动生成API文档，启动项目后可通过以下地址访问：

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserServiceTest

# 生成测试覆盖率报告
mvn jacoco:report
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

- 项目维护者: [Your Name]
- 邮箱: [your.email@example.com]
- 项目链接: [https://github.com/your-username/agri-trade-platform](https://github.com/your-username/agri-trade-platform)

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现用户注册登录功能
- 实现产品发布与采购需求发布
- 实现供需对接功能
- 实现订单管理与物流跟踪