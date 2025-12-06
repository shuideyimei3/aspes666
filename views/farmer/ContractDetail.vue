<template>
  <div class="contract-detail-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/farmer' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/farmer/contracts' }">合同管理</el-breadcrumb-item>
      <el-breadcrumb-item>合同详情</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="page-header">
      <h2>合同详情</h2>
      <p>查看合同详细信息</p>
    </div>
    
    <el-card v-loading="loading" class="contract-card">
      <template #header>
        <div class="card-header">
          <span>合同信息</span>
          <div class="header-actions">
            <el-button 
              v-if="contractDetail.status === 'draft' && !contractDetail.farmerSignUrl"
              type="success" 
              @click="signContract"
            >
              签署合同
            </el-button>
            <el-button 
              v-if="contractDetail.status === 'draft' && !contractDetail.farmerSignUrl"
              type="danger" 
              @click="rejectContract"
            >
              拒签合同
            </el-button>
            <el-button 
              v-if="contractDetail.contractFileUrl"
              type="info" 
              @click="downloadContract"
            >
              下载合同
            </el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同编号">{{ contractDetail.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="关联对接">{{ contractDetail.dockingId }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ contractDetail.productInfo?.name }}</el-descriptions-item>
        <el-descriptions-item label="产品规格">{{ contractDetail.productInfo?.spec }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">¥{{ contractDetail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ contractDetail.paymentTerms }}</el-descriptions-item>
        <el-descriptions-item label="交货时间">{{ contractDetail.deliveryTime }}</el-descriptions-item>
        <el-descriptions-item label="交货地址">{{ contractDetail.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="质量标准" :span="2">{{ contractDetail.qualityStandards }}</el-descriptions-item>
        <el-descriptions-item label="违约条款" :span="2">{{ contractDetail.breachTerms }}</el-descriptions-item>
        <el-descriptions-item label="合同状态">
          <el-tag :type="getStatusType(contractDetail.status)">
            {{ getStatusText(contractDetail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(contractDetail.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="农户签署状态">
          <el-tag :type="contractDetail.farmerSignUrl ? 'success' : 'info'">
            {{ contractDetail.farmerSignUrl ? '已签署' : '未签署' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="采购方签署状态">
          <el-tag :type="contractDetail.purchaserSignUrl ? 'success' : 'info'">
            {{ contractDetail.purchaserSignUrl ? '已签署' : '未签署' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 签名文件展示 -->
      <div class="signature-section" v-if="contractDetail.farmerSignUrl || contractDetail.purchaserSignUrl">
        <h3>签名文件</h3>
        <div class="signature-files">
          <div class="signature-file" v-if="contractDetail.farmerSignUrl">
            <h4>农户签名</h4>
            <el-image 
              v-if="isImageFile(contractDetail.farmerSignUrl)"
              :src="getFullImageUrl(contractDetail.farmerSignUrl)" 
              fit="contain"
              style="width: 200px; height: 200px;"
              :preview-src-list="[getFullImageUrl(contractDetail.farmerSignUrl)]"
            />
            <div v-else class="file-preview">
              <el-icon><Document /></el-icon>
              <span>签名文件</span>
              <el-button type="primary" link @click="openFile(getFullImageUrl(contractDetail.farmerSignUrl))">查看</el-button>
            </div>
          </div>
          
          <div class="signature-file" v-if="contractDetail.purchaserSignUrl">
            <h4>采购方签名</h4>
            <el-image 
              v-if="isImageFile(contractDetail.purchaserSignUrl)"
              :src="getFullImageUrl(contractDetail.purchaserSignUrl)" 
              fit="contain"
              style="width: 200px; height: 200px;"
              :preview-src-list="[getFullImageUrl(contractDetail.purchaserSignUrl)]"
            />
            <div v-else class="file-preview">
              <el-icon><Document /></el-icon>
              <span>签名文件</span>
              <el-button type="primary" link @click="openFile(getFullImageUrl(contractDetail.purchaserSignUrl))">查看</el-button>
            </div>
          </div>
        </div>
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
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Document } from '@element-plus/icons-vue';
import { farmerAPI } from '/api/index';

const router = useRouter();
const route = useRoute();

// 合同详情
const contractDetail = ref({});
const loading = ref(false);

// 签署合同对话框
const signContractDialogVisible = ref(false);
const signContractFormRef = ref(null);
const uploadRef = ref(null);
const signContractForm = ref({
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
const rejectContractForm = ref({
  id: '',
  contractNo: '',
  reason: ''
});
const rejectContractRules = {
  reason: [{ required: true, message: '请输入拒签原因', trigger: 'blur' }]
};

// 获取合同详情
const fetchContractDetail = async () => {
  try {
    loading.value = true;
    const contractId = route.params.id;
    
    const response = await farmerAPI.getContractDetail(contractId);
    
    if (response.code === 200) {
      contractDetail.value = response.data || {};
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

// 返回上一页
const goBack = () => {
  router.go(-1);
};

// 签署合同
const signContract = () => {
  signContractForm.value.id = contractDetail.value.id;
  signContractForm.value.contractNo = contractDetail.value.contractNo;
  signContractForm.value.signFile = null;
  fileList.value = [];
  signContractDialogVisible.value = true;
};

// 处理文件变化
const handleFileChange = (file) => {
  signContractForm.value.signFile = file.raw;
};

// 提交签署合同
const submitSignContract = async () => {
  if (!signContractFormRef.value) return;
  
  try {
    await signContractFormRef.value.validate();
    
    const response = await farmerAPI.signContract(
      signContractForm.value.id,
      signContractForm.value.signFile
    );
    
    if (response.code === 200) {
      ElMessage.success('合同签署成功');
      signContractDialogVisible.value = false;
      fetchContractDetail(); // 重新获取详情
    } else {
      ElMessage.error(response.message || '签署合同失败');
    }
  } catch (error) {
    console.error('签署合同失败:', error);
    ElMessage.error('签署合同失败');
  }
};

// 拒签合同
const rejectContract = () => {
  rejectContractForm.value.id = contractDetail.value.id;
  rejectContractForm.value.contractNo = contractDetail.value.contractNo;
  rejectContractForm.value.reason = '';
  rejectContractDialogVisible.value = true;
};

// 提交拒签合同
const submitRejectContract = async () => {
  if (!rejectContractFormRef.value) return;
  
  try {
    await rejectContractFormRef.value.validate();
    
    const response = await farmerAPI.rejectContract(
      rejectContractForm.value.id,
      rejectContractForm.value.reason
    );
    
    if (response.code === 200) {
      ElMessage.success('合同拒签成功');
      rejectContractDialogVisible.value = false;
      fetchContractDetail(); // 重新获取详情
    } else {
      ElMessage.error(response.message || '拒签合同失败');
    }
  } catch (error) {
    console.error('拒签合同失败:', error);
    ElMessage.error('拒签合同失败');
  }
};

// 下载合同
const downloadContract = () => {
  if (contractDetail.value.contractFileUrl) {
    window.open(getFullImageUrl(contractDetail.value.contractFileUrl), '_blank');
  } else {
    ElMessage.warning('合同文件不存在');
  }
};

// 获取完整图片URL
const getFullImageUrl = (url) => {
  if (!url) return '';
  // 如果URL已经是完整的http/https链接，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }
  // 否则添加https://aspes前缀
  return `https://aspes.${url}`;
};

// 判断是否为图片文件
const isImageFile = (url) => {
  if (!url) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
  const lowerUrl = url.toLowerCase();
  return imageExtensions.some(ext => lowerUrl.includes(ext));
};

// 打开文件
const openFile = (url) => {
  if (url) {
    window.open(url, '_blank');
  }
};

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'draft': 'info',
    'signed': 'success',
    'executing': 'primary',
    'completed': 'success',
    'terminated': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'draft': '草稿',
    'signed': '已签署',
    'executing': '执行中',
    'completed': '已完成',
    'terminated': '已终止'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchContractDetail();
});
</script>

<style scoped>
.contract-detail-page {
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

.contract-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.signature-section {
  margin-top: 20px;
}

.signature-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.signature-files {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.signature-file {
  text-align: center;
}

.signature-file h4 {
  margin-bottom: 10px;
  font-size: 16px;
  color: #606266;
}

.file-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  width: 200px;
  height: 200px;
  box-sizing: border-box;
}

.file-preview .el-icon {
  font-size: 40px;
  color: #909399;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-descriptions) {
  margin-bottom: 20px;
}

:deep(.el-descriptions__label) {
  font-weight: bold;
}
</style>