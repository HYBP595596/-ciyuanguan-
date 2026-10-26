<template>
  <div class="admin-sidebar">
    <div class="admin-logo">
      <i class="el-icon-s-platform"></i>
      <span>次元馆 · 管理后台</span>
    </div>
    <el-menu
        :default-active="$route.path"
        class="admin-menu"
        router
        background-color="#1e293b"
        text-color="#cbd5e1"
        active-text-color="#60a5fa">
      <el-menu-item index="/image-management-image-management">
        <i class="el-icon-picture-outline"></i>
        <span slot="title">图片管理</span>
      </el-menu-item>
      <el-menu-item index="/audit-management">
        <i class="el-icon-s-check"></i>
        <span slot="title">作品审核</span>
      </el-menu-item>
      <el-menu-item index="/artist-management">
        <i class="el-icon-user"></i>
        <span slot="title">画师审核</span>
      </el-menu-item>
      <el-menu-item index="/comment-management">
        <i class="el-icon-chat-dot-round"></i>
        <span slot="title">评论管理</span>
      </el-menu-item>
      <el-menu-item index="/user-management">
        <i class="el-icon-user-solid"></i>
        <span slot="title">用户管理</span>
      </el-menu-item>
      <el-menu-item index="/category-management">
        <i class="el-icon-menu"></i>
        <span slot="title">分类管理</span>
      </el-menu-item>
      <el-menu-item index="/order-management">
        <i class="el-icon-s-order"></i>
        <span slot="title">约稿订单</span>
      </el-menu-item>
      <el-menu-item index="/notice-management">
        <i class="el-icon-bell"></i>
        <span slot="title">公告管理</span>
      </el-menu-item>
      <el-menu-item index="/admin-account-management">
        <i class="el-icon-s-custom"></i>
        <span slot="title">管理员管理</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default { name: "AdminAsideMenu" }
</script>

<style scoped>
.admin-sidebar { height: 100%; background: #1e293b; display: flex; flex-direction: column; }
.admin-logo {
  height: 64px; line-height: 64px; text-align: center;
  color: #fff; font-size: 16px; font-weight: 700;
  border-bottom: 1px solid #334155; letter-spacing: 1px;
}
.admin-logo i { margin-right: 8px; color: #60a5fa; font-size: 18px; }
.admin-menu { border-right: none; flex: 1; padding-top: 8px; }
.admin-menu .el-menu-item { height: 46px; line-height: 46px; }
.admin-menu .el-menu-item.is-active { background: #1e40af !important; border-right: 3px solid #60a5fa; }
</style>