<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>采购需求数</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.demandCount || 0 }}</div>
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
              <span>支付总额</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.totalAmount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>对接数</span>
            </div>
          </template>
          <div class="stat-value">{{ stats.dockingCount || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const stats = ref({
  demandCount: 3,
  orderCount: 5,
  totalAmount: 50000,
  dockingCount: 8,
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
</style>
