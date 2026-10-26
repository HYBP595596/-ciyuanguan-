<template>
  <div class="admin-body">
    <el-container class="container">
      <el-aside width="220px" class="aside">
        <AdminAsideMenu />
      </el-aside>
      <el-container class="right-container">
        <el-header class="header" height="64px"><Header/></el-header>
        <el-main class="main"><router-view class="child-page"/></el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import AdminAsideMenu from "@/components/Admin/AdminAsideMenu/AdminAsideMenu";
import Header from "@/components/Admin/Header/Header";
export default { name: "AdminIndex", components: { Header, AdminAsideMenu } }
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
  /* 兜底：页头内容永远不可能溢出到内容区上（历史上就是这样被压住的） */
  overflow: hidden;
  box-sizing: border-box;
}
.main {
  padding: 0;
  overflow-y: auto;
  background: var(--bg-page);
  /* 内容区滚动条常驻占位，切换页面时不会因为滚动条出现/消失而抖动 */
  scrollbar-gutter: stable;
}
.child-page {
  min-height: 100%;
}
</style>