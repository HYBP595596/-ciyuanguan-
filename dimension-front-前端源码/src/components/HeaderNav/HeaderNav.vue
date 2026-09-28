<template>
  <div :class="$route.path == '/index-body' ? 'new-body' : ''">
    <!--头部导航栏组件-->
    <div class="body">
      <!--首页链接-->
      <div class="index">
        <p @click="clickIndex" class="body-image2">次元馆</p>
      </div>
      <!--搜索输入框-->
      <div class="search-input">
        <!--搜索输入框-->
        <el-input v-model="keyword" placeholder="关键字" clearable style="width: 230px"></el-input>
        <!--搜索按钮-->
        <el-button type="primary" class="search" @click="search" icon="el-icon-search">搜索</el-button>
      </div>
      <!--登录按钮或用户头像-->
      <div class="login-button-or-user-image">
        <!--登录按钮-->
        <div class="login-button" v-show="!isLogin">
          <span @click="clickLogin">登录</span>
        </div>
        <!--用户头像-->
        <div class="user-image" v-show="isLogin">
          <el-dropdown style="margin-top: 25px">
          <span class="el-dropdown-link">
            <img :src="user.avatar" @click="clickUserCenter" class="body-image"/>
          </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="clickUserCenter">个人中心</el-dropdown-item>
              <el-divider></el-divider>
              <el-dropdown-item @click.native="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

        </div>
      </div>
      <!--收藏按钮-->
      <div class="collect my-hover">
        <span @click="clickCollect">收藏</span>
      </div>
      <!--历史按钮-->
      <div class="history my-hover">
        <span @click="clickHistory">历史</span>
      </div>

      <!--******************************************-->

      <!--其他并不直接展示到页面上的信息 // 如对话框之类的-->
      <div class="other">
        <!--登录对话框-->
        <div class="login-dialog">
          <el-dialog
              :before-close="beforeLoginDialogClose"
              :visible.sync="loginDialogVisible"
              width="30%">
            <!--登录或注册标签-->
            <el-row class="login-or-register" style="text-align: center">
              <!--登录标签-->
              <el-col :span="11" class="login" style="padding-left: 30%; cursor:pointer;">
                <span :style="{color: isLoginForm ? 'blue' : ''}" @click="isLoginForm = true">登录</span>
              </el-col>
              <el-col :span="2"><el-divider direction="vertical"/></el-col>
              <!--注册标签-->
              <el-col :span="11" class="register" style="padding-right: 30%; cursor: pointer">
                <span :style="{color: !isLoginForm ? 'blue' : ''}" @click="isLoginForm = false">注册</span>
              </el-col>
            </el-row>
            <!--登录或注册表单-->
            <div class="form" style="margin-top: 30px">
              <!--登录表单-->
              <div class="login-form" v-show="isLoginForm">
                <el-form :model="loginForm" :rules="loginFormRules" ref="loginForm" label-width="80px" class="demo-ruleForm">
                  <el-form-item label="账号" prop="username">
                    <el-input v-model="loginForm.username"></el-input>
                  </el-form-item>
                  <el-form-item label="密码" prop="password">
                    <el-input show-password v-model="loginForm.password"></el-input>
                  </el-form-item>
                </el-form>
              </div>
              <!--注册表单-->
              <div class="register-form" v-show="!isLoginForm">
                <el-form :model="registerForm" :rules="registerFormRules" ref="registerForm" label-width="80px" class="demo-ruleForm">
                  <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="registerForm.nickname"></el-input>
                  </el-form-item>
                  <el-form-item label="账号" prop="username">
                    <el-input v-model="registerForm.username"></el-input>
                  </el-form-item>
                  <el-form-item label="密码" prop="password">
                    <el-input show-password v-model="registerForm.password"></el-input>
                  </el-form-item>
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input show-password v-model="registerForm.confirmPassword"></el-input>
                  </el-form-item>
                </el-form>
              </div>
            </div>
            <!--登录或注册按钮-->
            <div class="login-or-register-button">
              <!--登录按钮-->
              <div class="login-button-01" v-show="isLoginForm" style="text-align: center">
                <el-button type="primary" @click="login" style="width: 375px; margin-left: 80px">登录</el-button>
              </div>
              <!--注册按钮-->
              <div class="register-button" v-show="!isLoginForm" style="text-align: center">
                <el-button type="primary" @click="register" style="width: 375px; margin-left: 80px">注册</el-button>
              </div>
            </div>
          </el-dialog>
        </div>
      </div>
    </div>
  </div>

</template>

<script>
import Cookies from 'js-cookie'

export default {
  name: "HeaderNav",
  data() {
    return {
      keyword: '',
      isLoginForm: true,
      loginDialogVisible: false,
      loginForm: {username: '', password: ''},
      loginFormRules: {
        username: [{required: true, message: '请输入账号', trigger: 'blur'}],
        password: [{required: true, message: '请输入密码', trigger: 'blur'}]
      },
      user: {}, // 这里应该就是自动登录了把，如果取出来的话那么就直接显示用户信息自动登录了 - 嗯的 - 其实还要去查一下，无所谓了吧，先这样
      isLogin: false, // 是否登录
      registerForm: {nickname: '', username: '', password: '', confirmPassword: ''},
      registerFormRules: {
        nickname: [{required: true, message: '请输入昵称', trigger: 'blur'}],
        username: [{required: true, message: '请输入账号', trigger: 'blur'}],
        password: [{required: true, message: '请输入密码', trigger: 'blur'}],
        confirmPassword: [{required: true, message: '请输入确认密码', trigger: 'blur'}]
      }
    }
  },
  mounted() {
    // 监听弹出登录框事件
    this.listenPopLoginDialogEvent()
    // 判断用户登录状态
    // this.judgeUserLoginState()
    console.log(this.user)
    console.log(sessionStorage.getItem('user'))
    this.isLogin = sessionStorage.getItem('token') ? true : false
    if (this.isLogin) {
      this.user = JSON.parse(sessionStorage.getItem('user'))
    }
    console.log(this.user)
  },
  methods: {
    beforeLoginDialogClose() {
      this.$refs['loginForm'].resetFields()
      this.$refs['registerForm'].resetFields()
      this.loginDialogVisible = false
    },
    // 退出登录
    logout() {
      // 删除Cookie信息，跳到首页，刷新页面
      sessionStorage.clear()
      // 跳转并刷新
      // if (this.$route.path != '/index-body') {
      //   // this.$router.replace('/index-body')
      //   console.log('准备跳转')
      //   this.$router.push({
      //     path: '/index-body',
      //     query: {
      //       load: '1'
      //     }
      //   })
      // }
      location.reload()
      // this.$router.push('/blank-page3')
    },
    // 点击个人中心
    clickUserCenter() {
      this.$router.push({
        path: '/blank-page2',
        query: {
          activeUserTabName: 'infoTab'
        }
      })
    },
    // 监听弹出登录框事件
    listenPopLoginDialogEvent() {
      this.event.$on('popLoginForm', () => {
        this.loginDialogVisible = true
      })
    },
    // 点击历史
    clickHistory() {
      // 判断是否登录 - 刚进来已经判断过 - 有isLogin字段
      if (!this.isLogin) {
        // 提示
        this.$message.warning('请先登录')
        // 未登录，弹出对话框
        this.loginDialogVisible = true
      } else {
        // 已登录 - 跳到历史页 - 间接跳转
        this.$router.push({
          path: '/blank-page2',
          query: {
            activeUserTabName: "historyTab"
          }
        })
      }
    },
    // 点击收藏
    clickCollect() {
      // 判断是否登录 - 刚进来已经判断过 - 有isLogin字段
      if (!this.isLogin) {
        // 提示
        this.$message.warning('请先登录')
        // 未登录，弹出对话框
        this.loginDialogVisible = true
      } else {
        // 已登录 - 跳到收藏页 - 间接跳转
        this.$router.push({
          path: '/blank-page2',
          query: {
            activeUserTabName: "collectTab"
          }
        })
      }
    },
    // 注册
    register() {
      this.$refs['registerForm'].validate(valid => {
        if (valid) {
          if (this.registerForm.password !== this.registerForm.confirmPassword) {
            this.$message.error('两次输入密码不一致')
            return
          }
          this.request.post(`UserRegister?nickname=${this.registerForm.nickname}&username=${this.registerForm.username}&password=${this.registerForm.password}`).then(res => {
            if (res.flag == 0) {
              this.$message.warning('该昵称用户已存在')
            } else if (res.flag == 1) {
              this.$message.warning('该账号用户已存在')
            } else if (res.flag == 2) {
              this.$message.success('注册成功')
              // 切换到登录标签页
              this.isLoginForm = true
              // 回显账号
              this.loginForm.username = this.registerForm.username
            }
          })
        }
      })
    },
    // 判断用户是否登录
    judgeUserLoginState() {
      this.request.get(`AuthenticateUserIsLogin`).then(res => {
        this.isLogin = res.isLogin
        console.log(this.isLogin)
        if (this.isLogin) {
          this.user = JSON.parse(Cookies.get('user'))
        }
      })
    },
    // 点击登录
    clickLogin() {
      this.loginDialogVisible = true
    },
    // 登录
    login() {
      // 校验表单
      this.$refs['loginForm'].validate(valid => {
        if (valid) {
          this.request.post(`UserLogin?username=${this.loginForm.username}&password=${this.loginForm.password}`).then(res => {
            // 登录成功
            if (res.flag == 0) {
              // 校验用户的状态 - 若被禁用则禁止登录
              // 直接根据返回的用户状态校验即可
              if (res.user.status == 1) {
                this.$message.error('该用户已被禁用，请联系管理员')
                return
              }

              // 保存Cookie
              // Cookies.set('user', JSON.stringify(res.user))
              // Cookies.set('token', res.token)
              // 提示信息
              this.$message.success('登录成功')
              // 保存用户信息
              this.user = res.user
              this.isLogin = true
              console.log(res.token)
              console.log(res.user)

              sessionStorage.setItem('token', res.token)
              sessionStorage.setItem('user', JSON.stringify(res.user))

              // 关闭对话框
              this.loginDialogVisible = false
              // 刷新页面
              location.reload()
            } else if (res.flag == 1) {
              // 登录失败
              this.$message.error('用户名或密码错误')
            }
          })
        }
      })
    },
    // 点击搜索
    search() {
      if (this.keyword) {
        // 跳到首页
        if (this.$route.path != '/index-body') {
          this.$router.push('/index-body')
        }
        // 触发事件
        this.event.$emit('searchImageWithKeyword', this.keyword)
      }
    },
    // 点击首页跳转
    clickIndex() {
      if (this.$route.path != '/index-body') {
        this.$router.push('/index-body')
      }
    }
  }
}
</script>

<style scoped src="./HeaderNav.css">

</style>