import axios from 'axios';
import { ElMessage } from 'element-plus';

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.DEV ? 'http://localhost:8080' : '',
  timeout: 10000,
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token，避免循环依赖
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log('Request config:', config);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { data } = response;
    console.log('Response received:', response);
    if (data.code === 200) {
      // 后端已经将大整数ID以字符串形式返回，无需特殊处理
      return data;
    } else {
      console.log('API error code:', data.code, 'message:', data.message);
      ElMessage.error(data.message || '操作失败');
      return Promise.reject(new Error(data.message));
    }
  },
  (error) => {
    console.log('Response error:', error);
    if (error.response?.status === 401) {
      // 清除token并重定向
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      ElMessage.error('没有权限访问');
    } else {
      console.log('Error response:', error.response);
      console.log('Error message:', error.message);
      ElMessage.error(error.message || '请求失败');
    }
    return Promise.reject(error);
  }
);

export default request;