<template>
  <div class="cc-page">
    <el-tabs v-model="activeTab" class="cc-tabs">

      <!-- ============ 约稿广场 ============ -->
      <el-tab-pane label="约稿广场" name="square">
        <div class="cc-filter">
          <span class="cc-muted">约稿类型</span>
          <el-select v-model="squareType" size="small" style="width:120px" @change="onTypeChange">
            <el-option :value="0" label="全部"></el-option>
            <el-option :value="1" label="插画"></el-option>
            <el-option :value="2" label="漫画"></el-option>
            <el-option :value="3" label="小说"></el-option>
          </el-select>
        </div>
        <el-empty
            v-if="!artistList.length"
            :description="squareType ? '当前类型下还没有接稿中的画师，换个类型试试' : '暂时没有接稿中的画师'"></el-empty>
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
              <img v-if="p.coverUrl" :src="$img(p.coverUrl)" class="cc-plan-cover" alt=""/>
              <div class="cc-plan-main">
                <div class="cc-plan-title">{{ p.title }}</div>
                <div class="cc-plan-meta">￥{{ p.price }} · {{ p.deliveryDays }} 天交稿 · {{ typeText(p.contentType) }}</div>
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
              <el-button v-if="s.row.status === 0" size="mini" @click="cancel(s.row)">取消</el-button>
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

          <!-- 结算：余额 / 提现 -->
          <div class="cc-balance">
            <div class="cc-balance-item">
              <div class="cc-balance-num">￥{{ balanceInfo.balance }}</div>
              <div class="cc-muted">可提现余额</div>
            </div>
            <div class="cc-balance-item">
              <div class="cc-balance-num">￥{{ balanceInfo.totalIncome }}</div>
              <div class="cc-muted">累计实收</div>
            </div>
            <div class="cc-balance-item">
              <div class="cc-balance-num">￥{{ balanceInfo.totalWithdrawn }}</div>
              <div class="cc-muted">累计已提现</div>
            </div>
            <div class="cc-balance-actions">
              <el-button type="primary" size="small" @click="openWithdraw">申请提现</el-button>
              <el-button size="small" @click="loadMyWithdraws">提现记录</el-button>
            </div>
          </div>
          <el-table :data="myWithdraws" stripe size="mini" style="width:100%;margin-bottom:16px"
                    v-if="myWithdraws.length">
            <el-table-column prop="amount" label="金额" width="100"></el-table-column>
            <el-table-column label="状态" width="100">
              <template slot-scope="s">
                <el-tag size="mini" :type="withdrawStatusType(s.row.status)">
                  {{ withdrawStatusText(s.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="account" label="收款方式" min-width="140"></el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140"></el-table-column>
            <el-table-column label="申请时间" width="170">
              <template slot-scope="s">{{ formatTime(s.row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="cc-plan-head">
            <h3>我的方案</h3>
            <span class="cc-muted">「新增并上架」会立刻出现在约稿广场；拿不准就先「存为草稿」，之后在表格里点「上架」。</span>
          </div>
          <div class="cc-add-plan">
            <el-input v-model="planForm.title" placeholder="方案标题，如 Q版头像" style="width:180px"/>
            <el-select v-model="planForm.contentType" placeholder="类型" style="width:110px">
              <el-option :value="1" label="插画"></el-option>
              <el-option :value="2" label="漫画"></el-option>
              <el-option :value="3" label="小说"></el-option>
            </el-select>
            <el-input v-model="planForm.price" placeholder="价格" style="width:100px"/>
            <el-input v-model="planForm.deliveryDays" placeholder="交稿天数" style="width:100px"/>
            <el-input v-model="planForm.coverUrl" placeholder="封面图 URL（可选）" style="width:210px"/>
            <el-button type="primary" size="small" :loading="planSaving" @click="addPlan(1)">新增并上架</el-button>
            <el-button size="small" :loading="planSaving" @click="addPlan(0)">存为草稿</el-button>
          </div>
          <el-table
              :data="myPlans"
              stripe
              style="width:100%;margin-top:12px"
              empty-text="还没有接稿方案：填好上面的标题、类型、价格、交稿天数，点「新增并上架」或「存为草稿」">
            <el-table-column label="封面" width="86">
              <template slot-scope="s">
                <img v-if="s.row.coverUrl" :src="$img(s.row.coverUrl)" class="cc-thumb" alt=""/>
                <span v-else class="cc-muted">无</span>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="方案" min-width="150"></el-table-column>
            <el-table-column label="类型" width="90">
              <template slot-scope="s">{{ typeText(s.row.contentType) }}</template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100"></el-table-column>
            <el-table-column prop="deliveryDays" label="交稿天数" width="100"></el-table-column>
            <el-table-column label="状态" width="110">
              <template slot-scope="s">
                <el-tag size="mini" :type="planStatusType(s.row.status)">{{ planStatusText(s.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="230">
              <template slot-scope="s">
                <el-button
                    v-if="s.row.status === 1"
                    size="mini"
                    type="danger"
                    plain
                    @click="setPlanStatus(s.row, 2)">下架</el-button>
                <el-button
                    v-else
                    size="mini"
                    type="success"
                    @click="setPlanStatus(s.row, 1)">{{ s.row.status === 0 ? '上架' : '重新上架' }}</el-button>
                <el-button size="mini" @click="openPlanEdit(s.row)">编辑</el-button>
                <el-button size="mini" type="danger" plain @click="removePlan(s.row)">删除</el-button>
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

    <!-- 提现申请 -->
    <el-dialog title="申请提现" :visible.sync="withdrawDialogVisible" width="420px">
      <p class="cc-muted">可提现余额：￥{{ balanceInfo.balance }}（订单验收后入账，平台收取服务费后的金额）</p>
      <el-form label-width="90px">
        <el-form-item label="提现金额">
          <el-input v-model="withdrawForm.amount" placeholder="最低 1 元，最多两位小数"></el-input>
        </el-form-item>
        <el-form-item label="收款方式">
          <el-input v-model="withdrawForm.account" placeholder="支付宝账号 / 微信 / 银行卡，方便管理员打款"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="withdrawing" @click="submitWithdraw">提交申请</el-button>
      </span>
    </el-dialog>

    <!-- 编辑方案 -->
    <el-dialog title="编辑方案" :visible.sync="planEditVisible" width="480px">
      <el-form label-width="90px">
        <el-form-item label="方案标题">
          <el-input v-model="planEditForm.title" placeholder="如 Q版头像"/>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="planEditForm.contentType" style="width:100%">
            <el-option :value="1" label="插画"></el-option>
            <el-option :value="2" label="漫画"></el-option>
            <el-option :value="3" label="小说"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="planEditForm.price" placeholder="单位：元"/>
        </el-form-item>
        <el-form-item label="交稿天数">
          <el-input v-model="planEditForm.deliveryDays" placeholder="单位：天"/>
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="planEditForm.coverUrl" placeholder="图片 URL，可留空"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="planEditForm.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="2">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="planEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="planSaving" @click="savePlanEdit">保存</el-button>
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
      planForm: { title: '', contentType: 1, price: '', deliveryDays: '', coverUrl: '' },
      planSaving: false,
      planEditVisible: false,
      planEditForm: {
        id: null,
        title: '',
        contentType: 1,
        price: '',
        deliveryDays: '',
        coverUrl: '',
        status: 1
      },
      // 广场按约稿类型筛选：0=全部 1=插画 2=漫画 3=小说
      squareType: 0,
      // 模拟收银台
      cashierVisible: false,
      cashierPaymentNo: '',
      cashierAmount: '',
      cashierChannel: 'MOCK',
      payChannel: 'MOCK',
      paying: false,
      myPlans: [],
      artistOrders: [],
      // 画师结算
      balanceInfo: { balance: 0, totalIncome: 0, totalWithdrawn: 0, pendingWithdraws: 0 },
      withdrawDialogVisible: false,
      withdrawForm: { amount: '', account: '' },
      withdrawing: false,
      myWithdraws: []
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
  watch: {
    // 从画师主页点“约稿”进来时带 artistId，切换画师时也能自动重新选中
    '$route.query.artistId'(val) {
      if (val) this.applyRouteArtist()
    }
  },
  methods: {
    statusText(s) {
      return { 0: '待支付', 1: '绘制中', 2: '待验收', 3: '已完成', 4: '已退款', 5: '已关闭' }[s] || '未知'
    },
    statusType(s) {
      return { 0: 'warning', 1: '', 2: 'primary', 3: 'success', 4: 'info', 5: 'info' }[s] || 'info'
    },

    // ---------- 广场 ----------
    loadSquare() {
      const typeFilter = this.squareType > 0 ? `&contentType=${this.squareType}` : ''
      this.request.get(`Commission?action=artists&currentPage=1&pageSize=50${typeFilter}`).then(res => {
        this.artistList = (res && res.artistList) || []
        // 换了筛选条件后：选中的人还在列表里就保留，否则清空
        const keep = this.selectedArtist
            ? this.artistList.find(a => String(a.id) === String(this.selectedArtist.id))
            : null
        if (keep) {
          this.selectedArtist = keep
          this.loadArtistPlans(keep.id)
        } else {
          this.selectedArtist = null
          this.artistPlans = []
          // 列表到了再按路由参数定位画师，否则拿不到对应的人
          this.applyRouteArtist()
        }
      })
    },
    onTypeChange() {
      this.selectedArtist = null
      this.artistPlans = []
      this.loadSquare()
    },
    loadArtistPlans(artistId) {
      const typeFilter = this.squareType > 0 ? `&contentType=${this.squareType}` : ''
      this.request.get(`Commission?action=artistPlans&artistId=${artistId}${typeFilter}`).then(res => {
        this.artistPlans = (res && res.planList) || []
      })
    },
    // 从画师主页点“约稿”过来：自动切到广场并选中这位画师
    applyRouteArtist() {
      const artistId = this.$route.query.artistId
      if (!artistId) return
      const target = this.artistList.find(a => String(a.id) === String(artistId))
      if (target) {
        this.activeTab = 'square'
        if (!this.selectedArtist || String(this.selectedArtist.id) !== String(target.id)) {
          this.selectArtist(target)
        }
      } else {
        this.$message.info('该画师当前未开启接稿，可在广场里看看其他画师')
      }
    },
    selectArtist(a) {
      this.selectedArtist = a
      this.loadArtistPlans(a.id)
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
          `&requirement=${encodeURIComponent(this.requirement)}&channel=${this.payChannel}`
      this.request.post(url).then(res => {
        this.ordering = false
        if (!res || res.flag !== 0) {
          return this.$message.error((res && res.message) || '下单失败')
        }
        this.orderDialogVisible = false
        this.activeTab = 'myOrders'
        this.loadMyOrders()
        // 下单即出支付参数：直接进收银台，不再让用户多点一次「支付」
        if (res.payReady && res.paymentNo) {
          this.cashierPaymentNo = res.paymentNo
          this.cashierAmount = res.amount
          this.cashierChannel = res.channel
          this.cashierVisible = true
          this.$message.success(`下单成功，请在 ${res.expireMinutes || 30} 分钟内完成支付`)
        } else {
          this.$message.warning(res.payMessage || '订单已创建，请稍后在我的约稿里继续支付')
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
      this.$confirm('确认已收到成品？确认后款项将结算给画师（平台收取服务费）。', '提示', { type: 'warning' }).then(() => {
        this.request.post(`Commission?action=confirm&buyerId=${this.user.id}&orderId=${order.id}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success(res.message || '已确认完成')
            this.loadMyOrders()
          } else {
            this.$message.error((res && res.message) || '确认失败')
          }
        })
      })
    },
    // 取消待支付订单（同时会关掉对应的支付单）
    cancel(order) {
      this.$confirm('取消后该订单将关闭，需要重新下单。确定取消？', '取消订单', {type: 'warning'}).then(() => {
        this.request.post(`Commission?action=cancel&buyerId=${this.user.id}&orderId=${order.id}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success('订单已取消')
            this.loadMyOrders()
          } else {
            this.$message.error((res && res.message) || '取消失败')
          }
        })
      }).catch(() => {})
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
          this.loadBalance()
          this.loadMyWithdraws()
        }
      })
    },
    // ---------- 结算：余额 / 提现 ----------
    loadBalance() {
      this.request.get(`Settlement?action=balance&userId=${this.user.id}`).then(res => {
        if (res && res.flag === 0) {
          this.balanceInfo = {
            balance: res.balance != null ? res.balance : 0,
            totalIncome: res.totalIncome != null ? res.totalIncome : 0,
            totalWithdrawn: res.totalWithdrawn != null ? res.totalWithdrawn : 0,
            pendingWithdraws: res.pendingWithdraws || 0
          }
        }
      })
    },
    loadMyWithdraws() {
      this.request.get(`Settlement?action=myWithdraws&userId=${this.user.id}`).then(res => {
        this.myWithdraws = (res && res.withdrawList) || []
      })
    },
    openWithdraw() {
      this.withdrawForm = { amount: this.balanceInfo.balance || '', account: '' }
      this.withdrawDialogVisible = true
    },
    submitWithdraw() {
      const amount = Number(this.withdrawForm.amount)
      if (!amount || amount <= 0) return this.$message.warning('请填写正确的提现金额')
      if (amount > Number(this.balanceInfo.balance || 0)) return this.$message.warning('提现金额不能超过可提现余额')
      this.withdrawing = true
      this.request.post(`Settlement?action=apply&userId=${this.user.id}` +
          `&amount=${amount}&account=${encodeURIComponent(this.withdrawForm.account || '')}`).then(res => {
        this.withdrawing = false
        if (res && res.flag === 0) {
          this.$message.success(res.message || '提现申请已提交')
          this.withdrawDialogVisible = false
          this.loadBalance()
          this.loadMyWithdraws()
        } else {
          this.$message.error((res && res.message) || '提现申请失败')
        }
      })
    },
    withdrawStatusText(s) {
      return { 0: '审核中', 1: '已打款', 2: '已驳回' }[s] || '未知'
    },
    withdrawStatusType(s) {
      return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
    },
    formatTime(t) {
      if (!t) return '-'
      const d = new Date(t)
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ` +
          `${pad(d.getHours())}:${pad(d.getMinutes())}`
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
    // status: 1=直接上架，0=存草稿（草稿不会出现在约稿广场）
    addPlan(status) {
      const f = this.planForm
      if (!f.title || !f.title.trim()) return this.$message.warning('请填写方案标题')
      if (!f.price || Number(f.price) <= 0) return this.$message.warning('请填写正确的价格')
      if (!f.deliveryDays || Number(f.deliveryDays) <= 0) return this.$message.warning('请填写正确的交稿天数')
      this.planSaving = true
      const url = `Artist?action=planAdd&artistId=${this.user.id}` +
          `&title=${encodeURIComponent(f.title.trim())}&price=${f.price}&deliveryDays=${f.deliveryDays}` +
          `&contentType=${f.contentType || 1}&status=${status}` +
          `&coverUrl=${encodeURIComponent(f.coverUrl || '')}`
      this.request.post(url).then(res => {
        this.planSaving = false
        if (res && res.flag === 0) {
          this.$message.success(status === 1 ? '方案已上架，买家可以在广场下单' : '已存为草稿，可在表格里点「上架」')
          this.planForm = { title: '', contentType: 1, price: '', deliveryDays: '', coverUrl: '' }
          this.loadMyPlans()
        } else {
          this.$message.error((res && res.message) || '新增失败')
        }
      }).catch(() => { this.planSaving = false })
    },
    planStatusText(status) {
      return { 0: '草稿', 1: '上架中', 2: '已下架' }[status] || '未知'
    },
    planStatusType(status) {
      return { 0: 'info', 1: 'success', 2: 'warning' }[status] || 'info'
    },
    openPlanEdit(plan) {
      this.planEditForm = {
        id: plan.id,
        title: plan.title || '',
        contentType: plan.contentType || 1,
        price: plan.price,
        deliveryDays: plan.deliveryDays,
        coverUrl: plan.coverUrl || '',
        status: plan.status == null ? 1 : plan.status
      }
      this.planEditVisible = true
    },
    savePlanEdit() {
      const f = this.planEditForm
      if (!f.title || !f.title.trim()) return this.$message.warning('请填写方案标题')
      if (!f.price || Number(f.price) <= 0) return this.$message.warning('请填写正确的价格')
      if (!f.deliveryDays || Number(f.deliveryDays) <= 0) return this.$message.warning('请填写正确的交稿天数')
      this.planSaving = true
      const url = `Artist?action=planEdit&artistId=${this.user.id}&planId=${f.id}` +
          `&title=${encodeURIComponent(f.title.trim())}&price=${f.price}&deliveryDays=${f.deliveryDays}` +
          `&contentType=${f.contentType || 1}&status=${f.status}` +
          `&coverUrl=${encodeURIComponent(f.coverUrl || '')}`
      this.request.post(url).then(res => {
        this.planSaving = false
        if (res && res.flag === 0) {
          this.$message.success('已保存')
          this.planEditVisible = false
          this.loadMyPlans()
        } else {
          this.$message.error((res && res.message) || '保存失败')
        }
      }).catch(() => { this.planSaving = false })
    },
    removePlan(plan) {
      this.$confirm(
          `删除后该方案不可恢复；如果已经有人下过单，系统会拒绝删除，只能下架。确定删除「${plan.title || ''}」？`,
          '删除方案',
          {type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger'}
      ).then(() => {
        this.request.post(`Artist?action=planDelete&artistId=${this.user.id}&planId=${plan.id}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success('方案已删除')
            this.loadMyPlans()
          } else {
            this.$message.error((res && res.message) || '删除失败')
          }
        })
      }).catch(() => {})
    },
    setPlanStatus(plan, status) {
      const action = status === 1 ? '上架' : '下架'
      const tip = status === 1
          ? `上架后买家就能在约稿广场下单「${plan.title || ''}」。确定上架？`
          : `下架后买家将看不到「${plan.title || ''}」，已下单的订单不受影响。确定下架？`
      this.$confirm(tip, action + '方案', {type: 'warning'}).then(() => {
        this.request.post(`Artist?action=planStatus&artistId=${this.user.id}&planId=${plan.id}&status=${status}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success(action + '成功')
            this.loadMyPlans()
          } else {
            this.$message.error((res && res.message) || (action + '失败'))
          }
        })
      }).catch(() => {})
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
.cc-add-plan { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.cc-plan-head { display: flex; align-items: baseline; gap: 12px; flex-wrap: wrap; }
.cc-plan-head h3 { margin: 22px 0 10px; }
.cc-filter { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.cc-thumb { width: 56px; height: 56px; object-fit: cover; border-radius: 6px; display: block; }
.cc-plan-cover { width: 68px; height: 68px; object-fit: cover; border-radius: 8px; margin-right: 14px; flex: 0 0 auto; }
.cc-muted { color: var(--text-muted); font-size: 13px; }
.cc-link { font-size: 12px; color: #0096fa; margin: 0 8px; }
.cc-apply h3 { margin-bottom: 8px; }
/* 模拟收银台 */
.cc-cashier { text-align: center; padding: 6px 0 10px; }
.cc-cashier-amount { font-size: 28px; font-weight: 700; color: #0096fa; margin: 4px 0 14px; }
/* 结算卡片 */
.cc-balance {
  display: flex; align-items: center; gap: 28px; flex-wrap: wrap;
  padding: 16px 20px; margin-bottom: 16px;
  border: 1px solid var(--border-color); border-radius: 10px;
  background: var(--bg-header);
}
.cc-balance-num { font-size: 20px; font-weight: 700; color: var(--text-primary); }
.cc-balance-actions { margin-left: auto; display: flex; gap: 10px; }
h3 { margin: 22px 0 10px; color: var(--text-primary); font-size: 15px; }
</style>
