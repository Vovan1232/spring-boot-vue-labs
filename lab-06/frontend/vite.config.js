import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: process.env.API_TARGET || 'http://127.0.0.1:8080',
        configure(proxy) {
          proxy.on('error', (_error, _request, response) => {
            if (!response.headersSent && !response.writableEnded) {
              response.writeHead(502).end()
            }
          })
        },
      },
    },
  },
})
