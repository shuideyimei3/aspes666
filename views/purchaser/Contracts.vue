<template>
  <div class="contracts-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>合同管理</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="page-header">
      <h2>合同管理</h2>
      <p>查看和管理您的采购合同</p>
    </div>
    
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="合同编号">
          <el-input 
            v-model="searchForm.contractNo" 
            placeholder="请输入合同编号" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="农户名称">
          <el-input 
            v-model="searchForm.farmerName" 
            placeholder="请输入农户名称" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="searchForm.status" placeholder="请选择合同状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待签署" value="PENDING_SIGN" />
            <el-option label="已签署" value="SIGNED" />
            <el-option label="已终止" value="TERMINATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="签署时间">
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
        :data="contractsList" 
        style="width: 100%" 
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column prop="contractNo" label="合同编号" width="150" />
        <el-table-column prop="orderNo" label="关联订单" width="150" />
        <el-table-column prop="farmerName" label="农户名称" min-width="120" />
        <el-table-column prop="productName" label="产品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="合同金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="合同状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签署状态" width="120">
          <template #default="scope">
            <div class="sign-status">
              <div class="sign-item">
                <span>农户: </span>
                <el-tag :type="scope.row.farmerSignTime ? 'success' : 'info'" size="small">
                  {{ scope.row.farmerSignTime ? '已签署' : '未签署' }}
                </el-tag>
              </div>
              <div class="sign-item">
                <span>采购方: </span>
                <el-tag :type="scope.row.purchaserSignTime ? 'success' : 'info'" size="small">
                  {{ scope.row.purchaserSignTime ? '已签署' : '未签署' }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="viewDetail(scope.row.id)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING_SIGN' && !scope.row.purchaserSignTime"
              type="success" 
              link 
              @click="signContract(scope.row)"
            >
              签署合同
            </el-button>
            <el-button 
              v-if="scope.row.contractFileUrl"
              type="info" 
              link 
              @click="downloadContract(scope.row)"
            >
              下载合同
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
    
    <!-- 对接记录卡片 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>对接记录</span>
          <el-button type="primary" @click="refreshDockingsList">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
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
        <el-table-column prop="purchaserName" label="采购方名称" min-width="120" />
        <el-table-column prop="productId" label="产品ID" width="100" />
        <el-table-column prop="quotePrice" label="报价(元)" width="100" />
        <el-table-column prop="canSupply" label="可供应量" width="100" />
        <el-table-column prop="supplyTime" label="供应时间" width="120" />
        <el-table-column prop="contactWay" label="联系方式" min-width="150" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'agreed'"
              type="primary" 
              link 
              @click="showCreateContractDialog(scope.row)"
            >
              创建合同
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
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
    
    <!-- 签署合同对话框 -->
    <el-dialog 
      v-model="signContractDialogVisible" 
      title="签署合同" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="signContractForm" :rules="signContractRules" ref="signContractFormRef" label-width="100px">
        <el-form-item label="合同编号">
          <el-input v-model="signContractForm.contractNo" disabled />
        </el-form-item>
        <el-form-item label="签名文件" prop="signFile">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
            accept=".jpg,.jpeg,.png,.pdf"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传签名文件，支持jpg、png、pdf格式，文件大小不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="signContractDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSignContract">确认签署</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 创建合同对话框 -->
    <el-dialog 
      v-model="createContractDialogVisible" 
      title="创建合同" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="createContractForm" :rules="createContractRules" ref="createContractFormRef" label-width="100px">
        <el-form-item label="对接ID">
          <el-input v-model="createContractForm.dockingId" disabled />
        </el-form-item>
        <el-form-item label="农户名称">
          <el-input v-model="createContractForm.farmerName" disabled />
        </el-form-item>
        <el-form-item label="农场名称">
          <el-input v-model="createContractForm.farmName" disabled />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="createContractForm.productName" disabled />
        </el-form-item>
        <el-form-item label="产品ID">
          <el-input v-model="createContractForm.productId" disabled />
        </el-form-item>
        <el-form-item label="产品数量" prop="quantity">
          <el-input-number 
            v-model="createContractForm.quantity" 
            :min="1" 
            :max="createContractForm.canSupply"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="付款方式" prop="paymentTerms">
          <el-select v-model="createContractForm.paymentTerms" placeholder="请选择付款方式">
            <el-option label="预付全款" value="预付全款" />
            <el-option label="预付50%" value="预付50%" />
            <el-option label="预付30%" value="预付30%" />
            <el-option label="货到付款" value="货到付款" />
            <el-option label="分期付款" value="分期付款" />
          </el-select>
        </el-form-item>
        <el-form-item label="交货时间" prop="deliveryTime">
          <el-date-picker
            v-model="createContractForm.deliveryTime"
            type="date"
            placeholder="选择交货时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="交货地址" prop="deliveryAddress">
          <el-input 
            v-model="createContractForm.deliveryAddress" 
            type="textarea" 
            :rows="2"
            placeholder="请输入交货地址"
          />
        </el-form-item>
        <el-form-item label="质量标准">
          <el-input 
            v-model="createContractForm.qualityStandards" 
            type="textarea" 
            :rows="2"
            placeholder="请输入质量标准"
          />
        </el-form-item>
        <el-form-item label="违约条款">
          <el-input 
            v-model="createContractForm.breachTerms" 
            type="textarea" 
            :rows="2"
            placeholder="请输入违约条款"
          />
        </el-form-item>
        <el-form-item label="合同文件" prop="signFile">
          <el-upload
            ref="contractUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleContractFileChange"
            :file-list="contractFileList"
            accept=".jpg,.jpeg,.png,.pdf"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传合同文件，支持jpg、png、pdf格式，文件大小不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createContractDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreateContract">创建合同</el-button>
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
  contractNo: '',
  farmerName: '',
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

// 合同列表
const contractsList = ref([]);
const loading = ref(false);

// 对接记录
const dockingsList = ref([]);
const dockingsLoading = ref(false);
const dockingsPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 签署合同对话框
const signContractDialogVisible = ref(false);
const signContractFormRef = ref(null);
const uploadRef = ref(null);
const signContractForm = reactive({
  id: '',
  contractNo: '',
  signFile: null
});
const signContractRules = {
  signFile: [{ required: true, message: '请上传签名文件', trigger: 'change' }]
};
const fileList = ref([]);

// 创建合同对话框
const createContractDialogVisible = ref(false);
const createContractFormRef = ref(null);
const contractUploadRef = ref(null);
const createContractForm = reactive({
  dockingId: '',
  farmerName: '',
  farmName: '',
  productName: '',
  productId: '',
  quantity: 1,
  canSupply: 1,
  paymentTerms: '',
  deliveryTime: '',
  deliveryAddress: '',
  qualityStandards: '',
  breachTerms: '',
  signFile: null
});
const createContractRules = {
  quantity: [{ required: true, message: '请输入产品数量', trigger: 'blur' }],
  paymentTerms: [{ required: true, message: '请选择付款方式', trigger: 'change' }],
  deliveryTime: [{ required: true, message: '请选择交货时间', trigger: 'change' }],
  deliveryAddress: [{ required: true, message: '请输入交货地址', trigger: 'blur' }],
  signFile: [{ required: true, message: '请上传合同文件', trigger: 'change' }]
};
const contractFileList = ref([]);

// 获取合同列表
const fetchContractsList = async () => {
  try {
    loading.value = true;
    
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      contractNo: searchForm.contractNo || undefined,
      farmerName: searchForm.farmerName || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.dateRange && searchForm.dateRange[0] || undefined,
      endDate: searchForm.dateRange && searchForm.dateRange[1] || undefined,
      sortBy: sortData.prop,
      sortOrder: sortData.order === 'ascending' ? 'asc' : 'desc'
    };
    
    const response = await purchaserAPI.getContractsList(params);
    
    if (response.code === 200) {
      contractsList.value = response.data.records || [];
      pagination.total = Number(response.data.total || 0);
    } else {
      ElMessage.error(response.message || '获取合同列表失败');
    }
  } catch (error) {
    console.error('获取合同列表失败:', error);
    ElMessage.error('获取合同列表失败');
  } finally {
    loading.value = false;
  }
};

// 获取对接记录列表
const fetchDockingsList = async () => {
  try {
    dockingsLoading.value = true;
    
    const params = {
      pageNum: dockingsPagination.currentPage,
      pageSize: dockingsPagination.pageSize,
      status: 'agreed' // 只获取已同意的对接记录
    };
    
    const response = await purchaserAPI.getDockingsList(params);
    
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

// 刷新对接记录列表
const refreshDockingsList = () => {
  dockingsPagination.currentPage = 1;
  fetchDockingsList();
};

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchContractsList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.contractNo = '';
  searchForm.farmerName = '';
  searchForm.status = '';
  searchForm.dateRange = [];
  pagination.currentPage = 1;
  fetchContractsList();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  pagination.currentPage = 1;
  fetchContractsList();
};

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  fetchContractsList();
};

// 对接记录分页大小变化
const handleDockingsSizeChange = (size) => {
  dockingsPagination.pageSize = size;
  dockingsPagination.currentPage = 1;
  fetchDockingsList();
};

// 对接记录当前页变化
const handleDockingsCurrentChange = (page) => {
  dockingsPagination.currentPage = page;
  fetchDockingsList();
};

// 排序变化
const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop;
  sortData.order = order;
  fetchContractsList();
};

// 查看详情
const viewDetail = (id) => {
  router.push(`/purchaser/contracts/${id}`);
};

// 签署合同
const signContract = (row) => {
  signContractForm.id = row.id;
  signContractForm.contractNo = row.contractNo;
  signContractForm.signFile = null;
  fileList.value = [];
  signContractDialogVisible.value = true;
};

// 处理文件变化
const handleFileChange = (file) => {
  signContractForm.signFile = file.raw;
};

// 提交签署合同
const submitSignContract = async () => {
  if (!signContractFormRef.value) return;
  
  try {
    await signContractFormRef.value.validate();
    
    const response = await purchaserAPI.signContract(
      signContractForm.id,
      signContractForm.signFile
    );
    
    if (response.code === 200) {
      ElMessage.success('合同签署成功');
      signContractDialogVisible.value = false;
      fetchContractsList(); // 重新获取列表
    } else {
      ElMessage.error(response.message || '签署合同失败');
    }
  } catch (error) {
    console.error('签署合同失败:', error);
    ElMessage.error('签署合同失败');
  }
};

// 下载合同
const downloadContract = (row) => {
  if (row.contractFileUrl) {
    window.open(row.contractFileUrl, '_blank');
  } else {
    ElMessage.warning('合同文件不存在');
  }
};

// 显示创建合同对话框
const showCreateContractDialog = (row) => {
  createContractForm.dockingId = row.id;
  createContractForm.farmerName = row.farmName; // 使用farmName作为农户名称
  createContractForm.farmName = row.farmName;
  createContractForm.productName = row.productId; // 使用productId作为产品名称
  createContractForm.productId = row.productId;
  createContractForm.canSupply = row.canSupply;
  createContractForm.quantity = Math.min(1, row.canSupply); // 默认数量为1，但不能超过可供应量
  createContractForm.paymentTerms = '';
  createContractForm.deliveryTime = '';
  createContractForm.deliveryAddress = '';
  createContractForm.qualityStandards = '';
  createContractForm.breachTerms = '';
  createContractForm.signFile = null;
  contractFileList.value = [];
  createContractDialogVisible.value = true;
};

// 处理合同文件变化
const handleContractFileChange = (file) => {
  createContractForm.signFile = file.raw;
};

// 提交创建合同
const submitCreateContract = async () => {
  if (!createContractFormRef.value) return;
  
  try {
    await createContractFormRef.value.validate();
    
    const response = await purchaserAPI.createContract(createContractForm);
    
    if (response.code === 200) {
      ElMessage.success('合同创建成功');
      createContractDialogVisible.value = false;
      fetchContractsList(); // 重新获取合同列表
      fetchDockingsList(); // 重新获取对接记录列表
    } else {
      ElMessage.error(response.message || '创建合同失败');
    }
  } catch (error) {
    console.error('创建合同失败:', error);
    ElMessage.error('创建合同失败');
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'DRAFT': return 'info';
    case 'PENDING_SIGN': return 'warning';
    case 'SIGNED': return 'success';
    case 'TERMINATED': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'DRAFT': return '草稿';
    case 'PENDING_SIGN': return '待签署';
    case 'SIGNED': return '已签署';
    case 'TERMINATED': return '已终止';
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
  fetchContractsList();
  fetchDockingsList();
});
</script>

<style scoped>
.contracts-page {
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

.sign-status {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sign-item {
  display: flex;
  align-items: center;
  font-size: 12px;
}

.sign-item span {
  margin-right: 4px;
  white-space: nowrap;
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