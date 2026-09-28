<template>
  <!--图片详细信息展示页-->
  <div class="body" style="height: 1300px">
    <!--左侧分类标签列表-->
    <div class="category">
      <div class="category-div body-image2" @click="clickAllCategory">
        <span class="new-font" style="color: rgb(255,116,66); font-weight: bolder; font-size: 17px">全部</span>
      </div>
      <!--各个分类块-->
      <div class="category-div body-image2" v-for="(item, index) in categoryList" :key="index" @click="clickCategory(item.id, item.count)"
           :style="{backgroundColor: item.id == highlightCategoryId ? 'red' : ''}">
        <!--分类名称-->
        <span class="new-font" style="color: rgb(255,116,66); font-weight: bolder; font-size: 17px">{{item.name}}</span>
        <!--分类下的图片数-->
        <span style="position: relative; right: -7px; top: -7px; color: rgb(89,91,224)">{{item.count}}</span>
      </div>
    </div>
    <!--中侧图片详情展示-->
    <div class="image-detail-region">
      <!--图片详情-->
      <div class="image-detail">
        <!--图片标签列表-->
        <div class="image-tag-list">
          <el-tag style="margin-right: 10px; font-size: 14px" type="danger" v-for="(item, index) in imageTag.tagList" :key="index">
            {{item.name}}
          </el-tag>
<!--          <el-tag style="margin-left: 10px; font-size: 14px" type="danger" v-for="(item1, index1) in imageTag.tagList" :key="index1">-->
<!--            {{item1.name}}-->
<!--          </el-tag>-->
        </div>
        <!--图片-->
        <div class="image">
          <img v-if="imageTag && imageTag.image" :src="imageTag.image.url" />
        </div>
      </div>
      <!--分数、浏览数、收藏、下载部分-->
                        <div style="background-color: rgba(22,22,21,0.41); margin-top: 20px" class="other-option">
      <!--      <div style="background-color: rgba(157,220,255,0.94); margin-top: 20px" class="other-option">-->
                          <!--            <div style="background: radial-gradient(circle, rgba(255,197,161,0.53), rgba(255,74,74,0.87)); margin-top: 20px" class="other-option">-->
        <!--分数展示-->
        <div class="like-or-dislike">
          <!--点赞或拉踩-->
          <div class="rating">
            <img src="@/static/image/like.png" class="body-image" v-show="!isLogin || isLike != 0" @click="tryLikeImage"/> <img class="body-image" src="@/static/image/like-active.png" style="margin-right: 5px" v-show="isLogin && isLike == 0" @click="cancelLikeImage"/>
            <span v-if="imageTag && imageTag.image">{{imageTag.image.score}}</span>
            <img src="@/static/image/dislike.png" class="body-image" v-show="!isLogin || isLike != 1" @click="tryDislikeImage"/> <img class="body-image" src="@/static/image/dislike-active.png" style="margin-left: 0" v-show="isLogin && isLike == 1" @click="cancelDislikeImage"/>
          </div>
          <!--投票数-->
          <div class="votes">
            <span v-if="imageTag && imageTag.image">{{imageTag.image.votes}} 投票数</span>
          </div>
        </div>
        <!--浏览数和上传时间展示-->
        <div class="view-and-upload-time">
          <!--浏览数-->
          <div class="view">
            <i class="el-icon-view"></i>&nbsp;
            <span>浏览数</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <span v-if="imageTag && imageTag.image">{{imageTag.image.views}}</span>
          </div>
          <!--上传时间-->
          <div class="upload-time">
            <i class="el-icon-upload"></i>&nbsp;
            <span>上传时间</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <span v-if="imageTag && imageTag.image">{{format1(imageTag.image.createTime)}}</span>
          </div>
        </div>
        <!--收藏和下载按钮-->
        <div class="collect-and-download">
          <!--收藏按钮-->
          <div class="collect">
            <el-button :type="isCollect ? 'danger' : ''" @click="clickCollect" :icon="isCollect ? 'el-icon-star-on' : 'el-icon-star-off'">收藏</el-button>
          </div>
          <!--下载按钮-->
          <div class="download">
            <el-button type="success" @click="downloadImage" icon="el-icon-download">下载</el-button>
          </div>
        </div>
      </div>
      <!--评论列表展示区-->
      <div class="comment-list-region">
        <!--评论数和评论按钮-->
        <div class="comment-count-and-comment-button">
          <!--评论数-->
          <div class="comment-count" v-if="imageTag && imageTag.image">
            评论列表 ({{imageTag.image.comments}})
          </div>
          <!--评论按钮-->
          <div class="comment-button">
            <el-button type="primary" @click="clickComment" icon="el-icon-edit">评论</el-button>
          </div>
        </div>
        <!--评论列表-->
        <div class="comment-list" v-if="commentUserList">
          <!--各个评论展示块-->
          <div class="comment-and-user" v-for="(item, index) in commentUserList" :key="index">
            <!--用户头像-->
            <div class="user-avatar">
              <img v-if="item && item.user" :src="item.user.avatar"/>
            </div>
            <!--用户昵称和评论-->
            <div class="user-nickname-and-comment">
              <!--用户昵称-->
              <div style="color: rgba(251, 116, 157)" class="user-nickname" v-if="item && item.user">
                {{item.user.nickname}}
              </div>
              <!--评论-->
              <div class="comment">
                <!--评论内容-->
                <div class="comment-content" v-if="item && item.comment">
                  {{item.comment.content}}
                </div>
                <!--评论时间-->
                <div class="comment-time" v-if="item && item.comment" style="margin-top: 10px; color: rgba(179, 182, 186)">
                  {{format1(item.comment.createTime)}}
                </div>
              </div>
            </div>
          </div>
          <el-empty description="快来发条友善的评论吧~~~" v-show="!commentUserList || commentUserList.length == 0"></el-empty>
        </div>
        <!--评论分页器-->
        <div class="pagination">
            <el-pagination
                :hide-on-single-page="collectHide"
                background
                :page-size="pagination.pageSize"
                @current-change="handleCurrentChange"
                :current-page="pagination.currentPage"
                layout="prev, pager, next"
                :total="pagination.total">
            </el-pagination>
        </div>
      </div>
    </div>
    <!--右侧推荐图片列表-->
    <div class="recommend-image-list" v-if="recommendImageList">
      <span>相关推荐</span>
      <!--各个推荐图片块-->
      <div class="recommend-image body-image" v-for="(item, index) in recommendImageList" :key="index" @click="clickImage(item.image.id)">
        <!--图片-->
        <div class="recommend-image-detail">
          <img v-if="item && item.image" :src="item.image.url" />
        </div>
        <!--分数、收藏数、浏览数展示-->
        <div  class="score-collects-views" v-if="item && item.image">
          <i class="el-icon-medal"></i>
          <span>{{item.image.score}}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <i class="el-icon-star-off"></i>
          <span>{{item.image.collects}}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
          <i class="el-icon-view"></i>
          <span>{{item.image.views}}</span>
        </div>
        <!--标签列表-->
        <div class="tag-list" >
          <el-tag style="margin-left: 10px; font-size: 14px" type="danger" v-for="(item1, index1) in item.tagList" :key="index1">
            {{item1.name}}
          </el-tag>
        </div>
      </div>
    </div>

    <!--******************************************-->

    <!--其他并不直接展示到页面上的信息 // 如对话框之类的-->
    <div class="other">
      <!--评论对话框-->
      <div class="comment-dialog">
        <el-dialog
            @close="beforeCommentDialogClose"
            center
            title="评论~"
            :visible.sync="commentDialogVisible"
            width="30%">
          <!--评论输入框-->
          <div class="comment-input">
            <el-form :model="commentForm" :rules="commentFormRules" ref="commentForm" label-width="0px" class="demo-ruleForm">
              <el-form-item label="" prop="content">
                <el-input
                    type="textarea"
                    :rows="7"
                    placeholder="发条友善的评论吧~"
                    v-model="commentForm.content">
                </el-input>
              </el-form-item>
            </el-form>
          </div>
          <!--发布按钮-->
          <div class="publish-comment">
            <el-button type="primary" style="width: 400px; text-align: center; margin-left: 8%" @click="publishComment">发布</el-button>
          </div>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script>
import {format1} from '@/utils/time-format'
export default {
  name: "ImageDetail",
  data() {
    return {
      collectHide: false,
      imageId: 0,
      highlightCategoryId: -1,
      categoryList: [],
      imageTag: {},
      isLogin: false, // 用户是否登录
      user: {}, // 用户信息
      isLike: -1, // 点赞情况 -1-未评分或未登录 0-点赞 1-拉踩
      isCollect: false, // 收藏情况 false-未收藏或未登录 true-已收藏
      pagination: { // 评论列表分页信息
        currentPage: 1,
        pageSize: 5,
        total: 0
      },
      commentUserList: [], // 评论列表
      recommendImageList: [], // 推荐图片列表
      commentDialogVisible: false, // 评论对话框显示
      commentForm: {content: ''}, // 评论表单
      commentFormRules: {
        content: [{required: true, message: '请输入评论内容', trigger: 'blur'}]
      }
    }
  },
  mounted() {
    // 处理路由 -> 注意以下方法调用顺序
    this.handleRouter()
    // 获取分类列表
    this.getCategoryList()
    // 获取图片详情信息
    this.getImageDetail()
    // // 获取推荐图片列表
    // this.getRecommendImageList()
    // 判断用户是否登录 - 要在以下操作之前
    // this.judgeUserIsLogin()

    this.isLogin = sessionStorage.getItem('token') ? true : false

    console.log('isLogin', this.isLogin)
    if (this.isLogin) {
      this.user = JSON.parse(sessionStorage.getItem('user'))
      // 如果已登录，获取用户评分和收藏信息
      this.getUserRatingAndCollectInfoIfIsLogin()
      // 记录图片浏览历史 - 如果已登录
      this.recordImageWatchHistoryIfIsLogin()
    }

    // // 获取评论列表
    // this.getCommentList()
    // 图片浏览数+1
    this.imageViewIncrement()
  },
  methods: {
    beforeCommentDialogClose() {
      console.log('触发')
      this.$refs['commentForm'].resetFields()
      this.commentDialogVisible = false
    },
    format1(timestamp) {
      return format1(timestamp)
    },
    // 点击所有分类，跳转到首页，并进行无条件查询 - 也即不传递参数
    clickAllCategory() {
      this.$router.push('/index-body')
    },
    // 下载图片
    downloadImage() {
      // window.open("http://localhost:9000/dimension/DownloadImage?url=" + this.imageTag.image.url)
      window.open("http://47.113.192.130:9090/dimension/DownloadImage?url=" + this.imageTag.image.url)
      // window.open("http://47.113.192.130:9090/dimension3/DownloadImage?url=" + this.imageTag.image.url)
      // this.request.get(`DownloadImage?url=${this.imageTag.image.url}`).then(res => {})
    },
    // 发布评论
    publishComment() {
      this.$refs['commentForm'].validate(valid => {
        if (valid) {
          this.request.post(`CommentImage`, {content: this.commentForm.content, imageId: this.imageId, userId: this.user.id}).then(res => {
            this.$message.success('评论成功')
            // 关闭评论对话框
            this.commentDialogVisible = false
            // 刷新评论列表，并从第一页开始查询
            this.pagination.currentPage = 1
            // 评论数+1
            this.request.post(`ImageCommentsIncrement?imageId=${this.imageId}`).then(res => {
              // 手动评论数+1
              this.imageTag.image.comments++
              this.getCommentList()

            })
          })
        }
      })
    },
    // 点击评论
    clickComment() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      // 打开评论对话框
      this.commentDialogVisible = true
    },
    // 点击收藏
    clickCollect() {
      // 如果未登录
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        // 弹出登录框
        this.event.$emit('popLoginForm')
        return
      }
      // 如果未收藏
      if (!this.isCollect) {
        // 收藏图片
        this.request.post(`AddImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
          // 提示
          this.$message.success('收藏成功')
          // 设置为已收藏
          this.isCollect = true
          // 该图片收藏数+1
          // this.request.post(`AddImageBookmark?userId=${this.user.id}&imageId=${this.imageId}`).then(res => {})
        })
      }
      // 如果已收藏
      if (this.isCollect) {
        // 取消收藏图片
        this.request.post(`RemoveImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
          // 设置为未收藏
          this.isCollect = false
          // 该图片收藏数-1
          // this.request.post(``)
        })
      }
    },
    // 尝试点赞图片
    tryLikeImage() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        // 弹出登录框
        this.event.$emit('popLoginForm')
      } else {
        // 点赞图片
        this.request.post(`LikeImage?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
          // 若isLike原来为-1未评分，则图片投票数+1，且页面手动显示
          if (this.isLike == -1) {
            this.request.post(`ImageVotesIncrement?imageId=${this.imageId}`).then(res => {
              this.imageTag.image.votes++
            })
          }
          // 提示
          this.$message.success('点赞成功')
          // 修改isLike=0表示点赞
          this.isLike = 0
          // 重新计算图片评分
          this.reComputeImageScore()
        })
      }
    },
    // 尝试拉踩图片
    tryDislikeImage() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        // 弹出登录框
        this.event.$emit('popLoginForm')
      } else {
        // 拉踩图片
        this.request.post(`DislikeImage?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
          // 若isLike原来为-1未评分，则图片投票数+1，且页面手动显示
          if (this.isLike == -1) {
            this.request.post(`ImageVotesIncrement?imageId=${this.imageId}`).then(res => {
              this.imageTag.image.votes++
            })
          }
          // 修改isLike=1表示拉踩
          this.isLike = 1
          // 重新计算图片评分
          this.reComputeImageScore()
        })
      }
    },
    // 取消点赞图片
    cancelLikeImage() {
      // 取消评分图片
      this.request.post(`CancelRateImage?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
        // 图片投票数-1
        this.request.post(`ImageVotesDecrement?imageId=${this.imageId}`).then(res => {
          this.imageTag.image.votes--
        })
        // isLike=-1表示未评分
        this.isLike = -1
        // 重新计算图片评分 - 其实也可以在页面关闭时计算 - 直接绑定个destroy即可
        this.reComputeImageScore()
      })
    },
    // 取消拉踩图片
    cancelDislikeImage() {
      // 取消评分图片
      this.request.post(`CancelRateImage?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
        // 图片投票数-1
        this.request.post(`ImageVotesDecrement?imageId=${this.imageId}`).then(res => {
          this.imageTag.image.votes--
        })
        // isLike=-1表示未评分
        this.isLike = -1
        // 重新计算图片评分
        this.reComputeImageScore()
      })
    },
    // 重新计算图片评分
    reComputeImageScore() {
      let likes
      let dislikes
      // 获取图片点赞数
      this.request.get(`GetImageLikes?imageId=${this.imageId}`).then(res => {
        likes = res.likes
        // 获取图片拉差数
        this.request.get(`GetImageDislikes?imageId=${this.imageId}`).then(res => {
          dislikes = res.dislikes
          // 重新评分
          this.request.post(`ComputeImageScore?imageId=${this.imageId}&likes=${likes}&dislikes=${dislikes}`).then(res => {})
        })
      })

    },
    // 点击推荐图片跳转
    clickImage(imageId) {
      // 间接跳转 - 防止重复路由
      this.$router.push({
        path: '/blank-page',
        query: {
          imageId,
          highlightCategoryId: this.highlightCategoryId
        }
      })
    },
    // 获取推荐图片列表
    getRecommendImageList() {
      this.request.get(`GetRecommendImageList?categoryId=${this.imageTag.image.categoryId}&imageId=${this.imageId}`).then(res => {
        this.recommendImageList = res.imageTagList
      })
    },
    // 记录图片浏览历史 - 如果已登录
    recordImageWatchHistoryIfIsLogin() {
      if (this.isLogin) {
        this.request.post(`AddImageWatchHistory?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {})
      }
    },
    // 图片浏览数+1
    imageViewIncrement() {
      this.request.post(`ImageViewsIncrement?imageId=${this.imageId}`).then(res => {})
    },
    // 切换页
    handleCurrentChange(currentPage) {
      // 直接获取即可 - 因为无条件
      this.pagination.currentPage = currentPage
      this.getCommentList()
    },
    // 获取评论列表
    getCommentList() {
      // if (!this.pagination || !this.pagination.currentPage) {
      //   this.pagination = {currentPage: 1, pageSize: 5, total: this.imageTag.image.comments, onlyHasOnePage: false}
      // }
      console.log('get-comment')
      this.request.get(`GetImageCommentList?imageId=${this.imageId}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.commentUserList = res.commentUserList
        if (this.commentUserList.length <= 5) {
          this.collectHide = true
        }
        // this.pagination.total = this.imageTag.image.comments // total直接等于图片评论数
        this.pagination.total = res.total
        console.log('pagination', this.pagination.total)
        // console.log(this.pagination.total)
        console.log(this.commentUserList)
        console.log(res)
      })
      console.log('---')
    },
    // 如果已登录，获取用户评分和收藏信息
    getUserRatingAndCollectInfoIfIsLogin() {
      if (this.isLogin) {
        this.getUserRatingInfo()
        this.getUserCollectInfo()
      }
    },
    // 获取用户收藏信息
    getUserCollectInfo() {
      this.request.get(`GetOneImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
        console.log('res', res)
        // 未收藏
        if (!res.imageBookmark  || res.imageBookmark.length == 0) {
          this.isCollect = false
        } else {
          // 已收藏
          this.isCollect = true
        }
        console.log('collect', this.isCollect)
      })
    },
    // 获取用户评分信息
    getUserRatingInfo() {
      this.request.get(`GetUserRateCase?imageId=${this.imageId}&userId=${this.user.id}`).then(res => {
        console.log('rating', res)
        // 如果为空表示未评分
        if (!res.imageRating) {
          this.isLike = -1
        } else {
          this.isLike = res.imageRating.like
        }
        console.log('isLike', this.isLike)
      })
    },
    // 判断用户是否登录
    judgeUserIsLogin() {
      this.request.get(`AuthenticateUserIsLogin`).then(res => {
        console.log(res.isLogin)
        this.isLogin = res.isLogin
        console.log(this.isLogin)
      })
    },
    // 获取图片详情信息
    getImageDetail() {
      this.request.get(`GetImageDetail?imageId=${this.imageId}`).then(res => {
        this.imageTag = res.imageTag
        console.log("imageTag ", this.imageTag)
        this.getCommentList()
        // 获取推荐列表
        this.getRecommendImageList()
        // 获取评论列表
      })
    },
    // 点击分类 - 跳转到首页查询分类下的图片
    clickCategory(categoryId, count) {
      this.$router.push({
        path: '/index-body',
        query: {
          categoryId,
          count
        }
      })
    },
    // 获取所有分类列表
    getCategoryList() {
      this.request.get(`GetAllCategory`).then(res => {
        this.categoryList = res.categoryList
      })
    },
    // 处理路由跳转参数
    handleRouter() {
      this.imageId = this.$route.query.imageId
      this.highlightCategoryId = this.$route.query.highlightCategoryId
    }
  }
}
</script>

<style scoped src="./ImageDetail.css">

</style>