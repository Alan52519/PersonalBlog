<template>
  <div class="register-container">
    <h2>注册</h2>
   
      <div>
        <input v-model="username" placeholder="用户名" />
      </div>
      <div>
        <input v-model="password" type="password" placeholder="密码" />
      </div>
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
<style>
.register-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 6px;
}
.register-container input {
  width: 100%;
  padding: 6px;
  margin: 6px 0;
}
</style>
