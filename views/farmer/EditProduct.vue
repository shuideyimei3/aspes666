<template>
  <div class="edit-product-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>编辑产品</span>
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
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%" :loading="categoriesLoading">
            <el-option 
              v-for="category in categories" 
              :key="category.id || category.name" 
              :label="category.name" 
              :value="(category.id || '').toString()" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" placeholder="如：500g/袋、5kg/箱等" />
        </el-form-item>

        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：斤、公斤、箱等" style="width: 200px" />
        </el-form-item>

        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="form.price"
            :precision="2"
            :min="0.01"
            :max="999999"
            :placeholder="product.price || '请输入单价'"
            style="width: 200px"
          />
          <span style="margin-left: 10px">元/{{ form.unit || '单位' }}</span>
        </el-form-item>

        <el-form-item label="起订量" prop="minPurchase">
          <el-input-number
            v-model="form.minPurchase"
            :min="1"
            :max="999999"
            :placeholder="product.minPurchase || '请输入起订量'"
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
            :placeholder="product.stock || '请输入库存数量'"
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="产地" prop="originAreaId">
          <el-select v-model="form.originAreaId" placeholder="请选择产地" style="width: 100%" :loading="originAreasLoading">
            <el-option 
              v-for="area in originAreas" 
              :key="area.id || area.name" 
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

        <el-form-item label="产品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述产品特点、种植/养殖方式等"
          />
        </el-form-item>

        <el-form-item label="产品图片">
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="5"
            :on-exceed="handleExceed"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                编辑产品时需要重新上传所有图片，第一张图片将作为封面图，最多上传5张图片，每张图片不超过5MB
              </div>
            </template>
          </el-upload>
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
import { Plus } from '@element-plus/icons-vue';
import { farmerAPI, categoryAPI, originAreaAPI } from '../../api';
import { bigIntToString, processResponseData, safeJsonParse } from '../../utils/bigint';

const router = useRouter();
const route = useRoute();
const formRef = ref(null);
const loading = ref(false);
const submitting = ref(false);
const productId = route.params.id;
const fileList = ref([]); // 添加文件列表
const product = ref({}); // 存储原始产品数据，用于显示默认值

// 分类和产地数据
const categories = ref([]); // 产品分类列表
const originAreas = ref([]); // 产地列表
const categoriesLoading = ref(false); // 分类加载状态
const originAreasLoading = ref(false); // 产地加载状态

const form = ref({
  name: '',
  categoryId: null,
  spec: '',
  unit: '',
  description: '',
  price: null,
  minPurchase: null,
  stock: 1, // 修改为stock，与后端DTO一致
  originAreaId: null,
  productionDate: '', // 添加生产日期字段
  shelfLife: '', // 添加保质期字段
  productionMethod: '', // 添加生产方式字段
});

// 初始化表单数据
const initializeForm = () => {
  form.value = {
    name: '',
    categoryId: null,
    spec: '',
    unit: '',
    description: '',
    price: null,
    minPurchase: null,
    stock: 1, // 修改为stock，与后端DTO一致
    originAreaId: null,
    productionDate: '', // 添加生产日期字段
    shelfLife: '', // 添加保质期字段
    productionMethod: '', // 添加生产方式字段
  };
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

// 处理树形分类数据，提取所有分类节点
const flattenCategories = (categories, level = 0) => {
  let result = [];
  
  categories.forEach(category => {
    // 添加当前分类节点
    result.push({
      ...category,
      level: level,
      name: level > 0 ? `${'  '.repeat(level)}${category.name}` : category.name
    });
    
    // 如果有子分类，递归处理
    if (category.children && Array.isArray(category.children) && category.children.length > 0) {
      result = result.concat(flattenCategories(category.children, level + 1));
    }
  });
  
  return result;
};

// 加载产品分类
const loadCategories = async () => {
  try {
    categoriesLoading.value = true;
    const response = await categoryAPI.getTree();
    console.log('Categories API response:', response);
    
    // 处理API返回的数据
    let categoriesData = [];
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        // 如果直接返回数组，使用flattenCategories处理
        categoriesData = flattenCategories(response.data);
      } else if (response.data.children && Array.isArray(response.data.children)) {
        // 如果是树形结构，取第一层子节点并使用flattenCategories处理
        categoriesData = flattenCategories(response.data.children);
      }
    }
    
    categories.value = categoriesData;
    console.log('Processed categories:', categories.value);
  } catch (error) {
    console.error('Load categories error:', error);
    ElMessage.error('加载产品分类失败');
  } finally {
    categoriesLoading.value = false;
  }
};

// 加载产地数据
const loadOriginAreas = async () => {
  try {
    originAreasLoading.value = true;
    const response = await originAreaAPI.getPage(1, 100); // 获取前100个产地
    console.log('Origin areas API response:', response);
    
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
  } finally {
    originAreasLoading.value = false;
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

// 处理图片URL，添加协议头
const getImageUrl = (url) => {
  if (!url) return '';
  // 如果URL不包含协议头，添加https://aspes.
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    return `https://aspes.${url}`;
  }
  return url;
};

const loadProduct = async () => {
  try {
    loading.value = true;
    const response = await farmerAPI.getProductDetail(productId);
    // 响应数据已经在拦截器中处理过，但为了确保安全，再次处理
    const productData = processResponseData(response.data);
    
    // 存储原始产品数据
    product.value = productData;
    
    form.value = {
      name: productData.name,
      categoryId: productData.categoryId, // 确保categoryId转换为字符串
      spec: productData.spec || '',
      description: productData.description,
      price: productData.price,
      unit: productData.unit,
      minPurchase: productData.minPurchase !== undefined ? productData.minPurchase : null,
      stock: Number(productData.stock) || 1, // 修改为stock，确保为数字类型，默认值为1
      originAreaId: bigIntToString(productData.originAreaId), // 确保originAreaId转换为字符串
      productionDate: productData.productionDate || '', // 添加生产日期字段
      shelfLife: productData.shelfLife || '', // 添加保质期字段
      productionMethod: productData.productionMethod || '', // 添加生产方式字段
    };
    
    // 处理图片：编辑产品时需要重新上传所有图片
    // 不保留已有图片，用户需要重新上传所有图片
    if (fileList.value.length > 0) {
      submitData.productImageDetails = fileList.value.map((file, index) => {
        // 确保file.raw存在，这是Element Plus上传组件中实际文件对象
        if (!file.raw) {
          console.error('File raw data missing for:', file.name);
          return null;
        }
        
        return {
          file: file.raw,
          imageType: index === 0 ? 'COVER' : 'DETAIL', // 使用大写格式，匹配Java枚举命名规范
          sort: index,
        };
      }).filter(item => item !== null); // 过滤掉无效的文件项
      
      console.log('Processed images for submission:', submitData.productImageDetails.length, 'files');
    } else {
      console.log('No files in fileList to submit');
    }
  } catch (error) {
    console.error('Load product error:', error);
    ElMessage.error('加载产品信息失败');
  } finally {
    loading.value = false;
  }
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    submitting.value = true;

    // 确保ID转换为字符串
    const submitData = {
      ...form.value,
      categoryId: form.value.categoryId, // 确保categoryId转换为字符串
      originAreaId: form.value.originAreaId, // 确保originAreaId转换为字符串
      stock: Number(form.value.stock) || 1, // 确保stock为数字类型，默认值为1
      minPurchase: form.value.minPurchase !== null && form.value.minPurchase !== undefined ? Number(form.value.minPurchase) : 1,
    };
    
    // 确保minPurchase是有效的数字，如果为null或undefined则设为1
    if (submitData.minPurchase === null || submitData.minPurchase === undefined || isNaN(submitData.minPurchase)) {
      submitData.minPurchase = 1;
    }

    // 处理图片：编辑产品时需要重新上传所有图片
    // 不保留已有图片，用户需要重新上传所有图片
    if (fileList.value.length > 0) {
      submitData.productImageDetails = fileList.value.map((file, index) => {
        return {
          file: file.raw,
          imageType: index === 0 ? 'COVER' : 'DETAIL', // 使用大写格式，匹配Java枚举命名规范
          sort: index,
        };
      });
    }

    console.log('Submitting product with images:', submitData.productImageDetails);
    await farmerAPI.updateProduct(productId, submitData);
    ElMessage.success('修改成功');
    router.push('/farmer/products');
  } catch (error) {
    console.error('Update product error:', error);
    if (error.message) {
      ElMessage.error(error.message);
    }
  } finally {
    submitting.value = false;
  }
};

// 图片处理方法
const handleFileChange = (file, files) => {
  // 验证文件大小
  if (file.raw.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB');
    return false;
  }
  
  // 更新fileList，确保包含新添加的文件
  fileList.value = files;
  console.log('File added to fileList:', file.name, 'Total files:', fileList.value.length);
  return true;
};

const handleFileRemove = (file, files) => {
  // 更新fileList，移除被删除的文件
  fileList.value = files;
  console.log('File removed from fileList:', file.name, 'Total files:', fileList.value.length);
};

const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片');
};

onMounted(() => {
  initializeForm();
  loadCategories();
  loadOriginAreas();
  loadProduct();
});
</script>

<style scoped>
.edit-product-page {
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
