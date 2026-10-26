import axios from 'axios'
import router from '@/router'
import ElementUI from 'element-ui'

const request = axios.create({
    baseURL: '/dimension_dev_01_war_exploded/',
    // baseURL: 'http://localhost:9000/dimension_dev_01_war_exploded/',
    headers: {
        'Content-Type': 'application/json; charset=utf-8'
    },
    withCredentials: false
})

request.interceptors.request.use(config => {
    // 有 token 就带上：管理员接口（如 /AdminAccount）会校验 AdminAuthentication
    const adminToken = sessionStorage.getItem('adminToken')
    if (adminToken) {
        config.headers.AdminAuthentication = adminToken
    }
    const userToken = sessionStorage.getItem('token')
    if (userToken) {
        config.headers.Authentication = userToken
    }
    return config
}, error => {
    return Promise.reject(error)
})

/**
 * 这次 401 到底该按"管理员掉线"还是"普通用户掉线"处理：
 *  - 后台页面（/admin 开头）里的请求 → 管理员
 *  - 只带了管理员 token、没有用户 token → 管理员
 *  - 其余（前台浏览、点赞、收藏、私信…）→ 普通用户
 */
function isAdminSide(error) {
    const cfg = (error && error.config) || {}
    const headers = cfg.headers || {}
    const path = (router.currentRoute && router.currentRoute.path) || ''
    if (path.indexOf('/admin') === 0) {
        return true
    }
    const hasAdminToken = !!headers.AdminAuthentication
    const hasUserToken = !!headers.Authentication
    return hasAdminToken && !hasUserToken
}

request.interceptors.response.use(response => {
    return response.data
}, error => {
    const resp = error && error.response
    const data = (resp && resp.data) || {}

    // 401：未登录 / token 过期 / 改过密码导致旧 token 失效
    if (resp && resp.status === 401) {
        if (isAdminSide(error)) {
            sessionStorage.removeItem('adminToken')
            sessionStorage.removeItem('admin')
            if (router.currentRoute.path !== '/admin-login') {
                ElementUI.Message.error(data.message || '管理员登录已过期，请重新登录')
                router.push('/admin-login')
            }
            return Promise.resolve({ flag: 1, needLogin: true, message: data.message || '管理员未登录或登录已过期' })
        }
        // 前台：清掉用户登录态，让头部弹登录框（不是跳后台登录页）
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('user')
        ElementUI.Message.error(data.message || '登录已过期，请重新登录')
        try {
            window.dispatchEvent(new CustomEvent('user-need-login'))
        } catch (e) {
            // 老浏览器没有 CustomEvent 构造器就只提示
        }
        return Promise.resolve({ flag: 1, needLogin: true, message: data.message || '请先登录' })
    }

    // 403：登录了，但请求里的 userId/buyerId/artistId 不是自己
    if (resp && resp.status === 403) {
        ElementUI.Message.error(data.message || '不能操作其他账号的数据')
        return Promise.resolve({ flag: 1, forbidden: true, message: data.message || '不能操作其他账号的数据' })
    }

    return Promise.resolve(error)
})

export default request
