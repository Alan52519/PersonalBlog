
<template>
  <div class="home-container">
    <h1>📚 文章列表</h1>
    <div v-if="articles.length === 0">暂无文章</div>
    <ul>
      <li v-for="article in articles" :key="article.id">
        <h3>{{ article.title }}</h3>
        <p>{{ article.content }}</p>
        <small>创建时间: {{ formatDate(article.created_at) }}</small>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const articles = ref([])

// 从 localStorage 获取 token
const token = localStorage.getItem('token')

// 创建 axios 实例，带上 token
const request = axios.create({
  baseURL: 'http://localhost:9900',
  headers: {
    Authorization: `Bearer ${token}`
  }
})

const loadArticles = async () => {
  try {
    const res = await request.get('/posts')
    articles.value = res.data
    console.log('获取文章列表成功:', res.data)
  } catch (err) {
    console.error('获取文章失败:', err)
    alert('获取文章失败')
  }
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString()
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.home-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 10px;
}
ul {
  list-style: none;
  padding: 0;
}
li {
  border: 1px solid #ddd;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 6px;
}
h3 {
  margin: 0 0 5px 0;
}
small {
  color: #999;
}
</style>
