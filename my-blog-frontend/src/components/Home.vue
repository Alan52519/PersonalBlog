<template>
  <div class="home-container">
    <header class="home-header">
      <h1>📚 文章列表</h1>
      <div class="header-buttons">
        <button @click="handleClick" class="primary-btn">去创作</button>
        <button @click="aihandleClick" class="primary-btn">AI助手</button>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <div v-if="articles.length === 0" class="no-articles">暂无文章</div>

    <ul class="article-list">
      <li v-for="article in articles" :key="article.id" class="article-card">
        <h3>{{ article.title }}</h3>
        <p>{{ article.content }}</p>
        <div class="article-meta">
          <small>创建时间: {{ formatDate(article.createdAt) }}</small>
          <small class="author">作者: {{ article.authorName || '匿名' }}</small>
        </div>
        <div class="article-actions" v-if="article.authorName === currentUser">
          <button class="edit-btn" @click="editPost(article.id)">编辑</button>
          <button class="delete-btn" @click="deletePost(article.id)">删除</button>
        </div>
      </li>
    </ul>

    <div class="pagination">
      <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">上一页</button>
      <span>第 {{ currentPage }} 页</span>
      <button :disabled="currentPage * pageSize >= total" @click="changePage(currentPage + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'
import { getCurrentUser } from "@/utils/auth.js"

const router = useRouter()
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(9)
const total = ref(0)
const currentUser = getCurrentUser().username

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
  if (!dateStr) return "未知时间"
  const date = new Date(dateStr.replace("T", " "))
  return date.toLocaleString()
}

const changePage = (page) => {
  currentPage.value = page
  loadArticles()
}

const handleClick = () => router.push('/newpost')
const aihandleClick = () => router.push('/chat')
const logout = () => {
  localStorage.removeItem("token")
  router.push("/login")
}

const deletePost = async (id) => {
  if (!confirm("确定要删除这篇文章吗？")) return;

  try {
    console.log(id);
    await request.delete(`/posts/${id}`);

    // 删除本地列表中的
    articles.value = articles.value.filter(a => a.id !== id);

    alert("删除成功");
  } catch (err) {
    console.error("删除失败", err);
    alert("删除失败，请检查权限或登录状态");
  }
};

const editPost = async (id) => {
   router.push(`/edit/${id}`) 

};

onMounted(() => loadArticles())
</script>

<style scoped>
.home-container {
  max-width: 900px;
  margin: 30px auto;
  padding: 0 15px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-buttons button {
  margin-left: 10px;
}

.primary-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.primary-btn:hover {
  background-color: #45a049;
}

.logout-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.logout-btn:hover {
  background-color: #d32f2f;
}

.no-articles {
  text-align: center;
  margin: 30px 0;
  color: #888;
  font-size: 1.2rem;
}

.article-list {
  list-style: none;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.article-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 2px 6px rgba(0,0,0,0.05);
  transition: box-shadow 0.3s, transform 0.3s;
}
.article-card:hover {
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.article-card h3 {
  margin: 0 0 10px;
  font-size: 1.2rem;
  color: #333;
}

.article-card p {
  font-size: 0.95rem;
  color: #555;
  margin-bottom: 12px;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: #777;
}

.article-meta .author {
  text-align: right;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 25px 0;
}

.pagination button {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 6px 12px;
  margin: 0 5px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
.pagination button:hover:not(:disabled) {
  background-color: #45a049;
}
</style>
