<template>
  <div class="products-page">
    <el-card class="header-card">
      <h2>产品列表</h2>
      <div class="filter-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="产品名称">
            <el-input
              v-model="searchForm.keyword"
              placeholder="输入产品名称"
              clearable
              @clear="handleSearch"
            />
          </el-form-item>
          <el-form-item label="产品分类">
            <el-select
              v-model="searchForm.categoryId"
              placeholder="请选择分类"
              clearable
              @clear="handleSearch"
            >
              <el-option
                v-for="category in categories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="产地">
            <el-select
              v-model="searchForm.originAreaId"
              placeholder="请选择产地"
              clearable
              @clear="handleSearch"
            >
              <el-option
                v-for="area in originAreas"
                :key="area.areaId"
                :label="area.areaName"
                :value="area.areaId"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="content-card">
      <el-table :data="products" v-loading="loading" stripe>
        <el-table-column prop="name" label="产品名称" min-width="150" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ row.categoryName }}
          </template>
        </el-table-column>
        <el-table-column label="产地" width="120">
          <template #default="{ row }">
            {{ row.originAreaName }}
          </template>
        </el-table-column>
        <el-table-column label="规格" width="120">
          <template #default="{ row }">
            {{ row.spec }}
          </template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}/{{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100">
          <template #default="{ row }">
            {{ row.stock }} {{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="农场" width="120">
          <template #default="{ row }">
            {{ row.farmName }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewProductDetail(row.id)">
              查看详情
            </el-button>
            <el-button 
              v-if="row.status === 'on_sale'" 
              link type="success" 
              size="small" 
              @click="createDemandFromProduct(row)"
            >
              发布需求
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { categoryAPI, originAreaAPI, purchaserAPI, searchAPI } from '../../api';
import { bigIntToString, processResponseData } from '../../utils/bigint';

const router = useRouter();
const loading = ref(false);
const products = ref([]);
const categories = ref([]);
const originAreas = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const searchForm = ref({
  keyword: '',
  categoryId: null,
  originAreaId: null,
});

const loadCategories = async () => {
  try {
    const response = await categoryAPI.getTree();
    categories.value = processResponseData(response.data);
  } catch (error) {
    console.error('Load categories error:', error);
  }
};

const loadOriginAreas = async () => {
  try {
    const response = await originAreaAPI.getPage(1, 1000);
    originAreas.value = processResponseData(response.data.records);
  } catch (error) {
    console.error('Load origin areas error:', error);
  }
};

const loadProducts = async () => {
  try {
    loading.value = true;
    let response;
    
    if (searchForm.value.keyword) {
      // 使用搜索接口
      response = await searchAPI.searchProducts({
        keyword: searchForm.value.keyword,
        pageNum: currentPage.value,
        pageSize: pageSize.value
      });
    } else {
      // 使用普通列表接口
      response = await purchaserAPI.getProductsPage(
        currentPage.value,
        pageSize.value,
        {
          categoryId: searchForm.value.categoryId,
          originAreaId: searchForm.value.originAreaId
        }
      );
    }
    
    // 处理响应数据，确保大整数ID正确显示
    products.value = processResponseData(response.data.records || response.data);
    total.value = Number(response.data.total || response.data.total || 0);
  } catch (error) {
    console.error('Load products error:', error);
    ElMessage.error('加载产品列表失败');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  currentPage.value = 1;
  loadProducts();
};

const resetSearch = () => {
  searchForm.value = {
    keyword: '',
    categoryId: null,
    originAreaId: null,
  };
  currentPage.value = 1;
  loadProducts();
};

const handleSizeChange = () => {
  loadProducts();
};

const handleCurrentChange = () => {
  loadProducts();
};

const viewProductDetail = (productId) => {
  router.push(`/products/${productId}`);
};

const createDemandFromProduct = (product) => {
  // 跳转到发布需求页面，并预填充产品信息
  router.push({
    path: '/purchaser/demands/publish',
    query: {
      productName: product.name,
      categoryId: bigIntToString(product.categoryId),
      originAreaId: bigIntToString(product.originAreaId),
    }
  });
};

// 获取产品状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'on_sale':
      return 'success';
    case 'off_sale':
      return 'info';
    default:
      return 'info';
  }
};

// 获取产品状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'on_sale':
      return '在售';
    case 'off_sale':
      return '下架';
    default:
      return '未知';
  }
};

onMounted(() => {
  loadCategories();
  loadOriginAreas();
  loadProducts();
});
</script>

<style scoped>
.products-page {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: #333;
}

.filter-container {
  margin-bottom: 10px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.content-card {
  min-height: 500px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>