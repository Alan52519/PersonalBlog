import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src') // 关键：把 @ 指向 src
      }
    },
  server: {
    host: true,       // 监听 0.0.0.0，允许局域网访问
    port: 5173,       // 指定端口
    strictPort: false, // 如果端口被占用，可以自动换端口
  }
});
