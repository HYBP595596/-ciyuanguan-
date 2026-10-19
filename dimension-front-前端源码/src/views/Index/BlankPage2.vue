<template>

</template>

<script>
export default {
  name: "BlankPage2",
  mounted() {
    this.handleRouter()
  },
  methods: {
    handleRouter() {
      const activeUserTabName = this.$route.query.activeUserTabName
      console.log('blank2', activeUserTabName)
      this.$router.push({
        path: '/user-center',
        query: {
          'activeUserTabName': activeUserTabName
        }
      })
    }
  }
}
</script>

<style scoped>

</style>