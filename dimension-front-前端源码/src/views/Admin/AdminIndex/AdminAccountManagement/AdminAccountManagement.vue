<template>
  <div class="am-page">
    <div class="am-head">
      <div>
        <h2>管理员管理</h2>
        <p class="am-hint">
          当前登录：<b>{{ currentAdmin.nickname || currentAdmin.username || '-' }}</b>
          （账号 {{ currentAdmin.username || '-' }}）。新建/改密会做加盐哈希，不会明文落库。
        </p>
      </div>
      <div class="am-head-actions">
        <el-button type="primary" icon="el-icon-plus" @click="openCreate">新建管理员</el-button>
        <el-button icon="el-icon-switch-button" @click="logout">退出登录</el-button>
      </div>
    </div>

    <el-table :data="adminList" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="70"></el-table-column>
      <el-table-column label="头像" width="80">
        <template slot-scope="s">
          <img :src="$img(s.row.avatar)" class="am-avatar"/>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="账号" width="180"></el-table-column>
      <el-table-column prop="nickname" label="昵称" min-width="160"></el-table-column>
      <el-table-column label="操作" width="290">
        <template slot-scope="s">
          <el-button size="mini" @click="openEdit(s.row)">编辑</el-button>
          <el-button size="mini" @click="openReset(s.row)">重置密码</el-button>
          <el-popconfirm
              v-if="s.row.id !== currentAdmin.id"
              title="确定删除该管理员？"
              @confirm="remove(s.row)">
            <el-button slot="reference" size="mini" type="danger">删除</el-button>
          </el-popconfirm>
          <el-tag v-else size="mini" type="info">当前账号</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <div class="am-pagination">
      <el-pagination
          background
          :page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20]"
          :current-page="pagination.currentPage"
          layout="total, sizes, prev, pager, next"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
      </el-pagination>
    </div>

    <!-- 新建 / 编辑 -->
    <el-dialog :title="dialogMode === 'create' ? '新建管理员' : '编辑管理员'" :visible.sync="dialogVisible" width="440px">
      <el-form label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="3~20 位，字母/数字/下划线" maxlength="20"/>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="留空则用账号" maxlength="200"/>
        </el-form-item>
        <el-form-item label="密码" v-if="dialogMode === 'create'">
          <el-input v-model="form.password" show-password placeholder="至少 6 位"/>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </span>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog title="重置密码" :visible.sync="resetVisible" width="420px">
      <p class="am-hint">账号：<b>{{ resetTarget.username }}</b></p>
      <el-input v-model="newPassword" show-password placeholder="新密码，至少 6 位"/>
      <span slot="footer">
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReset">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "AdminAccountManagement",
  data() {
    return {
      loading: false,
      saving: false,
      adminList: [],
      currentAdmin: {},
      pagination: { currentPage: 1, pageSize: 10, total: 0 },
      dialogVisible: false,
      dialogMode: 'create',
      form: { id: null, username: '', nickname: '', password: '' },
      resetVisible: false,
      resetTarget: {},
      newPassword: ''
    }
  },
  mounted() {
    try {
      this.currentAdmin = JSON.parse(sessionStorage.getItem('admin') || '{}')
    } catch (e) {
      this.currentAdmin = {}
    }
    this.loadList()
  },
  methods: {
    logout() {
      this.$confirm('确定要退出后台登录吗？', '提示', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 只清管理员凭证，保留前台用户登录态
        sessionStorage.removeItem('adminToken')
        sessionStorage.removeItem('admin')
        this.$router.push('/admin-login')
      }).catch(() => {
      })
    },
    loadList() {
      this.loading = true
      const offset = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const url = `AdminAccount?action=list&pageNum=${offset}&pageSize=${this.pagination.pageSize}`
      this.request.get(url).then(res => {
        if (res && res.needLogin) {
          this.$message.error(res.message || '管理员未登录')
          this.$router.push('/admin-login')
          return
        }
        this.adminList = (res && res.adminList) || []
        this.pagination.total = (res && res.total) || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.currentPage = 1
      this.loadList()
    },
    handleCurrentChange(page) {
      this.pagination.currentPage = page
      this.loadList()
    },
    openCreate() {
      this.dialogMode = 'create'
      this.form = { id: null, username: '', nickname: '', password: '' }
      this.dialogVisible = true
    },
    openEdit(row) {
      this.dialogMode = 'edit'
      this.form = { id: row.id, username: row.username, nickname: row.nickname, password: '' }
      this.dialogVisible = true
    },
    submitForm() {
      if (!this.form.username.trim()) return this.$message.warning('请填写账号')
      if (this.dialogMode === 'create' && (!this.form.password || this.form.password.length < 6)) {
        return this.$message.warning('密码至少 6 位')
      }
      this.saving = true
      const base = `username=${encodeURIComponent(this.form.username)}` +
          `&nickname=${encodeURIComponent(this.form.nickname || '')}`
      const url = this.dialogMode === 'create'
          ? `AdminAccount?action=create&${base}&password=${encodeURIComponent(this.form.password)}`
          : `AdminAccount?action=update&id=${this.form.id}&${base}`
      this.request.post(url).then(res => {
        this.saving = false
        if (res && res.flag === 0) {
          this.$message.success('已保存')
          this.dialogVisible = false
          this.loadList()
        } else {
          this.$message.error((res && res.message) || '保存失败')
        }
      }).catch(() => { this.saving = false })
    },
    openReset(row) {
      this.resetTarget = row
      this.newPassword = ''
      this.resetVisible = true
    },
    submitReset() {
      if (!this.newPassword || this.newPassword.length < 6) {
        return this.$message.warning('新密码至少 6 位')
      }
      this.saving = true
      const url = `AdminAccount?action=resetPassword&id=${this.resetTarget.id}` +
          `&password=${encodeURIComponent(this.newPassword)}`
      this.request.post(url).then(res => {
        this.saving = false
        if (res && res.flag === 0) {
          this.$message.success('密码已重置')
          this.resetVisible = false
        } else {
          this.$message.error((res && res.message) || '重置失败')
        }
      }).catch(() => { this.saving = false })
    },
    remove(row) {
      this.request.post(`AdminAccount?action=delete&id=${row.id}`).then(res => {
        if (res && res.flag === 0) {
          this.$message.success('已删除')
          this.loadList()
        } else {
          this.$message.error((res && res.message) || '删除失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.am-page { padding: 20px 24px; }
.am-head {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 16px; margin-bottom: 16px;
}
.am-head h2 { margin: 0 0 6px; font-size: 18px; color: var(--text-primary); }
.am-head-actions { display: flex; align-items: center; gap: 10px; flex: 0 0 auto; }
.am-hint { margin: 0; font-size: 13px; color: var(--text-muted); }
.am-hint b { color: var(--text-primary); }
.am-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; display: block; }
.am-pagination { margin-top: 16px; text-align: center; }
</style>
