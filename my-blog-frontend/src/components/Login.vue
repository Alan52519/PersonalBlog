<template>
  <div class="auth-page-container">
    <div class="login-card">
      <h2 class="title">欢迎回来</h2>
      <p class="subtitle">登录以访问您的文章列表和创作中心</p>

      <form @submit.prevent="login" class="login-form">
        <div class="input-group">
          <label for="username">用户名</label>
          <input 
            id="username"
            v-model="username" 
            type="text" 
            required 
            placeholder="请输入用户名"
            class="input-field"
          />
        </div>
        
        <div class="input-group">
          <label for="password">密码</label>
          <input 
            id="password"
            v-model="password" 
            type="password" 
            required 
            placeholder="请输入密码"
            class="input-field"
          />
        </div>
        
        <button type="submit" class="submit-btn">
          {{ loading ? '登录中...' : '立即登录' }}
        </button>
      </form>
      
      <!-- 注册和消息区域 -->
      <p v-if="message" :class="['message', isError ? 'error-message' : 'success-message']">
        {{ message }}
      </p>
      
      <div class="switch-link">
        <p>还没有账号？</p>
       <!-- <button @click="handleClick" class="register-link-btn">去注册</button>-->
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js';

const username = ref('')
const password = ref('')
const message = ref('')
const isError = ref(false)
const loading = ref(false)
const router = useRouter()

const handleClick = () => {
  router.push('/register')
}

const login = async () => {
  if (loading.value) return; 

  loading.value = true;
  message.value = '';
  isError.value = false;

  try {
    // 清理旧 token
    localStorage.removeItem('token');

    const res = await request.post(`/login`, {
      username: username.value,
      password: password.value
    });

    // 假设后端返回的数据结构是 { token: 'jwt-token', username: 'user1' }
    if (res.data && res.data.token) {
        localStorage.setItem('token', res.data.token);
    }
    // 假设后端也返回了用户名，方便前端使用（如果 auth.js 中需要，就保存）
    if (res.data && res.data.username) {
        localStorage.setItem('username', res.data.username);
    }
    
    message.value = '登录成功，正在跳转...';
    isError.value = false;
    
    // 延迟跳转，让用户看到成功消息
    setTimeout(() => {
        router.push('/Home'); 
    }, 500);

  } catch (err) {
    const errorMsg = err.response?.data?.message || '登录失败，请检查网络或凭证';
    message.value = errorMsg;
    isError.value = true;
    console.error('登录失败:', errorMsg);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* 整体页面布局 */
.auth-page-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f7f9; /* 轻柔的背景色 */
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); /* 深度阴影 */
  transition: transform 0.3s ease;
}

/* 标题和副标题 */
.title {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 5px;
  text-align: center;
}

.subtitle {
  font-size: 0.95rem;
  color: #7f8c8d;
  margin-bottom: 30px;
  text-align: center;
}

/* 表单布局 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
}

.input-group label {
  font-size: 0.9rem;
  color: #34495e;
  margin-bottom: 6px;
  font-weight: 600;
}

.input-field {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  transition: border-color 0.3s, box-shadow 0.3s;
  font-size: 1rem;
}

.input-field:focus {
  border-color: #4CAF50; /* 聚焦时使用主题色 */
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.1);
  outline: none;
}

/* 登录按钮 */
.submit-btn {
  padding: 12px;
  margin-top: 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 700;
  transition: background-color 0.3s, transform 0.1s;
}

.submit-btn:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

/* 消息提示 */
.message {
  padding: 10px;
  border-radius: 4px;
  margin-top: 20px;
  text-align: center;
  font-weight: 500;
}

.error-message {
  background-color: #fce4e4;
  color: #c0392b;
  border: 1px solid #e74c3c;
}

.success-message {
  background-color: #e8f5e9;
  color: #4CAF50;
  border: 1px solid #4CAF50;
}

/* 注册链接 */
.switch-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  font-size: 0.9rem;
  color: #7f8c8d;
}

.register-link-btn {
  background: none;
  border: none;
  color: #3498db; /* 使用次要强调色 */
  cursor: pointer;
  padding: 0;
  font-size: 0.9rem;
  font-weight: 600;
  transition: color 0.2s;
}

.register-link-btn:hover {
  color: #2980b9;
  text-decoration: underline;
}
</style>