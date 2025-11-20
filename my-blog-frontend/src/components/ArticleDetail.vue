<template>
  <div class="detail-container">
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="article" class="article-content">
      <header class="detail-header">
        <h1>{{ article.title }}</h1>
        <div class="meta-info">
          <span>作者: {{ article.authorName || '匿名' }}</span>
          <span>发布于: {{ formatDate(article.createdAt) }}</span>
        </div>
      </header>

      <hr />

      <main class="body-text">
        {{ article.content }}
      </main>
    </div>

    <div v-else class="error-state">
      文章不存在或已被删除
    </div>

    <div class="footer-actions">
      <button @click="goBack" class="back-btn">返回列表</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request.js' // 确保路径正确

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(true)

// 获取路由参数中的 id
const articleId = route.params.id

const loadArticleDetail = async () => {
  loading.value = true
  try {
    // 假设后端接口是 GET /posts/{id}
    const res = await request.get(`/posts/${articleId}`)
    article.value = res.data // 根据你实际的后端返回结构调整，可能是 res.data.data
  } catch (err) {
    console.error('获取详情失败', err)
    alert('获取文章详情失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return "未知时间"
  return new Date(dateStr.replace("T", " ")).toLocaleString()
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadArticleDetail()
})
</script>

<style scoped>
/* 容器和卡片基础样式保持不变，但增加一点圆角和阴影的深度 */
.detail-container {
    max-width: 800px;
    margin: 40px auto;
    padding: 30px 40px; /* 增加左右内边距，使内容更舒适 */
    background: #ffffff;
    border-radius: 12px; /* 略微增加圆角 */
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 增加阴影深度 */
    font-family: 'PingFang SC', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

/* 标题优化 */
.detail-header h1 {
    font-size: 2.2rem; /* 增大标题 */
    color: #2c3e50; /* 深色标题 */
    margin-bottom: 10px;
    padding-bottom: 5px;
    border-bottom: 2px solid #4CAF50; /* 底部加一根强调线 */
    display: inline-block; /* 确保强调线只占标题宽度 */
}

/* Meta 信息优化 */
.meta-info {
    color: #95a5a6; /* 使用更柔和的灰色 */
    font-size: 0.9rem;
    display: flex;
    gap: 25px; /* 增加间隔 */
    padding-top: 10px;
    border-top: 1px dashed #ecf0f1; /* 使用虚线分隔，更轻量 */
}

hr {
    display: none; /* 移除原来的 hr，使用 meta-info 的 border 替代 */
}

/* 正文优化 - 提升阅读体验的关键 */
.body-text {
    font-size: 1.05rem; /* 稍微减小字体，与行高配合 */
    line-height: 1.85; /* 增加行高，提升阅读舒适度 */
    color: #34495e; /* 更深的文本颜色，对比度更好 */
    padding: 20px 0; /* 增加上下边距 */
    white-space: pre-wrap; 
    min-height: 200px;
}

.footer-actions {
    margin-top: 50px; /* 增加与正文的距离 */
    text-align: center;
}

/* 返回按钮优化 - 使用主色调 */
.back-btn {
    padding: 10px 30px;
    background-color: #4CAF50; /* 主色调 */
    color: white;
    border: none;
    border-radius: 6px; /* 略微圆角 */
    cursor: pointer;
    transition: background-color 0.3s, transform 0.2s;
    font-size: 1rem;
    font-weight: 600; /* 加粗 */
    box-shadow: 0 4px 6px rgba(76, 175, 80, 0.3); /* 按钮阴影 */
}

.back-btn:hover {
    background-color: #45a049;
    transform: translateY(-1px); /* 悬停时稍微抬升 */
}

.loading, .error-state {
    /* 保持不变或根据需要调整 */
    text-align: center;
    padding: 50px;
    color: #666;
    font-size: 1.2rem;
}
</style>