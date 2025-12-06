<template>
  <div class="farmer-layout">
    <el-container>
      <el-aside width="250px">
        <div class="logo">
          <h2>农户管理平台</h2>
        </div>
        <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/farmer/dashboard">
            <el-icon><House /></el-icon>
            <span>仪表板</span>
          </el-menu-item>
          <el-menu-item index="/farmer/info">
            <el-icon><User /></el-icon>
            <span>农户信息</span>
          </el-menu-item>
          <el-menu-item index="/farmer/products">
            <el-icon><Box /></el-icon>
            <span>我的产品</span>
          </el-menu-item>
          <el-menu-item index="/farmer/dockings">
            <el-icon><Connection /></el-icon>
            <span>对接记录</span>
          </el-menu-item>
          <el-menu-item index="/farmer/orders">
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/farmer/contracts">
            <el-icon><Files /></el-icon>
            <span>合同管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header>
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/farmer' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item>{{ getCurrentPageName() }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ userStore.username }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '/stores/user';
import { ElMessage } from 'element-plus';
import { 
  House, 
  User, 
  Box, 
  Connection, 
  Document, 
  Files,
  Van,
  ArrowDown 
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

// 获取当前页面名称
const getCurrentPageName = () => {
  const path = route.path;
  if (path.includes('/dashboard')) return '仪表板';
  if (path.includes('/info')) return '农户信息';
  if (path.includes('/products')) return '我的产品';
  if (path.includes('/dockings')) return '对接记录';
  if (path.includes('/orders')) return '订单管理';
  if (path.includes('/contracts')) return '合同管理';
  if (path.includes('/logistics')) return '物流管理';
  return '未知页面';
};

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/farmer/info');
  } else if (command === 'logout') {
    userStore.logout();
    ElMessage.success('退出登录成功');
    router.push('/login');
  }
};
</script>

<style scoped>
.farmer-layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #bfcbd9;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: #fff;
  border-bottom: 1px solid #3a3f51;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-item {
  height: 50px;
  line-height: 50px;
}

.el-header {
  background-color: #fff;
  color: #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>