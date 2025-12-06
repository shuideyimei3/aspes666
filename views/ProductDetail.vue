<template>
  <div class="product-detail-page" v-loading="loading">
    <div class="detail-container" v-if="product">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/purchaser/products' }">产品列表</el-breadcrumb-item>
        <el-breadcrumb-item>产品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card class="product-card">
        <template #header>
          <div class="card-header">
            <span>产品详情</span>
            
          </div>
        </template>

        <div class="product-content">
          <div class="product-images">
            <el-carousel height="400px" v-if="product.images && product.images.length > 0">
              <el-carousel-item v-for="(image, index) in product.images" :key="index">
                <img :src="getImageUrl(image.url)" :alt="product.name" class="product-image" />
              </el-carousel-item>
            </el-carousel>
            <div class="no-image" v-else>
              <el-empty description="暂无产品图片" />
            </div>
          </div>

          <div class="product-info">
            <h2 class="product-name">{{ product.name }}</h2>
            <div class="product-meta">
              <el-tag type="success" size="large">{{ getCategoryName(product.categoryId) }}</el-tag>
              <el-tag type="info" size="large">{{ getOriginAreaName(product.originAreaId) }}</el-tag>
              <el-tag :type="getStatusType(product.status)" size="large">{{ getStatusText(product.status) }}</el-tag>
            </div>

            <div class="product-price">
              <span class="price-label">价格：</span>
              <span class="price-value">¥{{ product.price }}</span>
              <span class="price-unit">/ {{ product.unit }}</span>
            </div>

            <div class="product-stock">
              <span class="stock-label">库存：</span>
              <span class="stock-value">{{ product.stock }} {{ product.unit }}</span>
            </div>

            <div class="product-description">
              <h3>产品描述</h3>
              <p>{{ product.description }}</p>
            </div>

            <div class="product-details">
              <el-descriptions title="详细信息" :column="2" border>
                <el-descriptions-item label="产品名称">{{ product.name }}</el-descriptions-item>
                <el-descriptions-item label="产品分类">{{ getCategoryName(product.categoryId) }}</el-descriptions-item>
                <el-descriptions-item label="规格">{{ product.spec || '无' }}</el-descriptions-item>
                <el-descriptions-item label="计量单位">{{ product.unit || '无' }}</el-descriptions-item>
                <el-descriptions-item label="单价">¥{{ product.price }} / {{ product.unit || '单位' }}</el-descriptions-item>
                <el-descriptions-item label="起订量">{{ product.minPurchase || 0 }} {{ product.unit || '单位' }}</el-descriptions-item>
                <el-descriptions-item label="库存数量">{{ product.stock || 0 }} {{ product.unit || '单位' }}</el-descriptions-item>
                <el-descriptions-item label="产地">{{ getOriginAreaName(product.originAreaId) }}</el-descriptions-item>
                <el-descriptions-item label="生产日期">{{ product.productionDate || '无' }}</el-descriptions-item>
                <el-descriptions-item label="保质期">{{ product.shelfLife || '无' }}</el-descriptions-item>
                <el-descriptions-item label="生产方式">{{ product.productionMethod || '无' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { purchaserAPI, categoryAPI, originAreaAPI } from '../api';
import { bigIntToString, processResponseData, safeJsonParse } from '../utils/bigint';

const router = useRouter();
const route = useRoute();
const productId = route.params.id;

const loading = ref(false);
const product = ref(null);

// 分类和产地数据
const categories = ref([]); // 产品分类列表
const originAreas = ref([]); // 产地列表
const categoriesLoading = ref(false); // 分类加载状态
const originAreasLoading = ref(false); // 产地加载状态

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

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId || !categories.value || categories.value.length === 0) {
    return '未知分类';
  }
  const id = bigIntToString(categoryId);
  const category = categories.value.find(cat => bigIntToString(cat.id) === id);
  return category ? category.name : '未知分类';
};

// 获取产地名称
const getOriginAreaName = (originAreaId) => {
  if (!originAreaId || !originAreas.value || originAreas.value.length === 0) {
    return '未知产地';
  }
  const id = bigIntToString(originAreaId);
  const area = originAreas.value.find(area => bigIntToString(area.id) === id);
  return area ? area.areaName : '未知产地';
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'ON_SALE': 
    case 'ONSALE':
    case 'on_sale':
    case '在售':
    case '在售卖':
      return 'success';
    case 'OFF_SALE': 
    case 'OFFSALE':
    case 'off_sale':
    case '下架':
      return 'info';
    default: 
      // 如果状态为空或null，默认显示为info类型
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'ON_SALE': 
    case 'ONSALE':
    case 'on_sale':
    case '在售':
    case '在售卖':
      return '在售卖';
    case 'OFF_SALE': 
    case 'OFFSALE':
    case 'off_sale':
    case '下架':
      return '下架';
    default: 
      // 如果状态为空或null，默认显示为下架
      return status ? '下架' : '下架';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
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

// 加载产品详情
const loadProductDetail = async () => {
  try {
    loading.value = true;
    const response = await purchaserAPI.getProductDetail(productId);
    // 响应数据已经在拦截器中处理过，但为了确保安全，再次处理
    product.value = processResponseData(response.data);
  } catch (error) {
    console.error('Load product detail error:', error);
    ElMessage.error('加载产品详情失败');
  } finally {
    loading.value = false;
  }
};

// 基于此产品创建需求
const createDemandFromProduct = () => {
  if (!product.value) return;
  
  // 将产品信息传递给需求发布页面
  const productInfo = {
    name: product.value.name,
    categoryId: product.value.categoryId,
    originAreaId: product.value.originAreaId,
    description: product.value.description
  };
  
  // 跳转到需求发布页面，并传递产品信息
  router.push({
    path: '/purchaser/demands/create',
    query: { productInfo: JSON.stringify(productInfo) }
  });
};

onMounted(() => {
  loadCategories();
  loadOriginAreas();
  loadProductDetail();
});
</script>

<style scoped>
.product-detail-page {
  padding: 20px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
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

.product-card {
  margin-bottom: 20px;
}

.product-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-images {
  flex: 1;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  margin: 0 0 15px 0;
  font-size: 24px;
  color: #303133;
}

.product-meta {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.product-price {
  margin-bottom: 15px;
  font-size: 18px;
}

.price-label {
  color: #606266;
}

.price-value {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.price-unit {
  color: #909399;
}

.product-stock {
  margin-bottom: 20px;
  font-size: 16px;
}

.stock-label {
  color: #606266;
}

.stock-value {
  font-weight: bold;
}

.product-description {
  margin-bottom: 20px;
}

.product-description h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.product-description p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

.product-details {
  margin-top: 20px;
}

@media (min-width: 768px) {
  .product-content {
    flex-direction: row;
  }
  
  .product-images {
    flex: 1;
    max-width: 500px;
  }
  
  .product-info {
    flex: 1;
  }
}
</style>
