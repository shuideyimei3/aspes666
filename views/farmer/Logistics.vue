<template>
  <div class="logistics-management">
    <div class="page-header">
      <h2>物流管理</h2>
      <el-button type="primary" @click="showCreateDialog = true" v-if="hasUnshippedOrders">
        创建物流记录
      </el-button>
    </div>

    <!-- 物流记录列表 -->
    <el-card class="logistics-list">
      <template #header>
        <div class="card-header">
          <span>物流记录</span>
          <el-select v-model="statusFilter" placeholder="筛选状态" clearable style="width: 150px">
            <el-option label="待发货" value="pending" />
            <el-option label="已发货" value="shipped" />
            <el-option label="运输中" value="transit" />
            <el-option label="已到达" value="arrived" />
            <el-option label="已签收" value="signed" />
          </el-select>
        </div>
      </template>

      <el-table :data="filteredLogisticsList" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单编号" width="180" />
        <el-table-column prop="logisticsCompany" label="物流公司" width="150" />
        <el-table-column prop="trackingNo" label="物流单号" width="180" />
        <el-table-column prop="transportType" label="运输方式" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="departureTime" label="发货时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.departureTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="arrivalTime" label="到达时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.arrivalTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="viewLogisticsDetail(row.id)">查看详情</el-button>
            <el-button size="small" type="primary" @click="showTraceDialog(row.id)" v-if="row.status !== 'signed'">
              添加轨迹
            </el-button>
            <el-button size="small" type="success" @click="shipGoods(row.id)" v-if="row.status === 'pending'">
              发货
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建物流记录对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建物流记录" width="600px">
      <el-form :model="logisticsForm" :rules="logisticsRules" ref="logisticsFormRef" label-width="120px">
        <el-form-item label="关联订单" prop="orderId">
          <el-select v-model="logisticsForm.orderId" placeholder="请选择订单" style="width: 100%">
            <el-option
              v-for="order in unshippedOrders"
              :key="order.id"
              :label="`${order.orderNo} - ${order.productName}`"
              :value="order.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-input v-model="logisticsForm.logisticsCompany" placeholder="请输入物流公司名称" />
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model="logisticsForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="运输方式" prop="transportType">
          <el-select v-model="logisticsForm.transportType" placeholder="请选择运输方式" style="width: 100%">
            <el-option label="陆运" value="陆运" />
            <el-option label="海运" value="海运" />
            <el-option label="空运" value="空运" />
            <el-option label="铁路运输" value="铁路运输" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际交货数量" prop="actualQuantity">
          <el-input-number v-model="logisticsForm.actualQuantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="检验结果" prop="inspectionResult">
          <el-input v-model="logisticsForm.inspectionResult" type="textarea" placeholder="请输入检验结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createLogistics">创建</el-button>
      </template>
    </el-dialog>

    <!-- 物流详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="物流详情" width="800px">
      <div v-if="currentLogistics">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ currentLogistics.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="物流公司">{{ currentLogistics.logisticsCompany }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ currentLogistics.trackingNo }}</el-descriptions-item>
          <el-descriptions-item label="运输方式">{{ currentLogistics.transportType }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentLogistics.status)">
              {{ getStatusText(currentLogistics.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDate(currentLogistics.departureTime) }}</el-descriptions-item>
          <el-descriptions-item label="到达时间">{{ formatDate(currentLogistics.arrivalTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentLogistics.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">物流轨迹</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(trace, index) in currentLogistics.traces"
            :key="trace.id"
            :timestamp="formatDate(trace.nodeTime)"
            placement="top"
          >
            <el-card>
              <h4>{{ trace.nodeLocation || '位置信息' }}</h4>
              <p>{{ trace.nodeDesc }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <!-- 添加物流轨迹对话框 -->
    <el-dialog v-model="showTraceDialog" title="添加物流轨迹" width="600px">
      <el-form :model="traceForm" :rules="traceRules" ref="traceFormRef" label-width="120px">
        <el-form-item label="节点时间" prop="nodeTime">
          <el-date-picker
            v-model="traceForm.nodeTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="节点位置" prop="nodeLocation">
          <el-input v-model="traceForm.nodeLocation" placeholder="请输入节点位置" />
        </el-form-item>
        <el-form-item label="节点描述" prop="nodeDesc">
          <el-input v-model="traceForm.nodeDesc" type="textarea" placeholder="请输入节点描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTraceDialog = false">取消</el-button>
        <el-button type="primary" @click="addTrace">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { farmerAPI } from '../../api';

// 响应式数据
const loading = ref(false);
const logisticsList = ref([]);
const ordersList = ref([]);
const statusFilter = ref('');
const showCreateDialog = ref(false);
const showDetailDialog = ref(false);
const showTraceDialog = ref(false);
const currentLogistics = ref(null);
const currentLogisticsId = ref(null);

// 表单数据
const logisticsForm = reactive({
  orderId: '',
  logisticsCompany: '',
  trackingNo: '',
  transportType: '',
  actualQuantity: 0,
  inspectionResult: ''
});

const traceForm = reactive({
  nodeTime: '',
  nodeLocation: '',
  nodeDesc: ''
});

// 表单引用
const logisticsFormRef = ref(null);
const traceFormRef = ref(null);

// 表单验证规则
const logisticsRules = {
  orderId: [{ required: true, message: '请选择订单', trigger: 'change' }],
  logisticsCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  transportType: [{ required: true, message: '请选择运输方式', trigger: 'change' }],
  actualQuantity: [
    { required: true, message: '请输入实际交货数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '实际交货数量必须大于0', trigger: 'blur' }
  ],
  inspectionResult: [{ required: true, message: '请输入检验结果', trigger: 'blur' }]
};

const traceRules = {
  nodeTime: [{ required: true, message: '请选择节点时间', trigger: 'change' }],
  nodeDesc: [{ required: true, message: '请输入节点描述', trigger: 'blur' }]
};

// 计算属性
const filteredLogisticsList = computed(() => {
  if (!statusFilter.value) return logisticsList.value;
  return logisticsList.value.filter(item => item.status === statusFilter.value);
});

const hasUnshippedOrders = computed(() => {
  return unshippedOrders.value.length > 0;
});

const unshippedOrders = computed(() => {
  return ordersList.value.filter(order => !order.logisticsId);
});

// 方法
const fetchLogisticsList = async () => {
  loading.value = true;
  try {
    // 获取所有订单
    const ordersRes = await farmerAPI.getMyOrders();
    ordersList.value = ordersRes.data || [];
    
    // 获取每个订单的物流信息
    const logisticsPromises = ordersList.value.map(async (order) => {
      try {
        const logisticsRes = await farmerAPI.getLogisticsByOrderId(order.id);
        return logisticsRes.data || null;
      } catch (error) {
        // 如果订单没有物流信息，返回null
        return null;
      }
    });
    
    const logisticsResults = await Promise.all(logisticsPromises);
    logisticsList.value = logisticsResults.filter(logistics => logistics !== null);
  } catch (error) {
    console.error('获取物流列表失败:', error);
    ElMessage.error('获取物流列表失败');
  } finally {
    loading.value = false;
  }
};

const createLogistics = async () => {
  if (!logisticsFormRef.value) return;
  
  try {
    await logisticsFormRef.value.validate();
    await farmerAPI.createLogistics(logisticsForm);
    ElMessage.success('物流记录创建成功');
    showCreateDialog.value = false;
    resetLogisticsForm();
    fetchLogisticsList();
  } catch (error) {
    console.error('创建物流记录失败:', error);
    ElMessage.error('创建物流记录失败');
  }
};

const viewLogisticsDetail = async (logisticsId) => {
  try {
    const res = await farmerAPI.getLogisticsDetail(logisticsId);
    currentLogistics.value = res.data;
    
    // 获取物流轨迹
    const tracesRes = await farmerAPI.getLogisticsTraces(logisticsId);
    currentLogistics.value.traces = tracesRes.data || [];
    
    showDetailDialog.value = true;
  } catch (error) {
    console.error('获取物流详情失败:', error);
    ElMessage.error('获取物流详情失败');
  }
};

const addTrace = async () => {
  if (!traceFormRef.value) return;
  
  try {
    await traceFormRef.value.validate();
    await farmerAPI.addLogisticsTrace(currentLogisticsId.value, traceForm);
    ElMessage.success('物流轨迹添加成功');
    showTraceDialog.value = false;
    resetTraceForm();
    
    // 刷新物流详情
    if (showDetailDialog.value) {
      viewLogisticsDetail(currentLogisticsId.value);
    }
  } catch (error) {
    console.error('添加物流轨迹失败:', error);
    ElMessage.error('添加物流轨迹失败');
  }
};

const shipGoods = async (logisticsId) => {
  try {
    await ElMessageBox.confirm('确认发货？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await farmerAPI.shipGoods(logisticsId);
    ElMessage.success('发货成功');
    fetchLogisticsList();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error);
      ElMessage.error('发货失败');
    }
  }
};

const resetLogisticsForm = () => {
  Object.keys(logisticsForm).forEach(key => {
    if (key === 'actualQuantity') {
      logisticsForm[key] = 0;
    } else {
      logisticsForm[key] = '';
    }
  });
};

const resetTraceForm = () => {
  Object.keys(traceForm).forEach(key => {
    traceForm[key] = '';
  });
};

const getStatusType = (status) => {
  const statusMap = {
    'pending': 'info',
    'shipped': 'primary',
    'transit': 'warning',
    'arrived': 'success',
    'signed': 'success'
  };
  return statusMap[status] || 'info';
};

const getStatusText = (status) => {
  const statusMap = {
    'pending': '待发货',
    'shipped': '已发货',
    'transit': '运输中',
    'arrived': '已到达',
    'signed': '已签收'
  };
  return statusMap[status] || '未知状态';
};

const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

// 生命周期
onMounted(() => {
  fetchLogisticsList();
});
</script>

<style scoped>
.logistics-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.logistics-list {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>