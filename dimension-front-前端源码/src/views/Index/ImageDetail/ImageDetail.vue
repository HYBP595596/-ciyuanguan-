<template>
  <div class="detail-page">
    <!-- ================= 主体：左图 + 右信息 ================= -->
    <div class="detail-main">
      <!-- 1. 左侧：小说正文 / 图片（双击缩放、左右翻页、缩略图） -->
      <div class="image-box" :class="{ 'is-novel': isNovel }">
        <!-- 小说：朴素文本 + 按字数分页 -->
        <div v-if="isNovel" class="novel-panel" ref="novelPanel">
          <img
              v-if="imageTag && imageTag.image"
              :src="$img(imageTag.image.url)"
              class="novel-cover"
              alt="cover"/>

          <div class="novel-loading" v-if="novelLoading">正文加载中…</div>
          <template v-else>
            <div class="novel-meta" v-if="novelPages.length">
              共 {{ novelCharCount }} 字 · {{ novelPages.length }} 页（每页约 {{ charsPerPage }} 字）
            </div>

            <p
                v-for="(para, i) in currentNovelParagraphs"
                :key="novelPage + '-' + i"
                class="novel-para">{{ para }}</p>
            <p v-if="!novelPages.length" class="novel-empty">暂无可读正文</p>

            <div class="novel-pager" v-if="novelPages.length > 1">
              <el-button size="mini" :disabled="novelPage === 0" @click="novelPrev">上一页</el-button>
              <span class="novel-page-info">第 {{ novelPage + 1 }} / {{ novelPages.length }} 页</span>
              <el-button size="mini" :disabled="novelPage >= novelPages.length - 1" @click="novelNext">下一页</el-button>
            </div>
          </template>
        </div>

        <!-- 插画 / 漫画 -->
        <template v-else>
          <div class="stage">
            <img
                v-if="imageTag && imageTag.image"
                :src="currentImageUrl"
                :class="{ zoomed: isZoomed }"
                @dblclick="toggleZoom"
                alt="image"/>

            <div class="page-indicator" v-if="imageGallery.length > 1">
              {{ pageIndex + 1 }} / {{ imageGallery.length }}
            </div>
          </div>

          <!-- 左右翻页：贴着黑色背景的边缘（不是图片边缘），加宽更易点中 -->
          <div class="page-zone left" v-if="canPage" @click="prevPage" title="上一页">
            <i class="el-icon-arrow-left"></i>
          </div>
          <div class="page-zone right" v-if="canPage" @click="nextPage" title="下一页">
            <i class="el-icon-arrow-right"></i>
          </div>

          <div class="thumb-strip" v-if="imageGallery.length > 1">
            <img
                v-for="(src, index) in imageGallery"
                :key="index"
                :src="src"
                :class="{ active: index === pageIndex }"
                @click="pageIndex = index"
                alt="thumb" />
          </div>

          <p class="zoom-hint">双击图片可放大/还原<span v-if="canPage">，左右两侧点击可翻页</span></p>
        </template>
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

            <!-- 名字 + 认证 + 关注/粉丝数 -->
            <div class="author-meta">
              <div class="author-name">
                {{ imageTag.user.nickname }}
                <el-tag
                    v-if="imageTag.user.artistStatus === 2"
                    size="mini"
                    type="warning"
                    class="author-artist-tag">
                  认证画师
                </el-tag>
              </div>
              <div class="author-fans">
                <span class="author-stat"><b>{{ authorFollowingCount }}</b> 关注</span>
                <span class="author-stat"><b>{{ authorFansCount }}</b> 粉丝</span>
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

          <!-- 通用私信入口：随时给作者发消息 -->
          <el-button
              v-if="user && user.id !== imageTag.user.id"
              size="small"
              round
              icon="el-icon-chat-dot-round"
              @click="openChatAuthor">
            私信
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
          <!-- 点赞 -->
          <div class="action-item" @click="isLike === 0 ? cancelLikeImage() : tryLikeImage()">
            <i class="el-icon-thumb" :class="{ active: isLike === 0 }"></i>
            <!-- 修复：把 score 改为 votes -->
            <span>{{ imageTag.image ? (imageTag.image.votes || 0) : 0 }}</span>
          </div>
          <!-- 收藏 -->
          <div class="action-item" @click="clickCollect">
            <i class="el-icon-star-on" :class="{ active: isCollect }"></i>
            <span>{{ imageTag.image ? (imageTag.image.collects || 0) : 0 }}</span>
          </div>
          <!-- 下载 -->
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
              <img :src="$img(item.image.url)" alt="" />
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

    <!-- 评论对话框 -->
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
      authorFollowingCount: 0,  // 作者的关注数
      authorFansCount: 0,       // 作者的粉丝数
      pageIndex: 0,      // 当前第几页（多图 / 漫画翻页）
      isZoomed: false,   // 双击缩放状态
      novelText: '',     // 小说正文
      novelLoading: false,
      novelPage: 0,      // 小说当前页（按字数分页）
      charsPerPage: 2000 // 每页目标字数：中文网文常见 1500~3000 字/页，2000 折中
    }
  },
  computed: {
    // 封面 + extraImages（逗号分隔）拼成完整图集
    imageGallery() {
      const image = this.imageTag && this.imageTag.image
      if (!image) return []
      const list = [image.url]
      if (image.extraImages) {
        image.extraImages.split(',').forEach(item => {
          const url = (item || '').trim()
          if (url && list.indexOf(url) === -1) {
            list.push(url)
          }
        })
      }
      return list.filter(Boolean)
    },
    currentImageUrl() {
      return this.imageGallery[this.pageIndex] || (this.imageTag.image ? this.imageTag.image.url : '')
    },
    canPage() {
      return this.imageGallery.length > 1
    },
    // 分类 3 = 小说；或者记录了正文 txt 也算小说
    isNovel() {
      const image = this.imageTag && this.imageTag.image
      if (!image) return false
      return image.categoryId === 3 || !!image.txtUrl
    },
    // 正文按换行切段，朴素文本展示
    novelParagraphs() {
      if (!this.novelText) return []
      return this.novelText
          .replace(/\r\n/g, '\n')
          .split(/\n+/)
          .map(p => p.trim())
          .filter(p => p.length > 0)
    },
    // 按字数把段落装进一页（超长段落先按句号切碎，避免单段撑爆一页）
    novelPages() {
      const paras = this.novelParagraphs
      if (!paras.length) return []
      const limit = this.charsPerPage
      const pages = []
      let cur = []
      let curLen = 0
      for (const para of paras) {
        for (const chunk of this.splitLongParagraph(para, Math.floor(limit * 0.6))) {
          if (curLen > 0 && curLen + chunk.length > limit) {
            pages.push(cur)
            cur = []
            curLen = 0
          }
          cur.push(chunk)
          curLen += chunk.length
        }
      }
      if (cur.length) pages.push(cur)
      return pages
    },
    currentNovelParagraphs() {
      return this.novelPages[this.novelPage] || []
    },
    novelCharCount() {
      return this.novelParagraphs.reduce((sum, p) => sum + p.length, 0)
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
    // 检查关注状态
    checkFollowStatus() {
      if (!this.isLogin || !this.imageTag.user) return
      // 不能查自己关注自己
      if (this.user.id === this.imageTag.user.id) return

      this.request.get(`CheckFollowStatus?userId=${this.user.id}&targetUserId=${this.imageTag.user.id}`).then(res => {
        this.isFollowingAuthor = res.isFollowing
        this.authorFansCount = res.fansCount // 顺便更新粉丝数
      })
    },
    beforeCommentDialogClose() {
      this.$refs['commentForm'].resetFields()
      this.commentDialogVisible = false
    },
    format1(timestamp) { return format1(timestamp) },

    clickAllCategory() { this.$router.push('/index-body') },

    // 下载：小说优先下正文 txt，其它下作品原图；走相对地址由 dev 代理转发
    downloadImage() {
      const img = this.imageTag && this.imageTag.image
      if (!img) return
      const target = (this.isNovel && img.txtUrl) ? img.txtUrl : img.url
      if (!target) {
        return this.$message.warning('该作品没有可下载的文件')
      }
      const url = `/dimension_dev_01_war_exploded/DownloadImage?url=${encodeURIComponent(target)}`
      window.open(url, '_blank')
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

    // 点击作者头像跳转到作者主页
    clickUserCenter(authorId) {
      if (!authorId) return;

      // 如果是自己点自己，直接跳自己的个人中心
      if (this.isLogin && this.user.id === authorId) {
        this.$router.push({ path: '/user-center' });
      } else {
        // 如果是点别人，把作者的 userId 通过 query 传过去
        this.$router.push({
          path: '/user-center',
          query: { userId: authorId }
        });
      }
    },

    toggleFollowAuthor() {
      if (!this.isLogin) {
        this.$message.warning('请先登录');
        this.event.$emit('popLoginForm');
        return;
      }

      const url = this.isFollowingAuthor ? 'UnfollowUser' : 'FollowUser';

      this.request.post(`${url}?userId=${this.user.id}&targetUserId=${this.imageTag.user.id}`).then(() => {
        this.isFollowingAuthor = !this.isFollowingAuthor;
        // 通知顶栏刷新关注/粉丝数字
        this.event.$emit('follow-changed');

        // 动态更新作者粉丝数显示
        if (this.isFollowingAuthor) {
          this.authorFansCount = (this.authorFansCount || 0) + 1;
          this.$message.success('关注成功');
        } else {
          this.authorFansCount = Math.max(0, (this.authorFansCount || 0) - 1);
          this.$message.success('已取消关注');
        }
      })
    },

    clickComment() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      this.commentDialogVisible = true
    },

    // 修复后的收藏逻辑
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
          // 本地收藏数 +1
          this.imageTag.image.collects = (this.imageTag.image.collects || 0) + 1
        })
      } else {
        this.request.post(`RemoveImageBookmark?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
          this.isCollect = false
          // 本地收藏数 -1
          this.imageTag.image.collects = Math.max(0, (this.imageTag.image.collects || 0) - 1)
          this.$message.success('取消收藏')
        })
      }
    },

    // 修复后的点赞逻辑
    tryLikeImage() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      this.request.post(`LikeImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        // 如果原先不是点赞状态（-1未评价 或 1踩），点赞数 +1
        if (this.isLike !== 0) {
          this.imageTag.image.votes = (this.imageTag.image.votes || 0) + 1
        }
        this.$message.success('点赞成功')
        this.isLike = 0
        this.reComputeImageScore()
      })
    },

    // 修复后的取消点赞逻辑
    cancelLikeImage() {
      if (!this.isLogin) return
      this.request.post(`CancelRateImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        // 如果原先就是点赞状态，取消后点赞数 -1
        if (this.isLike === 0) {
          this.imageTag.image.votes = Math.max(0, (this.imageTag.image.votes || 0) - 1)
        }
        this.isLike = -1
        this.reComputeImageScore()
      })
    },

    // 踩（虽然当前UI没有展示踩的按钮，但保留逻辑并修复）
    tryDislikeImage() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      this.request.post(`DislikeImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        // 如果原先处于点赞状态，切换为踩时点赞数 -1
        if (this.isLike === 0) {
          this.imageTag.image.votes = Math.max(0, (this.imageTag.image.votes || 0) - 1)
        }
        this.isLike = 1
        this.reComputeImageScore()
      })
    },

    // 取消踩
    cancelDislikeImage() {
      if (!this.isLogin) return
      this.request.post(`CancelRateImage?imageId=${this.imageId}&userId=${this.user.id}`).then(() => {
        this.isLike = -1
        this.reComputeImageScore()
      })
    },

    // 重新计算评分
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
        // 每次进入新详情都重置翻页 / 缩放 / 正文状态
        this.pageIndex = 0
        this.isZoomed = false
        this.novelText = ''
        this.novelPage = 0
        if (this.isNovel) {
          this.loadNovelText()
        }
        this.getCommentList()
        this.getRecommendImageList()
        this.checkFollowStatus() // 拿到详情后立刻查关注状态
        this.loadAuthorCounts()  // 作者的关注数/粉丝数
      })
    },

    // 双击放大 / 还原
    toggleZoom() {
      this.isZoomed = !this.isZoomed
    },
    // 作者主页的关注数/粉丝数
    loadAuthorCounts() {
      const author = this.imageTag && this.imageTag.user
      if (!author || !author.id) return
      this.request.get(`Follow?action=counts&userId=${author.id}`).then(res => {
        this.authorFollowingCount = (res && res.followingCount) || 0
        this.authorFansCount = (res && res.fansCount) || 0
      }).catch(() => {})
    },
    // 通用私信：给作品作者发消息
    openChatAuthor() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.event.$emit('popLoginForm')
        return
      }
      const author = this.imageTag && this.imageTag.user
      if (!author) return
      this.$router.push({ path: '/messages', query: { peerId: author.id } })
    },
    // 左右翻页（循环）
    prevPage() {
      if (!this.canPage) return
      const total = this.imageGallery.length
      this.pageIndex = (this.pageIndex - 1 + total) % total
      this.isZoomed = false
    },
    nextPage() {
      if (!this.canPage) return
      const total = this.imageGallery.length
      this.pageIndex = (this.pageIndex + 1) % total
      this.isZoomed = false
    },
    // 小说正文：复用 ShowUploadImage 把 .txt 读成文本
    loadNovelText() {
      const image = this.imageTag && this.imageTag.image
      if (!image || !image.txtUrl) {
        this.novelText = ''
        return
      }
      this.novelLoading = true
      fetch(image.txtUrl)
          .then(resp => {
            if (!resp.ok) throw new Error('HTTP ' + resp.status)
            return resp.text()
          })
          .then(text => {
            this.novelText = (text || '').trim()
          })
          .catch(() => {
            this.novelText = ''
            this.$message.error('正文加载失败')
          })
          .finally(() => {
            this.novelLoading = false
          })
    },

    // 超长段落按句子边界切块（找不到句号就硬切），保证单段不会超过一页
    splitLongParagraph(text, max) {
      if (!text || text.length <= max) return [text]
      const stops = '。！？!?…；;'
      const result = []
      let rest = text
      while (rest.length > max) {
        let cut = -1
        for (let i = max; i > Math.floor(max * 0.5); i--) {
          if (stops.indexOf(rest.charAt(i - 1)) >= 0) {
            cut = i
            break
          }
        }
        if (cut < 0) cut = max
        result.push(rest.slice(0, cut))
        rest = rest.slice(cut)
      }
      if (rest) result.push(rest)
      return result
    },
    // 小说翻页：翻完回到正文顶部
    novelPrev() {
      if (this.novelPage <= 0) return
      this.novelPage -= 1
      this.scrollToNovelTop()
    },
    novelNext() {
      if (this.novelPage >= this.novelPages.length - 1) return
      this.novelPage += 1
      this.scrollToNovelTop()
    },
    scrollToNovelTop() {
      const el = this.$refs.novelPanel
      if (el && el.scrollIntoView) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
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