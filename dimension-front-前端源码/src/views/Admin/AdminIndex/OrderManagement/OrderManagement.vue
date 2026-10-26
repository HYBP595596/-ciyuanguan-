<template>
  <div class="ao-page">
    <el-tabs v-model="activeTab">
      <!-- ==================== 订单 ==================== -->
      <el-tab-pane label="约稿订单" name="orders">
        <div class="ao-stats">
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.total || 0 }}</div>
            <div class="ao-stat-label">总订单</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.waitingPay || 0 }}</div>
            <div class="ao-stat-label">待支付</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.drawing || 0 }}</div>
            <div class="ao-stat-label">绘制中</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.waitingAccept || 0 }}</div>
            <div class="ao-stat-label">待验收</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.finished || 0 }}</div>
            <div class="ao-stat-label">已完成</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.refunded || 0 }}</div>
            <div class="ao-stat-label">已退款</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">{{ stats.closed || 0 }}</div>
            <div class="ao-stat-label">已关闭</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">￥{{ stats.paidAmount || 0 }}</div>
            <div class="ao-stat-label">成交额</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">￥{{ stats.platformFee || 0 }}</div>
            <div class="ao-stat-label">平台抽成</div>
          </div>
          <div class="ao-stat">
            <div class="ao-stat-num">￥{{ stats.artistIncome || 0 }}</div>
            <div class="ao-stat-label">画师实收</div>
          </div>
        </div>

        <div class="ao-filter">
          <el-select v-model="query.status" placeholder="全部状态" clearable style="width:140px">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"></el-option>
          </el-select>
          <el-input v-model="query.keyword" placeholder="订单号 / 买家 / 画师" clearable style="width:240px"/>
          <el-button type="primary" icon="el-icon-search" @click="search">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button icon="el-icon-time" @click="closeExpired">立即关闭超时订单</el-button>
        </div>

        <el-table :data="orderList" stripe v-loading="loading" style="width:100%">
          <el-table-column prop="orderNo" label="订单号" width="185"></el-table-column>
          <el-table-column prop="planTitle" label="方案" width="140" show-overflow-tooltip></el-table-column>
          <el-table-column label="买家" width="110">
            <template slot-scope="s">{{ s.row.buyerNickname || ('用户' + s.row.buyerId) }}</template>
          </el-table-column>
          <el-table-column label="画师" width="110">
            <template slot-scope="s">{{ s.row.artistNickname || ('用户' + s.row.artistId) }}</template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template slot-scope="s">{{ typeText(s.row.contentType) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="90"></el-table-column>
          <el-table-column label="抽成/实收" width="130">
            <template slot-scope="s">
              <span v-if="s.row.status === 3">
                ￥{{ s.row.platformFee || 0 }} / ￥{{ s.row.artistIncome || 0 }}
              </span>
              <span v-else class="ao-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="s">
              <el-tag size="mini" :type="statusType(s.row.status)">{{ statusText(s.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="下单时间" width="160">
            <template slot-scope="s">{{ formatTime(s.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="230" fixed="right">
            <template slot-scope="s">
              <el-button size="mini" @click="openDetail(s.row)">详情</el-button>
              <el-button v-if="s.row.status === 1 || s.row.status === 2" size="mini" type="danger"
                         @click="forceRefund(s.row)">强制退款</el-button>
              <el-button v-if="s.row.status === 0" size="mini" @click="forceClose(s.row)">关闭</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="ao-pagination">
          <el-pagination
              background
              :page-size="query.pageSize"
              :page-sizes="[10, 20, 50]"
              :current-page="query.currentPage"
              layout="total, sizes, prev, pager, next"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </el-tab-pane>

      <!-- ==================== 提现审核 ==================== -->
      <el-tab-pane label="提现审核" name="withdraws">
        <div class="ao-filter">
          <el-select v-model="withdrawQuery.status" placeholder="全部状态" clearable style="width:140px">
            <el-option label="审核中" :value="0"></el-option>
            <el-option label="已打款" :value="1"></el-option>
            <el-option label="已驳回" :value="2"></el-option>
          </el-select>
          <el-button type="primary" icon="el-icon-search" @click="loadWithdraws">查询</el-button>
        </div>
        <el-table :data="withdrawList" stripe v-loading="withdrawLoading" style="width:100%">
          <el-table-column prop="id" label="ID" width="70"></el-table-column>
          <el-table-column label="画师" width="150">
            <template slot-scope="s">{{ s.row.nickname || ('用户' + s.row.userId) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="100"></el-table-column>
          <el-table-column prop="account" label="收款方式" min-width="160"></el-table-column>
          <el-table-column label="状态" width="100">
            <template slot-scope="s">
              <el-tag size="mini" :type="withdrawType(s.row.status)">{{ withdrawText(s.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140"></el-table-column>
          <el-table-column label="申请时间" width="160">
            <template slot-scope="s">{{ formatTime(s.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template slot-scope="s">
              <template v-if="s.row.status === 0">
                <el-button size="mini" type="success" @click="audit(s.row, 1)">已打款</el-button>
                <el-button size="mini" type="danger" @click="audit(s.row, 2)">驳回</el-button>
              </template>
              <span v-else class="ao-muted">已处理</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="ao-pagination">
          <el-pagination
              background
              :page-size="withdrawQuery.pageSize"
              :page-sizes="[10, 20, 50]"
              :current-page="withdrawQuery.currentPage"
              layout="total, sizes, prev, pager, next"
              :total="withdrawTotal"
              @size-change="s => { withdrawQuery.pageSize = s; withdrawQuery.currentPage = 1; loadWithdraws() }"
              @current-change="p => { withdrawQuery.currentPage = p; loadWithdraws() }">
          </el-pagination>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 订单详情 -->
    <el-dialog title="订单详情" :visible.sync="detailVisible" width="640px">
      <div v-if="detailOrder" class="ao-detail">
        <p>订单号：{{ detailOrder.orderNo }} <el-tag size="mini" :type="statusType(detailOrder.status)">
          {{ statusText(detailOrder.status) }}</el-tag></p>
        <p>方案：{{ detailOrder.planTitle }}（{{ typeText(detailOrder.contentType) }}）</p>
        <p>需求：{{ detailOrder.requirement }}</p>
        <p>金额：￥{{ detailOrder.amount }}　
          <span v-if="detailOrder.status === 3">
            平台抽成 ￥{{ detailOrder.platformFee || 0 }}　画师实收 ￥{{ detailOrder.artistIncome || 0 }}
          </span>
        </p>
        <p>买家：{{ detailOrder.buyerNickname || detailOrder.buyerId }}　
          画师：{{ detailOrder.artistNickname || detailOrder.artistId }}</p>
        <p>下单：{{ formatTime(detailOrder.createTime) }}　
          关闭：{{ detailOrder.closeTime ? formatTime(detailOrder.closeTime) : '-' }}</p>
        <p v-if="detailOrder.deliveryFinal">
          交付包：<a :href="detailOrder.deliveryFinal" target="_blank">下载 / 查看</a>
        </p>
        <h4>资金流水</h4>
        <el-table :data="detailTransactions" size="mini" stripe style="width:100%">
          <el-table-column prop="type" label="类型" width="130">
            <template slot-scope="s">{{ txText(s.row.type) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="110"></el-table-column>
          <el-table-column prop="transactionId" label="渠道单号" min-width="180"></el-table-column>
          <el-table-column label="时间" width="160">
            <template slot-scope="s">{{ formatTime(s.row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!detailTransactions.length" description="暂无流水"></el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'OrderManagement',
  data() {
    return {
      activeTab: 'orders',
      loading: false,
      orderList: [],
      total: 0,
      stats: {},
      query: { status: null, keyword: '', currentPage: 1, pageSize: 10 },
      statusOptions: [
        { value: 0, label: '待支付' },
        { value: 1, label: '绘制中' },
        { value: 2, label: '待验收' },
        { value: 3, label: '已完成' },
        { value: 4, label: '已退款' },
        { value: 5, label: '已关闭' }
      ],
      detailVisible: false,
      detailOrder: null,
      detailTransactions: [],
      // 提现
      withdrawLoading: false,
      withdrawList: [],
      withdrawTotal: 0,
      withdrawQuery: { status: null, currentPage: 1, pageSize: 10 }
    }
  },
  mounted() {
    this.loadOrders()
    this.loadWithdraws()
  },
  methods: {
    loadOrders() {
      this.loading = true
      const offset = (this.query.currentPage - 1) * this.query.pageSize
      let url = `AdminOrder?action=list&pageNum=${offset}&pageSize=${this.query.pageSize}`
      if (this.query.status !== null && this.query.status !== '') url += `&status=${this.query.status}`
      if (this.query.keyword) url += `&keyword=${encodeURIComponent(this.query.keyword)}`
      this.request.get(url).then(res => {
        this.loading = false
        if (res && res.needLogin) {
          this.$message.error(res.message || '管理员未登录')
          this.$router.push('/admin-login')
          return
        }
        this.orderList = (res && res.orderList) || []
        this.total = (res && res.total) || 0
        this.stats = (res && res.stats) || {}
      }).catch(() => { this.loading = false })
    },
    search() {
      this.query.currentPage = 1
      this.loadOrders()
    },
    resetQuery() {
      this.query = { status: null, keyword: '', currentPage: 1, pageSize: this.query.pageSize }
      this.loadOrders()
    },
    handleSizeChange(size) {
      this.query.pageSize = size
      this.query.currentPage = 1
      this.loadOrders()
    },
    handleCurrentChange(page) {
      this.query.currentPage = page
      this.loadOrders()
    },
    closeExpired() {
      this.request.get('AdminOrder?action=closeExpired').then(res => {
        if (res && res.flag === 0) {
          this.$message.success(res.message || '已处理')
          this.loadOrders()
        } else {
          this.$message.error((res && res.message) || '操作失败')
        }
      })
    },
    openDetail(row) {
      this.detailOrder = row
      this.detailTransactions = []
      this.detailVisible = true
      this.request.get(`AdminOrder?action=detail&orderId=${row.id}`).then(res => {
        if (res && res.flag === 0) {
          this.detailOrder = res.order
          this.detailTransactions = res.transactions || []
        }
      })
    },
    forceRefund(row) {
      this.$prompt('强制退款会调用渠道退款并把订单置为已退款，请填写原因', '强制退款', {
        inputPlaceholder: '例如：画师违约，平台介入退款'
      }).then(({value}) => {
        const reason = encodeURIComponent(value || '管理员强制退款')
        this.request.post(`AdminOrder?action=forceRefund&orderNo=${row.orderNo}&reason=${reason}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success(res.message || '已退款')
            this.loadOrders()
          } else {
            this.$message.error((res && res.message) || '退款失败')
          }
        })
      }).catch(() => {})
    },
    forceClose(row) {
      this.$confirm(`确定关闭订单 ${row.orderNo}？该订单将不能再支付。`, '关闭订单', { type: 'warning' })
          .then(() => {
            this.request.post(`AdminOrder?action=forceClose&orderId=${row.id}`).then(res => {
              if (res && res.flag === 0) {
                this.$message.success('订单已关闭')
                this.loadOrders()
              } else {
                this.$message.error((res && res.message) || '关闭失败')
              }
            })
          }).catch(() => {})
    },

    // ---------- 提现审核 ----------
    loadWithdraws() {
      this.withdrawLoading = true
      const offset = (this.withdrawQuery.currentPage - 1) * this.withdrawQuery.pageSize
      let url = `Settlement?action=withdraws&pageNum=${offset}&pageSize=${this.withdrawQuery.pageSize}`
      if (this.withdrawQuery.status !== null && this.withdrawQuery.status !== '') {
        url += `&status=${this.withdrawQuery.status}`
      }
      this.request.get(url).then(res => {
        this.withdrawLoading = false
        if (res && res.needLogin) {
          this.$message.error(res.message || '管理员未登录')
          return
        }
        this.withdrawList = (res && res.withdrawList) || []
        this.withdrawTotal = (res && res.total) || 0
      }).catch(() => { this.withdrawLoading = false })
    },
    audit(row, status) {
      const doAudit = remark => {
        this.request.post(`Settlement?action=audit&withdrawId=${row.id}&status=${status}` +
            `&remark=${encodeURIComponent(remark || '')}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success(res.message || '已处理')
            this.loadWithdraws()
          } else {
            this.$message.error((res && res.message) || '处理失败')
          }
        })
      }
      if (status === 1) {
        this.$confirm(`确认已向画师打款 ￥${row.amount}？`, '标记已打款', { type: 'warning' })
            .then(() => doAudit('管理员已打款')).catch(() => {})
      } else {
        this.$prompt('请填写驳回原因，金额会退回画师余额', '驳回提现', { inputPlaceholder: '例如：账号信息有误' })
            .then(({value}) => doAudit(value || '管理员驳回')).catch(() => {})
      }
    },

    // ---------- 展示辅助 ----------
    statusText(s) {
      return { 0: '待支付', 1: '绘制中', 2: '待验收', 3: '已完成', 4: '已退款', 5: '已关闭' }[s] || '未知'
    },
    statusType(s) {
      return { 0: 'warning', 1: '', 2: 'primary', 3: 'success', 4: 'info', 5: 'info' }[s] || 'info'
    },
    typeText(t) {
      return { 1: '插画', 2: '漫画', 3: '小说' }[t] || '插画'
    },
    txText(t) {
      return {
        PAY: '买家支付（托管）',
        INCOME: '画师实收',
        PLATFORM_FEE: '平台抽成',
        REFUND: '退款'
      }[t] || t
    },
    withdrawText(s) {
      return { 0: '审核中', 1: '已打款', 2: '已驳回' }[s] || '未知'
    },
    withdrawType(s) {
      return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
    },
    formatTime(t) {
      if (!t) return '-'
      const d = new Date(t)
      const pad = n => (n < 10 ? '0' + n : '' + n)
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ` +
          `${pad(d.getHours())}:${pad(d.getMinutes())}`
    }
  }
}
</script>

<style scoped>
.ao-page { padding: 20px 24px; }
.ao-stats {
  display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 16px;
}
.ao-stat {
  min-width: 104px; padding: 12px 16px; border-radius: 10px;
  border: 1px solid var(--border-color); background: var(--bg-header);
}
.ao-stat-num { font-size: 18px; font-weight: 700; color: var(--text-primary); }
.ao-stat-label { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
.ao-filter { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; flex-wrap: wrap; }
.ao-pagination { margin-top: 16px; text-align: center; }
.ao-muted { color: var(--text-muted); font-size: 12px; }
.ao-detail p { margin: 6px 0; font-size: 13px; color: var(--text-secondary); }
.ao-detail h4 { margin: 16px 0 8px; color: var(--text-primary); font-size: 14px; }
</style>
