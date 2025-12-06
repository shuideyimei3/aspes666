<template>
  <div class="edit-demand-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>编辑采购需求</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入需要采购的产品名称" />
        </el-form-item>

        <el-form-item label="产品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option label="蔬菜" value="1" />
            <el-option label="水果" value="2" />
            <el-option label="粮食" value="3" />
            <el-option label="畜牧" value="4" />
            <el-option label="水产" value="5" />
          </el-select>
        </el-form-item>

        <el-form-item label="需求描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述采购需求、质量要求等"
          />
        </el-form-item>

        <el-form-item label="预期价格" prop="expectedPrice">
          <el-input-number
            v-model="form.expectedPrice"
            :precision="2"
            :min="0"
            :max="999999"
            style="width: 200px"
          />
          <span style="margin-left: 10px">元/{{ form.unit || '单位' }}</span>
        </el-form-item>

        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：斤、公斤、箱等" style="width: 200px" />
        </el-form-item>

        <el-form-item label="需求数量" prop="quantity">
          <el-input-number
            v-model="form.quantity"
            :min="1"
            :max="999999"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="期望产地" prop="preferredOriginAreaId">
          <el-select v-model="form.preferredOriginAreaId" placeholder="请选择期望产地" clearable style="width: 100%">
            <el-option label="北京市" value="1" />
            <el-option label="上海市" value="2" />
            <el-option label="广东省" value="3" />
            <el-option label="山东省" value="4" />
            <el-option label="河南省" value="5" />
          </el-select>
        </el-form-item>

        <el-form-item label="质量要求" prop="qualityRequirement">
          <el-input
            v-model="form.qualityRequirement"
            type="textarea"
            :rows="3"
            placeholder="如：需要绿色食品认证、有机产品等"
          />
        </el-form-item>

        <el-form-item label="交货时间" prop="deliveryTime">
          <el-date-picker
            v-model="form.deliveryTime"
            type="date"
            placeholder="选择期望交货时间"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="有效期" prop="validUntil">
          <el-date-picker
            v-model="form.validUntil"
            type="date"
            placeholder="选择需求有效期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            保存修改
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { purchaserAPI } from '../../api';
import { bigIntToString, processResponseData } from '../../utils/bigint';

const router = useRouter();
const route = useRoute();
const formRef = ref(null);
const loading = ref(false);
const submitting = ref(false);
const demandId = route.params.id;

const form = ref({
  productName: '',
  categoryId: null,
  description: '',
  expectedPrice: null,
  unit: '',
  quantity: null,
  preferredOriginAreaId: null,
  qualityRequirement: '',
  deliveryTime: '',
  validUntil: '',
});

// 初始化表单数据
const initializeForm = () => {
  form.value = {
    productName: '',
    categoryId: null,
    description: '',
    expectedPrice: null,
    unit: '',
    quantity: null,
    preferredOriginAreaId: null,
    qualityRequirement: '',
    deliveryTime: '',
    validUntil: '',
  };
};

const rules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入需求描述', trigger: 'blur' }],
  expectedPrice: [{ required: true, message: '请输入预期价格', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入需求数量', trigger: 'blur' }],
  deliveryTime: [{ required: true, message: '请选择交货时间', trigger: 'change' }],
  validUntil: [{ required: true, message: '请选择有效期', trigger: 'change' }],
};

const loadDemand = async () => {
  try {
    loading.value = true;
    const response = await purchaserAPI.getDemandDetail(demandId);
    // 响应数据已经在拦截器中处理过，但为了确保安全，再次处理
    const demand = processResponseData(response.data);
    
    form.value = {
      productName: demand.productName,
      categoryId: bigIntToString(demand.categoryId), // 确保categoryId转换为字符串
      description: demand.description,
      expectedPrice: demand.expectedPrice,
      unit: demand.unit,
      quantity: demand.quantity,
      preferredOriginAreaId: bigIntToString(demand.preferredOriginAreaId), // 确保preferredOriginAreaId转换为字符串
      qualityRequirement: demand.qualityRequirement || '',
      deliveryTime: demand.deliveryTime,
      validUntil: demand.validUntil,
    };
  } catch (error) {
    console.error('Load demand error:', error);
    ElMessage.error('加载需求信息失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    // 确保ID字段为字符串类型
    const submitData = {
      ...form.value,
      categoryId: bigIntToString(form.value.categoryId),
      preferredOriginAreaId: bigIntToString(form.value.preferredOriginAreaId),
    };

    await purchaserAPI.updateDemand(demandId, submitData);
    ElMessage.success('修改成功');
    router.push('/purchaser/demands');
  } catch (error) {
    console.error('Update demand error:', error);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  initializeForm();
  loadDemand();
});
</script>

<style scoped>
.edit-demand-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}
</style>
