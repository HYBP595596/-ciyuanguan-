<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<style>
*, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

/* ===== 浅色主题（默认） ===== */
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

/* ===== 深色主题 ===== */
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

/* 全局过渡，切换时更顺滑 */
body {
  background: var(--bg-page);
  color: var(--text-primary);
  transition: background-color 0.25s ease, color 0.25s ease;
}
</style>
<!-- set JAVA_HOME=C:\Program Files\Java\JDK
set JRE_HOME=C:\Program Files\Java\JDK
cd /d D:\apache-tomcat-9.0.xx\apache-tomcat-9.0.122\bin
startup.bat -->