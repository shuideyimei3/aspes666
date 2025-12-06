<template>
  <div class="farmer-info-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>农户信息</span>
          <el-button v-if="!isEditing && farmerInfo" type="primary" @click="isEditing = true">
            编辑信息
          </el-button>
        </div>
      </template>

      <!-- 显示模式 -->
      <div v-if="!isEditing && farmerInfo" class="info-display">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="农场名称">{{ farmerInfo.farmName }}</el-descriptions-item>
          <el-descriptions-item label="产地">{{ farmerInfo.originAreaName }}</el-descriptions-item>
          <el-descriptions-item label="生产规模">{{ farmerInfo.productionScale || '-' }}</el-descriptions-item>
          <el-descriptions-item label="认证状态">
            <el-tag :type="farmerInfo.isCertified ? 'success' : 'warning'">
              {{ farmerInfo.isCertified ? '已认证' : '未认证' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="银行账号">{{ farmerInfo.bankAccount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开户行">{{ farmerInfo.bankName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ farmerInfo.idNumber }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ farmerInfo.createTime }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 身份证照片展示 -->
        <div class="id-card-images" v-if="farmerInfo.idCardFrontUrl || farmerInfo.idCardBackUrl">
          <h3>身份证照片</h3>
          <div class="image-container">
            <div class="image-item" v-if="farmerInfo.idCardFrontUrl">
              <p>身份证正面：</p>
              <el-image 
                :src="processedIdCardFrontUrl" 
                :preview-src-list="[processedIdCardFrontUrl]"
                fit="cover"
                style="width: 300px; height: 200px;"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="image-item" v-if="farmerInfo.idCardBackUrl">
              <p>身份证反面：</p>
              <el-image 
                :src="processedIdCardBackUrl" 
                :preview-src-list="[processedIdCardBackUrl]"
                fit="cover"
                style="width: 300px; height: 200px;"
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
        </div>
      </div>

      <!-- 编辑/新建模式 -->
      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="农场名称" prop="farmName">
          <el-input v-model="form.farmName" placeholder="请输入农场名称" />
        </el-form-item>

        <el-form-item label="产地" prop="originAreaId">
          <el-select v-model="form.originAreaId" placeholder="请选择产地" style="width: 100%">
            <el-option
              v-for="area in originAreas"
              :key="area.id"
              :label="area.areaName"
              :value="area.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="生产规模" prop="productionScale">
          <el-input v-model="form.productionScale" type="textarea" placeholder="请描述生产规模" />
        </el-form-item>

        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="form.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>

        <el-form-item label="开户行" prop="bankName">
          <el-input v-model="form.bankName" placeholder="请输入开户行名称" />
        </el-form-item>

        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="form.idNumber" placeholder="请输入身份证号" />
        </el-form-item>

        <el-form-item label="身份证正面" prop="idCardFrontFile" v-if="!farmerInfo">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept="image/*"
            :on-change="handleIdCardFrontChange"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="身份证反面" prop="idCardBackFile" v-if="!farmerInfo">
          <el-upload
            :auto-upload="false"
            :limit="1"
            accept="image/*"
            :on-change="handleIdCardBackChange"
          >
            <el-button type="primary">选择文件</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="申请理由" prop="applyReason" v-if="!farmerInfo">
          <el-input v-model="form.applyReason" type="textarea" placeholder="请说明申请理由" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ farmerInfo ? '更新信息' : '提交认证' }}
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
import { farmerAPI, originAreaAPI } from '../../api';
import { bigIntToString, processResponseData, safeJsonParse, processBackendResponse } from '../../utils/bigint';

const loading = ref(false);
const submitting = ref(false);
const isEditing = ref(false);
const farmerInfo = ref(null);
const formRef = ref(null);
const originAreas = ref([]); // 产地列表

const form = ref({
  farmName: '',
  originAreaId: null,
  productionScale: '',
  bankAccount: '',
  bankName: '',
  idNumber: '',
  idCardFrontFile: null,
  idCardBackFile: null,
  applyReason: '',
});

const rules = {
  farmName: [{ required: true, message: '请输入农场名称', trigger: 'blur' }],
  originAreaId: [{ required: true, message: '请选择产地', trigger: 'change' }],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '身份证号格式不正确', trigger: 'blur' },
  ],
  bankAccount: [{ required: false, message: '请输入银行账号', trigger: 'blur' }],
  bankName: [{ required: false, message: '请输入开户行', trigger: 'blur' }],
  productionScale: [{ required: false, message: '请输入生产规模', trigger: 'blur' }],
  applyReason: [{ required: true, message: '请输入申请理由', trigger: 'blur' }],
};

// 处理图片URL的计算属性
const processedIdCardFrontUrl = computed(() => {
  if (!farmerInfo.value?.idCardFrontUrl) return '';
  if (farmerInfo.value.idCardFrontUrl.startsWith('http')) {
    return farmerInfo.value.idCardFrontUrl;
  }
  return `https://aspes.${farmerInfo.value.idCardFrontUrl}`;
});

const processedIdCardBackUrl = computed(() => {
  if (!farmerInfo.value?.idCardBackUrl) return '';
  if (farmerInfo.value.idCardBackUrl.startsWith('http')) {
    return farmerInfo.value.idCardBackUrl;
  }
  return `https://aspes.${farmerInfo.value.idCardBackUrl}`;
});

// 加载产地列表
const loadOriginAreas = async () => {
  try {
    console.log('Loading origin areas from backend...');
    const response = await originAreaAPI.getPage(1, 100);
    console.log('Origin area API response:', response);
    
    if (!response || !response.data || !response.data.records) {
      throw new Error('Invalid response data');
    }
    
    // 使用安全JSON解析器处理响应数据，确保大整数正确处理
    let processedData = response.data;
    
    // 尝试使用安全JSON解析器重新处理数据
    try {
      const jsonString = JSON.stringify(response.data);
      processedData = safeJsonParse(jsonString);
      console.log('Safe JSON parsed origin areas:', processedData);
    } catch (error) {
      console.warn('安全JSON解析失败，使用原始数据:', error);
    }
    
    // 再次处理确保所有ID字段正确转换
    processedData = processResponseData(processedData);
    
    originAreas.value = processedData.records
      .filter(area => area.areaId != null) // 过滤掉无效数据
      .map(area => ({
        id: bigIntToString(area.areaId),  // 确保ID转换为字符串
        areaName: area.areaName || `${area.province || ''}${area.city || ''}`,
      }));
    
    console.log('Loaded origin areas successfully:', originAreas.value);
    
    if (originAreas.value.length === 0) {
      console.warn('No origin areas found in backend, using defaults');
      useDefaultOriginAreas();
    }
  } catch (error) {
    console.error('Load origin areas error:', error);
    console.log('Falling back to default origin areas');
    useDefaultOriginAreas();
  }
};

// 使用默认产地
const useDefaultOriginAreas = () => {
  originAreas.value = [
    { id: '1', areaName: '北京' },
    { id: '2', areaName: '上海' },
    { id: '3', areaName: '广州' },
    { id: '4', areaName: '深圳' },
    { id: '5', areaName: '杭州' },
  ];
  console.log('Using default origin areas:', originAreas.value);
};

const loadFarmerInfo = async () => {
  try {
    loading.value = true;
    const response = await farmerAPI.getMyInfo();
    farmerInfo.value = response.data;
    
    // 填充表单
    if (farmerInfo.value) {
      form.value = {
        farmName: farmerInfo.value.farmName,
        originAreaId: farmerInfo.value.originAreaId,
        productionScale: farmerInfo.value.productionScale,
        bankAccount: farmerInfo.value.bankAccount,
        bankName: farmerInfo.value.bankName,
        idNumber: farmerInfo.value.idNumber,
        idCardFrontFile: null,
        idCardBackFile: null,
        applyReason: '',
      };
    } else {
      // 没有信息，进入编辑模式
      isEditing.value = true;
    }
  } catch (error) {
    // 如果没有找到信息，进入新建模式
    isEditing.value = true;
    console.error('Load farmer info error:', error);
  } finally {
    loading.value = false;
  }
};

const handleIdCardFrontChange = (file) => {
  form.value.idCardFrontFile = file.raw;
};

const handleIdCardBackChange = (file) => {
  form.value.idCardBackFile = file.raw;
};

const handleSubmit = async () => {
  try {
    // 表单验证
    const valid = await formRef.value.validate().catch((error) => {
      console.error('Form validation error:', error);
      return false;
    });
    
    if (!valid) {
      ElMessage.error('请检查表单填写是否正确');
      return;
    }
    
    submitting.value = true;

    if (farmerInfo.value) {
      // 更新信息（这里简化处理，实际应该有专门的更新接口）
      ElMessage.warning('更新功能需要后端提供对应接口');
    } else {
      // 提交新信息
      await farmerAPI.submitInfo(form.value);
      ElMessage.success('提交成功，等待审核');
      loadFarmerInfo();
      isEditing.value = false;
    }
  } catch (error) {
    console.error('Submit error:', error);
    ElMessage.error(error.message || '提交失败，请重试');
  } finally {
    submitting.value = false;
  }
};

const handleCancel = () => {
  if (farmerInfo.value) {
    isEditing.value = false;
    loadFarmerInfo();
  } else {
    // 重置表单
    formRef.value.resetFields();
  }
};

onMounted(async () => {
  await loadFarmerInfo();
  await loadOriginAreas(); // 加载产地列表
});
</script>

<style scoped>
.farmer-info-page {
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

.id-card-images {
  margin-top: 20px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.id-card-images h3 {
  margin-bottom: 15px;
  color: #333;
}

.image-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.image-item {
  flex: 1;
  min-width: 300px;
}

.image-item p {
  margin-bottom: 10px;
  font-weight: bold;
  color: #555;
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
