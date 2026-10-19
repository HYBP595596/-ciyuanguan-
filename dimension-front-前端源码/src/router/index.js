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
        // 首页
      {
        path: '/index-body',
        name: 'index-body',
        component: () => import('@/views/Index/IndexBody/IndexBody')
      },
        // 图片详情页
      {
        path: '/image-detail',
        name: 'image-detail',
        component: () => import('@/views/Index/ImageDetail/ImageDetail')
      },
        // 用户个人中心页
      {
        path: '/user-center',
        name: 'user-center',
        component: () => import('@/views/Index/UserCenter/UserCenter')
      },
        // 间接路由跳转页
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
      }
    ],
  },
    // 管理员登录页
  {
    path: '/admin-login',
    name: 'admin-login',
    component: () => import('@/views/Admin/AdminLogin/AdminLogin')
  },
    // 后台系统首页
  {
    path: '/admin-index',
    name: 'admin-index',
    component: () => import('@/views/Admin/AdminIndex/AdminIndex'),
    redirect: '/image-management-image-management',
    children: [
        // 后台系统首页
      {
        path: '/image-management-image-management',
        name: 'image-management-image-management',
        component: () => import('@/views/Admin/AdminIndex/ImageManagement/ImageManagement/ImageManagement')
      },
        // 新增图片页
      {
        path: '/image-management-add-image',
        name: 'image-management-add-image',
        component: () => import('@/views/Admin/AdminIndex/ImageManagement/AddImage/AddImage')
      },
        // 评论管理页
      {
        path: '/comment-management',
        name: 'comment-management',
        component: () => import('@/views/Admin/AdminIndex/CommentManagement/CommentManagement')
      },
        // 用户管理页
      {
        path: '/user-management',
        name: 'user-management',
        component: () => import('@/views/Admin/AdminIndex/UserManagement/UserManagement')
      }
    ]
  }
]

const router = new VueRouter({
  routes
})

export default router
