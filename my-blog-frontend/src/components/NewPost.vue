<template>
  <div class="page-layout">
    <div class="editor-card">
      <h2 class="title">撰写新文章</h2>
      
      <form @submit.prevent="submitPost" class="post-form">
        <div class="form-group">
          <label for="title" class="label">文章标题：</label>
          <input 
            v-model="title" 
            id="title" 
            type="text" 
            placeholder="请输入标题..." 
            class="input-field" 
            required 
            :disabled="isSubmitting"
          />
        </div>
        
        <!-- Markdown 编辑区域和实时预览区域 -->
        <div class="editor-area-container">
          <!-- 编辑器 -->
          <div class="editor-pane">
            <label for="content" class="label editor-label">文章内容 (Markdown)：</label>
            <textarea 
              v-model="content" 
              id="content" 
              rows="12" 
              placeholder="在此输入您的 Markdown 内容..." 
              class="textarea-field" 
              required 
              :disabled="isSubmitting"
            ></textarea>
          </div>
          
          <!-- 预览 -->
          <div class="preview-pane">
            <div class="label preview-label">实时预览：</div>
            <div v-html="renderedContent" class="markdown-preview"></div>
          </div>
        </div>
        
        <div class="button-group">
          <button 
            type="submit" 
            class="submit-btn primary-btn" 
            :disabled="isSubmitting || !title.trim() || !content.trim()"
          >
            <!-- 根据状态显示不同文本 -->
            <span v-if="isSubmitting">
              <span class="spinner"></span> 正在发布...
            </span>
            <span v-else>
              发布文章
            </span>
          </button>
          
          <button 
            @click="goHome" 
            type="button" 
            class="home-btn secondary-btn"
            :disabled="isSubmitting"
          >
            <span class="icon">🏠</span> 返回主页
          </button>
        </div>
      </form>
    </div>
  </div>

  <!-- 自定义通知/Toast 组件 -->
  <div v-if="notification.visible" :class="['notification', notification.type]">
    <div class="notification-icon">
      <!-- 动态图标 -->
      <span v-if="notification.type === 'success'">✅</span>
      <span v-else>❌</span>
    </div>
    <div class="notification-content">
      {{ notification.message }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue' // 引入 computed
import { useRouter } from 'vue-router'
// 假设 request 模块已正确配置，保持用户原有引入方式
import request from '@/utils/request.js';
// 引入 marked 库进行 Markdown 解析
import { marked } from 'marked'; 

const title = ref('')
const content = ref('')
const isSubmitting = ref(false) // 新增状态：发布中
const router = useRouter()

// **新增：计算属性，实时将 Markdown 转换为 HTML**
const renderedContent = computed(() => {
    // 使用 marked 库将 markdown 字符串转换为 HTML
    return marked(content.value || "");
});

// 自定义通知状态
const notification = ref({
  visible: false,
  message: '',
  type: 'success' // success | error
})

/**
 * 显示自定义通知
 * @param {string} message 提示信息
 * @param {string} type 'success' 或 'error'
 */
const showNotification = (message, type) => {
  notification.value.message = message
  notification.value.type = type
  notification.value.visible = true

  // 3秒后自动隐藏
  setTimeout(() => {
    notification.value.visible = false
  }, 3000)
}

const goHome = () => {
  // 注意路径要和路由表一致，保持用户原有逻辑
  router.push('/home')
}

const submitPost = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true

  try {
    // 保持用户原有 API 调用逻辑
    await request.post('/posts', {
      title: title.value,
      // 提交时发送原始 Markdown 内容
      content: content.value 
    })
    
    // 替换 alert('发布成功') 为自定义通知
    showNotification('文章发布成功！正在跳转...', 'success')
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
        router.push('/home')
    }, 1500)

  } catch (err) {
    console.error('发布失败', err)
    // 替换 alert('发布失败') 为自定义通知
    showNotification('文章发布失败，请检查网络或重试。', 'error')
  } finally {
    // 无论成功或失败，都解除提交状态（但成功后会跳转，所以可能不会执行到）
    // 为了防止延迟跳转期间用户多次点击，保持 isSubmitting 为 true，直到跳转或失败提示完成。
    if (notification.value.type === 'error') {
        isSubmitting.value = false
    }
  }
}
</script>

<style scoped>
/* -------------------- 布局与卡片 -------------------- */
.page-layout {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  padding: 40px 20px;
  background-color: #f0f2f5; /* 柔和背景色 */
}

.editor-card {
  width: 100%;
  max-width: 1200px; /* 增大卡片宽度以适应分栏 */
  background: white;
  padding: 30px 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 30px;
  border-bottom: 2px solid #4CAF50;
  padding-bottom: 10px;
}

/* -------------------- 表单元素 -------------------- */
.post-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 25px;
}

.label {
  display: block;
  font-size: 1rem;
  font-weight: 500;
  color: #555;
  margin-bottom: 8px;
}

.input-field {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s, box-shadow 0.3s;
  box-sizing: border-box; /* 确保 padding 不增加宽度 */
}

.input-field:focus {
  border-color: #4CAF50;
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.2);
  outline: none;
}

/* 禁用状态 */
.input-field:disabled,
.textarea-field:disabled {
    background-color: #f8f8f8;
    cursor: not-allowed;
}

/* -------------------- 新增：Markdown 编辑和预览分栏 -------------------- */

.editor-area-container {
    display: flex;
    gap: 20px;
    margin-bottom: 25px;
    height: 500px; /* 固定高度，确保滚动 */
}

.editor-pane, .preview-pane {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.editor-label, .preview-label {
    /* 保持与通用 label 样式一致，但去除底部 margin 以紧邻内容区域 */
    margin-bottom: 8px;
}

.textarea-field {
  flex-grow: 1; /* 占据剩余空间 */
  min-height: 100%; /* 确保高度填充 editor-pane */
  resize: none; /* 禁用手动缩放 */
}

.markdown-preview {
    flex-grow: 1;
    padding: 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background-color: #ffffff;
    overflow-y: auto; /* 允许滚动 */
    line-height: 1.6;
    color: #333;
    box-sizing: border-box;
}

/* **Markdown 渲染内容样式 (深入选择器)** */
.markdown-preview :deep(h1),
.markdown-preview :deep(h2),
.markdown-preview :deep(h3) {
    border-bottom: 1px solid #eee;
    padding-bottom: 0.3em;
    margin-top: 1em;
}

.markdown-preview :deep(pre), 
.markdown-preview :deep(code) {
    background: #f4f6f8;
    padding: 2px 4px;
    border-radius: 4px;
    font-size: 0.9rem;
    font-family: Consolas, Monaco, 'Courier New', monospace;
    overflow-x: auto;
}
.markdown-preview :deep(pre) {
    padding: 10px;
    border-radius: 6px;
    margin: 15px 0;
    white-space: pre-wrap; /* 允许代码块换行 */
}

.markdown-preview :deep(ul),
.markdown-preview :deep(ol) {
    padding-left: 20px;
    margin-top: 5px;
    margin-bottom: 15px;
}

.markdown-preview :deep(blockquote) {
    border-left: 4px solid #4CAF50;
    padding: 10px 15px;
    margin: 15px 0;
    background-color: #f8fff8;
    color: #666;
}
.markdown-preview :deep(p) {
    margin-bottom: 15px;
}


/* -------------------- 按钮组 -------------------- */
.button-group {
  display: flex;
  gap: 20px;
  margin-top: 30px;
  justify-content: flex-end;
}

button {
  padding: 12px 25px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.3s, transform 0.1s, opacity 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

button:active {
    transform: translateY(1px);
}

button:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.primary-btn {
  background-color: #4CAF50; /* 绿色主色调 */
  color: white;
}
.primary-btn:not(:disabled):hover {
  background-color: #45a049;
}

.secondary-btn {
  background-color: #f0f0f0; /* 柔和辅助色 */
  color: #555;
}
.secondary-btn:not(:disabled):hover {
  background-color: #e0e0e0;
}

.icon {
    margin-right: 8px;
}

/* -------------------- 提交加载动画 -------------------- */
.spinner {
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid #fff;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* -------------------- 自定义通知/Toast -------------------- */
.notification {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 15px 25px;
  border-radius: 8px;
  color: white;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  opacity: 0;
  transform: translateY(100%);
  animation: slideIn 0.5s forwards, fadeOut 0.5s 2.5s forwards;
}

.notification.success {
  background-color: #4CAF50; /* 绿色 */
}

.notification.error {
  background-color: #f44336; /* 红色 */
}

.notification-icon {
    font-size: 1.5rem;
    margin-right: 15px;
}

.notification-content {
    font-weight: 500;
}

/* 动画定义 */
@keyframes slideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes fadeOut {
  to {
    opacity: 0;
  }
}

/* -------------------- 响应式调整 -------------------- */
@media (max-width: 992px) {
  .editor-card {
    max-width: 100%;
  }
  .editor-area-container {
    flex-direction: column; /* 小屏幕堆叠 */
    height: auto;
  }
  .editor-pane {
    margin-bottom: 20px;
  }
  .editor-pane, .preview-pane {
    min-height: 400px;
  }
  .button-group {
    flex-direction: row; /* 保持按钮行内显示 */
    justify-content: center;
  }
}

@media (max-width: 768px) {
  .editor-card {
    padding: 20px;
    margin: 10px;
  }
  .title {
    font-size: 1.5rem;
  }
  .button-group {
    flex-direction: column; /* 极小屏幕再次堆叠 */
    gap: 10px;
  }
}
</style>