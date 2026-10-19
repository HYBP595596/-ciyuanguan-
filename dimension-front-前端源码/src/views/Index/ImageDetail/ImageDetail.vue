<template>
  <div class="detail-page">
    <!-- ================= 主体：左图 + 右信息 ================= -->
    <div class="detail-main">
      <!-- 1. 左侧沉浸式大图 -->
      <div class="image-box">
        <img v-if="imageTag && imageTag.image" :src="imageTag.image.url" alt="image" />
      </div>

      <!-- 2. 右侧信息与操作 -->
      <aside class="info-box">
      
            <!-- 上传者信息 -->
      <div class="author-box" v-if="imageTag && imageTag.user">
        <div class="author-info" @click="clickUserCenter(imageTag.user.id)">
          <!-- 头像 (带容错) -->
          <img 
            :src="imageTag.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
            class="author-avatar" 
          />
          
          <!-- 名字 + 粉丝数 -->
          <div class="author-meta">
            <div class="author-name">
              {{ imageTag.user.nickname }}
            </div>
            <div class="author-fans">
              {{ imageTag.fansCount || 0 }} 粉丝
            </div>
          </div>
        </div>
        
        <!-- 关注按钮 -->
        <el-button 
          v-if="user && user.id !== imageTag.user.id"
          :type="isFollowingAuthor ? 'info' : 'primary'" 
          size="small" 
          round
          @click="toggleFollowAuthor">
          {{ isFollowingAuthor ? '已关注' : '关注' }}
        </el-button>
      </div>

        <!-- 标签区 -->
        <div class="tag-group">
          <el-tag
            v-for="(tag, i) in imageTag.tagList"
            :key="i"
            size="medium"
            effect="plain">
            {{ tag.name }}
          </el-tag>
        </div>

        <!-- 操作按钮组 -->
        <div class="action-group">
          <div class="action-item" @click="isLike === 0 ? cancelLikeImage() : tryLikeImage()">
            <i class="el-icon-thumb" :class="{ active: isLike === 0 }"></i>
            <span>{{ imageTag.image ? imageTag.image.score : 0 }}</span>
          </div>
          <div class="action-item" @click="isCollect ? clickCollect() : clickCollect()">
            <i class="el-icon-star-on" :class="{ active: isCollect }"></i>
            <span>{{ imageTag.image ? imageTag.image.collects : 0 }}</span>
          </div>
          <div class="action-item" @click="downloadImage">
            <i class="el-icon-download"></i>
            <span>下载</span>
          </div>
        </div>

        <!-- 数据统计 -->
        <div class="stats-group">
          <div class="stat-row">
            <span><i class="el-icon-view"></i> 浏览量</span>
            <span>{{ imageTag.image ? imageTag.image.views : 0 }}</span>
          </div>
          <div class="stat-row">
            <span><i class="el-icon-time"></i> 上传时间</span>
            <span>{{ imageTag.image ? format1(imageTag.image.createTime) : '' }}</span>
          </div>
        </div>

        <!-- 相关推荐（右侧缩略图） -->
        <div class="recommend-group" v-if="recommendImageList && recommendImageList.length">
          <div class="recommend-title">相关推荐</div>
          <div class="recommend-list">
            <div
              class="recommend-item"
              v-for="(item, index) in recommendImageList"
              :key="index"
              @click="clickImage(item.image.id)">
              <img :src="item.image.url" alt="" />
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- ================= 底部：评论区 ================= -->
    <div class="comment-box">
      <div class="comment-head">
        <span class="comment-title">评论 ({{ imageTag.image ? imageTag.image.comments : 0 }})</span>
        <el-button type="primary" size="small" icon="el-icon-edit" @click="clickComment">
          发表评论
        </el-button>
      </div>

      <div class="comment-list" v-if="commentUserList && commentUserList.length">
        <div class="comment-item" v-for="(item, index) in commentUserList" :key="index">
          <img class="user-avatar" :src="item.user.avatar" alt="" />
          <div class="comment-content">
            <div class="user-name">{{ item.user.nickname }}</div>
            <div class="text">{{ item.comment.content }}</div>
            <div class="time">{{ format1(item.comment.createTime) }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无评论"></el-empty>

      <div class="pagination" v-if="commentUserList && commentUserList.length > 5">
        <el-pagination
          background
          :page-size="pagination.pageSize"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          layout="prev, pager, next"
          :total="pagination.total">
        </el-pagination>
      </div>
    </div>

    <!-- 评论对话框 (原样保留) -->
    <el-dialog
      @close="beforeCommentDialogClose"
      center title="评论~"
      :visible.sync="commentDialogVisible" width="480px">
      <el-form :model="commentForm" :rules="commentFormRules" ref="commentForm" label-width="0">
        <el-form-item prop="content">
          <el-input type="textarea" :rows="6" placeholder="发条友善的评论吧~" v-model="commentForm.content"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" style="width: 100%" @click="publishComment">发布</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { format1 } from '@/utils/time-format'

export default {
  name: "ImageDetail",
  data() {
    return {
      collectHide: false,
      imageId: 0,
      highlightCategoryId: -1,
      categoryList: [],
      imageTag: {},
      isLogin: false,
      user: {},
      isLike: -1,
      isCollect: false,
      pagination: { currentPage: 1, pageSize: 5, total: 0 },
      commentUserList: [],
      recommendImageList: [],
      commentDialogVisible: false,
      commentForm: { content: '' },
      commentFormRules: {
        content: [{ required: true, message: '请输入评论内容', trigger: 'blur' }]
      },
      isFollowingAuthor: false, // 是否已关注作者
    }
  },
  mounted() {
    this.handleRouter()
    this.getCategoryList()
    this.getImageDetail()

    this.isLogin = !!sessionStorage.getItem('token')
    if (this.isLogin) {
      this.user = JSON.parse(sessionStorage.getItem('user'))
      this.getUserRatingAndCollectInfoIfIsLogin()
      this.recordImageWatchHistoryIfIsLogin()
    }
    this.imageViewIncrement()
  },
  methods: {
    beforeCommentDialogClose() {
      this.$refs['commentForm'].resetFields()
      this.commentDialogVisible = false
    },
    format1(timestamp) { return format1(timestamp) },

    clickAllCategory() { this.$router.push('/index-body') },

    downloadImage() {
      window.open("http://47.113.192.130:9090/dimension/DownloadImage?url=" + this.imageTag.image.url)
    },

    publishComment() {
      this.$refs['commentForm'].validate(valid => {
        if (!valid) return
        this.request.post(`CommentImage`, {
          content: this.commentForm.content,
          imageId: this.imageId,
          userId: this.user.id
        }).then(() => {
          this.$message.success('评论成功')
          this.commentDialogVisible = false
          this.pagination.currentPage = 1
          this.request.post(`ImageCommentsIncrement?imageId=${this.imageId}`).then(() => {
            this.imageTag.image.comments++
            this.getCommentList()
          })
        })
      })
    },
    // 点击作者头像跳转到作者主页（可复用你的 BlankPage2 逻辑）
clickUserCenter(authorId) {
  // 如果后端支持查看他人主页，可以跳转过去；目前先做个提示
  this.$message.info('查看用户 ' + authorId + ' 的主页功能开发中');
},

// 关注 / 取关切换
toggleFollowAuthor() {
  if (!this.isLogin) {
    this.$message.warning('请先登录');
    this.event.$emit('popLoginForm');
    return;
  }
  
  // ⚠️ 这里需要后端提供 FollowUser 接口
  // this.request.post(`FollowUser?userId=${this.user.id}&targetUserId=${this.imageTag.user.id}`).then(...)
  
  // 目前前端先做纯 UI 切换演示：
  this.isFollowingAuthor = !this.isFollowingAuthor;
  this.$message.success(this.isFollowingAuthor ? '关注成功' : '已取消关注');
},

    clickComment() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      this.commentDialogVisible = true
    },

    clickCollect() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      if (!this.isCollect) {
        this.request.post(`AddImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
          this.$message.success('收藏成功')
          this.isCollect = true
        })
      } else {
        this.request.post(`RemoveImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
          this.isCollect = false
        })
      }
    },

    tryLikeImage() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      this.request.post(`LikeImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        if (this.isLike == -1) {
          this.request.post(`ImageVotesIncrement?imageId=${this.imageId}`).then(() => {
            this.imageTag.image.votes++
          })
        }
        this.$message.success('点赞成功')
        this.isLike = 0
        this.reComputeImageScore()
      })
    },

    tryDislikeImage() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      this.request.post(`DislikeImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        if (this.isLike == -1) {
          this.request.post(`ImageVotesIncrement?imageId=${this.imageId}`).then(() => {
            this.imageTag.image.votes++
          })
        }
        this.isLike = 1
        this.reComputeImageScore()
      })
    },

    cancelLikeImage() {
      this.request.post(`CancelRateImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        this.request.post(`ImageVotesDecrement?imageId=${this.imageId}`).then(() => {
          this.imageTag.image.votes--
        })
        this.isLike = -1
        this.reComputeImageScore()
      })
    },

    cancelDislikeImage() {
      this.request.post(`CancelRateImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        this.request.post(`ImageVotesDecrement?imageId=${this.imageId}`).then(() => {
          this.imageTag.image.votes--
        })
        this.isLike = -1
        this.reComputeImageScore()
      })
    },

    reComputeImageScore() {
      this.request.get(`GetImageLikes?imageId=${this.imageId}`).then(res => {
        const likes = res.likes
        this.request.get(`GetImageDislikes?imageId=${this.imageId}`).then(res2 => {
          this.request.post(`ComputeImageScore?imageId=${this.imageId}&likes=${likes}&dislikes=${res2.dislikes}`).then(() => {})
        })
      })
    },

    clickImage(imageId) {
      this.$router.push({
        path: '/blank-page',
        query: { imageId, highlightCategoryId: this.highlightCategoryId }
      })
    },

    getRecommendImageList() {
      this.request.get(`GetRecommendImageList?categoryId=${this.imageTag.image.categoryId}&imageId=${this.imageId}`).then(res => {
        this.recommendImageList = res.imageTagList
      })
    },

    recordImageWatchHistoryIfIsLogin() {
      if (this.isLogin) {
        this.request.post(`AddImageWatchHistory?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {})
      }
    },

    imageViewIncrement() {
      this.request.post(`ImageViewsIncrement?imageId=${this.imageId}`).then(() => {})
    },

    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage
      this.getCommentList()
    },

    getCommentList() {
      this.request.get(`GetImageCommentList?imageId=${this.imageId}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.commentUserList = res.commentUserList
        if (this.commentUserList.length <= 5) this.collectHide = true
        this.pagination.total = res.total
      })
    },

    getUserRatingAndCollectInfoIfIsLogin() {
      if (this.isLogin) {
        this.getUserRatingInfo()
        this.getUserCollectInfo()
      }
    },

    getUserCollectInfo() {
      this.request.get(`GetOneImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
        this.isCollect = !!(res.imageBookmark && res.imageBookmark.length)
      })
    },

    getUserRatingInfo() {
      this.request.get(`GetUserRateCase?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
        this.isLike = res.imageRating ? res.imageRating.like : -1
      })
    },

    getImageDetail() {
      this.request.get(`GetImageDetail?imageId=${this.imageId}`).then(res => {
        this.imageTag = res.imageTag
        this.getCommentList()
        this.getRecommendImageList()
      })
    },

    clickCategory(categoryId, count) {
      this.$router.push({
        path: '/index-body',
        query: { categoryId, count }
      })
    },

    getCategoryList() {
      this.request.get(`GetAllCategory`).then(res => {
        this.categoryList = res.categoryList
      })
    },

    handleRouter() {
      this.imageId = this.$route.query.imageId
      this.highlightCategoryId = this.$route.query.highlightCategoryId
    }
  }
}
</script>

<style scoped src="./ImageDetail.css"></style>