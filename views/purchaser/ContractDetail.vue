<template>
  <div class="contract-detail-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/purchaser/contracts' }">合同管理</el-breadcrumb-item>
      <el-breadcrumb-item>合同详情</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div v-loading="loading" class="detail-container">
      <!-- 合同基本信息 -->
      <el-card class="detail-card" v-if="contractDetail">
        <template #header>
          <div class="card-header">
            <span>合同信息</span>
            <el-tag :type="getStatusType(contractDetail.status)">
              {{ getStatusText(contractDetail.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同编号">{{ contractDetail.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(contractDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="农户名称">{{ contractDetail.farmName }}</el-descriptions-item>
          <el-descriptions-item label="农场名称">{{ contractDetail.farmName }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ contractDetail.productName }}</el-descriptions-item>
          <el-descriptions-item label="产品数量">{{ contractDetail.quantity }}</el-descriptions-item>
          <el-descriptions-item label="单价">{{ contractDetail.unitPrice }}元</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ contractDetail.totalAmount }}元</el-descriptions-item>
          <el-descriptions-item label="付款方式">{{ contractDetail.paymentTerms }}</el-descriptions-item>
          <el-descriptions-item label="交付时间">{{ contractDetail.deliveryTime }}</el-descriptions-item>
          <el-descriptions-item label="交付地址" :span="2">{{ contractDetail.deliveryAddress }}</el-descriptions-item>
          <el-descriptions-item label="质量标准" :span="2">{{ contractDetail.qualityStandards }}</el-descriptions-item>
          <el-descriptions-item label="违约条款" :span="2">{{ contractDetail.breachTerms }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 合同文件 -->
        <div class="contract-files" v-if="contractDetail.contractFileUrl || contractDetail.purchaserSignUrl || contractDetail.farmerSignUrl">
          <h4>合同文件</h4>
          <div class="file-list">
            <div class="file-item" v-if="contractDetail.contractFileUrl">
              <span>合同文件：</span>
              <el-link type="primary" @click="openFile(contractDetail.contractFileUrl)">查看合同文件</el-link>
            </div>
            <div class="file-item" v-if="contractDetail.purchaserSignUrl">
              <span>采购方签字：</span>
              <el-link type="primary" @click="openFile(contractDetail.purchaserSignUrl)">查看签字文件</el-link>
            </div>
            <div class="file-item" v-if="contractDetail.farmerSignUrl">
              <span>农户签字：</span>
              <el-link type="primary" @click="openFile(contractDetail.farmerSignUrl)">查看签字文件</el-link>
            </div>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="showSignDialog"
          >
            签署合同
          </el-button>
          <el-button 
            type="danger" 
            @click="showRejectDialog"
          >
            拒签合同
          </el-button>
          <el-button 
            type="danger" 
            @click="showTerminateDialog"
          >
            终止合同
          </el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 签署合同对话框 -->
    <el-dialog 
      v-model="signDialogVisible" 
      title="签署合同" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="signForm" :rules="signRules" ref="signFormRef" label-width="100px">
        <el-form-item label="合同编号">
          <el-input v-model="signForm.contractNo" disabled />
        </el-form-item>
        <el-form-item label="签字文件" prop="signFile">
          <el-upload
            class="upload-demo"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept="image/*,.pdf"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                请上传签字文件，支持jpg/png/pdf格式，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="signDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSign">确认签署</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 拒签合同对话框 -->
    <el-dialog 
      v-model="rejectDialogVisible" 
      title="拒签合同" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="拒签理由" prop="reason">
          <el-input 
            v-model="rejectForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入拒签理由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitReject">确认拒签</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 终止合同对话框 -->
    <el-dialog 
      v-model="terminateDialogVisible" 
      title="终止合同" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="terminateForm" :rules="terminateRules" ref="terminateFormRef" label-width="100px">
        <el-form-item label="终止理由" prop="reason">
          <el-input 
            v-model="terminateForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入终止合同的理由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="terminateDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="submitTerminate">确认终止</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';
import { purchaserAPI } from '/api/index';

const route = useRoute();

// 合同详情
const contractDetail = ref(null);
const loading = ref(false);

// 签署合同对话框
const signDialogVisible = ref(false);
const signFormRef = ref(null);
const signForm = reactive({
  contractNo: '',
  signFile: null
});
const signRules = {
  signFile: [
    { required: true, message: '请上传签字文件', trigger: 'change' }
  ]
};

// 拒签合同对话框
const rejectDialogVisible = ref(false);
const rejectFormRef = ref(null);
const rejectForm = reactive({
  reason: ''
});
const rejectRules = {
  reason: [
    { required: true, message: '请输入拒签理由', trigger: 'blur' },
    { min: 5, max: 500, message: '拒签理由长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 终止合同对话框
const terminateDialogVisible = ref(false);
const terminateFormRef = ref(null);
const terminateForm = reactive({
  reason: ''
});
const terminateRules = {
  reason: [
    { required: true, message: '请输入终止合同的理由', trigger: 'blur' },
    { min: 5, max: 500, message: '终止理由长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 获取合同详情
const fetchContractDetail = async () => {
  try {
    loading.value = true;
    const contractId = route.params.id;
    
    const response = await purchaserAPI.getContractDetail(contractId);
    
    if (response.code === 200) {
      contractDetail.value = response.data;
    } else {
      ElMessage.error(response.message || '获取合同详情失败');
    }
  } catch (error) {
    console.error('获取合同详情失败:', error);
    ElMessage.error('获取合同详情失败');
  } finally {
    loading.value = false;
  }
};

// 显示签署合同对话框
const showSignDialog = () => {
  signForm.contractNo = contractDetail.value.contractNo;
  signForm.signFile = null;
  signDialogVisible.value = true;
};

// 显示拒签合同对话框
const showRejectDialog = () => {
  rejectForm.reason = '';
  rejectDialogVisible.value = true;
};

// 显示终止合同对话框
const showTerminateDialog = () => {
  terminateForm.reason = '';
  terminateDialogVisible.value = true;
};

// 处理文件变化
const handleFileChange = (file) => {
  signForm.signFile = file.raw;
};

// 提交签署合同
const submitSign = async () => {
  if (!signFormRef.value) return;
  
  try {
    await signFormRef.value.validate();
    
    const response = await purchaserAPI.signContract(
      contractDetail.value.id,
      signForm.signFile
    );
    
    if (response.code === 200) {
      ElMessage.success('合同签署成功');
      signDialogVisible.value = false;
      fetchContractDetail(); // 重新获取详情
    } else {
      ElMessage.error(response.message || '签署合同失败');
    }
  } catch (error) {
    console.error('签署合同失败:', error);
    ElMessage.error('签署合同失败');
  }
};

// 提交拒签合同
const submitReject = async () => {
  if (!rejectFormRef.value) return;
  
  try {
    await rejectFormRef.value.validate();
    
    await ElMessageBox.confirm('确认拒签此合同吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await purchaserAPI.rejectContract(
      contractDetail.value.id,
      rejectForm.reason
    );
    
    if (response.code === 200) {
      ElMessage.success('合同拒签成功');
      rejectDialogVisible.value = false;
      fetchContractDetail(); // 重新获取详情
    } else {
      ElMessage.error(response.message || '拒签合同失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒签合同失败:', error);
      ElMessage.error('拒签合同失败');
    }
  }
};

// 提交终止合同
const submitTerminate = async () => {
  if (!terminateFormRef.value) return;
  
  try {
    await terminateFormRef.value.validate();
    
    await ElMessageBox.confirm('确认终止此合同吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await purchaserAPI.terminateContract(
      contractDetail.value.id,
      terminateForm.reason
    );
    
    if (response.code === 200) {
      ElMessage.success('合同终止成功');
      terminateDialogVisible.value = false;
      fetchContractDetail(); // 重新获取详情
    } else {
      ElMessage.error(response.message || '终止合同失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('终止合同失败:', error);
      ElMessage.error('终止合同失败');
    }
  }
};

// 打开文件
const openFile = (url) => {
  window.open(url, '_blank');
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
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

// 页面加载时获取合同详情
onMounted(() => {
  fetchContractDetail();
});
</script>

<style scoped>
.contract-detail-page {
  padding: 20px;
}

.detail-container {
  margin-top: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.contract-files {
  margin-top: 20px;
}

.contract-files h4 {
  margin-bottom: 10px;
  color: #606266;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.upload-demo {
  width: 100%;
}
</style>