<template>
  <div class="demand-detail-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/purchaser/demands' }">需求管理</el-breadcrumb-item>
      <el-breadcrumb-item>需求详情</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div v-loading="loading" class="detail-container">
      <!-- 需求基本信息 -->
      <el-card class="detail-card" v-if="demandDetail">
        <template #header>
          <div class="card-header">
            <span>需求信息</span>
            <el-tag :type="getStatusType(demandDetail.status)">
              {{ getStatusText(demandDetail.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求ID">{{ demandDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatDate(demandDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ demandDetail.companyName }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ demandDetail.productName }}</el-descriptions-item>
          <el-descriptions-item label="规格要求">{{ demandDetail.specRequire }}</el-descriptions-item>
          <el-descriptions-item label="需求量">{{ demandDetail.quantity }} {{ demandDetail.unit }}</el-descriptions-item>
          <el-descriptions-item label="价格范围">{{ demandDetail.priceRange }}</el-descriptions-item>
          <el-descriptions-item label="交货日期">{{ demandDetail.deliveryDate }}</el-descriptions-item>
          <el-descriptions-item label="交货地址">{{ demandDetail.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="质量要求" :span="2">{{ demandDetail.qualityRequire }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 对接记录 -->
      <el-card class="dockings-card">
        <template #header>
          <div class="card-header">
            <span>对接记录</span>
            <el-button type="primary" @click="fetchDockingsList" :icon="Refresh">刷新</el-button>
          </div>
        </template>
        
        <el-table 
          v-loading="dockingsLoading" 
          :data="dockingsList" 
          style="width: 100%" 
          stripe
        >
          <el-table-column prop="id" label="对接ID" width="100" />
          <el-table-column prop="farmName" label="农场名称" min-width="120" />
          <el-table-column prop="quotePrice" label="报价(元)" width="100" />
          <el-table-column prop="canSupply" label="可供应量" width="100" />
          <el-table-column prop="supplyTime" label="供应时间" width="120" />
          <el-table-column prop="contactWay" label="联系方式" min-width="150" />
          <el-table-column prop="status" label="对接状态" width="100">
            <template #default="scope">
              <el-tag :type="getDockingStatusType(scope.row.status)">
                {{ getDockingStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="对接时间" width="160">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button 
                v-if="scope.row.status === 'pending' || scope.row.status === 'agreed'"
                type="success" 
                link 
                @click="showHandleDialog(scope.row, 'ACCEPT')"
              >
                接受
              </el-button>
              <el-button 
                v-if="scope.row.status === 'pending' || scope.row.status === 'agreed'"
                type="danger" 
                link 
                @click="showHandleDialog(scope.row, 'REJECT')"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container" v-if="dockingsPagination.total > 0">
          <el-pagination
            v-model:current-page="dockingsPagination.currentPage"
            v-model:page-size="dockingsPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="dockingsPagination.total"
            @size-change="handleDockingsSizeChange"
            @current-change="handleDockingsCurrentChange"
          />
        </div>
      </el-card>
    </div>
    
    <!-- 处理对接对话框 -->
    <el-dialog 
      v-model="handleDialogVisible" 
      :title="handleAction === 'ACCEPT' ? '接受对接' : '拒绝对接'" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="handleForm" :rules="handleRules" ref="handleFormRef" label-width="80px">
        <el-form-item label="农场名称">
          <el-input v-model="handleForm.farmName" disabled />
        </el-form-item>
        <el-form-item label="报价">
          <el-input v-model="handleForm.quotePrice" disabled />
        </el-form-item>
        <el-form-item label="可供应量">
          <el-input v-model="handleForm.canSupply" disabled />
        </el-form-item>
        <el-form-item label="处理意见" prop="remark">
          <el-input 
            v-model="handleForm.remark" 
            type="textarea" 
            :rows="4" 
            :placeholder="handleAction === 'ACCEPT' ? '请输入接受对接的意见' : '请输入拒绝对接的理由'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import { purchaserAPI } from '/api/index';

const route = useRoute();

// 需求详情
const demandDetail = ref(null);
const loading = ref(false);

// 对接记录
const dockingsList = ref([]);
const dockingsLoading = ref(false);
const dockingsPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 处理对接对话框
const handleDialogVisible = ref(false);
const handleFormRef = ref(null);
const handleAction = ref('');
const handleForm = reactive({
  dockingId: '',
  farmerName: '',
  farmName: '',
  quotePrice: '',
  canSupply: '',
  remark: ''
});
const handleRules = {
  remark: [
    { required: true, message: '请输入处理意见', trigger: 'blur' },
    { min: 5, max: 500, message: '处理意见长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 获取需求详情
const fetchDemandDetail = async () => {
  try {
    loading.value = true;
    const demandId = route.params.id;
    
    const response = await purchaserAPI.getDemandDetail(demandId);
    
    if (response.code === 200) {
      demandDetail.value = response.data;
    } else {
      ElMessage.error(response.message || '获取需求详情失败');
    }
  } catch (error) {
    console.error('获取需求详情失败:', error);
    ElMessage.error('获取需求详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取对接记录列表
const fetchDockingsList = async () => {
  try {
    dockingsLoading.value = true;
    const demandId = route.params.id;
    
    const response = await purchaserAPI.getDemandDockings(demandId, dockingsPagination.currentPage, dockingsPagination.pageSize);
    
    if (response.code === 200) {
      dockingsList.value = response.data.records || [];
      dockingsPagination.total = Number(response.data.total || 0);
    } else {
      ElMessage.error(response.message || '获取对接记录失败');
    }
  } catch (error) {
    console.error('获取对接记录失败:', error);
    ElMessage.error('获取对接记录失败');
  } finally {
    dockingsLoading.value = false;
  }
};

// 分页大小变化
const handleDockingsSizeChange = (size) => {
  dockingsPagination.pageSize = size;
  dockingsPagination.currentPage = 1;
  fetchDockingsList();
};

// 当前页变化
const handleDockingsCurrentChange = (page) => {
  dockingsPagination.currentPage = page;
  fetchDockingsList();
};

// 显示处理对接对话框
const showHandleDialog = (row, action) => {
  handleAction.value = action;
  handleForm.dockingId = row.id;
  handleForm.farmerName = row.farmerName;
  handleForm.farmName = row.farmName;
  handleForm.quotePrice = row.quotePrice;
  handleForm.canSupply = row.canSupply;
  handleForm.remark = '';
  handleDialogVisible.value = true;
};

// 提交处理对接
const submitHandle = async () => {
  if (!handleFormRef.value) return;
  
  try {
    await handleFormRef.value.validate();
    
    const data = {
      status: handleAction.value === 'ACCEPT' ? 'agreed' : 'rejected',
      remark: handleForm.remark
    };
    
    const response = await purchaserAPI.handleDocking(handleForm.dockingId, data);
    
    if (response.code === 200) {
      ElMessage.success(`对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}成功`);
      handleDialogVisible.value = false;
      // 重新获取对接记录
      await fetchDockingsList();
    } else {
      ElMessage.error(response.message || `对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}失败`);
    }
  } catch (error) {
    console.error(`对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}失败:`, error);
    ElMessage.error(`对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}失败`);
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'published': return 'success';
    case 'matched': return 'warning';
    case 'completed': return 'info';
    case 'cancelled': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'published': return '已发布';
    case 'matched': return '已匹配';
    case 'completed': return '已完成';
    case 'cancelled': return '已取消';
    default: return '未知状态';
  }
};

// 获取对接状态类型
const getDockingStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning';
    case 'agreed': return 'success';
    case 'rejected': return 'danger';
    default: return 'info';
  }
};

// 获取对接状态文本
const getDockingStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理';
    case 'agreed': return '已同意';
    case 'rejected': return '已拒绝';
    default: return '未知状态';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchDemandDetail();
  fetchDockingsList();
});
</script>

<style scoped>
.demand-detail-page {
  padding: 20px;
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card, .dockings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-descriptions) {
  margin-bottom: 20px;
}

:deep(.el-table) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .detail-container {
    gap: 15px;
  }
}
</style>
