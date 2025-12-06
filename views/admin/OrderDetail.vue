<template>
  <div class="order-detail-page" v-loading="loading">
    <div class="detail-container" v-if="order">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/admin' }">管理员首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/admin/orders' }">订单管理</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card class="order-card">
        <template #header>
          <div class="card-header">
            <span>订单详情</span>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
        </template>

        <el-descriptions title="订单信息" :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="合同ID">{{ order.contractId }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ order.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="交付时间" v-if="order.deliveryTime">{{ order.deliveryTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="product-card">
        <template #header>
          <span>产品信息</span>
        </template>
        <el-descriptions :column="2" border v-if="order.productInfo">
          <el-descriptions-item label="产品名称">{{ order.productInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="产品ID">{{ order.productInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="规格">{{ order.productInfo.spec }}</el-descriptions-item>
          <el-descriptions-item label="计量单位">{{ order.productInfo.unit }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ order.productInfo.price }} / {{ order.productInfo.unit }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ order.productInfo.stock }} {{ order.productInfo.unit }}</el-descriptions-item>
          <el-descriptions-item label="最小采购量">{{ order.productInfo.minPurchase }} {{ order.productInfo.unit }}</el-descriptions-item>
          <el-descriptions-item label="产地ID">{{ order.productInfo.originAreaId }}</el-descriptions-item>
          <el-descriptions-item label="农户ID">{{ order.productInfo.farmerId }}</el-descriptions-item>
          <el-descriptions-item label="产品状态">
            <el-tag :type="getProductStatusType(order.productInfo.status)">{{ getProductStatusText(order.productInfo.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="quantity-card">
        <template #header>
          <span>数量与金额</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订购数量">{{ order.quantity }} {{ order.productInfo?.unit }}</el-descriptions-item>
          <el-descriptions-item label="实际数量">{{ order.actualQuantity }} {{ order.productInfo?.unit }}</el-descriptions-item>
          <el-descriptions-item label="订购金额">¥{{ order.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="实际金额">¥{{ order.actualAmount }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="parties-card">
        <template #header>
          <span>交易方信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="农户ID">{{ order.farmerId }}</el-descriptions-item>
          <el-descriptions-item label="采购方ID">{{ order.purchaserId }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="inspection-card" v-if="order.inspectionResult">
        <template #header>
          <span>验收结果</span>
        </template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="验收结果">{{ order.inspectionResult }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { adminAPI } from '../../api';

const router = useRouter();
const route = useRoute();
const orderId = route.params.id;

const loading = ref(false);
const order = ref(null);

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'primary',
    'SHIPPED': 'info',
    'DELIVERED': 'success',
    'COMPLETED': 'success',
    'CANCELLED': 'danger',
  };
  return typeMap[status] || 'info';
};

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
  };
  return textMap[status] || status;
};

const getProductStatusType = (status) => {
  const typeMap = {
    'on_sale': 'success',
    'off_sale': 'info',
    'sold_out': 'danger',
  };
  return typeMap[status] || 'info';
};

const getProductStatusText = (status) => {
  const textMap = {
    'on_sale': '在售',
    'off_sale': '下架',
    'sold_out': '售罄',
  };
  return textMap[status] || status;
};

const loadOrderDetail = async () => {
  try {
    loading.value = true;
    const response = await adminAPI.getOrderDetail(orderId);
    order.value = response.data;
  } catch (error) {
    console.error('Load order detail error:', error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadOrderDetail();
});
</script>

<style scoped>
.order-detail-page {
  padding: 20px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
}

.order-card,
.product-card,
.quantity-card,
.parties-card,
.inspection-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>