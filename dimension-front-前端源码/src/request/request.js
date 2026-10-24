import axios from 'axios'
import Cookies from 'js-cookie'
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
    // if (Cookies.get('token')) {
    //     config.headers.Authentication = sessionStorage.getItem('token')
    // }
    // if (Cookies.get('adminToken')) {
    //     config.headers.AdminAuthentication = sessionStorage.getItem('adminToken')
    // }
    return config
}, error => {
    return Promise.reject(error)
})

request.interceptors.response.use(response => {

    return response.data
}, error => {
    // if (error.response.status == 600) {
    //     ElementUI.Message.error('请先登录')
    //     router.push('/admin-login')
    // } else if (error.response.status == 500) {
    //     ElementUI.Message.error('服务器错误，请联系管理员')
    // }
    return Promise.resolve(error)
})

export default request