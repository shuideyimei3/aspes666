<template>
  <div class="home-container">
    <el-carousel class="carousel">
      <el-carousel-item v-for="item in 3" :key="item">
        <div class="carousel-item">
          <h2>农副产品电商平台</h2>
          <p>连接农户和采购方，促进农产品交易</p>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="features">
      <h2>平台功能</h2>
      <div class="feature-grid">
        <div class="feature-card">
          <el-icon class="feature-icon"><Shop /></el-icon>
          <h3>产品发布</h3>
          <p>农户可以轻松发布农产品，展示给采购方</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon"><Search /></el-icon>
          <h3>需求搜索</h3>
          <p>采购方发布需求，农户可以快速响应</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon"><DocumentCopy /></el-icon>
          <h3>合同管理</h3>
          <p>在线签署合同，保障交易安全</p>
        </div>
        <div class="feature-card">
          <el-icon class="feature-icon"><Money /></el-icon>
          <h3>支付结算</h3>
          <p>安全便捷的支付和结算方式</p>
        </div>
      </div>
    </div>

    <div class="quick-links">
      <h2>快速开始</h2>
      <div class="link-grid">
        <el-button
          v-if="!userStore.isLoggedIn"
          type="primary"
          size="large"
          @click="router.push('/login')"
        >
          登 录
        </el-button>
        <el-button
          v-if="!userStore.isLoggedIn"
          size="large"
          @click="router.push('/register')"
        >
          注 册
        </el-button>
        <el-button
          v-else
          type="primary"
          size="large"
          @click="goDashboard"
        >
          进入平台
        </el-button>
        <el-button size="large" @click="router.push('/search')">
          浏 览
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/user';
import { Shop, Search, DocumentCopy, Money } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();

const goDashboard = () => {
  const role = userStore.role?.toLowerCase();
  router.push(`/${role}/dashboard`);
};
</script>

<style scoped>
.home-container {
  padding: 0;
}

.carousel {
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 40px;
}

.carousel-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.carousel-item h2 {
  font-size: 36px;
  margin-bottom: 10px;
}

.carousel-item p {
  font-size: 18px;
  opacity: 0.9;
}

.features {
  margin-bottom: 60px;
}

.features h2 {
  text-align: center;
  font-size: 28px;
  margin-bottom: 30px;
  color: #333;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.feature-card {
  background: white;
  padding: 30px 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.feature-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 32px;
  color: #667eea;
  margin-bottom: 15px;
}

.feature-card h3 {
  margin: 15px 0;
  color: #333;
}

.feature-card p {
  color: #999;
  font-size: 14px;
}

.quick-links {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
}

.quick-links h2 {
  margin-bottom: 30px;
  color: #333;
}

.link-grid {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

:deep(.el-button) {
  min-width: 150px;
}
</style>
