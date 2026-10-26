import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import event from '@/utils/event-bus'
Vue.prototype.event = event

Vue.config.productionTip = false

import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
Vue.use(ElementUI);

import request from '@/request/request'
Vue.prototype.request = request

// 封面缺省占位：小说允许无封面(url=null)，全站统一用纯白替代
import { imageOrWhite } from '@/utils/image-fallback'
Vue.prototype.$img = imageOrWhite

import { initTheme } from '@/utils/theme'
initTheme()

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
