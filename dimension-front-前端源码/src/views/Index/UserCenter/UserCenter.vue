<template>
  <!--用户个人中心页-->
  <div class="body" style="margin-top: 50px;">
    <!--上部用户头像和背景图片展示区-->
    <div class="background-and-user-avatar">
      <!--用户头像-->
      <div class="user-avatar">
        <img :src="user.avatar" class="body-image"/>
      </div>
      <!--用户昵称和个人简介-->
      <div class="user-nickname-and-description">
        <!--用户昵称-->
        <div class="user-nickname">
          <span style="color: rgba(255, 255, 255); font-weight: bolder; font-size: 18px">{{user.nickname}}</span>
        </div>
        <!--用户简介-->
        <div class="description">
          <span >{{user.description}}</span>
        </div>
      </div>
    </div>
    <!--下部用户标签页展示区域-->
    <div class="bottom-user-tabs-region">
      <el-card style="background-color: rgba(255, 255, 255, 0.7)">
        <el-tabs v-model="activeUserTabName" @tab-click="clickTab">
          <!--个人信息标签页-->
          <el-tab-pane label="个人信息" name="infoTab" style="height: 330px">
            <div style="margin-left: 35%; margin-top: 4%">
              <el-form label-width="100px">
                <el-form-item label="账号">
                  {{user.username}}
                </el-form-item>
                <el-form-item label="性别">
                  <el-tag v-show="user.sex == 0">男</el-tag>
                  <el-tag type="danger" v-show="user.sex == 1">女</el-tag>
                  <el-tag v-show="user.sex == 2">保密</el-tag>
                </el-form-item>
                <el-form-item label="出生日期">
                  {{format2(user.birthday)}}
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
          <!--收藏标签页-->
          <el-tab-pane label="收藏" name="collectTab">
            <el-empty :image-size="400" v-if="imageTagBookmarkList && imageTagBookmarkList.length == 0" image="https://dimension-pojo.oss-cn-beijing.aliyuncs.com/%E6%AC%A1%E5%85%83%E9%A6%86/%E5%85%B6%E5%AE%83/load-error.685235d2.png" description="快去收藏喜欢的图片吧~~~"></el-empty>
            <!--收藏图片列表-->
            <div class="collect-image-list">
              <!--每个收藏图片-->
              <div class="collect-image-div body-image" v-for="(item, index) in imageTagBookmarkList" :key="index">
                <!--图片-->
                <div class="image-detail" @click="clickImage(item.image.id)">
                  <img :src="item.image.url"/>
                </div>
                <!--分数、收藏数、浏览数展示-->
                <div class="score-collects-views">
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
                <!--收藏时间-->
                <div class="collect-time" style="text-align: center; padding-left: 50px">
                  {{format1(item.imageBookmark.createTime)}}
                </div>
                <!--删除小红叉-->
                <div class="delete">
                  <el-popconfirm
                      @confirm="clickCollectDelete(item.image.id)"
                      title="确认删除该收藏？"
                  >
                    <span slot="reference">❌</span>
                  </el-popconfirm>
                </div>
              </div>
            </div>
            <!--收藏图片分页器-->
            <div class="pagination" style="margin-top: 100px; margin-bottom: 70px">
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
          <!--历史标签页-->
          <el-tab-pane label="历史" name="historyTab" class="history">
            <!--历史浏览图片展示块-->
            <div class="history-image-div body-image" v-for="(item, index) in imageTagHistoryCategoryList" :key="index">
              <!--            <div class="fen-ge-xian">-->
              <!--              <el-divider direction="vertical"></el-divider>-->
              <!--            </div>-->
              <!--时间-->
              <div class="time" v-if="item && item.watchHistory" style="padding-left: 30px">
                {{format1(item.watchHistory.createTime)}}
              </div>
              <!--              图片-->
              <div class="history-image" v-if="item && item.image" @click="clickImage(item.image.id)">
                <img :src="item.image.url" class="history-image"/>
              </div>
              <div class="history-body" v-if="item && item.image && item.category">
                <!--                名字-->
                <div class="history-body-tagList">
                  <el-tag style="margin-left: 10px; font-size: 14px" type="danger" v-for="(item1, index1) in item.tagList" :key="index1">
                    {{item1.name}}
                  </el-tag>
                </div>
                <div class="history-body-bottom">
                  <div class="left">
                    <i class="el-icon-medal"></i>
                    <span>{{item.image.score}}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <i class="el-icon-star-off"></i>
                    <span>{{item.image.collects}}</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <i class="el-icon-view"></i>
                    <span>{{item.image.views}}</span>
                  </div>
                  <div class="right">
                    <el-tag style="font-size: 14px">{{item.category.name}}</el-tag>
                  </div>
                </div>
                <!--              <div class="fen-ge-xian">-->
                <!--                <el-divider></el-divider>-->
                <!--              </div>-->
                <!--可以用定位来定位到那里😂-->
                <div class="delete-button" @click="clickHistoryDelete(item.watchHistory.id)">
                  <img src="@/static/image/history-delete-image.png"/>
                </div>
              </div>
            </div>
            <!--              分页器-->
            <div class="history-pagination">
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
          <!--修改信息标签页-->
          <el-tab-pane label="修改信息" name="updateTab">
            <el-tabs tab-position="left" type="border-card" @tab-click="clickUpdate" style="height: 400px;">
              <!--修改头像标签页-->
              <el-tab-pane label="修改头像">
                <!--上部-->
                <div class="top">
                  <!--选择图片-->
                  <div class="select-image">
                    <!--                        action="http://localhost:9000/dimension/UploadImageOrUserAvatar"-->
                    <el-upload
                        class="upload-demo"
                        :before-upload="beforeUpload"
                        :on-success="userAvatarUploadSuccess"
                        :show-file-list="false"
                        action="http://47.113.192.130:9090/dimension/UploadImageOrUserAvatar"
                        :limit="1">
                      <el-button style="height: 100px; font-size: 20px" type="success" icon="el-icon-picture">选择本地图片</el-button>
                    </el-upload>
                  </div>
                  <!--当前头像-->
                  <div class="current-image">
                    <img :src="selectImage" class="body-image" style="height: 200px; width: 200px; border-radius: 50%"/>
                  </div>
                </div>
                <!--下部更新按钮-->
                <div class="bottom" style="margin-top: 110px;">
                  <el-button type="primary" @click="updateUserAvatar" style="width: 200px">更新</el-button>
                </div>
              </el-tab-pane>
              <!--修改信息标签页-->
              <el-tab-pane label="修改信息" name="update-info-tab">
                <!--修改信息表单-->
                <div class="update-form" style="margin-left: 20%">
                  <el-form :model="updateUserInfoForm" :rules="updateUserInfoFormRules" ref="updateUserInfoForm" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="昵称" prop="nickname">
                      <el-input v-model="updateUserInfoForm.nickname" style="width: 400px" placeholder="昵称"></el-input>
                    </el-form-item>
                    <el-form-item label="我的签名" prop="description">
                      <el-input type="textarea" :rows="5" style="width: 400px" v-model="updateUserInfoForm.description" placeholder="我的签名"></el-input>
                    </el-form-item>
                    <el-form-item label="性别" prop="sex">
                      <el-radio-group v-model="updateUserInfoForm.sex">
                        <el-radio label="0">男</el-radio>
                        <el-radio label="1">女</el-radio>
                        <el-radio label="2">保密</el-radio>
                      </el-radio-group>
                    </el-form-item>
                    <el-form-item label="出生日期" prop="birthday">
                      <el-date-picker
                          style="width: 400px"
                          v-model="updateUserInfoForm.birthday"
                          type="date"
                          placeholder="选择日期">
                      </el-date-picker>
                    </el-form-item>
                  </el-form>
                </div>
                <!--保存修改信息按钮-->
                <div class="updateInfoButton">
                  <el-button type="primary" @click="saveUserInfo" style="width: 400px">保存</el-button>
                </div>
              </el-tab-pane>
              <!--修改密码标签页-->
              <el-tab-pane label="修改密码">
                <!--表单-->
                <div style="margin-left: 22%; margin-top: 2%">
                  <el-form :model="updatePasswordForm" :rules="updatePasswordFormRules" ref="updatePasswordForm" label-width="100px" class="demo-ruleForm">
                    <el-form-item label="原密码" prop="originPassword">
                      <el-input style="width: 400px" v-model="updatePasswordForm.originPassword" placeholder="原密码"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input style="width: 400px" v-model="updatePasswordForm.newPassword" placeholder="新密码"></el-input>
                    </el-form-item>
                    <el-form-item label="确认密码" prop="confirmNewPassword">
                      <el-input style="width: 400px" v-model="updatePasswordForm.confirmNewPassword" placeholder="确认密码"></el-input>
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" style="width: 400px; margin-top: 15px" @click="updatePassword">保存</el-button>
                    </el-form-item>
                  </el-form>
                  <!--                &lt;!&ndash;按钮&ndash;&gt;-->
                  <!--                <div>-->
                  <!--                </div>-->
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>
  </div>
</template>

<script>
import {format1, format2} from '@/utils/time-format'
export default {
  name: "UserCenter",
  data() {
    return {
      collectHide: false,
      historyHide: false,
      activeUserTabName: "infoTab",
      user: {},
      imageTagBookmarkList: [],
      // bookmarkPagination: {
      //   currentPage: 1,
      //   pageSize: 4,
      //   total: 0
      // },
      historyPagination: {
        currentPage: 1,
        pageSize: 8,
        total: 0
      },
      pageSize: 8,
      currentPage: 1,
      total: 0,
      selectImage: '',
      updateUserInfoForm: {nickname: '', description: '', sex: '2', birthday: ''}, // 修改信息表单
      updateUserInfoFormRules: {
        nickname: [{required: true, message: '请输入昵称', trigger: 'blur'}]
      },
      updatePasswordForm: {originPassword: '', newPassword: '', confirmNewPassword: ''},
      updatePasswordFormRules: {
        originPassword: [{required: true, message: '请输入原密码', trigger: 'blur'}],
        newPassword: [{required: true, message: '请输入新密码', trigger: 'blur'}],
        confirmNewPassword: [{required: true, message: '请输入确认密码', trigger: 'blur'}]
      },
      imageTagHistoryCategoryList: []
    }
  },
  mounted() {
    console.log('刷新了')
    // 如果用户对象为空，表示退出登录了，要跳转到首页
    if (!sessionStorage.getItem('user')) {
      console.log('执行了')
      this.$router.push('/index-body')
    }

    this.user = JSON.parse(sessionStorage.getItem('user'))
    this.selectImage = this.user.avatar
    console.log('user', this.user)

    this.handleRouter()
  },
  methods: {
    format2(timestamp) {
      return format2((timestamp))
    },
    format1(timestamp) {
      return format1(timestamp)
    },
    beforeUpload() {
      this.$message.info('正在上传中，请稍等...')
    },
    clickUpdate(tab) {
      console.log(tab.name)
      if (tab.name == 'update-info-tab') {
        this.updateUserInfoForm = {...this.user}
        console.log(this.updateUserInfoForm)
      }
    },
    handleCurrentChangeHistory(newPage) {
      this.historyPagination.currentPage = newPage
      this.getHistory()
    },
    clickHistoryDelete(id) {
      this.request.post(`RemoveWatchHistoryById?id=${id}`).then(res => {
        this.$message.success('删除成功')
        // var newArr = []
        // for (var item of this.imageTagHistoryCategoryList) {
        //   if (item.watchHistory.id != id) {
        //     newArr.push(item)
        //   }
        // }
        // console.log('newArr', newArr)
        // this.imageTagHistoryCategoryList = newArr
        this.getHistory()
      })
    },
    // 修改密码
    updatePassword() {
      this.$refs['updatePasswordForm'].validate(valid => {
        if (valid) {
          if (this.updatePasswordForm.newPassword != this.updatePasswordForm.confirmNewPassword) {
            this.$message.error('两次输入密码不一致')
            return
          }
          if (this.updatePasswordForm.originPassword != this.user.password) {
            this.$message.error('密码错误')
            return
          }
          this.request.post(`UpdateUserPassword?userId=${this.user.id}&password=${this.updatePasswordForm.newPassword}`).then(res => {
            this.$message.success({
              message: '修改成功，请重新登录',
              duration: 1000
            })
            setTimeout(() => {
              // 注销登录
              sessionStorage.clear()
              // 刷新页面，并打开登录框
              this.event.$emit('popLoginForm')
            }, 1000)
          })
        }
      })

    },
    // 保存用户信息
    saveUserInfo() {
      this.$refs['updateUserInfoForm'].validate(valid => {
        if (valid) {
          this.updateUserInfoForm.birthday = new Date(this.updateUserInfoForm.birthday).toLocaleDateString().replace(/\//g,"-"); //返回'2017-2-20'
          // this.updateUserInfoForm.birthday = this.updateUserInfoForm.birthday.toString()
          this.request.post(`UpdateUserInfo`, {...this.updateUserInfoForm, userId: this.user.id}).then(res => {
            // 修改user信息
            this.user.nickname = this.updateUserInfoForm.nickname
            this.user.description = this.updateUserInfoForm.description
            this.user.sex = this.updateUserInfoForm.sex
            this.user.birthday = this.updateUserInfoForm.birthday
            // 修改Cookie中的user信息
            sessionStorage.setItem('user', JSON.stringify(this.user))
            // 提示并刷新页面
            this.$message.success({
              message: '更新成功，即将刷新页面',
              duration: 1000
            })
            setTimeout(() => {
              location.reload()
            }, 1000)
          })
        }
      })
    },
    // 回显用户信息 - 直接从原有信息中取出即可
    clickUpdateInfoTab() {
      console.log('点击了update-info')
      this.updateUserInfoForm = { ...this.user }
    },
    // 更新用户头像
    updateUserAvatar() {
      this.request.post(`UpdateUserAvatar?userId=${this.user.id}&avatar=${this.selectImage}`).then(res => {
        // 修改user信息
        this.user.avatar = this.selectImage
        // 修改Cookie中的user信息
        // Cookies.set('user', this.user)
        sessionStorage.setItem('user', JSON.stringify(this.user))
        // 提示并刷新页面
        this.$message.success({
          message: '更新成功，即将刷新页面',
          duration: 1000
        })
        setTimeout(() => {
          location.reload()
        }, 1000)
      })
    },
    // 用户头像上传成功事件
    userAvatarUploadSuccess(res) {
      console.log(res)
      // 修改展示图片
      this.$message.success('上传成功')
      this.selectImage = res.url
    },
    // 图片收藏换页
    handleCurrentChange(currentPage) {
      this.currentPage = currentPage
      console.log('currentPage', currentPage)
      this.getImageBookmarkList()
    },
    // 点击收藏页的删除小红叉
    clickCollectDelete(imageId) {
      console.log('点击小红叉')
      this.request.post(`RemoveImageBookmark?imageId=${imageId}&userId=${this.user.id}`).then(res => {
        this.$message.success('删除成功')
        // 重新查询，页数不变
        this.getImageBookmarkList()
      })
    },
    // 收藏页点击image，跳转到详情页
    clickImage(imageId) {
      this.$router.push({
        path: '/image-detail',
        query: {
          imageId,
          highlightCategoryId: '-1' // 分页不高亮展示
        }
      })
    },
    // 获取收藏图片列表
    getImageBookmarkList() {
      // (2 - 1) * 4 = 4 -> 4, 4
      console.log(this.total)
      this.request.get(`GetUserImageBookmarkList?userId=${this.user.id}&currentPage=${this.currentPage}&pageSize=${this.pageSize}`).then(res => {
        this.imageTagBookmarkList = res.imageTagBookmarkList
        if (this.imageTagBookmarkList && this.imageTagBookmarkList.length <= 8) {
          this.collectHide = true
        }
        this.total = res.total
        console.log(this.total)
        console.log(res)
      })
    },
    // 点击切换标签页
    clickTab(tab) {
      // 收藏页
      if (tab.name == 'collectTab') {
        // 先判断收藏列表是否空
        console.log('点击收藏')
        if (!this.imageTagBookmarkList || (this.imageTagBookmarkList && this.imageTagBookmarkList.length == 0)) {
          // 从第一页开始查
          this.currentPage = 1
          this.getImageBookmarkList()
        }
      } else if (tab.name == 'historyTab') {
        // 先判断历史图片列表是否为空 - 应该是分页查询第一页 - 然后绑定触底事件 - 触底再currentPage+1查询下一页 - 然后再将查询出来的新数据push到列表中 - 如果查出来是空的呢 - 那么就不push即可 - 所以push前先判断一下即可
        this.historyPagination.currentPage = 1
        this.imageTagHistoryCategoryList = []
        this.getHistory()
      }
    },
    // 处理路由逻辑
    handleRouter() {
      var activeUserTabName = this.$route.query.activeUserTabName
      console.log('activeUserTab', activeUserTabName, this.$route.query.activeUserTabName)
      if (activeUserTabName) {
        this.activeUserTabName = activeUserTabName
      }
      if (activeUserTabName == 'collectTab') {
        // 查询收藏页
        this.getImageBookmarkList()
      } else if (activeUserTabName == 'historyTab') {
        // 获取历史页
        this.getHistory()
      }
    },
    getHistory() {
      this.request.get(`GetUserWatchHistory?userId=${this.user.id}&currentPage=${this.historyPagination.currentPage}&pageSize=${this.historyPagination.pageSize}`).then(res => {
        console.log(res)
        this.imageTagHistoryCategoryList = res.imageTagHistoryCategoryList
        if (this.imageTagHistoryCategoryList && this.imageTagHistoryCategoryList.length <= 8) {
          this.historyHide = true
        }
        this.historyPagination.total = res.total
        console.log(this.imageTagHistoryCategoryList)
      })
    }
  }
}
</script>

<style scoped src="./UserCenter.css">
.background-and-user-avatar {
  background-image: url("@/static/image/user-center-background.jpg");
}
</style>