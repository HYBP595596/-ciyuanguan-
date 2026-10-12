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
      sidebarCollapsed: true,  // ✅ 默认收起
      isLogin: !!sessionStorage.getItem('token'),
      loginDialogVisible: false
    }
  }
}
</script>

<style>
.layout {
  min-height: 100vh;
}
.layout-body {
  display: flex;
  height: calc(100vh - 60px);
}
.main-content {
  flex: 1;
  overflow-y: auto;
  background: #f5f5f5;
}
</style>