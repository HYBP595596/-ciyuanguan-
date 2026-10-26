<template>
  <div class="cc-page">
    <el-tabs v-model="activeTab" class="cc-tabs">

      <!-- ============ 约稿广场 ============ -->
      <el-tab-pane label="约稿广场" name="square">
        <el-empty v-if="!artistList.length" description="暂时没有接稿中的画师"></el-empty>
        <div class="cc-square" v-else>
          <div class="cc-artists">
            <div
                class="cc-artist"
                v-for="a in artistList"
                :key="a.id"
                :class="{ active: selectedArtist && selectedArtist.id === a.id }"
                @click="selectArtist(a)">
              <img :src="$img(a.avatar)" alt=""/>
              <div class="cc-artist-name">{{ a.nickname || ('用户' + a.id) }}</div>
            </div>
          </div>

          <div class="cc-plans" v-if="selectedArtist">
            <h3>{{ selectedArtist.nickname || ('用户' + selectedArtist.id) }} 的接稿方案</h3>
            <div class="cc-plan" v-for="p in artistPlans" :key="p.id">
              <div class="cc-plan-main">
                <div class="cc-plan-title">{{ p.title }}</div>
                <div class="cc-plan-meta">￥{{ p.price }} · {{ p.deliveryDays }} 天交稿</div>
              </div>
              <el-button type="primary" size="small" @click="openOrderDialog(p)">约稿</el-button>
            </div>
            <el-empty v-if="!artistPlans.length" description="该画师暂无上架方案"></el-empty>
          </div>
          <div class="cc-plans cc-hint" v-else>
            选择左侧画师查看其接稿方案
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 我的约稿 ============ -->
      <el-tab-pane label="我的约稿" name="myOrders">
        <el-table :data="myOrders" stripe style="width:100%">
          <el-table-column prop="orderNo" label="订单号" width="190"></el-table-column>
          <el-table-column prop="planTitle" label="方案" width="160"></el-table-column>
          <el-table-column label="画师" width="120">
            <template slot-scope="s">{{ s.row.artistNickname || ('用户' + s.row.artistId) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="100"></el-table-column>
          <el-table-column label="状态" width="110">
            <template slot-scope="s">
              <el-tag size="mini" :type="statusType(s.row.status)">{{ statusText(s.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="220">
            <template slot-scope="s">
              <el-button v-if="s.row.status === 0" size="mini" type="primary" @click="pay(s.row)">支付</el-button>
              <el-button v-if="s.row.status === 1" size="mini" type="danger" @click="refund(s.row)">退款</el-button>
              <el-button v-if="s.row.status === 2" size="mini" type="success" @click="confirm(s.row)">确认收货</el-button>
              <a v-if="s.row.deliveryFinal" :href="s.row.deliveryFinal" target="_blank" class="cc-link">查看交付</a>
              <span v-if="s.row.status === 1" class="cc-muted">等待画师交稿</span>
              <el-button size="mini" @click="openChat(s.row.artistId)">私信画师</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!myOrders.length" description="还没有约稿记录"></el-empty>
      </el-tab-pane>

      <!-- ============ 画师中心 ============ -->
      <el-tab-pane label="画师中心" name="artist">
        <!-- 未认证 -->
        <div v-if="artistStatus !== 2" class="cc-apply">
          <h3>申请成为画师</h3>
          <p class="cc-muted">
            当前状态：<b>{{ artistStatusText }}</b>
            <span v-if="artistStatus === 1">（等待管理员审核）</span>
            <span v-if="artistStatus === 3">（已被驳回，可修改后重新提交）</span>
          </p>
          <el-input
              type="textarea"
              :rows="4"
              v-model="applyDesc"
              placeholder="介绍一下你的画风、擅长题材、过往作品等"
              style="max-width:560px"/>
          <div style="margin-top:12px">
            <el-button type="primary" @click="applyArtist" :loading="applying">
              {{ artistStatus === 1 ? '重新提交申请' : '提交申请' }}
            </el-button>
          </div>
        </div>

        <!-- 已认证 -->
        <div v-else>
          <div class="cc-switch-row">
            <span>接稿开关</span>
            <el-switch v-model="acceptCommission" :active-value="1" :inactive-value="0" @change="toggleAccept"></el-switch>
            <span class="cc-muted">{{ acceptCommission ? '接稿中，会出现在约稿广场' : '已暂停接稿' }}</span>
          </div>

          <h3>我的方案</h3>
          <div class="cc-add-plan">
            <el-input v-model="planForm.title" placeholder="方案标题，如 Q版头像" style="width:180px"/>
            <el-select v-model="planForm.contentType" placeholder="类型" style="width:110px">
              <el-option :value="1" label="插画"></el-option>
              <el-option :value="2" label="漫画"></el-option>
              <el-option :value="3" label="小说"></el-option>
            </el-select>
            <el-input v-model="planForm.price" placeholder="价格" style="width:100px"/>
            <el-input v-model="planForm.deliveryDays" placeholder="交稿天数" style="width:100px"/>
            <el-button type="primary" size="small" @click="addPlan">新增方案</el-button>
          </div>
          <el-table :data="myPlans" stripe style="width:100%;margin-top:12px">
            <el-table-column prop="title" label="方案" min-width="160"></el-table-column>
            <el-table-column label="类型" width="90">
              <template slot-scope="s">{{ typeText(s.row.contentType) }}</template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100"></el-table-column>
            <el-table-column prop="deliveryDays" label="交稿天数" width="100"></el-table-column>
            <el-table-column label="状态" width="110">
              <template slot-scope="s">
                <el-tag size="mini" :type="s.row.status === 1 ? 'success' : 'info'">
                  {{ s.row.status === 1 ? '上架中' : '已下架' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140">
              <template slot-scope="s">
                <el-button size="mini" @click="setPlanStatus(s.row, s.row.status === 1 ? 2 : 1)">
                  {{ s.row.status === 1 ? '下架' : '上架' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <h3>收到的约稿</h3>
          <el-table :data="artistOrders" stripe style="width:100%">
            <el-table-column prop="orderNo" label="订单号" width="190"></el-table-column>
            <el-table-column prop="planTitle" label="方案" width="150"></el-table-column>
            <el-table-column label="买家" width="110">
              <template slot-scope="s">{{ s.row.buyerNickname || ('用户' + s.row.buyerId) }}</template>
            </el-table-column>
            <el-table-column prop="requirement" label="需求" min-width="160" show-overflow-tooltip></el-table-column>
            <el-table-column prop="amount" label="金额" width="90"></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="s">
                <el-tag size="mini" :type="statusType(s.row.status)">{{ statusText(s.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="s">
                <el-upload
                    v-if="s.row.status === 1"
                    style="display:inline-block;margin-right:8px"
                    action="/dimension_dev_01_war_exploded/UploadDeliveryFile"
                    :headers="uploadHeaders"
                    :show-file-list="false"
                    :on-success="res => handleDeliverSuccess(res, s.row)"
                    :on-error="handleDeliverError"
                    accept=".zip,.rar,.7z,.tar,.gz,.pdf,.txt,.jpg,.jpeg,.png,.webp,.gif">
                  <el-button size="mini" type="primary">上传交付包</el-button>
                </el-upload>
                <el-button size="mini" @click="openChat(s.row.buyerId)">私信买家</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 下单弹窗 -->
    <el-dialog title="填写约稿需求" :visible.sync="orderDialogVisible" width="480px">
      <p class="cc-muted">方案：{{ orderPlan && orderPlan.title }} · ￥{{ orderPlan && orderPlan.price }}</p>
      <el-input type="textarea" :rows="5" v-model="requirement" placeholder="描述你想要的画面、尺寸、用途等"></el-input>
      <span slot="footer">
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ordering" @click="createOrder">提交并去支付</el-button>
      </span>
    </el-dialog>

    <!-- 模拟三方收银台 -->
    <el-dialog title="模拟收银台" :visible.sync="cashierVisible" width="420px">
      <div class="cc-cashier">
        <p class="cc-cashier-amount">￥{{ cashierAmount }}</p>
        <p class="cc-muted">支付单号：{{ cashierPaymentNo }}</p>
        <p class="cc-muted">渠道：{{ cashierChannel }}（模拟，不产生真实扣款）</p>
        <p class="cc-muted">点击「确认支付」后，模拟渠道会带上签名回调平台</p>
      </div>
      <span slot="footer">
        <el-button @click="cashierVisible = false">取消</el-button>
        <el-button type="primary" :loading="paying" @click="confirmPay">确认支付</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "CommissionCenter",
  data() {
    return {
      activeTab: 'square',
      user: {},
      // 广场
      artistList: [],
      selectedArtist: null,
      artistPlans: [],
      orderDialogVisible: false,
      orderPlan: null,
      requirement: '',
      ordering: false,
      // 我的约稿
      myOrders: [],
      // 画师中心
      artistStatus: 0,
      acceptCommission: 0,
      applyDesc: '',
      applying: false,
      planForm: { title: '', contentType: 1, price: '', deliveryDays: '' },
      // 模拟收银台
      cashierVisible: false,
      cashierPaymentNo: '',
      cashierAmount: '',
      cashierChannel: 'MOCK',
      paying: false,
      myPlans: [],
      artistOrders: []
    }
  },
  computed: {
    artistStatusText() {
      return { 0: '未申请', 1: '审核中', 2: '已认证画师', 3: '已驳回' }[this.artistStatus] || '未知'
    },
    // el-upload 直传不带 axios 拦截器的头，这里手动补登录凭证
    uploadHeaders() {
      const headers = {}
      const token = sessionStorage.getItem('token')
      if (token) headers['Authentication'] = token
      const adminToken = sessionStorage.getItem('adminToken')
      if (adminToken) headers['AdminAuthentication'] = adminToken
      return headers
    }
  },
  mounted() {
    const u = sessionStorage.getItem('user')
    if (!u) {
      this.$message.warning('请先登录')
      this.$router.replace('/index-body')
      return
    }
    this.user = JSON.parse(u)
    this.loadSquare()
    this.loadMyOrders()
    this.loadArtistPanel()
  },
  methods: {
    statusText(s) {
      return { 0: '待支付', 1: '绘制中', 2: '待验收', 3: '已完成' }[s] || '未知'
    },
    statusType(s) {
      return { 0: 'warning', 1: '', 2: 'primary', 3: 'success' }[s] || 'info'
    },

    // ---------- 广场 ----------
    loadSquare() {
      this.request.get('Commission?action=artists&currentPage=1&pageSize=50').then(res => {
        this.artistList = (res && res.artistList) || []
      })
    },
    selectArtist(a) {
      this.selectedArtist = a
      this.request.get(`Commission?action=artistPlans&artistId=${a.id}`).then(res => {
        this.artistPlans = (res && res.planList) || []
      })
    },
    openOrderDialog(plan) {
      this.orderPlan = plan
      this.requirement = ''
      this.orderDialogVisible = true
    },
    createOrder() {
      if (!this.requirement.trim()) return this.$message.warning('请填写需求')
      this.ordering = true
      const url = `Commission?action=create&buyerId=${this.user.id}&planId=${this.orderPlan.id}` +
          `&requirement=${encodeURIComponent(this.requirement)}`
      this.request.post(url).then(res => {
        this.ordering = false
        if (res && res.flag === 0) {
          this.orderDialogVisible = false
          this.$message.success('下单成功，请完成支付')
          this.activeTab = 'myOrders'
          this.loadMyOrders()
        } else {
          this.$message.error((res && res.message) || '下单失败')
        }
      })
    },

    // ---------- 我的约稿 ----------
    loadMyOrders() {
      this.request.get(`Commission?action=myOrders&buyerId=${this.user.id}`).then(res => {
        this.myOrders = (res && res.orderList) || []
      })
    },
    typeText(t) {
      return { 1: '插画', 2: '漫画', 3: '小说' }[t] || '插画'
    },
    // 第一步：创建平台支付单，打开模拟收银台
    pay(order) {
      const url = `Payment?action=create&orderNo=${order.orderNo}&buyerId=${this.user.id}&channel=MOCK`
      this.request.post(url).then(res => {
        if (!res || res.flag !== 0) {
          return this.$message.error((res && res.message) || '创建支付单失败')
        }
        this.cashierPaymentNo = res.paymentNo
        this.cashierAmount = res.amount
        this.cashierChannel = res.channel
        this.cashierVisible = true
      })
    },
    // 第二步：模拟渠道完成支付 → 带签名回调 → 平台验签入账
    confirmPay() {
      this.paying = true
      this.request
          .get(`Payment?action=mockCashier&paymentNo=${this.cashierPaymentNo}`)
          .then(res => {
            if (!res || res.flag !== 0 || !res.params) {
              this.paying = false
              return this.$message.error((res && res.message) || '获取收银台参数失败')
            }
            const p = res.params
            const body = new URLSearchParams()
            body.append('paymentNo', p.paymentNo)
            body.append('tradeNo', p.tradeNo)
            body.append('amount', p.amount)
            body.append('timestamp', p.timestamp)
            body.append('sign', p.sign)
            // 模拟渠道回调平台（走真实 HTTP + 验签）
            return fetch(res.notifyUrl, {
              method: 'POST',
              headers: {'Content-Type': 'application/x-www-form-urlencoded'},
              body: body.toString()
            }).then(r => r.text()).then(text => {
              this.paying = false
              if (text === 'SUCCESS') {
                this.cashierVisible = false
                this.$message.success('支付成功，资金已进入平台托管')
                this.loadMyOrders()
              } else {
                this.$message.error('支付回调未通过：' + text)
              }
            })
          })
          .catch(() => {
            this.paying = false
            this.$message.error('支付失败')
          })
    },
    // 退款（仅「绘制中」可退）
    refund(order) {
      this.$prompt('请填写退款原因', '申请退款', {inputPlaceholder: '例如：画师无法按期交付'}).then(({value}) => {
        this.request.get(`Payment?action=byOrder&outTradeNo=${order.orderNo}`).then(res => {
          const payment = res && res.payment
          if (!payment || payment.status !== 1) {
            return this.$message.error('该订单没有可退款的支付记录')
          }
          const reason = encodeURIComponent(value || '买家取消')
          this.request
              .post(`Payment?action=refund&buyerId=${this.user.id}&paymentNo=${payment.paymentNo}&reason=${reason}`)
              .then(r2 => {
                if (r2 && r2.flag === 0) {
                  this.$message.success('退款成功，订单已退款')
                  this.loadMyOrders()
                } else {
                  this.$message.error((r2 && r2.message) || '退款失败')
                }
              })
        })
      }).catch(() => {})
    },
    confirm(order) {
      this.$confirm('确认已收到成品？确认后款项将结算给画师。', '提示', { type: 'warning' }).then(() => {
        this.request.post(`Commission?action=confirm&buyerId=${this.user.id}&orderId=${order.id}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success('已确认完成')
            this.loadMyOrders()
          } else {
            this.$message.error((res && res.message) || '确认失败')
          }
        })
      })
    },

    // ---------- 画师中心 ----------
    loadArtistPanel() {
      this.request.get(`GetUserInfo?userId=${this.user.id}`).then(res => {
        const u = (res && res.user) || {}
        this.artistStatus = u.artistStatus || 0
        this.acceptCommission = u.acceptCommission || 0
        this.applyDesc = u.artistApplyDesc || ''
        if (this.artistStatus === 2) {
          this.loadMyPlans()
          this.loadArtistOrders()
        }
      })
    },
    applyArtist() {
      if (!this.applyDesc.trim()) return this.$message.warning('请填写申请说明')
      this.applying = true
      this.request.post(`ApplyArtist?userId=${this.user.id}&desc=${encodeURIComponent(this.applyDesc)}`).then(res => {
        this.applying = false
        if (res && res.flag === 0) {
          this.$message.success('已提交，等待管理员审核')
          this.artistStatus = 1
        } else {
          this.$message.error('提交失败')
        }
      })
    },
    toggleAccept(val) {
      this.request.post(`Artist?action=accept&userId=${this.user.id}&accept=${val}`).then(res => {
        if (res && res.flag === 0) {
          this.$message.success(val ? '已开启接稿' : '已暂停接稿')
        } else {
          this.acceptCommission = val ? 0 : 1
          this.$message.error((res && res.message) || '操作失败')
        }
      })
    },
    loadMyPlans() {
      this.request.get(`Artist?action=plans&artistId=${this.user.id}`).then(res => {
        this.myPlans = (res && res.planList) || []
      })
    },
    loadArtistOrders() {
      this.request.get(`Artist?action=orders&artistId=${this.user.id}`).then(res => {
        this.artistOrders = (res && res.orderList) || []
      })
    },
    addPlan() {
      const f = this.planForm
      if (!f.title.trim() || !f.price || !f.deliveryDays) {
        return this.$message.warning('请填写方案标题、价格、交稿天数')
      }
      const url = `Artist?action=planAdd&artistId=${this.user.id}` +
          `&title=${encodeURIComponent(f.title)}&price=${f.price}&deliveryDays=${f.deliveryDays}` +
          `&contentType=${f.contentType || 1}`
      this.request.post(url).then(res => {
        if (res && res.flag === 0) {
          this.$message.success('方案已上架')
          this.planForm = { title: '', contentType: 1, price: '', deliveryDays: '' }
          this.loadMyPlans()
        } else {
          this.$message.error((res && res.message) || '新增失败')
        }
      })
    },
    setPlanStatus(plan, status) {
      this.request.post(`Artist?action=planStatus&artistId=${this.user.id}&planId=${plan.id}&status=${status}`).then(res => {
        if (res && res.flag === 0) {
          this.$message.success('已更新')
          this.loadMyPlans()
        } else {
          this.$message.error('更新失败')
        }
      })
    },
    // 交付包上传成功 → 立刻写入订单
    handleDeliverSuccess(res, order) {
      if (!res || res.flag !== 0 || !res.url) {
        return this.$message.error((res && res.message) || '交付包上传失败')
      }
      const url = `Artist?action=deliver&artistId=${this.user.id}&orderId=${order.id}` +
          `&type=final&url=${encodeURIComponent(res.url)}`
      this.request.post(url).then(r => {
        if (r && r.flag === 0) {
          this.$message.success('已交付，等待买家验收')
          this.loadArtistOrders()
        } else {
          this.$message.error((r && r.message) || '交稿失败')
        }
      })
    },
    handleDeliverError(err) {
      let msg = '交付包上传失败'
      try {
        msg = JSON.parse(err.message).message || msg
      } catch (e) {
        // 保留默认提示
      }
      this.$message.error(msg)
    },

    openChat(peerId) {
      this.$router.push({path: '/messages', query: {peerId}})
    }
  }
}
</script>

<style scoped>
.cc-page { max-width: 1200px; margin: 0 auto; padding: 24px; min-height: 70vh; }
.cc-square { display: flex; gap: 24px; align-items: flex-start; }
.cc-artists { display: flex; flex-wrap: wrap; gap: 12px; width: 320px; }
.cc-artist {
  width: 92px; text-align: center; cursor: pointer; padding: 8px 4px;
  border: 1px solid var(--border-color); border-radius: 10px; transition: all .2s;
}
.cc-artist.active, .cc-artist:hover { border-color: #0096fa; box-shadow: 0 2px 10px var(--shadow-color); }
.cc-artist img { width: 56px; height: 56px; border-radius: 50%; object-fit: cover; }
.cc-artist-name { font-size: 12px; margin-top: 6px; color: var(--text-secondary);
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cc-plans { flex: 1; min-width: 0; }
.cc-hint { color: var(--text-muted); padding: 40px 0; text-align: center; }
.cc-plan {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border: 1px solid var(--border-color);
  border-radius: 10px; margin-bottom: 10px;
}
.cc-plan-title { font-weight: 600; color: var(--text-primary); }
.cc-plan-meta { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.cc-switch-row { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.cc-add-plan { display: flex; gap: 10px; align-items: center; }
.cc-muted { color: var(--text-muted); font-size: 13px; }
.cc-link { font-size: 12px; color: #0096fa; margin: 0 8px; }
.cc-apply h3 { margin-bottom: 8px; }
/* 模拟收银台 */
.cc-cashier { text-align: center; padding: 6px 0 10px; }
.cc-cashier-amount { font-size: 28px; font-weight: 700; color: #0096fa; margin: 4px 0 14px; }
h3 { margin: 22px 0 10px; color: var(--text-primary); font-size: 15px; }
</style>
