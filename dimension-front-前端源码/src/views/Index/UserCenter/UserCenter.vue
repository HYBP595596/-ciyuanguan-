<template>
  <div class="uc-page">
    <!-- ============ 顶部用户信息 ============ -->
    <div class="uc-header" :style="getCoverStyle()">
      <div class="user-avatar">
        <img v-if="user.avatar" :src="user.avatar" alt=""/>
        <div v-else class="user-avatar-dummy">
          {{ user.nickname ? user.nickname[0] : 'U' }}
        </div>
      </div>
      <div class="user-info">
        <div class="uc-info-panel">
          <div class="user-nickname">
            {{ user.nickname }}
            <el-tag v-if="user.artistStatus === 2" size="mini" type="warning" class="uc-artist-tag">
              认证画师{{ user.acceptCommission === 1 ? ' · 接稿中' : '' }}
            </el-tag>
          </div>
          <div class="user-description" :title="user.description">{{ user.description }}</div>
          <div class="uc-stats">
            <span class="uc-stat" @click="switchTab('followingTab')">
              <b>{{ followingCount }}</b> 关注
            </span>
            <span class="uc-stat" @click="switchTab('fansTab')">
              <b>{{ fansCount }}</b> 粉丝
            </span>
          </div>
        </div>
      </div>

      <!-- 通用私信入口：任何人的主页都能发起对话，不限于约稿 -->
      <div class="uc-actions" v-if="!isSelf">
        <el-button size="small" type="primary" icon="el-icon-chat-dot-round" @click="openChat">私信 TA</el-button>
      </div>
      <div class="uc-actions" v-else-if="user.artistStatus === 2">
        <el-button size="small" type="warning" @click="$router.push('/commission')">画师中心</el-button>
      </div>
    </div>

    <!-- ============ 标签页 ============ -->
    <div class="uc-body">
      <el-tabs v-model="activeUserTabName" @tab-click="clickTab">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="infoTab" style="height: 330px">
          <div class="info-pane">
            <el-form label-width="100px" label-position="left">
              <el-form-item label="账号">{{ user.username }}</el-form-item>
              <el-form-item label="性别">
                <el-tag v-show="user.sex == 0">男</el-tag>
                <el-tag type="danger" v-show="user.sex == 1">女</el-tag>
                <el-tag v-show="user.sex == 2">保密</el-tag>
              </el-form-item>
              <el-form-item label="出生日期">{{ format2(user.birthday) }}</el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 我的作品 -->
        <el-tab-pane label="我的作品" name="myImagesTab" v-if="isSelf">
          <el-empty v-if="myImageList && myImageList.length === 0" description="你还没有上传过作品哦"></el-empty>
          <div class="grid-list" v-else>
            <div class="grid-card" v-for="item in myImageList" :key="item.image.id">
              <div class="grid-image" style="position: relative;" @click="clickMyImage(item.image)">
                <img :src="$img(item.image.url)" alt=""/>
                <div v-if="item.image.status === 0" style="position:absolute; top:5px; right:5px; background:orange; color:#fff; padding:2px 6px; border-radius:4px; font-size:12px;">待审核</div>
                <div v-if="item.image.status === 2" style="position:absolute; top:5px; right:5px; background:red; color:#fff; padding:2px 6px; border-radius:4px; font-size:12px;">已驳回</div>
              </div>
              <div class="grid-stats">
                <i class="el-icon-medal"></i><span>{{ item.image.score }}</span>
                <i class="el-icon-star-off"></i><span>{{ item.image.collects }}</span>
                <i class="el-icon-view"></i><span>{{ item.image.views }}</span>
              </div>
              <div class="grid-tags">
                <el-tag v-for="(tag, i) in item.tagList" :key="i" size="mini" type="danger">{{ tag.name }}</el-tag>
              </div>
              <div class="grid-footer">
                <span class="edit-btn" @click="openEdit(item)">编辑</span>
                <span class="delete-btn" @click="deleteMyImage(item.image.id)">删除</span>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 收藏 -->
        <el-tab-pane label="收藏" name="collectTab" v-if="isSelf">
          <el-empty v-if="imageTagBookmarkList && imageTagBookmarkList.length == 0" :image-size="260" description="快去收藏喜欢的图片吧~~~"></el-empty>
          <div class="grid-list" v-else>
            <div class="grid-card" v-for="(item, index) in imageTagBookmarkList" :key="index">
              <div class="grid-image" @click="clickImage(item.image.id)">
                <img :src="$img(item.image.url)" alt=""/>
              </div>
              <div class="grid-stats">
                <i class="el-icon-medal"></i><span>{{ item.image.score }}</span>
                <i class="el-icon-star-off"></i><span>{{ item.image.collects }}</span>
                <i class="el-icon-view"></i><span>{{ item.image.views }}</span>
              </div>
              <div class="grid-tags">
                <el-tag v-for="(tag, i) in item.tagList" :key="i" size="mini" type="danger">{{ tag.name }}</el-tag>
              </div>
              <div class="grid-footer">
                <span class="collect-time">{{ format1(item.imageBookmark.createTime) }}</span>
                <el-popconfirm @confirm="clickCollectDelete(item.image.id)" title="确认删除该收藏？">
                  <span slot="reference" class="delete-btn">✕</span>
                </el-popconfirm>
              </div>
            </div>
          </div>
          <div class="pagination">
            <el-pagination background :hide-on-single-page="collectHide" :page-size="pageSize" @current-change="handleCurrentChange" :current-page="currentPage" layout="prev, pager, next" :total="total"></el-pagination>
          </div>
        </el-tab-pane>

        <!-- 历史 -->
        <el-tab-pane label="历史" name="historyTab" class="history" v-if="isSelf">
          <div class="grid-list" v-if="imageTagHistoryCategoryList && imageTagHistoryCategoryList.length">
            <div class="grid-card" v-for="(item, index) in imageTagHistoryCategoryList" :key="index">
              <div class="grid-time">{{ format1(item.watchHistory.createTime) }}</div>
              <div class="grid-image" @click="clickImage(item.image.id)">
                <img :src="$img(item.image.url)" alt=""/>
              </div>
              <div class="grid-stats">
                <i class="el-icon-medal"></i><span>{{ item.image.score }}</span>
                <i class="el-icon-star-off"></i><span>{{ item.image.collects }}</span>
                <i class="el-icon-view"></i><span>{{ item.image.views }}</span>
              </div>
              <div class="grid-tags">
                <el-tag v-for="(tag, i) in item.tagList" :key="i" size="mini" type="danger">{{ tag.name }}</el-tag>
              </div>
              <div class="grid-footer">
                <el-tag size="mini">{{ item.category.name }}</el-tag>
                <span class="delete-btn" @click="clickHistoryDelete(item.watchHistory.id)">✕</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无浏览历史"></el-empty>
          <div class="pagination">
            <el-pagination :hide-on-single-page="historyHide" background :page-size="historyPagination.pageSize" @current-change="handleCurrentChangeHistory" :current-page="historyPagination.currentPage" layout="prev, pager, next" :total="historyPagination.total"></el-pagination>
          </div>
        </el-tab-pane>

        <!-- 关注 -->
        <el-tab-pane label="关注" name="followingTab">
          <el-empty v-if="!followingList.length" description="还没有关注任何人"></el-empty>
          <div class="uc-user-grid" v-else>
            <div class="uc-user-card" v-for="u in followingList" :key="u.id" @click="clickUserCenter(u.id)">
              <img v-if="u.avatar" :src="u.avatar" alt=""/>
              <div v-else class="uc-user-dummy">{{ u.nickname ? u.nickname[0] : 'U' }}</div>
              <div class="uc-user-name">{{ u.nickname }}</div>
              <el-tag v-if="u.artistStatus === 2" size="mini" type="warning">画师</el-tag>
            </div>
          </div>
        </el-tab-pane>

        <!-- 粉丝 -->
        <el-tab-pane label="粉丝" name="fansTab">
          <el-empty v-if="!fansList.length" description="还没有粉丝"></el-empty>
          <div class="uc-user-grid" v-else>
            <div class="uc-user-card" v-for="u in fansList" :key="u.id" @click="clickUserCenter(u.id)">
              <img v-if="u.avatar" :src="u.avatar" alt=""/>
              <div v-else class="uc-user-dummy">{{ u.nickname ? u.nickname[0] : 'U' }}</div>
              <div class="uc-user-name">{{ u.nickname }}</div>
              <el-tag v-if="u.artistStatus === 2" size="mini" type="warning">画师</el-tag>
            </div>
          </div>
        </el-tab-pane>

        <!-- 修改信息 -->
        <el-tab-pane label="修改信息" name="updateTab" v-if="isSelf">
          <el-tabs tab-position="left" type="border-card" @tab-click="clickUpdate" style="min-height: 420px">
            <!-- 修改头像 -->
            <el-tab-pane label="修改头像">
              <div class="avatar-update">
                <div class="avatar-picker">
                  <el-upload ref="avatarUpload" :before-upload="beforeUpload" :on-success="userAvatarUploadSuccess" :show-file-list="false" :headers="uploadHeaders" :action="'/dimension_dev_01_war_exploded/UploadImageOrUserAvatar?type=avatar&userId=' + user.id" :limit="1">
                    <el-button type="success" icon="el-icon-picture">选择本地图片</el-button>
                  </el-upload>
                  <el-button type="primary" @click="updateUserAvatar">更新头像</el-button>
                </div>
                <div class="avatar-preview">
                  <img :src="selectImage" alt=""/>
                </div>
              </div>
            </el-tab-pane>

            <!-- 修改信息 -->
            <el-tab-pane label="修改信息" name="update-info-tab">
              <el-form :model="updateUserInfoForm" :rules="updateUserInfoFormRules" ref="updateUserInfoForm" label-width="100px" class="update-form">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="updateUserInfoForm.nickname" placeholder="昵称"></el-input>
                </el-form-item>
                <el-form-item label="我的签名" prop="description">
                  <el-input type="textarea" :rows="4" v-model="updateUserInfoForm.description" placeholder="我的签名"></el-input>
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                  <el-radio-group v-model="updateUserInfoForm.sex">
                    <el-radio label="0">男</el-radio>
                    <el-radio label="1">女</el-radio>
                    <el-radio label="2">保密</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="出生日期" prop="birthday">
                  <el-date-picker v-model="updateUserInfoForm.birthday" type="date" placeholder="选择日期" style="width: 100%"></el-date-picker>
                </el-form-item>
                <el-form-item label="画师认证">
                  <el-tag v-if="user.artistStatus === 2" type="success" effect="dark">已认证画师</el-tag>
                  <el-tag v-else-if="user.artistStatus === 1" type="warning">审核中，请耐心等待</el-tag>
                  <el-tag v-else-if="user.artistStatus === 3" type="danger">已被驳回</el-tag>
                  <el-button v-else type="primary" size="small" @click="applyArtistDialog = true">申请成为画师</el-button>
                  <div v-if="user.artistStatus === 3 && user.artistApplyDesc" style="font-size:12px; color:#999; margin-top:5px;">驳回原因：{{ user.artistApplyDesc }}</div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" style="width: 100%" @click="saveUserInfo">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改背景 -->
            <el-tab-pane label="修改背景">
              <div class="avatar-update">
                <div class="avatar-picker">
                  <el-upload ref="coverUpload" :before-upload="beforeUpload" :on-success="userCoverUploadSuccess" :show-file-list="false" :headers="uploadHeaders" :action="'/dimension_dev_01_war_exploded/UploadImageOrUserAvatar?type=cover&userId=' + user.id" :limit="1">
                    <el-button type="success" icon="el-icon-picture">选择本地图片</el-button>
                  </el-upload>
                  <el-button type="primary" @click="updateUserCover">更新背景</el-button>
                  <div style="margin-top: 20px;">
                    <span style="font-size: 14px; color: #666;">调整背景高度: {{ coverHeight }}px</span>
                    <el-slider v-model="coverHeight" :min="150" :max="1000" :step="10" @change="saveCoverHeight"></el-slider>
                  </div>
                </div>
                <div class="avatar-preview">
                  <img :src="selectCover" alt="" style="width: 300px; height: 150px; border-radius: 8px; object-fit: cover;" />
                </div>
              </div>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码">
              <el-form :model="updatePasswordForm" :rules="updatePasswordFormRules" ref="updatePasswordForm" label-width="100px" class="update-form">
                <el-form-item label="原密码" prop="originPassword"><el-input v-model="updatePasswordForm.originPassword" placeholder="原密码"></el-input></el-form-item>
                <el-form-item label="新密码" prop="newPassword"><el-input v-model="updatePasswordForm.newPassword" placeholder="新密码"></el-input></el-form-item>
                <el-form-item label="确认密码" prop="confirmNewPassword"><el-input v-model="updatePasswordForm.confirmNewPassword" placeholder="确认密码"></el-input></el-form-item>
                <el-form-item><el-button type="primary" style="width: 100%" @click="updatePassword">保存</el-button></el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 🚨 申请画师弹窗（移到了 uc-page 内部） -->
    <el-dialog title="申请成为画师" :visible.sync="applyArtistDialog" width="500px">
      <el-form :model="applyForm">
        <el-form-item label="申请说明/个人简介">
          <el-input type="textarea" :rows="4" v-model="applyForm.desc" placeholder="请简单介绍自己的擅长画风、过往作品经验等..."></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="applyArtistDialog = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </span>
    </el-dialog>

    <!-- 编辑我的作品：分类 + 标签 -->
    <el-dialog title="编辑作品" :visible.sync="editDialogVisible" width="460px">
      <el-form label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="editForm.categoryValueId" placeholder="请选择分类" filterable style="width:100%">
            <el-option
                v-for="c in editCategoryList"
                :key="c.id"
                :label="c.name"
                :value="c.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editForm.tagListValueIds" multiple filterable placeholder="请选择标签" style="width:100%">
            <el-option
                v-for="t in editTagList"
                :key="t.id"
                :label="t.name"
                :value="t.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="saveEdit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { format1, format2 } from '@/utils/time-format'

export default {
  name: "UserCenter",
  data() {
    return {
      collectHide: false,
      historyHide: false,
      activeUserTabName: "infoTab",
      user: {},
      myImageList: [], // 👈 我的作品列表
      imageTagBookmarkList: [],
      historyPagination: { currentPage: 1, pageSize: 8, total: 0 },
      pageSize: 8,
      currentPage: 1,
      total: 0,
      selectImage: '',
      updateUserInfoForm: { nickname: '', description: '', sex: '2', birthday: '' },
      updateUserInfoFormRules: { nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }] },
      updatePasswordForm: { originPassword: '', newPassword: '', confirmNewPassword: '' },
      updatePasswordFormRules: {
        originPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
        confirmNewPassword: [{ required: true, message: '请输入确认密码', trigger: 'blur' }]
      },
      imageTagHistoryCategoryList: [],
      // 编辑作品的弹窗
      editDialogVisible: false,
      editSaving: false,
      editFormLoaded: false,
      editForm: { imageId: null, categoryValueId: null, tagListValueIds: [] },
      editCategoryList: [],
      editTagList: [],
      isSelf: true,
      // 关注/粉丝联动展示
      followingCount: 0,
      fansCount: 0,
      followingList: [],
      fansList: [],
      selectCover: '',
      coverHeight: 260,

      // 🚨 画师申请弹窗的状态
      applyArtistDialog: false,
      applyForm: { desc: '' }
    }
  },
  mounted() {
    const loginUserStr = sessionStorage.getItem('user');
    if (!loginUserStr) {
      this.$router.push('/index-body');
      return;
    }

    const loginUser = JSON.parse(loginUserStr);
    const targetUserId = this.$route.query.userId;
    let profileId = loginUser.id;

    if (targetUserId && String(targetUserId) !== String(loginUser.id)) {
      this.isSelf = false;
      this.user = {};
      this.getTargetUserInfo(targetUserId);
      profileId = targetUserId;
    } else {
      this.isSelf = true;
      this.user = loginUser;
      this.selectImage = this.user.avatar;
      this.getTargetUserInfo(loginUser.id);
      this.handleRouter();
    }

    this.coverHeight = Number(sessionStorage.getItem('coverHeight')) || 220;
    if (this.isSelf) {
      this.selectCover = this.user.coverImage || '';
    }
    // 关注/粉丝联动数据（自己或他人主页都展示）
    this.loadFollowData(profileId)
  },
  computed: {
    // el-upload 直传不经过 axios 拦截器，必须自己带上登录凭证
    uploadHeaders() {
      const headers = {};
      const token = sessionStorage.getItem('token');
      if (token) headers['Authentication'] = token;
      const adminToken = sessionStorage.getItem('adminToken');
      if (adminToken) headers['AdminAuthentication'] = adminToken;
      return headers;
    }
  },
  methods: {
    // 加载关注数、粉丝数以及两个列表
    loadFollowData(userId) {
      if (!userId) return
      this.request.get(`Follow?action=counts&userId=${userId}`).then(res => {
        this.followingCount = (res && res.followingCount) || 0
        this.fansCount = (res && res.fansCount) || 0
      })
      this.request.get(`Follow?action=following&userId=${userId}`).then(res => {
        this.followingList = (res && res.userList) || []
      })
      this.request.get(`Follow?action=fans&userId=${userId}`).then(res => {
        this.fansList = (res && res.userList) || []
      })
    },
    switchTab(name) {
      this.activeUserTabName = name
    },
    clickUserCenter(userId) {
      this.$router.push({ path: '/user-center', query: { userId } })
    },
    format1(t) { return format1(t) },
    format2(t) { return format2(t) },

    // 通用私信：从任何人的主页发起对话
    openChat() {
      const me = sessionStorage.getItem('user')
      if (!me) {
        this.$message.warning('请先登录')
        return
      }
      const self = JSON.parse(me)
      if (String(self.id) === String(this.user.id)) return
      this.$router.push({ path: '/messages', query: { peerId: this.user.id } })
    },

    beforeUpload() { this.$message.info('正在上传中，请稍等...') },

    clickUpdate(tab) {
      if (tab.name == 'update-info-tab') {
        this.updateUserInfoForm = { ...this.user }
      }
    },

    // 获取用户信息
    getTargetUserInfo(userId) {
      this.request.get(`GetUserInfo?userId=${userId}`).then(res => {
        this.user = res.user || res;
        this.selectImage = this.user.avatar;
        if (this.isSelf) {
          this.selectCover = this.user.coverImage || '';
        }
        // 背景高度以数据库为准（原来只存 sessionStorage，换会话就丢）
        if (this.user.coverHeight) {
          this.coverHeight = this.user.coverHeight;
          sessionStorage.setItem('coverHeight', this.coverHeight);
        }
        if (!this.isSelf) this.activeUserTabName = 'infoTab';
      }).catch(() => {
        this.$message.error('获取用户信息失败');
        this.$router.push('/index-body');
      });
    },

    // 🚨 提交画师申请
    submitApply() {
      if (!this.applyForm.desc) return this.$message.warning('请填写申请说明');
      this.request.post(`ApplyArtist?userId=${this.user.id}&desc=${this.applyForm.desc}`).then(res => {
        if (res.flag === 0) {
          this.$message.success('申请已提交，请等待审核');
          this.applyArtistDialog = false;
          // 重新获取信息刷新页面
          this.getTargetUserInfo(this.user.id);
        }
      })
    },

    // 🚨 我的作品逻辑
    getMyImages() {
      if (!this.user.id) return;
      this.request.get(`GetUserImageList?userId=${this.user.id}`).then(res => {
        if (res.flag === 0) {
          this.myImageList = res.imageList;
        }
      });
    },
    deleteMyImage(imageId) {
      this.$confirm('确认删除这张作品吗？', '提示', { type: 'warning' }).then(() => {
        this.request.post(`RemoveImageById?imageId=${imageId}&userId=${this.user.id}`).then(res => {
          if (res && res.flag === 0) {
            this.$message.success('删除成功');
            this.getMyImages();
          } else {
            this.$message.error((res && res.message) || '删除失败');
          }
        });
      });
    },
    // 编辑自己的作品：改分类和标签（复用后台的 UpdateImage，带 userId 做归属校验）
    openEdit(item) {
      if (!this.editFormLoaded) {
        this.request.get('GetAllCategory').then(res => {
          this.editCategoryList = (res && res.categoryList) || [];
        });
        this.request.get('GetAllTag').then(res => {
          this.editTagList = (res && res.tagList) || [];
        });
        this.editFormLoaded = true;
      }
      this.editForm = {
        imageId: item.image.id,
        categoryValueId: item.image.categoryId,
        tagListValueIds: (item.tagList || []).map(t => t.id)
      };
      this.editDialogVisible = true;
    },
    saveEdit() {
      if (!this.editForm.categoryValueId) return this.$message.warning('请选择分类');
      this.editSaving = true;
      const payload = {
        imageId: this.editForm.imageId,
        categoryValueId: this.editForm.categoryValueId,
        tagListValueIds: this.editForm.tagListValueIds,
        userId: this.user.id
      };
      this.request.post('UpdateImage', payload).then(res => {
        this.editSaving = false;
        if (res && res.flag === 0) {
          this.$message.success('已保存');
          this.editDialogVisible = false;
          this.getMyImages();
        } else {
          this.$message.error((res && res.message) || '保存失败');
        }
      }).catch(() => { this.editSaving = false });
    },
    clickMyImage(image) {
      if (image.status === 0) return this.$message.warning('该作品正在审核中，暂不能查看详情');
      if (image.status === 2) return this.$message.error('该作品已被驳回，请修改后重新上传');
      this.$router.push({ path: '/image-detail', query: { imageId: image.id } });
    },

    // 背景图获取样式
    getCoverStyle() {
      let styleObj = { height: this.coverHeight + 'px' };
      if (this.user && this.user.coverImage) {
        styleObj.backgroundImage = `url("${this.user.coverImage}")`;
        styleObj.backgroundSize = 'cover';
        styleObj.backgroundPosition = 'center';
        styleObj.backgroundRepeat = 'no-repeat';
        styleObj.border = 'none';
      } else {
        styleObj.backgroundImage = 'none';
        styleObj.backgroundColor = 'var(--bg-hover)';
        styleObj.border = '1px solid var(--border-color)';
      }
      return styleObj;
    },

    // 其他保留的方法...
    handleCurrentChangeHistory(newPage) { this.historyPagination.currentPage = newPage; this.getHistory(); },
    clickHistoryDelete(id) { this.request.post(`RemoveWatchHistoryById?id=${id}`).then(() => { this.$message.success('删除成功'); this.getHistory(); }) },
    updatePassword() {
      this.$refs['updatePasswordForm'].validate(valid => {
        if (!valid) return
        if (this.updatePasswordForm.newPassword != this.updatePasswordForm.confirmNewPassword) return this.$message.error('两次输入密码不一致')
        if (this.updatePasswordForm.originPassword != this.user.password) return this.$message.error('密码错误')
        this.request.post(`UpdateUserPassword?userId=${this.user.id}&password=${this.updatePasswordForm.newPassword}`).then(() => {
          this.$message.success({ message: '修改成功，请重新登录', duration: 1000 })
          setTimeout(() => { sessionStorage.clear(); this.event.$emit('popLoginForm') }, 1000)
        })
      })
    },
    saveUserInfo() {
      this.$refs['updateUserInfoForm'].validate(valid => {
        if (!valid) return
        this.updateUserInfoForm.birthday = new Date(this.updateUserInfoForm.birthday).toLocaleDateString().replace(/\//g, "-")
        this.request.post(`UpdateUserInfo`, { ...this.updateUserInfoForm, userId: this.user.id }).then(() => {
          this.user.nickname = this.updateUserInfoForm.nickname
          this.user.description = this.updateUserInfoForm.description
          this.user.sex = this.updateUserInfoForm.sex
          this.user.birthday = this.updateUserInfoForm.birthday
          sessionStorage.setItem('user', JSON.stringify(this.user))
          this.$message.success({ message: '更新成功，即将刷新页面', duration: 1000 })
          setTimeout(() => location.reload(), 1000)
        })
      })
    },
    updateUserAvatar() {
      const safeAvatar = encodeURIComponent(this.selectImage);
      this.request.post(`UpdateUserAvatar?userId=${this.user.id}&avatar=${safeAvatar}`).then(() => {
        this.user.avatar = this.selectImage;
        sessionStorage.setItem('user', JSON.stringify(this.user));
        this.event.$emit('user-info-updated', this.user);
        this.$message.success('头像更新成功！');
      })
    },
    userAvatarUploadSuccess(res) { this.$message.success('上传成功'); this.selectImage = res.url; this.$refs.avatarUpload.clearFiles() },
    handleCurrentChange(currentPage) { this.currentPage = currentPage; this.getImageBookmarkList() },
    clickCollectDelete(imageId) {
      this.request.post(`RemoveImageBookmark?imageId=${imageId}&userId=${this.user.id}`).then(() => { this.$message.success('删除成功'); this.getImageBookmarkList() })
    },
    clickImage(imageId) { this.$router.push({ path: '/image-detail', query: { imageId, highlightCategoryId: '-1' } }) },
    getImageBookmarkList() {
      this.request.get(`GetUserImageBookmarkList?userId=${this.user.id}&currentPage=${this.currentPage}&pageSize=${this.pageSize}`).then(res => {
        this.imageTagBookmarkList = res.imageTagBookmarkList
        if (this.imageTagBookmarkList && this.imageTagBookmarkList.length <= 8) this.collectHide = true
        this.total = res.total
      })
    },
    clickTab(tab) {
      if (tab.name == 'collectTab') {
        if (!this.imageTagBookmarkList || this.imageTagBookmarkList.length == 0) { this.currentPage = 1; this.getImageBookmarkList() }
      } else if (tab.name == 'historyTab') {
        this.historyPagination.currentPage = 1; this.imageTagHistoryCategoryList = []; this.getHistory()
      } else if (tab.name == 'myImagesTab') {
        this.getMyImages()
      }
    },
    handleRouter() {
      const activeUserTabName = this.$route.query.activeUserTabName
      if (activeUserTabName) this.activeUserTabName = activeUserTabName
      if (activeUserTabName == 'collectTab') this.getImageBookmarkList()
      else if (activeUserTabName == 'historyTab') this.getHistory()
      else if (activeUserTabName == 'myImagesTab') this.getMyImages()
    },
    getHistory() {
      this.request.get(`GetUserWatchHistory?userId=${this.user.id}&currentPage=${this.historyPagination.currentPage}&pageSize=${this.historyPagination.pageSize}`).then(res => {
        this.imageTagHistoryCategoryList = res.imageTagHistoryCategoryList
        if (this.imageTagHistoryCategoryList && this.imageTagHistoryCategoryList.length <= 8) this.historyHide = true
        this.historyPagination.total = res.total
      })
    },
    userCoverUploadSuccess(res) { this.$message.success('背景上传成功，请点击更新背景'); this.selectCover = res.url; this.$refs.coverUpload.clearFiles() },
    updateUserCover() {
      const safeCover = encodeURIComponent(this.selectCover);
      this.request.post(`UpdateUserCover?userId=${this.user.id}&coverImage=${safeCover}&coverHeight=${this.coverHeight}`).then(() => {
        this.user.coverImage = this.selectCover;
        this.user.coverHeight = this.coverHeight;
        sessionStorage.setItem('user', JSON.stringify(this.user));
        sessionStorage.setItem('coverHeight', this.coverHeight);
        this.event.$emit('user-info-updated', this.user);
        this.$message.success('背景更新成功！');
      });
    },
    // 拖动滑块松手即保存高度（不需要重新选图，也不会因为换会话丢失）
    saveCoverHeight(val) {
      if (!this.isSelf || !this.user || !this.user.id) return;
      const cover = encodeURIComponent(this.user.coverImage || this.selectCover || '');
      this.request
          .post(`UpdateUserCover?userId=${this.user.id}&coverImage=${cover}&coverHeight=${val}`)
          .then(() => {
            this.user.coverHeight = val;
            sessionStorage.setItem('coverHeight', val);
            sessionStorage.setItem('user', JSON.stringify(this.user));
            this.event.$emit('user-info-updated', this.user);
            this.$message.success('背景高度已保存');
          });
    }
  }
}
</script>

<style scoped src="./UserCenter.css"></style>