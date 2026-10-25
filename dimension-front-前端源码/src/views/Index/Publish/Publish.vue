<template>
  <div class="publish-page">
    <div class="publish-container">
      <!-- 左侧：图片上传区 -->
      <div class="upload-area">
        <el-upload
            class="uploader"
            action="/dimension_dev_01_war_exploded/UploadImageOrUserAvatar?type=illust"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeUpload">
          <img v-if="imageUrl" :src="imageUrl" class="preview-img" />
          <div v-else class="upload-placeholder">
            <i class="el-icon-plus"></i>
            <p>点击上传作品</p>
            <span>支持 JPG, PNG 格式</span>
          </div>
        </el-upload>
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
            </div>
            <span class="hint">如果不确定，请选择插画</span>
          </el-form-item>

          <!-- 标签：开启 allow-create，回车即创建 -->
          <el-form-item label="作品标签">
            <el-select
                v-model="form.tagListValueIds"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入标签后按回车键创建（如：原创、二次元）"
                style="width: 100%"
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
      imageUrl: '',
      categoryList: [], // 暂时保留，硬编码在模板中了
      tagList: [],      // 数据库里已有的标签
      form: {
        categoryValueId: 1, // 默认插画
        tagListValueIds: []
      },
      isSubmitting: false
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
    beforeUpload(file) {
      const isLt50M = file.size / 1024 / 1024 < 200;
      if (!isLt50M) {
        this.$message.error('图片大小不能超过 200MB!');
      }
      return isLt50M;
    },
    handleAvatarSuccess(res) {
      if (res.url) {
        this.imageUrl = res.url;
        this.$message.success('图片上传成功');
      }
    },
    getTagList() {
      this.request.get(`GetAllTag`).then(res => {
        this.tagList = res.tagList;
      })
    },
    // 这里处理用户输入的“新标签”
    handleTagChange(val) {
      if (val.length > 10) {
        this.$message.warning('最多只能添加 10 个标签');
        this.form.tagListValueIds = val.slice(0, 10);
      }
      // 在真实开发中，这里需要把用户输入的字符串（如"原创"）发给后端，
      // 后端新建一个 Tag 存入数据库，并返回新的 tagId。
      // 为了目前测试，先让用户从下拉列表里选。
    },
    submitPublish() {
      if (!this.imageUrl) return this.$message.error('请先上传作品图片');
      if (!this.form.categoryValueId) return this.$message.error('请选择作品类型');

      this.isSubmitting = true;
      const user = JSON.parse(sessionStorage.getItem('user'));

      // 直接把混合了数字(老ID)和字符串(新标签)的数组传过去
      const submitData = {
        url: this.imageUrl,
        categoryValueId: this.form.categoryValueId,
        tagListValueIds: this.form.tagListValueIds,
        userId: user.id
      };

      this.request.post(`AddImage`, submitData).then(res => {
        this.isSubmitting = false;
        if (res.flag === 0) {
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
  align-items: center;
  justify-content: center;
  padding: 30px;
}

.uploader {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-placeholder {
  width: 400px;
  height: 400px;
  border: 2px dashed var(--border-color);
  border-radius: 12px;
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
  font-size: 48px;
  margin-bottom: 10px;
}
.upload-placeholder span {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 8px;
}

.preview-img {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
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