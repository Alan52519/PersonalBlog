<template>
  <div class="home-container">
    <header class="home-header">
      <h1>📚 文章列表</h1>

      <div class="search-area">
        <input type="text" 
               v-model="searchKeyword" 
               @keyup.enter="searchArticles"
               placeholder="输入标题或内容关键字..." 
               class="search-input">
        <button @click="searchArticles" class="search-btn">搜索</button>
      </div>
      <div class="header-buttons">
        <button @click="handleClick" class="primary-btn">去创作</button>
        <button @click="aihandleClick" class="primary-btn">AI助手</button>
        <button @click="logout" class="logout-btn">退出登录</button>
      </div>
    </header>

    <div v-if="articles.length === 0" class="no-articles">暂无文章</div>

    <ul class="article-list">
      <li v-for="article in articles" :key="article.id" class="article-card">
        <h3 class="article-title" @click="viewDetail(article.id)">
          {{ article.title }}
        </h3>
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

// 👇 新增：搜索关键字
const searchKeyword = ref('')

// 👇 修改 loadArticles 函数，使其接受 keyword 参数
const loadArticles = async () => {
  try {
    // 每次加载文章时，确保分页回到第一页（如果不是通过 changePage 触发的）
    if (currentPage.value < 1) currentPage.value = 1; 

    const params = {
      page: currentPage.value,
      size: pageSize.value
    };

    // 如果搜索关键字不为空，则添加到请求参数中
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value;
    }

    const res = await request.get('/posts', { params });
    
    articles.value = res.data.records
    total.value = res.data.total
  } catch (err) {
    console.error('加载文章失败', err)
  }
}

// 👇 新增：搜索文章函数
const searchArticles = () => {
    // 重置到第一页，并加载文章
    currentPage.value = 1; 
    loadArticles();
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
const viewDetail = (id) => {
  router.push(`/article/${id}`)
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
/* 优化后的 CSS */

/* -------------------- 容器和头部优化 -------------------- */
.home-container {
    max-width: 1000px; /* 稍微增加最大宽度 */
    margin: 40px auto;
    padding: 0 20px;
    font-family: 'Segoe UI', 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.home-header {
    /* 调整布局，让搜索栏能挤进来 */
    display: grid; 
    grid-template-columns: 1fr auto 1fr; /* 标题 | 搜索 | 按钮 */
    gap: 20px;
    align-items: center;
    padding-bottom: 15px;
    border-bottom: 2px solid #f0f0f0;
    margin-bottom: 30px;
}
/* 确保标题和按钮区域的样式 */
.home-header h1 {
    margin: 0;
}
.header-buttons {
    /* 保持右对齐 */
    justify-self: end; 
}


/* -------------------- 新增的搜索区域样式 -------------------- */
.search-area {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
}

.search-input {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    width: 250px; /* 设定宽度 */
    font-size: 0.95rem;
    transition: border-color 0.3s;
}

.search-input:focus {
    border-color: #4CAF50;
    outline: none;
    box-shadow: 0 0 5px rgba(76, 175, 80, 0.2);
}

.search-btn {
    background-color: #4CAF50;
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
    font-size: 0.95rem;
}

.search-btn:hover {
    background-color: #45a049;
}

/* 头部按钮排版优化：使用 Flexbox 控制间距 */
.header-buttons {
    display: flex;
    gap: 12px; /* 使用 gap 代替 margin-left */
    align-items: center;
}

.primary-btn {
    background-color: #4CAF50;
    color: white;
    border: none;
    padding: 10px 18px; /* 增大点击区域 */
    border-radius: 6px; /* 略微增加圆角 */
    cursor: pointer;
    transition: background-color 0.3s, transform 0.1s;
    font-weight: 600;
}
.primary-btn:hover {
    background-color: #45a049;
    transform: translateY(-1px); /* 悬停微抬 */
}

.logout-btn {
    background-color: #e74c3c; /* 使用更鲜明的红色 */
    color: white;
    border: none;
    padding: 10px 18px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.3s;
    font-weight: 600;
}
.logout-btn:hover {
    background-color: #c0392b;
}

/* -------------------- 文章卡片优化 -------------------- */
.article-list {
    list-style: none;
    padding: 0;
    /* 调整列数和卡片大小，以适应 1000px 的最大宽度 */
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); 
    gap: 25px; /* 增加卡片间距 */
}

.article-card {
    border: none; /* 移除原来的边框 */
    border-radius: 10px; /* 增加圆角 */
    padding: 20px; /* 增加内边距 */
    background-color: #fff;
    /* 采用更柔和、现代的阴影 */
    box-shadow: 0 4px 12px rgba(0,0,0,0.08); 
    transition: box-shadow 0.3s, transform 0.3s;
    display: flex; /* 使用 flex 布局辅助内容排版 */
    flex-direction: column;
}
.article-card:hover {
    box-shadow: 0 8px 25px rgba(0,0,0,0.15); /* 悬停时阴影更深 */
    transform: translateY(-4px); /* 悬停时抬升效果更明显 */
}

/* 标题优化 */
.article-title {
    font-size: 1.35rem; /* 增大标题字体 */
    margin: 0 0 10px;
    color: #2c3e50; /* 深色 */
    cursor: pointer;
    transition: color 0.2s;
}

.article-title:hover {
    color: #3498db; /* 标题悬停使用蓝色，与主色调区分 */
    text-decoration: underline;
}

/* 内容/摘要优化 */
.article-card p {
    font-size: 1rem;
    color: #7f8c8d; /* 柔和的灰色 */
    margin-bottom: 15px;
    flex-grow: 1; /* 确保内容撑开空间，使 Meta 和 Actions 底部对齐 */
    /* 限制摘要行数，保持卡片整洁 */
    display: -webkit-box;
    -webkit-line-clamp: 3; /* 限制为 3 行 */
    -webkit-box-orient: vertical;
    overflow: hidden;
    text-overflow: ellipsis;
}

/* Meta信息优化 */
.article-meta {
    font-size: 0.8rem;
    color: #95a5a6;
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px dashed #ecf0f1; /* 虚线分隔，更优雅 */
}

/* -------------------- 操作按钮排版和样式优化 -------------------- */
.article-actions {
    display: flex;
    gap: 10px; /* 按钮间距 */
    justify-content: flex-end; /* 按钮靠右对齐 */
    margin-top: 15px;
}

.article-actions button {
    padding: 6px 15px;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
    font-size: 0.9rem;
}

.edit-btn {
    background-color: #3498db; /* 蓝色作为编辑色 */
    color: white;
    border: none;
}
.edit-btn:hover {
    background-color: #2980b9;
}

.delete-btn {
    background-color: #e74c3c; /* 红色作为删除色 */
    color: white;
    border: none;
}
.delete-btn:hover {
    background-color: #c0392b;
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



/* -------------------- 分页按钮保持不变 -------------------- */
/* ...（分页样式保持不变，因为它们已经使用了主题色）... */
</style>
