<template>
  <div class="hn-bar">
    <div class="hn-collapse-btn" @click="$emit('toggle-sidebar')">
      <i class="el-icon-s-fold" v-if="!sidebarCollapsed"></i>
      <i class="el-icon-s-unfold" v-else></i>
    </div>

    <!-- Logo -->
    <div class="hn-logo" @click="clickIndex">
      <span>次元馆</span>
    </div>

    <!-- 搜索 -->
    <div class="hn-search">
      <el-input
        v-model="keyword"
        placeholder="搜索插画、漫画..."
        size="small"
        @keyup.enter.native="search">
      </el-input>
      <el-button type="primary" size="small" icon="el-icon-search" @click="search"></el-button>
    </div>

    <!-- 右侧 -->
    <div class="hn-right">
      <span class="hn-item" @click="goPremium">Pixiv</span>

      <!-- 主题切换 -->
      <i
        :class="isDark ? 'el-icon-sunny' : 'el-icon-moon-night'"
        class="hn-icon"
        @click="switchTheme"
        :title="isDark ? '切换到白天模式' : '切换到黑夜模式'"
      ></i>

      <el-button type="warning" size="mini" icon="el-icon-edit" @click="goPublish">发布</el-button>

      <!-- 消息：私信 + 评论回复 + @提及 -->
      <span class="hn-badge-wrap" @click="goMessage" title="消息：私信 / 评论回复 / @提及">
        <i class="el-icon-message hn-icon"></i>
        <em class="hn-badge" v-if="msgUnread">{{ msgUnread > 99 ? '99+' : msgUnread }}</em>
      </span>

      <!-- 通知：系统公告 + 作品更新 + 活动推送 -->
      <span class="hn-badge-wrap" @click="goNotification" title="通知：系统公告 / 作品更新">
        <i class="el-icon-bell hn-icon"></i>
        <em class="hn-badge" v-if="noticeUnread">{{ noticeUnread > 99 ? '99+' : noticeUnread }}</em>
      </span>

      <!-- 已登录：头像下拉 -->
      <el-dropdown v-if="isLogin" class="hn-avatar-wrap" trigger="click">
        <img v-if="user.avatar" :src="user.avatar" class="hn-avatar"/>
        <div v-else class="hn-avatar-dummy">{{ user.nickname ? user.nickname[0] : 'U' }}</div>

        <el-dropdown-menu slot="dropdown" class="hn-dropdown-menu">
          <div class="hn-stats" @click.stop>
            <div class="hn-stat-item" @click="goFollowing">
              <span class="hn-stat-num">{{ followingCount }}</span>
              <span class="hn-stat-label">关注</span>
            </div>
            <div class="hn-stat-item" @click="goFans">
              <span class="hn-stat-num">{{ fansCount }}</span>
              <span class="hn-stat-label">粉丝</span>
            </div>
          </div>

          <el-dropdown-item @click.native="clickUserCenter">个人中心</el-dropdown-item>
          <el-dropdown-item @click.native="clickCollect">我的收藏</el-dropdown-item>
          <el-dropdown-item @click.native="clickHistory">浏览历史</el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>

      <!-- 未登录 -->
      <span v-if="!isLogin" class="hn-login" @click="clickLogin">登录</span>
    </div>

    <!-- 登录/注册弹窗 -->
    <el-dialog :visible.sync="loginDialogVisible" width="30%" :before-close="beforeLoginDialogClose">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginFormRules" ref="loginForm">
            <el-form-item label="账号" prop="username"><el-input v-model="loginForm.username"></el-input></el-form-item>
            <el-form-item label="密码" prop="password"><el-input show-password v-model="loginForm.password"></el-input></el-form-item>
          </el-form>
          <el-button type="primary" @click="login" style="width:100%">登录</el-button>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerFormRules" ref="registerForm">
            <el-form-item label="昵称" prop="nickname"><el-input v-model="registerForm.nickname"></el-input></el-form-item>
            <el-form-item label="账号" prop="username"><el-input v-model="registerForm.username"></el-input></el-form-item>
            <el-form-item label="密码" prop="password"><el-input show-password v-model="registerForm.password"></el-input></el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword"><el-input show-password v-model="registerForm.confirmPassword"></el-input></el-form-item>
          </el-form>
          <el-button type="primary" @click="register" style="width:100%">注册</el-button>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { getTheme, toggleTheme } from '@/utils/theme'

export default {
  name: "HeaderNav",
  props: {
    sidebarCollapsed: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      keyword: '',
      activeTab: 'login',
      loginDialogVisible: false,
      loginForm: { username: '', password: '' },
      loginFormRules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      registerForm: { nickname: '', username: '', password: '', confirmPassword: '' },
      registerFormRules: {
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        confirmPassword: [{ required: true, message: '请输入确认密码', trigger: 'blur' }]
      },
      isDark: getTheme() === 'dark',
      isLogin: false,
      user: {},
      msgUnread: 0,
      noticeUnread: 0,
      followingCount: 0,
      fansCount: 0
    }
  },
  watch: {
    // 切页面时刷新消息/通知角标
    $route() {
      this.loadInbox()
    }
  },
  mounted() {
    this.isLogin = !!sessionStorage.getItem('token')
    if (this.isLogin) {
      try { this.user = JSON.parse(sessionStorage.getItem('user')) } catch (e) {}
      this.loadInbox()
      this.loadFollowCounts()
    }
    if (this.event && this.event.$on) {
      this.event.$on('popLoginForm', () => { this.loginDialogVisible = true })
      // 关注/取关后刷新顶部数字
      this.event.$on('follow-changed', () => this.loadFollowCounts())
      // 消息中心标记已读后刷新角标
      this.event.$on('inbox-changed', () => this.loadInbox())

      // 👇 新增这一段：监听信息更新事件
      this.event.$on('user-info-updated', (newUser) => {
        this.user = newUser; // 收到通知后，立刻替换最新的用户数据
      })

    }
    // token 过期时，request.js 会派发这个事件 → 主动弹出登录框
    this.needLoginHandler = () => {
      this.isLogin = false
      this.user = {}
      this.msgUnread = 0
      this.noticeUnread = 0
      this.loginDialogVisible = true
    }
    window.addEventListener('user-need-login', this.needLoginHandler)
  },
  beforeDestroy() {
    if (this.needLoginHandler) {
      window.removeEventListener('user-need-login', this.needLoginHandler)
      this.needLoginHandler = null
    }
  },
  methods: {
    // 拉取当前登录用户的关注数/粉丝数
    loadFollowCounts() {
      if (!this.isLogin || !this.user || !this.user.id) return
      this.request.get(`Follow?action=counts&userId=${this.user.id}`).then(res => {
        this.followingCount = (res && res.followingCount) || 0
        this.fansCount = (res && res.fansCount) || 0
      }).catch(() => {})
    },
    clickIndex() { if (this.$route.path !== '/index-body') this.$router.push('/index-body') },
    search() {
      if (!this.keyword) return
      if (this.$route.path !== '/index-body') this.$router.push('/index-body')
      if (this.event) this.event.$emit('searchImageWithKeyword', this.keyword)
    },
    clickLogin() { this.loginDialogVisible = true },
    beforeLoginDialogClose() {
      this.loginDialogVisible = false
      if (this.$refs.loginForm) this.$refs.loginForm.resetFields()
      if (this.$refs.registerForm) this.$refs.registerForm.resetFields()
    },
    login() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.request.post(`UserLogin?username=${this.loginForm.username}&password=${this.loginForm.password}`).then(res => {
          if (res.flag == 0) {
            if (res.user.status == 1) { this.$message.error('该用户已被禁用'); return }
            this.$message.success('登录成功')
            this.user = res.user; this.isLogin = true
            sessionStorage.setItem('token', res.token)
            sessionStorage.setItem('user', JSON.stringify(res.user))
            this.loginDialogVisible = false
            location.reload()
          } else { this.$message.error('用户名或密码错误') }
        })
      })
    },
    register() {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return
        if (this.registerForm.password !== this.registerForm.confirmPassword) {
          this.$message.error('两次密码不一致'); return
        }
        this.request.post(`UserRegister?nickname=${this.registerForm.nickname}&username=${this.registerForm.username}&password=${this.registerForm.password}`).then(res => {
          if (res.flag == 2) {
            this.$message.success('注册成功')
            this.activeTab = 'login'
            this.loginForm.username = this.registerForm.username
          } else if (res.flag == 0) {
            this.$message.warning('该昵称已存在')
          } else if (res.flag == 1) {
            this.$message.warning('该账号已存在')
          }
        })
      })
    },
    clickCollect() {
      if (!this.isLogin) { this.$message.warning('请先登录'); this.loginDialogVisible = true }
      else this.$router.push({ path: '/blank-page2', query: { activeUserTabName: 'collectTab' } })
    },
    clickHistory() {
      if (!this.isLogin) { this.$message.warning('请先登录'); this.loginDialogVisible = true }
      else this.$router.push({ path: '/blank-page2', query: { activeUserTabName: 'historyTab' } })
    },
    clickUserCenter() {
      this.$router.push({ path: '/blank-page2', query: { activeUserTabName: 'infoTab' } })
    },
    logout() { sessionStorage.clear(); location.reload() },
    goPremium() {
      window.open('https://www.pixiv.net/', '_blank')
    },
    goPublish() {
      if (!this.isLogin) { this.$message.warning('请先登录'); this.loginDialogVisible = true }
      else this.$router.push('/publish')
    },
    goMessage() {
      if (!this.isLogin) { this.$message.warning('请先登录'); this.loginDialogVisible = true }
      else this.$router.push({ path: '/inbox', query: { tab: 'message' } })
    },
    goNotification() {
      if (!this.isLogin) { this.$message.warning('请先登录'); this.loginDialogVisible = true }
      else this.$router.push({ path: '/inbox', query: { tab: 'notice' } })
    },
    // 顶部两个角标：消息（私信+评论回复+@提及）、通知（公告+作品更新）
    loadInbox() {
      if (!this.isLogin || !this.user || !this.user.id) return
      this.request.get(`Inbox?action=summary&userId=${this.user.id}`).then(res => {
        this.msgUnread = (res && res.messageUnread) || 0
        this.noticeUnread = (res && res.noticeUnread) || 0
      }).catch(() => {})
    },
    goFollowing() {
      this.$router.push({ path: '/user-center', query: { activeUserTabName: 'followingTab' } })
    },
    goFans() {
      this.$router.push({ path: '/user-center', query: { activeUserTabName: 'fansTab' } })
    },
    switchTheme() {
      const next = toggleTheme()
      this.isDark = next === 'dark'
      this.event && this.event.$emit('theme-change', next)
    }
  }
}

</script>

<style scoped src="./HeaderNav.css"></style>