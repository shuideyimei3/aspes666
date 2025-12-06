<template>
  <div class="demands-page">
    <el-card class="header-card">
      <div class="header-actions">
        <h2>采购需求</h2>
        <el-button type="primary" @click="router.push('/purchaser/demands/publish')">
          <el-icon><Plus /></el-icon>
          发布需求
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card">
      <el-table :data="demands" v-loading="loading" stripe>
        <el-table-column prop="productName" label="产品名称" min-width="120" />
        <el-table-column prop="specRequire" label="规格要求" min-width="120" />
        <el-table-column label="采购量" width="150">
          <template #default="{ row }">
            {{ row.quantity }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="priceRange" label="价格范围" width="150" />
        <el-table-column prop="deliveryDate" label="交货日期" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewDemand(row.id)">
              查看
            </el-button>
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="editDemand(row.id)"
              v-if="row.status === 'published'"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="cancelDemand(row)"
              v-if="row.status === 'published'"
            >
              取消
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { purchaserAPI } from '../../api';
import { bigIntToString, processResponseData } from '../../utils/bigint';

const router = useRouter();
const loading = ref(false);
const demands = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const getStatusType = (status) => {
  const typeMap = {
    'published': 'success',
    'matched': 'warning',
    'completed': 'info',
    'cancelled': 'danger',
  };
  return typeMap[status] || 'info';
};

const getStatusText = (status) => {
  const textMap = {
    'published': '已发布',
    'matched': '已匹配',
    'completed': '已完成',
    'cancelled': '已取消',
  };
  return textMap[status] || status;
};

const loadDemands = async () => {
  try {
    loading.value = true;
    const response = await purchaserAPI.getDemandsPage(currentPage.value, pageSize.value);
    // 响应数据已经在拦截器中处理过，但为了确保安全，再次处理
    const processedData = processResponseData(response.data);
    demands.value = processedData.records;
    total.value = Number(processedData.total);
  } catch (error) {
    console.error('Load demands error:', error);
  } finally {
    loading.value = false;
  }
};

const viewDemand = (id) => {
  // 跳转到需求详情页
  router.push(`/purchaser/demands/${bigIntToString(id)}`);
};

const editDemand = (id) => {
  router.push(`/purchaser/demands/${bigIntToString(id)}/edit`);
};

const cancelDemand = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该需求吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    // 确保ID为字符串类型
    await purchaserAPI.updateDemand(bigIntToString(row.id), { ...row, status: 'cancelled' });
    ElMessage.success('取消成功');
    loadDemands();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Cancel demand error:', error);
    }
  }
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  loadDemands();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadDemands();
};

onMounted(() => {
  loadDemands();
});
</script>

<style scoped>
.demands-page {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions h2 {
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
