<template>
  <div class="layout">
    <HeaderNav
      :sidebar-collapsed="sidebarCollapsed"
      @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed"
      :is-login="isLogin"
      :login-dialog-visible.sync="loginDialogVisible"
    />

    <div class="layout-body">
      <SidebarNav
        :collapsed="sidebarCollapsed"
        :event="event"
        :is-login="isLogin"
        @show-login="loginDialogVisible = true"
      />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script>
import HeaderNav from '@/components/HeaderNav/HeaderNav.vue'
import SidebarNav from '@/components/Admin/AsideMenu/AsideMenu.vue'

export default {
  name: 'Index',
  components: { HeaderNav, SidebarNav },
  data() {
    return {
      sidebarCollapsed: false,
      isLogin: !!sessionStorage.getItem('token'),
      loginDialogVisible: false
    }
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.layout-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.main-content {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--bg-page);
  color: var(--text-primary);
}
</style>