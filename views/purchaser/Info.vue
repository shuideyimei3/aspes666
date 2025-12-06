<template>
  <div class="purchaser-info-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>采购方信息</span>
          <el-button v-if="!isEditing && purchaserInfo" type="primary" @click="isEditing = true">
            编辑信息
          </el-button>
        </div>
      </template>

      <!-- 显示模式 -->
      <div v-if="!isEditing && purchaserInfo" class="info-display">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="公司名称">{{ purchaserInfo.companyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="公司类型">{{ purchaserInfo.companyType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="法人代表">{{ purchaserInfo.legalRepresentative || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购规模">{{ purchaserInfo.purchaseScale || '-' }}</el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag :type="getAuthStatusType(purchaserInfo.auditStatus)">
              {{ getAuthStatusText(purchaserInfo.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(purchaserInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">{{ purchaserInfo.applyReason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核备注" :span="2">{{ purchaserInfo.auditRemark || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 营业执照展示 -->
        <div class="business-license-image" v-if="purchaserInfo.businessLicenseUrl">
          <h3>营业执照</h3>
          <el-image 
            :src="processedBusinessLicenseUrl" 
            :preview-src-list="[processedBusinessLicenseUrl]"
            fit="contain"
            style="width: 400px; height: 300px;"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
      </div>

      <!-- 编辑/新建模式 -->
      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="150px"
      >
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>

        <el-form-item label="公司类型" prop="companyType">
          <el-input v-model="form.companyType" placeholder="请输入公司类型" />
        </el-form-item>

        <el-form-item label="法人代表" prop="legalRepresentative">
          <el-input v-model="form.legalRepresentative" placeholder="请输入法人代表姓名" />
        </el-form-item>

        <el-form-item label="采购规模" prop="purchaseScale">
          <el-input v-model="form.purchaseScale" placeholder="请输入采购规模" />
        </el-form-item>

        <el-form-item label="申请理由" prop="applyReason">
          <el-input v-model="form.applyReason" type="textarea" placeholder="请输入申请理由" />
        </el-form-item>

        <el-form-item label="营业执照" prop="businessLicenseFile" v-if="!purchaserInfo">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept="image/*"
            :on-change="handleBusinessLicenseChange"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ purchaserInfo ? '更新信息' : '提交认证' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';
import { purchaserAPI } from '../../api';

const loading = ref(false);
const submitting = ref(false);
const isEditing = ref(false);
const purchaserInfo = ref(null);
const formRef = ref(null);

const form = ref({
  companyName: '',
  companyType: '',
  legalRepresentative: '',
  purchaseScale: '',
  applyReason: '',
  businessLicenseFile: null,
});

const rules = {
  companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  companyType: [{ required: true, message: '请输入公司类型', trigger: 'blur' }],
  legalRepresentative: [{ required: true, message: '请输入法人代表', trigger: 'blur' }],
  purchaseScale: [{ required: true, message: '请输入采购规模', trigger: 'blur' }],
  applyReason: [{ required: true, message: '请输入申请理由', trigger: 'blur' }],
};

// 处理营业执照URL的计算属性
const processedBusinessLicenseUrl = computed(() => {
  if (!purchaserInfo.value?.businessLicenseUrl) return '';
  if (purchaserInfo.value.businessLicenseUrl.startsWith('http')) {
    return purchaserInfo.value.businessLicenseUrl;
  }
  return `https://aspes.${purchaserInfo.value.businessLicenseUrl}`;
});

// 认证状态类型映射
const getAuthStatusType = (status) => {
  if (!status) return 'info';
  // status 是 AuditStatus 枚举，需要转换为对应的类型
  const statusStr = typeof status === 'string' ? status : status.toString();
  const typeMap = {
    'PENDING': 'warning',  // 待审核
    'APPROVED': 'success',  // 已认证
    'REJECTED': 'danger',   // 审核拒绝
  };
  return typeMap[statusStr] || 'info';
};

// 认证状态文本映射
const getAuthStatusText = (status) => {
  if (!status) return '未知';
  // status 是 AuditStatus 枚举，需要转换为对应的文本
  const statusStr = typeof status === 'string' ? status : status.toString();
  const textMap = {
    'pending': '待审核',
    'approved': '已认证',
    'rejected': '审核拒绝',
  };
  return textMap[statusStr] || '未知';
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

const loadPurchaserInfo = async () => {
  try {
    loading.value = true;
    const response = await purchaserAPI.getMyInfo();
    purchaserInfo.value = response.data;
    
    if (purchaserInfo.value) {
      form.value = {
        companyName: purchaserInfo.value.companyName,
        companyType: purchaserInfo.value.companyType,
        legalRepresentative: purchaserInfo.value.legalRepresentative,
        purchaseScale: purchaserInfo.value.purchaseScale,
        applyReason: purchaserInfo.value.applyReason,
        businessLicenseFile: null,
      };
    } else {
      isEditing.value = true;
    }
  } catch (error) {
    isEditing.value = true;
    console.error('Load purchaser info error:', error);
  } finally {
    loading.value = false;
  }
};

const handleBusinessLicenseChange = (file) => {
  form.value.businessLicenseFile = file.raw;
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    if (purchaserInfo.value) {
      ElMessage.warning('更新功能需要后端提供对应接口');
    } else {
      await purchaserAPI.submitInfo(form.value);
      ElMessage.success('提交成功，等待审核');
      loadPurchaserInfo();
      isEditing.value = false;
    }
  } catch (error) {
    console.error('Submit error:', error);
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  if (purchaserInfo.value) {
    isEditing.value = false;
    loadPurchaserInfo();
  } else {
    formRef.value.resetFields();
  }
};

onMounted(() => {
  loadPurchaserInfo();
});
</script>

<style scoped>
.purchaser-info-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-display {
  padding: 20px;
}

.business-license-image {
  margin-top: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  text-align: center;
}

.business-license-image h3 {
  margin-bottom: 15px;
  color: #333;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

:deep(.el-form) {
  max-width: 800px;
}
</style>
