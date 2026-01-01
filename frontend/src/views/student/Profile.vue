<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <!-- Balance Card -->
      <div class="balance-card">
        <div class="balance-info">
          <span class="label">账户余额</span>
          <div class="amount-row">
             <span class="symbol">¥</span>
             <span class="amount">{{ balance.toFixed(2) }}</span>
             <el-button :icon="Refresh" circle size="small" @click="fetchBalance" class="refresh-btn" />
          </div>
        </div>
        <el-button type="primary" round class="recharge-btn" @click="rechargeDialogVisible = true">充值</el-button>
      </div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本资料" name="basic">
          <el-form :model="form" label-width="100px" style="max-width: 500px">
            <el-form-item label="头像">
               <el-upload
                class="avatar-uploader"
                action="http://localhost:8080/file/upload"
                name="file"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="form.avatar" :src="form.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" />
            </el-form-item>
            <el-form-item label="学号">
              <el-input v-model="form.schoolId" disabled />
              <span class="tip">关键信息不可修改</span>
            </el-form-item>
             <el-form-item label="手机号">
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

        <el-tab-pane label="地址管理" name="address">
          <div style="margin-bottom: 15px">
            <el-button type="primary" @click="openAddressDialog(null)">新增地址</el-button>
          </div>
          <el-table :data="addressList" border>
            <el-table-column label="地址详情" prop="detail" />
            <el-table-column label="联系人" prop="name" width="120" />
            <el-table-column label="电话" prop="phone" width="150" />
            <el-table-column label="默认" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.isDefault" type="success">是</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button link type="primary" size="small" @click="openAddressDialog(scope.row)">编辑</el-button>
                <el-button link type="danger" size="small" @click="deleteAddress(scope.$index)">删除</el-button>
                <el-button v-if="!scope.row.isDefault" link type="success" size="small" @click="setDefaultAddress(scope.$index)">设为默认</el-button>
              </template>
            </el-table-column>
          </el-table>
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

        <el-tab-pane label="数据概览" name="stats">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>历史订单</template>
                <div class="stat-num">{{ stats.orders || 0 }}</div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>发布需求</template>
                <div class="stat-num">{{ stats.requests || 0 }}</div>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="钱包记录" name="wallet">
           <el-table :data="rechargeHistory" border stripe>
              <el-table-column prop="createTime" label="时间" width="180">
                  <template #default="scope">{{ new Date(scope.row.createTime).toLocaleString() }}</template>
              </el-table-column>
              <el-table-column prop="amount" label="金额">
                  <template #default="scope">+{{ scope.row.amount }}</template>
              </el-table-column>
              <el-table-column prop="method" label="方式" />
              <el-table-column prop="status" label="状态">
                  <template #default="scope">
                      <el-tag type="success" v-if="scope.row.status === 1">成功</el-tag>
                      <el-tag type="danger" v-else>失败</el-tag>
                  </template>
              </el-table-column>
           </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的收藏" name="favorites">
            <div v-if="!favoriteList.length && !favoritesLoading" style="text-align: center; padding: 20px; color: #999;">
                暂无收藏记录
            </div>
            <el-table :data="favoriteList" border stripe v-loading="favoritesLoading" element-loading-text="加载中...">
                <el-table-column label="商品图片" width="100">
                    <template #default="scope">
                         <el-image 
                            :src="getProductImage(scope.row.images)" 
                            style="width: 60px; height: 60px"
                            fit="cover"
                         />
                    </template>
                </el-table-column>
                <el-table-column prop="name" label="商品名称" />
                <el-table-column prop="proxyPrice" label="价格" width="120">
                    <template #default="scope">¥{{ scope.row.proxyPrice }}</template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                    <template #default="scope">
                         <el-button link type="primary" size="small" @click="viewProduct(scope.row.productId)">查看</el-button>
                         <el-button link type="danger" size="small" @click="handleRemoveFavorite(scope.row.productId)">取消收藏</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Recharge Dialog -->
    <el-dialog v-model="rechargeDialogVisible" title="账户充值" width="400px">
      <div class="recharge-options">
        <div 
          v-for="amt in [50, 100, 200, 500]" 
          :key="amt" 
          class="amt-option"
          :class="{ active: rechargeForm.amount === amt && !rechargeForm.isCustom }"
          @click="selectAmount(amt)"
        >
          {{ amt }}元
        </div>
        <div 
           class="amt-option custom" 
           :class="{ active: rechargeForm.isCustom }"
           @click="rechargeForm.isCustom = true"
        >
           自定义
        </div>
      </div>
      
      <div v-if="rechargeForm.isCustom" style="margin-top: 15px">
         <el-input-number v-model="rechargeForm.customAmount" :min="10" :max="1000" placeholder="请输入金额" style="width: 100%" />
      </div>

      <div class="pay-methods" style="margin-top: 20px">
         <p>支付方式</p>
         <el-radio-group v-model="rechargeForm.method">
            <el-radio label="WeChat">微信支付</el-radio>
            <el-radio label="Alipay">支付宝</el-radio>
            <el-radio label="Bank">银行卡</el-radio>
         </el-radio-group>
      </div>

      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge" :loading="rechargeLoading">确认支付</el-button>
      </template>
    </el-dialog>

    <!-- Address Dialog -->
    <el-dialog v-model="addressDialogVisible" :title="currentAddressIndex === -1 ? '新增地址' : '编辑地址'">
      <el-form :model="addressForm">
        <el-form-item label="地址详情">
          <el-input v-model="addressForm.detail" placeholder="请输入宿舍楼号/教学楼等校内地址" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="addressForm.name" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="addressForm.phone" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

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
xiu       </div>
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
import { ref, onMounted, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { getFavoriteList, removeFavorite } from '@/api/favorite'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('basic')

const form = ref({
  avatar: '',
  nickname: '',
  schoolId: '',
  phone: ''
})

const addressList = ref([])
const stats = ref({})

// Wallet
const balance = ref(0)
const rechargeDialogVisible = ref(false)
const rechargeLoading = ref(false)
const rechargeHistory = ref([])
const rechargeForm = reactive({
    amount: 100,
    customAmount: 0,
    isCustom: false,
    method: 'WeChat'
})

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

const addressDialogVisible = ref(false)
const currentAddressIndex = ref(-1)
const addressForm = reactive({
  detail: '',
  name: '',
  phone: '',
  isDefault: false
})

// Favorite Logic
const favoriteList = ref([])
const favoritesLoading = ref(false)

const fetchFavorites = async () => {
    if (!userStore.user.studentId) {
        ElMessage.warning('用户信息不完整，请重新登录')
        return
    }
    
    favoritesLoading.value = true
    try {
        const res = await getFavoriteList(userStore.user.studentId, 1, 100)
        favoriteList.value = res.records || []
    } catch (e) {
        console.error(e)
        ElMessage.error('获取收藏列表失败')
    } finally {
        favoritesLoading.value = false
    }
}

const getProductImage = (imagesStr) => {
    if (!imagesStr) return ''
    try {
        // Try to parse as JSON array
        // Check if it looks like a JSON array first
        if (imagesStr.trim().startsWith('[')) {
             const arr = JSON.parse(imagesStr)
             return Array.isArray(arr) && arr.length > 0 ? arr[0] : ''
        }
        // If not JSON array, treat as direct string
        return imagesStr
    } catch (e) {
        // If parsing fails, assume it's a direct URL string
        return imagesStr
    }
}

const handleRemoveFavorite = async (productId) => {
    try {
        await removeFavorite(userStore.user.studentId, productId)
        ElMessage.success('已移除')
        fetchFavorites()
    } catch (e) {
        ElMessage.error('移除失败')
    }
}

const viewProduct = (id) => {
    if (!id) return
    router.push('/student/product/' + id)
}

watch(activeTab, (val) => {
    if (val === 'favorites') {
        fetchFavorites()
    }
})

onMounted(() => {
  initData()
  fetchStats()
  fetchBalance()
  fetchRechargeHistory()
})

const initData = () => {
  const user = userStore.user
  form.value = {
    avatar: user.avatar,
    nickname: user.nickname,
    schoolId: user.schoolId,
    phone: user.phone
  }
  try {
    addressList.value = JSON.parse(user.addressList || '[]')
  } catch (e) {
    addressList.value = []
  }
}

const fetchStats = async () => {
  try {
    const res = await request.get('/user/student/stats', {
      params: { studentId: userStore.user.studentId }
    })
    stats.value = res
  } catch (e) {
    // ignore
  }
}

// Wallet Functions
const fetchBalance = async () => {
    try {
        const res = await request.get('/student/balance', {
            params: { studentId: userStore.user.studentId }
        })
        balance.value = res || 0
    } catch(e) {}
}

const fetchRechargeHistory = async () => {
    try {
        const res = await request.get('/student/recharge/history', {
            params: { studentId: userStore.user.studentId }
        })
        rechargeHistory.value = res || []
    } catch(e) {}
}

const selectAmount = (amt) => {
    rechargeForm.amount = amt
    rechargeForm.isCustom = false
}

const handleRecharge = async () => {
    const finalAmount = rechargeForm.isCustom ? rechargeForm.customAmount : rechargeForm.amount
    if (finalAmount < 10 || finalAmount > 1000) {
        return ElMessage.warning('充值金额需在 10-1000 元之间')
    }
    
    rechargeLoading.value = true
    try {
        // Mock payment delay
        await new Promise(r => setTimeout(r, 1500))
        
        await request.post('/student/recharge', {
            studentId: userStore.user.studentId,
            amount: finalAmount,
            method: rechargeForm.method
        })
        
        ElMessage.success('充值成功')
        rechargeDialogVisible.value = false
        fetchBalance()
        fetchRechargeHistory()
    } catch(e) {
        ElMessage.error('充值失败')
    } finally {
        rechargeLoading.value = false
    }
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200 || res.code === '200') {
    form.value.avatar = res.data
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
      studentId: userStore.user.studentId,
      nickname: form.value.nickname,
      avatar: form.value.avatar,
      addressList: JSON.stringify(addressList.value)
    }
    const res = await request.post('/user/student/update', data)
    userStore.setUser(res, 'student')
    ElMessage.success('资料已更新')
  } catch (e) {
    ElMessage.error(e.message)
  }
}

// Address Logic
const openAddressDialog = (addr) => {
  if (addr) {
    // Edit
    currentAddressIndex.value = addressList.value.indexOf(addr)
    Object.assign(addressForm, addr)
  } else {
    // New
    currentAddressIndex.value = -1
    addressForm.detail = ''
    addressForm.name = ''
    addressForm.phone = ''
    addressForm.isDefault = false
  }
  addressDialogVisible.value = true
}

const saveAddress = () => {
  if (!addressForm.detail.includes('校') && !addressForm.detail.includes('楼') && !addressForm.detail.includes('舍')) {
    ElMessage.warning('请填写校内地址（如宿舍楼、教学楼）')
    // return // Strict check? Let's just warn for now.
  }
  
  const newAddr = { ...addressForm }
  
  if (newAddr.isDefault) {
    addressList.value.forEach(a => a.isDefault = false)
  }
  
  if (currentAddressIndex.value === -1) {
    addressList.value.push(newAddr)
  } else {
    addressList.value[currentAddressIndex.value] = newAddr
  }
  
  updateProfile() // Auto save profile to sync address
  addressDialogVisible.value = false
}

const deleteAddress = (index) => {
  addressList.value.splice(index, 1)
  updateProfile()
}

const setDefaultAddress = (index) => {
  addressList.value.forEach(a => a.isDefault = false)
  addressList.value[index].isDefault = true
  updateProfile()
}

// Security Logic
const changePassword = async () => {
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.error('两次密码输入不一致')
    return
  }
  try {
    await request.post('/user/password/change', {
      userId: userStore.user.studentId,
      role: 'student',
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
    // router push login handled by user store logout? no, manual
    window.location.reload()
  } catch (e) {
    // handled
  }
}

const rebindPhone = async () => {
  try {
    await request.post('/user/phone/rebind', {
      userId: userStore.user.studentId,
      role: 'student',
      newPhone: phoneForm.newPhone,
      code: phoneForm.code
    })
    ElMessage.success('手机号已更新')
    form.value.phone = phoneForm.newPhone
    phoneDialogVisible.value = false
    // Sync to store? Ideally re-fetch profile or update store manually
    const user = userStore.user
    user.phone = phoneForm.newPhone
    userStore.setUser(user, 'student')
  } catch (e) {
    // handled
  }
}

const supportDialogVisible = ref(false)
const supportList = ref([])
const systemPhone = ref('')

const contactAdmin = async () => {
  try {
     // Fetch phone
     const pRes = await request.get('/auth/system/phone')
     systemPhone.value = pRes

     const res = await request.get('/user/support/list')
     // console.log('Support List:', res)
     
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
.tip {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}
.stat-num {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}

/* Wallet Styles */
.balance-card {
    background: linear-gradient(135deg, #409EFF, #66b1ff);
    color: white;
    border-radius: 8px;
    padding: 20px 25px;
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.balance-info .label {
    font-size: 14px;
    opacity: 0.9;
    display: block;
    margin-bottom: 8px;
}

.amount-row {
    display: flex;
    align-items: baseline;
}

.amount-row .symbol {
    font-size: 20px;
    margin-right: 4px;
}

.amount-row .amount {
    font-size: 32px;
    font-weight: bold;
    margin-right: 10px;
}

.refresh-btn {
    background: rgba(255,255,255,0.2) !important;
    border: none !important;
    color: white !important;
}

.refresh-btn:hover {
    background: rgba(255,255,255,0.3) !important;
}

.recharge-btn {
    background: white !important;
    color: #409EFF !important;
    border: none !important;
    padding: 12px 25px;
    font-weight: bold;
}

.recharge-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
}

.amt-option {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    text-align: center;
    padding: 15px 0;
    cursor: pointer;
    transition: all 0.2s;
}

.amt-option:hover {
    border-color: #409EFF;
    color: #409EFF;
}

.amt-option.active {
    background: #409EFF;
    color: white;
    border-color: #409EFF;
}
</style>
