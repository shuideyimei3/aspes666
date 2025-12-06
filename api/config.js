// API配置
const isDev = import.meta.env.DEV;

export const API_BASE_URL = isDev ? 'http://localhost:8080' : '/api';

export const API_ENDPOINTS = {
  // 认证相关
  AUTH_LOGIN: '/api/common/auth/login',
  AUTH_REGISTER: '/api/common/auth/register',
  AUTH_LOGOUT: '/api/common/auth/logout',
  AUTH_PROFILE: '/api/common/auth/profile',

  // 产品分类
  PRODUCT_CATEGORY_TREE: '/api/shared/product-category/tree',
  PRODUCT_CATEGORY_DETAIL: '/api/shared/product-category',

  // 搜索
  SEARCH_FARMERS: '/api/search/farmers',
  SEARCH_PRODUCTS: '/api/search/products',
  SEARCH_DEMANDS: '/api/search/demands',
  SEARCH_PURCHASERS: '/api/search/purchasers',

  // 农户管理（C端）
  FARMER_INFO_GET: '/api/farmer/farmer-info',
  FARMER_INFO_MY: '/api/farmer/farmer-info/my',
  FARMER_INFO_SUBMIT: '/api/farmer/farmer-info',
  FARMER_PRODUCTS_PUBLISH: '/api/farmer/products',
  FARMER_PRODUCTS_PAGE: '/api/farmer/products/page',
  FARMER_PRODUCTS_DETAIL: '/api/farmer/products',
  FARMER_DOCKING_RESPOND: '/api/shared/dockings/respond',
  FARMER_DOCKING_LIST: '/api/farmer/dockings/page',
  FARMER_CONTRACTS_LIST: '/api/farmer/contracts/my',
  FARMER_CONTRACTS_DETAIL: '/api/farmer/contracts',
  FARMER_CONTRACTS_SIGN: '/api/farmer/contracts',

  // 采购方管理（B端）
  PURCHASER_INFO_GET: '/api/purchaser/purchaser-info',
  PURCHASER_INFO_MY: '/api/purchaser/purchaser-info/my',
  PURCHASER_INFO_SUBMIT: '/api/purchaser/purchaser-info',
  PURCHASER_DEMANDS_PUBLISH: '/api/purchaser/demands',
  PURCHASER_DEMANDS_PAGE: '/api/purchaser/demands/page',
  PURCHASER_DEMANDS_DETAIL: '/api/purchaser/demands',
  PURCHASER_ORDERS_PAGE: '/api/purchaser/orders/page',
  PURCHASER_ORDERS_MY: '/api/purchaser/orders/my',
  PURCHASER_ORDERS_DETAIL: '/api/purchaser/orders',
  PURCHASER_CONTRACTS_CREATE: '/api/purchaser/contracts',
  PURCHASER_CONTRACTS_SIGN: '/api/purchaser/contracts',
  PURCHASER_PAYMENTS_SUBMIT: '/api/purchaser/payments',
  PURCHASER_PAYMENTS_PAGE: '/api/purchaser/payments/page',

  // 管理后台
  ADMIN_USERS_PAGE: '/api/admin/users/page',
  ADMIN_USERS_STATUS: '/api/admin/users',
  ADMIN_USERS_FORCE_LOGOUT: '/api/admin/users',
  ADMIN_PRODUCTS_PAGE: '/api/admin/products/page',
  ADMIN_PRODUCTS_DETAIL: '/api/admin/products',
  ADMIN_ORDERS_PAGE: '/api/admin/orders/page',
  ADMIN_ORDERS_DETAIL: '/api/admin/orders',
  ADMIN_COOPERATION_PAGE: '/api/admin/cooperation/page',
  ADMIN_COOPERATION_AUDIT: '/api/admin/cooperation',
  ADMIN_STATISTICS_PLATFORM: '/api/admin/statistics/platform',
  ADMIN_STATISTICS_FARMER_ACTIVITY: '/api/admin/statistics/farmer-activity/stream',
};
