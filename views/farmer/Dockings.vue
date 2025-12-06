<template>
  <div class="dockings-page" v-loading="loading">
    <div class="page-header">
      <h2>对接管理</h2>
      <p>管理与采购方的对接记录和查看采购需求</p>
    </div>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 我的对接记录标签页 -->
      <el-tab-pane label="我的对接记录" name="my-dockings">
        <el-card class="filter-card">
          <el-form :model="searchForm" inline>
            <el-form-item label="采购方">
              <el-input v-model="searchForm.purchaserName" placeholder="请输入采购方名称" clearable />
            </el-form-item>
            <el-form-item label="农场名称">
              <el-input v-model="searchForm.farmName" placeholder="请输入农场名称" clearable />
            </el-form-item>
            <el-form-item label="对接状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
                <el-option label="全部" value="" />
                <el-option label="待处理" value="pending" />
                <el-option label="协商中" value="negotiating" />
                <el-option label="已达成" value="agreed" />
                <el-option label="已拒绝" value="rejected" />
              </el-select>
            </el-form-item>
            <el-form-item label="创建时间">
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
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card">
          <el-table :data="dockingsList" stripe style="width: 100%">
            <el-table-column prop="purchaserName" label="采购方" min-width="120" />
            <el-table-column prop="farmName" label="农场名称" min-width="120" />
            <el-table-column prop="canSupply" label="可供应量" width="100">
              <template #default="scope">
                {{ scope.row.canSupply }}
              </template>
            </el-table-column>
            <el-table-column prop="quotePrice" label="报价" width="120">
              <template #default="scope">
                {{ scope.row.quotePrice }} 元
              </template>
            </el-table-column>
            <el-table-column prop="supplyTime" label="供应时间" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.supplyTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewDockingDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
        <el-pagination
          v-model:current-page="dockingPagination.current"
          v-model:page-size="dockingPagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="dockingPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
        </el-card>
      </el-tab-pane>

      <!-- 采购需求标签页 -->
      <el-tab-pane label="采购需求" name="demands">
        <el-card class="filter-card">
          <el-form :model="demandSearchForm" inline>
            <el-form-item label="产品名称">
              <el-input v-model="demandSearchForm.productName" placeholder="请输入产品名称" clearable />
            </el-form-item>
            <el-form-item label="采购方">
              <el-input v-model="demandSearchForm.companyName" placeholder="请输入采购方名称" clearable />
            </el-form-item>
            <el-form-item label="发布时间">
              <el-date-picker
                v-model="demandSearchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleDemandSearch">搜索</el-button>
              <el-button @click="resetDemandSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card">
          <el-table :data="demandsList" stripe style="width: 100%">
            <el-table-column prop="productName" label="产品名称" min-width="120" />
            <el-table-column prop="companyName" label="采购方" min-width="120" />
            <el-table-column prop="quantity" label="采购数量" width="100">
              <template #default="scope">
                {{ scope.row.quantity }} {{ scope.row.unit }}
              </template>
            </el-table-column>
            <el-table-column prop="priceRange" label="价格区间" width="120" />
            <el-table-column prop="deliveryDate" label="交付日期" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.deliveryDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="发布时间" width="160">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="respondToDemand(scope.row)">响应需求</el-button>
                <el-button type="success" size="small" @click="viewDemandDockings(scope.row)">查看对接</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="demandPagination.currentPage"
              v-model:page-size="demandPagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="demandPagination.total"
              @size-change="handleDemandSizeChange"
              @current-change="handleDemandCurrentChange"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 查看需求对接列表对话框 -->
    <el-dialog v-model="demandDockingsDialogVisible" title="需求对接列表" width="70%">
      <el-descriptions :column="2" border class="demand-info">
        <el-descriptions-item label="产品名称">{{ currentDemand?.productName }}</el-descriptions-item>
        <el-descriptions-item label="采购方">{{ currentDemand?.companyName }}</el-descriptions-item>
        <el-descriptions-item label="采购数量">{{ currentDemand?.quantity }} {{ currentDemand?.unit }}</el-descriptions-item>
        <el-descriptions-item label="价格区间">{{ currentDemand?.priceRange }}</el-descriptions-item>
        <el-descriptions-item label="交付日期">{{ formatDate(currentDemand?.deliveryDate) }}</el-descriptions-item>
        <el-descriptions-item label="需求描述">{{ currentDemand?.description }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider content-position="left">对接记录</el-divider>
      
      <el-table :data="demandDockingsList" stripe style="width: 100%" v-loading="demandDockingsLoading">
        <el-table-column prop="farmerName" label="农户名称" min-width="120" />
        <el-table-column prop="productName" label="产品名称" min-width="120" />
        <el-table-column prop="quotePrice" label="报价" width="120">
          <template #default="scope">
            {{ scope.row.quotePrice }} 元/{{ currentDemand?.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="canSupply" label="可供应量" width="120">
          <template #default="scope">
            {{ scope.row.canSupply }} {{ currentDemand?.unit }}
          </template>
        </el-table-column>
        <el-table-column prop="supplyTime" label="供应时间" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.supplyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="响应时间" width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDockingDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="demandDockingsPagination.currentPage"
          v-model:page-size="demandDockingsPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="demandDockingsPagination.total"
          @size-change="handleDemandDockingsSizeChange"
          @current-change="handleDemandDockingsCurrentChange"
        />
      </div>
    </el-dialog>

    <!-- 响应采购需求对话框 -->
    <el-dialog v-model="respondDialogVisible" title="响应采购需求" width="50%">
      <el-form :model="respondForm" :rules="respondRules" ref="respondFormRef" label-width="100px">
        <el-form-item label="需求信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="产品名称">{{ currentDemand?.productName }}</el-descriptions-item>
            <el-descriptions-item label="采购方">{{ currentDemand?.companyName }}</el-descriptions-item>
            <el-descriptions-item label="采购数量">{{ currentDemand?.quantity }} {{ currentDemand?.unit }}</el-descriptions-item>
            <el-descriptions-item label="价格区间">{{ currentDemand?.priceRange }}</el-descriptions-item>
            <el-descriptions-item label="交付日期">{{ formatDate(currentDemand?.deliveryDate) }}</el-descriptions-item>
            <el-descriptions-item label="需求描述">{{ currentDemand?.description }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="选择产品" prop="productId">
          <el-select v-model="respondForm.productId" placeholder="请选择您的产品" style="width: 100%">
            <el-option
              v-for="product in myProducts"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报价" prop="quotePrice">
          <el-input-number v-model="respondForm.quotePrice" :precision="2" :step="0.1" :min="0" style="width: 100%">
            <template #append>元/{{ currentDemand?.unit }}</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="可供应量" prop="canSupply">
          <el-input-number v-model="respondForm.canSupply" :min="1" style="width: 100%">
            <template #append>{{ currentDemand?.unit }}</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="供应时间" prop="supplyTime">
          <el-date-picker
            v-model="respondForm.supplyTime"
            type="date"
            placeholder="选择供应时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="联系方式" prop="contactWay">
          <el-input v-model="respondForm.contactWay" placeholder="请输入您的联系方式" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="respondForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="respondDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRespond" :loading="submittingRespond">提交响应</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 对接详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="对接详情" width="60%">
      <div v-if="currentDocking" class="detail-content">
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="采购方">{{ currentDocking.purchaserName }}</el-descriptions-item>
          <el-descriptions-item label="农场名称">{{ currentDocking.farmName }}</el-descriptions-item>
          <el-descriptions-item label="产品ID">{{ currentDocking.productId }}</el-descriptions-item>
          <el-descriptions-item label="需求ID">{{ currentDocking.demandId }}</el-descriptions-item>
          <el-descriptions-item label="可供应量">{{ currentDocking.canSupply }}</el-descriptions-item>
          <el-descriptions-item label="报价">{{ currentDocking.quotePrice }} 元</el-descriptions-item>
          <el-descriptions-item label="供应时间">{{ formatDate(currentDocking.supplyTime) }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ currentDocking.contactWay }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDocking.status)">
              {{ getStatusText(currentDocking.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(currentDocking.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentDocking.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { farmerAPI } from '../../api';
import { bigIntToString, processResponseData } from '../../utils/bigint';

const loading = ref(false);
const messagesLoading = ref(false);
const submittingMessage = ref(false);
const dockingsList = ref([]);
const messagesList = ref([]);
const currentDocking = ref(null);
const detailDialogVisible = ref(false);
const messageDialogVisible = ref(false);
const messageFormRef = ref(null);

// 搜索表单
const searchForm = reactive({
  purchaserName: '',
  farmName: '',
  status: '',
  dateRange: []
});

// 分页
const dockingPagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 需求对接列表相关数据
const demandDockingsDialogVisible = ref(false);
const demandDockingsList = ref([]);
const demandDockingsLoading = ref(false);
const demandDockingsPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

// 采购需求相关数据
const activeTab = ref('my-dockings');
const demandSearchForm = reactive({
  productName: '',
  companyName: '',
  dateRange: []
});

const demandPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
});

const demandsList = ref([]);
const demandLoading = ref(false);

// 响应需求对话框数据
const respondDialogVisible = ref(false);
const currentDemand = ref(null);
const myProducts = ref([]);
const submittingRespond = ref(false);

const respondForm = reactive({
  demandId: '',
  productId: '',
  quotePrice: 0,
  canSupply: 1,
  supplyTime: '',
  contactWay: '',
  remark: ''
});

const respondRules = {
  productId: [
    { required: true, message: '请选择产品', trigger: 'change' }
  ],
  quotePrice: [
    { required: true, message: '请输入报价', trigger: 'blur' },
    { type: 'number', min: 0, message: '报价必须大于0', trigger: 'blur' }
  ],
  canSupply: [
    { required: true, message: '请输入可供应量', trigger: 'blur' },
    { type: 'number', min: 1, message: '可供应量必须大于0', trigger: 'blur' }
  ],
  supplyTime: [
    { required: true, message: '请选择供应时间', trigger: 'change' }
  ],
  contactWay: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
};

const respondFormRef = ref(null);

// 消息表单
const messageForm = reactive({
  dockingId: '',
  recipient: '',
  content: ''
});

// 消息表单验证规则
const messageRules = {
  content: [
    { required: true, message: '请输入消息内容', trigger: 'blur' },
    { min: 1, max: 500, message: '消息内容长度在 1 到 500 个字符', trigger: 'blur' }
  ]
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning';
    case 'negotiating': return 'primary';
    case 'agreed': return 'success';
    case 'rejected': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理';
    case 'negotiating': return '协商中';
    case 'agreed': return '已达成';
    case 'rejected': return '已拒绝';
    default: return '未知状态';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString();
};

// 加载采购需求列表
const loadDemandsList = async () => {
  demandLoading.value = true;
  try {
    const filters = {};
    if (demandSearchForm.productName) {
      filters.productName = demandSearchForm.productName;
    }
    if (demandSearchForm.companyName) {
      filters.companyName = demandSearchForm.companyName;
    }
    if (demandSearchForm.dateRange && demandSearchForm.dateRange.length === 2) {
      filters.startDate = demandSearchForm.dateRange[0];
      filters.endDate = demandSearchForm.dateRange[1];
    }
    
    const response = await farmerAPI.getDemandsList(
      demandPagination.currentPage,
      demandPagination.pageSize,
      filters
    );
    
    if (response.code === 200) {
      demandsList.value = response.data.records || [];
      demandPagination.total = response.data.total || 0;
    } else {
      ElMessage.error(response.message || '获取采购需求列表失败');
    }
  } catch (error) {
    console.error('获取采购需求列表失败:', error);
    ElMessage.error('获取采购需求列表失败');
  } finally {
    demandLoading.value = false;
  }
};

// 搜索采购需求
const handleDemandSearch = () => {
  demandPagination.currentPage = 1;
  loadDemandsList();
};

// 重置采购需求搜索
const resetDemandSearch = () => {
  demandSearchForm.productName = '';
  demandSearchForm.companyName = '';
  demandSearchForm.dateRange = [];
  demandPagination.currentPage = 1;
  loadDemandsList();
};

// 处理采购需求分页大小变化
const handleDemandSizeChange = (size) => {
  demandPagination.pageSize = size;
  demandPagination.currentPage = 1;
  loadDemandsList();
};

// 处理采购需求页码变化
const handleDemandCurrentChange = (page) => {
  demandPagination.currentPage = page;
  loadDemandsList();
};

// 查看需求的对接列表
const viewDemandDockings = async (demand) => {
  currentDemand.value = demand;
  demandDockingsPagination.value.currentPage = 1;
  demandDockingsDialogVisible.value = true;
  await loadDemandDockingsList();
};

// 加载需求对接列表
const loadDemandDockingsList = async () => {
  if (!currentDemand.value) return;
  
  demandDockingsLoading.value = true;
  try {
    const response = await farmerAPI.getDemandDockings(
      currentDemand.value.id,
      demandDockingsPagination.value.currentPage,
      demandDockingsPagination.value.pageSize
    );
    
    if (response.code === 200) {
      demandDockingsList.value = response.data.records || [];
      demandDockingsPagination.value.total = response.data.total || 0;
    } else {
      ElMessage.error(response.message || '获取需求对接列表失败');
    }
  } catch (error) {
    console.error('获取需求对接列表失败:', error);
    ElMessage.error('获取需求对接列表失败');
  } finally {
    demandDockingsLoading.value = false;
  }
};

// 处理需求对接列表分页大小变化
const handleDemandDockingsSizeChange = (size) => {
  demandDockingsPagination.value.pageSize = size;
  demandDockingsPagination.value.currentPage = 1;
  loadDemandDockingsList();
};

// 处理需求对接列表页码变化
const handleDemandDockingsCurrentChange = (page) => {
  demandDockingsPagination.value.currentPage = page;
  loadDemandDockingsList();
};

// 查看对接详情（从需求对接列表）
const viewDockingDetail = async (docking) => {
  try {
    // 设置当前对接记录
    currentDocking.value = docking;
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('查看对接详情失败:', error);
    ElMessage.error('查看对接详情失败');
  }
};

// 打开响应需求对话框
const respondToDemand = async (demand) => {
  currentDemand.value = demand;
  respondForm.demandId = demand.id;
  respondForm.productId = '';
  respondForm.quotePrice = 0;
  respondForm.canSupply = 1;
  respondForm.supplyTime = '';
  respondForm.contactWay = '';
  respondForm.remark = '';
  
  // 加载农户自己的产品列表
  await loadMyProducts();
  
  respondDialogVisible.value = true;
};

// 加载农户自己的产品列表
const loadMyProducts = async () => {
  try {
    const response = await farmerAPI.getProductsPage({
      pageNum: 1,
      pageSize: 100 // 获取所有产品
    });
    
    if (response.code === 200) {
      myProducts.value = response.data.records;
    } else {
      ElMessage.error(response.message || '获取产品列表失败');
    }
  } catch (error) {
    console.error('获取产品列表失败:', error);
    ElMessage.error('获取产品列表失败');
  }
};

// 提交响应
const submitRespond = async () => {
  if (!respondFormRef.value) return;
  
  try {
    await respondFormRef.value.validate();
    
    submittingRespond.value = true;
    
    const response = await farmerAPI.respondToDemand({
      demandId: respondForm.demandId,
      productId: respondForm.productId,
      quotePrice: respondForm.quotePrice,
      canSupply: respondForm.canSupply,
      supplyTime: respondForm.supplyTime,
      contactWay: respondForm.contactWay,
      remark: respondForm.remark
    });
    
    if (response.code === 200) {
      ElMessage.success('响应需求成功');
      respondDialogVisible.value = false;
      // 刷新采购需求列表
      loadDemandsList();
      // 如果在"我的对接记录"标签页，也刷新对接记录
      if (activeTab.value === 'my-dockings') {
        loadDockingsList();
      }
    } else {
      ElMessage.error(response.message || '响应需求失败');
    }
  } catch (error) {
    console.error('响应需求失败:', error);
    ElMessage.error('响应需求失败');
  } finally {
    submittingRespond.value = false;
  }
};

// 加载对接记录列表
const loadDockingsList = async () => {
  try {
    loading.value = true;
    const params = {
      pageNum: dockingPagination.current,
      pageSize: dockingPagination.size
    };
    
    // 添加筛选条件
    if (searchForm.purchaserName) {
      params.purchaserName = searchForm.purchaserName;
    }
    if (searchForm.farmName) {
      params.farmName = searchForm.farmName;
    }
    if (searchForm.status) {
      params.status = searchForm.status;
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0];
      params.endDate = searchForm.dateRange[1];
    }

    // 使用 /api/shared/dockings/my 接口
    const response = await farmerAPI.getMyDockings(
      dockingPagination.current,
      dockingPagination.size,
      params
    );
    
    if (response.code === 200) {
      // 处理可能包含的大整数ID
      dockingsList.value = processResponseData(response.data.records) || [];
      dockingPagination.total = response.data.total || 0;
    } else {
      ElMessage.error(response.message || '获取对接记录失败');
    }
  } catch (error) {
    console.error('获取对接记录失败:', error);
    ElMessage.error('获取对接记录失败');
  } finally {
    loading.value = false;
  }
};

// 搜索对接记录
const handleSearch = () => {
  dockingPagination.current = 1;
  loadDockingsList();
};

// 重置搜索
const resetSearch = () => {
  searchForm.purchaserName = '';
  searchForm.farmName = '';
  searchForm.status = '';
  searchForm.dateRange = [];
  dockingPagination.current = 1;
  loadDockingsList();
};

// 处理分页大小变化
const handleSizeChange = (size) => {
  dockingPagination.size = size;
  dockingPagination.current = 1;
  loadDockingsList();
};

// 处理当前页变化
const handleCurrentChange = (page) => {
  dockingPagination.current = page;
  loadDockingsList();
};

// 查看详情
const viewDetail = async (row) => {
  try {
    currentDocking.value = row;
    detailDialogVisible.value = true;
  } catch (error) {
    console.error('View docking detail error:', error);
    ElMessage.error('加载对接详情失败');
  }
};

// 标签页切换处理
const handleTabClick = (tab) => {
  activeTab.value = tab.name;
  if (tab.name === 'my-dockings') {
    loadDockingsList();
  } else if (tab.name === 'demands') {
    loadDemandsList();
  }
};

// 初始化
onMounted(() => {
  loadDockingsList();
});
</script>

<style scoped>
.dockings-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 5px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.messages-section {
  margin-top: 20px;
}

.messages-section h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

.messages-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  max-height: 300px;
  overflow-y: auto;
}

.message-item {
  margin-bottom: 15px;
}

.message-item:last-child {
  margin-bottom: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.sender {
  font-weight: bold;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 12px;
}

.message-content {
  padding: 8px 12px;
  border-radius: 4px;
  word-wrap: break-word;
}

.farmer-message .message-content {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
}

.purchaser-message .message-content {
  background-color: #f0f9eb;
  border: 1px solid #c2e7b0;
}

.no-messages {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>