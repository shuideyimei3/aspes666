<template>
  <div class="product-detail">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser/products' }">产品浏览</el-breadcrumb-item>
      <el-breadcrumb-item>产品详情</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="product-container" v-loading="loading">
      <div class="product-header">
        <div class="product-images">
          <el-carousel :interval="4000" type="card" height="300px" v-if="productImages.length > 0">
            <el-carousel-item v-for="(image, index) in productImages" :key="index">
              <img :src="getImageUrl(image.url)" :alt="`产品图片 ${index + 1}`" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="no-image">
            <el-icon size="80"><Picture /></el-icon>
            <p>暂无图片</p>
          </div>
        </div>
        
        <div class="product-info">
          <h1>{{ product.name }}</h1>
          <div class="product-meta">
            <el-tag type="success" size="large">{{ product.categoryName }}</el-tag>
            <el-tag type="info" size="large" v-if="product.originAreaName">{{ product.originAreaName }}</el-tag>
            <el-tag :type="getStatusType(product.status)" size="large">{{ getStatusText(product.status) }}</el-tag>
          </div>
          
          <div class="product-price">
            <span class="price-label">价格：</span>
            <span class="price-value">¥{{ product.price }}/{{ product.unit }}</span>
          </div>
          
          <div class="product-stock">
            <span class="stock-label">库存：</span>
            <span class="stock-value">{{ product.stock }}{{ product.unit }}</span>
          </div>
          
          <div class="product-description">
            <h3>产品描述</h3>
            <p>{{ product.description || '暂无描述' }}</p>
          </div>
          
          <div class="farmer-info">
            <h3>农户信息</h3>
            <div class="farmer-card">
              <el-avatar :size="50" :src="farmerInfo.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="farmer-details">
                <p class="farmer-name">{{ farmerInfo.name }}</p>
                <p class="farmer-address" v-if="farmerInfo.address">{{ farmerInfo.address }}</p>
                <div class="farmer-rating">
                  <el-rate v-model="farmerInfo.rating" disabled show-score text-color="#ff9900" />
                </div>
              </div>
            </div>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="showPublishDemandDialog">
              发布采购需求
            </el-button>
            <el-button size="large" @click="contactFarmer">
              联系农户
            </el-button>
          </div>
        </div>
      </div>
      
      <div class="product-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="产品详情" name="details">
            <div class="tab-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="产品名称">{{ product.name }}</el-descriptions-item>
                <el-descriptions-item label="产品分类">{{ product.categoryName }}</el-descriptions-item>
                <el-descriptions-item label="产地">{{ product.originAreaName }}</el-descriptions-item>
                <el-descriptions-item label="价格">¥{{ product.price }}/{{ product.unit }}</el-descriptions-item>
                <el-descriptions-item label="库存">{{ product.stock }}{{ product.unit }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusType(product.status)">{{ getStatusText(product.status) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="发布时间" :span="2">{{ formatDate(product.publishTime) }}</el-descriptions-item>
                <el-descriptions-item label="产品描述" :span="2">{{ product.description || '暂无描述' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="农户信息" name="farmer">
            <div class="tab-content">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="农户名称">{{ farmerInfo.name }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ farmerInfo.phone || '未提供' }}</el-descriptions-item>
                <el-descriptions-item label="地址" :span="2">{{ farmerInfo.address || '未提供' }}</el-descriptions-item>
                <el-descriptions-item label="农户简介" :span="2">{{ farmerInfo.description || '暂无简介' }}</el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <!-- 发布采购需求对话框 -->
    <el-dialog v-model="publishDemandDialogVisible" title="发布采购需求" width="600px">
      <el-form :model="demandForm" :rules="demandRules" ref="demandFormRef" label-width="100px">
        <el-form-item label="需求标题" prop="title">
          <el-input v-model="demandForm.title" placeholder="请输入需求标题"></el-input>
        </el-form-item>
        <el-form-item label="产品分类" prop="categoryId">
          <el-cascader
            v-model="demandForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            placeholder="请选择产品分类"
            clearable
          ></el-cascader>
        </el-form-item>
        <el-form-item label="期望产地" prop="preferredOriginAreaId">
          <el-select v-model="demandForm.preferredOriginAreaId" placeholder="请选择期望产地" clearable>
            <el-option
              v-for="area in originAreaOptions"
              :key="area.id"
              :label="area.name"
              :value="area.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="需求数量" prop="quantity">
          <el-input-number v-model="demandForm.quantity" :min="1" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="demandForm.unit" placeholder="请输入单位"></el-input>
        </el-form-item>
        <el-form-item label="期望价格" prop="expectedPrice">
          <el-input-number v-model="demandForm.expectedPrice" :min="0" :precision="2"></el-input-number>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="demandForm.deadline"
            type="date"
            placeholder="请选择截止日期"
            :disabled-date="disabledDate"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="需求描述" prop="description">
          <el-input type="textarea" v-model="demandForm.description" :rows="4" placeholder="请描述您的需求"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDemandDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDemand">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Picture, User } from '@element-plus/icons-vue';
import { searchAPI, categoryAPI, originAreaAPI, purchaserAPI } from '/api/index';

const route = useRoute();
const router = useRouter();

const productId = ref(route.params.id);
const product = ref({});
const productImages = ref([]);
const farmerInfo = ref({});
const loading = ref(true);
const activeTab = ref('details');

// 发布需求相关
const publishDemandDialogVisible = ref(false);
const demandFormRef = ref(null);
const demandForm = ref({
  title: '',
  categoryId: null,
  preferredOriginAreaId: null,
  quantity: 1,
  unit: '',
  expectedPrice: null,
  deadline: null,
  description: ''
});
const demandRules = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入需求数量', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  expectedPrice: [{ required: true, message: '请输入期望价格', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }]
};

const categoryOptions = ref([]);
const originAreaOptions = ref([]);

// 获取产品详情
const fetchProductDetail = async () => {
  try {
    loading.value = true;
    const response = await purchaserAPI.getProductDetail(productId.value);

    if (response.code === 200 && response.data) {
      const productData = response.data;
      product.value = productData;
      
      // 获取产品图片
      if (productData.images && productData.images.length > 0) {
        productImages.value = productData.images;
      }
      
      // 获取农户信息
      if (productData.farmerId) {
        const farmerResponse = await purchaserAPI.getFarmerInfo(productData.farmerId);
        if (farmerResponse.code === 200 && farmerResponse.data) {
          farmerInfo.value = farmerResponse.data;
        }
      }
    } else {
      ElMessage.error('产品不存在或已被下架');
      router.push('/purchaser/products');
    }
  } catch (error) {
    console.error('获取产品详情失败:', error);
    ElMessage.error('获取产品详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取分类选项
const fetchCategoryOptions = async () => {
  try {
    const response = await categoryAPI.getTree();
    if (response.code === 200) {
      categoryOptions.value = response.data;
    }
  } catch (error) {
    console.error('获取分类选项失败:', error);
  }
};

// 获取产地选项
const fetchOriginAreaOptions = async () => {
  try {
    const response = await originAreaAPI.getPage(1, 100);
    if (response.code === 200) {
      originAreaOptions.value = response.data.records;
    }
  } catch (error) {
    console.error('获取产地选项失败:', error);
  }
};

// 显示发布需求对话框
const showPublishDemandDialog = () => {
  // 预填充表单数据
  demandForm.value = {
    title: `采购${product.value.name}`,
    categoryId: product.value.categoryId,
    preferredOriginAreaId: product.value.originAreaId,
    quantity: 1,
    unit: product.value.unit,
    expectedPrice: product.value.price,
    deadline: null,
    description: `希望采购${product.value.name}，品质要求高，价格合理。`
  };
  publishDemandDialogVisible.value = true;
};

// 提交需求
const submitDemand = async () => {
  if (!demandFormRef.value) return;
  
  try {
    await demandFormRef.value.validate();
    
    // 处理级联选择器的值
    const categoryId = Array.isArray(demandForm.value.categoryId) 
      ? demandForm.value.categoryId[demandForm.value.categoryId.length - 1]
      : demandForm.value.categoryId;
    
    const demandData = {
      ...demandForm.value,
      categoryId
    };
    
    const response = await purchaserAPI.publishDemand(demandData);
    if (response.code === 200) {
      ElMessage.success('采购需求发布成功');
      publishDemandDialogVisible.value = false;
      router.push('/purchaser/demands');
    } else {
      ElMessage.error(response.message || '发布失败');
    }
  } catch (error) {
    console.error('发布需求失败:', error);
    ElMessage.error('发布需求失败');
  }
};

// 联系农户
const contactFarmer = () => {
  ElMessageBox.confirm(
    `确定要联系 ${farmerInfo.value.name} 吗？`,
    '联系农户',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    }
  ).then(() => {
    // 这里可以实现联系农户的功能，比如跳转到聊天页面或显示联系方式
    ElMessage.info('农户联系方式：' + (farmerInfo.value.phone || '未提供'));
  }).catch(() => {
    // 用户取消操作
  });
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'on_sale': return 'success';
    case 'off_sale': return 'info';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'on_sale': return '在售';
    case 'off_sale': return '下架';
    default: return '未知';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知';
  const date = new Date(dateString);
  return date.toLocaleString();
};

// 禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7;
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

onMounted(() => {
  fetchProductDetail();
  fetchCategoryOptions();
  fetchOriginAreaOptions();
});
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.product-container {
  margin-top: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.product-header {
  display: flex;
  padding: 30px;
  gap: 30px;
}

.product-images {
  flex: 1;
  max-width: 500px;
}

.carousel-image {
  width: 100%;
  height: 300px;
  object-fit: cover;
  border-radius: 8px;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #909399;
}

.product-info {
  flex: 1;
}

.product-info h1 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 24px;
  color: #303133;
}

.product-meta {
  margin-bottom: 20px;
}

.product-meta .el-tag {
  margin-right: 10px;
}

.product-price {
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.product-stock {
  margin-bottom: 20px;
  font-size: 16px;
  color: #606266;
}

.product-description {
  margin-bottom: 20px;
}

.product-description h3 {
  margin-bottom: 10px;
  font-size: 18px;
  color: #303133;
}

.farmer-info {
  margin-bottom: 30px;
}

.farmer-info h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.farmer-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.farmer-details {
  flex: 1;
}

.farmer-name {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.farmer-address {
  margin: 0 0 10px;
  font-size: 14px;
  color: #606266;
}

.action-buttons {
  display: flex;
  gap: 15px;
}

.product-tabs {
  padding: 0 30px 30px;
}

.tab-content {
  padding: 20px 0;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-carousel__item) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-descriptions) {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .product-header {
    flex-direction: column;
  }
  
  .product-images {
    max-width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>