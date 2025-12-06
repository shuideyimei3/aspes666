import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '../stores/user';

const routes = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue'),
  },
  // 农户路由
  {
    path: '/farmer',
    component: () => import('../views/layouts/FarmerLayout.vue'),
    meta: { requiresAuth: true, role: 'FARMER' },
    children: [
      {
        path: 'dashboard',
        name: 'FarmerDashboard',
        component: () => import('../views/farmer/Dashboard.vue'),
      },
      {
        path: 'info',
        name: 'FarmerInfo',
        component: () => import('../views/farmer/Info.vue'),
      },
      {
        path: 'products',
        name: 'FarmerProducts',
        component: () => import('../views/farmer/Products.vue'),
      },
      {
        path: 'products/publish',
        name: 'PublishProduct',
        component: () => import('../views/farmer/PublishProduct.vue'),
      },
      {
        path: 'products/:id/edit',
        name: 'EditProduct',
        component: () => import('../views/farmer/EditProduct.vue'),
      },
      {
        path: 'dockings',
        name: 'FarmerDockings',
        component: () => import('../views/farmer/Dockings.vue'),
      },
      {
        path: 'orders',
        name: 'FarmerOrders',
        component: () => import('../views/farmer/Orders.vue'),
      },
      {
        path: 'orders/:id',
        name: 'FarmerOrderDetail',
        component: () => import('../views/farmer/OrderDetail.vue'),
      },
      {
        path: 'products/:id',
        name: 'FarmerProductDetail',
        component: () => import('../views/farmer/ProductDetail.vue'),
      },
      {
        path: 'contracts',
        name: 'FarmerContracts',
        component: () => import('../views/farmer/Contracts.vue'),
      },
      {
        path: 'contracts/:id',
        name: 'FarmerContractDetail',
        component: () => import('../views/farmer/ContractDetail.vue'),
      },
      {
        path: 'logistics',
        name: 'FarmerLogistics',
        component: () => import('../views/farmer/Logistics.vue'),
      },
    ],
  },
  // 采购方路由
  {
    path: '/purchaser',
    component: () => import('../views/layouts/PurchaserLayout.vue'),
    meta: { requiresAuth: true, role: 'PURCHASER' },
    children: [
      {
        path: 'dashboard',
        name: 'PurchaserDashboard',
        component: () => import('../views/purchaser/Dashboard.vue'),
      },
      {
        path: 'info',
        name: 'PurchaserInfo',
        component: () => import('../views/purchaser/Info.vue'),
      },
      {
        path: 'demands',
        name: 'PurchaserDemands',
        component: () => import('../views/purchaser/Demands.vue'),
      },
      {
        path: 'products',
        name: 'PurchaserProducts',
        component: () => import('../views/purchaser/Products.vue'),
      },
      {
        path: 'demands/publish',
        name: 'PublishDemand',
        component: () => import('../views/purchaser/PublishDemand.vue'),
      },
      {
        path: 'demands/:id/edit',
        name: 'EditDemand',
        component: () => import('../views/purchaser/EditDemand.vue'),
      },
      {
        path: 'orders',
        name: 'PurchaserOrders',
        component: () => import('../views/purchaser/Orders.vue'),
      },
      {
        path: 'orders/:id',
        name: 'PurchaserOrderDetail',
        component: () => import('../views/purchaser/OrderDetail.vue'),
      },
      {
        path: 'contracts',
        name: 'PurchaserContracts',
        component: () => import('../views/purchaser/Contracts.vue'),
      },
      {
        path: 'contracts/:id',
        name: 'PurchaserContractDetail',
        component: () => import('../views/purchaser/ContractDetail.vue'),
      },
      {
        path: 'payments',
        name: 'PurchaserPayments',
        component: () => import('../views/purchaser/Payments.vue'),
      },
      {
        path: 'demands/:id',
        name: 'PurchaserDemandDetail',
        component: () => import('../views/purchaser/DemandDetail.vue')
      },
    ],
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../views/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/Users.vue'),
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/Products.vue'),
      },
      {
        path: 'products/:id',
        name: 'AdminProductDetail',
        component: () => import('../views/admin/ProductDetail.vue'),
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/Orders.vue'),
      },
      {
        path: 'orders/:id',
        name: 'AdminOrderDetail',
        component: () => import('../views/admin/OrderDetail.vue'),
      },
      {
        path: 'statistics',
        name: 'AdminStatistics',
        component: () => import('../views/admin/Statistics.vue'),
      },
    ],
  },
  // 公共路由
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue'),
  },
  {
    path: '/farmers/:id',
    name: 'FarmerDetail',
    component: () => import('../views/FarmerDetail.vue'),
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
  },
  {
    path: '/purchaser/products/:id',
    name: 'PurchaserProductDetail',
    component: () => import('../views/purchaser/ProductDetail.vue'),
  },
  // 测试路由
  {
    path: '/test/bigint-string',
    name: 'BigIntStringTest',
    component: () => import('../views/test/BigIntStringTest.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();

  console.log('Route guard:', {
    to: to.path,
    requiresAuth: to.meta.requiresAuth,
    routeRole: to.meta.role,
    userRole: userStore.role,
    isLoggedIn: userStore.isLoggedIn,
  }); // 调试信息

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } });
      return;
    }

    // 检查角色权限（不区分大小写）
    if (to.meta.role && userStore.role.toUpperCase() !== to.meta.role.toUpperCase()) {
      console.log('Role mismatch, redirecting to home');
      next({ name: 'Home' });
      return;
    }
  }

  next();
});

export default router;
