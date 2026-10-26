<template>
  <div class="ib-body">
    <div class="ib-toolbar">
      <el-tag size="mini" type="info">未读 {{ unread }}</el-tag>
      <el-button size="mini" @click="markAllRead" :disabled="marking">标记全部已读</el-button>
      <el-button size="mini" icon="el-icon-refresh" @click="refresh">刷新</el-button>
    </div>

    <el-empty v-if="!loading && !itemList.length" :description="emptyText"></el-empty>

    <div class="ib-list" v-loading="loading">
      <div
          class="ib-item"
          v-for="(it, i) in itemList"
          :key="kind + '-' + i"
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

/**
 * 消息 / 通知 共用的列表组件。
 * 两者只差一个 kind：消息=私信提醒+评论回复+@提及，通知=公告+作品更新+活动。
 * 页面层各自独立（/inbox 与 /notifications），不再混在一个页面的 tab 里。
 */
export default {
  name: 'InboxListView',
  props: {
    kind: {
      type: String,
      default: 'message' // message | notice
    }
  },
  data() {
    return {
      user: {},
      loading: false,
      marking: false,
      itemList: [],
      page: 0,
      pageSize: 20,
      unread: 0
    }
  },
  computed: {
    emptyText() {
      return this.kind === 'message' ? '暂时没有新消息' : '暂时没有通知'
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
    this.refresh()
  },
  methods: {
    formatTime(t) { return format1(t) },
    typeText(t) {
      return {
        PRIVATE: '私信', REPLY: '评论回复', MENTION: '@提及',
        ANNOUNCE: '系统公告', WORK_UPDATE: '作品更新', ACTIVITY: '活动'
      }[t] || (this.kind === 'message' ? '消息' : '通知')
    },
    typeStyle(t) {
      return {
        PRIVATE: 'primary', REPLY: 'success', MENTION: 'warning',
        ANNOUNCE: 'danger', WORK_UPDATE: '', ACTIVITY: 'success'
      }[t] || 'info'
    },
    refresh() {
      this.loadSummary()
      this.page = 0
      this.itemList = []
      this.loadList()
    },
    loadSummary() {
      if (!this.user.id) return
      this.request.get(`Inbox?action=summary&userId=${this.user.id}`).then(res => {
        const msg = (res && res.messageUnread) || 0
        const notice = (res && res.noticeUnread) || 0
        this.unread = this.kind === 'message' ? msg : notice
        // 让顶部图标与左侧栏的角标同步刷新
        this.event && this.event.$emit('inbox-changed')
      })
    },
    loadList() {
      if (!this.user.id) return
      this.loading = true
      const url = `Inbox?action=${this.kind}&userId=${this.user.id}` +
          `&pageNum=${this.page * this.pageSize}&pageSize=${this.pageSize}`
      this.request.get(url).then(res => {
        this.itemList = (res && res.itemList) || []
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    loadMore() {
      const next = this.page + 1
      const url = `Inbox?action=${this.kind}&userId=${this.user.id}` +
          `&pageNum=${next * this.pageSize}&pageSize=${this.pageSize}`
      this.request.get(url).then(res => {
        const more = (res && res.itemList) || []
        if (!more.length) {
          return this.$message.info('没有更多了')
        }
        this.page = next
        this.itemList = this.itemList.concat(more)
      })
    },
    markAllRead() {
      this.marking = true
      const type = this.kind === 'message' ? 'ALL_MESSAGE' : 'ALL_NOTICE'
      this.request.post(`Inbox?action=read&userId=${this.user.id}&type=${type}`).then(() => {
        this.marking = false
        this.unread = 0
        this.$message.success('已标记为已读')
        this.event && this.event.$emit('inbox-changed')
      }).catch(() => { this.marking = false })
    },
    openItem(it) {
      if (it.type === 'PRIVATE') {
        this.$router.push({ path: '/messages', query: { peerId: it.actorId } })
      } else if (it.imageId) {
        this.$router.push({ path: '/image-detail', query: { imageId: it.imageId } })
      }
    }
  }
}
</script>

<style scoped>
.ib-toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.ib-list { display: flex; flex-direction: column; gap: 10px; min-height: 80px; }
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
