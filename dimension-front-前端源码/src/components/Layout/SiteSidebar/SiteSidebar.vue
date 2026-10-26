<template>
  <aside class="sidebar" :class="{ 'sidebar-collapsed': collapsed }">
    <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="collapsed"
        @select="handleSelect">

      <!-- 浏览 -->
      <el-menu-item index="home">
        <i class="el-icon-house"></i>
        <span slot="title">首頁</span>
      </el-menu-item>
      <el-menu-item index="illust">
        <i class="el-icon-picture-outline"></i>
        <span slot="title">插畫</span>
      </el-menu-item>
      <el-menu-item index="comic">
        <i class="el-icon-notebook-2"></i>
        <span slot="title">漫畫</span>
      </el-menu-item>
      <el-menu-item index="novel">
        <i class="el-icon-document"></i>
        <span slot="title">小說</span>
      </el-menu-item>

      <div class="menu-divider" v-show="!collapsed"></div>

      <!-- 我的 -->
      <el-menu-item index="following">
        <i class="el-icon-user-solid"></i>
        <span slot="title">關注列表</span>
      </el-menu-item>
      <el-menu-item index="fans">
        <i class="el-icon-user"></i>
        <span slot="title">粉絲列表</span>
      </el-menu-item>
      <el-menu-item index="bookmark">
        <i class="el-icon-collection"></i>
        <span slot="title">珍藏冊</span>
      </el-menu-item>

      <div class="menu-divider" v-show="!collapsed"></div>

      <!-- 互动 -->
      <el-menu-item index="commission">
        <i class="el-icon-edit-outline"></i>
        <span slot="title">約稿</span>
      </el-menu-item>
      <el-menu-item index="inbox">
        <!-- 与顶部「消息」保持一致：信封 -->
        <i class="el-icon-message"></i>
        <span slot="title">
          消息<em class="aside-unread" v-if="msgUnread">{{ msgUnread > 99 ? '99+' : msgUnread }}</em>
        </span>
      </el-menu-item>
      <el-menu-item index="notifications">
        <!-- 与顶部「通知」保持一致：铃铛 -->
        <i class="el-icon-bell"></i>
        <span slot="title">
          通知<em class="aside-unread" v-if="noticeUnread">{{ noticeUnread > 99 ? '99+' : noticeUnread }}</em>
        </span>
      </el-menu-item>
      <el-menu-item index="messages">
        <!-- 私信用自己的图标：带横线的对话气泡 -->
        <i class="el-icon-chat-line-round"></i>
        <span slot="title">
          私信<em class="aside-unread" v-if="unreadCount">{{ unreadCount > 99 ? '99+' : unreadCount }}</em>
        </span>
      </el-menu-item>
    </el-menu>

    <!-- 底部信息栏（保留原有文字，公告可用，其余为占位说明） -->
    <div class="sidebar-footer" v-show="!collapsed">
      <div class="footer-line">
        <a href="javascript:;" @click="$router.push('/notices')">公告</a>
        <a href="javascript:;" @click="onFooterClick('幫助')">幫助</a>
        <a href="javascript:;" @click="onFooterClick('使用條款')">使用條款</a>
      </div>
      <div class="footer-line">
        <a href="javascript:;" @click="onFooterClick('隱私政策')">隱私政策</a>
      </div>
      <div class="footer-line">
        <a href="javascript:;" @click="onFooterClick('基於特定商業交易法的註釋')">基於特定商業交易法的註釋</a>
      </div>
      <div class="footer-line">
        <a href="javascript:;" @click="onFooterClick('營運公司')">營運公司</a>
        <a href="javascript:;" @click="onFooterClick('刊登廣告')">刊登廣告</a>
        <a href="javascript:;" @click="onFooterClick('徵才資訊')">徵才資訊</a>
      </div>
      <div class="footer-copyright">© 次元馆</div>
    </div>
  </aside>
</template>

<script>
export default {
  name: 'SiteSidebar',
  props: {
    collapsed: { type: Boolean, default: false },
    event: Object,
    isLogin: Boolean
  },
  data() {
    return {
      activeMenu: 'home',
      unreadCount: 0,   // 私信未读
      msgUnread: 0,     // 消息未读（私信提醒/评论回复/@提及）
      noticeUnread: 0   // 通知未读（公告/作品更新）
    }
  },
  watch: {
    $route() {
      this.loadUnread()
    }
  },
  mounted() {
    this.loadUnread()
    // 消息/通知页面标记已读后，角标要立刻跟着变
    if (this.event && this.event.$on) {
      this.event.$on('inbox-changed', () => this.loadUnread())
    }
  },
  methods: {
    handleSelect(key) {
      this.activeMenu = key
      switch (key) {
        case 'home':
          this.$router.push({ path: '/index-body' })
          break
        case 'illust':
          this.$router.push({ path: '/index-body', query: { categoryId: 1 } })
          break
        case 'comic':
          this.$router.push({ path: '/index-body', query: { categoryId: 2 } })
          break
        case 'novel':
          this.$router.push({ path: '/index-body', query: { categoryId: 3 } })
          break
        case 'following':
          this.goFollowTab('followingTab')
          break
        case 'fans':
          this.goFollowTab('fansTab')
          break
        case 'bookmark':
          this.clickCollect()
          break
        case 'commission':
          this.$router.push('/commission')
          break
        case 'inbox':
          this.goPrivatePage('/inbox')
          break
        case 'notifications':
          this.goPrivatePage('/notifications')
          break
        case 'messages':
          this.goPrivatePage('/messages')
          break
        default:
          break
      }
    },
    // 消息/通知/私信都是私有页面，未登录先弹登录框
    goPrivatePage(path) {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.$emit('show-login')
        return
      }
      this.$router.push(path)
    },
    loadUnread() {
      if (!this.isLogin) return
      let me = null
      try { me = JSON.parse(sessionStorage.getItem('user')) } catch (e) { me = null }
      if (!me || !me.id) return
      // 一次拿全三类未读：私信 / 消息 / 通知
      this.request.get(`Inbox?action=summary&userId=${me.id}`).then(res => {
        this.unreadCount = (res && res.privateUnread) || 0
        this.msgUnread = (res && res.messageUnread) || 0
        this.noticeUnread = (res && res.noticeUnread) || 0
      }).catch(() => {})
    },
    clickCollect() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.$emit('show-login')
        return
      }
      this.$router.push({ path: '/user-center', query: { activeUserTabName: 'collectTab' } })
    },
    // 关注/粉丝列表属于"我的"数据，未登录先弹登录框
    goFollowTab(tabName) {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.$emit('show-login')
        return
      }
      this.$router.push({ path: '/user-center', query: { activeUserTabName: tabName } })
    },
    onFooterClick(name) {
      this.$message.info(name + ' 功能开发中')
    }
  }
}
</script>

<style scoped>
/* ========== 侧栏容器（Pixiv 风格：窄栏、分组、底部信息补齐） ========== */
.sidebar {
  width: 220px;
  height: 100%;
  background: var(--bg-sidebar);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  transition: background-color .25s ease, border-color .25s ease, box-shadow .25s ease;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
  box-shadow: 1px 0 4px var(--shadow-color);
  scrollbar-width: thin;
  /* noinspection CssUnresolvedCustomProperty */
  scrollbar-color: var(--scrollbar-thumb) transparent;
}
.sidebar-collapsed { width: 0; }

/* ========== 菜单 ========== */
.sidebar-menu {
  border-right: none;
  flex: 0 0 auto;
  padding-top: 12px;
  background-color: transparent !important;
}
.sidebar-menu .el-menu-item {
  display: flex !important;
  align-items: center;
  height: 46px !important;
  line-height: 1 !important;
  padding: 0 20px !important;
  font-size: 14px;
  color: var(--text-secondary) !important;
  background-color: transparent !important;
}
.sidebar-menu .el-menu-item i {
  margin-right: 12px;
  font-size: 16px;
  line-height: 1;
  color: var(--text-secondary) !important;
  display: inline-flex;
  align-items: center;
}
.sidebar-menu .el-menu-item span {
  display: inline-flex;
  align-items: center;
  line-height: 1;
}
.sidebar-menu .el-menu-item:hover {
  background-color: var(--bg-hover) !important;
  color: #0096fa !important;
}
.sidebar-menu .el-menu-item:hover i { color: #0096fa !important; }
.sidebar-menu .el-menu-item.is-active {
  background-color: var(--bg-active) !important;
  color: #0096fa !important;
  border-left: 3px solid #0096fa;
  padding-left: 17px !important;
}
.sidebar-menu .el-menu-item.is-active i { color: #0096fa !important; }

.menu-divider {
  height: 1px;
  background: var(--border-color);
  margin: 10px 20px;
}

/* ========== 侧栏滚动条 ========== */
.sidebar::-webkit-scrollbar { width: 6px; }
.sidebar::-webkit-scrollbar-track { background: transparent; }
.sidebar::-webkit-scrollbar-thumb {
  /* noinspection CssUnresolvedCustomProperty */
  background: var(--scrollbar-thumb);
  border-radius: 3px;
}
.sidebar::-webkit-scrollbar-thumb:hover {
  /* noinspection CssUnresolvedCustomProperty */
  background: var(--scrollbar-thumb-hover);
}

/* ========== 底部信息栏（文字补回来，页面下面不再空） ========== */
.sidebar-footer {
  /* 贴到侧栏底部：菜单在上、信息栏在下，中间不会留出一大块突兀的空白 */
  margin-top: auto;
  padding: 14px 20px 18px;
  flex-shrink: 0;
  border-top: 1px solid var(--border-color);
}
.footer-line {
  display: flex;
  flex-wrap: wrap;
  gap: 0 12px;
  line-height: 1.9;
}
.footer-line a {
  font-size: 12.5px;
  color: var(--text-muted);
  text-decoration: none;
  white-space: nowrap;
  cursor: pointer;
  transition: color .2s;
}
.footer-line a:hover { color: #0096fa; }
.footer-copyright {
  margin-top: 8px;
  font-size: 12.5px;
  color: var(--text-muted);
  line-height: 1.9;
}

/* 私信未读角标 */
.aside-unread {
  display: inline-block;
  min-width: 16px;
  height: 16px;
  line-height: 16px;
  padding: 0 4px;
  margin-left: 6px;
  border-radius: 8px;
  background: #f56c6c;
  color: #fff;
  font-size: 11px;
  font-style: normal;
  text-align: center;
  vertical-align: middle;
}

/* 让 el-menu 内所有层级背景跟随侧栏 */
.sidebar ::v-deep .el-menu,
.sidebar ::v-deep .el-menu-item,
.sidebar ::v-deep .el-submenu__title,
.sidebar ::v-deep .el-menu-item-group__title,
.sidebar ::v-deep .el-menu--inline {
  background-color: transparent !important;
}
</style>
