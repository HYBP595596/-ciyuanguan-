<template>
  <div class="layout">
    <!-- 顶部栏 -->
    <HeaderNav
      :sidebar-collapsed="sidebarCollapsed"
      @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
      :is-login="isLogin"
      :login-dialog-visible.sync="loginDialogVisible"
    />

    <div class="layout-body">
      <!-- 侧边栏 -->
      <SidebarNav
        :collapsed="sidebarCollapsed"
        :event="event"
        :is-login="isLogin"
        @show-login="loginDialogVisible = true"
      />

      <!-- 主内容区 -->
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import HeaderNav from '@/components/HeaderNav/HeaderNav.vue'
import SidebarNav from '@/components/Admin/AsideMenu/AsideMenu.vue'

export default {
  components: { HeaderNav, SidebarNav },
  data() {
    return {
      sidebarCollapsed: false,          // 默认展开侧栏
      isLogin: !!sessionStorage.getItem('token'),
      loginDialogVisible: false
    }
  }
}
</script>

<style>
/* ========== 整体骨架：100vh 撑满、页面级不滚动 ========== */
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

/* 顶栏以下区域：flex 自动分掉剩余高度 */
.layout-body {
  display: flex;
  flex: 1;
  min-height: 0;          /* 关键：让内部 overflow 生效 */
  overflow: hidden;
}

/* 主内容区：滚动只发生在这里 */
.main-content {
  flex: 1;
  min-width: 0;           /* 关键：防止内容撑破 flex 容器 */
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--bg-page);
  color: var(--text-primary);
}
</style>