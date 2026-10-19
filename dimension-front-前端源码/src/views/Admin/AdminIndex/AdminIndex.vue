<template>
  <div class="admin-body">
    <el-container class="container">
      <el-aside width="220px" class="aside">
        <AsideMenu/>
      </el-aside>

      <el-container class="right-container">
        <el-header class="header" height="64px">
          <Header/>
        </el-header>
        <el-main class="main">
          <router-view class="child-page"/>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import AsideMenu from "@/components/Admin/AsideMenu/AsideMenu";
import Header from "@/components/Admin/Header/Header";
export default {
  name: "AdminIndex",
  components: { Header, AsideMenu }
}
</script>

<style scoped>
.admin-body {
  height: 100vh;
  overflow: hidden;
  background: var(--bg-page);
}
.container {
  height: 100%;
}
.aside {
  height: 100%;
  overflow: hidden;
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
}
.right-container {
  min-width: 0;
  height: 100%;
}
.header {
  padding: 0 !important;
  background: var(--bg-header);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 4px var(--shadow-color);
  flex-shrink: 0;
}
.main {
  padding: 0;
  overflow-y: auto;
  background: var(--bg-page);
}
.child-page {
  min-height: 100%;
}
</style>