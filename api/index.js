import request from './request';

// 用户认证服务
export const userAPI = {
  // 登录
  login: (username, password) => {
    return request.post('/api/common/auth/login', { username, password });
  },

  // 注册
  register: (data) => {
    return request.post('/api/common/auth/register', data);
  },

  // 登出
  logout: () => {
    return request.post('/api/common/auth/logout');
  },

  // 更新个人信息
  updateProfile: (data) => {
    return request.put('/api/common/auth/profile', data);
  },
};

// 农户信息服务
export const farmerAPI = {
  // 获取农户详情
  getDetail: (id) => {
    return request.get(`/api/farmer/farmer-info/${id}`);
  },

  // 获取当前用户农户信息
  getMyInfo: () => {
    return request.get('/api/farmer/farmer-info/my');
  },

  // 提交农户信息
  submitInfo: (data) => {
    const formData = new FormData();
    Object.keys(data).forEach((key) => {
      if (data[key] != null) {
        // 特殊处理文件对象
        if (key === 'idCardFrontFile' || key === 'idCardBackFile') {
          if (data[key] instanceof File) {
            formData.append(key, data[key]);
          }
        } else {
          // 确保其他值都转换为字符串，特别是大整数ID
          const value = typeof data[key] === 'bigint' ? data[key].toString() : data[key];
          formData.append(key, value);
        }
      }
    });
    return request.post('/api/farmer/farmer-info', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 发布产品
  publishProduct: (data) => {
    const formData = new FormData();
    
    // 处理产品图片详细信息
    if (data.productImageDetails && Array.isArray(data.productImageDetails)) {
      data.productImageDetails.forEach((img, index) => {
        formData.append(`productImageDetails[${index}].file`, img.file);
        formData.append(`productImageDetails[${index}].imageType`, img.imageType); // 使用大写格式的枚举值
        if (img.sort != null) {
          formData.append(`productImageDetails[${index}].sort`, img.sort);
        }
      });
    }
    
    // 处理其他字段
    Object.keys(data).forEach((key) => {
      if (key === 'productImageDetails') {
        // 已经处理过了
        return;
      } else if (data[key] != null && data[key] !== '') {
        // 确保所有值都转换为字符串，特别是大整数ID
        const value = typeof data[key] === 'bigint' ? data[key].toString() : data[key];
        formData.append(key, value);
      }
    });
    
    // 调试日志
    console.log('API publishProduct - FormData keys:');
    for (let pair of formData.entries()) {
      console.log(pair[0], ':', pair[1]);
    }
    
    return request.post('/api/farmer/products', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 分页查询产品（我的产品）
  getProductsPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/farmer/products/my', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取产品详情
  getProductDetail: (productId) => {
    return request.get(`/api/farmer/products/${productId}`);
  },

  // 更新产品
  updateProduct: (productId, data) => {
    const formData = new FormData();
    
    // 添加图片信息（编辑产品时只处理新上传的图片）
    if (data.productImageDetails) {
      console.log('Processing productImageDetails:', data.productImageDetails.length, 'items');
      data.productImageDetails.forEach((img, index) => {
        if (img.file) {
          console.log(`Adding file ${index} to FormData:`, img.file.name, img.file.type, img.file.size);
          formData.append(`productImageDetails[${index}].file`, img.file);
          formData.append(`productImageDetails[${index}].imageType`, img.imageType); // 使用大写格式的枚举值
          if (img.sort != null) {
            formData.append(`productImageDetails[${index}].sort`, img.sort);
          }
        } else {
          console.error(`File object missing for image ${index}:`, img);
        }
      });
    } else {
      console.log('No productImageDetails found in data');
    }
    
    // 添加其他表单数据
    Object.keys(data).forEach((key) => {
      if (key !== 'productImageDetails') {
        // 确保所有值都转换为字符串，特别是大整数ID
        const value = typeof data[key] === 'bigint' ? data[key].toString() : data[key];
        formData.append(key, value);
      }
    });
    
    // 调试日志
    console.log('API updateProduct - FormData keys:');
    for (let pair of formData.entries()) {
      console.log(pair[0], ':', pair[1]);
    }
    
    return request.put(`/api/farmer/products/${productId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 下架产品
  offlineProduct: (productId) => {
    return request.put(`/api/farmer/products/${productId}/off-sale`);
  },

  // 上架产品
  onSaleProduct: (productId) => {
    return request.put(`/api/farmer/products/${productId}/on-sale`);
  },
  



  // 响应采购需求
  respondToDemand: (data) => {
    return request.post('/api/shared/dockings/respond', data);
  },

  // 查询我的对接记录
  getMyDockings: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/shared/dockings/my', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 查询需求的对接列表
  getDemandDockings: (demandId, pageNum = 1, pageSize = 10) => {
    return request.get(`/api/shared/dockings/demand/${demandId}`, {
      params: { pageNum, pageSize },
    });
  },

  // 获取采购需求列表
  getDemandsList: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/purchaser/demands', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取对接记录
  getDockingList: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/farmer/dockings/page', {
      params: { pageNum, pageSize, ...filters },
    });
  },




  // 获取合同列表
  getContractsList: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/purchaser/contracts/my', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取合同详情
  getContractDetail: (contractId) => {
    return request.get(`/api/purchaser/contracts/${contractId}`);
  },

  // 签署合同
  signContract: (contractId, signFile) => {
    const formData = new FormData();
    formData.append('signFile', signFile);
    return request.put(`/api/purchaser/contracts/${contractId}/sign`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 拒签合同
  rejectContract: (contractId, reason) => {
    return request.put(`/api/purchaser/contracts/${contractId}/reject`, null, {
      params: { reason },
    });
  },

  

  // 物流管理相关接口
  // 创建物流记录
  createLogistics: (data) => {
    return request.post('/api/farmer/logistics', data);
  },

  // 根据订单ID获取物流信息
  getLogisticsByOrderId: (orderId) => {
    return request.get(`/api/farmer/logistics/order/${orderId}`);
  },

  // 获取订单的所有物流信息
  getAllLogisticsByOrderId: (orderId) => {
    return request.get(`/api/farmer/logistics/order/${orderId}/all`);
  },

  // 获取物流详情
  getLogisticsDetail: (logisticsId) => {
    return request.get(`/api/farmer/logistics/${logisticsId}`);
  },

  // 获取物流轨迹列表
  getLogisticsTraces: (logisticsId) => {
    return request.get(`/api/farmer/logistics/${logisticsId}/traces`);
  },

  // 分页查询物流轨迹
  getLogisticsTracesPage: (logisticsId, pageNum = 1, pageSize = 10) => {
    return request.get(`/api/farmer/logistics/${logisticsId}/traces/page`, {
      params: { pageNum, pageSize },
    });
  },

  // 添加物流轨迹
  addLogisticsTrace: (logisticsId, data) => {
    return request.post(`/api/farmer/logistics/${logisticsId}/traces`, data);
  },

  // 发货
  shipGoods: (logisticsId, data) => {
    return request.put(`/api/farmer/logistics/${logisticsId}/ship`, data);
  },

  // 获取订单列表
  getOrdersPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/purchaser/orders/page', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取订单详情
  getOrderDetail: (orderId) => {
    return request.get(`/api/purchaser/orders/${orderId}`);
  },
};

// 采购方信息服务
export const purchaserAPI = {
  // 获取采购方详情
  getDetail: (id) => {
    return request.get(`/api/purchaser/purchaser-info/${id}`);
  },

  // 获取当前用户采购方信息
  getMyInfo: () => {
    return request.get('/api/purchaser/purchaser-info/my');
  },

  // 提交采购方信息
  submitInfo: (data) => {
    const formData = new FormData();
    Object.keys(data).forEach((key) => {
      // 确保所有值都转换为字符串，特别是大整数ID
      const value = typeof data[key] === 'bigint' ? data[key].toString() : data[key];
      formData.append(key, value);
    });
    return request.post('/api/purchaser/purchaser-info', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 获取产品列表
  getProductsPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/farmer/products', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取产品详情
  getProductDetail: (productId) => {
    return request.get(`/api/farmer/products/${productId}`);
  },




  // 发布采购需求
  publishDemand: (data) => {
    // 确保所有大整数ID转换为字符串
    const processedData = { ...data };
    if (processedData.categoryId && typeof processedData.categoryId === 'bigint') {
      processedData.categoryId = processedData.categoryId.toString();
    }
    
    return request.post('/api/purchaser/demands', processedData);
  },

  // 分页查询采购需求（我的需求）
  getDemandsPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/purchaser/demands/my', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取采购需求详情
  getDemandDetail: (demandId) => {
    return request.get(`/api/purchaser/demands/${demandId}`);
  },

  // 更新采购需求
  updateDemand: (demandId, data) => {
    // 确保所有大整数ID转换为字符串
    const processedData = { ...data };
    if (processedData.categoryId && typeof processedData.categoryId === 'bigint') {
      processedData.categoryId = processedData.categoryId.toString();
    }
    
    return request.put(`/api/purchaser/demands/${demandId}`, processedData);
  },

  // 获取对接记录列表
  getDockingsList: (params = {}) => {
    return request.get('/api/shared/dockings/my', { params });
  },

  // 获取对接详情
  getDockingDetail: (dockingId) => {
    return request.get(`/api/shared/dockings/${dockingId}`);
  },

  // 获取对接消息
  getDockingMessages: (dockingId) => {
    return request.get(`/api/shared/dockings/${dockingId}/messages`);
  },

  // 发送对接消息
  sendDockingMessage: (data) => {
    return request.post('/api/shared/dockings/messages', data);
  },

  // 获取需求对接列表
  getDemandDockings: (demandId, pageNum = 1, pageSize = 10) => {
    return request.get(`/api/shared/dockings/demand/${demandId}`, {
      params: { pageNum, pageSize },
    });
  },

  // 获取需求详情
  getDemandDetail: (demandId) => {
    return request.get(`/api/purchaser/demands/${demandId}`);
  },




  // 分页查询订单
  getOrdersPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/purchaser/orders/page', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取我的订单
  getMyOrders: (pageNum = 1, pageSize = 10) => {
    return request.get('/api/purchaser/orders/my', {
      params: { current: pageNum, size: pageSize },
    });
  },

  // 获取订单详情
  getOrderDetail: (orderId) => {
    return request.get(`/api/purchaser/orders/${orderId}`);
  },




  // 创建合同
  createContract: (data) => {
    const formData = new FormData();
    Object.keys(data).forEach((key) => {
      // 确保所有值都转换为字符串，特别是大整数ID
      const value = typeof data[key] === 'bigint' ? data[key].toString() : data[key];
      formData.append(key, value);
    });
    return request.post('/api/purchaser/contracts', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 签署合同
  signContract: (contractId, signFile) => {
    const formData = new FormData();
    formData.append('signFile', signFile);
    return request.put(`/api/purchaser/contracts/${contractId}/sign`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 拒签合同
  rejectContract: (contractId, reason) => {
    return request.put(`/api/purchaser/contracts/${contractId}/reject`, null, {
      params: { reason },
    });
  },

  // 终止合同
  terminateContract: (contractId, reason) => {
    return request.put(`/api/purchaser/contracts/${contractId}/terminate`, null, {
      params: { reason },
    });
  },




  // 提交支付
  submitPayment: (data) => {
    const formData = new FormData();
    Object.keys(data).forEach((key) => {
      // 确保所有值都转换为字符串，特别是大整数ID
      const value = typeof data[key] === 'bigint' ? data[key].toString() : data[key];
      formData.append(key, value);
    });
    return request.post('/api/purchaser/payments', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // 获取支付记录
  getPaymentsPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/purchaser/payments/my', {
      params: { page: pageNum, size: pageSize, ...filters },
    });
  },


  // 获取对接详情
  getDockingDetail: (dockingId) => {
    return request.get(`/api/purchaser/dockings/${dockingId}`);
  },

  // 处理对接
  handleDocking: (dockingId, data) => {
    return request.put(`/api/shared/dockings/${dockingId}/handle`, data);
  },

  // 获取合同列表
  getContractsList: (params = {}) => {
    return request.get('/api/purchaser/contracts/my', { params });
  },

  // 获取合同详情
  getContractDetail: (contractId) => {
    return request.get(`/api/purchaser/contracts/${contractId}`);
  },

  // 采购方确认订单完成
  completeOrder: (id) => {
    return request.post(`/api/purchaser/orders/${id}/complete`);
  },

};

// 搜索服务
export const searchAPI = {
  // 搜索农户
  searchFarmers: (params = {}) => {
    return request.get('/api/search/farmers', { params });
  },

  // 搜索产品
  searchProducts: (params = {}) => {
    return request.get('/api/search/products', { params });
  },

  // 搜索采购需求
  searchDemands: (params = {}) => {
    return request.get('/api/search/demands', { params });
  },

  // 搜索采购方
  searchPurchasers: (params = {}) => {
    return request.get('/api/search/purchasers', { params });
  },
};

// 产品分类服务
export const categoryAPI = {
  // 获取树形分类
  getTree: () => {
    return request.get('/api/shared/product-category/tree');
  },

  // 获取分类详情
  getDetail: (id) => {
    return request.get(`/api/shared/product-category/${id}`);
  },
};

// 产地服务
export const originAreaAPI = {
  // 分页查询产地
  getPage: (current = 1, size = 100) => {
    return request.get('/api/shared/origin-area/page', {
      params: { current, size },
    });
  },

  // 获取产地详情
  getDetail: (id) => {
    return request.get(`/api/shared/origin-area/${id}`);
  },
};

// 管理员服务
export const adminAPI = {
  // 分页查询用户
  getUsersPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/admin/users/page', {
      params: { current: pageNum, size: pageSize, ...filters },
    });
  },

  // 禁用/启用用户
  toggleUserStatus: (userId, isDelete) => {
    return request.put(`/api/admin/users/${userId}/status`, null, {
      params: { isDelete },
    });
  },

  // 强制用户下线
  forceLogoutUser: (userId) => {
    return request.post(`/api/admin/users/${userId}/force-logout`);
  },

  // 分页查询产品
  getProductsPage: (pageNum = 1, pageSize = 10, filters = {}) => {
    return request.get('/api/admin/products/page', {
      params: { pageNum, pageSize, ...filters },
    });
  },

  // 获取产品详情
  getProductDetail: (productId) => {
    return request.get(`/api/admin/products/${productId}`);
  },

  // 下架产品
  offlineProduct: (productId) => {
    return request.put(`/api/admin/products/${productId}/force-off-sale`);
  },

  // 上架产品
  onSaleProduct: (productId) => {
    return request.put(`/api/admin/products/${productId}/force-on-sale`);
  },

  // 删除产品
  deleteProduct: (productId) => {
    return request.delete(`/api/admin/products/${productId}`);
  },

  // 分页查询订单
  getOrdersPage: (current = 1, size = 10, filters = {}) => {
    return request.get('/api/admin/orders/page', {
      params: { current, size, ...filters },
    });
  },

  // 获取订单详情
  getOrderDetail: (orderId) => {
    return request.get(`/api/admin/orders/${orderId}`);
  },

  // 获取平台统计数据
  getPlatformStats: () => {
    return request.get('/api/admin/statistics/platform');
  },

  // 获取农户活跃度数据
  getFarmerActivity: () => {
    return request.get('/api/admin/statistics/farmer-activity');
  },

  // 分页查询农户信息（审核）
  getFarmersPage: (current = 1, size = 10, filters = {}) => {
    return request.get('/api/admin/users/farmers/page', {
      params: { current, size, ...filters },
    });
  },

  // 审核农户信息
  auditFarmer: (id, data) => {
    return request.put(`/api/admin/users/farmers/${id}/audit`, data);
  },

  // 分页查询采购方信息（审核）
  getPurchasersPage: (current = 1, size = 10, filters = {}) => {
    return request.get('/api/admin/users/purchasers/page', {
      params: { current, size, ...filters },
    });
  },

  // 审核采购方信息
  auditPurchaser: (id, data) => {
    return request.put(`/api/admin/users/purchasers/${id}/audit`, data);
  },
};
