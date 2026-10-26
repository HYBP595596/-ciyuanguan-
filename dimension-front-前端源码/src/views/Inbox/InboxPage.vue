<template>
  <div class="ib-page">
    <div class="ib-head">
      <h2>{{ tab === 'message' ? '消息' : '通知' }}</h2>
      <p class="ib-hint">
        <template v-if="tab === 'message'">私信、评论回复、@提及</template>
        <template v-else>系统公告、关注画师的作品更新、活动推送</template>
      </p>
    </div>

    <el-tabs v-model="tab" @tab-click="onTabChange">
      <el-tab-pane label="消息" name="message"></el-tab-pane>
      <el-tab-pane label="通知" name="notice"></el-tab-pane>
    </el-tabs>

    <div class="ib-toolbar">
      <el-tag size="mini" type="info">未读 {{ tab === 'message' ? msgUnread : noticeUnread }}</el-tag>
      <el-button size="mini" @click="markAllRead" :disabled="marking">标记本类全部已读</el-button>
    </div>

    <el-empty v-if="!loading && !itemList.length" :description="tab === 'message' ? '暂时没有新消息' : '暂时没有通知'"></el-empty>

    <div class="ib-list" v-loading="loading">
      <div
          class="ib-item"
          v-for="(it, i) in itemList"
          :key="i"
          @click="openItem(it)">
        <div class="ib-item-main">
          <div class="ib-item-head">
            <el-tag size="mini" :type="typeStyle(it.type)">{{ typeText(it.type) }}</el-tag>
            <span class="ib-item-actor">{{ it.actorName || '系统' }}</span>
            <span class="ib-item-time">{{ formatTime(it.createTime) }}</span>
          </div>
          <div class="ib-item-title" v-if="it.title">{{ it.title }}</div>
          <div class="ib-item-content">{{ it.content }}</div>
        </div>
      </div>
    </div>

    <div class="ib-pagination" v-if="itemList.length >= pageSize">
      <el-button size="small" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script>
import { format1 } from '@/utils/time-format'

export default {
  name: "InboxPage",
  data() {
    return {
      user: {},
      tab: 'message',
      loading: false,
      marking: false,
      itemList: [],
      page: 0,
      pageSize: 20,
      msgUnread: 0,
      noticeUnread: 0
    }
  },
  mounted() {
    const u = sessionStorage.getItem('user')
    if (!u) {
      this.$message.warning('请先登录')
      this.$router.replace('/index-body')
      return
    }
    this.user = JSON.parse(u)
    this.tab = this.$route.query.tab === 'notice' ? 'notice' : 'message'
    this.refresh()
  },
  methods: {
    formatTime(t) { return format1(t) },
    typeText(t) {
      return {
        PRIVATE: '私信', REPLY: '评论回复', MENTION: '@提及',
        ANNOUNCE: '系统公告', WORK_UPDATE: '作品更新'
      }[t] || '消息'
    },
    typeStyle(t) {
      return {
        PRIVATE: 'primary', REPLY: 'success', MENTION: 'warning',
        ANNOUNCE: 'danger', WORK_UPDATE: ''
      }[t] || 'info'
    },
    onTabChange() {
      this.itemList = []
      this.page = 0
      this.loadList()
      this.$router.replace({path: '/inbox', query: {tab: this.tab}})
    },
    refresh() {
      this.loadSummary()
      this.page = 0
      this.itemList = []
      this.loadList()
    },
    loadSummary() {
      this.request.get(`Inbox?action=summary&userId=${this.user.id}`).then(res => {
        this.msgUnread = (res && res.messageUnread) || 0
        this.noticeUnread = (res && res.noticeUnread) || 0
        this.$emit('changed')
        this.event && this.event.$emit('inbox-changed')
      })
    },
    loadList() {
      this.loading = true
      const action = this.tab === 'message' ? 'message' : 'notice'
      const url = `Inbox?action=${action}&userId=${this.user.id}` +
          `&pageNum=${this.page * this.pageSize}&pageSize=${this.pageSize}`
      this.request.get(url).then(res => {
        this.itemList = (res && res.itemList) || []
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    loadMore() {
      this.page += 1
      const action = this.tab === 'message' ? 'message' : 'notice'
      const url = `Inbox?action=${action}&userId=${this.user.id}` +
          `&pageNum=${this.page * this.pageSize}&pageSize=${this.pageSize}`
      this.request.get(url).then(res => {
        const more = (res && res.itemList) || []
        if (!more.length) {
          this.page -= 1
          return this.$message.info('没有更多了')
        }
        this.itemList = this.itemList.concat(more)
      })
    },
    markAllRead() {
      this.marking = true
      const type = this.tab === 'message' ? 'ALL_MESSAGE' : 'ALL_NOTICE'
      this.request.post(`Inbox?action=read&userId=${this.user.id}&type=${type}`).then(() => {
        this.marking = false
        this.$message.success('已标记为已读')
        this.loadSummary()
      }).catch(() => { this.marking = false })
    },
    openItem(it) {
      if (it.type === 'PRIVATE') {
        this.$router.push({path: '/messages', query: {peerId: it.actorId}})
      } else if (it.imageId) {
        this.$router.push({path: '/image-detail', query: {imageId: it.imageId}})
      }
    }
  }
}
</script>

<style scoped>
.ib-page { max-width: 900px; margin: 0 auto; padding: 24px; min-height: 60vh; }
.ib-head h2 { margin: 0 0 6px; font-size: 20px; color: var(--text-primary); }
.ib-hint { margin: 0 0 14px; font-size: 13px; color: var(--text-muted); }
.ib-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.ib-list { display: flex; flex-direction: column; gap: 10px; }
.ib-item {
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--bg-card);
  cursor: pointer;
  transition: border-color .2s;
}
.ib-item:hover { border-color: #0096fa; }
.ib-item-head { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.ib-item-actor { font-size: 13px; color: var(--text-primary); font-weight: 600; }
.ib-item-time { margin-left: auto; font-size: 12px; color: var(--text-muted); }
.ib-item-title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 4px; }
.ib-item-content {
  font-size: 13px; line-height: 1.7; color: var(--text-secondary);
  white-space: pre-wrap; word-break: break-word;
}
.ib-pagination { margin-top: 18px; text-align: center; }
</style>
