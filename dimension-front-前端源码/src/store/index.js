import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

import Cookies from 'js-cookie'

export default new Vuex.Store({
  state: {
    // user: Cookies.get('user') ? JSON.parse(Cookies.get('user')) : '',
    // token: Cookies.get('token')
  },
  getters: {
  },
  mutations: {
  },
  actions: {
  },
  modules: {
  }
})
