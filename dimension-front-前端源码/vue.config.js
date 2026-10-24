const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    host: '0.0.0.0',
    port: 8080,
    strictPort: true,
    allowedHosts: 'all',
    proxy: {
      '/dimension_dev_01_war_exploded/': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})