<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="sidebar">
      <el-menu router @select="handleMenuSelect">
        <el-menu-item index="/purchaser/dashboard">
          <el-icon><House /></el-icon>
          <span>仪表板</span>
        </el-menu-item>
        <el-menu-item index="/purchaser/info">
          <el-icon><User /></el-icon>
          <span>采购方信息</span>
        </el-menu-item>
        <el-menu-item index="/purchaser/demands">
          <el-icon><DocumentCopy /></el-icon>
          <span>采购需求</span>
        </el-menu-item>
        <el-menu-item index="/purchaser/products">
          <el-icon><Goods /></el-icon>
          <span>产品浏览</span>
        </el-menu-item>
        <el-menu-item index="/purchaser/orders">
          <el-icon><ShoppingCart /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/purchaser/contracts">
          <el-icon><DocumentAdd /></el-icon>
          <span>合同管理</span>
        </el-menu-item>
        <el-menu-item index="/purchaser/payments">
          <el-icon><Money /></el-icon>
          <span>支付管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { House, User, DocumentCopy, ShoppingCart, DocumentAdd, Money, Goods } from '@element-plus/icons-vue';

const handleMenuSelect = (key) => {
  // 路由已经在菜单项中配置了
};
</script>

<style scoped>
.layout-container {
  height: calc(100vh - 60px);
}

.sidebar {
  background-color: #fff;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}

:deep(.el-menu) {
  border: none;
}

:deep(.el-main) {
  padding: 20px;
  background-color: #f0f2f5;
}
</style>
