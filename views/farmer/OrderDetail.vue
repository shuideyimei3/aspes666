<template>
  <div class="order-detail">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/farmer/orders' }">订单管理</el-breadcrumb-item>
      <el-breadcrumb-item>订单详情</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div class="order-container" v-loading="loading">
      <el-card class="order-status-card">
        <div class="status-header">
          <h2>订单状态</h2>
          <el-tag :type="getStatusType(order.status)" size="large">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        
        <el-steps :active="getStepActive(order.status)" finish-status="success" align-center>
          <el-step title="待确认" description="农户已响应需求，等待采购方确认"></el-step>
          <el-step title="待检验" description="双方确认订单，等待产品检验"></el-step>
          <el-step title="待支付" description="检验完成，等待采购方支付"></el-step>
          <el-step title="待发货" description="支付已完成，等待农户发货"></el-step>
          <el-step title="待收货" description="商品已发货，等待采购方收货"></el-step>
          <el-step title="已完成" description="交易已完成"></el-step>
        </el-steps>
      </el-card>
      
      <!-- 订单信息 -->
      <el-card class="order-info-card">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ order.productInfo?.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="产品规格">{{ order.productInfo?.spec || '-' }}</el-descriptions-item>
          <el-descriptions-item label="采购数量">{{ order.quantity }} {{ order.productInfo?.unit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ order.totalAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实际数量">{{ order.actualQuantity || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ order.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ order.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="交付时间" v-if="order.deliveryTime">{{ order.deliveryTime }}</el-descriptions-item>
          <el-descriptions-item label="检验结果" v-if="order.inspectionResult">{{ order.inspectionResult }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="order-actions">
          <el-button 
            v-if="order.status === 'paid' && !logisticsInfo" 
            type="primary" 
            @click="showCreateLogisticsDialog"
          >
            创建物流信息
          </el-button>
          
          <el-button 
            v-if="logisticsInfo && logisticsInfo.id && (logisticsInfo.status === 'PENDING' || logisticsInfo.status === 'CREATED')" 
            type="success" 
            @click="showShipGoodsDialog"
          >
            发货
          </el-button>
          
          <el-button 
            v-if="order.status === 'pending'" 
            type="danger" 
            @click="cancelOrder"
          >
            取消订单
          </el-button>
          
          <el-button 
            v-if="order.status === 'completed'" 
            type="primary" 
            @click="createNewOrder"
          >
            再次下单
          </el-button>
        </div>
      </el-card>
      
      <el-card class="parties-info-card">
        <template #header>
          <span>交易双方信息</span>
        </template>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-descriptions title="采购方信息" :column="1" border>
              <el-descriptions-item label="名称">{{ purchaserInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ purchaserInfo.contactPerson }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ purchaserInfo.contactPhone }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ purchaserInfo.address }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
          
          <el-col :span="12">
            <el-descriptions title="农户信息" :column="1" border>
              <el-descriptions-item label="名称">{{ farmerInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ farmerInfo.contactPerson }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ farmerInfo.contactPhone }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ farmerInfo.address }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </el-card>
      
      <!-- 物流信息 -->
      <el-card class="logistics-card" v-if="logisticsInfo && logisticsInfo.id">
        <template #header>
          <div class="card-header">
            <span>物流信息</span>
            <el-tag :type="getLogisticsStatusType(logisticsInfo.status)">{{ getLogisticsStatusText(logisticsInfo.status) }}</el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物流公司">{{ logisticsInfo.logisticsCompany }}</el-descriptions-item>
          <el-descriptions-item label="物流单号">{{ logisticsInfo.trackingNo }}</el-descriptions-item>
          <el-descriptions-item label="运输方式">{{ logisticsInfo.transportType }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ logisticsInfo.departureTime }}</el-descriptions-item>
          <el-descriptions-item label="预计到达时间">{{ logisticsInfo.arrivalTime || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ logisticsInfo.createTime }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="logistics-actions" v-if="logisticsInfo.status === 'SHIPPED' || logisticsInfo.status === 'TRANSIT'">
          <el-button type="primary" @click="showAddTraceDialog">添加物流轨迹</el-button>
        </div>
      </el-card>
      
      <!-- 物流轨迹 -->
      <el-card class="logistics-trace-card" v-if="logisticsTraces.length > 0">
        <template #header>
          <span>物流轨迹</span>
        </template>
        
        <el-timeline>
          <el-timeline-item
            v-for="(trace, index) in logisticsTraces"
            :key="trace.id"
            :timestamp="trace.nodeTime"
            :type="index === 0 ? 'primary' : 'info'"
          >
            <div class="trace-content">
              <div class="trace-location">{{ trace.nodeLocation }}</div>
              <div class="trace-desc">{{ trace.nodeDesc }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
    
    <!-- 创建物流信息对话框 -->
    <el-dialog
      v-model="createLogisticsDialogVisible"
      title="创建物流信息"
      width="600px"
    >
      <el-form
        ref="logisticsFormRef"
        :model="logisticsForm"
        :rules="logisticsRules"
        label-width="120px"
      >
        <el-form-item label="物流公司" prop="logisticsCompany">
          <el-input v-model="logisticsForm.logisticsCompany" placeholder="请输入物流公司名称"></el-input>
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model="logisticsForm.trackingNo" placeholder="请输入物流单号"></el-input>
        </el-form-item>
        <el-form-item label="运输方式" prop="transportType">
          <el-select v-model="logisticsForm.transportType" placeholder="请选择运输方式" style="width: 100%">
            <el-option label="陆运" value="陆运"></el-option>
            <el-option label="海运" value="海运"></el-option>
            <el-option label="空运" value="空运"></el-option>
            <el-option label="铁路" value="铁路"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="实际交货数量" prop="actualQuantity">
          <el-input-number
            v-model="logisticsForm.actualQuantity"
            :min="1"
            :max="order.quantity"
            style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="检验结果" prop="inspectionResult">
          <el-input
            v-model="logisticsForm.inspectionResult"
            type="textarea"
            :rows="3"
            placeholder="请输入产品检验结果"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createLogisticsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="createLogistics">确认创建</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加物流轨迹对话框 -->
    <el-dialog
      v-model="addTraceDialogVisible"
      title="添加物流轨迹"
      width="600px"
    >
      <el-form
        ref="traceFormRef"
        :model="traceForm"
        :rules="traceRules"
        label-width="120px"
      >
        <el-form-item label="节点时间" prop="nodeTime">
          <el-date-picker
            v-model="traceForm.nodeTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="节点位置" prop="nodeLocation">
          <el-input v-model="traceForm.nodeLocation" placeholder="请输入节点位置"></el-input>
        </el-form-item>
        <el-form-item label="节点描述" prop="nodeDesc">
          <el-input
            v-model="traceForm.nodeDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入节点描述"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addTraceDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addTrace">确认添加</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipGoodsDialogVisible"
      title="发货"
      width="600px"
    >
      <el-form
        ref="shipGoodsFormRef"
        :model="shipGoodsForm"
        :rules="shipGoodsRules"
        label-width="120px"
      >
        <el-form-item label="发货时间" prop="departureTime">
          <el-date-picker
            v-model="shipGoodsForm.departureTime"
            type="datetime"
            placeholder="选择发货时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="预计到达时间" prop="arrivalTime">
          <el-date-picker
            v-model="shipGoodsForm.arrivalTime"
            type="datetime"
            placeholder="选择预计到达时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="发货备注" prop="departureNote">
          <el-input
            v-model="shipGoodsForm.departureNote"
            type="textarea"
            :rows="3"
            placeholder="请输入发货备注"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipGoodsDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="shipGoods">确认发货</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { farmerAPI } from '../../api';

const route = useRoute();
const router = useRouter();
const orderId = route.params.id;

// 数据
const loading = ref(false);
const order = ref({});
const purchaserInfo = ref({});
const farmerInfo = ref({});
const logisticsInfo = ref(null);
const logisticsTraces = ref([]);

// 对话框状态
const createLogisticsDialogVisible = ref(false);
const addTraceDialogVisible = ref(false);
const shipGoodsDialogVisible = ref(false);

// 表单数据
const logisticsForm = ref({
  orderId: orderId,
  logisticsCompany: '',
  trackingNo: '',
  transportType: '',
  actualQuantity: null,
  inspectionResult: ''
});

const traceForm = ref({
  nodeTime: '',
  nodeLocation: '',
  nodeDesc: ''
});

const shipGoodsForm = ref({
  departureTime: '',
  arrivalTime: '',
  departureNote: ''
});

// 表单引用
const logisticsFormRef = ref(null);
const traceFormRef = ref(null);
const shipGoodsFormRef = ref(null);

// 表单验证规则
const logisticsRules = {
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

const shipGoodsRules = {
  departureTime: [{ required: true, message: '请选择发货时间', trigger: 'change' }],
  arrivalTime: [{ required: true, message: '请选择预计到达时间', trigger: 'change' }]
};

// 方法
const getStatusType = (status) => {
  const typeMap = {
    'pending': 'warning',
    'pending_inspection': 'primary',
    'paid': 'success',
    'delivered': 'info',
    'completed': 'success',
    'cancelled': 'danger',
  };
  return typeMap[status] || 'info';
};

const getStatusText = (status) => {
  const textMap = {
    'pending': '待确认',
    'pending_inspection': '待检验',
    'paid': '已支付',
    'delivered': '已交货',
    'completed': '已完成',
    'cancelled': '已取消',
  };
  return textMap[status] || status;
};

const getStepActive = (status) => {
  const stepMap = {
    'pending': 0,
    'pending_inspection': 1,
    'paid': 2,
    'delivered': 3,
    'completed': 4,
  };
  return stepMap[status] || 0;
};

const getLogisticsStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'SHIPPED': 'primary',
    'TRANSIT': 'info',
    'DELIVERED': 'success',
    'COMPLETED': 'success',
  };
  return typeMap[status] || 'info';
};

const getLogisticsStatusText = (status) => {
  const textMap = {
    'PENDING': '待发货',
    'SHIPPED': '已发货',
    'TRANSIT': '运输中',
    'DELIVERED': '已送达',
    'COMPLETED': '已完成',
  };
  return textMap[status] || status;
};

// 加载订单详情
const loadOrderDetail = async () => {
  try {
    loading.value = true;
    const response = await farmerAPI.getOrderDetail(orderId);
    order.value = response.data;
    
    // 加载物流信息
    try {
      const logisticsResponse = await farmerAPI.getLogisticsByOrderId(orderId);
      logisticsInfo.value = logisticsResponse.data;
      
      // 加载物流轨迹
      if (logisticsInfo.value && logisticsInfo.value.id) {
        const tracesResponse = await farmerAPI.getLogisticsTraces(logisticsInfo.value.id);
        logisticsTraces.value = tracesResponse.data || [];
      }
    } catch (error) {
      console.log('该订单暂无物流信息');
      logisticsInfo.value = null; // 明确设置为null，避免空对象问题
    }
    
    // TODO: 加载交易双方信息
    purchaserInfo.value = {
      name: order.value.purchaserName || '采购方名称',
      contactPerson: order.value.purchaserContact || '采购联系人',
      contactPhone: order.value.purchaserPhone || '13800138000',
      address: order.value.purchaserAddress || '采购方地址'
    };
    
    farmerInfo.value = {
      name: order.value.farmerName || '农户名称',
      contactPerson: order.value.farmerContact || '农户联系人',
      contactPhone: order.value.farmerPhone || '13900139000',
      address: order.value.farmerAddress || '农户地址'
    };
  } catch (error) {
    console.error('Load order detail error:', error);
    ElMessage.error('加载订单详情失败');
  } finally {
    loading.value = false;
  }
};

// 显示创建物流对话框
const showCreateLogisticsDialog = () => {
  logisticsForm.value.actualQuantity = order.value.quantity;
  createLogisticsDialogVisible.value = true;
};

// 创建物流信息
const createLogistics = async () => {
  try {
    await logisticsFormRef.value.validate();
    
    await farmerAPI.createLogistics(logisticsForm.value);
    ElMessage.success('创建物流信息成功');
    createLogisticsDialogVisible.value = false;
    
    // 重新加载物流信息
    const logisticsResponse = await farmerAPI.getLogisticsByOrderId(orderId);
    logisticsInfo.value = logisticsResponse.data;
    
    // 重新加载订单详情
    await loadOrderDetail();
  } catch (error) {
    console.error('Create logistics error:', error);
    ElMessage.error('创建物流信息失败');
  }
};

// 显示添加物流轨迹对话框
const showAddTraceDialog = () => {
  traceForm.value = {
    nodeTime: '',
    nodeLocation: '',
    nodeDesc: ''
  };
  addTraceDialogVisible.value = true;
};

// 添加物流轨迹
const addTrace = async () => {
  try {
    await traceFormRef.value.validate();
    
    if (!logisticsInfo.value || !logisticsInfo.value.id) {
      ElMessage.error('物流信息不存在');
      return;
    }
    
    await farmerAPI.addLogisticsTrace(logisticsInfo.value.id, traceForm.value);
    ElMessage.success('添加物流轨迹成功');
    addTraceDialogVisible.value = false;
    
    // 重新加载物流轨迹
    const tracesResponse = await farmerAPI.getLogisticsTraces(logisticsInfo.value.id);
    logisticsTraces.value = tracesResponse.data || [];
  } catch (error) {
    console.error('Add trace error:', error);
    ElMessage.error('添加物流轨迹失败');
  }
};

// 显示发货对话框
const showShipGoodsDialog = () => {
  shipGoodsForm.value = {
    departureTime: '',
    arrivalTime: '',
    departureNote: ''
  };
  shipGoodsDialogVisible.value = true;
};

// 发货
const shipGoods = async () => {
  try {
    await shipGoodsFormRef.value.validate();
    
    if (!logisticsInfo.value || !logisticsInfo.value.id) {
      ElMessage.error('物流信息不存在');
      return;
    }
    
    // 格式化日期时间
    const formattedData = {
      ...shipGoodsForm.value,
      departureTime: shipGoodsForm.value.departureTime.toISOString(),
      arrivalTime: shipGoodsForm.value.arrivalTime.toISOString()
    };
    
    await farmerAPI.shipGoods(logisticsInfo.value.id, formattedData);
    ElMessage.success('发货成功');
    shipGoodsDialogVisible.value = false;
    
    // 重新加载物流信息和订单详情
    const logisticsResponse = await farmerAPI.getLogisticsByOrderId(orderId);
    logisticsInfo.value = logisticsResponse.data;
    
    await loadOrderDetail();
  } catch (error) {
    console.error('Ship goods error:', error);
    ElMessage.error('发货失败');
  }
};

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后不可恢复', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    // TODO: 调用取消订单API
    // await farmerAPI.cancelOrder(orderId);
    ElMessage.success('订单已取消');
    
    // 重新加载订单详情
    await loadOrderDetail();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Cancel order error:', error);
      ElMessage.error('取消订单失败');
    }
  }
};

// 再次下单
const createNewOrder = () => {
  // TODO: 实现再次下单逻辑
  ElMessage.info('再次下单功能开发中');
};

// 生命周期
onMounted(() => {
  loadOrderDetail();
});
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.order-container {
  max-width: 1200px;
  margin: 0 auto;
}

.order-status-card {
  margin-bottom: 20px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-header h2 {
  margin: 0;
}

.order-info-card {
  margin-bottom: 20px;
}

.parties-info-card {
  margin-bottom: 20px;
}

.logistics-card {
  margin-bottom: 20px;
}

.logistics-trace-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-actions {
  margin-top: 20px;
  text-align: center;
}

.logistics-actions {
  margin-top: 20px;
  text-align: center;
}

.trace-content {
  padding: 5px 0;
}

.trace-location {
  font-weight: bold;
  margin-bottom: 5px;
}

.trace-desc {
  color: #666;
}
</style>