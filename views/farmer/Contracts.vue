<template>
  <div class="contracts-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/farmer' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>合同管理</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="page-header">
      <h2>合同管理</h2>
      <p>查看和管理您的销售合同</p>
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
        <el-form-item label="采购方名称">
          <el-input 
            v-model="searchForm.purchaserName" 
            placeholder="请输入采购方名称" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="合同状态">
          <el-select v-model="searchForm.status" placeholder="请选择合同状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="草稿" value="draft" />
            <el-option label="已签署" value="signed" />
            <el-option label="执行中" value="executing" />
            <el-option label="已完成" value="completed" />
            <el-option label="已终止" value="terminated" />
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
        <el-table-column prop="dockingId" label="关联对接" width="150" />
        <el-table-column prop="productInfo.name" label="产品名称" min-width="150" show-overflow-tooltip />
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
                <el-tag :type="scope.row.farmerSignUrl ? 'success' : 'info'" size="small">
                  {{ scope.row.farmerSignUrl ? '已签署' : '未签署' }}
                </el-tag>
              </div>
              <div class="sign-item">
                <span>采购方: </span>
                <el-tag :type="scope.row.purchaserSignUrl ? 'success' : 'info'" size="small">
                  {{ scope.row.purchaserSignUrl ? '已签署' : '未签署' }}
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
              v-if="scope.row.status === 'pending_sign' && !scope.row.farmerSignUrl"
              type="success" 
              link 
              @click="signContract(scope.row)"
            >
              签署合同
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending_sign' && !scope.row.farmerSignUrl"
              type="danger" 
              link 
              @click="rejectContract(scope.row)"
            >
              拒签合同
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
    
    <!-- 拒签合同对话框 -->
    <el-dialog 
      v-model="rejectContractDialogVisible" 
      title="拒签合同" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectContractForm" :rules="rejectContractRules" ref="rejectContractFormRef" label-width="100px">
        <el-form-item label="合同编号">
          <el-input v-model="rejectContractForm.contractNo" disabled />
        </el-form-item>
        <el-form-item label="拒签原因" prop="reason">
          <el-input 
            v-model="rejectContractForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请输入拒签原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectContractDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitRejectContract">确认拒签</el-button>
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
import { farmerAPI } from '/api/index';

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  contractNo: '',
  purchaserName: '',
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

// 拒签合同对话框
const rejectContractDialogVisible = ref(false);
const rejectContractFormRef = ref(null);
const rejectContractForm = reactive({
  id: '',
  contractNo: '',
  reason: ''
});
const rejectContractRules = {
  reason: [{ required: true, message: '请输入拒签原因', trigger: 'blur' }]
};

// 获取合同列表
const fetchContractsList = async () => {
  try {
    loading.value = true;
    
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      contractNo: searchForm.contractNo || undefined,
      purchaserName: searchForm.purchaserName || undefined,
      status: searchForm.status || undefined,
      startDate: searchForm.dateRange && searchForm.dateRange[0] || undefined,
      endDate: searchForm.dateRange && searchForm.dateRange[1] || undefined,
      sortBy: sortData.prop,
      sortOrder: sortData.order === 'ascending' ? 'asc' : 'desc'
    };
    
    const response = await farmerAPI.getContractsList(params);
    
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

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchContractsList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.contractNo = '';
  searchForm.purchaserName = '';
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

// 排序变化
const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop;
  sortData.order = order;
  fetchContractsList();
};

// 查看详情
const viewDetail = (id) => {
  router.push(`/farmer/contracts/${id}`);
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
    
    const response = await farmerAPI.signContract(
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

// 拒签合同
const rejectContract = (row) => {
  rejectContractForm.id = row.id;
  rejectContractForm.contractNo = row.contractNo;
  rejectContractForm.reason = '';
  rejectContractDialogVisible.value = true;
};

// 提交拒签合同
const submitRejectContract = async () => {
  if (!rejectContractFormRef.value) return;
  
  try {
    await rejectContractFormRef.value.validate();
    
    const response = await farmerAPI.rejectContract(
      rejectContractForm.id,
      rejectContractForm.reason
    );
    
    if (response.code === 200) {
      ElMessage.success('合同拒签成功');
      rejectContractDialogVisible.value = false;
      fetchContractsList(); // 重新获取列表
    } else {
      ElMessage.error(response.message || '拒签合同失败');
    }
  } catch (error) {
    console.error('拒签合同失败:', error);
    ElMessage.error('拒签合同失败');
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

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'draft': return 'info';
    case 'signed': return 'success';
    case 'executing': return 'primary';
    case 'completed': return 'success';
    case 'terminated': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'draft': return '草稿';
    case 'signed': return '已签署';
    case 'executing': return '执行中';
    case 'completed': return '已完成';
    case 'terminated': return '已终止';
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