<template>
  <div class="nt-page">
    <div class="nt-head">
      <h2>公告管理</h2>
      <p class="nt-hint">发布全站公告，用户可在侧栏「公告」里看到。</p>
    </div>

    <!-- 发布 -->
    <div class="nt-form">
      <el-input v-model="form.title" placeholder="公告标题（100 字以内）" maxlength="100" show-word-limit style="margin-bottom:10px"/>
      <el-input
          type="textarea"
          :rows="4"
          v-model="form.content"
          placeholder="公告内容"
          maxlength="2000"
          show-word-limit/>
      <div class="nt-form-actions">
        <el-button type="primary" :loading="submitting" @click="publish">发布公告</el-button>
      </div>
    </div>

    <!-- 列表 -->
    <el-table :data="noticeList" stripe v-loading="loading" style="width:100%;margin-top:18px">
      <el-table-column prop="id" label="ID" width="70"></el-table-column>
      <el-table-column prop="title" label="标题" width="220" show-overflow-tooltip></el-table-column>
      <el-table-column prop="content" label="内容" min-width="320" show-overflow-tooltip></el-table-column>
      <el-table-column label="发布人" width="120">
        <template slot-scope="s">{{ s.row.adminNickname || ('管理员' + s.row.adminId) }}</template>
      </el-table-column>
      <el-table-column label="发布时间" width="180">
        <template slot-scope="s">{{ formatTime(s.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template slot-scope="s">
          <el-popconfirm title="确定删除该公告？" @confirm="remove(s.row)">
            <el-button slot="reference" size="mini" type="danger">删除</el-button>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <div class="nt-pagination">
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
  </div>
</template>

<script>
import { format1 } from '@/utils/time-format'

export default {
  name: "NoticeManagement",
  data() {
    return {
      form: { title: '', content: '' },
      submitting: false,
      loading: false,
      noticeList: [],
      pagination: { currentPage: 1, pageSize: 10, total: 0 }
    }
  },
  mounted() {
    this.loadList()
  },
  methods: {
    formatTime(t) { return format1(t) },
    adminId() {
      try {
        const a = JSON.parse(sessionStorage.getItem('admin') || '{}')
        return a.id || 1
      } catch (e) {
        return 1
      }
    },
    loadList() {
      this.loading = true
      const url = `Notice?action=list&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`
      this.request.get(url).then(res => {
        this.noticeList = (res && res.noticeList) || []
        this.pagination.total = (res && res.total) || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.loadList()
    },
    handleCurrentChange(page) {
      this.pagination.currentPage = page
      this.loadList()
    },
    publish() {
      if (!this.form.title.trim()) return this.$message.warning('请填写公告标题')
      if (!this.form.content.trim()) return this.$message.warning('请填写公告内容')
      this.submitting = true
      const url = `Notice?action=add&adminId=${this.adminId()}` +
          `&title=${encodeURIComponent(this.form.title)}` +
          `&content=${encodeURIComponent(this.form.content)}`
      this.request.post(url).then(res => {
        this.submitting = false
        if (res && res.flag === 0) {
          this.$message.success('公告已发布')
          this.form = { title: '', content: '' }
          this.pagination.currentPage = 1
          this.loadList()
        } else {
          this.$message.error((res && res.message) || '发布失败')
        }
      }).catch(() => { this.submitting = false })
    },
    remove(row) {
      this.request.post(`Notice?action=delete&id=${row.id}`).then(res => {
        if (res && res.flag === 0) {
          this.$message.success('已删除')
          this.loadList()
        } else {
          this.$message.error('删除失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.nt-page { padding: 20px 24px; }
.nt-head h2 { margin: 0 0 6px; color: var(--text-primary); font-size: 18px; }
.nt-hint { margin: 0 0 16px; font-size: 13px; color: var(--text-muted); }
.nt-form {
  max-width: 720px;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--bg-card);
}
.nt-form-actions { margin-top: 12px; text-align: right; }
.nt-pagination { margin-top: 16px; text-align: center; }
</style>
