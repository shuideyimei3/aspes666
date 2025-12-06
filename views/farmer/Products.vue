<template>
  <div class="products-page">
    <el-card class="header-card">
      <div class="header-actions">
        <h2>我的产品</h2>
        <el-button type="primary" @click="router.push('/farmer/products/publish')">
          <el-icon><Plus /></el-icon>
          发布产品
        </el-button>
      </div>
    </el-card>

    <el-card class="content-card">
      <el-table :data="products" v-loading="loading" stripe>
        <el-table-column prop="name" label="产品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}/{{ row.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewProduct(row.id)">
              查看
            </el-button>
            <el-button link type="primary" size="small" @click="editProduct(row.id)">
              编辑
            </el-button>
            <el-button 
              link 
              :type="row.status === 'ON_SALE' || row.status === 'on_sale' ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ON_SALE' || row.status === 'on_sale' ? '下架' : '上架' }}
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
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { farmerAPI } from '../../api';
import { processResponseData } from '../../utils/bigint';

const router = useRouter();
const loading = ref(false);
const products = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

const getStatusType = (status) => {
  const typeMap = {
    'ON_SALE': 'success',
    'on_sale': 'success',
    'OFF_SALE': 'info',
    'off_sale': 'info',
  };
  return typeMap[status] || 'info';
};

const getStatusText = (status) => {
  const textMap = {
    'ON_SALE': '在售',
    'on_sale': '在售',
    'OFF_SALE': '下架',
    'off_sale': '下架',
  };
  return textMap[status] || status;
};

const loadProducts = async () => {
  try {
    loading.value = true;
    const response = await farmerAPI.getProductsPage(currentPage.value, pageSize.value);
    console.log('Farmer API raw response:', response);
    // 处理响应数据，确保大整数ID正确转换
    products.value = processResponseData(response.data).records;
    total.value = Number(response.data.total);
    
    // 添加调试日志
    console.log('Farmer loaded products:', products.value.map(p => ({ 
      id: p.id, 
      name: p.name, 
      status: p.status, 
      statusType: typeof p.status 
    })));
  } catch (error) {
    console.error('Load products error:', error);
  } finally {
    loading.value = false;
  }
};

const viewProduct = (id) => {
  router.push({ name: 'FarmerProductDetail', params: { id } });
};

const editProduct = (id) => {
  router.push(`/farmer/products/${id}/edit`);
};

const toggleStatus = async (row) => {
  try {
    const action = row.status === 'ON_SALE' || row.status === 'on_sale' ? '下架' : '上架';
    console.log(`Farmer toggle status: Before ${action}, product status = ${row.status}`);
    
    await ElMessageBox.confirm(
      `确定要${action}该产品吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    if (row.status === 'ON_SALE' || row.status === 'on_sale') {
      await farmerAPI.offlineProduct(row.id);
    } else {
      await farmerAPI.onSaleProduct(row.id);
    }
    ElMessage.success(`${action}成功`);
    loadProducts();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Toggle status error:', error);
      ElMessage.error(`${action}失败`);
    }
  }
};

const handleSizeChange = (val) => {
  pageSize.value = val;
  loadProducts();
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
  loadProducts();
};

onMounted(() => {
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

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
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
