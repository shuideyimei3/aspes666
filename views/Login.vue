<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">农副产品电商平台</h1>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        @submit.prevent="handleLogin"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            placeholder="密码"
            type="password"
            prefix-icon="Lock"
            clearable
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-button"
            @click="handleLogin"
            :loading="loading"
          >
            登 录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <router-link to="/register" class="link">注册新账户</router-link>
          <span class="divider">|</span>
          <a href="#" class="link">忘记密码?</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../stores/user';
import { userAPI } from '../api';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const formRef = ref(null);
const loading = ref(false);

const form = ref({
  username: '',
  password: '',
});

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
  ],
};

const handleLogin = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    const response = await userAPI.login(form.value.username, form.value.password);
    
    console.log('Login response:', response.data); // 调试信息
    
    // 保存token和用户信息
    userStore.setToken(response.data.token);
    userStore.setUserInfo(response.data);

    ElMessage.success('登录成功');

    // 重定向到指定页面或根据角色重定向
    const redirectUrl = route.query.redirect;
    if (redirectUrl) {
      router.push(redirectUrl);
    } else {
      // 确保角色是小写
      const role = response.data.role?.toLowerCase();
      console.log('Redirecting to:', `/${role}/dashboard`); // 调试信息
      
      if (role) {
        router.push(`/${role}/dashboard`);
      } else {
        ElMessage.error('用户角色信息异常');
      }
    }
  } catch (error) {
    console.error('Login error:', error);
    if (!error.message?.includes('请求失败')) {
      ElMessage.error(error.message || '登录失败');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 24px;
  font-weight: bold;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  font-size: 16px;
  height: 40px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.login-footer {
  text-align: center;
  font-size: 12px;
  color: #999;
}

.link {
  color: #667eea;
  text-decoration: none;
  margin: 0 5px;
}

.link:hover {
  text-decoration: underline;
}

.divider {
  color: #ddd;
}
</style>
