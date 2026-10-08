import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    server: {
      // 在这里设置你想要的端口号，例如 8080
      port: 9000,
      // 如果端口被占用，是否自动尝试其他端口
      strictPort: true,
      // 添加代理配置，使用环境变量
      proxy: {
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:9001',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, '')
        }
      }
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
  }
})
