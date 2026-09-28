<template>
  <!--首页图片展示页-->
  <div class="body">
    <!--左侧分类标签列表-->
    <div class="category">
      <!--全部分类-->
      <div class="category-div body-image2" @click="clickAllCategory">
        <span class="new-font" style="color: rgb(255,116,66); font-weight: bolder; font-size: 17px">全部</span>
      </div>
      <!--各个分类块-->
      <div  class="category-div body-image2" v-for="(item, index) in categoryList" :key="index" @click="clickCategory(item.id, item.count)"
           :style="{backgroundColor: item.id == highlightCategoryId ? 'red' : ''}">

<!--        <el-badge :value="item.count" class="item" type="primary">-->
<!--            <span style="margin-right: 10px">{{item.name}}</span>-->
<!--        </el-badge>-->

        <!--分类名称-->
        <span class="new-font" style="color: rgb(255,116,66); font-weight: bolder; font-size: 17px">{{item.name}}</span>
        <!--分类下的图片数-->
        <span style="position: relative; right: -7px; top: -7px; color: rgb(89,91,224)">{{item.count}}</span>
      </div>
    </div>
    <!--右侧图片列表-->
    <div class="image-list-body">
      <!--图片列表区域-->
      <div class="image-list">
        <!--图片具体展示区-->
        <div class="image body-image" v-for="(item, index) in imageTagList" :key="index" @click="clickImage(item.image.id)">
          <!--图片-->
          <div class="image-detail">
            <img :src="item.image.url" class="body-image1"/>
          </div>
          <!--分数、收藏数、浏览数展示-->
          <div class="score-collects-views" >
            <i class="el-icon-medal"></i>
            <span>{{item.image.score}}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <i class="el-icon-star-off"></i>
            <span>{{item.image.collects}}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <i class="el-icon-view"></i>
            <span>{{item.image.views}}</span>
          </div>
          <!--标签列表-->
          <div class="tag-list">
            <el-tag style="margin-left: 10px; font-size: 14px" type="danger" v-for="(item1, index1) in item.tagList" :key="index1">
              {{item1.name}}
            </el-tag>
          </div>
        </div>
      </div>
      <!--图片分页器-->
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
    </div>
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
      categoryList: [],
      searchCondition: 0, // 0-无条件 1-分类条件 2-关键字条件
      highlightCategoryId: -1, // 高亮展示分类的id
      currentCategoryCount: 0, // 当前点击分类下的图片
      keyword: '' // 查询关键字
    }
  },
  mounted() { // 当这个页面挂载成功
    // 只能传递字符串参数
    // if (this.$route.query.load == '1') {
    //   console.log('load执行了')
    //   this.$router.go()
    // }

    // 监听头部导航栏关键字搜索事件
    this.event.$on('searchImageWithKeyword', keyword => {
      this.getImageListWithKeyword(keyword)
    })
    // 获取所有分类
    this.getCategoryList()
    // 接收图片详情页跳转过来的参数并处理
    this.handleRouter()
  },
  methods: {
    // 点击所有分类，无条件查询，且从第一页开始查，且其他分类不高亮，且当前搜索状态为无条件搜索
    clickAllCategory() {
      this.pagination.currentPage = 1
      this.highlightCategoryId = -1
      if (this.searchCondition != 0) {
        this.searchCondition = 0
      }
      this.getImageListWithNoCondition()
    },
    // 从图片详情页点击分类跳转到首页
    handleRouter() {
      // 如果是从图片详情页跳转过来的
      if (this.$route.query.categoryId) {
        this.highlightCategoryId = this.$route.query.categoryId
        this.currentCategoryCount = this.$route.query.count
        // 相当于点击分类查询
        this.clickCategory(this.highlightCategoryId, this.currentCategoryCount)
      } else {
        // 否则，无条件查询
        this.getImageListWithNoCondition()
      }
    },
    // 点击图片跳转
    clickImage(imageId) {
      this.$router.push({
        path: '/image-detail',
        query: {
          imageId,
          highlightCategoryId: this.highlightCategoryId
        }
      })
    },
    // 点击搜索，根据关键字模糊检索图片列表
    getImageListWithKeyword(keyword) {
      // 如果keyword是空呢 - 那么就要无条件搜索了 - 但也还是从第一页开始搜 - 但此时的搜索状态就是无条件搜索了 - 不对 - 不用 - 加个ALL分类即可
      // 那么如果为空，则不进行操作即可 - 导航栏页已经进行了判断
      this.keyword = keyword // 保存keyword，为分页搜索做准备
      // 搜索时不带分类
      if (this.highlightCategoryId != -1) {
        this.highlightCategoryId = -1
      }
      // 点击搜索，都要从第一页开始查
      this.pagination.currentPage = 1
      // 表示处于搜索查询状态
      if (this.searchCondition != 2) {
        this.searchCondition = 2
      }
      this.request.get(`GetImageListWithCondition?keyword=${keyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) {
          this.pagination.onlyHasOnePage = true
        }
        this.pagination.total = res.total
      })
    },
    // 搜索情况下切换页查询
    getImageListWithKeywordWhenChangePage(keyword) {
      this.request.get(`GetImageListWithCondition?keyword=${keyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList
        // if (this.imageTagList.length <= 9) {
        //   this.pagination.onlyHasOnePage = true
        // }
        this.pagination.total = res.total
      })
    },
    // 点击分类查询图片列表
    clickCategory(categoryId, count) {
      // 点击分类，都从第一页开始查
      this.pagination.currentPage = 1
      // 高亮该分类 - 同时为换页查询使用
      this.highlightCategoryId = categoryId
      // 记录改分类下的图片数，为之后换页查询使用
      this.currentCategoryCount = count
      // 切换搜索状态为根据分类搜索
      if (this.searchCondition != 1) {
        this.searchCondition = 1
      }
      this.request.get(`GetImageListWithCategoryId?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}&categoryId=${categoryId}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) {
          this.pagination.onlyHasOnePage = true
        }
        this.pagination.total = count
      })
    },
    // 点击分类的情况下切换页查询
    getImageListWithCategoryIdWhenChangePage(categoryId, count) {
      this.request.get(`GetImageListWithCategoryId?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}&categoryId=${categoryId}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) {
          this.pagination.onlyHasOnePage = true
        }
        this.pagination.total = count
      })
    },
    // 无条件获取图片列表
    getImageListWithNoCondition() {
      this.request.get(`/GetImageListWithNoCondition?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagList = res.imageTagList
        if (this.imageTagList && this.imageTagList.length <= 9) {
          this.pagination.onlyHasOnePage = true
        }
        this.pagination.total = res.total
      })
    },
    // 获取所有分类列表
    getCategoryList() {
      this.request.get(`GetAllCategory`).then(res => {
        this.categoryList = res.categoryList
      })
    },
    // 页面改变触发
    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage

      if (this.searchCondition == 0) {
        this.getImageListWithNoCondition()
      } else if (this.searchCondition == 1) {
        this.getImageListWithCategoryIdWhenChangePage(this.highlightCategoryId, this.currentCategoryCount)
      } else if (this.searchCondition == 2) {
        this.getImageListWithKeywordWhenChangePage(this.keyword)
      }
    }
  }
}
</script>

<style scoped src="./IndexBody.css">

</style>