<template>
  <div class="login-container">
    <h2>登录</h2>
    <form @submit.prevent="login">
      <div>
        <label>用户名:</label>
        <input v-model="username" type="text" required />
      </div>
      <div>
        <label>密码:</label>
        <input v-model="password" type="password" required />
      </div>
      <button type="submit">登录</button>
    </form>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const message = ref('')
const router = useRouter()

const login = async () => {
  try {
    const res = await axios.post('http://localhost:9900/login', {
      username: username.value,
      password: password.value
    })

    // 保存 token
    localStorage.setItem('token', res.data.token)
    console.log('保存的 token:', res.data.token)

    message.value = '登录成功'
    router.push('/Home') // 跳转到 Home 页面
  } catch (err) {
    message.value = err.response?.data || err.message
    console.error('登录失败:', message.value)
  }
}
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.login-container input {
  width: 100%;
  padding: 6px;
  margin: 6px 0;
}
button {
  padding: 8px 16px;
  margin-top: 10px;
}
</style>
