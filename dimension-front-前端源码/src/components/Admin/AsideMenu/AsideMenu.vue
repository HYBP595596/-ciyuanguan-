<template>
  <!--左侧导航菜单栏组件-->
  <div class="body">
    <el-menu
        style="height: 100vh"
        router
        :default-active="$route.path"
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
        class="el-menu-vertical-demo">
      <el-submenu index="1">
        <template slot="title">
          <i class="el-icon-location"></i>
          <span>图片管理</span>
        </template>
        <el-menu-item index="/image-management-image-management">
          <i class="el-icon-menu"></i>
          <span slot="title">图片管理</span>
        </el-menu-item>
        <el-menu-item index="/image-management-add-image">
          <i class="el-icon-menu"></i>
          <span slot="title">新增图片</span>
        </el-menu-item>
      </el-submenu>
      <el-menu-item index="/user-management">
        <i class="el-icon-menu"></i>
        <span slot="title">用户管理</span>
      </el-menu-item>
      <el-menu-item index="/comment-management">
        <i class="el-icon-menu"></i>
        <span slot="title">评论管理</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: "AsideMenu"
}
</script>

<style scoped src="./AsideMenu.css">
.body {
  height: 1000px;
}
</style>