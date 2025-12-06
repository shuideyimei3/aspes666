<template>
  <div class="publish-demand-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布采购需求</span>
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
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
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

        <el-form-item label="价格范围" prop="priceRange">
          <el-input
            v-model="form.priceRange"
            placeholder="请输入价格范围，如：100-200元/公斤"
            style="width: 300px"
          />
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

        <el-form-item label="规格要求" prop="specRequire">
          <el-input
            v-model="form.specRequire"
            placeholder="请输入规格要求，如：大小、重量等"
            style="width: 300px"
          />
        </el-form-item>

        <el-form-item label="质量要求" prop="qualityRequire">
          <el-input
            v-model="form.qualityRequire"
            type="textarea"
            :rows="3"
            placeholder="请输入质量要求"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="交货日期" prop="deliveryDate">
          <el-date-picker
            v-model="form.deliveryDate"
            type="date"
            placeholder="请选择交货日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="交货地址" prop="deliveryAddress">
          <el-input
            v-model="form.deliveryAddress"
            placeholder="请输入交货地址"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            发布需求
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { purchaserAPI, categoryAPI } from '../../api';
import { bigIntToString, processBackendResponse } from '../../utils/bigint';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const categories = ref([]);

onMounted(() => {
  initializeForm();
  loadCategories();
});

// 表单数据
const form = ref({
  productName: '',
  categoryId: null,
  description: '',
  priceRange: '',
  unit: '',
  quantity: 1,
  specRequire: '',
  qualityRequire: '',
  deliveryDate: '',
  deliveryAddress: ''
});

// 初始化表单数据
const initializeForm = () => {
  form.value = {
    productName: '',
    categoryId: null,
    description: '',
    priceRange: '',
    unit: '',
    quantity: 1,
    specRequire: '',
    qualityRequire: '',
    deliveryDate: '',
    deliveryAddress: ''
  };
};

const rules = {
  productName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入需求描述', trigger: 'blur' }],
  priceRange: [{ required: true, message: '请输入价格范围', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入需求数量', trigger: 'blur' }],
  deliveryDate: [{ required: true, message: '请选择交货日期', trigger: 'change' }],
  deliveryAddress: [{ required: true, message: '请输入交货地址', trigger: 'blur' }],
};

// 加载产品分类
const loadCategories = async () => {
  try {
    console.log('Loading categories from backend...');
    const response = await categoryAPI.getTree();
    console.log('Category API response:', response);
    
    if (!response || !response.data) {
      throw new Error('Invalid response data');
    }
    
    // 后端返回的就是扁平列表，不需要展平
    if (Array.isArray(response.data)) {
      // 使用processBackendResponse处理响应数据，确保大整数正确处理
      let processedCategories = processBackendResponse(response.data);
      console.log('Processed categories:', processedCategories);
      
      // 验证处理结果
      if (Array.isArray(processedCategories)) {
        processedCategories.forEach((cat, index) => {
          const originalId = response.data[index]?.id;
          const parsedId = cat.id;
          if (originalId && parsedId && originalId.toString() !== parsedId.toString()) {
            console.warn(`分类ID精度问题已修复: 原始ID=${originalId}, 处理后ID=${parsedId}`);
          }
        });
      }
      
      categories.value = processedCategories.map(cat => ({
        id: bigIntToString(cat.id), // 确保ID转换为字符串
        name: cat.name
      }));
    } else {
      categories.value = [];
    }
    
    console.log('Loaded categories successfully:', categories.value);
    
    if (categories.value.length === 0) {
      console.warn('No categories found in backend, using defaults');
      useDefaultCategories();
    }
  } catch (error) {
    console.error('Load categories error:', error);
    console.log('Falling back to default categories');
    useDefaultCategories();
  }
};

// 使用默认分类
const useDefaultCategories = () => {
  categories.value = [
    { id: '1', name: '蔬菜' },
    { id: '2', name: '水果' },
    { id: '3', name: '粮食' },
    { id: '4', name: '畜牧' },
    { id: '5', name: '水产' },
  ];
  console.log('Using default categories:', categories.value);
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    // 确保ID转换为字符串
    const submitData = {
      ...form.value,
      categoryId: bigIntToString(form.value.categoryId), // 确保categoryId转换为字符串
    };

    await purchaserAPI.publishDemand(submitData);
    ElMessage.success('需求发布成功');
    router.push('/purchaser/demands');
  } catch (error) {
    console.error('Publish demand error:', error);
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.publish-demand-page {
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