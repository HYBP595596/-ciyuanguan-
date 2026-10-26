import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    // 首页
    path: '/',
    name: 'index',
    component: () => import('@/views/Index/Index'),
    redirect: '/index-body',
    children: [
      {
        path: '/index-body',
        name: 'index-body',
        component: () => import('@/views/Index/IndexBody/IndexBody')
      },
      {
        path: '/image-detail',
        name: 'image-detail',
        component: () => import('@/views/Index/ImageDetail/ImageDetail')
      },
      {
        path: '/user-center',
        name: 'user-center',
        component: () => import('@/views/Index/UserCenter/UserCenter')
      },
      {
        path: '/blank-page',
        name: 'blank-page',
        component: () => import('@/views/Index/BlankPage')
      },
      {
        path: '/blank-page2',
        name: 'blank-page2',
        component: () => import('@/views/Index/BlankPage2')
      },
      {
        path: '/blank-page3',
        name: 'blank-page3',
        component: () => import('@/views/Index/BlankPage3')
      },
      {
        path: '/publish',
        name: 'publish',
        component: () => import('@/views/Index/Publish/Publish')
      },
      {
        // 约稿中心：广场 / 我的约稿 / 画师中心
        path: '/commission',
        name: 'commission',
        component: () => import('@/views/Commission/CommissionCenter')
      },
      {
        // 私信
        path: '/messages',
        name: 'messages',
        component: () => import('@/views/Message/MessageCenter')
      },
      {
        // 全站公告
        path: '/notices',
        name: 'notices',
        component: () => import('@/views/Notice/NoticeList')
      },
      {
        // 消息中心：消息（私信/评论回复/@提及）+ 通知（公告/作品更新）
        path: '/inbox',
        name: 'inbox',
        component: () => import('@/views/Inbox/InboxPage')
      }
    ],
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
      // 🚨 新增：作品审核管理
      {
        path: '/audit-management',
        name: 'audit-management',
        component: () => import('@/views/Admin/AdminIndex/AuditManagement/AuditManagement')
      },
      // 🚨 新增：画师审核管理
      {
        path: '/artist-management',
        name: 'artist-management',
        component: () => import('@/views/Admin/AdminIndex/ArtistManagement/ArtistManagement')
      },
      // 分类管理：管理作品所属的类型
      {
        path: '/category-management',
        name: 'category-management',
        component: () => import('@/views/Admin/AdminIndex/CategoryManagement/CategoryManagement')
      },
      // 公告管理
      {
        path: '/notice-management',
        name: 'notice-management',
        component: () => import('@/views/Admin/AdminIndex/NoticeManagement/NoticeManagement')
      },
      // 管理员账号管理
      {
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

// ================= 路由守卫：权限拦截 =================
router.beforeEach((to, from, next) => {
  // 判断要去的地方是不是后台管理页面（路径包含 admin-index 或者审核页面）
  const isAdminPage = to.path.includes('/admin-index') ||
      to.path.includes('/audit-management') ||
      to.path.includes('/artist-management') ||
      to.path.includes('/image-management') ||
      to.path.includes('/user-management') ||
      to.path.includes('/comment-management') ||
      to.path.includes('/category-management') ||
      to.path.includes('/notice-management') ||
      to.path.includes('/admin-account-management');

  if (isAdminPage) {
    // 如果是去后台，必须携带 adminToken
    const adminToken = sessionStorage.getItem('adminToken');
    if (!adminToken) {
      // 没有登录凭证，强行踢回管理员登录页
      next('/admin-login');
    } else {
      // 凭证存在，放行
      next();
    }
  } else {
    // 如果去的是前台页面，直接放行
    next();
  }
});
// ======================================================

export default router