<template>
  <div class="chat-container">
    <div class="chat-window">
      <div v-for="(msg, index) in messages" :key="index" class="message-row">
        <div :class="msg.role">{{ msg.content }}</div>
      </div>
    </div>

    <div class="input-box">
      <input v-model="input" @keyup.enter="sendMessage" placeholder="输入内容..." />
      <button @click="sendMessage">发送</button>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      input: "",
      messages: [],
    };
  },

  methods: {
    async sendMessage() {
      if (!this.input) return;

      // 展示用户消息
      this.messages.push({ role: "user", content: this.input });

      const userInput = this.input;
      this.input = "";

      // 调用本地 Ollama
      const res = await fetch("http://localhost:11434/api/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: "qwen3:4b", // 你本地的模型名
          messages: [{ role: "user", content: userInput }],
          stream: false,
        }),
      });

      const data = await res.json();

      this.messages.push({
        role: "assistant",
        content: data.message.content,
      });
    },
  },
};
</script>

<style>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.chat-window {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background: #f6f6f6;
}

.message-row {
  margin-bottom: 12px;
}

.user {
  text-align: right;
  background: #cef;
  padding: 8px;
  border-radius: 6px;
}

.assistant {
  background: #eee;
  padding: 8px;
  border-radius: 6px;
}

.input-box {
  display: flex;
  padding: 10px;
  background: #fff;
}

input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
</style>
