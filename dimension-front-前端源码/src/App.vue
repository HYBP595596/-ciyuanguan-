<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default { name: 'App' }
</script>

<style>
/* ===================== 全局重置（关键：height 只给三层） ===================== */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

/* ===================== 主题变量 ===================== */
:root {
  --bg-page: #f5f5f5;
  --bg-card: #ffffff;
  --bg-header: #ffffff;
  --bg-sidebar: #ffffff;
  --bg-hover: #f5f7fa;
  --bg-active: #ecf5ff;
  --text-primary: #333333;
  --text-secondary: #555555;
  --text-muted: #9aa0a6;
  --border-color: #e6e6e6;
  --shadow-color: rgba(0, 0, 0, 0.06);
}

html.theme-dark {
  --bg-page: #1a1a1a;
  --bg-card: #242424;
  --bg-header: #242424;
  --bg-sidebar: #242424;
  --bg-hover: #333333;
  --bg-active: #2a3a4a;
  --text-primary: #e8e8e8;
  --text-secondary: #c0c0c0;
  --text-muted: #888888;
  --border-color: #3a3a3a;
  --shadow-color: rgba(0, 0, 0, 0.4);
}

/* ===================== 全局基础样式 ===================== */
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI",
               "PingFang SC", "Microsoft YaHei", "Helvetica Neue",
               Helvetica, Arial, sans-serif;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-primary);
  background: var(--bg-page);
  transition: background-color 0.25s ease, color 0.25s ease;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  color: inherit;
  text-decoration: none;
}

img {
  display: block;                /* 干掉行内图片底部 3px 空隙 */
  max-width: 100%;
}

/* ===================== 滚动条美化 ===================== */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}

/* ===================== Element UI 干扰清除 ===================== */
.el-menu {
  border-right: none !important;
}
.el-card {
  background: var(--bg-card) !important;
  border: 1px solid var(--border-color) !important;
  color: var(--text-primary) !important;
}
.el-dialog {
  border-radius: 10px;
}
</style>