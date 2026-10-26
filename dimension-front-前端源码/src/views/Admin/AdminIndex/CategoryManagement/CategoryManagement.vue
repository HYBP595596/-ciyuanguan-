<template>
  <div class="cm-page">
    <div class="cm-head">
      <h2>分类管理</h2>
      <p class="cm-hint">管理作品所属的类型（插画 / 漫画 / 小说）。左侧选分类，右侧可单个或批量把作品移到其它分类。</p>
    </div>

    <div class="cm-body">
      <!-- 左侧分类 -->
      <aside class="cm-cats">
        <div
            v-for="c in categories"
            :key="c.id"
            class="cm-cat"
            :class="{ active: selectedCategoryId === c.id }"
            @click="selectCategory(c.id)">
          <span class="cm-cat-name">{{ c.name }}</span>
          <span class="cm-cat-count">{{ c.count || 0 }}</span>
        </div>
        <el-empty v-if="!categories.length" description="暂无分类" :image-size="60"></el-empty>
      </aside>

      <!-- 右侧作品 -->
      <section class="cm-main">
        <div class="cm-toolbar">
          <span class="cm-current">
            当前分类：<b>{{ currentCategoryName }}</b>　共 {{ pagination.total }} 个作品
          </span>
          <div class="cm-actions">
            <el-select v-model="batchTarget" placeholder="批量移到…" size="small" style="width:150px">
              <el-option
                  v-for="c in otherCategories"
                  :key="c.id"
                  :label="c.name"
                  :value="c.id">
              </el-option>
            </el-select>
            <el-button
                type="primary"
                size="small"
                :disabled="!selectedRows.length || !batchTarget"
                @click="moveBatch">
              移动选中（{{ selectedRows.length }}）
            </el-button>
          </div>
        </div>

        <el-table
            :data="imageTagList"
            stripe
            v-loading="loading"
            style="width:100%"
            @selection-change="onSelectionChange">
          <el-table-column type="selection" width="45"></el-table-column>
          <el-table-column label="作品" width="230">
            <template slot-scope="s">
              <img :src="$img(s.row.image.url)" class="cm-thumb"/>
            </template>
          </el-table-column>
          <el-table-column label="ID" width="80">
            <template slot-scope="s">{{ s.row.image.id }}</template>
          </el-table-column>
          <el-table-column label="标签" min-width="180">
            <template slot-scope="s">
              <el-tag
                  v-for="(t, i) in s.row.tagList"
                  :key="i"
                  size="mini"
                  style="margin:2px 4px 2px 0">
                {{ t.name }}
              </el-tag>
              <span v-if="!s.row.tagList || !s.row.tagList.length" class="cm-muted">无标签</span>
            </template>
          </el-table-column>
          <el-table-column label="审核状态" width="110">
            <template slot-scope="s">
              <el-tag size="mini" :type="statusType(s.row.image.status)">
                {{ statusText(s.row.image.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="当前分类" width="110">
            <template slot-scope="s">{{ categoryName(s.row.image.categoryId) }}</template>
          </el-table-column>
          <el-table-column label="移到" width="200">
            <template slot-scope="s">
              <el-select v-model="rowTarget[s.row.image.id]" size="mini" placeholder="选择分类" style="width:110px">
                <el-option
                    v-for="c in otherCategories"
                    :key="c.id"
                    :label="c.name"
                    :value="c.id">
                </el-option>
              </el-select>
              <el-button
                  size="mini"
                  type="primary"
                  :disabled="!rowTarget[s.row.image.id]"
                  style="margin-left:6px"
                  @click="moveOne(s.row.image.id)">
                移动
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="cm-pagination">
          <el-pagination
              background
              :page-size="pagination.pageSize"
              :page-sizes="[5, 10, 20, 30]"
              :current-page="pagination.currentPage"
              layout="total, sizes, prev, pager, next"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange">
          </el-pagination>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  name: "CategoryManagement",
  data() {
    return {
      loading: false,
      categories: [],
      selectedCategoryId: null,
      imageTagList: [],
      selectedRows: [],
      rowTarget: {},
      batchTarget: null,
      pagination: { currentPage: 1, pageSize: 10, total: 0 }
    }
  },
  computed: {
    currentCategoryName() {
      const c = this.categories.find(x => x.id === this.selectedCategoryId)
      return c ? c.name : '-'
    },
    otherCategories() {
      return this.categories.filter(c => c.id !== this.selectedCategoryId)
    }
  },
  mounted() {
    this.loadCategories()
  },
  methods: {
    statusText(s) {
      return { 0: '待审核', 1: '已通过', 2: '已驳回' }[s] || '未知'
    },
    statusType(s) {
      return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
    },
    categoryName(id) {
      const c = this.categories.find(x => x.id === id)
      return c ? c.name : ('分类' + id)
    },
    loadCategories() {
      this.request.get('GetAllCategory').then(res => {
        const list = (res && res.categoryList) || []
        // 按 sort 升序，和后台上架顺序一致
        this.categories = list.slice().sort((a, b) => (a.sort || 0) - (b.sort || 0))
        if (!this.selectedCategoryId && this.categories.length) {
          this.selectCategory(this.categories[0].id)
        }
      })
    },
    selectCategory(id) {
      this.selectedCategoryId = id
      this.pagination.currentPage = 1
      this.selectedRows = []
      this.rowTarget = {}
      this.batchTarget = null
      this.loadImages()
    },
    loadImages() {
      if (!this.selectedCategoryId) return
      this.loading = true
      const url = `CategoryAdmin?action=images&categoryId=${this.selectedCategoryId}` +
          `&currentPage=${this.pagination.currentPage}&pageSize=${this.pagination.pageSize}`
      this.request.get(url).then(res => {
        this.imageTagList = (res && res.imageTagList) || []
        this.pagination.total = (res && res.total) || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    onSelectionChange(rows) {
      this.selectedRows = rows
    },
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.loadImages()
    },
    handleCurrentChange(page) {
      this.pagination.currentPage = page
      this.loadImages()
    },
    moveOne(imageId) {
      const target = this.rowTarget[imageId]
      if (!target) return
      this.doMove([imageId], target)
    },
    moveBatch() {
      if (!this.selectedRows.length || !this.batchTarget) return
      const ids = this.selectedRows.map(r => r.image.id)
      this.doMove(ids, this.batchTarget)
    },
    doMove(ids, targetCategoryId) {
      const url = `CategoryAdmin?action=move&imageIds=${ids.join(',')}&categoryId=${targetCategoryId}`
      this.request.post(url).then(res => {
        if (res && res.flag === 0) {
          this.$message.success(res.message || '已移动')
          this.selectedRows = []
          this.rowTarget = {}
          this.batchTarget = null
          // 计数会变，分类和列表都刷新
          this.loadCategories()
          this.loadImages()
        } else {
          this.$message.error((res && res.message) || '移动失败')
        }
      })
    }
  }
}
</script>

<style scoped>
.cm-page { padding: 20px 24px; }
.cm-head h2 { margin: 0 0 6px; color: var(--text-primary); font-size: 18px; }
.cm-hint { margin: 0 0 18px; font-size: 13px; color: var(--text-muted); }
.cm-body { display: flex; gap: 18px; align-items: flex-start; }
.cm-cats {
  width: 200px; flex: 0 0 200px; display: flex; flex-direction: column; gap: 10px;
}
.cm-cat {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 14px; border: 1px solid var(--border-color);
  border-radius: 10px; cursor: pointer; transition: all .2s;
  background: var(--bg-card);
}
.cm-cat:hover { border-color: #0096fa; }
.cm-cat.active { border-color: #0096fa; background: rgba(0, 150, 250, .08); }
.cm-cat-name { font-size: 14px; color: var(--text-primary); font-weight: 600; }
.cm-cat-count {
  min-width: 26px; text-align: center; font-size: 12px;
  padding: 1px 7px; border-radius: 10px;
  background: var(--bg-hover); color: var(--text-secondary);
}
.cm-main { flex: 1; min-width: 0; }
.cm-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; margin-bottom: 12px; flex-wrap: wrap;
}
.cm-current { font-size: 13px; color: var(--text-secondary); }
.cm-current b { color: var(--text-primary); }
.cm-actions { display: flex; align-items: center; gap: 10px; }
.cm-thumb {
  width: 200px; height: 120px; object-fit: cover;
  display: block; border-radius: 6px;
}
.cm-muted { color: var(--text-muted); font-size: 12px; }
.cm-pagination { margin-top: 16px; text-align: center; }
</style>
