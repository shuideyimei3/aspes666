<template>
  <div class="statistics">
    <h2>数据统计</h2>
    
    <!-- 平台数据统计 -->
    <el-card class="platform-stats-card">
      <template #header>
        <div class="card-header">
          <span>平台数据统计</span>
          <el-button type="primary" @click="refreshPlatformStats" :loading="loadingPlatformStats">
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      
      <el-row :gutter="20" v-if="platformStats">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">用户总数</div>
            <div class="stat-value">{{ platformStats.totalUsers || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">农户总数</div>
            <div class="stat-value">{{ platformStats.totalFarmers || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">采购方总数</div>
            <div class="stat-value">{{ platformStats.totalPurchasers || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">产品总数</div>
            <div class="stat-value">{{ platformStats.totalProducts || 0 }}</div>
          </div>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" v-if="platformStats">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">订单总数</div>
            <div class="stat-value">{{ platformStats.totalOrders || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">今日活跃用户</div>
            <div class="stat-value">{{ platformStats.activeUsersToday || 0 }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">交易总额</div>
            <div class="stat-value">¥{{ formatAmount(platformStats.totalTransactionAmount || 0) }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">平均订单金额</div>
            <div class="stat-value">¥{{ formatAmount(platformStats.totalOrders ? (platformStats.totalTransactionAmount || 0) / platformStats.totalOrders : 0) }}</div>
          </div>
        </el-col>
      </el-row>
      
      <div v-if="!platformStats && !loadingPlatformStats" class="no-data">
        暂无数据
      </div>
      
      <div v-if="loadingPlatformStats" class="loading-container">
        <el-skeleton :rows="4" animated />
      </div>
    </el-card>
    
    <!-- 农户活跃度实时监控 -->
    <el-card class="farmer-activity-card">
      <template #header>
        <div class="card-header">
          <span>农户活跃度实时监控</span>
          <div class="activity-controls">
            <el-button type="primary" @click="restartActivityStream" :loading="connecting">
              <el-icon><Refresh /></el-icon> {{ connecting ? '连接中...' : '重新连接' }}
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="activity-status">
        <el-tag :type="connectionStatus === 'connected' ? 'success' : connectionStatus === 'error' ? 'danger' : 'warning'">
          {{ connectionStatusText }}
        </el-tag>
        <span class="last-update" v-if="lastUpdateTime">最后更新: {{ formatTime(lastUpdateTime) }}</span>
      </div>
      
      <div class="activity-content">
        <div v-if="Object.keys(farmerActivity).length === 0 && connectionStatus === 'connected'" class="no-data">
          暂无活跃农户数据
        </div>
        
        <div v-else-if="connectionStatus === 'error'" class="error-message">
          连接失败，请检查网络或重新连接
        </div>
        
        <div v-else class="activity-list">
          <div v-for="(count, city) in farmerActivity" :key="city" class="activity-item">
            <div class="city-name">{{ city }}</div>
            <div class="activity-count">
              <el-progress :percentage="getActivityPercentage(count)" :color="getActivityColor(count)" />
              <span class="count-text">{{ count }} 人活跃</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- AI总结功能 -->
    <div class="ai-summary-section">
      <div class="report-actions">
        <el-button type="primary" @click="generateSummary" :loading="generatingSummary">
          <el-icon><Document /></el-icon> 生成数据报告
        </el-button>
        <el-button type="success" @click="downloadReport" :disabled="!summaryResult" class="report-download-btn">
          <el-icon><Download /></el-icon> 下载报告
        </el-button>
      </div>
      
      <el-card class="ai-summary-card" v-if="summaryResult">
        <template #header>
          <div class="card-header">
            <span>数据总结</span>
          </div>
        </template>
        <div class="summary-result">{{ summaryResult }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh, Document, Download } from '@element-plus/icons-vue';
import { adminAPI } from '@/api';

// 平台统计数据
const platformStats = ref(null);
const loadingPlatformStats = ref(false);

// 农户活跃度数据
const farmerActivity = ref({});
const eventSource = ref(null);
const connectionStatus = ref('disconnected'); // disconnected, connecting, connected, error
const lastUpdateTime = ref(null);
const connecting = ref(false);

// AI总结功能
const generatingSummary = ref(false);
const summaryResult = ref('');

// 计算属性
const connectionStatusText = computed(() => {
  switch (connectionStatus.value) {
    case 'disconnected': return '未连接';
    case 'connecting': return '连接中...';
    case 'connected': return '已连接';
    case 'error': return '连接错误';
    default: return '未知状态';
  }
});

// 获取平台统计数据
const fetchPlatformStats = async () => {
  loadingPlatformStats.value = true;
  try {
    const response = await adminAPI.getPlatformStats();
    platformStats.value = response.data;
  } catch (error) {
    console.error('获取平台统计数据失败:', error);
    ElMessage.error('获取平台统计数据失败');
  } finally {
    loadingPlatformStats.value = false;
  }
};

// 刷新平台统计数据
const refreshPlatformStats = () => {
  fetchPlatformStats();
};

// 格式化金额
const formatAmount = (amount) => {
  if (!amount) return '0';
  return Number(amount).toLocaleString();
};

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  return new Date(timestamp).toLocaleTimeString();
};

// 获取活跃度百分比
const getActivityPercentage = (count) => {
  if (!farmerActivity.value || Object.keys(farmerActivity.value).length === 0) return 0;
  const maxCount = Math.max(...Object.values(farmerActivity.value));
  return maxCount > 0 ? Math.round((count / maxCount) * 100) : 0;
};

// 获取活跃度颜色
const getActivityColor = (count) => {
  if (count === 0) return '#909399';
  if (count < 5) return '#67c23a';
  if (count < 10) return '#e6a23c';
  if (count < 20) return '#f56c6c';
  return '#ff0000';
};

// 获取农户活跃度数据
const fetchFarmerActivity = async () => {
  try {
    const response = await adminAPI.getFarmerActivity();
    farmerActivity.value = response.data || {};
    connectionStatus.value = 'connected';
    lastUpdateTime.value = Date.now();
  } catch (error) {
    console.error('获取农户活跃度数据失败:', error);
    connectionStatus.value = 'error';
  }
};

// 启动农户活跃度流
const startActivityStream = () => {
  if (eventSource.value) {
    eventSource.value.close();
  }
  
  connectionStatus.value = 'connecting';
  
  // 立即获取一次数据
  fetchFarmerActivity();
  
  // 创建一个定时器，每5秒获取一次数据
  const pollInterval = setInterval(fetchFarmerActivity, 5000);
  
  // 保存轮询ID以便清理
  eventSource.value = { close: () => clearInterval(pollInterval) };
};

// 重新启动农户活跃度流
const restartActivityStream = () => {
  startActivityStream();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchPlatformStats();
  startActivityStream();
});

// 收集页面数据
const collectPageData = () => {
  let pageData = "";
  
  // 添加平台统计数据
  if (platformStats.value) {
    pageData += "平台统计数据：\n";
    pageData += `- 用户总数：${platformStats.value.totalUsers}人\n`;
    pageData += `- 农户总数：${platformStats.value.totalFarmers}人\n`;
    pageData += `- 采购方总数：${platformStats.value.totalPurchasers}人\n`;
    pageData += `- 产品总数：${platformStats.value.totalProducts}个\n`;
    pageData += `- 订单总数：${platformStats.value.totalOrders}个\n`;
    pageData += `- 今日活跃用户：${platformStats.value.activeUsersToday}人\n`;
    pageData += `- 交易总额：¥${formatAmount(platformStats.value.totalTransactionAmount)}\n`;
    pageData += `- 平均订单金额：¥${formatAmount(platformStats.value.totalOrders ? (platformStats.value.totalTransactionAmount || 0) / platformStats.value.totalOrders : 0)}\n\n`;
  }
  
  // 添加农户活跃度数据
  if (Object.keys(farmerActivity.value).length > 0) {
    pageData += "农户活跃度实时监控：\n";
    pageData += `- 连接状态：${connectionStatusText.value}\n`;
    if (lastUpdateTime.value) {
      pageData += `- 最后更新时间：${formatTime(lastUpdateTime.value)}\n`;
    }
    pageData += "- 各城市活跃农户数量：\n";
    for (const [city, count] of Object.entries(farmerActivity.value)) {
      pageData += `  * ${city}：${count}人活跃\n`;
    }
  }
  
  return pageData;
};

// 生成AI总结
const generateSummary = async () => {
  generatingSummary.value = true;
  summaryResult.value = '';
  
  try {
    // 收集页面数据
    const pageData = collectPageData();
    
    if (!pageData) {
      ElMessage.warning('暂无数据可总结');
      return;
    }
    
    // 调用AI接口
    const apiKey = '2a6551802da1439682b919fa139dfbe1.3LGHlZnKCsxs98Ub';
    const response = await fetch('https://open.bigmodel.cn/api/paas/v4/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: 'glm-3-turbo',
        messages: [
          {
            role: 'system',
            content: '你是一位专业的数据分析报告撰写者，请根据以下农业平台的统计数据，生成一份结构化的数据分析报告。报告应包含以下部分：1. 报告概述（报告目的、数据来源、统计时间）；2. 平台核心指标分析（用户、农户、采购方、产品、订单等）；3. 农户活跃度分析（各城市活跃情况）；4. 关键发现与结论；5. 建议与优化方向。请使用正式的报告语言，结构清晰，数据准确，突出关键信息和趋势。'
          },
          {
            role: 'user',
            content: pageData
          }
        ]
      })
    });
    
    if (!response.ok) {
      throw new Error('API请求失败');
    }
    
    const data = await response.json();
    summaryResult.value = data.choices[0].message.content;
    ElMessage.success('数据报告生成成功');
  } catch (error) {
    console.error('生成总结失败:', error);
    ElMessage.error('生成总结失败，请稍后重试');
  } finally {
    generatingSummary.value = false;
  }
};

// 下载报告
const downloadReport = () => {
  if (!summaryResult.value) {
    ElMessage.warning('暂无报告可下载');
    return;
  }
  
  try {
    // 添加报告标题和生成时间
    const reportTitle = '农业平台数据统计报告';
    const generateTime = new Date().toLocaleString();
    const reportContent = `${reportTitle}\n\n生成时间：${generateTime}\n\n${summaryResult.value}`;
    
    // 创建Blob对象
    const blob = new Blob([reportContent], { type: 'text/plain;charset=utf-8' });
    
    // 创建下载链接
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `农业平台数据统计报告_${new Date().toISOString().slice(0, 10)}.txt`;
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('报告下载成功');
  } catch (error) {
    console.error('报告下载失败:', error);
    ElMessage.error('报告下载失败，请稍后重试');
  }
};

// 组件卸载时清理资源
onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close();
  }
});
</script>

<style scoped>
.statistics {
  padding: 20px;
}

.platform-stats-card,
.farmer-activity-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-controls {
  display: flex;
  align-items: center;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 20px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.activity-status {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.last-update {
  margin-left: 10px;
  font-size: 12px;
  color: #999;
}

.activity-content {
  min-height: 200px;
}

.no-data,
.error-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.activity-item {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.city-name {
  font-weight: bold;
  margin-bottom: 10px;
}

.activity-count {
  display: flex;
  align-items: center;
}

.count-text {
  margin-left: 10px;
  font-size: 12px;
  color: #666;
  min-width: 60px;
}

.loading-container {
  padding: 20px;
}

/* AI总结功能样式 */
.ai-summary-section {
  margin-top: 20px;
}

.ai-summary-card {
  margin-top: 20px;
}

.summary-result {
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  white-space: pre-wrap;
  line-height: 1.6;
}

/* 报告下载功能样式 */
.report-actions {
  margin-top: 10px;
}

.report-download-btn {
  margin-left: 10px;
}
</style>