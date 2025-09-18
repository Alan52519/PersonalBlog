
<template>
  <div class="home-container">
    <h1>📚 文章列表</h1>
    <div v-if="articles.length === 0">暂无文章</div>
    <ul>
      <li v-for="article in articles" :key="article.id">
        <h3>{{ article.title }}</h3>
        <p>{{ article.content }}</p>
        <div class="article-meta">
          <small>创建时间: {{ formatDate(article.createdAt) }}</small>
          <small class="author">作者: {{ article.authorName || '匿名' }}</small>
        </div>
      </li>
    </ul>
  </div>
   <div class="pagination">
      <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button :disabled="currentPage * pageSize >= total" @click="changePage(currentPage + 1)">下一页</button>
    </div>

    <div>
      <button @click="handleClick">去创作</button>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter() // ✅ 初始化 router
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)

// 从 localStorage 获取 token
const token = localStorage.getItem('token')

// 创建 axios 实例，带上 token
const request = axios.create({
  baseURL: 'http://localhost:9900',
  headers: {
    Authorization: `Bearer ${token}`
  }
})

// 加载文章
const loadArticles = async () => {
  try {
    const res = await request.get('/posts', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    articles.value = res.data.records
    total.value = res.data.total
  } catch (err) {
    console.error('加载文章失败', err)
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return "未知时间";
  // 将后端 "2025-09-18T10:35:09" 格式转成可以解析的 Date
  const date = new Date(dateStr.replace("T", " "));
  return date.toLocaleString();
}

const changePage = (page) => {
  currentPage.value = page
  loadArticles()
}

const handleClick = () => {
  router.push('/newpost')
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
.article-meta {
  display: flex;
  justify-content: space-between; /* 左右分开 */
  font-size: 0.85rem;
  color: #666;
}

.article-meta .author {
  text-align: right;
}
.pagination {
  margin-top: 20px;
}
</style>
