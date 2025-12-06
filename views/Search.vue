<template>
  <div class="search-page">
    <el-card class="search-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="搜索产品" name="products">
          <el-input
            v-model="keyword"
            placeholder="请输入产品关键词"
            clearable
            @keyup.enter="search"
          >
            <template #append>
              <el-button :icon="Search" @click="search" />
            </template>
          </el-input>

          <div class="results" v-loading="loading">
            <el-empty v-if="!loading && products.length === 0" description="暂无搜索结果" />
            <div v-else class="product-grid">
              <el-card 
                v-for="product in products" 
                :key="product.id" 
                class="product-card"
                shadow="hover"
                @click="viewProduct(product.id)"
              >
                <div class="product-info">
                  <h3>{{ product.name }}</h3>
                  <p class="price">¥{{ product.price }}/{{ product.unit }}</p>
                  <p class="stock">库存：{{ product.stockQuantity }}</p>
                  <p class="category">分类：{{ product.categoryName }}</p>
                </div>
              </el-card>
            </div>

            <div class="pagination" v-if="productTotal > 0">
              <el-pagination
                v-model:current-page="productPage"
                v-model:page-size="pageSize"
                :total="productTotal"
                layout="prev, pager, next"
                @current-change="handleProductPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="搜索需求" name="demands">
          <el-input
            v-model="keyword"
            placeholder="请输入需求关键词"
            clearable
            @keyup.enter="search"
          >
            <template #append>
              <el-button :icon="Search" @click="search" />
            </template>
          </el-input>

          <div class="results" v-loading="loading">
            <el-empty v-if="!loading && demands.length === 0" description="暂无搜索结果" />
            <div v-else class="demand-list">
              <el-card 
                v-for="demand in demands" 
                :key="demand.id" 
                class="demand-card"
                shadow="hover"
              >
                <div class="demand-info">
                  <h3>{{ demand.productCategoryName }}</h3>
                  <p>采购量：{{ demand.quantity }} {{ demand.unit }}</p>
                  <p>期望交货日期：{{ demand.expectedDeliveryDate }}</p>
                  <p class="description">{{ demand.description }}</p>
                </div>
              </el-card>
            </div>

            <div class="pagination" v-if="demandTotal > 0">
              <el-pagination
                v-model:current-page="demandPage"
                v-model:page-size="pageSize"
                :total="demandTotal"
                layout="prev, pager, next"
                @current-change="handleDemandPageChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="搜索农户" name="farmers">
          <el-input
            v-model="keyword"
            placeholder="请输入农户名称"
            clearable
            @keyup.enter="search"
          >
            <template #append>
              <el-button :icon="Search" @click="search" />
            </template>
          </el-input>

          <div class="results" v-loading="loading">
            <el-empty v-if="!loading && farmers.length === 0" description="暂无搜索结果" />
            <div v-else class="farmer-list">
              <el-card 
                v-for="farmer in farmers" 
                :key="farmer.id" 
                class="farmer-card"
                shadow="hover"
                @click="viewFarmer(farmer.id)"
              >
                <div class="farmer-info">
                  <h3>{{ farmer.name }}</h3>
                  <p>联系人：{{ farmer.contactPerson }}</p>
                  <p>联系电话：{{ farmer.contactPhone }}</p>
                  <p>产地：{{ farmer.originAreaName }}</p>
                </div>
              </el-card>
            </div>

            <div class="pagination" v-if="farmerTotal > 0">
              <el-pagination
                v-model:current-page="farmerPage"
                v-model:page-size="pageSize"
                :total="farmerTotal"
                layout="prev, pager, next"
                @current-change="handleFarmerPageChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { Search } from '@element-plus/icons-vue';
import { searchAPI } from '../api';

const router = useRouter();
const activeTab = ref('products');
const keyword = ref('');
const loading = ref(false);
const pageSize = ref(12);

// 产品搜索
const products = ref([]);
const productPage = ref(1);
const productTotal = ref(0);

// 需求搜索
const demands = ref([]);
const demandPage = ref(1);
const demandTotal = ref(0);

// 农户搜索
const farmers = ref([]);
const farmerPage = ref(1);
const farmerTotal = ref(0);

const search = async () => {
  if (!keyword.value.trim()) {
    return;
  }

  loading.value = true;
  try {
    if (activeTab.value === 'products') {
      await searchProducts();
    } else if (activeTab.value === 'demands') {
      await searchDemands();
    } else if (activeTab.value === 'farmers') {
      await searchFarmers();
    }
  } finally {
    loading.value = false;
  }
};

const searchProducts = async () => {
  const response = await searchAPI.searchProducts({
    keyword: keyword.value,
    pageNum: productPage.value,
    pageSize: pageSize.value,
  });
  products.value = response.data.records;
  productTotal.value = Number(response.data.total);
};

const searchDemands = async () => {
  const response = await searchAPI.searchDemands({
    keyword: keyword.value,
    pageNum: demandPage.value,
    pageSize: pageSize.value,
  });
  demands.value = response.data.records;
  demandTotal.value = Number(response.data.total);
};

const searchFarmers = async () => {
  const response = await searchAPI.searchFarmers({
    keyword: keyword.value,
    current: farmerPage.value,
    size: pageSize.value,
  });
  farmers.value = response.data.records;
  farmerTotal.value = Number(response.data.total);
};

const handleTabChange = () => {
  // 清空结果
  products.value = [];
  demands.value = [];
  farmers.value = [];
  productTotal.value = 0;
  demandTotal.value = 0;
  farmerTotal.value = 0;
  keyword.value = '';
};

const handleProductPageChange = () => {
  searchProducts();
};

const handleDemandPageChange = () => {
  searchDemands();
};

const handleFarmerPageChange = () => {
  searchFarmers();
};

const viewProduct = (id) => {
  router.push(`/products/${id}`);
};

const viewFarmer = (id) => {
  router.push(`/farmers/${id}`);
};
</script>

<style scoped>
.search-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  min-height: 600px;
}

:deep(.el-input-group__append) {
  padding: 0 15px;
}

.results {
  margin-top: 30px;
  min-height: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.product-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.product-info .price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.demand-list,
.farmer-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.demand-card,
.farmer-card {
  cursor: pointer;
  transition: all 0.2s;
}

.demand-card:hover,
.farmer-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.demand-info h3,
.farmer-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.demand-info p,
.farmer-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.description {
  color: #999;
  line-height: 1.6;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
