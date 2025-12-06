<template>
  <div class="users-page">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>用户管理</h2>
          <el-tabs v-model="activeTab" @tab-click="handleTabClick" style="margin-top: 10px">
            <el-tab-pane label="普通用户" name="users"></el-tab-pane>
            <el-tab-pane label="农户审核" name="farmers"></el-tab-pane>
            <el-tab-pane label="采购方审核" name="purchasers"></el-tab-pane>
          </el-tabs>
        </div>
        <div class="filters" v-show="activeTab === 'users'">
          <el-select v-model="filters.role" placeholder="用户角色" clearable style="width: 150px">
            <el-option label="农户" value="farmer" />
            <el-option label="采购方" value="purchaser" />
            <el-option label="管理员" value="admin" />
          </el-select>
          <el-select v-model="filters.isCertified" placeholder="认证状态" clearable style="width: 150px">
            <el-option label="已认证" :value="1" />
            <el-option label="未认证" :value="0" />
          </el-select>
          <el-button type="primary" @click="loadUsers">查询</el-button>
        </div>
        <div class="filters" v-show="activeTab === 'farmers'">
          <el-select v-model="farmerFilters.auditStatus" placeholder="审核状态" clearable style="width: 150px">
            <el-option label="待审核" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="审核拒绝" value="rejected" />
          </el-select>
          <el-button type="primary" @click="handleFarmerFilter">查询</el-button>
        </div>
        <div class="filters" v-show="activeTab === 'purchasers'">
          <el-select v-model="purchaserFilters.auditStatus" placeholder="审核状态" clearable style="width: 150px">
            <el-option label="待审核" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="审核拒绝" value="rejected" />
          </el-select>
          <el-button type="primary" @click="handlePurchaserFilter">查询</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="content-card">
      <!-- 普通用户管理 -->
      <div v-show="activeTab === 'users'">
        <el-table :data="users" v-loading="loading" stripe>
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-tag :type="getRoleType(row.role)">{{ getRoleText(row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="contactPerson" label="联系人" width="120" />
          <el-table-column prop="contactPhone" label="联系电话" width="150" />
          <el-table-column prop="contactEmail" label="邮箱" min-width="180" />
          <el-table-column label="认证状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.isCertified ? 'success' : 'warning'">
                {{ row.isCertified ? '已认证' : '未认证' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isDelete ? 'danger' : 'success'">
                {{ row.isDelete ? '禁用' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button 
                link 
                :type="row.isDelete ? 'success' : 'warning'" 
                size="small" 
                @click="toggleUserStatus(row)"
              >
                {{ row.isDelete ? '启用' : '禁用' }}
              </el-button>
              <el-button link type="danger" size="small" @click="forceLogout(row)">
                强制下线
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
      </div>

      <!-- 农户审核 -->
      <div v-show="activeTab === 'farmers'">
        <el-table :data="farmers" v-loading="farmerLoading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="farmName" label="农场名称" />
          <el-table-column prop="originAreaId" label="产地ID" width="80" />
          <el-table-column prop="productionScale" label="生产规模" />
          <el-table-column prop="auditStatus" label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)">
                {{ getAuditStatusText(row.auditStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                size="small"
                @click="showFarmerDetail(row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="row.auditStatus === 'pending'"
                link
                type="success"
                size="small"
                @click="auditFarmer(row, true)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.auditStatus === 'pending'"
                link
                type="danger"
                size="small"
                @click="auditFarmer(row, false)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="farmerCurrentPage"
            v-model:page-size="farmerPageSize"
            :total="farmerTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleFarmerSizeChange"
            @current-change="handleFarmerCurrentChange"
          />
        </div>
      </div>

      <!-- 采购方审核 -->
      <div v-show="activeTab === 'purchasers'">
        <el-table :data="purchasers" v-loading="purchaserLoading" stripe>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="companyName" label="公司名称" />
          <el-table-column prop="companyType" label="公司类型" />
          <el-table-column prop="purchaseScale" label="采购规模" />
          <el-table-column prop="auditStatus" label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getAuditStatusType(row.auditStatus)">
                {{ getAuditStatusText(row.auditStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                size="small"
                @click="showPurchaserDetail(row)"
              >
                查看详情
              </el-button>
              <el-button
                v-if="row.auditStatus === 'pending'"
                link
                type="success"
                size="small"
                @click="auditPurchaser(row, true)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.auditStatus === 'pending'"
                link
                type="danger"
                size="small"
                @click="auditPurchaser(row, false)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="purchaserCurrentPage"
            v-model:page-size="purchaserPageSize"
            :total="purchaserTotal"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePurchaserSizeChange"
            @current-change="handlePurchaserCurrentChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 农户详情对话框 -->
    <el-dialog v-model="farmerDetailVisible" title="农户详情" width="50%">
      <div v-if="currentFarmer">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentFarmer.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentFarmer.userId }}</el-descriptions-item>
          <el-descriptions-item label="农场名称">{{ currentFarmer.farmName }}</el-descriptions-item>
          <el-descriptions-item label="产地ID">{{ currentFarmer.originAreaId }}</el-descriptions-item>
          <el-descriptions-item label="生产规模">{{ currentFarmer.productionScale }}</el-descriptions-item>
          <el-descriptions-item label="银行账户">{{ currentFarmer.bankAccount }}</el-descriptions-item>
          <el-descriptions-item label="银行名称">{{ currentFarmer.bankName }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentFarmer.idNumber }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getAuditStatusType(currentFarmer.auditStatus)">
              {{ getAuditStatusText(currentFarmer.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ currentFarmer.auditRemark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentFarmer.createTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentFarmer.approvedTime || '未审核' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="image-container" v-if="currentFarmer.idCardFrontUrl || currentFarmer.idCardBackUrl">
          <h4>身份证照片</h4>
          <el-row :gutter="20">
            <el-col :span="12" v-if="currentFarmer.idCardFrontUrl">
              <el-image :src="currentFarmer.idCardFrontUrl" fit="contain" style="width: 100%; height: 200px;" />
              <p>身份证正面</p>
            </el-col>
            <el-col :span="12" v-if="currentFarmer.idCardBackUrl">
              <el-image :src="currentFarmer.idCardBackUrl" fit="contain" style="width: 100%; height: 200px;" />
              <p>身份证背面</p>
            </el-col>
          </el-row>
        </div>
        
        <div class="reason-container" v-if="currentFarmer.applyReason">
          <h4>申请理由</h4>
          <p>{{ currentFarmer.applyReason }}</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="farmerDetailVisible = false">关闭</el-button>
          <el-button
            v-if="currentFarmer && currentFarmer.auditStatus === 'pending'"
            type="success"
            @click="auditFarmer(currentFarmer, true)"
          >
            通过
          </el-button>
          <el-button
            v-if="currentFarmer && currentFarmer.auditStatus === 'pending'"
            type="danger"
            @click="auditFarmer(currentFarmer, false)"
          >
            拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 采购方详情对话框 -->
    <el-dialog v-model="purchaserDetailVisible" title="采购方详情" width="50%">
      <div v-if="currentPurchaser">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentPurchaser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentPurchaser.userId }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ currentPurchaser.companyName }}</el-descriptions-item>
          <el-descriptions-item label="公司类型">{{ currentPurchaser.companyType }}</el-descriptions-item>
          <el-descriptions-item label="采购规模">{{ currentPurchaser.purchaseScale }}</el-descriptions-item>
          <el-descriptions-item label="法定代表人">{{ currentPurchaser.legalRepresentative }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getAuditStatusType(currentPurchaser.auditStatus)">
              {{ getAuditStatusText(currentPurchaser.auditStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核备注">{{ currentPurchaser.auditRemark || '无' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentPurchaser.createTime }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentPurchaser.approvedTime || '未审核' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="image-container" v-if="currentPurchaser.businessLicenseUrl">
          <h4>营业执照</h4>
          <el-image :src="currentPurchaser.businessLicenseUrl" fit="contain" style="width: 100%; height: 300px;" />
        </div>
        
        <div class="preferred-origin" v-if="currentPurchaser.preferredOrigin && currentPurchaser.preferredOrigin.length">
          <h4>偏好产地</h4>
          <el-tag v-for="origin in currentPurchaser.preferredOrigin" :key="origin" style="margin-right: 10px;">
            {{ origin }}
          </el-tag>
        </div>
        
        <div class="reason-container" v-if="currentPurchaser.applyReason">
          <h4>申请理由</h4>
          <p>{{ currentPurchaser.applyReason }}</p>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="purchaserDetailVisible = false">关闭</el-button>
          <el-button
            v-if="currentPurchaser && currentPurchaser.auditStatus === 'pending'"
            type="success"
            @click="auditPurchaser(currentPurchaser, true)"
          >
            通过
          </el-button>
          <el-button
            v-if="currentPurchaser && currentPurchaser.auditStatus === 'pending'"
            type="danger"
            @click="auditPurchaser(currentPurchaser, false)"
          >
            拒绝
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 审核备注对话框 -->
    <el-dialog v-model="auditRemarkVisible" :title="auditDialogTitle" width="30%">
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核备注">
          <el-input
            v-model="auditForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入审核备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditRemarkVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAudit">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { adminAPI } from '../../api';

// 活动标签页
const activeTab = ref('users');

// 普通用户相关数据
const loading = ref(false);
const users = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const filters = ref({
  role: '',
  isCertified: null,
});

// 农户审核相关数据
const farmerLoading = ref(false);
const farmers = ref([]);
const farmerCurrentPage = ref(1);
const farmerPageSize = ref(10);
const farmerTotal = ref(0);
const farmerFilters = reactive({
  auditStatus: ''
});
const farmerDetailVisible = ref(false);
const currentFarmer = ref(null);

// 采购方审核相关数据
const purchaserLoading = ref(false);
const purchasers = ref([]);
const purchaserCurrentPage = ref(1);
const purchaserPageSize = ref(10);
const purchaserTotal = ref(0);
const purchaserFilters = reactive({
  auditStatus: ''
});
const purchaserDetailVisible = ref(false);
const currentPurchaser = ref(null);

// 审核相关数据
const auditRemarkVisible = ref(false);
const auditDialogTitle = ref('');
const auditForm = reactive({
  remark: ''
});
const currentAuditItem = ref(null);
const currentAuditType = ref('');
const currentAuditApproved = ref(false);

// 获取角色类型
const getRoleType = (role) => {
  const typeMap = {
    'FARMER': 'success',
    'PURCHASER': 'warning',
    'ADMIN': 'danger',
  };
  return typeMap[role] || 'info';
};

// 获取角色文本
const getRoleText = (role) => {
  const textMap = {
    'FARMER': '农户',
    'PURCHASER': '采购方',
    'ADMIN': '管理员',
  };
  return textMap[role] || role;
};

// 获取审核状态类型
const getAuditStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  };
  return statusMap[status] || '';
};

// 获取审核状态文本
const getAuditStatusText = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '审核通过',
    rejected: '审核拒绝'
  };
  return statusMap[status] || '';
};

// 加载普通用户数据
const loadUsers = async () => {
  try {
    loading.value = true;
    const response = await adminAPI.getUsersPage(currentPage.value, pageSize.value, filters.value);
    users.value = response.data.records;
    total.value = Number(response.data.total);
  } catch (error) {
    console.error('Load users error:', error);
  } finally {
    loading.value = false;
  }
};

// 加载农户数据
const loadFarmers = async () => {
  farmerLoading.value = true;
  try {
    const params = {
      current: farmerCurrentPage.value,
      size: farmerPageSize.value
    };
    
    if (farmerFilters.auditStatus) params.auditStatus = farmerFilters.auditStatus;
    
    const res = await adminAPI.getFarmersPage(params.current, params.size, { auditStatus: params.auditStatus });
    farmers.value = res.data.records || [];
    farmerTotal.value = res.data.total || 0;
  } catch (error) {
    ElMessage.error('加载农户数据失败');
    console.error(error);
  } finally {
    farmerLoading.value = false;
  }
};

// 加载采购方数据
const loadPurchasers = async () => {
  purchaserLoading.value = true;
  try {
    const params = {
      current: purchaserCurrentPage.value,
      size: purchaserPageSize.value
    };
    
    if (purchaserFilters.auditStatus) params.auditStatus = purchaserFilters.auditStatus;
    
    const res = await adminAPI.getPurchasersPage(params.current, params.size, { auditStatus: params.auditStatus });
    purchasers.value = res.data.records || [];
    purchaserTotal.value = res.data.total || 0;
  } catch (error) {
    ElMessage.error('加载采购方数据失败');
    console.error(error);
  } finally {
    purchaserLoading.value = false;
  }
};

// 农户筛选
const handleFarmerFilter = () => {
  farmerCurrentPage.value = 1; // 重置到第一页
  loadFarmers();
};

// 采购方筛选
const handlePurchaserFilter = () => {
  purchaserCurrentPage.value = 1; // 重置到第一页
  loadPurchasers();
};

// 标签页切换
const handleTabClick = () => {
  if (activeTab.value === 'users') {
    loadUsers();
  } else if (activeTab.value === 'farmers') {
    loadFarmers();
  } else if (activeTab.value === 'purchasers') {
    loadPurchasers();
  }
};

// 分页相关
const handleSizeChange = () => {
  loadUsers();
};

const handleCurrentChange = () => {
  loadUsers();
};

const handleFarmerSizeChange = () => {
  loadFarmers();
};

const handleFarmerCurrentChange = () => {
  loadFarmers();
};

const handlePurchaserSizeChange = () => {
  loadPurchasers();
};

const handlePurchaserCurrentChange = () => {
  loadPurchasers();
};

// 切换用户状态
const toggleUserStatus = async (row) => {
  try {
    const action = row.isDelete ? '启用' : '禁用';
    await ElMessageBox.confirm(
      `确定要${action}用户【${row.username}】吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    const newStatus = row.isDelete ? 0 : 1;
    await adminAPI.toggleUserStatus(row.id, newStatus);
    ElMessage.success(`${action}成功`);
    loadUsers();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Toggle status error:', error);
    }
  }
};

// 强制用户下线
const forceLogout = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要强制用户【${row.username}】下线吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await adminAPI.forceLogoutUser(row.id);
    ElMessage.success('强制下线成功');
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Force logout error:', error);
    }
  }
};

// 显示农户详情
const showFarmerDetail = (farmer) => {
  // 处理图片URL，添加前缀
  const processedFarmer = { ...farmer };
  if (processedFarmer.idCardFrontUrl && !processedFarmer.idCardFrontUrl.startsWith('http')) {
    processedFarmer.idCardFrontUrl = `https://aspes.${processedFarmer.idCardFrontUrl}`;
  }
  if (processedFarmer.idCardBackUrl && !processedFarmer.idCardBackUrl.startsWith('http')) {
    processedFarmer.idCardBackUrl = `https://aspes.${processedFarmer.idCardBackUrl}`;
  }
  
  currentFarmer.value = processedFarmer;
  farmerDetailVisible.value = true;
};

// 显示采购方详情
const showPurchaserDetail = (purchaser) => {
  // 处理图片URL，添加前缀
  const processedPurchaser = { ...purchaser };
  if (processedPurchaser.businessLicenseUrl && !processedPurchaser.businessLicenseUrl.startsWith('http')) {
    processedPurchaser.businessLicenseUrl = `https://aspes.${processedPurchaser.businessLicenseUrl}`;
  }
  
  currentPurchaser.value = processedPurchaser;
  purchaserDetailVisible.value = true;
};

// 审核农户
const auditFarmer = (farmer, approved) => {
  currentAuditItem.value = farmer;
  currentAuditType.value = 'farmer';
  currentAuditApproved.value = approved;
  auditDialogTitle.value = approved ? '审核通过' : '审核拒绝';
  auditForm.remark = '';
  auditRemarkVisible.value = true;
};

// 审核采购方
const auditPurchaser = (purchaser, approved) => {
  currentAuditItem.value = purchaser;
  currentAuditType.value = 'purchaser';
  currentAuditApproved.value = approved;
  auditDialogTitle.value = approved ? '审核通过' : '审核拒绝';
  auditForm.remark = '';
  auditRemarkVisible.value = true;
};

// 确认审核
const confirmAudit = async () => {
  try {
    const data = {
      auditStatus: currentAuditApproved.value ? 'approved' : 'rejected',
      auditRemark: auditForm.remark
    };
    
    if (currentAuditType.value === 'farmer') {
      await adminAPI.auditFarmer(currentAuditItem.value.id, data);
      ElMessage.success('农户审核成功');
      farmerDetailVisible.value = false;
      loadFarmers();
    } else if (currentAuditType.value === 'purchaser') {
      await adminAPI.auditPurchaser(currentAuditItem.value.id, data);
      ElMessage.success('采购方审核成功');
      purchaserDetailVisible.value = false;
      loadPurchasers();
    }
    
    auditRemarkVisible.value = false;
  } catch (error) {
    ElMessage.error('审核失败');
    console.error(error);
  }
};

// 初始化
onMounted(() => {
  loadUsers();
});
</script>

<style scoped>
.users-page {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.filters {
  display: flex;
  gap: 10px;
}

.content-card {
  min-height: 500px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-container {
  margin-top: 20px;
}

.image-container h4 {
  margin-bottom: 10px;
}

.reason-container {
  margin-top: 20px;
}

.reason-container h4 {
  margin-bottom: 10px;
}

.preferred-origin {
  margin-top: 20px;
}

.preferred-origin h4 {
  margin-bottom: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
