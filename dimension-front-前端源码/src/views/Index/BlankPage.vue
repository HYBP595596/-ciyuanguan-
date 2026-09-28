<template>
  <!--间接路由跳转中介页-->
  <div>

  </div>
</template>

<script>
export default {
  name: "BlankPage",
  mounted() {
    // 重复跳转到图片详情页
    this.duplicateRedirectToImageDetail()
  },
  methods: {
    // 重复跳转到图片详情页
    duplicateRedirectToImageDetail() {
      let imageId = this.$route.query.imageId
      let highlightCategoryId = this.$route.query.highlightCategoryid
      this.$router.push({
        path: '/image-detail',
        query: {
          imageId,
          highlightCategoryId
        }
      })
    }
  }
}
</script>

<style scoped>

</style>