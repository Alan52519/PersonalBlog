// src/utils/request.js
import axios from 'axios';
import { BASE_URL } from '@/config.js';

const request = axios.create({
  baseURL: BASE_URL,//'http://localhost:9900', // 后端地址
  timeout: 5000
});

// 请求拦截器，自动带上 token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token');

  // 登录接口不添加 Authorization
  if (token && !config.url.includes('/login')) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：发现 401 自动登出
request.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
export default request;