<template>
  <div class="body" style="padding: 20px;">
    <div class="header">
      <h2>作品审核大厅</h2>
      <p style="color: #999;">请仔细审查用户上传的作品，通过后将展示在前台。</p>
    </div>

    <el-table :data="pendingList" style="width: 100%; margin-top: 20px;" stripe v-loading="loading">
      <el-table-column label="预览图" width="200">
        <template slot-scope="scope">
          <el-image
              style="width: 150px; height: 100px; border-radius: 8px;"
              :src="scope.row.url"
              :preview-src-list="[scope.row.url]">
          </el-image>
        </template>
      </el-table-column>

      <el-table-column prop="id" label="图片ID" width="100"></el-table-column>
      <el-table-column prop="userId" label="上传者ID" width="120"></el-table-column>
      <el-table-column label="上传时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" width="250">
        <template slot-scope="scope">
          <el-button type="success" size="small" @click="handleAudit(scope.row.id, 1)">通过上架</el-button>
          <el-button type="danger" size="small" @click="handleAudit(scope.row.id, 2)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" style="margin-top: 20px; text-align: center;">
      <el-pagination
          background
          @size-change="handleSizeChange"
          :page-size="pagination.pageSize"
          :page-sizes="[5, 10, 20]"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          layout="total, sizes, prev, pager, next"
          :total="pagination.total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { format1 } from '@/utils/time-format'

export default {
  name: "AuditManagement",
  data() {
    return {
      loading: false,
      pendingList: [],
      pagination: {
        currentPage: 1,
        pageSize: 5,
        total: 0
      }
    }
  },
  mounted() {
    this.getPendingList();
  },
  methods: {
    formatTime(time) { return format1(time) },
    getPendingList() {
      this.loading = true;
      this.request.get(`GetPendingImage?currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`).then(res => {
        this.pendingList = res.imageList;
        this.pagination.total = res.total;
        this.loading = false;
      })
    },
    handleAudit(imageId, status) {
      const actionName = status === 1 ? '通过' : '驳回';
      this.$confirm(`确定要${actionName}该作品吗？`, '提示', { type: 'warning' }).then(() => {
        this.request.post(`AuditImage?imageId=${imageId}&status=${status}`).then(res => {
          if (res.flag === 0) {
            this.$message.success(`${actionName}成功`);
            this.getPendingList(); // 刷新列表
          }
        })
      })
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.getPendingList();
    },
    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.getPendingList();
    }
  }
}
</script>

<style scoped>
.header h2 { margin-bottom: 8px; color: var(--text-primary); }
</style>