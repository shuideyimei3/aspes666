<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="register-title">创建账户</h1>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            clearable
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
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            placeholder="确认密码"
            type="password"
            prefix-icon="Lock"
            clearable
            show-password
          />
        </el-form-item>

        <el-form-item prop="contactPerson">
          <el-input
            v-model="form.contactPerson"
            placeholder="联系人"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="contactPhone">
          <el-input
            v-model="form.contactPhone"
            placeholder="联系电话"
            prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item prop="contactEmail">
          <el-input
            v-model="form.contactEmail"
            placeholder="邮箱"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="选择用户角色">
            <el-option label="农户" value="FARMER" />
            <el-option label="采购方" value="PURCHASER" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            @click="handleRegister"
            :loading="loading"
          >
            注 册
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span>已有账户？</span>
          <router-link to="/login" class="link">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { userAPI } from '../api';

const router = useRouter();
const formRef = ref(null);
const loading = ref(false);

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  role: 'FARMER',
});

const validatePassword = (rule, value, callback) => {
  if (value !== form.value.password) {
    callback(new Error('密码不一致'));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' },
  ],
  contactPerson: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '联系电话不能为空', trigger: 'blur' },
    { pattern: /^1[0-9]{10}$/, message: '请输入正确的电话号码', trigger: 'blur' },
  ],
  contactEmail: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  role: [{ required: true, message: '请选择用户角色', trigger: 'change' }],
};

const handleRegister = async () => {
  try {
    await formRef.value.validate();
    loading.value = true;

    const registerData = {
      username: form.value.username,
      password: form.value.password,
      contactPerson: form.value.contactPerson,
      contactPhone: form.value.contactPhone,
      contactEmail: form.value.contactEmail,
      role: form.value.role.toLowerCase(), // 转换为小写
    };

    await userAPI.register(registerData);
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (error) {
    console.error('Register error:', error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
}

.register-title {
  text-align: center;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 24px;
  font-weight: bold;
}

.register-form {
  margin-bottom: 20px;
}

.register-button {
  width: 100%;
  font-size: 16px;
  height: 40px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #999;
}

.link {
  color: #667eea;
  text-decoration: none;
  margin-left: 5px;
  font-weight: bold;
}

.link:hover {
  text-decoration: underline;
}
</style>
