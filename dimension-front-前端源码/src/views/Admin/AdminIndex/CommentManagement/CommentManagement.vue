<template>
  <!--评论管理页-->
  <div class="body">
    <!--搜索输入框-->
    <div class="search-input">
      <el-input class="placeholder" clearable v-model="nickname" placeholder="用户昵称"></el-input>
      <el-button class="search" icon="el-icon-search" type="primary" @click="searchCommentWithNickname">搜索</el-button>
    </div>
    <!--评论表格-->
    <div class="comment-table">
      <el-table
          :data="commentUserImageTable"
          style="width: 100%" stripe>
        <el-table-column
            align="center"
            label="图片"
            width="250">
          <template slot-scope="scope">
              <img :src="scope.row.image.url" class="image-image"/>
          </template>
        </el-table-column>
        <el-table-column
            label="评论用户"
            width="180">
          <template slot-scope="scope">
            <img :src="scope.row.user.avatar" v-if="scope.row && scope.row.user" class="user-avatar"/>
          </template>
        </el-table-column>
        <el-table-column
            prop="user.nickname"
            label="用户昵称"
            width="130">
        </el-table-column>
        <el-table-column
            prop="user.username"
            label="用户账号"
            width="130">
        </el-table-column>
        <el-table-column
            label="评论时间"
            width="180">
          <template slot-scope="scope">
            <span v-if="scope && scope.row && scope.row.comment">{{format1(scope.row.comment.createTime)}}</span>
          </template>
        </el-table-column>
        <el-table-column
            prop="comment.content"
            label="评论内容"
            width="250">
        </el-table-column>
        <el-table-column align="center" label="操作">
          <template slot-scope="scope">
            <el-popconfirm
                @confirm="deleteComment(scope.row)"
                title="确定删除该评论？">
              <el-button
                  style="margin-right: 5px"
                  slot="reference"
                  type="danger"
                  size="mini">删除</el-button>
            </el-popconfirm>
            <el-button
                size="mini"
                type="primary"
                @click="lookCommentDetail(scope.row)">详情</el-button>
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
      <!--评论详情对话框-->
      <div class="comment-detail-dialog">
        <el-dialog
            center
            title="评论内容"
            :visible.sync="commentDetailDialogVisible"
            width="30%">
          <el-row>
            <!--用户头像-->
            <el-col :span="4" class="user-image" style="margin-left: 20%">
              <img :src="currentRow.user.avatar" v-if="currentRow && currentRow.user" style="width: 50px; height: 50px; border-radius: 50%"/>
            </el-col>
            <el-col :span="10" class="right">
              <!--用户昵称-->
              <div class="user-nickname" v-if="currentRow && currentRow.user">
                <span>{{currentRow.user.nickname}}</span>
              </div>
              <!--评论内容-->
              <div class="content" v-if="currentRow && currentRow.comment" style="margin-top: 10px">
                <span>{{currentRow.comment.content}}</span>
              </div>
              <!--评论时间-->
              <div class="time" v-if="currentRow && currentRow.comment">
                <span>{{currentRow.comment.createTime}}</span>
              </div>
            </el-col>
          </el-row>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import {format1} from '@/utils/time-format'
export default {
  name: "CommentManagement",
  data() {
    return {
      nickname: '',
      commentUserImageTable: [],
      pagination: {
          currentPage: 1,
          pageSize: 3,
          total: 0,
          onlyHasOnePage: false
      },
      lastNickname: '',
      isInSearch: false,
      commentDetailDialogVisible: false,
      currentRow: {}
    }
  },
  mounted() {
    this.getCommentListWithNoCondition()
  },
  methods: {
    format1(timestamp) {
      return format1(timestamp)
    },
    handleSizeChange(newPageSize){
      this.pagination.pageSize = newPageSize
      this.getCommentListWithNoCondition()
    },
    // 查看评论详情
    lookCommentDetail(row) {
      this.currentRow = row
      this.commentDetailDialogVisible = true
    },
    // 删除评论
    deleteComment(row) {
      this.request.post(`RemoveCommentById?commentId=${row.comment.id}`).then(res => {
        this.$message.success('删除成功')
        // 该图片评论数-1
        this.request.post(`ImageCommentsDecrement?imageId=${row.image.id}`).then(res => {})
        // 重查
        this.research()
      })
    },
    // 换页
    handleCurrentChange(newPage) {
      this.pagination.currentPage = newPage
      this.research()
    },
    // 重查
    research() {
      if (this.isInSearch) {
        this.search()
      } else {
        this.getCommentListWithNoCondition()
      }
    },
    // 搜索评论列表
    searchCommentWithNickname() {
      // 从第一页开始搜
      this.pagination.currentPage = 1
      // 记录
      this.lastNickname = this.nickname
      this.isInSearch = true
      // 如果为空，表示无条件搜索
      if (!this.lastNickname) {
        this.isInSearch = false
        this.getCommentListWithNoCondition()
        return
      }
      this.search()
    },
    // 有条件搜索
    search() {
      this.request.get(`GetCommentTableWithCondition?nickname=${this.lastNickname}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.commentUserImageTable = res.commentUserImageList
        this.pagination.total = res.total
      })
    },
    // 无条件获取评论列表
    getCommentListWithNoCondition() {
      this.request.get(`GetCommentTableWithNoCondition?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        console.log('comment', res)
        this.commentUserImageTable = res.commentUserImageList
        this.pagination.total = res.total
      })
    }
  }
}
</script>

<style scoped src="./CommentManagement.css">

</style>