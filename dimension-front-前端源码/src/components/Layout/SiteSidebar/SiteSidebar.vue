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
        <span slot="title">追蹤用戶</span>
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
      <el-menu-item index="messages">
        <i class="el-icon-message"></i>
        <span slot="title">
          私信<em class="aside-unread" v-if="unreadCount">{{ unreadCount > 99 ? '99+' : unreadCount }}</em>
        </span>
      </el-menu-item>
    </el-menu>

    <!-- 底部：只留真实可用的入口 -->
    <div class="sidebar-footer" v-show="!collapsed">
      <a href="javascript:;" class="footer-link" @click="$router.push('/notices')">公告</a>
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
      unreadCount: 0
    }
  },
  watch: {
    $route() {
      this.loadUnread()
    }
  },
  mounted() {
    this.loadUnread()
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
          this.$router.push({ path: '/user-center', query: { activeUserTabName: 'followingTab' } })
          break
        case 'bookmark':
          this.clickCollect()
          break
        case 'commission':
          this.$router.push('/commission')
          break
        case 'messages':
          this.$router.push('/messages')
          break
        default:
          break
      }
    },
    loadUnread() {
      if (!this.isLogin) return
      let me = null
      try { me = JSON.parse(sessionStorage.getItem('user')) } catch (e) { me = null }
      if (!me || !me.id) return
      this.request.get(`Message?action=unread&userId=${me.id}`).then(res => {
        this.unreadCount = (res && res.unread) || 0
      }).catch(() => {})
    },
    clickCollect() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.$emit('show-login')
        return
      }
      this.$router.push({ path: '/user-center', query: { activeUserTabName: 'collectTab' } })
    }
  }
}
</script>

<style scoped>
/* ========== 侧栏容器（Pixiv 风格：窄、贴边、无多余留白） ========== */
.sidebar {
  width: 200px;
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
  padding-top: 6px;
  background-color: transparent !important;
}
.sidebar-menu .el-menu-item {
  display: flex !important;
  align-items: center;
  height: 40px !important;
  line-height: 1 !important;
  padding: 0 16px !important;
  font-size: 13.5px;
  color: var(--text-secondary) !important;
  background-color: transparent !important;
}
.sidebar-menu .el-menu-item i {
  margin-right: 10px;
  font-size: 15px;
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
  padding-left: 13px !important;
}
.sidebar-menu .el-menu-item.is-active i { color: #0096fa !important; }

.menu-divider {
  height: 1px;
  background: var(--border-color);
  margin: 6px 16px;
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

/* ========== 底部 ========== */
.sidebar-footer {
  margin-top: 12px;
  padding: 10px 16px 14px;
  flex-shrink: 0;
  border-top: 1px solid var(--border-color);
}
.footer-link {
  font-size: 12.5px;
  color: var(--text-muted);
  text-decoration: none;
  cursor: pointer;
  transition: color .2s;
}
.footer-link:hover { color: #0096fa; }
.footer-copyright {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-muted);
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
