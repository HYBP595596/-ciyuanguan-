<template>
  <div class="uc-page">
    <!-- ============ 顶部用户信息 ============ -->
    <div class="uc-header">
      <div class="user-avatar">
        <img :src="user.avatar" alt=""/>
      </div>
      <div class="user-info">
        <div class="user-nickname">{{ user.nickname }}</div>
        <div class="user-description">{{ user.description }}</div>
      </div>
    </div>

    <!-- ============ 标签页 ============ -->
    <div class="uc-body">
      <el-tabs v-model="activeUserTabName" @tab-click="clickTab">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="infoTab">
          <div class="info-pane">
            <el-form label-width="100px" label-position="left">
              <el-form-item label="账号">{{ user.username }}</el-form-item>
              <el-form-item label="性别">
                <el-tag v-show="user.sex == 0">男</el-tag>
                <el-tag type="danger" v-show="user.sex == 1">女</el-tag>
                <el-tag v-show="user.sex == 2">保密</el-tag>
              </el-form-item>
              <el-form-item label="出生日期">{{ format2(user.birthday) }}</el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 收藏 -->
        <el-tab-pane label="收藏" name="collectTab">
          <el-empty
            v-if="imageTagBookmarkList && imageTagBookmarkList.length == 0"
            :image-size="260"
            description="快去收藏喜欢的图片吧~~~">
          </el-empty>

          <div class="grid-list" v-else>
            <div
              class="grid-card"
              v-for="(item, index) in imageTagBookmarkList"
              :key="index">
              <div class="grid-image" @click="clickImage(item.image.id)">
                <img :src="item.image.url" alt=""/>
              </div>
              <div class="grid-stats">
                <i class="el-icon-medal"></i><span>{{ item.image.score }}</span>
                <i class="el-icon-star-off"></i><span>{{ item.image.collects }}</span>
                <i class="el-icon-view"></i><span>{{ item.image.views }}</span>
              </div>
              <div class="grid-tags">
                <el-tag v-for="(tag, i) in item.tagList" :key="i" size="mini" type="danger">
                  {{ tag.name }}
                </el-tag>
              </div>
              <div class="grid-footer">
                <span class="collect-time">{{ format1(item.imageBookmark.createTime) }}</span>
                <el-popconfirm
                  @confirm="clickCollectDelete(item.image.id)"
                  title="确认删除该收藏？">
                  <span slot="reference" class="delete-btn">✕</span>
                </el-popconfirm>
              </div>
            </div>
          </div>

          <div class="pagination">
            <el-pagination
              background
              :hide-on-single-page="collectHide"
              :page-size="pageSize"
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              layout="prev, pager, next"
              :total="total">
            </el-pagination>
          </div>
        </el-tab-pane>

        <!-- 历史 -->
        <el-tab-pane label="历史" name="historyTab">
          <div class="grid-list" v-if="imageTagHistoryCategoryList && imageTagHistoryCategoryList.length">
            <div
              class="grid-card"
              v-for="(item, index) in imageTagHistoryCategoryList"
              :key="index">
              <div class="grid-time">{{ format1(item.watchHistory.createTime) }}</div>
              <div class="grid-image" @click="clickImage(item.image.id)">
                <img :src="item.image.url" alt=""/>
              </div>
              <div class="grid-stats">
                <i class="el-icon-medal"></i><span>{{ item.image.score }}</span>
                <i class="el-icon-star-off"></i><span>{{ item.image.collects }}</span>
                <i class="el-icon-view"></i><span>{{ item.image.views }}</span>
              </div>
              <div class="grid-tags">
                <el-tag v-for="(tag, i) in item.tagList" :key="i" size="mini" type="danger">
                  {{ tag.name }}
                </el-tag>
              </div>
              <div class="grid-footer">
                <el-tag size="mini">{{ item.category.name }}</el-tag>
                <span class="delete-btn" @click="clickHistoryDelete(item.watchHistory.id)">✕</span>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无浏览历史"></el-empty>

          <div class="pagination">
            <el-pagination
              :hide-on-single-page="historyHide"
              background
              :page-size="historyPagination.pageSize"
              @current-change="handleCurrentChangeHistory"
              :current-page="historyPagination.currentPage"
              layout="prev, pager, next"
              :total="historyPagination.total">
            </el-pagination>
          </div>
        </el-tab-pane>

        <!-- 修改信息 -->
        <el-tab-pane label="修改信息" name="updateTab">
          <el-tabs tab-position="left" type="border-card" @tab-click="clickUpdate" style="min-height: 420px">
            <!-- 修改头像 -->
            <el-tab-pane label="修改头像">
              <div class="avatar-update">
                <div class="avatar-picker">
                  <el-upload
                    :before-upload="beforeUpload"
                    :on-success="userAvatarUploadSuccess"
                    :show-file-list="false"
                    action="http://47.113.192.130:9090/dimension/UploadImageOrUserAvatar"
                    :limit="1">
                    <el-button type="success" icon="el-icon-picture">选择本地图片</el-button>
                  </el-upload>
                  <el-button type="primary" @click="updateUserAvatar">更新头像</el-button>
                </div>
                <div class="avatar-preview">
                  <img :src="selectImage" alt=""/>
                </div>
              </div>
            </el-tab-pane>

            <!-- 修改信息 -->
            <el-tab-pane label="修改信息" name="update-info-tab">
              <el-form
                :model="updateUserInfoForm"
                :rules="updateUserInfoFormRules"
                ref="updateUserInfoForm"
                label-width="100px"
                class="update-form">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="updateUserInfoForm.nickname" placeholder="昵称"></el-input>
                </el-form-item>
                <el-form-item label="我的签名" prop="description">
                  <el-input type="textarea" :rows="4" v-model="updateUserInfoForm.description" placeholder="我的签名"></el-input>
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                  <el-radio-group v-model="updateUserInfoForm.sex">
                    <el-radio label="0">男</el-radio>
                    <el-radio label="1">女</el-radio>
                    <el-radio label="2">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="出生日期" prop="birthday">
                  <el-date-picker v-model="updateUserInfoForm.birthday" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" style="width: 100%" @click="saveUserInfo">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码">
              <el-form
                :model="updatePasswordForm"
                :rules="updatePasswordFormRules"
                ref="updatePasswordForm"
                label-width="100px"
                class="update-form">
                <el-form-item label="原密码" prop="originPassword">
                  <el-input v-model="updatePasswordForm.originPassword" placeholder="原密码"></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="updatePasswordForm.newPassword" placeholder="新密码"></el-input>
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmNewPassword">
                  <el-input v-model="updatePasswordForm.confirmNewPassword" placeholder="确认密码"></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" style="width: 100%" @click="updatePassword">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { format1, format2 } from '@/utils/time-format'

export default {
  name: "UserCenter",
  data() {
    return {
      collectHide: false,
      historyHide: false,
      activeUserTabName: "infoTab",
      user: {},
      imageTagBookmarkList: [],
      historyPagination: { currentPage: 1, pageSize: 8, total: 0 },
      pageSize: 8,
      currentPage: 1,
      total: 0,
      selectImage: '',
      updateUserInfoForm: { nickname: '', description: '', sex: '2', birthday: '' },
      updateUserInfoFormRules: {
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
      },
      updatePasswordForm: { originPassword: '', newPassword: '', confirmNewPassword: '' },
      updatePasswordFormRules: {
        originPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
        confirmNewPassword: [{ required: true, message: '请输入确认密码', trigger: 'blur' }]
      },
      imageTagHistoryCategoryList: []
    }
  },
  mounted() {
    if (!sessionStorage.getItem('user')) {
      this.$router.push('/index-body')
      return
    }
    this.user = JSON.parse(sessionStorage.getItem('user'))
    this.selectImage = this.user.avatar
    this.handleRouter()
  },
  methods: {
    format1(t) { return format1(t) },
    format2(t) { return format2(t) },

    beforeUpload() {
      this.$message.info('正在上传中，请稍等...')
    },

    clickUpdate(tab) {
      if (tab.name == 'update-info-tab') {
        this.updateUserInfoForm = { ...this.user }
      }
    },

    handleCurrentChangeHistory(newPage) {
      this.historyPagination.currentPage = newPage
      this.getHistory()
    },

    clickHistoryDelete(id) {
      this.request.post(`RemoveWatchHistoryById?id=${id}`).then(() => {
        this.$message.success('删除成功')
        this.getHistory()
      })
    },

    updatePassword() {
      this.$refs['updatePasswordForm'].validate(valid => {
        if (!valid) return
        if (this.updatePasswordForm.newPassword != this.updatePasswordForm.confirmNewPassword) {
          this.$message.error('两次输入密码不一致')
          return
        }
        if (this.updatePasswordForm.originPassword != this.user.password) {
          this.$message.error('密码错误')
          return
        }
        this.request.post(`UpdateUserPassword?userId=${this.user.id}&password=${this.updatePasswordForm.newPassword}`).then(() => {
          this.$message.success({ message: '修改成功，请重新登录', duration: 1000 })
          setTimeout(() => {
            sessionStorage.clear()
            this.event.$emit('popLoginForm')
          }, 1000)
        })
      })
    },

    saveUserInfo() {
      this.$refs['updateUserInfoForm'].validate(valid => {
        if (!valid) return
        this.updateUserInfoForm.birthday = new Date(this.updateUserInfoForm.birthday)
          .toLocaleDateString().replace(/\//g, "-")
        this.request.post(`UpdateUserInfo`, { ...this.updateUserInfoForm, userId: this.user.id }).then(() => {
          this.user.nickname = this.updateUserInfoForm.nickname
          this.user.description = this.updateUserInfoForm.description
          this.user.sex = this.updateUserInfoForm.sex
          this.user.birthday = this.updateUserInfoForm.birthday
          sessionStorage.setItem('user', JSON.stringify(this.user))
          this.$message.success({ message: '更新成功，即将刷新页面', duration: 1000 })
          setTimeout(() => location.reload(), 1000)
        })
      })
    },

    updateUserAvatar() {
      this.request.post(`UpdateUserAvatar?userId=${this.user.id}&avatar=${this.selectImage}`).then(() => {
        this.user.avatar = this.selectImage
        sessionStorage.setItem('user', JSON.stringify(this.user))
        this.$message.success({ message: '更新成功，即将刷新页面', duration: 1000 })
        setTimeout(() => location.reload(), 1000)
      })
    },

    userAvatarUploadSuccess(res) {
      this.$message.success('上传成功')
      this.selectImage = res.url
    },

    handleCurrentChange(currentPage) {
      this.currentPage = currentPage
      this.getImageBookmarkList()
    },

    clickCollectDelete(imageId) {
      this.request.post(`RemoveImageBookmark?imageId=${imageId}&userId=${this.user.id}`).then(() => {
        this.$message.success('删除成功')
        this.getImageBookmarkList()
      })
    },

    clickImage(imageId) {
      this.$router.push({
        path: '/image-detail',
        query: { imageId, highlightCategoryId: '-1' }
      })
    },

    getImageBookmarkList() {
      this.request.get(`GetUserImageBookmarkList?userId=${this.user.id}&currentPage=${this.currentPage}&pageSize=${this.pageSize}`).then(res => {
        this.imageTagBookmarkList = res.imageTagBookmarkList
        if (this.imageTagBookmarkList && this.imageTagBookmarkList.length <= 8) {
          this.collectHide = true
        }
        this.total = res.total
      })
    },

    clickTab(tab) {
      if (tab.name == 'collectTab') {
        if (!this.imageTagBookmarkList || this.imageTagBookmarkList.length == 0) {
          this.currentPage = 1
          this.getImageBookmarkList()
        }
      } else if (tab.name == 'historyTab') {
        this.historyPagination.currentPage = 1
        this.imageTagHistoryCategoryList = []
        this.getHistory()
      }
    },

    handleRouter() {
      const activeUserTabName = this.$route.query.activeUserTabName
      if (activeUserTabName) this.activeUserTabName = activeUserTabName
      if (activeUserTabName == 'collectTab') this.getImageBookmarkList()
      else if (activeUserTabName == 'historyTab') this.getHistory()
    },

    getHistory() {
      this.request.get(`GetUserWatchHistory?userId=${this.user.id}&currentPage=${this.historyPagination.currentPage}&pageSize=${this.historyPagination.pageSize}`).then(res => {
        this.imageTagHistoryCategoryList = res.imageTagHistoryCategoryList
        if (this.imageTagHistoryCategoryList && this.imageTagHistoryCategoryList.length <= 8) {
          this.historyHide = true
        }
        this.historyPagination.total = res.total
      })
    }
  }
}
</script>

<style scoped src="./UserCenter.css"></style>