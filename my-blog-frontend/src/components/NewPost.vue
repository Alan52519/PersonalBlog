<template>
  <div class="editor-container">
    <h2>撰写新文章</h2>
    <form @submit.prevent="submitPost">
      <div>
        <label for="title">标题：</label>
        <input v-model="title" id="title" type="text" required />
      </div>
      <div>
        <label for="content">内容：</label>
        <textarea v-model="content" id="content" rows="10" required></textarea>
      </div>
      <button type="submit">发布文章</button>
      <button @click="goHome" class="home-btn">🏠 返回主页</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import request from '@/utils/request.js';

const title = ref('')
const content = ref('')
const router = useRouter()
const goHome = () => {
  router.push('/home') // 注意路径要和路由表一致
}
const submitPost = async () => {
  try {
    const res = await request.post('/posts', {
      title: title.value,
      content: content.value
    })
    alert('发布成功')
    router.push('/home')
  } catch (err) {
    console.error('发布失败', err)
    alert('发布失败')
  }
}
</script>

<style scoped>
.editor-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}
input,
textarea {
  width: 100%;
  margin-bottom: 20px;
}
</style>
