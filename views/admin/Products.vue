<template>
  <div class="admin-products-page">
    <el-card class="header-card">
      <h2>产品管理</h2>
    </el-card>

    <el-card class="content-card">
      <el-table :data="products" v-loading="loading" stripe>
        <el-table-column prop="name" label="产品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="farmName" label="农户" width="150" />
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
            <el-button 
              link 
              :type="row.status === 'ON_SALE' || row.status === 'on_sale' ? 'warning' : 'success'" 
              size="small" 
              @click="toggleStatus(row)"
            >
              {{ row.status === 'ON_SALE' || row.status === 'on_sale' ? '下架' : '上架' }}
            </el-button>
            <el-button link type="danger" size="small" @click="deleteProduct(row)">
              删除
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
import { adminAPI } from '../../api';
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
    'SOLD_OUT': 'danger',
    'sold_out': 'danger',
  };
  return typeMap[status] || 'info';
};

const getStatusText = (status) => {
  const textMap = {
    'ON_SALE': '在售',
    'on_sale': '在售',
    'OFF_SALE': '下架',
    'off_sale': '下架',
    'SOLD_OUT': '售罄',
    'sold_out': '售罄',
  };
  return textMap[status] || status;
};

const loadProducts = async () => {
  try {
    loading.value = true;
    const response = await adminAPI.getProductsPage(currentPage.value, pageSize.value);
    console.log('Admin API raw response:', response);
    // 处理响应数据，确保大整数ID正确转换
    products.value = processResponseData(response.data).records;
    total.value = Number(response.data.total);
    
    // 添加调试日志
    console.log('Admin loaded products:', products.value.map(p => ({ 
      id: p.id, 
      name: p.name, 
      farmName: p.farmName,
      stock: p.stock,
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
  router.push(`/admin/products/${id}`);
};

const toggleStatus = async (row) => {
  // Define action outside try block so it's accessible in catch block
  const action = row.status === 'ON_SALE' || row.status === 'on_sale' ? '下架' : '上架';
  
  try {
    console.log(`Admin toggle status: Before ${action}, product status = ${row.status}, product id = ${row.id}`);
    
    await ElMessageBox.confirm(
      `确定要${action}该产品吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    let response;
    if (row.status === 'ON_SALE' || row.status === 'on_sale') {
      console.log(`Calling adminAPI.offlineProduct with id: ${row.id}`);
      response = await adminAPI.offlineProduct(row.id);
    } else {
      console.log(`Calling adminAPI.onSaleProduct with id: ${row.id}`);
      response = await adminAPI.onSaleProduct(row.id);
    }
    
    console.log(`${action} API response:`, response);
    ElMessage.success(`${action}成功`);
    
    // 重新加载产品列表以获取最新状态
    await loadProducts();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Toggle status error:', error);
      console.error('Error details:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      ElMessage.error(`${action}失败: ${error.message || '未知错误'}`);
    }
  }
};

const deleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品【${row.name}】吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error',
      }
    );

    console.log(`Admin deleting product with ID: ${row.id}`);
    await adminAPI.deleteProduct(row.id);
    ElMessage.success('删除产品成功');
    
    // 重新加载产品列表
    await loadProducts();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete product error:', error);
      ElMessage.error('删除产品失败');
    }
  }
};

const handleSizeChange = () => {
  loadProducts();
};

const handleCurrentChange = () => {
  loadProducts();
};

onMounted(() => {
  loadProducts();
});
</script>

<style scoped>
.admin-products-page {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
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
