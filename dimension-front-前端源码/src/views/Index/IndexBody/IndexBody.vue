<template>
  <div class="page-body">
    <!-- ============ 顶部热门标签 ============ -->
    <nav class="tag-wrap">
      <div class="tag-title">热门标签</div>

      <!-- 左箭头 -->
      <div class="scroll-arrow left" :class="{ 'is-disabled': !canScrollLeft }" v-show="showArrows" @click="scrollBy(-240)">
        <i class="el-icon-arrow-left"></i>
      </div>

      <!-- 标签滚动区 -->
      <div class="tag-bar" ref="categoryBar" @scroll="updateScrollState">
        <!-- 全部按钮 -->
        <div class="tag-item" :class="{ 'is-active': currentTagId === -1 && currentCategoryId === -1 }" @click="clickAllTags">
          <span>全部最新</span>
        </div>

        <!-- 标签列表 -->
        <div
            class="tag-item"
            :class="{ 'is-active': item.id === currentTagId }"
            v-for="(item, index) in tagList"
            :key="index"
            @click="clickTag(item)">
          #{{ item.name }}
        </div>
      </div>

      <!-- 右箭头 -->
      <div class="scroll-arrow right" :class="{ 'is-disabled': !canScrollRight }" v-show="showArrows" @click="scrollBy(240)">
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
            <img :src="$img(item.image.url)" alt=""/>
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
      tagList: [],
      currentTagId: -1,
      currentCategoryId: -1,
      searchCondition: 0,    // 0-全部 1-分类 2-标签/关键字
      keyword: '',
      canScrollLeft: false,
      canScrollRight: false,
      showArrows: false
    }
  },
  mounted() {
    this.event.$on('searchImageWithKeyword', keyword => {
      this.currentTagId = -1;
      this.currentCategoryId = -1;
      this.getImageListWithKeyword(keyword);
    })
    this.getTagList()

    this.$nextTick(() => {
      this.updateScrollState()
      window.addEventListener('resize', this.updateScrollState)
    })
  },
  watch: {
    // 监听路由参数 categoryId 的变化（左侧菜单点击时触发）
    '$route.query.categoryId': {
      handler(newVal) {
        if (newVal) {
          this.currentCategoryId = Number(newVal);
          this.currentTagId = -1;
          this.pagination.currentPage = 1;
          this.searchCondition = 1;
          this.getImageListWithCategoryId(); // 👈 调用分类查询
        } else {
          // 如果没有参数，查全部
          this.currentCategoryId = -1;
          this.pagination.currentPage = 1;
          this.searchCondition = 0;
          this.getImageListWithNoCondition();
        }
      },
      immediate: true
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.updateScrollState)
  },
  methods: {
    // 1. 点击图片跳转详情
    clickImage(imageId) {
      this.$router.push({
        path: '/image-detail',
        query: { imageId: imageId }
      })
    },

    // 2. 获取标签列表
    getTagList() {
      this.request.get(`GetAllTag`).then(res => {
        this.tagList = res.tagList ? res.tagList.slice(0, 20) : [];
        this.$nextTick(() => {
          this.updateScrollState();
        });
      })
    },

    // 3. 点击“全部最新”
    clickAllTags() {
      this.currentTagId = -1;
      this.keyword = '';
      // 如果当前 URL 有分类参数，需要清空，这步会触发 watch 重置回大厅
      if (this.$route.query.categoryId) {
        this.$router.push({ path: '/index-body' });
      } else {
        this.currentCategoryId = -1;
        this.pagination.currentPage = 1;
        this.searchCondition = 0;
        this.getImageListWithNoCondition();
      }
    },

    // 4. 点击某个标签
    clickTag(tag) {
      this.currentTagId = tag.id;
      this.currentCategoryId = -1; // 标签和分类互斥，清空分类
      this.keyword = tag.name;
      this.pagination.currentPage = 1;
      this.searchCondition = 2;
      this.getImageListWithKeyword(tag.name);
    },

    // 5. 按关键字/标签获取图片（从第一页搜）
    getImageListWithKeyword(keyword) {
      this.keyword = keyword;
      this.pagination.currentPage = 1;
      this.searchCondition = 2;
      this.request.get(`GetImageListWithCondition?keyword=${keyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList;
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true;
        this.pagination.total = res.total;
      })
    },

    // 6. 按关键字/标签翻页
    getImageListWithKeywordWhenChangePage(keyword) {
      this.request.get(`GetImageListWithCondition?keyword=${keyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList;
        this.pagination.total = res.total;
      })
    },

    // 7. 无条件获取图片（首页默认显示）
    getImageListWithNoCondition() {
      this.request.get(`/GetImageListWithNoCondition?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList;
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true;
        this.pagination.total = res.total;
      })
    },

    // ================= 🚨 刚刚缺失的分类方法补全了 =================
    // 8. 按分类获取图片（第一页）
    getImageListWithCategoryId() {
      this.request.get(`GetImageListWithCategoryId?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}&categoryId=${this.currentCategoryId}`).then(res => {
        this.imageTagList = res.imageTagList;
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true;
        // 注意：这里需要后端返回对应的 total，如果没有返回就是 0
        this.pagination.total = res.total || 0;
      })
    },

    // 9. 按分类获取图片（翻页）
    getImageListWithCategoryIdWhenChangePage() {
      this.request.get(`GetImageListWithCategoryId?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}&categoryId=${this.currentCategoryId}`).then(res => {
        this.imageTagList = res.imageTagList;
        if (this.imageTagList && this.imageTagList.length <= 9) this.pagination.onlyHasOnePage = true;
        this.pagination.total = res.total || 0;
      })
    },
    // ==============================================================

    // 10. 分页切换（补齐了分类的翻页逻辑）
    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage;
      if (this.searchCondition === 0) {
        this.getImageListWithNoCondition();
      } else if (this.searchCondition === 1) {
        this.getImageListWithCategoryIdWhenChangePage(); // 👈 补上了分类翻页
      } else if (this.searchCondition === 2) {
        this.getImageListWithKeywordWhenChangePage(this.keyword);
      }
    },

    // 11. 标签栏左右滑动箭头逻辑
    scrollBy(offset) {
      const bar = this.$refs.categoryBar;
      if (!bar) return;
      bar.scrollBy({ left: offset, behavior: 'smooth' });
    },
    updateScrollState() {
      const bar = this.$refs.categoryBar;
      if (!bar) return;
      const { scrollLeft, scrollWidth, clientWidth } = bar;
      this.showArrows = scrollWidth > clientWidth + 4;
      this.canScrollLeft = scrollLeft > 2;
      this.canScrollRight = scrollLeft + clientWidth < scrollWidth - 2;
    }
  }
}
</script>

<style scoped src="./IndexBody.css"></style>