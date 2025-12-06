<template>
  <div class="orders-page">
    <el-card class="header-card">
      <h2>订单管理</h2>
    </el-card>

    <el-card class="content-card">
      <el-table :data="orders" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="产品名称" min-width="150">
          <template #default="{ row }">
            {{ row.productInfo?.name }}
          </template>
        </el-table-column>
        <el-table-column label="采购量" width="120">
          <template #default="{ row }">
            {{ row.quantity }} {{ row.productInfo?.unit }}
          </template>
        </el-table-column>
        <el-table-column label="总价" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewOrder(row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { adminAPI } from '../../api';

const router = useRouter();
const loading = ref(false);
const orders = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

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

const loadOrders = async () => {
  try {
    loading.value = true;
    const response = await adminAPI.getOrdersPage(currentPage.value, pageSize.value);
    orders.value = response.data.records;
    total.value = Number(response.data.total);
  } catch (error) {
    console.error('Load orders error:', error);
  } finally {
    loading.value = false;
  }
};

const viewOrder = (id) => {
  router.push(`/admin/orders/${id}`);
};

const handleSizeChange = () => {
  loadOrders();
};

const handleCurrentChange = () => {
  loadOrders();
};

onMounted(() => {
  loadOrders();
});
</script>

<style scoped>
.orders-page {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.content-card {
  min-height: 500px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
