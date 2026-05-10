import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173, //前端端口
    proxy: {
 
      '/api': {
        target: 'http://localhost:8081', //后端地址
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '') // 去掉/api前缀
      }
    }
  }
})