<template>
  <div class="docking-detail-page">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/purchaser' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/purchaser/dockings' }">对接记录</el-breadcrumb-item>
      <el-breadcrumb-item>对接详情</el-breadcrumb-item>
    </el-breadcrumb>
    
    <div v-loading="loading" class="detail-container">
      <!-- 对接基本信息 -->
      <el-card class="detail-card" v-if="dockingDetail">
        <template #header>
          <div class="card-header">
            <span>对接信息</span>
            <el-tag :type="getStatusType(dockingDetail.status)">
              {{ getStatusText(dockingDetail.status) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="对接ID">{{ dockingDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="对接时间">{{ formatDate(dockingDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="农户名称">{{ dockingDetail.farmerName }}</el-descriptions-item>
          <el-descriptions-item label="农场名称">{{ dockingDetail.farmName }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ dockingDetail.demandProductName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联需求">{{ dockingDetail.demandTitle }}</el-descriptions-item>
          <el-descriptions-item label="报价">{{ dockingDetail.quotePrice }}元</el-descriptions-item>
          <el-descriptions-item label="可供应量">{{ dockingDetail.canSupply }}</el-descriptions-item>
          <el-descriptions-item label="供应时间">{{ dockingDetail.supplyTime }}</el-descriptions-item>
          <el-descriptions-item label="农户联系方式">{{ dockingDetail.farmerContact }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 操作按钮 -->
        <div class="action-buttons" v-if="dockingDetail.status === 'PENDING' || dockingDetail.status === 'IN_PROGRESS'">
          <el-button type="success" @click="showHandleDialog('ACCEPT')">接受对接</el-button>
          <el-button type="danger" @click="showHandleDialog('REJECT')">拒绝对接</el-button>
        </div>
      </el-card>
      
      <!-- 消息记录 -->
      <el-card class="message-card">
        <template #header>
          <div class="card-header">
            <span>消息记录</span>
            <el-button type="primary" @click="showMessageDialog">发送消息</el-button>
          </div>
        </template>
        
        <div class="message-list" v-if="messages.length > 0">
          <div 
            v-for="message in messages" 
            :key="message.id" 
            :class="['message-item', message.senderRole === 'PURCHASER' ? 'message-sent' : 'message-received']"
          >
            <div class="message-header">
              <span class="sender-name">{{ message.senderName }}</span>
              <span class="message-time">{{ formatDate(message.createTime) }}</span>
            </div>
            <div class="message-content">{{ message.content }}</div>
          </div>
        </div>
        
        <el-empty v-else description="暂无消息记录" />
      </el-card>
    </div>
    
    <!-- 处理对接对话框 -->
    <el-dialog 
      v-model="handleDialogVisible" 
      :title="handleAction === 'ACCEPT' ? '接受对接' : '拒绝对接'" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="handleForm" :rules="handleRules" ref="handleFormRef" label-width="80px">
        <el-form-item label="处理意见" prop="remark">
          <el-input 
            v-model="handleForm.remark" 
            type="textarea" 
            :rows="4" 
            :placeholder="handleAction === 'ACCEPT' ? '请输入接受对接的意见' : '请输入拒绝对接的理由'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHandle">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 发送消息对话框 -->
    <el-dialog 
      v-model="messageDialogVisible" 
      title="发送消息" 
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="messageForm" :rules="messageRules" ref="messageFormRef" label-width="80px">
        <el-form-item label="接收方">
          <el-input v-model="messageForm.receiverName" disabled />
        </el-form-item>
        <el-form-item label="消息内容" prop="content">
          <el-input 
            v-model="messageForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入消息内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="messageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMessage">发送</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { purchaserAPI } from '/api/index';

const route = useRoute();
const router = useRouter();

// 对接详情
const dockingDetail = ref(null);
const loading = ref(false);
const messages = ref([]);

// 处理对接对话框
const handleDialogVisible = ref(false);
const handleFormRef = ref(null);
const handleAction = ref('');
const handleForm = reactive({
  remark: ''
});
const handleRules = {
  remark: [
    { required: true, message: '请输入处理意见', trigger: 'blur' },
    { min: 5, max: 500, message: '处理意见长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 消息对话框
const messageDialogVisible = ref(false);
const messageFormRef = ref(null);
const messageForm = reactive({
  dockingId: '',
  receiverId: '',
  receiverName: '',
  content: ''
});
const messageRules = {
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 5, max: 500, message: '消息内容长度在5到500个字符之间', trigger: 'blur' }
  ]
};

// 获取对接详情
const fetchDockingDetail = async () => {
  try {
    loading.value = true;
    const dockingId = route.params.id;
    
    const response = await purchaserAPI.getDockingDetail(dockingId);
    
    if (response.code === 200) {
      dockingDetail.value = response.data;
      // 设置消息表单的接收方信息
      messageForm.dockingId = dockingId;
      messageForm.receiverId = response.data.farmerId;
      messageForm.receiverName = response.data.farmerName;
      
      // 获取消息记录
      await fetchMessages(dockingId);
    } else {
      ElMessage.error(response.message || '获取对接详情失败');
    }
  } catch (error) {
    console.error('获取对接详情失败:', error);
    ElMessage.error('获取对接详情失败');
  } finally {
    loading.value = false;
  }
};

// 获取消息记录
const fetchMessages = async (dockingId) => {
  try {
    const response = await purchaserAPI.getDockingMessages(dockingId);
    
    if (response.code === 200) {
      messages.value = response.data || [];
    } else {
      ElMessage.error(response.message || '获取消息记录失败');
    }
  } catch (error) {
    console.error('获取消息记录失败:', error);
    ElMessage.error('获取消息记录失败');
  }
};

// 显示处理对接对话框
const showHandleDialog = (action) => {
  handleAction.value = action;
  handleForm.remark = '';
  handleDialogVisible.value = true;
};

// 提交处理对接
const submitHandle = async () => {
  if (!handleFormRef.value) return;
  
  try {
    await handleFormRef.value.validate();
    
    const dockingId = dockingDetail.value.id;
    const data = {
      action: handleAction.value,
      remark: handleForm.remark
    };
    
    const response = await purchaserAPI.handleDocking(dockingId, data);
    
    if (response.code === 200) {
      ElMessage.success(`对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}成功`);
      handleDialogVisible.value = false;
      // 重新获取对接详情
      await fetchDockingDetail();
    } else {
      ElMessage.error(response.message || `对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}失败`);
    }
  } catch (error) {
    console.error(`对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}失败:`, error);
    ElMessage.error(`对接${handleAction.value === 'ACCEPT' ? '接受' : '拒绝'}失败`);
  }
};

// 显示消息对话框
const showMessageDialog = () => {
  messageForm.content = '';
  messageDialogVisible.value = true;
};

// 提交消息
const submitMessage = async () => {
  if (!messageFormRef.value) return;
  
  try {
    await messageFormRef.value.validate();
    
    const response = await purchaserAPI.sendDockingMessage({
      dockingId: messageForm.dockingId,
      receiverId: messageForm.receiverId,
      content: messageForm.content
    });
    
    if (response.code === 200) {
      ElMessage.success('消息发送成功');
      messageDialogVisible.value = false;
      // 重新获取消息记录
      await fetchMessages(messageForm.dockingId);
    } else {
      ElMessage.error(response.message || '消息发送失败');
    }
  } catch (error) {
    console.error('消息发送失败:', error);
    ElMessage.error('消息发送失败');
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning';
    case 'IN_PROGRESS': return 'primary';
    case 'COMPLETED': return 'success';
    case 'CANCELLED': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待回复';
    case 'IN_PROGRESS': return '对接中';
    case 'COMPLETED': return '已完成';
    case 'CANCELLED': return '已取消';
    default: return '未知状态';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

onMounted(() => {
  fetchDockingDetail();
});
</script>

<style scoped>
.docking-detail-page {
  padding: 20px;
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-card, .message-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.message-list {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px 0;
}

.message-item {
  margin-bottom: 15px;
  padding: 10px 15px;
  border-radius: 8px;
}

.message-sent {
  background-color: #e1f3ff;
  margin-left: 20%;
  text-align: right;
}

.message-received {
  background-color: #f5f5f5;
  margin-right: 20%;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.sender-name {
  font-weight: bold;
}

.message-content {
  word-break: break-word;
}

:deep(.el-breadcrumb) {
  margin-bottom: 20px;
}

:deep(.el-descriptions) {
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .message-sent {
    margin-left: 10%;
  }
  
  .message-received {
    margin-right: 10%;
  }
}
</style>
