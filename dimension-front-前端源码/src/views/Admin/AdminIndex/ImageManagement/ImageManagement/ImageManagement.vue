<template>
  <!--图片管理页-->
  <div class="body">
    <!--搜索输入框和添加按钮-->
    <div class="search-input-and-add-button">
      <!--搜索输入框-->
      <div class="search-input">
        <el-input class="placeholder" clearable v-model="keyword" placeholder="关键字"></el-input>
        <el-button class="search" type="primary" icon="el-icon-search" @click="searchImageTable">搜索</el-button>
      </div>
      <!--新增按钮-->
      <div class="add-button">
        <el-button type="warning" icon="el-icon-circle-plus" @click="$router.push('/image-management-add-image')">新增</el-button>
      </div>
    </div>
    <!--图片展示表格-->
    <div class="image-table">
        <el-table
            stripe
            :data="imageTagCategoryTable"
            style="width: 100%">
          <el-table-column
              align="center"
              label="图片"
              width="250">
            <template slot-scope="scope">
                <img :src="scope.row.image.url" class="image-image"/>
            </template>
          </el-table-column>
          <el-table-column
              label="标签"
              width="200">
            <template slot-scope="scope">
                <span v-for="(item, index) in scope.row.tagList" :key="index">
                  {{item.name}}
                </span>
            </template>
          </el-table-column>
          <el-table-column
              prop="image.id"
              label="ID"
              width="100">
          </el-table-column>
          <el-table-column
              prop="category.name"
              label="分类"
              width="100">
          </el-table-column>
          <el-table-column
              prop="category.id"
              label="分类ID"
              width="100">
          </el-table-column>
          <el-table-column
              prop="image.score"
              label="分数"
              width="100">
          </el-table-column>
          <el-table-column
              prop="image.collects"
              label="收藏数"
              width="100">
          </el-table-column>
          <el-table-column
              prop="image.comments"
              label="评论数"
              width="100">
          </el-table-column>
          <el-table-column
              prop="image.votes"
              label="投票数"
              width="100">
          </el-table-column>
          <el-table-column
              prop="image.views"
              label="浏览数"
              width="100">
          </el-table-column>
          <el-table-column align="center" label="操作">
            <template slot-scope="scope">
              <el-button
                  size="mini"
                  type="primary"
                  @click="updateImageDetail(scope.row)">修改</el-button>
              <el-popconfirm
                  @confirm="deleteImage(scope.row)"
                  title="确定要删除该图片？">
                <el-button
                    slot="reference"
                    size="mini"
                    type="danger">删除</el-button>
              </el-popconfirm>

            </template>
          </el-table-column>
        </el-table>
    </div>
    <!--分页器-->
    <div class="pagination">
      <el-pagination
          background
          @size-change="handleSizeChange"
          :page-size="pagination.pageSize"
          :page-sizes="[3, 5, 10, 20, 30]"
          :hide-on-single-page="pagination.onlyHasOnePage"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          layout="total, sizes, prev, pager, next"
          :total="pagination.total">
      </el-pagination>
    </div>

    <!--******************************************-->

    <!--其他并不直接展示到页面上的信息 // 如对话框之类的-->
    <div class="other">
      <!--修改图片信息对话框-->
      <div class="update-image-dialog">
        <el-dialog
            title="修改图片信息"
            :visible.sync="updateImageDialogVisible"
            width="30%">
          <!--修改表单-->
          <div class="update-form">
            <el-form :model="updateImageForm" ref="updateImageForm" label-width="100px" class="demo-ruleForm">
              <el-form-item label="分类">
                <el-select v-model="updateImageForm.categoryValueId" filterable placeholder="请选择分类">
                  <el-option
                      v-for="(item, index) in categoryList"
                      :key="index"
                      :label="item.name"
                      :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="标签">
                <el-select v-model="updateImageForm.tagListValueIds" filterable multiple placeholder="请选择标签">
                  <el-option
                      v-for="(item, index) in tagList"
                      :key="index"
                      :label="item.name"
                      :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <!--保存按钮-->
          <div class="save-button">
            <el-button type="primary" @click="save">保存</el-button>
          </div>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "ImageManagement",
  data() {
    return {
      keyword: '',
      imageTagCategoryTable: [],
      pagination: {
        currentPage: 1,
        pageSize: 3,
        total: 0,
        onlyHasOnePage: false
      },
      updateImageDialogVisible: false,
      updateImageForm: {imageId: '', categoryValueId: '', tagListValueIds: []},
      categoryList: [],
      tagList: [],
      lastKeyword: '',
      isInSearch: false
    }
  },
  mounted() {

    // 无条件获取图片表格
    this.getImageTableWithNoCondition()
  },
  methods: {
    handleSizeChange(newPageSize) {
      this.pagination.pageSize = newPageSize
      this.getImageTableWithNoCondition()
    },
    // 删除图片
    deleteImage(row) {
      this.request.post(`RemoveImageById?imageId=${row.image.id}`).then(res => {
        this.$message.success('删除成功')
        // 重新查询
        this.research()
      })
    },
    // 保存修改图片
    save() {
      this.request.post(`UpdateImage`, this.updateImageForm).then(res => {
        this.$message.success('修改成功')
        this.updateImageDialogVisible = false
        // 重新查询
        this.research()
      })
    },
    research() {
      if (this.isInSearch) {
        this.request.get(`GetImageTableWithCondition?keyword=${this.lastKeyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
          this.imageTagCategoryTable = res.imageTagCategoryList
          this.pagination.total = res.total
        })
      } else {
        this.getImageTableWithNoCondition()
      }
    },
    // 修改图片信息
    updateImageDetail(row) {
      // 保存图片id
      this.updateImageForm.imageId = row.image.id
      // 获取所有分类 - 如果没有的话
      if (!this.categoryList || this.categoryList.length == 0) {
        this.getCategoryList()
      }
      // 获取所有标签 - 如果没有的话
      if (!this.tagList || this.tagList.length == 0) {
        this.getTagList()
      }
      // 回显分类
      this.updateImageForm.categoryValueId = row.category.id
      // 回显标签
      this.updateImageForm.tagListValueIds = row.tagList.map(tag => tag.id)
      // 打开对话框
      this.updateImageDialogVisible = true
    },
    // 获取所有分类
    getCategoryList() {
      this.request.get(`GetAllCategory`).then(res => {
        console.log('category', res)
        this.categoryList = res.categoryList
      })
    },
    // 获取所有标签
    getTagList() {
      this.request.get(`GetAllTag`).then(res => {
        console.log('tags', res)
        this.tagList = res.tagList
      })
    },
    // 换页
    handleCurrentChange(newPage) {
      this.pagination.currentPage = newPage
      // 如果是正在搜索状态，那么还要按关键字搜索
      // 搜索状态 - 点击搜索之后就变为搜索状态 - ok - 那如果又修改了关键字呢 - 那就保留上一次搜索的数据 - 点击搜索时关键字不为空才变为搜索状态 - 可以了 - 还有 - 如果是搜索状态且关键字为空 - 则还是无条件查询
      if (this.isInSearch && this.keyword) {
        this.searchImageTable()
      } else {
        // 否则，按无条件搜索
        this.getImageTableWithNoCondition()
      }
    },
    // 搜索图片
    searchImageTable() {
      // 为空 - 也要搜索的 - 重新查询所有数据 - 从第一页开始查
      if (!this.keyword) {
        // 设置为未搜索状态
        this.isInSearch = false
        this.pagination.currentPage = 1
        this.getImageTableWithNoCondition()
      }
      // 不为空则进行关键字搜索
      if (this.keyword) {
        // 进入搜索状态
        this.isInSearch = true
        // 记录上一次搜索，也即此次
        this.lastKeyword = this.keyword
        // 搜索 - 按上一次的关键字进行搜索
        this.request.get(`GetImageTableWithCondition?keyword=${this.lastKeyword}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
          this.imageTagCategoryTable = res.imageTagCategoryList
          this.pagination.total = res.total
        })
      }
    },
    // 无条件获取图片表格
    getImageTableWithNoCondition() {
      this.request.get(`GetImageTableWithNoCondition?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.imageTagCategoryTable = res.imageTagCategoryList
        this.pagination.total = res.total
      })
    }
  }
}
</script>

<style scoped src="./ImageManagement.css">

</style>