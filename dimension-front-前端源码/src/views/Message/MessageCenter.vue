<template>
  <div class="mc-page">
    <div class="mc-layout">
      <!-- 会话列表 -->
      <aside class="mc-sessions">
        <div class="mc-title">私信</div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="70"></el-empty>
        <div
            v-for="s in sessions"
            :key="s.peerId"
            class="mc-session"
            :class="{ active: peerId === s.peerId }"
            @click="openPeer(s.peerId, s.peerNickname)">
          <img :src="$img(s.peerAvatar)" alt=""/>
          <div class="mc-session-main">
            <div class="mc-session-name">{{ s.peerNickname || ('用户' + s.peerId) }}</div>
            <div class="mc-session-last">{{ s.content }}</div>
          </div>
          <span class="mc-badge" v-if="s.unreadCount">{{ s.unreadCount }}</span>
        </div>
      </aside>

      <!-- 聊天窗口 -->
      <section class="mc-chat">
        <template v-if="peerId">
          <div class="mc-chat-head">{{ peerName || ('用户' + peerId) }}</div>
          <div class="mc-chat-body" ref="body">
            <div
                v-for="m in messages"
                :key="m.id"
                class="mc-msg"
                :class="{ mine: m.senderId === user.id }">
              <div class="mc-bubble">{{ m.content }}</div>
            </div>
            <div class="mc-empty" v-if="!messages.length">还没有消息，打个招呼吧</div>
          </div>
          <div class="mc-send">
            <el-input
                v-model="draft"
                placeholder="输入消息，Enter 发送"
                maxlength="1000"
                @keyup.enter.native="send"></el-input>
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </template>
        <el-empty v-else description="在用户主页或作品详情点「私信」，即可发起对话"></el-empty>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: "MessageCenter",
  data() {
    return {
      user: {},
      sessions: [],
      messages: [],
      peerId: null,
      peerName: '',
      draft: '',
      sending: false
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
    this.loadSessions()
    const peer = this.$route.query.peerId
    if (peer) {
      this.openPeer(Number(peer), '')
    }
  },
  methods: {
    loadSessions() {
      this.request.get(`Message?action=sessions&userId=${this.user.id}`).then(res => {
        this.sessions = (res && res.sessionList) || []
      })
    },
    openPeer(peerId, name) {
      this.peerId = peerId
      this.peerName = name || this.peerName
      // 会话列表里没有这个对象时补一个占位，便于继续聊
      if (!this.sessions.some(s => s.peerId === peerId)) {
        this.sessions.unshift({ peerId, peerNickname: name, content: '', unreadCount: 0 })
      }
      this.loadConversation()
      // 读完后刷新会话列表的未读标记
      setTimeout(this.loadSessions, 500)
    },
    loadConversation() {
      if (!this.peerId) return
      this.request
          .get(`Message?action=conversation&userId=${this.user.id}&peerId=${this.peerId}`)
          .then(res => {
            this.messages = (res && res.messageList) || []
            this.scrollBottom()
          })
    },
    send() {
      const text = this.draft.trim()
      if (!text) return
      this.sending = true
      const url = `Message?action=send&senderId=${this.user.id}&receiverId=${this.peerId}` +
          `&content=${encodeURIComponent(text)}`
      this.request.post(url).then(res => {
        this.sending = false
        if (res && res.flag === 0) {
          this.draft = ''
          this.loadConversation()
          this.loadSessions()
        } else {
          this.$message.error((res && res.message) || '发送失败')
        }
      })
    },
    scrollBottom() {
      this.$nextTick(() => {
        const el = this.$refs.body
        if (el) el.scrollTop = el.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.mc-page { max-width: 1100px; margin: 0 auto; padding: 24px; min-height: 70vh; }
.mc-layout { display: flex; gap: 16px; height: 64vh; }
.mc-sessions {
  width: 260px; flex: 0 0 260px; overflow-y: auto;
  border: 1px solid var(--border-color); border-radius: 10px; padding: 8px;
}
.mc-title { font-weight: 600; padding: 8px 10px; color: var(--text-primary); }
.mc-session {
  display: flex; align-items: center; gap: 10px; padding: 10px;
  border-radius: 8px; cursor: pointer;
}
.mc-session:hover { background: var(--bg-hover); }
.mc-session.active { background: var(--bg-hover); }
.mc-session img { width: 38px; height: 38px; border-radius: 50%; object-fit: cover; flex: 0 0 auto; }
.mc-session-main { flex: 1; min-width: 0; }
.mc-session-name { font-size: 14px; color: var(--text-primary); }
.mc-session-last {
  font-size: 12px; color: var(--text-muted);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.mc-badge {
  min-width: 18px; height: 18px; line-height: 18px; padding: 0 5px;
  border-radius: 9px; background: #f56c6c; color: #fff;
  font-size: 11px; text-align: center;
}
.mc-chat {
  flex: 1; min-width: 0; display: flex; flex-direction: column;
  border: 1px solid var(--border-color); border-radius: 10px; overflow: hidden;
}
.mc-chat-head {
  padding: 12px 16px; border-bottom: 1px solid var(--border-color);
  font-weight: 600; color: var(--text-primary);
}
.mc-chat-body { flex: 1; overflow-y: auto; padding: 16px; }
.mc-msg { display: flex; margin-bottom: 10px; }
.mc-msg.mine { justify-content: flex-end; }
.mc-bubble {
  max-width: 70%; padding: 8px 12px; border-radius: 10px;
  background: var(--bg-hover); color: var(--text-primary);
  font-size: 14px; line-height: 1.6; white-space: pre-wrap; word-break: break-word;
}
.mc-msg.mine .mc-bubble { background: #0096fa; color: #fff; }
.mc-empty { color: var(--text-muted); text-align: center; padding-top: 40px; font-size: 13px; }
.mc-send { display: flex; gap: 10px; padding: 12px 16px; border-top: 1px solid var(--border-color); }
</style>
