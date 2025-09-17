<template>
  <div>
    <h2>注册</h2>
    <input v-model="username" placeholder="用户名" />
    <input v-model="password" type="password" placeholder="密码" />
    <button @click="register">注册</button>
    <p>{{ message }}</p>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      username: '',
      password: '',
      message: ''
    };
  },
  methods: {
    async register() {
      try {
        const res = await axios.post('http://localhost:9900/register', {
          username: this.username,
          password: this.password
        });
        this.message = res.data;
      } catch (err) {
        this.message = err.response?.data || err.message;
      }
    }
  }
};
</script>
