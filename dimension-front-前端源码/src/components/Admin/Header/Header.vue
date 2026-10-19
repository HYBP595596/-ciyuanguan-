<template>
  <!--头部系统标题和管理员信息展示组件-->
  <div class="body">
    <!--标题-->
    <div class="title">
      <span>次元馆后台管理系统</span>
    </div>
    <!--管理员信息-->
    <div class="admin-info">
      <!--管理员头像-->
      <div class="admin-avatar">
        <img :src="admin.avatar" class="body-image"/>
      </div>
      <!--管理员昵称-->
      <div class="admin-nickname">
        <span>{{admin.nickname}}</span>
      </div>
    </div>
    <!--注销按钮-->
    <div class="logout-button">
      <el-button type="plain" @click="adminLogout">注销</el-button>
    </div>
  </div>
</template>

<script>
import Cookies from 'js-cookie'

export default {
  name: "Header",
  data() {
    return {
      admin: {}
    }
  },
  mounted() {
    if (!sessionStorage.getItem('adminToken')) {
      this.$message.error('请先登录')
      this.$router.push('/admin-login')
    }

    if (sessionStorage.getItem('admin')) {
      this.admin = JSON.parse(sessionStorage.getItem('admin'))
      console.log(this.admin)
    }
  },
  methods: {
    adminLogout() {
      // 删除cookie的adminToken和admin
      sessionStorage.clear()
      // 跳转到管理员登录页
      this.$router.push('/admin-login')
    }
  }
}
</script>

<style scoped src="./Header.css">

</style>