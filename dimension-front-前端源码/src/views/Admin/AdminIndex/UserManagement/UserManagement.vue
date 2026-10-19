<template>
  <!--用户管理页-->
  <div class="body">
    <!--搜索框-->
    <div class="search-input">
      <!--昵称搜索框-->
      <div class="nickname-search">
        <el-input class="nickname" v-model="nickname" clearable placeholder="昵称"></el-input>
      </div>
      <!--账号搜索框-->
      <div class="username-search">
        <el-input class="username" v-model="username" clearable placeholder="账号"></el-input>
      </div>
      <!--搜索按钮-->
      <div class="search-button">
        <el-button class="search" type="primary" icon="el-icon-search" @click="searchUser">搜索</el-button>
      </div>
    </div>
    <!--用户表格-->
    <div class="table">
      <el-table
          :data="userTable"
          style="width: 100%" stripe>
        <el-table-column
            align="center"
            label="头像"
            width="180">
          <template slot-scope="scope">
              <img :src="scope.row.avatar" class="user-avatar"/>
          </template>
        </el-table-column>
        <el-table-column
            prop="id"
            label="ID"
            width="100">
        </el-table-column>
        <el-table-column
            prop="nickname"
            label="昵称"
            width="180">
        </el-table-column>
        <el-table-column
            prop="username"
            label="账号"
            width="180">
        </el-table-column>
        <el-table-column
            prop="password"
            label="密码"
            width="180">
        </el-table-column>
        <el-table-column
            prop="description"
            label="个人简介"
            width="250">
        </el-table-column>
        <el-table-column
            label="状态"
            width="50">
          <template slot-scope="scope">
            <el-tag type="success" v-show="scope.row.status == 0">正常</el-tag>
            <el-tag type="danger" v-show="scope.row.status == 1">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作">
          <template slot-scope="scope">
            <el-button
                v-show="scope.row.status == 1"
                type="success"
                size="mini"
                @click="resumeUser(scope.row)">恢复</el-button>
            <el-button
                v-show="scope.row.status == 0"
                size="mini"
                type="danger"
                @click="forbidUser(scope.row)">禁用</el-button>
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
          :page-sizes="[4, 5, 10, 20, 30]"
          :hide-on-single-page="pagination.onlyHasOnePage"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          layout="total, sizes, prev, pager, next"
          :total="pagination.total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: "UserManagement",
  data() {
    return {
      nickname: '',
      username: '',
      userTable: [],
      pagination: {
          currentPage: 1,
          pageSize: 4,
          total: 0,
          onlyHasOnePage: false
      },
      isInSearch: false,
      lastNickname: '',
      lastUsername: ''
    }
  },
  mounted() {
    if (!sessionStorage.getItem('adminToken')) {
      this.$message.error('管理员请先登录')
      this.$router.push('/admin-login')
    }

    // 无条件获取用户
    this.getUserListWithNoCondition()
  },
  methods: {
    handleSizeChange(newPageSize) {
      this.pagination.pageSize = newPageSize
      this.getUserListWithNoCondition()
    },
    // 禁用用户
    forbidUser(row) {
      this.request.post(`ForbidUser?userId=${row.id}`).then(res => {
        this.handleSearch()
      })
    },
    // 恢复用户
    resumeUser(row) {
      this.request.post(`ResumeUser?userId=${row.id}`).then(res => {
        // 重新查询
        this.handleSearch()
      })
    },
    // 搜素用户
    searchUser() {
      // 记录
      this.lastNickname = this.nickname
      this.lastUsername = this.username
      this.isInSearch = true
      // 统一从第一页开始搜索
      this.pagination.currentPage = 1
      // 根据nickname搜索
      if (this.lastNickname && !this.lastUsername) {
        this.searchUserByNickname()
      }
      // 根据username搜索
      if (this.username) {
        this.searchUserByUsername()
      }
      // 无条件搜索
      if (!this.lastNickname && !this.lastUsername) {
        // 记录
        this.isInSearch = false
        this.getUserListWithNoCondition()
      }
    },
    // 换页
    handleCurrentChange(newPage) {
      this.pagination.currentPage = newPage
      this.handleSearch()
    },
    handleSearch() {
      if (this.isInSearch) {
        // 搜索查询
        // 根据nickname搜索
        if (this.lastNickname && !this.lastUsername) {
          this.searchUserByNickname()
        }
        // 根据username搜索
        if (this.username) {
          this.searchUserByUsername()
        }
        // 无条件搜索
        if (!this.lastNickname && !this.lastUsername) {
          this.getUserListWithNoCondition()
        }
      } else {
        // 无条件查询
        this.getUserListWithNoCondition()
      }
    },
    // 根据用户账号搜索用户
    searchUserByUsername() {
      this.request.get(`GetUserTableWithUsername?username=${this.username}`).then(res => {
        this.userTable = res.userList
        this.pagination.total = res.total
      })
    },
    // 根据用户昵称搜索用户
    searchUserByNickname() {
      this.request.get(`GetUserTableWithNickname?nickname=${this.nickname}&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.userTable = res.userList
        this.pagination.total = res.total
      })
    },
    // 无条件获取用户
    getUserListWithNoCondition() {
      this.request.get(`GetUserTableWithNoCondition?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.userTable = res.userList
        this.pagination.total = res.total
      })
    }
  }
}
</script>

<style scoped src="./UserManagement.css">

</style>