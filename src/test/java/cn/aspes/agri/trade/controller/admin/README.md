# Admin Controller 单元测试

本目录包含了所有管理员控制器的单元测试类。

## 测试覆盖范围

### 1. AdminOrderControllerTest
- 分页查询所有订单
- 查询订单详情
- 取消订单
- 强制完成订单
- 根据订单状态查询订单列表
- 获取订单统计信息
- 根据农户ID查询订单
- 根据采购方ID查询订单

### 2. AdminProductControllerTest
- 分页查询所有产品
- 查询产品详情
- 强制产品下架
- 强制产品上架
- 删除产品
- 根据产品名称搜索产品
- 获取产品统计信息

### 3. AdminUserControllerTest
- 分页查询用户列表
- 禁用/启用用户
- 强制用户下线
- 分页查询农户信息（审核）
- 审核农户信息
- 分页查询采购方信息（审核）
- 审核采购方信息

### 4. AdminCooperationControllerTest
- 删除评价

### 5. AdminStatisticsControllerTest
- 获取平台数据统计
- SSE实时推送市级农户活跃度

## 运行测试

### 运行单个测试类
```bash
mvn test -Dtest=AdminOrderControllerTest
```

### 运行所有Admin控制器测试
```bash
mvn test -Dtest=AdminControllerTestSuite
```

### 运行所有测试
```bash
mvn test
```

## 测试配置

测试使用了以下配置：
- H2内存数据库
- 嵌入式Redis
- Spring Security测试配置
- MockMvc进行Web层测试

## 测试覆盖率

所有测试类都覆盖了控制器的主要功能，包括：
- 正常流程测试
- 权限验证测试
- 边界条件测试

## 注意事项

1. 所有测试都使用了`@WithMockUser(roles = {"ADMIN"})`注解来模拟管理员权限
2. 使用了Mockito来模拟服务层的依赖
3. 测试数据在每个测试方法前都会重新初始化，确保测试之间的独立性