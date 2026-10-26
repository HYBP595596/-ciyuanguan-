import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    // 前台主布局：顶部导航 + 左侧栏 + 内容区
    path: '/',
    name: 'index',
    component: () => import('@/views/Site/Index'),
    redirect: '/index-body',
    children: [
      {
        path: '/index-body',
        name: 'index-body',
        component: () => import('@/views/Site/IndexBody/IndexBody')
      },
      {
        path: '/image-detail',
        name: 'image-detail',
        component: () => import('@/views/Site/ImageDetail/ImageDetail')
      },
      {
        path: '/user-center',
        name: 'user-center',
        component: () => import('@/views/Site/UserCenter/UserCenter')
      },
      {
        path: '/publish',
        name: 'publish',
        component: () => import('@/views/Site/Publish/Publish')
      },
      {
        // 约稿中心：广场 / 我的约稿 / 画师中心（含余额提现）
        path: '/commission',
        name: 'commission',
        component: () => import('@/views/Community/Commission/CommissionCenter')
      },
      {
        // 私信
        path: '/messages',
        name: 'messages',
        component: () => import('@/views/Community/Message/MessageCenter')
      },
      {
        // 全站公告
        path: '/notices',
        name: 'notices',
        component: () => import('@/views/Community/Notice/NoticeList')
      },
      {
        // 消息中心：消息（私信/评论回复/@提及）+ 通知（公告/作品更新）
        path: '/inbox',
        name: 'inbox',
        component: () => import('@/views/Community/Inbox/InboxPage')
      }
    ]
  },
  {
    path: '/admin-login',
    name: 'admin-login',
    component: () => import('@/views/Admin/AdminLogin/AdminLogin')
  },
  {
    path: '/admin-index',
    name: 'admin-index',
    component: () => import('@/views/Admin/AdminIndex/AdminIndex'),
    redirect: '/image-management-image-management',
    children: [
      {
        path: '/image-management-image-management',
        name: 'image-management-image-management',
        component: () => import('@/views/Admin/AdminIndex/ImageManagement/ImageManagement/ImageManagement')
      },
      {
        path: '/image-management-add-image',
        name: 'image-management-add-image',
        component: () => import('@/views/Admin/AdminIndex/ImageManagement/AddImage/AddImage')
      },
      {
        path: '/comment-management',
        name: 'comment-management',
        component: () => import('@/views/Admin/AdminIndex/CommentManagement/CommentManagement')
      },
      {
        path: '/user-management',
        name: 'user-management',
        component: () => import('@/views/Admin/AdminIndex/UserManagement/UserManagement')
      },
      {
        // 作品审核
        path: '/audit-management',
        name: 'audit-management',
        component: () => import('@/views/Admin/AdminIndex/AuditManagement/AuditManagement')
      },
      {
        // 画师审核
        path: '/artist-management',
        name: 'artist-management',
        component: () => import('@/views/Admin/AdminIndex/ArtistManagement/ArtistManagement')
      },
      {
        // 分类管理
        path: '/category-management',
        name: 'category-management',
        component: () => import('@/views/Admin/AdminIndex/CategoryManagement/CategoryManagement')
      },
      {
        // 约稿订单管理（强制退款 / 强制关单 / 提现审核）
        path: '/order-management',
        name: 'order-management',
        component: () => import('@/views/Admin/AdminIndex/OrderManagement/OrderManagement')
      },
      {
        // 公告管理
        path: '/notice-management',
        name: 'notice-management',
        component: () => import('@/views/Admin/AdminIndex/NoticeManagement/NoticeManagement')
      },
      {
        // 管理员账号管理
        path: '/admin-account-management',
        name: 'admin-account-management',
        component: () => import('@/views/Admin/AdminIndex/AdminAccountManagement/AdminAccountManagement')
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

// ================= 解决重复点击路由报错 =================
const originalPush = VueRouter.prototype.push
const originalReplace = VueRouter.prototype.replace

VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

VueRouter.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch(err => err)
}

// ================= 路由守卫：后台页面必须带管理员令牌 =================
// 约定：所有后台路由都在 /admin-index 下，或属于下列后台功能页
const ADMIN_ROUTE_PREFIXES = [
  '/admin-index',
  '/image-management',
  '/comment-management',
  '/user-management',
  '/audit-management',
  '/artist-management',
  '/category-management',
  '/order-management',
  '/notice-management',
  '/admin-account-management'
]

router.beforeEach((to, from, next) => {
  const isAdminPage = ADMIN_ROUTE_PREFIXES.some(prefix => to.path.indexOf(prefix) === 0)
  if (isAdminPage && !sessionStorage.getItem('adminToken')) {
    next('/admin-login')
    return
  }
  next()
})

export default router
