<template>
  <div class="sidebar" :class="{ 'sidebar-collapsed': collapsed }">
    <!-- 菜单主体 -->
    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      :collapse="collapsed"
      background-color="#fff"
      text-color="#555"
      active-text-color="#409EFF"
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

      <el-menu-item index="bookmark" @click="clickCollect">
        <i class="el-icon-collection"></i>
        <span slot="title">珍藏冊</span>
      </el-menu-item>

      <div class="menu-divider" v-show="!collapsed"></div>

      <el-menu-item index="project">
        <i class="el-icon-files"></i>
        <span slot="title">用戶企劃</span>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: 'SidebarNav',
  props: {
    collapsed: {
      type: Boolean,
      default: false
    },
    event: Object,
    isLogin: Boolean
  },
  data() {
    return {
      activeMenu: 'home'
    }
  },
  methods: {
    handleSelect(key) {
      this.activeMenu = key
      switch (key) {
        case 'home':
          if (this.$route.path !== '/index-body') this.$router.push('/index-body')
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
        default:
          this.$message.info('功能开发中')
      }
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
    }
  }
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  height: 100vh;
  background: #fff;
  color: #333;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width 0.3s ease;
  flex-shrink: 0;
  border-right: 1px solid #e6e6e6;
  box-shadow: 1px 0 4px rgba(0,0,0,0.06);
}

/* 收起状态 */
.sidebar-collapsed {
  width: 0%;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}
.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  padding: 0 20px !important;
  font-size: 14px;
  color: #555 !important;
}
.sidebar-menu .el-menu-item i {
  margin-right: 12px;
  color: #666 !important;
}
.sidebar-menu .el-menu-item:hover {
  background-color: #f5f7fa !important;
  color: #409EFF !important;
}
.sidebar-menu .el-menu-item:hover i {
  color: #409EFF !important;
}
.sidebar-menu .el-menu-item.is-active {
  background-color: #ecf5ff !important;
  color: #409EFF !important;
  border-left: 3px solid #409EFF;
  padding-left: 17px !important;
}
.sidebar-menu .el-menu-item.is-active i {
  color: #409EFF !important;
}

/* el-menu 的 collapse 模式自动处理图标居中 */
.sidebar-collapsed .el-menu {
  width: 64px;
}

.menu-divider {
  height: 1px;
  background: #e6e6e6;
  margin: 8px 20px;
}

.sidebar::-webkit-scrollbar {
  width: 6px;
}
.sidebar::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}
.sidebar::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>