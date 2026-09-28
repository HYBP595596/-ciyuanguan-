<template>
  <!--后台系统首页总体页面 // 组合左侧菜单导航栏组件、头部信息栏组件、中部子页面-->
  <div class="body" style="overflow: auto">
    <el-container class="container">
      <!--左侧导航菜单栏-->
      <el-aside width="220px" class="aside">
        <AsideMenu/>
      </el-aside>
      <!--右侧布局-->
      <el-container>
        <!--头部信息栏-->
        <el-header class="header">
          <Header/>
        </el-header>
        <!--中部子页面 // 子路由展动态展示子页面-->
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
  components: {Header, AsideMenu}
}
</script>

<style scoped>
.aside {
  height: 100vh;
}
</style>