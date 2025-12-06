<template>
  <div class="publish-product-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布产品</span>
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
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入产品名称" />
        </el-form-item>

        <el-form-item label="产品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择产品分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="如：500g/袋、5kg/箱等" />
        </el-form-item>

        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：斤、公斤、箱等" style="width: 200px" />
        </el-form-item>

        <el-form-item label="产品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述产品特点、种植/养殖方式等"
          />
        </el-form-item>

        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :min="0.01"
            :max="999999"
            style="width: 200px"
          />
          <span style="margin-left: 10px">元/{{ form.unit || '单位' }}</span>
        </el-form-item>

        <el-form-item label="起订量" prop="minPurchase">
          <el-input-number
            v-model="form.minPurchase"
            :min="1"
            :max="999999"
            style="width: 200px"
          />
          <span style="margin-left: 10px">{{ form.unit || '单位' }}</span>
        </el-form-item>

        <el-form-item label="库存数量" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="1"
            :max="999999"
            :step="1"
            :precision="0"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="产地" prop="originAreaId">
          <el-select v-model="form.originAreaId" placeholder="请选择产地">
            <el-option
              v-for="area in originAreas"
              :key="area.id"
              :label="area.areaName"
              :value="area.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="生产日期" prop="productionDate">
          <el-date-picker
            v-model="form.productionDate"
            type="date"
            placeholder="选择生产日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="保质期" prop="shelfLife">
          <el-input v-model="form.shelfLife" placeholder="如：30天、6个月等" />
        </el-form-item>

        <el-form-item label="生产方式" prop="productionMethod">
          <el-input v-model="form.productionMethod" placeholder="如：有机种植、大棚养殖等" />
        </el-form-item>

        <el-form-item label="产品图片" prop="images">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="5"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传5张图片，每张不超过2MB</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            发布产品
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="info" @click="analyzeProduct" :loading="aiLoading">
            AI分析产品
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- AI分析结果展示 -->
      <div v-if="aiResult" class="ai-result">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI分析结果</span>
            </div>
          </template>
          <div class="ai-result-content" v-html="aiResult"></div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElLoading } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { farmerAPI, categoryAPI, originAreaAPI } from '../../api';
import { toBigInt, bigIntToString, areBigIntsEqual, processResponseData, safeJsonParse, processBackendResponse } from '../../utils/bigint';

const router = useRouter();
const formRef = ref(null);
const submitting = ref(false);
const fileList = ref([]);
const categories = ref([]); // 分类列表
const originAreas = ref([]); // 产地列表
const form = ref({
  categoryId: null,
  name: '',
  spec: '',
  unit: '',
  description: '',
  price: 0,
  minPurchase: 1,
  stock: 1,
  originAreaId: null,
  productionDate: '',
  shelfLife: '',
  productionMethod: '',
});

// AI分析相关状态
const aiLoading = ref(false);
const aiResult = ref('');
// 初始化表单时，确保使用字符串ID
const initializeForm = () => {
  // 如果需要设置默认值，使用字符串ID
  // form.value.categoryId = '1'; // 示例：设置默认分类ID
  // form.value.originAreaId = '1'; // 示例：设置默认产地ID
};

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  spec: [{ required: true, message: '请输入规格', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '价格必须大于0', trigger: 'blur' }
  ],
  minPurchase: [
    { required: true, message: '请输入起订量', trigger: 'blur' },
    { type: 'number', min: 1, message: '起订量必须大于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '库存必须大于0', trigger: 'blur' }
  ],
  originAreaId: [{ required: true, message: '请选择产地', trigger: 'change' }],
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    console.log('Form data before submit:', form.value);

    console.log('=== Debug Info ===');
    console.log('Selected categoryId:', form.value.categoryId, 'Type:', typeof form.value.categoryId);
    console.log('Selected originAreaId:', form.value.originAreaId, 'Type:', typeof form.value.originAreaId);
    console.log('Available categories:', categories.value);
    console.log('Form data before submit:', form.value);
    console.log('==================');

    // 直接传数据对象，API中会创建 FormData
    const submitData = {
      categoryId: bigIntToString(form.value.categoryId), // 确保categoryId转换为字符串
      name: form.value.name,
      spec: form.value.spec,
      unit: form.value.unit,
      price: form.value.price,
      minPurchase: Number(form.value.minPurchase) || 1, // 确保minPurchase为数字类型，默认值为1
      stock: Number(form.value.stock) || 1, // 确保stock为数字类型，默认值为1
      originAreaId: bigIntToString(form.value.originAreaId), // 确保originAreaId转换为字符串
    };

    if (form.value.description) {
      submitData.description = form.value.description;
    }
    if (form.value.productionDate) {
      submitData.productionDate = form.value.productionDate;
    }
    if (form.value.shelfLife) {
      submitData.shelfLife = form.value.shelfLife;
    }
    if (form.value.productionMethod) {
      submitData.productionMethod = form.value.productionMethod;
    }

    // 处理图片：转换为 productImageDetails 格式
    if (fileList.value.length > 0) {
      submitData.productImageDetails = fileList.value
        .filter((file) => file.raw)
        .map((file, index) => ({
          file: file.raw,
          imageType: index === 0 ? 'COVER' : 'DETAIL', // 使用大写格式，匹配Java枚举命名规范
          sort: index,
        }));
    }

    console.log('Submit data:', {
      ...submitData,
      categoryId: submitData.categoryId,
      categoryIdType: typeof submitData.categoryId,
      originAreaId: submitData.originAreaId,
      originAreaIdType: typeof submitData.originAreaId,
      availableCategories: categories.value.map(c => ({ id: c.id.toString(), name: c.name })),
      productImageDetails: submitData.productImageDetails?.length || 0,
    });

    await farmerAPI.publishProduct(submitData);
    ElMessage.success('产品发布成功');
    router.push('/farmer/products');
  } catch (error) {
    console.error('Publish product error:', error);
  } finally {
    submitting.value = false;
  }
};

// 加载分类列表
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

// AI分析产品
async function analyzeProduct() {
  // 验证必要字段
  if (!form.value.name || !form.value.description || !form.value.categoryId) {
    ElMessage.error('请填写产品名称、描述和分类等必要信息');
    return;
  }
  
  aiLoading.value = true;
  aiResult.value = '';
  
  try {
    // 获取API Key
    const apiKey = localStorage.getItem('zhipu_api_key');
    console.log('使用的API Key:', apiKey ? `${apiKey.substring(0, 8)}...` : '未设置');
    
    if (!apiKey) {
      throw new Error('API Key未设置，请在浏览器控制台运行: localStorage.setItem("zhipu_api_key", "您的API Key")');
    }
    
    // 获取分类名称
    const category = categories.value.find(c => c.id === form.value.categoryId)?.name || '';
    // 获取产地名称
    const origin = originAreas.value.find(a => a.id === form.value.originAreaId)?.areaName || '';
    
    // 准备消息内容（多模态）
    const messages = [];
    
    // 添加用户消息
    const userMessage = {
      role: "user",
      content: []
    };
    
    // 添加文本内容
    userMessage.content.push({
      type: "text",
      text: `请分析以下农产品信息：\n\n产品名称：${form.value.name}\n产品分类：${category}\n规格：${form.value.spec}\n计量单位：${form.value.unit}\n产品描述：${form.value.description}\n单价：${form.value.price}元/单位\n起订量：${form.value.minPurchase}单位\n库存数量：${form.value.stock}单位\n产地：${origin}\n生产日期：${form.value.productionDate}\n保质期：${form.value.shelfLife}\n生产方式：${form.value.productionMethod}\n\n请从以下几个方面进行分析：\n1. 内容是否健康合规，是否包含违法违规信息\n2. 内容是否符合电商平台农产品发布规范\n3. 产品信息是否完整，是否需要补充\n4. 如果有问题，请指出具体问题并提供改进建议\n5. 最后给出审核结论（通过/不通过）`
    });
    
    // 处理图片（如果有）
    const imageBase64 = await processImages();
    if (imageBase64) {
      userMessage.content.push({
        type: "image_url",
        image_url: {
          url: imageBase64
        }
      });
      
      // 添加图片分析提示
      userMessage.content.push({
        type: "text",
        text: "\n\n同时请分析上传的图片内容：\n1. 图片内容是否与文字描述相符\n2. 图片是否清晰、真实\n3. 图片内容是否健康合规"
      });
    }
    
    messages.push(userMessage);
    
    // 构建请求数据
    const requestData = {
      model: "glm-4v", // 使用支持多模态的模型
      messages: messages,
      temperature: 0.7,
      max_tokens: 1000
    };
    
    console.log('发送的API请求数据:', JSON.stringify(requestData, null, 2));
    
    // 调用智谱清言API
    const response = await fetch('https://open.bigmodel.cn/api/paas/v4/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify(requestData)
    });
    
    console.log('API响应状态:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('API错误响应:', errorText);
      
      let errorMessage = `API请求失败：${response.status}`;
      try {
        const errorJson = JSON.parse(errorText);
        if (errorJson.error && errorJson.error.message) {
          errorMessage = errorJson.error.message;
        }
      } catch (e) {
        errorMessage = errorText;
      }
      
      throw new Error(errorMessage);
    }
    
    const result = await response.json();
    console.log('API成功响应:', result);
    
    // 处理AI返回的结果
    if (result.choices && result.choices[0] && result.choices[0].message) {
      const aiResponse = result.choices[0].message.content;
      // 将Markdown格式的响应转换为HTML显示
      aiResult.value = aiResponse.replace(/\n/g, '<br>').replace(/### (.*?)/g, '<h3>$1</h3>').replace(/## (.*?)/g, '<h2>$1</h2>').replace(/# (.*?)/g, '<h1>$1</h1>').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
    } else {
      throw new Error('API返回格式异常');
    }
    
  } catch (error) {
    console.error('AI分析出错:', error);
    ElMessage.error(`AI分析失败：${error.message}`);
  } finally {
    aiLoading.value = false;
  }
}

// 处理图片转换为Base64
async function processImages() {
  if (fileList.value.length === 0) return null;
  
  // 只处理第一张图片
  const file = fileList.value[0].raw;
  if (!file) return null;
  
  // 检查图片大小
  if (file.size > 2 * 1024 * 1024) {
    console.warn('图片大小超过2MB，将不进行AI分析');
    return null;
  }
  
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onloadend = () => {
      resolve(reader.result);
    };
    reader.readAsDataURL(file);
  });
}

onMounted(() => {
  initializeForm();
  loadCategories();
  loadOriginAreas();
});
</script>

<style scoped>
.publish-product-page {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}

.ai-result {
  margin-top: 20px;
}

.ai-result-content {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
