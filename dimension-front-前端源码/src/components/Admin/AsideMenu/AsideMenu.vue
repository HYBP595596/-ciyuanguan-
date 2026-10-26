<template>
  <div class="sidebar" :class="{ 'sidebar-collapsed': collapsed }">
    <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="collapsed"
        @select="handleSelect"
    >
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

      <el-menu-item index="following">
        <i class="el-icon-user-solid"></i>
        <span slot="title">追蹤用戶的最新作品</span>
      </el-menu-item>

      <el-menu-item index="discover">
        <i class="el-icon-compass"></i>
        <span slot="title">發現</span>
      </el-menu-item>

      <div class="menu-divider" v-show="!collapsed"></div>

      <el-menu-item index="ranking">
        <i class="el-icon-trophy"></i>
        <span slot="title">排行榜</span>
      </el-menu-item>

      <el-menu-item index="newest">
        <i class="el-icon-star-on"></i>
        <span slot="title">本站的最新作品</span>
      </el-menu-item>

      <el-menu-item index="contest">
        <i class="el-icon-medal"></i>
        <span slot="title">比賽</span>
      </el-menu-item>

      <el-menu-item index="commission">
        <i class="el-icon-chat-dot-round"></i>
        <span slot="title">約稿</span>
      </el-menu-item>

      <el-menu-item index="messages">
        <i class="el-icon-message"></i>
        <span slot="title">
          私信<em class="aside-unread" v-if="unreadCount">{{ unreadCount > 99 ? '99+' : unreadCount }}</em>
        </span>
      </el-menu-item>

      <el-menu-item index="bookmark" @click="clickCollect">
        <i class="el-icon-collection"></i>
        <span slot="title">珍藏冊</span>
      </el-menu-item>

      <div class="menu-divider" v-show="!collapsed"></div>



      <div class="menu-divider" v-show="!collapsed"></div>

      <el-menu-item index="project">
        <i class="el-icon-files"></i>
        <span slot="title">用戶企劃</span>
      </el-menu-item>
    </el-menu>

    <!-- 底部信息栏 -->
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
  </div>
</template>

<script>
export default {
  name: 'SidebarNav',
  props: {
    collapsed: { type: Boolean, default: false },
    event: Object,
    isLogin: Boolean
  },
  data() {
    return {
      activeMenu: 'home',
      isAdmin: false,
      unreadCount: 0
    }
  },
  watch: {
    $route() {
      this.loadUnread()
    }
  },
  mounted() {
    // 如果 sessionStorage 里有 adminToken，说明当前是管理员在后台
    this.isAdmin = !!sessionStorage.getItem('adminToken');
    this.loadUnread()
  },

  methods: {
    handleSelect(key) {
      this.activeMenu = key
      switch (key) {
        case 'home':
          this.$router.push({ path: '/index-body', query: {} })
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
        case 'bookmark':
          this.clickCollect()
          break
        case 'ranking':
          this.event && this.event.$emit('searchImageWithKeyword', '排行榜')
          break
        case 'newest':
          this.event && this.event.$emit('searchImageWithKeyword', '最新作品')
          break
        case 'commission':
          this.$router.push('/commission')
          break
        case 'messages':
          this.$router.push('/messages')
          break
        case 'following':
          this.$router.push({ path: '/user-center', query: { activeUserTabName: 'followingTab' } })
          break

        default:
          this.$message.info('功能开发中')
      }
    },
    loadUnread() {
      if (!this.isLogin) return
      let me = null
      try { me = JSON.parse(sessionStorage.getItem('user')) } catch (e) {}
      if (!me || !me.id) return
      this.request.get(`Message?action=unread&userId=${me.id}`).then(res => {
        this.unreadCount = (res && res.unread) || 0
      }).catch(() => {})
    },
    clickCollect() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.$emit('show-login')
      } else {
        this.$router.push({ path: '/blank-page2', query: { activeUserTabName: 'collectTab' } })
      }
    },
    clickHistory() {
      if (!this.isLogin) {
        this.$message.warning('请先登录')
        this.$emit('show-login')
      } else {
        this.$router.push({ path: '/blank-page2', query: { activeUserTabName: 'historyTab' } })
      }
    },
    onFooterClick(name) {
      this.$message.info(name + ' 功能开发中')
    }
  }
}
</script>

<style scoped>
/* ========== 侧栏容器 ========== */
.sidebar {
  width: 260px;
  height: 100%;
  background: var(--bg-sidebar);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  transition: background-color 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color);
  box-shadow: 1px 0 4px var(--shadow-color);
  scrollbar-width: thin;
  /* noinspection CssUnresolvedCustomProperty */
  scrollbar-color: var(--scrollbar-thumb) transparent;
}

.sidebar-collapsed {
  width: 0%;
}

/* ========== 菜单项：图标与文字严格对齐 ========== */
.sidebar-menu {
  border-right: none;
  flex: 0 0 auto;          /* 不拉满，消除菜单与底部之间的空白 */
  padding-top: 8px;
  background-color: transparent !important;
}

.sidebar-menu .el-menu-item {
  display: flex !important;
  align-items: center;
  height: 44px !important;
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
  color: #409EFF !important;
}
.sidebar-menu .el-menu-item:hover i {
  color: #409EFF !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: var(--bg-active) !important;
  color: #409EFF !important;
  border-left: 3px solid #409EFF;
  padding-left: 17px !important;
}
.sidebar-menu .el-menu-item.is-active i {
  color: #409EFF !important;
}

.sidebar-collapsed .el-menu {
  width: 64px;
}

.menu-divider {
  height: 1px;
  background: var(--border-color);
  margin: 8px 20px;
}

/* ========== 侧栏滚动条 ========== */
.sidebar::-webkit-scrollbar {
  width: 6px;
}
.sidebar::-webkit-scrollbar-track {
  background: transparent;
}
.sidebar::-webkit-scrollbar-thumb {
  /* noinspection CssUnresolvedCustomProperty */
  background: var(--scrollbar-thumb);
  border-radius: 3px;
}
.sidebar::-webkit-scrollbar-thumb:hover {
  /* noinspection CssUnresolvedCustomProperty */
  background: var(--scrollbar-thumb-hover);
}

/* ========== 底部信息栏：紧凑贴住菜单 ========== */
.sidebar-footer {
  margin-top: 24px;        /* 固定间距，不再用 auto 推到底 */
  padding: 14px 20px 18px;
  flex-shrink: 0;
  border-top: 1px solid var(--border-color);
}

.footer-line {
  display: flex;
  flex-wrap: wrap;
  gap: 0 14px;
  line-height: 1.9;
}

.footer-line a {
  font-size: 12.5px;
  color: var(--text-muted);
  text-decoration: none;
  white-space: nowrap;
  cursor: pointer;
  transition: color 0.2s;
}
.footer-line a:hover {
  color: #409EFF;
}

.footer-copyright {
  margin-top: 8px;
  font-size: 12.5px;
  color: var(--text-muted);
  line-height: 1.9;
}
/* 让 el-menu 内所有层级背景跟随侧栏 */
.sidebar ::v-deep .el-menu,
.sidebar ::v-deep .el-menu-item,
.sidebar ::v-deep .el-submenu__title,
.sidebar ::v-deep .el-menu-item-group__title,
.sidebar ::v-deep .el-menu--inline {
  background-color: transparent !important;
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
</style>