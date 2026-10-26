<template>
  <div class="nl-page">
    <h2 class="nl-title">全站公告</h2>

    <el-empty v-if="!loading && !noticeList.length" description="暂时没有公告"></el-empty>

    <div class="nl-list" v-loading="loading">
      <div class="nl-item" v-for="n in noticeList" :key="n.id">
        <div class="nl-item-head">
          <span class="nl-item-title">{{ n.title }}</span>
          <span class="nl-item-time">{{ formatTime(n.createTime) }}</span>
        </div>
        <div class="nl-item-content">{{ n.content }}</div>
      </div>
    </div>

    <div class="nl-pagination" v-if="pagination.total > pagination.pageSize">
      <el-pagination
          background
          :page-size="pagination.pageSize"
          :current-page="pagination.currentPage"
          layout="prev, pager, next"
          :total="pagination.total"
          @current-change="handleCurrentChange">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { format1 } from '@/utils/time-format'

export default {
  name: "NoticeList",
  data() {
    return {
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
    loadList() {
      this.loading = true
      const url = `Notice?action=list&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`
      this.request.get(url).then(res => {
        this.noticeList = (res && res.noticeList) || []
        this.pagination.total = (res && res.total) || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleCurrentChange(page) {
      this.pagination.currentPage = page
      this.loadList()
    }
  }
}
</script>

<style scoped>
.nl-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px;
  min-height: 60vh;
}
.nl-title {
  margin: 0 0 18px;
  font-size: 20px;
  color: var(--text-primary);
}
.nl-list { display: flex; flex-direction: column; gap: 14px; }
.nl-item {
  padding: 16px 18px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--bg-card);
}
.nl-item-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.nl-item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.nl-item-time { font-size: 12px; color: var(--text-muted); flex: 0 0 auto; }
.nl-item-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary);
  white-space: pre-wrap;
  word-break: break-word;
}
.nl-pagination { margin-top: 20px; text-align: center; }
</style>
