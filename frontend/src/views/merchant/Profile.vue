<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商户中心</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="店铺信息" name="shop">
          <el-form :model="form" label-width="100px" style="max-width: 600px">
            <el-form-item label="店铺头像">
               <el-upload
                class="avatar-uploader"
                action="http://localhost:8080/file/upload"
                name="file"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="form.shopAvatar" :src="form.shopAvatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="店铺名称">
              <el-input v-model="form.shopName" />
            </el-form-item>
            <el-form-item label="店铺简介">
              <el-input v-model="form.shopIntro" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="取货地点">
              <el-input v-model="form.pickupLocation" placeholder="请设置校内固定取货点" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" disabled>
                <template #append>
                   <el-button @click="phoneDialogVisible = true">换绑</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存修改</el-button>
              <el-button type="success" @click="contactAdmin">联系管理员</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="经营看板" name="dashboard">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card shadow="hover">
                <template #header>本月销量</template>
                <div class="stat-num">{{ stats.sales || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <template #header>待处理订单</template>
                <div class="stat-num warning">{{ stats.pendingOrders || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <template #header>待响应需求</template>
                <div class="stat-num info">{{ stats.pendingRequests || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover">
                <template #header>店铺评分</template>
                <div class="stat-num success">{{ stats.rating || '-' }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <el-form :model="pwdForm" label-width="100px" style="max-width: 500px">
            <el-form-item label="旧密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="changePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Phone Dialog -->
    <el-dialog v-model="phoneDialogVisible" title="更换手机号">
      <el-form :model="phoneForm">
        <el-form-item label="新手机号">
          <el-input v-model="phoneForm.newPhone" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="phoneForm.code" placeholder="Mock: 123456" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="phoneDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="rebindPhone">确认更换</el-button>
      </template>
    </el-dialog>

    <!-- Support Dialog -->
    <el-dialog v-model="supportDialogVisible" title="联系客服" width="400px">
       <div style="margin-bottom: 20px; text-align: center;" v-if="systemPhone">
          <p style="margin-bottom: 10px; color: #666;">平台客服热线</p>
          <h2 style="color: #409EFF; margin: 0;">{{ systemPhone }}</h2>
       </div>
       <el-divider v-if="systemPhone">或 在线咨询</el-divider>
       <el-table :data="supportList" @row-click="selectSupport" style="cursor: pointer">
          <el-table-column label="客服" width="80">
             <template #default="scope">
                <el-avatar size="small" :src="scope.row.avatar" />
             </template>
          </el-table-column>
          <el-table-column prop="realName" label="姓名" />
          <el-table-column prop="role" label="角色">
             <template #default="scope">
                <el-tag size="small">{{ scope.row.role === 'admin' ? '管理员' : '客服' }}</el-tag>
             </template>
          </el-table-column>
       </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('shop')

const form = ref({
  shopAvatar: '',
  shopName: '',
  shopIntro: '',
  pickupLocation: '',
  phone: ''
})

const stats = ref({})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const phoneDialogVisible = ref(false)
const phoneForm = reactive({
  newPhone: '',
  code: ''
})

onMounted(() => {
  initData()
  fetchStats()
})

const initData = () => {
  const user = userStore.user
  form.value = {
    shopAvatar: user.shopAvatar,
    shopName: user.shopName,
    shopIntro: user.shopIntro,
    pickupLocation: user.pickupLocation,
    phone: user.phone
  }
}

const fetchStats = async () => {
  try {
    const res = await request.get('/user/merchant/stats', {
      params: { merchantId: userStore.user.merchantId }
    })
    stats.value = res
  } catch (e) {
    // ignore
  }
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200 || res.code === '200') {
    form.value.shopAvatar = res.data
  } else {
    ElMessage.error('上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImg = file.type.startsWith('image/')
  if (!isImg) ElMessage.error('只能上传图片')
  return isImg
}

const updateProfile = async () => {
  try {
    const data = {
      merchantId: userStore.user.merchantId,
      shopName: form.value.shopName,
      shopAvatar: form.value.shopAvatar,
      shopIntro: form.value.shopIntro,
      pickupLocation: form.value.pickupLocation
    }
    const res = await request.post('/user/merchant/update', data)
    userStore.setUser(res, 'merchant')
    ElMessage.success('店铺信息已更新')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

const changePassword = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  try {
    await request.post('/user/password/change', {
      userId: userStore.user.merchantId,
      role: 'merchant',
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    window.location.reload()
  } catch (e) {
    // handled
  }
}

const rebindPhone = async () => {
  try {
    await request.post('/user/phone/rebind', {
      userId: userStore.user.merchantId,
      role: 'merchant',
      newPhone: phoneForm.newPhone,
      code: phoneForm.code
    })
    ElMessage.success('手机号已更新')
    form.value.phone = phoneForm.newPhone
    phoneDialogVisible.value = false
    const user = userStore.user
    user.phone = phoneForm.newPhone
    userStore.setUser(user, 'merchant')
  } catch (e) {
    // handled
  }
}

const supportDialogVisible = ref(false)
const supportList = ref([])
const systemPhone = ref('')

const contactAdmin = async () => {
  try {
     const pRes = await request.get('/auth/system/phone')
     systemPhone.value = pRes

     const res = await request.get('/user/support/list')
     supportList.value = res || []
     supportDialogVisible.value = true
  } catch(e) {
    ElMessage.error('无法获取客服信息')
  }
}

const callPhone = () => {
    window.location.href = `tel:${systemPhone.value}`
}

const selectSupport = (row) => {
  supportDialogVisible.value = false
  router.push({
    path: '/chat',
    query: { targetId: row.id, role: 3 }
  })
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
  border-radius: 50%;
}
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}
.stat-num {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}
.stat-num.warning { color: #E6A23C; }
.stat-num.info { color: #909399; }
.stat-num.success { color: #67C23A; }
</style>
