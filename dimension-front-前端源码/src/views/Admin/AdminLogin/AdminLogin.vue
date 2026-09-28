<template>
  <!--后台系统登录页-->
  <div class="body">
    <div style="margin-left: 35%; padding-top: 7%">
      <!--标题-->
      <div class="title" style="text-align: center; padding-right: 55%;">
        <span style="font-size: 25px; font-weight: bolder; ">次元馆后台管理系统</span>
      </div>
      <!--登录卡片-->
      <div class="login-card" style="margin-top: 3%">
        <el-card shadow="hover" class="box-card" style="width: 500px; height: 300px; opacity: 0.9">
          <!--登录表单-->
          <div class="login-form" style="margin-top: 8%">
            <el-form :model="loginForm" :rules="loginFormRules" ref="loginForm" label-width="100px" class="demo-ruleForm">
              <el-form-item label="账号" prop="username">
                <el-input style="width: 300px" v-model="loginForm.username"></el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input style="width: 300px" show-password v-model="loginForm.password"></el-input>
              </el-form-item>
              <el-form-item>
                <!--登录按钮-->
                <div class="login-button">
                  <el-button type="primary" style="width: 300px" @click="adminLogin">登录</el-button>
                </div>
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Login",
  data() {
    return {
      loginForm: {username: '', password: ''},
      loginFormRules: {
        username: [{required: true, message: '请输入账号', trigger: 'blur'}],
        password: [{required: true, message: '请输入密码', trigger: 'blur'}]
      }
    }
  },
  methods: {
    adminLogin() {
      this.$refs['loginForm'].validate(valid => {
        if (valid) {
          this.request.post(`AdminLogin?username=${this.loginForm.username}&password=${this.loginForm.password}`).then(res => {
            if (res.flag == 1) {
              this.$message.error('账号或密码错误')
              return
            }
            // 就不用保存管理员信息了 - 跳转到首页 - admin信息直接从cookie里面获取即可
            // 保存session
            sessionStorage.setItem('adminToken', res.adminToken)
            sessionStorage.setItem('admin', JSON.stringify(res.admin))

            this.$router.push('/image-management-image-management')
          })
        }
      })
    }
  }
}
</script>

<style scoped src="./AdminLogin.css">

</style>