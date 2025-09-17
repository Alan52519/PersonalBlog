// src/utils/request.js
import axios from 'axios';

const request = axios.create({
  baseURL: 'http://localhost:9900', // 后端地址
  timeout: 5000
});

// 请求拦截器，自动带上 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  console.log('发送请求时 token:', token); // ✅ 打印 token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => Promise.reject(error));

export default request;