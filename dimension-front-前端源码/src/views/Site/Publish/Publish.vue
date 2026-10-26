<template>
  <div class="publish-page">
    <div class="publish-container">
      <!-- 左侧：图片上传区（插画最多 9 张；漫画最多 50 页且可拖动排序） -->
      <div class="upload-area">
        <div class="upload-list">
          <div
              class="upload-item"
              v-for="(item, index) in imageList"
              :key="item.key"
              :class="{
                'is-draggable': canDrag,
                'is-dragging': dragIndex === index,
                'is-drag-over': dragOverIndex === index && dragIndex !== index
              }"
              :draggable="canDrag"
              @dragstart="onDragStart(index, $event)"
              @dragover.prevent="onDragOver(index)"
              @dragleave="onDragLeave(index)"
              @drop.prevent="onDrop(index)"
              @dragend="onDragEnd">
            <img :src="item.url" class="preview-img"/>
            <span class="order-badge">{{ index + 1 }}</span>
            <span class="cover-badge" v-if="index === 0">封面</span>
            <span class="remove-btn" title="移除这张（同时删除已上传的文件）" @click="removeImage(index)">
              <i class="el-icon-close"></i>
            </span>
          </div>

          <!-- 上传中的占位方块，保证顺序可见 -->
          <div class="upload-item uploading" v-for="n in uploadingCount" :key="'uploading-' + n">
            <i class="el-icon-loading"></i>
          </div>

          <div class="upload-tile" v-if="canAddMore" @click="pickFiles">
            <div class="upload-placeholder">
              <i class="el-icon-plus"></i>
              <p>添加图片</p>
              <span>{{ limitText }}</span>
            </div>
          </div>

          <input
              ref="fileInput"
              type="file"
              accept="image/png,image/jpeg,image/jpg,image/gif,image/webp"
              multiple
              class="hidden-file-input"
              @change="onFilesPicked"/>
        </div>

        <p class="upload-hint" v-if="imageList.length">
          <template v-if="isComic">
            第 1 页为封面，最多 {{ maxImages }} 页；<b>按住图片拖动可调整顺序</b>，点 × 可单独移除
          </template>
          <template v-else>
            第 1 张为封面，最多 {{ maxImages }} 张；点 × 可单独移除
          </template>
        </p>
      </div>

      <!-- 右侧：表单区 -->
      <div class="form-area">
        <h2>发布作品</h2>
        <p class="subtitle">提交后将进入审核，请耐心等待</p>

        <el-form :model="form" label-position="top">
          <!-- 作品类型：改成 Tab 按钮 -->
          <el-form-item label="作品类型">
            <div class="type-tabs">
              <div
                  class="type-btn"
                  :class="{ active: form.categoryValueId === 1 }"
                  @click="form.categoryValueId = 1">
                插画
              </div>
              <div
                  class="type-btn"
                  :class="{ active: form.categoryValueId === 2 }"
                  @click="form.categoryValueId = 2">
                漫画
              </div>
              <div
                  class="type-btn"
                  :class="{ active: form.categoryValueId === 3 }"
                  @click="form.categoryValueId = 3">
                小说
              </div>
            </div>
            <span class="hint">如果不确定，请选择插画</span>
          </el-form-item>

          <!-- 小说：需要正文 .txt -->
          <el-form-item label="小说正文（.txt）" v-if="isNovel">
            <div class="novel-upload">
              <el-button size="small" type="primary" :loading="novelUploading" @click="pickNovelFile">
                {{ novelUrl ? '重新选择 TXT' : '选择 TXT 文件' }}
              </el-button>
              <span class="novel-file" v-if="novelName">{{ novelName }}</span>
              <span class="novel-file" v-else-if="novelUrl">已上传正文</span>
              <input
                  ref="novelInput"
                  type="file"
                  accept=".txt,text/plain"
                  class="hidden-file-input"
                  @change="onNovelPicked"/>
            </div>
            <span class="hint">小说：.txt 正文必填；左侧封面可留空（无封面时全站显示纯白）</span>
          </el-form-item>

          <!-- 标签：开启 allow-create，回车即创建 -->
          <el-form-item label="作品标签">
            <el-select
                ref="tagSelect"
                v-model="form.tagListValueIds"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入标签后按回车键创建（如：原创、二次元）"
                style="width: 100%"
                @keyup.native="captureTagInput"
                @change="handleTagChange">
              <el-option
                  v-for="item in tagList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
            <span class="hint">最多添加 10 个标签</span>
          </el-form-item>

          <div class="action-buttons">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" @click="submitPublish" :loading="isSubmitting">确认发布</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Publish",
  data() {
    return {
      imageList: [],      // [{key, url, name}]，第 1 张作为封面，其余写入 extraImages
      uploadQueue: [],    // 待上传文件队列（串行消费，保证顺序 = 选择顺序）
      uploadingCount: 0,  // 正在上传的数量
      uploading: false,   // 是否正在处理队列
      dragIndex: -1,      // 拖动源下标（仅漫画可拖）
      dragOverIndex: -1,  // 拖动目标下标
      novelUrl: '',       // 小说正文 .txt 的地址
      novelName: '',      // 小说正文文件名（仅展示）
      novelUploading: false,
      categoryList: [], // 暂时保留，硬编码在模板中了
      tagList: [],      // 数据库里已有的标签
      form: {
        categoryValueId: 1, // 默认插画
        tagListValueIds: []
      },
      isSubmitting: false,
      // 影子副本：el-select 在下拉关闭时会把 query 清空，
      // 所以要在用户输入的过程中就把文字存下来，否则点“确认发布”时会丢
      pendingTagText: ''
    }
  },
  computed: {
    // 分类 1=插画，2=漫画，3=小说
    isComic() {
      return this.form.categoryValueId === 2;
    },
    isNovel() {
      return this.form.categoryValueId === 3;
    },
    // 插画最多 9 张；漫画最多 50 页；小说只要 1 张封面
    maxImages() {
      if (this.isNovel) return 1;
      return this.isComic ? 50 : 9;
    },
    canAddMore() {
      return (this.imageList.length + this.uploadingCount) < this.maxImages;
    },
    // 只有漫画、且至少 2 张时才允许拖动排序
    canDrag() {
      return this.isComic && this.imageList.length > 1;
    },
    limitText() {
      const used = this.imageList.length + this.uploadingCount;
      if (this.isNovel) {
        return used + '/1 封面';
      }
      return this.isComic
          ? used + '/' + this.maxImages + ' 页'
          : used + '/' + this.maxImages + ' · JPG/PNG';
    }
  },
  watch: {
    // 切换作品类型后，若已有图片超过新类型的上限，清掉多出来的（含服务器文件）
    'form.categoryValueId'() {
      const limit = this.maxImages;
      if (this.imageList.length > limit) {
        const removed = this.imageList.splice(limit);
        removed.forEach(item => this.deleteRemoteFile(item.url));
        const typeName = this.isComic ? '漫画' : '插画';
        this.$message.warning(`${typeName}最多 ${limit} ${this.isComic ? '页' : '张'}，已移除超出的 ${removed.length} ${this.isComic ? '页' : '张'}`);
      }
      this.resetDragState();
    }
  },
  mounted() {
    if (!sessionStorage.getItem('token')) {
      this.$message.warning('请先登录再发布作品');
      this.$router.push('/index-body');
      return;
    }
    this.getTagList(); // 只获取标签了，不需要获取分类列表
  },
  methods: {
    makeKey() {
      return 'img-' + Date.now() + '-' + Math.random().toString(36).slice(2, 8);
    },
    pickFiles() {
      if (this.$refs.fileInput) {
        this.$refs.fileInput.click();
      }
    },
    // 选完文件先入队，再串行上传 —— 保证顺序就是用户选择/点击的顺序
    onFilesPicked(e) {
      const files = Array.from(e.target.files || []);
      e.target.value = ''; // 允许再次选择同一个文件
      if (!files.length) return;

      // 张数限制（插画 9 张 / 漫画 50 页）
      const remaining = this.maxImages - this.imageList.length - this.uploadingCount;
      let accepted = files;
      if (accepted.length > remaining) {
        accepted = accepted.slice(0, Math.max(0, remaining));
        const typeName = this.isComic ? '漫画' : '插画';
        const unit = this.isComic ? '页' : '张';
        this.$message.warning(
          `${typeName}最多 ${this.maxImages} ${unit}，已忽略超出的 ${files.length - accepted.length} ${unit}`
        );
      }
      if (!accepted.length) return;

      const valid = [];
      for (const file of accepted) {
        if (!/^image\//.test(file.type)) {
          this.$message.error(`「${file.name}」不是图片，已跳过`);
          continue;
        }
        if (file.size / 1024 / 1024 >= 200) {
          this.$message.error(`「${file.name}」超过 200MB，已跳过`);
          continue;
        }
        valid.push(file);
      }
      if (!valid.length) return;

      this.uploadQueue.push(...valid);
      this.processUploadQueue();
    },
    async processUploadQueue() {
      if (this.uploading) return;
      this.uploading = true;
      while (this.uploadQueue.length) {
        const file = this.uploadQueue.shift();
        this.uploadingCount++;
        try {
          const url = await this.uploadOne(file);
          this.imageList.push({ key: this.makeKey(), url: url, name: file.name });
        } catch (err) {
          const reason = err && err.message ? err.message : '上传失败，请重试';
          this.$message.error(`「${file.name}」${reason}`);
        } finally {
          this.uploadingCount--;
        }
      }
      this.uploading = false;
    },
    // 用 fetch + FormData：跨过 axios 默认的 JSON Content-Type，浏览器自动带 boundary
    uploadOne(file, type) {
      const uploadType = type || 'illust';
      const formData = new FormData();
      formData.append('file', file);
      // 上传接口已开启登录校验，必须手动带 token（fetch 不经过 axios 拦截器）
      const headers = {};
      const token = sessionStorage.getItem('token');
      if (token) {
        headers['Authentication'] = token;
      }
      const adminToken = sessionStorage.getItem('adminToken');
      if (adminToken) {
        headers['AdminAuthentication'] = adminToken;
      }
      return fetch(`/dimension_dev_01_war_exploded/UploadImageOrUserAvatar?type=${uploadType}`, {
        method: 'POST',
        headers: headers,
        body: formData
      }).then(resp => {
        if (resp.status === 401) throw new Error('登录已过期，请重新登录后再上传');
        if (resp.status === 403) throw new Error('不能操作其他账号的数据，请重新登录');
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        return resp.json();
      }).then(res => {
        if (!res || !res.url) throw new Error((res && res.message) || '返回缺少 url');
        return res.url;
      });
    },
    // ---------- 小说正文 .txt ----------
    pickNovelFile() {
      if (this.$refs.novelInput) {
        this.$refs.novelInput.click();
      }
    },
    onNovelPicked(e) {
      const file = (e.target.files || [])[0];
      e.target.value = '';
      if (!file) return;
      if (!/\.txt$/i.test(file.name) && !/^text\/plain/.test(file.type)) {
        this.$message.error('请选择 .txt 格式的正文文件');
        return;
      }
      // 5MB 依据：UTF-8 中文约 3 字节/字，5MB ≈ 170 万字，已远超一般小说；
      // 正文是整篇 fetch 到前端再分页的，再大浏览器解析也会卡
      if (file.size > 5 * 1024 * 1024) {
        this.$message.error('正文文件不能超过 5MB（约 170 万字）');
        return;
      }
      this.uploadNovelTxt(file);
    },
    uploadNovelTxt(file) {
      const oldUrl = this.novelUrl;
      this.novelUploading = true;
      this.uploadOne(file, 'novel')
          .then(url => {
            this.novelUrl = url;
            this.novelName = file.name;
            // 替换正文时删掉旧文件，避免留孤儿文件
            if (oldUrl) {
              this.deleteRemoteFile(oldUrl);
            }
            this.$message.success('正文上传成功');
          })
          .catch(err => this.$message.error(err && err.message ? err.message : '正文上传失败，请重试'))
          .finally(() => {
            this.novelUploading = false;
          });
    },
    // 移除某一张，同时把服务器上的那份文件删掉
    removeImage(index) {
      const removed = this.imageList.splice(index, 1)[0];
      if (removed) {
        this.deleteRemoteFile(removed.url);
      }
    },
    deleteRemoteFile(url) {
      if (!url) return;
      this.request.post(`RemoveUploadedImage?url=${encodeURIComponent(url)}`).catch(() => {
        // 删除失败不影响前端列表，只是服务器上会留一份孤儿文件
      });
    },
    // ---------- 拖动排序（仅漫画） ----------
    onDragStart(index, event) {
      if (!this.canDrag) return;
      this.dragIndex = index;
      this.dragOverIndex = index;
      try {
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('text/plain', String(index));
      } catch (e) {
        // 某些浏览器限制，忽略即可
      }
    },
    onDragOver(index) {
      if (!this.canDrag || this.dragIndex < 0) return;
      this.dragOverIndex = index;
    },
    onDragLeave(index) {
      if (this.dragOverIndex === index) {
        this.dragOverIndex = -1;
      }
    },
    onDrop(index) {
      if (this.canDrag) {
        const from = this.dragIndex;
        if (from >= 0 && from !== index) {
          const moved = this.imageList.splice(from, 1)[0];
          this.imageList.splice(index, 0, moved);
        }
      }
      this.resetDragState();
    },
    onDragEnd() {
      this.resetDragState();
    },
    resetDragState() {
      this.dragIndex = -1;
      this.dragOverIndex = -1;
    },
    getTagList() {
      this.request.get(`GetAllTag`).then(res => {
        this.tagList = res.tagList;
      })
    },
    // 每次按键都把输入框里的文字存一份，避免下拉失焦后被清空
    captureTagInput() {
      try {
        const sel = this.$refs.tagSelect;
        const q = sel && typeof sel.query === 'string' ? sel.query : '';
        this.pendingTagText = q;
      } catch (e) {
        // 拿不到就保持原值
      }
    },
    // 只做数量限制，真正的“新建标签”由后端 AddImage 处理（字符串会自动建 Tag）
    handleTagChange(val) {
      // 值已提交进 v-model（选中的数字或按回车创建的新标签），影子副本作废
      this.pendingTagText = '';
      if (val.length > 10) {
        this.$message.warning('最多只能添加 10 个标签');
        this.form.tagListValueIds = val.slice(0, 10);
      }
    },
    // 收集最终要提交的标签：已选中的 + 手打但没按回车的 + 去重去空
    collectTags() {
      let list = Array.isArray(this.form.tagListValueIds) ? this.form.tagListValueIds.slice() : [];
      // allow-create 模式下，输入后没按回车，值不会进 v-model，这里补上
      if (this.pendingTagText) {
        list.push(this.pendingTagText);
      }
      try {
        const sel = this.$refs.tagSelect;
        const pending = sel && typeof sel.query === 'string' ? sel.query.trim() : '';
        if (pending) {
          list.push(pending);
        }
      } catch (e) {
        // ref 拿不到就忽略
      }
      const seen = new Set();
      const result = [];
      for (let item of list) {
        if (item === null || item === undefined) continue;
        if (typeof item === 'string') {
          const name = item.trim();
          if (!name || seen.has('s:' + name)) continue;
          seen.add('s:' + name);
          result.push(name);
        } else {
          if (seen.has('n:' + item)) continue;
          seen.add('n:' + item);
          result.push(item);
        }
      }
      return result.slice(0, 10);
    },
    submitPublish() {
      // 小说允许没有封面（写入 null，前端用纯白占位），但正文必填
      if (!this.isNovel && !this.imageList.length) {
        return this.$message.error('请先上传作品图片');
      }
      if (this.isNovel && !this.novelUrl) {
        return this.$message.error('请上传小说正文（.txt）');
      }
      if (!this.form.categoryValueId) return this.$message.error('请选择作品类型');

      this.isSubmitting = true;
      const user = JSON.parse(sessionStorage.getItem('user'));

      // 封面可空：小说没传封面时就写 null
      const coverUrl = this.imageList.length ? this.imageList[0].url : null;

      // 第 1 张作为封面 url；其余仅插画/漫画写入 extraImages；小说正文写 txtUrl
      const submitData = {
        url: coverUrl,
        extraImages: this.isNovel ? '' : this.imageList.slice(1).map(item => item.url).join(','),
        txtUrl: this.isNovel ? this.novelUrl : '',
        categoryValueId: this.form.categoryValueId,
        tagListValueIds: this.collectTags(),
        userId: user.id
      };

      this.request.post(`AddImage`, submitData).then(res => {
        this.isSubmitting = false;
        if (res.flag === 0) {
          // 发布成功，清掉本次的图片、正文与标签状态
          this.pendingTagText = '';
          this.form.tagListValueIds = [];
          this.imageList = [];
          this.novelUrl = '';
          this.novelName = '';
          this.$message.success('发布成功！正在等待管理员审核');
          this.$router.push('/user-center?activeUserTabName=myImagesTab');
        } else {
          this.$message.error('发布失败，请重试');
        }
      }).catch(() => {
        this.isSubmitting = false;
      })
    }
  }
}
</script>

<style scoped>
/* 这里保留你之前的样式，我顺便补充几个新加的 */
.publish-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  padding: 40px;
  display: flex;
  justify-content: center;
}

.publish-container {
  display: flex;
  width: 100%;
  max-width: 1100px;
  background: var(--bg-card);
  border-radius: 12px;
  box-shadow: 0 4px 20px var(--shadow-color);
  overflow: hidden;
}

.upload-area {
  flex: 1;
  background: var(--bg-hover);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 24px;
  min-width: 0;
}

/* 多图列表：每张一个方块，末尾固定一个“添加图片”方块 */
.upload-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  align-content: flex-start;
  width: 100%;
}

.upload-item {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
}

.upload-item .preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0;
  box-shadow: none;
  display: block;
}

.cover-badge {
  position: absolute;
  left: 0;
  bottom: 0;
  padding: 1px 6px;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 150, 250, 0.9);
  border-top-right-radius: 6px;
}

.remove-btn {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.upload-item:hover .remove-btn {
  opacity: 1;
}

/* 序号角标（拖动排序时看顺序更直观） */
.order-badge {
  position: absolute;
  top: 3px;
  left: 3px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 4px;
  text-align: center;
  border-radius: 9px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 11px;
}

/* 上传中的占位块 */
.upload-item.uploading {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0096fa;
  font-size: 24px;
  border-style: dashed;
}

/* 拖动状态 */
.upload-item.is-draggable {
  cursor: grab;
}
.upload-item.is-dragging {
  opacity: 0.4;
}
.upload-item.is-drag-over {
  outline: 2px dashed #0096fa;
  outline-offset: -3px;
}

.upload-tile {
  cursor: pointer;
}

.hidden-file-input {
  display: none;
}

.upload-placeholder {
  width: 120px;
  height: 120px;
  box-sizing: border-box;
  border: 2px dashed var(--border-color);
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #0096fa;
  color: #0096fa;
}

.upload-placeholder i {
  font-size: 26px;
  margin-bottom: 4px;
}
.upload-placeholder p {
  margin: 0;
  font-size: 13px;
}
.upload-placeholder span {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.upload-hint {
  margin-top: 14px;
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}

/* 小说正文上传 */
.novel-upload {
  display: flex;
  align-items: center;
  gap: 12px;
}

.novel-file {
  font-size: 13px;
  color: var(--text-secondary);
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-img {
  object-fit: contain;
  border-radius: 8px;
}

.form-area {
  width: 400px;
  padding: 40px;
  display: flex;
  flex-direction: column;
}

.form-area h2 {
  margin: 0 0 8px 0;
  color: var(--text-primary);
}

.subtitle {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 30px;
}

.action-buttons {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 新加的样式 */
.type-tabs {
  display: flex;
  gap: 12px;
}
.type-btn {
  padding: 8px 24px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}
.type-btn.active {
  background-color: #0096fa;
  border-color: #0096fa;
  color: white;
  font-weight: bold;
}
.hint {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
  display: block;
}
</style>