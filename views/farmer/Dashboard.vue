<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>产品总数</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.productCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>对接数量</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.dockingCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>订单数量</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.orderCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>交易额</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.totalAmount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近产品</span>
              <router-link to="/farmer/products" class="link-btn">查看全部</router-link>
            </div>
          </template>
          <el-table :data="recentProducts" size="small" stripe>
            <el-table-column prop="name" label="产品名称" />
            <el-table-column prop="status" label="状态" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <router-link to="/farmer/orders" class="link-btn">查看全部</router-link>
            </div>
          </template>
          <el-table :data="recentOrders" size="small" stripe>
            <el-table-column prop="orderNo" label="订单号" />
            <el-table-column prop="status" label="状态" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" size="small">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const stats = ref({
  productCount: 5,
  dockingCount: 3,
  orderCount: 2,
  totalAmount: 15000,
});

const recentProducts = ref([
  { id: 1, name: '有机水稻', status: '已上架' },
  { id: 2, name: '农家鸡蛋', status: '已上架' },
  { id: 3, name: '新鲜蔬菜', status: '已下架' },
]);

const recentOrders = ref([
  { id: 1, orderNo: 'PO20240101001', status: '已完成' },
  { id: 2, orderNo: 'PO20240101002', status: '处理中' },
  { id: 3, orderNo: 'PO20240101003', status: '待支付' },
]);

onMounted(() => {
  // 加载数据
});
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stat-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  text-align: center;
  padding: 20px 0;
}

.mt-20 {
  margin-top: 20px;
}

.link-btn {
  color: #667eea;
  text-decoration: none;
  font-size: 12px;
}

.link-btn:hover {
  text-decoration: underline;
}

:deep(.el-table) {
  font-size: 12px;
}
</style>
