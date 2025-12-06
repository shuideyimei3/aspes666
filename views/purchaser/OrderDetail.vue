<template>
  <div class="order-detail">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser/orders' }">订单管理</el-breadcrumb-item>
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
          <el-step title="待处理" description="订单已创建，等待处理"></el-step>
          <el-step title="待检验" description="商品待检验"></el-step>
          <el-step title="已送达" description="商品已送达，等待确认收货"></el-step>
          <el-step title="已支付" description="订单已支付"></el-step>
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
          <el-descriptions-item label="采购数量">{{ order.actualQuantity }} {{ order.productInfo?.unit || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ order.actualAmount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="实际数量">{{ order.actualQuantity || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ order.remark || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ order.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="交付时间" v-if="order.deliveryTime">{{ order.deliveryTime }}</el-descriptions-item>
          <el-descriptions-item label="检验结果" v-if="order.inspectionResult">{{ order.inspectionResult }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="order-actions" v-if="order.status === 'pending'">
          <el-button type="primary" @click="confirmOrder">确认订单</el-button>
        </div>
        <div class="order-actions" v-else-if="order.status === 'pending_inspection'">
          <el-button type="primary" @click="showPayDialog">立即支付</el-button>
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
              <el-descriptions-item label="联系电话">{{ farmerInfo.phone }}</el-descriptions-item>
              <el-descriptions-item label="地址">{{ farmerInfo.address }}</el-descriptions-item>
            </el-descriptions>
          </el-col>
        </el-row>
      </el-card>
      
      <el-card class="contract-card" v-if="contractInfo.id">
        <template #header>
          <div class="card-header">
            <span>合同信息</span>
            <div>
              <el-button 
                v-if="order.status === 'pending' && !contractInfo.purchaserSignTime" 
                type="primary" 
                @click="showSignContractDialog"
              >
                签署合同
              </el-button>
              <el-button 
                v-if="contractInfo.contractFileUrl" 
                @click="downloadContract"
              >
                下载合同
              </el-button>
            </div>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="合同编号">{{ contractInfo.contractNo }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(contractInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="农户签署时间">
            {{ contractInfo.farmerSignTime ? formatDate(contractInfo.farmerSignTime) : '未签署' }}
          </el-descriptions-item>
          <el-descriptions-item label="采购方签署时间">
            {{ contractInfo.purchaserSignTime ? formatDate(contractInfo.purchaserSignTime) : '未签署' }}
          </el-descriptions-item>
          <el-descriptions-item label="合同状态">
            <el-tag :type="getContractStatusType(contractInfo.status)">
              {{ getContractStatusText(contractInfo.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <el-card class="payment-card" v-if="paymentInfo.id">
        <template #header>
          <span>支付信息</span>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="支付编号">{{ paymentInfo.paymentNo }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">¥{{ paymentInfo.amount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ getPaymentMethodText(paymentInfo.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDate(paymentInfo.paymentTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="getPaymentStatusType(paymentInfo.status)">
              {{ getPaymentStatusText(paymentInfo.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <el-card class="logistics-card" v-if="logisticsInfo.id">
        <template #header>
          <div class="card-header">
            <span>物流信息</span>
            <el-button type="primary" size="small" @click="fetchAllLogistics">查看全部物流信息</el-button>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物流单号">{{ logisticsInfo.trackingNo }}</el-descriptions-item>
          <el-descriptions-item label="物流公司">{{ logisticsInfo.logisticsCompany }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ formatDate(logisticsInfo.departureTime) }}</el-descriptions-item>
          <el-descriptions-item label="预计送达时间">{{ formatDate(logisticsInfo.arrivalTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="logistics-timeline" v-if="logisticsTraces.length > 0">
          <h3>物流轨迹</h3>
          <el-timeline>
            <el-timeline-item
              v-for="(trace, index) in logisticsTraces"
              :key="index"
              :timestamp="formatDate(trace.traceTime)"
              :type="index === 0 ? 'primary' : ''"
            >
              {{ trace.description }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
      
      <!-- 确认收货按钮 -->
      <div class="order-actions" v-if="order.status === 'delivered'">
        <el-button type="success" @click="confirmReceipt">确认收货</el-button>
      </div>
    </div>
    
    <!-- 签署合同对话框 -->
    <el-dialog v-model="signContractDialogVisible" title="签署合同" width="500px">
      <el-form :model="signContractForm" :rules="signContractRules" ref="signContractFormRef" label-width="100px">
        <el-form-item label="签名文件" prop="signFile">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
            accept=".jpg,.jpeg,.png,.pdf"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传签名文件，支持jpg、png、pdf格式，文件大小不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="signContractDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSignContract">确认签署</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 支付对话框 -->
    <el-dialog v-model="payDialogVisible" title="支付订单" width="500px" :close-on-click-modal="false">
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
            <el-radio label="BANK_TRANSFER">银行转账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="支付凭证">
          <el-upload
            ref="payUploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handlePayFileChange"
            :file-list="payFileList"
            accept=".jpg,.jpeg,.png,.pdf"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传支付凭证，支持jpg、png、pdf格式，文件大小不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="payDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPayment">确认支付</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 全部物流信息对话框 -->
    <el-dialog v-model="allLogisticsDialogVisible" title="全部物流信息" width="800px">
      <div v-loading="allLogisticsLoading">
        <div v-if="allLogisticsList.length === 0" class="empty-logistics">
          <el-empty description="暂无物流信息" />
        </div>
        <div v-else>
          <el-timeline>
            <el-timeline-item
              v-for="(logistics, index) in allLogisticsList"
              :key="logistics.id"
              :timestamp="formatDate(logistics.createTime)"
              :type="index === 0 ? 'primary' : ''"
            >
              <el-card class="logistics-item">
                <template #header>
                  <div class="logistics-header">
                    <span>物流单号: {{ logistics.trackingNo }}</span>
                    <el-tag :type="getLogisticsStatusType(logistics.status)">
                      {{ getLogisticsStatusText(logistics.status) }}
                    </el-tag>
                  </div>
                </template>
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="物流公司">{{ logistics.logisticsCompany }}</el-descriptions-item>
                  <el-descriptions-item label="发货时间">{{ formatDate(logistics.departureTime) }}</el-descriptions-item>
                  <el-descriptions-item label="预计送达时间">{{ formatDate(logistics.arrivalTime) }}</el-descriptions-item>
                  <el-descriptions-item label="实际送达时间">{{ formatDate(logistics.arrivalTime) }}</el-descriptions-item>
                </el-descriptions>
                
                <div class="logistics-traces" v-if="logistics.traces && logistics.traces.length > 0">
                  <h4>物流轨迹</h4>
                  <el-timeline>
                    <el-timeline-item
                      v-for="(trace, traceIndex) in logistics.traces"
                      :key="traceIndex"
                      :timestamp="formatDate(trace.traceTime)"
                      :type="traceIndex === 0 ? 'primary' : ''"
                      placement="top"
                    >
                      <el-card>
                        <h4>{{ trace.description }}</h4>
                        <p v-if="trace.location">位置: {{ trace.location }}</p>
                      </el-card>
                    </el-timeline-item>
                  </el-timeline>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="allLogisticsDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { purchaserAPI } from '/api/index';

const route = useRoute();
const router = useRouter();

const orderId = ref(route.params.id);
const order = ref({});
const purchaserInfo = ref({});
const farmerInfo = ref({});
const contractInfo = ref({});
const paymentInfo = ref({});
const logisticsInfo = ref({})
const logisticsTraces = ref([])
const allLogisticsDialogVisible = ref(false)
const allLogisticsList = ref([])
const allLogisticsLoading = ref(false);
const loading = ref(true);

// 签署合同相关
const signContractDialogVisible = ref(false);
const signContractFormRef = ref(null);
const signContractForm = ref({
  signFile: null
});
const signContractRules = {
  signFile: [{ required: true, message: '请上传签名文件', trigger: 'change' }]
};
const fileList = ref([]);

// 支付相关
const payDialogVisible = ref(false);
const payFormRef = ref(null);
const payForm = ref({
  id: '',
  orderNo: '',
  amount: '',
  paymentMethod: 'ALIPAY',
  voucherFile: null
});
const payRules = {
  paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
};
const payFileList = ref([]);

// 获取全部物流信息
const fetchAllLogistics = async () => {
  allLogisticsDialogVisible.value = true
  allLogisticsLoading.value = true
  
  try {
    // 使用 farmerAPI.getAllLogisticsByOrderId 获取订单的所有物流信息
    const response = await farmerAPI.getAllLogisticsByOrderId(orderId.value)
    
    if (response.code === 200) {
      allLogisticsList.value = response.data || []
      
      // 为每个物流记录获取轨迹信息
      for (const logistics of allLogisticsList.value) {
        try {
          const traceResponse = await farmerAPI.getLogisticsTraces(logistics.id)
          if (traceResponse.code === 200) {
            logistics.traces = traceResponse.data || []
          }
        } catch (error) {
          console.error('获取物流轨迹失败:', error)
          logistics.traces = []
        }
      }
    } else {
      ElMessage.error(response.message || '获取物流信息失败')
    }
  } catch (error) {
    console.error('获取物流信息失败:', error)
    ElMessage.error('获取物流信息失败')
  } finally {
    allLogisticsLoading.value = false
  }
}

// 确认收货
const confirmReceipt = async () => {
  try {
    ElMessageBox.confirm('确认已收到货物？此操作不可撤销。', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      try {
        // 使用 purchaserAPI.completeOrder 确认订单完成
        const response = await purchaserAPI.completeOrder(orderId.value)
        
        if (response.code === 200) {
          ElMessage.success('确认收货成功')
          // 重新加载订单详情
          await fetchOrderDetail()
        } else {
          ElMessage.error(response.message || '确认收货失败')
        }
      } catch (error) {
        console.error('确认收货失败:', error)
        ElMessage.error('确认收货失败')
      }
    }).catch(() => {
      // 用户取消操作
    })
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 获取物流状态类型
const getLogisticsStatusType = (status) => {
  switch (status) {
    case 'CREATED':
      return 'info'
    case 'PENDING':
      return 'warning'
    case 'SHIPPED':
      return 'primary'
    case 'DELIVERED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取物流状态文本
const getLogisticsStatusText = (status) => {
  switch (status) {
    case 'CREATED':
      return '已创建'
    case 'PENDING':
      return '待发货'
    case 'SHIPPED':
      return '已发货'
    case 'DELIVERED':
      return '已送达'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知状态'
  }
}
const fetchOrderDetail = async () => {
  try {
    loading.value = true;
    const response = await purchaserAPI.getOrderDetail(orderId.value);
    
    console.log('API返回的订单详情数据:', response);
    
    if (response.code === 200) {
      // API返回的数据结构中，订单信息直接在data中
      order.value = response.data || {};
      
      console.log('订单状态:', order.value.status);
      
      // 如果API返回的是扁平结构，我们需要提取相关信息
      // 根据实际API返回结构调整
      purchaserInfo.value = response.data.purchaserInfo || {};
      farmerInfo.value = response.data.farmerInfo || {};
      contractInfo.value = response.data.contractInfo || {};
      paymentInfo.value = response.data.paymentInfo || {};
      logisticsInfo.value = response.data.logisticsInfo || {};
      logisticsTraces.value = response.data.logisticsTraces || [];
    } else {
      ElMessage.error(response.message || '获取订单详情失败');
      router.push('/purchaser/orders');
    }
  } catch (error) {
    console.error('获取订单详情失败:', error);
    ElMessage.error('获取订单详情失败');
  } finally {
    loading.value = false;
  }
};

// 确认订单
const confirmOrder = () => {
  ElMessageBox.confirm(
    '确认此订单？确认后将进入合同签署阶段。',
    '确认订单',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const response = await purchaserAPI.completeOrder(orderId.value);
      if (response.code === 200) {
        ElMessage.success('订单确认成功');
        fetchOrderDetail(); // 重新获取订单详情
      } else {
        ElMessage.error(response.message || '确认订单失败');
      }
    } catch (error) {
      console.error('确认订单失败:', error);
      ElMessage.error('确认订单失败');
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 显示签署合同对话框
const showSignContractDialog = () => {
  signContractDialogVisible.value = true;
  fileList.value = [];
};

// 处理文件变化
const handleFileChange = (file) => {
  signContractForm.value.signFile = file.raw;
};

// 提交签署合同
const submitSignContract = async () => {
  if (!signContractFormRef.value) return;
  
  try {
    await signContractFormRef.value.validate();
    
    const response = await purchaserAPI.signContract(
      contractInfo.value.id,
      signContractForm.value.signFile
    );
    
    if (response.code === 200) {
      ElMessage.success('合同签署成功');
      signContractDialogVisible.value = false;
      fetchOrderDetail(); // 重新获取订单详情
    } else {
      ElMessage.error(response.message || '签署合同失败');
    }
  } catch (error) {
    console.error('签署合同失败:', error);
    ElMessage.error('签署合同失败');
  }
};

// 下载合同
const downloadContract = () => {
  if (contractInfo.value.contractFileUrl) {
    window.open(contractInfo.value.contractFileUrl, '_blank');
  } else {
    ElMessage.warning('合同文件不存在');
  }
};

// 处理支付凭证文件变化
const handlePayFileChange = (file) => {
  payForm.value.voucherFile = file.raw;
};

// 显示支付对话框
const showPayDialog = () => {
  payForm.value.id = order.value.id;
      payForm.value.orderNo = order.value.orderNo;
    payForm.value.amount = order.value.actualAmount;
  payForm.value.paymentMethod = 'ALIPAY';
  payForm.value.voucherFile = null;
  payFileList.value = [];
  payDialogVisible.value = true;
};

// 提交支付
const submitPayment = async () => {
  if (!payFormRef.value) return;
  
  try {
    await payFormRef.value.validate();
    
    // 根据后端PaymentRequest的要求构建支付数据
    const paymentData = {
      orderId: payForm.value.id, // 使用orderId字段，与后端匹配
      paymentStage: 'FINAL_PAYMENT', // 添加支付阶段字段
      amount: payForm.value.amount,
      paymentMethod: payForm.value.paymentMethod // 保持大写的支付方式枚举值
    };
    
    // 如果有支付凭证文件，添加到FormData中
    if (payForm.value.voucherFile) {
      paymentData.voucherFile = payForm.value.voucherFile;
    }
    
    // 直接使用submitPayment函数提交支付
    const paymentResponse = await purchaserAPI.submitPayment(paymentData);
    
    if (paymentResponse.code === 200) {
      ElMessage.success('支付请求已提交，请完成支付');
      payDialogVisible.value = false;
      
      // 如果有支付链接，跳转到支付页面
      if (paymentResponse.data && paymentResponse.data.paymentUrl) {
        window.open(paymentResponse.data.paymentUrl, '_blank');
      }
      
      fetchOrderDetail(); // 重新获取订单详情
    } else {
      ElMessage.error(paymentResponse.message || '支付失败');
    }
  } catch (error) {
    console.error('支付失败:', error);
    ElMessage.error('支付失败');
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning';
    case 'pending_inspection': return 'warning';
    case 'delivered': return 'primary';
    case 'paid': return 'primary';
    case 'completed': return 'success';
    case 'cancelled': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理';
    case 'pending_inspection': return '待检验';
    case 'delivered': return '已送达';
    case 'paid': return '已支付';
    case 'completed': return '已完成';
    case 'cancelled': return '已取消';
    default: return '未知状态';
  }
};

// 获取步骤激活状态
const getStepActive = (status) => {
  switch (status) {
    case 'pending': return 0;
    case 'pending_inspection': return 1;
    case 'delivered': return 2;
    case 'paid': return 3;
    case 'completed': return 4;
    default: return -1;
  }
};

// 获取合同状态类型
const getContractStatusType = (status) => {
  switch (status) {
    case 'DRAFT': return 'info';
    case 'PENDING_SIGN': return 'warning';
    case 'SIGNED': return 'success';
    case 'TERMINATED': return 'danger';
    default: return 'info';
  }
};

// 获取合同状态文本
const getContractStatusText = (status) => {
  switch (status) {
    case 'DRAFT': return '草稿';
    case 'PENDING_SIGN': return '待签署';
    case 'SIGNED': return '已签署';
    case 'TERMINATED': return '已终止';
    default: return '未知状态';
  }
};

// 获取支付方式文本
const getPaymentMethodText = (method) => {
  switch (method) {
    case 'ALIPAY': return '支付宝';
    case 'WECHAT': return '微信支付';
    case 'BANK_TRANSFER': return '银行转账';
    case 'BALANCE': return '余额支付';
    default: return '未知方式';
  }
};

// 获取支付状态类型
const getPaymentStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'SUCCESS': return 'success';
    case 'FAILED': return 'danger';
    default: return 'info';
  }
};

// 获取支付状态文本
const getPaymentStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待支付';
    case 'SUCCESS': return '支付成功';
    case 'FAILED': return '支付失败';
    default: return '未知状态';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchOrderDetail();
});
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.order-container {
  margin-top: 20px;
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
  font-size: 20px;
  color: #303133;
}

.order-info-card,
.parties-info-card,
.contract-card,
.payment-card,
.logistics-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logistics-timeline {
  margin-top: 20px;
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

.empty-logistics {
  padding: 20px 0;
  text-align: center;
}

.logistics-item {
  margin-bottom: 20px;
}

.logistics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logistics-traces {
  margin-top: 15px;
}

.logistics-traces h4 {
  margin-bottom: 10px;
  color: #606266;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-descriptions) {
  margin-bottom: 0;
}

:deep(.el-steps) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .status-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>