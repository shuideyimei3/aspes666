<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-content">
        <router-link to="/" class="logo">
          <span class="logo-text">农副产品电商平台</span>
        </router-link>
        <div class="nav-links">
          <router-link to="/search" class="nav-link">搜索</router-link>
          <el-dropdown v-if="userStore.isLoggedIn">
            <span class="el-dropdown-link">
              {{ userStore.userInfo?.username }}
              <el-icon class="el-icon--right">
                <arrow-down />
              </el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <router-link :to="getDashboardPath()">个人中心</router-link>
                </el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <router-link v-else to="/login" class="nav-link">登录</router-link>
        </div>
      </div>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { userAPI } from '../api';

const router = useRouter();
const userStore = useUserStore();

const getDashboardPath = () => {
  const role = userStore.role?.toLowerCase();
  return `/${role}/dashboard`;
};

const handleLogout = async () => {
  try {
    await userAPI.logout();
    userStore.logout();
    ElMessage.success('已退出登录');
    router.push('/');
  } catch (error) {
    console.error('Logout error:', error);
    // 即使API调用失败，也清除本地数据
    userStore.logout();
    router.push('/');
  }
};
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background-color: #f0f2f5;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  text-decoration: none;
  color: #333;
  font-size: 18px;
  font-weight: bold;
}

.logo-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  text-decoration: none;
  color: #666;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-link:hover {
  color: #667eea;
  background-color: #f5f5f5;
}

:deep(.el-main) {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}
</style>
