<template>
  <div class="chat-layout">
    <!-- 左侧对话列表 -->
    <div class="sidebar">
      <button @click="goHome" class="home-btn">🏠 返回主页</button>
      <button @click="newSession" class="new-session-btn">+ 新对话</button>
      <ul>
        <li v-for="session in sessions" 
            :key="session.id"
            :class="{active: session.id === currentSessionId}"
            class="session-item"
        >
          <span class="session-title" @click="selectSession(session.id)">
            {{ session.title }}
          </span>

          <!-- 图标按钮 -->
          <span class="icon-btn" @click.stop="editTitle(session)" title="修改名称">
            ✏️
          </span>
          <span class="icon-btn" @click.stop="deleteSession(session)" title="删除会话">
            🗑️
          </span>
        </li>
      </ul>
    </div>

    <!-- 右侧聊天窗口 -->
    <div class="chat-container">
      <div class="chat-window">
        <div v-for="(msg, index) in currentSessionMessages" 
             :key="index" 
             class="message-row"
        >
          <div :class="msg.role">
            <!-- 渲染 Markdown -->
            <div v-if="msg.role === 'assistant'" v-html="renderMarkdown(msg.content)"></div>
            <div v-else>{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <div class="input-box">
        <input v-model="input" 
               @keyup.enter="sendMessage" 
               placeholder="输入内容..." />
        <button @click="sendMessage">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import { marked } from "marked";
import { BASE_URL } from "../config";

export default {
  data() {
    return {
      input: "",
      sessions: [],
      currentSessionId: null,
    };
  },

  computed: {
    currentSessionMessages() {
      const session = this.sessions.find(s => s.id === this.currentSessionId);
      return session ? session.messages : [];
    }
  },

  async created() {
    await this.loadSessions();
  },

  methods: {
    goHome() {
    this.$router.push("/Home"); // 假设你的主页路由是 "/"
  },
    renderMarkdown(content) {
      return marked(content || "");
    },

    async loadSessions() {
      try {
        const token = localStorage.getItem('token');
        const res = await fetch(BASE_URL+'/session/getSessions', {
          headers: {
            "Authorization": `Bearer ${token}`,
          }
        });
        const data = await res.json();
        this.sessions = data;
        if (this.sessions.length > 0) {
          this.currentSessionId = this.sessions[0].id;
        }
      } catch (err) {
        console.error("获取历史会话失败:", err);
      }
    },

    newSession() {
      const id = Date.now();
      this.sessions.push({ id, title: "新对话", messages: [] });
      this.currentSessionId = id;
    },

    selectSession(id) {
      this.currentSessionId = id;
    },

    async sendMessage() {
      if (!this.input || !this.currentSessionId) return;

      const session = this.sessions.find(s => s.id === this.currentSessionId);

      // 显示用户消息
      session.messages.push({ role: "user", content: this.input });
      const userInput = this.input;
      this.input = "";

      try {
        const token = localStorage.getItem('token');

        const res = await fetch(BASE_URL+'/chat', {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          },
          body: JSON.stringify({
            model: "qwen3:4b",
            messages: [{ role: "user", content: userInput }],
            sessionId: this.currentSessionId,
            stream: false
          })
        });

        const data = await res.json();

        if (data.response && data.response.message) {
          session.messages.push({
            role: "assistant",
            content: data.response.message.content,
          });
        } else {
          session.messages.push({
            role: "assistant",
            content: "后端返回格式不正确",
          });
        }

        // 更新 sessionId
        if (data.sessionId && data.sessionId !== this.currentSessionId) {
          this.currentSessionId = data.sessionId;
        }

      } catch (err) {
        console.error("调用后端出错:", err);
        session.messages.push({
          role: "assistant",
          content: "调用后端出错，请稍后重试",
        });
      }
    },

    // 修改会话标题
    async editTitle(session) {
      const newTitle = prompt("输入新的对话名称", session.title);
      if (!newTitle) return;

      const token = localStorage.getItem("token");
      try {
        const res = await fetch(BASE_URL+`/session/${session.id}/title`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
          },
          body: JSON.stringify({ title: newTitle })
        });

        if (res.ok) {
          session.title = newTitle;
        } else {
          alert("修改失败");
        }
      } catch (err) {
        console.error(err);
        alert("修改失败");
      }
    },

    // 删除会话
    async deleteSession(session) {
      if (!confirm("确认删除此会话吗？")) return;

      const token = localStorage.getItem("token");
      try {
        const res = await fetch(BASE_URL+`/session/${session.id}`, {
          method: "DELETE",
          headers: {
            "Authorization": `Bearer ${token}`
          }
        });

        if (res.ok) {
          this.sessions = this.sessions.filter(s => s.id !== session.id);
          if (this.currentSessionId === session.id) {
            this.currentSessionId = this.sessions.length ? this.sessions[0].id : null;
          }
        } else {
          alert("删除失败");
        }
      } catch (err) {
        console.error(err);
        alert("删除失败");
      }
    }
  }
};
</script>

<style>
.chat-layout {
  display: flex;
  height: 100vh;
}

/* 左侧对话列表 */
.sidebar {
  width: 220px;
  border-right: 1px solid #ccc;
  padding: 10px;
  background: #f9f9f9;
}

.new-session-btn {
  width: 100%;
  margin-bottom: 10px;
  padding: 6px;
}

ul {
  list-style: none;
  padding: 0;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px;
  cursor: pointer;
  margin-bottom: 4px;
}

.session-item.active {
  background: #ddd;
}

.session-title {
  flex: 1;
  margin-right: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 图标按钮 */
.icon-btn {
  cursor: pointer;
  margin-left: 4px;
  font-size: 14px;
  color: #666;
}

.icon-btn:hover {
  color: #333;
}

/* 右侧聊天窗口 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
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
  margin-right: 6px;
}

button {
  padding: 8px 12px;
  border: none;
  background: #4CAF50;
  color: white;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background: #45a049;
}
.home-btn {
  width: 100%;
  margin-bottom: 10px;
  padding: 6px;
  background-color: #f0ad4e;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.home-btn:hover {
  background-color: #ec971f;
}
</style>