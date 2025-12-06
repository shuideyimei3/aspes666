<template>
  <div class="dockings-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>对接记录</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="page-header">
      <h2>对接记录</h2>
      <p>查看与农户的对接记录和沟通信息</p>
    </div>
    
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="农户名称">
          <el-input 
            v-model="searchForm.farmerName" 
            placeholder="请输入农户名称" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="农场名称">
          <el-input 
            v-model="searchForm.farmName" 
            placeholder="请输入农场名称" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input 
            v-model="searchForm.productName" 
            placeholder="请输入产品名称" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="对接状态">
          <el-select v-model="searchForm.status" placeholder="请选择对接状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待回复" value="PENDING" />
            <el-option label="对接中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="对接时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-card class="table-card">
      <el-table 
        v-loading="loading" 
        :data="dockingsList" 
        style="width: 100%" 
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="对接ID" width="100" />
        <el-table-column prop="farmerName" label="农户名称" min-width="120" />
        <el-table-column prop="farmName" label="农场名称" min-width="120" />
        <el-table-column prop="demandProductName" label="产品名称" min-width="150">
          <template #default="scope">
            {{ scope.row.demandProductName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="demandTitle" label="关联需求" min-width="150" show-overflow-tooltip />
        <el-table-column prop="purchaserName" label="采购方名称" min-width="120" />
        <el-table-column prop="quotePrice" label="报价(元)" width="100" />
        <el-table-column prop="canSupply" label="可供应量" width="100" />
        <el-table-column prop="supplyTime" label="供应时间" width="120" />
        <el-table-column prop="status" label="对接状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastMessage" label="最新消息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="对接时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="viewDetail(scope.row.id)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING' || scope.row.status === 'IN_PROGRESS'"
              type="success" 
              link 
              @click="sendMessage(scope.row)"
            >
              发送消息
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING' || scope.row.status === 'IN_PROGRESS'"
              type="warning" 
              link 
              @click="handleDocking(scope.row)"
            >
              处理对接
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 发送消息对话框 -->
    <el-dialog 
      v-model="messageDialogVisible" 
      title="发送消息" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="messageForm" :rules="messageRules" ref="messageFormRef" label-width="80px">
        <el-form-item label="接收方">
          <el-input v-model="messageForm.receiverName" disabled />
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input 
            v-model="messageForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入消息内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="messageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMessage">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';
import { purchaserAPI } from '/api/index';

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  farmerName: '',
  farmName: '',
  productName: '',
  status: '',
  dateRange: []
});

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 排序数据
const sortData = reactive({
  prop: 'createTime',
  order: 'descending'
});

// 对接记录列表
const dockingsList = ref([]);
const loading = ref(false);

// 消息对话框
const messageDialogVisible = ref(false);
const messageFormRef = ref(null);
const messageForm = reactive({
  dockingId: '',
  receiverId: '',
  receiverName: '',
  content: ''
});
const messageRules = {
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 5, max: 500, message: '消息内容长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 获取对接记录列表
const fetchDockingsList = async () => {
  try {
    loading.value = true;
    
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      farmerName: searchForm.farmerName || undefined,
      farmName: searchForm.farmName || undefined,
      productName: searchForm.productName || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.dateRange && searchForm.dateRange[0] || undefined,
      endDate: searchForm.dateRange && searchForm.dateRange[1] || undefined,
      sortBy: sortData.prop,
      sortOrder: sortData.order === 'ascending' ? 'asc' : 'desc'
    };
    
    const response = await purchaserAPI.getDockingsList(params);
    
    if (response.code === 200) {
      dockingsList.value = response.data.records || [];
      pagination.total = Number(response.data.total || 0);
    } else {
      ElMessage.error(response.message || '获取对接记录列表失败');
    }
  } catch (error) {
    console.error('获取对接记录列表失败:', error);
    ElMessage.error('获取对接记录列表失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchDockingsList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.farmerName = '';
  searchForm.farmName = '';
  searchForm.productName = '';
  searchForm.status = '';
  searchForm.dateRange = [];
  pagination.currentPage = 1;
  fetchDockingsList();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  pagination.currentPage = 1;
  fetchDockingsList();
};

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  fetchDockingsList();
};

// 排序变化
const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop;
  sortData.order = order;
  fetchDockingsList();
};

// 查看详情
const viewDetail = (id) => {
  router.push(`/purchaser/dockings/${id}`);
};

// 发送消息
const sendMessage = (row) => {
  messageForm.dockingId = row.id;
  messageForm.receiverId = row.farmerId;
  messageForm.receiverName = row.farmerName;
  messageForm.content = '';
  messageDialogVisible.value = true;
};

// 提交消息
const submitMessage = async () => {
  if (!messageFormRef.value) return;
  
  try {
    await messageFormRef.value.validate();
    
    const response = await purchaserAPI.sendDockingMessage({
      dockingId: messageForm.dockingId,
      receiverId: messageForm.receiverId,
      content: messageForm.content
    });
    
    if (response.code === 200) {
      ElMessage.success('消息发送成功');
      messageDialogVisible.value = false;
      fetchDockingsList(); // 重新获取列表
    } else {
      ElMessage.error(response.message || '消息发送失败');
    }
  } catch (error) {
    console.error('消息发送失败:', error);
    ElMessage.error('消息发送失败');
  }
};

// 处理对接
const handleDocking = (row) => {
  router.push(`/purchaser/dockings/${row.id}`);
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'IN_PROGRESS': return 'primary';
    case 'COMPLETED': return 'success';
    case 'CANCELLED': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待回复';
    case 'IN_PROGRESS': return '对接中';
    case 'COMPLETED': return '已完成';
    case 'CANCELLED': return '已取消';
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
  fetchDockingsList();
});
</script>

<style scoped>
.dockings-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-bar {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-form {
  margin-bottom: -18px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-table) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .search-form {
    display: block;
  }
  
  .el-form-item {
    margin-right: 0;
    margin-bottom: 18px;
  }
}
</style>
