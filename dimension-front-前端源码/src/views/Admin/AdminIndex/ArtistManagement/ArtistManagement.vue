<template>
  <div class="body" style="padding: 20px;">
    <h2>画师资质审核</h2>
    <p style="color: #999; margin-bottom: 20px;">审核通过后，该用户将获得发布约稿方案的权限。</p>

    <el-table :data="artistList" style="width: 100%;" stripe v-loading="loading">
      <el-table-column label="头像" width="120">
        <template slot-scope="scope">
          <img :src="scope.row.avatar" style="width:60px; height:60px; border-radius:50%; object-fit:cover;">
        </template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" width="150"></el-table-column>
      <el-table-column prop="artistApplyDesc" label="申请说明" show-overflow-tooltip></el-table-column>
      <el-table-column align="center" label="操作" width="200">
        <template slot-scope="scope">
          <el-button type="success" size="small" @click="handleAudit(scope.row.id, 2)">通过认证</el-button>
          <el-button type="danger" size="small" @click="handleAudit(scope.row.id, 3)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" style="margin-top: 20px; text-align: center;">
      <el-pagination
          background
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          layout="prev, pager, next"
          :total="pagination.total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  name: "ArtistManagement",
  data() {
    return {
      loading: false,
      artistList: [],
      pagination: { currentPage: 1, pageSize: 10, total: 0 }
    }
  },
  mounted() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      this.request.get(`GetPendingArtist?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.artistList = res.artistList;
        this.pagination.total = res.total;
        this.loading = false;
      })
    },
    handleAudit(userId, status) {
      const action = status === 2 ? '通过' : '驳回';
      this.$confirm(`确定要${action}该用户的画师认证吗？`, '提示', { type: 'warning' }).then(() => {
        this.request.post(`AuditArtist?userId=${userId}&status=${status}`).then(res => {
          if (res.flag === 0) {
            this.$message.success(`${action}成功`);
            this.getList();
          }
        })
      })
    },
    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.getList();
    }
  }
}
</script>