import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'


export default defineConfig({
  base: '/ds/',
  plugins: [
    vue(),
    vueDevTools(),
  ],
  // Dev server proxy: forward requests starting with /api to the Spring backend
  // This avoids CORS during development. The backend is expected at http://localhost:8080
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // Remove the /api prefix when forwarding if your Spring controllers don't expect it
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
