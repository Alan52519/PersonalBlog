<template>
  <div class="editor-container">
    <h2>编辑文章</h2>
    <form @submit.prevent="submitEdit">
      <div>
        <label for="title">标题：</label>
        <input v-model="title" id="title" type="text" required />
      </div>
      <div>
        <label for="content">内容：</label>
        <textarea v-model="content" id="content" rows="10" required></textarea>
      </div>
      <button type="submit">保存修改</button>
      <button type="button" @click="goHome" class="home-btn">🏠 返回主页</button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request.js'

// 注入 router & route
const router = useRouter()
const route = useRoute()

// 获取 ID
const postId = route.params.id

// 数据
const title = ref('')
const content = ref('')

// 返回主页
const goHome = () => {
  router.push('/home')
}

// 加载文章数据
onMounted(async () => {

  if (!postId) {
    console.error("postId 未获取到")
    alert("无效的文章 ID")
    router.push('/home')
    return
  }

  try {
    const res = await request.get(`/posts/${postId}`)
    title.value = res.data.title
    content.value = res.data.content
  } catch (err) {
    console.error('加载文章失败', err)
    alert('加载文章失败')
  }
})

// 提交修改
const submitEdit = async () => {
  try {
    await request.put(`/posts/${postId}`, {
      title: title.value,
      content: content.value
    })
    alert('修改成功')
    router.push('/home')
  } catch (err) {
    console.error('修改失败', err)
    alert('修改失败')
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
