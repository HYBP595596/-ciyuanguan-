<template>
  <div class="page-body">
    <nav class="category-wrap">
  <!-- 左箭头 -->
  <div
    class="scroll-arrow left"
    :class="{ 'is-disabled': !canScrollLeft }"
    v-show="showArrows"
    @click="scrollBy(-240)">
    <i class="el-icon-arrow-left"></i>
  </div>

  <!-- 标签滚动区 -->
  <div class="category-bar" ref="categoryBar" @scroll="updateScrollState">
    <div
      class="category-div is-all"
      :class="{ 'is-active': highlightCategoryId == -1 }"
      @click="clickAllCategory">
      <span class="category-name">全部</span>
    </div>

    <div
      class="category-div"
      :class="{ 'is-active': item.id == highlightCategoryId }"
      :style="tagStyle(index)"
      v-for="(item, index) in categoryList"
      :key="index"
      @click="clickCategory(item.id, item.count)">
      <span class="category-name">{{ item.name }}</span>
    </div>
  </div>

  <!-- 右箭头 -->
  <div
    class="scroll-arrow right"
    :class="{ 'is-disabled': !canScrollRight }"
    v-show="showArrows"
    @click="scrollBy(240)">
    <i class="el-icon-arrow-right"></i>
  </div>
</nav>

    <!-- ============ 图片列表 ============ -->
    <section class="image-list-body">
      <div class="image-list" v-if="imageTagList && imageTagList.length">
        <article
          class="image-card"
          v-for="(item, index) in imageTagList"
          :key="index"
          @click="clickImage(item.image.id)">
          <div class="image-detail">
            <img :src="item.image.url" alt=""/>
          </div>

          <div class="score-collects-views">
            <i class="el-icon-medal"></i>
            <span>{{ item.image.score }}</span>
            <i class="el-icon-star-off"></i>
            <span>{{ item.image.collects }}</span>
            <i class="el-icon-view"></i>
            <span>{{ item.image.views }}</span>
          </div>

          <div class="tag-list">
            <el-tag
              v-for="(tag, i) in item.tagList"
              :key="i"
              size="mini"
              type="danger">
              {{ tag.name }}
            </el-tag>
          </div>
        </article>
      </div>

      <el-empty v-else description="暂无图片"></el-empty>

      <div class="pagination">
        <el-pagination
          background
          :page-size="pagination.pageSize"
          :hide-on-single-page="pagination.onlyHasOnePage"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          layout="prev, pager, next"
          :total="pagination.total">
        </el-pagination>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: "IndexBody",
  data() {
    return {
      imageTagList: [],
      pagination: {
        currentPage: 1,
        pageSize: 9,
        total: 0,
        onlyHasOnePage: false
      },
       palette: [
      'linear-gradient(135deg, #667eea, #764ba2)',
      'linear-gradient(135deg, #5b8def, #6fa8dc)',
      'linear-gradient(135deg, #e57373, #c94b4b)',
      'linear-gradient(135deg, #b06ab3, #a259c4)',
      'linear-gradient(135deg, #6a82fb, #5b6ef5)',
      'linear-gradient(135deg, #43cea2, #2bb673)',
      'linear-gradient(135deg, #e0a96d, #d08770)',
      'linear-gradient(135deg, #f093fb, #f5576c)',
      'linear-gradient(135deg, #4facfe, #00f2fe)',
      'linear-gradient(135deg, #43e97b, #38f9d7)'
    ],
    canScrollLeft: false,
    canScrollRight: false,
    showArrows: false,
      categoryList: [],
      searchCondition: 0,
      highlightCategoryId: -1,
      currentCategoryCount: 0,
      keyword: ''
    }
  },
  mounted() {
    this.event.$on('searchImageWithKeyword', keyword => {
      this.getImageListWithKeyword(keyword)
    })
    this.getCategoryList()
    this.handleRouter()
    this.$nextTick(() => {
  this.updateScrollState()
  window.addEventListener('resize', this.updateScrollState)
})
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateScrollState)
  },
  methods: {
    clickAllCategory() {
      this.pagination.currentPage = 1
      this.highlightCategoryId = -1
      if (this.searchCondition != 0) this.searchCondition = 0
      this.getImageListWithNoCondition()
    },
    tagStyle(index) {
    return { background: this.palette[index % this.palette.length] }
  },
    handleRouter() {
      if (this.$route.query.categoryId) {
        this.highlightCategoryId = this.$route.query.categoryId
        this.currentCategoryCount = this.$route.query.count
        this.clickCategory(this.highlightCategoryId, this.currentCategoryCount)
      } else {
        this.getImageListWithNoCondition()
      }
    },
    clickImage(imageId) {
      this.$router.push({
        path: '/image-detail',
        query: { imageId, highlightCategoryId: this.highlightCategoryId }
      })
    },
    getImageListWithKeyword(keyword) {
      this.keyword = keyword
      if (this.highlightCategoryId != -1) this.highlightCategoryId = -1
      this.pagination.currentPage = 1
      if (this.searchCondition != 2) this.searchCondition = 2
      this.request.get(`GetImageListWithCondition?keyword=${keyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true
        this.pagination.total = res.total
      })
    },
    getImageListWithKeywordWhenChangePage(keyword) {
      this.request.get(`GetImageListWithCondition?keyword=${keyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList
        this.pagination.total = res.total
      })
    },
    clickCategory(categoryId, count) {
      this.pagination.currentPage = 1
      this.highlightCategoryId = categoryId
      this.currentCategoryCount = count
      if (this.searchCondition != 1) this.searchCondition = 1
      this.request.get(`GetImageListWithCategoryId?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}&categoryId=${categoryId}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true
        this.pagination.total = count
      })
    },
    getImageListWithCategoryIdWhenChangePage(categoryId, count) {
      this.request.get(`GetImageListWithCategoryId?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}&categoryId=${categoryId}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true
        this.pagination.total = count
      })
    },
    getImageListWithNoCondition() {
      this.request.get(`/GetImageListWithNoCondition?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true
        this.pagination.total = res.total
      })
    },
    getCategoryList() {
      this.request.get(`GetAllCategory`).then(res => {
        this.categoryList = res.categoryList
      })
    },
    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage
      if (this.searchCondition == 0) {
        this.getImageListWithNoCondition()
      } else if (this.searchCondition == 1) {
        this.getImageListWithCategoryIdWhenChangePage(this.highlightCategoryId, this.currentCategoryCount)
      } else if (this.searchCondition == 2) {
        this.getImageListWithKeywordWhenChangePage(this.keyword)
      }
    },
    tagStyle(index) {
  return { background: this.palette[index % this.palette.length] }
},

scrollBy(offset) {
  const bar = this.$refs.categoryBar
  if (!bar) return
  bar.scrollBy({ left: offset, behavior: 'smooth' })
},

updateScrollState() {
  const bar = this.$refs.categoryBar
  if (!bar) return
  const { scrollLeft, scrollWidth, clientWidth } = bar
  this.showArrows = scrollWidth > clientWidth + 4   // 溢出才显示箭头
  this.canScrollLeft = scrollLeft > 2
  this.canScrollRight = scrollLeft + clientWidth < scrollWidth - 2
}
  }
}
</script>

<style scoped src="./IndexBody.css"></style>