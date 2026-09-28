<template>
  <!--前台用户首页总体页面 // 用来组合各个组件，作为父路由-->
  <div class="body">
    <!--引入头部导航栏组件 // 固定不变-->
    <HeaderNav class="headerNav"/>
    <!--具体子页面展示区域 // 在此展示子路由，会根据path路径而展示不同的子页面 // 可变展示区域-->
    <router-view class="child-body"/>
    <!--引入底部页脚组件 // 固定不变-->
    <Footer v-show="$route.path != '/user-center'"/>
  </div>
</template>

<script>
import HeaderNav from "@/components/HeaderNav/HeaderNav";
import Footer from "@/components/Footer/Footer";
export default {
  name: "Index",
  components: {Footer, HeaderNav}
}
</script>

<style scoped>

</style>