<template>
  <!--新增图片页-->
  <div class="body">
    <!--表单-->
    <div class="add-image-form">
      <el-form :model="addImageForm" ref="addImageForm" label-width="100px" class="demo-ruleForm">
        <el-form-item label="图片">
<!--          action="http://localhost:9000/dimension_dev_01_war_exploded/UploadImageOrUserAvatar"-->
          <el-upload
              class="avatar-uploader"
              action="/dimension_dev_01_war_exploded/UploadImageOrUserAvatar?type=illust"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :on-success="handleAvatarSuccess">
            <div class="avatar-uploader-box">
              <img v-if="imageUrl" :src="imageUrl" class="avatar-preview">
<!--              <span style="font-size: 100px">+</span>-->
              <i v-else class="el-icon-plus avatar-uploader-icon" style="font-size: 50px; color: #ccc"></i>
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
  computed: {
    // 上传接口开启了鉴权，后台靠管理员 token 通过
    uploadHeaders() {
      const headers = {}
      const adminToken = sessionStorage.getItem('adminToken')
      if (adminToken) headers['AdminAuthentication'] = adminToken
      const token = sessionStorage.getItem('token')
      if (token) headers['Authentication'] = token
      return headers
    }
  },
  methods: {
    beforeUpload() {
      this.$message.info('正在上传中，请稍等...')
    },
    // 保存，新增图片
    // 保存，新增图片
save() {
  if (this.imageUrl == '') {
    this.$message.error('请上传图片')
    return
  }
  this.addImageForm.url = this.imageUrl
  
  // 👇 新增：从 sessionStorage 拿到当前登录用户，把 id 塞进表单
  const userStr = sessionStorage.getItem('user')
  if (!userStr) {
    this.$message.error('请先登录')
    return
  }
  const user = JSON.parse(userStr)
  this.addImageForm.userId = user.id 
  // 👆

  this.request.post(`AddImage`, this.addImageForm).then(res => {
    this.$message.success('新增成功')
    this.imageUrl = ''
    // 提交完之后记得把 userId 也重置清空
    this.addImageForm = {url: '', categoryValueId: '', tagListValueIds: [], userId: ''} 
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

<style scoped src="./AddImage.css">

</style>