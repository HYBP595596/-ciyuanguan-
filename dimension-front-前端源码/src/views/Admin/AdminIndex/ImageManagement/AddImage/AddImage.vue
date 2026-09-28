<template>
  <!--新增图片页-->
  <div class="body" style="margin-top: 5%">
    <!--表单-->
    <div class="add-image-form" style="margin-left: 15%">
      <el-form :model="addImageForm" ref="addImageForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="图片">
<!--          action="http://localhost:9000/dimension/UploadImageOrUserAvatar"-->
          <el-upload
              class="avatar-uploader"
              action="http://47.113.192.130:9090/dimension/UploadImageOrUserAvatar"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleAvatarSuccess">
            <div style="border: 1px dashed #ccc; height: 200px; width: 200px">
              <img v-if="imageUrl" :src="imageUrl" class="avatar" style="height: 400px; width: 650px">
<!--              <span style="font-size: 100px">+</span>-->
              <i v-else class="el-icon-plus avatar-uploader-icon" style="text-align: center; line-height: 200px; font-size: 50px; color: #ccc"></i>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类">
          <el-select style="width: 300px" v-model="addImageForm.categoryValueId" filterable placeholder="请选择分类">
            <el-option
                v-for="(item, index) in categoryList"
                :key="index"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select style="width: 300px" v-model="addImageForm.tagListValueIds" filterable multiple placeholder="请选择标签">
            <el-option
                v-for="(item, index) in tagList"
                :key="index"
                :label="item.name"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <!--按钮-->
          <div class="save-button">
            <el-button type="primary" style="width: 300px" @click="save">新增</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: "AddImage",
  data() {
    return {
      addImageForm: {url: '', categoryValueId: '', tagListValueIds: []},
      imageUrl: '',
      categoryList: [],
      tagList: []
    }
  },
  mounted() {
    this.getCategoryList()
    this.getTagList()
  },
  methods: {
    beforeUpload() {
      this.$message.info('正在上传中，请稍等...')
    },
    // 保存，新增图片
    save() {
      if (this.imageUrl == '') {
        this.$message.error('请上传图片')
        return
      }
      this.addImageForm.url = this.imageUrl
      this.request.post(`AddImage`, this.addImageForm).then(res => {
        this.$message.success('新增成功')
        // 继续新增还是跳到管理页
        // this.$confirm('继续新增 or 回到图片管理页？', '提示', {
        //   confirmButtonText: '回到首页',
        //   cancelButtonText: '继续新增'
        // }).then(() => {
        //   // 回到首页
        //   this.$router.push('image-management-image-management')
        // }).catch(() => {
        //   // 继续新增 - 清空数据
        //   this.addImageForm.url = ''
        //   this.addImageForm.categoryId = ''
        //   this.addImageForm.tagIds = []
        //   // 清空图片
        //   this.imageUrl = ''
        // })
        this.imageUrl = ''
        this.addImageForm = {url: '', categoryValueId: '', tagListValueIds: []}
      })
    },
    // 上传文件成功
    handleAvatarSuccess(res) {
      console.log('image', res)
      this.imageUrl = res.url
      this.$message.success('上传成功')
    },
    // 获取所有分类
    getCategoryList() {
      this.request.get(`GetAllCategory`).then(res => {
        this.categoryList = res.categoryList
      })
    },
    // 获取所有标签
    getTagList() {
      this.request.get(`GetAllTag`).then(res => {
        this.tagList = res.tagList
      })
    }
  }
}
</script>

<style scoped>

</style>