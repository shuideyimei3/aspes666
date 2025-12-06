<template>
  <div class="payments-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>支付记录</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="page-header">
      <h2>支付记录</h2>
      <p>查看和管理您的支付记录</p>
    </div>
    
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单编号">
          <el-input 
            v-model="searchForm.orderNo" 
            placeholder="请输入订单编号" 
            clearable 
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="请选择支付状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待支付" value="PENDING" />
            <el-option label="支付中" value="PROCESSING" />
            <el-option label="已支付" value="SUCCESS" />
            <el-option label="支付失败" value="FAILED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="请选择支付方式" clearable>
            <el-option label="全部" value="" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信支付" value="WECHAT" />
            <el-option label="银行卡" value="BANK_CARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-card class="table-card">
      <el-table 
        v-loading="loading" 
        :data="paymentsList" 
        style="width: 100%" 
        stripe
        @sort-change="handleSortChange"
      >
        <el-table-column prop="paymentNo" label="支付编号" width="150" />
        <el-table-column prop="orderNo" label="关联订单" width="150" />
        <el-table-column prop="productName" label="产品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="farmerName" label="农户名称" min-width="120" />
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="支付状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="scope">
            {{ getPaymentMethodText(scope.row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDate(scope.row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="viewDetail(scope.row.id)"
            >
              查看详情
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING'"
              type="success" 
              link 
              @click="payOrder(scope.row)"
            >
              立即支付
            </el-button>
            <el-button 
              v-if="scope.row.status === 'SUCCESS' && scope.row.canRefund"
              type="warning" 
              link 
              @click="refundPayment(scope.row)"
            >
              申请退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 支付对话框 -->
    <el-dialog 
      v-model="payDialogVisible" 
      title="支付订单" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="payForm" :rules="payRules" ref="payFormRef" label-width="100px">
        <el-form-item label="订单编号">
          <el-input v-model="payForm.orderNo" disabled />
        </el-form-item>
        <el-form-item label="支付金额">
          <el-input v-model="payForm.amount" disabled />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-radio-group v-model="payForm.paymentMethod">
            <el-radio label="ALIPAY">支付宝</el-radio>
            <el-radio label="WECHAT">微信支付</el-radio>
            <el-radio label="BANK_CARD">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPayment">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 退款对话框 -->
    <el-dialog 
      v-model="refundDialogVisible" 
      title="申请退款" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="100px">
        <el-form-item label="支付编号">
          <el-input v-model="refundForm.paymentNo" disabled />
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="refundForm.amount" disabled />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input 
            v-model="refundForm.reason" 
            type="textarea" 
            rows="3" 
            placeholder="请输入退款原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRefund">申请退款</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';
import { purchaserAPI } from '/api/index';

const router = useRouter();

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  status: '',
  paymentMethod: '',
  dateRange: []
});

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 排序数据
const sortData = reactive({
  prop: 'paymentTime',
  order: 'descending'
});

// 支付记录列表
const paymentsList = ref([]);
const loading = ref(false);

// 支付对话框
const payDialogVisible = ref(false);
const payFormRef = ref(null);
const payForm = reactive({
  id: '',
  orderNo: '',
  amount: '',
  paymentMethod: 'ALIPAY'
});
const payRules = {
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
};

// 退款对话框
const refundDialogVisible = ref(false);
const refundFormRef = ref(null);
const refundForm = reactive({
  id: '',
  paymentNo: '',
  amount: '',
  reason: ''
});
const refundRules = {
  reason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
};

// 获取支付记录列表
const fetchPaymentsList = async () => {
  try {
    loading.value = true;
    
    const response = await purchaserAPI.getPaymentsPage(
      pagination.currentPage,
      pagination.pageSize,
      {
        orderNo: searchForm.orderNo || undefined,
        status: searchForm.status || undefined,
        paymentMethod: searchForm.paymentMethod || undefined,
        startDate: searchForm.dateRange && searchForm.dateRange[0] || undefined,
        endDate: searchForm.dateRange && searchForm.dateRange[1] || undefined,
        sortBy: sortData.prop,
        sortOrder: sortData.order === 'ascending' ? 'asc' : 'desc'
      }
    );
    
    if (response.code === 200) {
      paymentsList.value = response.data.records || [];
      pagination.total = Number(response.data.total || 0);
    } else {
      ElMessage.error(response.message || '获取支付记录失败');
    }
  } catch (error) {
    console.error('获取支付记录失败:', error);
    ElMessage.error('获取支付记录失败');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1;
  fetchPaymentsList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.orderNo = '';
  searchForm.status = '';
  searchForm.paymentMethod = '';
  searchForm.dateRange = [];
  pagination.currentPage = 1;
  fetchPaymentsList();
};

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size;
  pagination.currentPage = 1;
  fetchPaymentsList();
};

// 当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page;
  fetchPaymentsList();
};

// 排序变化
const handleSortChange = ({ prop, order }) => {
  sortData.prop = prop;
  sortData.order = order;
  fetchPaymentsList();
};

// 查看详情
const viewDetail = (id) => {
  router.push(`/purchaser/payments/${id}`);
};

// 支付订单
const payOrder = (row) => {
  payForm.id = row.id;
  payForm.orderNo = row.orderNo;
  payForm.amount = row.amount;
  payForm.paymentMethod = 'ALIPAY';
  payDialogVisible.value = true;
};

// 提交支付
const submitPayment = async () => {
  if (!payFormRef.value) return;
  
  try {
    await payFormRef.value.validate();
    
    const response = await purchaserAPI.payOrder({
      paymentId: payForm.id,
      paymentMethod: payForm.paymentMethod
    });
    
    if (response.code === 200) {
      ElMessage.success('支付请求已提交，请完成支付');
      payDialogVisible.value = false;
      
      // 如果有支付链接，跳转到支付页面
      if (response.data.paymentUrl) {
        window.open(response.data.paymentUrl, '_blank');
      }
      
      fetchPaymentsList(); // 重新获取列表
    } else {
      ElMessage.error(response.message || '支付失败');
    }
  } catch (error) {
    console.error('支付失败:', error);
    ElMessage.error('支付失败');
  }
};

// 申请退款
const refundPayment = (row) => {
  refundForm.id = row.id;
  refundForm.paymentNo = row.paymentNo;
  refundForm.amount = row.amount;
  refundForm.reason = '';
  refundDialogVisible.value = true;
};

// 提交退款
const submitRefund = async () => {
  if (!refundFormRef.value) return;
  
  try {
    await refundFormRef.value.validate();
    
    const response = await purchaserAPI.refundPayment({
      paymentId: refundForm.id,
      reason: refundForm.reason
    });
    
    if (response.code === 200) {
      ElMessage.success('退款申请已提交，请等待审核');
      refundDialogVisible.value = false;
      fetchPaymentsList(); // 重新获取列表
    } else {
      ElMessage.error(response.message || '退款申请失败');
    }
  } catch (error) {
    console.error('退款申请失败:', error);
    ElMessage.error('退款申请失败');
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'PROCESSING': return 'primary';
    case 'SUCCESS': return 'success';
    case 'FAILED': return 'danger';
    case 'REFUNDED': return 'info';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待支付';
    case 'PROCESSING': return '支付中';
    case 'SUCCESS': return '已支付';
    case 'FAILED': return '支付失败';
    case 'REFUNDED': return '已退款';
    default: return '未知状态';
  }
};

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  switch (method) {
    case 'ALIPAY': return '支付宝';
    case 'WECHAT': return '微信支付';
    case 'BANK_CARD': return '银行卡';
    default: return '未知方式';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchPaymentsList();
});
</script>

<style scoped>
.payments-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.search-bar {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-form {
  margin-bottom: -18px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-table) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .search-form {
    display: block;
  }
  
  .el-form-item {
    margin-right: 0;
    margin-bottom: 18px;
  }
}
</style>