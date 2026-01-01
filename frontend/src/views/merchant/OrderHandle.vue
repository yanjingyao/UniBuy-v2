<template>
  <div class="order-handle-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div v-if="announcement" class="announcement-bar" style="margin-bottom: 10px; width: 100%;">
            <el-alert :title="announcement.title" type="warning" :description="announcement.content" show-icon />
          </div>
          <span>订单处理</span>
        </div>
      </template>

      <el-tabs v-model="activeStatus" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待接单" name="1" />
        <el-tab-pane label="待取货" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>

      <el-table :data="orders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalPrice" label="金额" width="100">
           <template #default="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="addressSnapshot" label="收货信息" />
        <el-table-column label="状态" width="100">
           <template #default="scope">
             <el-tag :type="getStatusType(scope.row.orderStatus)">
               {{ getStatusText(scope.row.orderStatus) }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
           <template #default="scope">
             <el-button link type="primary" size="small" @click="handleChat(scope.row)">联系学生</el-button>
             <el-button v-if="scope.row.orderStatus === 1" type="primary" size="small" @click="handleAccept(scope.row)">接单</el-button>
             <el-button v-if="scope.row.orderStatus === 2" type="warning" size="small" @click="openVoucher(scope.row)">上传凭证</el-button>
           </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="pageNum"
        @current-change="fetchOrders"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- Upload Voucher Dialog -->
    <el-dialog v-model="dialogVisible" title="上传代购凭证">
      <el-form>
        <el-form-item label="凭证图片">
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/file/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            name="file"
          >
            <img v-if="voucherUrl" :src="voucherUrl" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitVoucher">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)
const activeStatus = ref('all')
const pageNum = ref(1)
const total = ref(0)
const announcement = ref(null)

const dialogVisible = ref(false)
const voucherUrl = ref('')
const currentOrder = ref(null)

onMounted(() => {
  fetchOrders()
  fetchAnnouncement()
})

const fetchAnnouncement = async () => {
  try {
    const res = await request.get('/admin/announcement/list')
    if (res && res.length > 0) {
      announcement.value = res[0]
    }
  } catch (e) {
    // ignore
  }
}

const handleTabChange = () => {
  pageNum.value = 1
  fetchOrders()
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      userId: userStore.user.merchantId,
      role: 'merchant',
      pageNum: pageNum.value,
      pageSize: 10
    }
    if (activeStatus.value !== 'all') {
      params.status = parseInt(activeStatus.value)
    }
    
    const res = await request.get('/order/list', { params })
    orders.value = res.records
    total.value = res.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  if (status === 1) return 'warning'
  if (status === 2) return 'primary'
  if (status === 3) return 'success'
  if (status === 4) return 'info'
  return 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '待接单', 2: '待取货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

const handleChat = (row) => {
  router.push({
    path: '/chat',
    query: {
      targetId: row.studentId,
      orderId: row.orderId
    }
  })
}

const handleAccept = async (row) => {
  try {
    await request.post('/order/accept', null, {
      params: { orderId: row.orderId }
    })
    ElMessage.success('接单成功')
    fetchOrders()
  } catch (error) {
    // handled
  }
}

const openVoucher = (row) => {
  currentOrder.value = row
  voucherUrl.value = row.voucherImg || ''
  dialogVisible.value = true
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    voucherUrl.value = res.data
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImg = file.type.startsWith('image/')
  if (!isImg) {
    ElMessage.error('只能上传图片文件!')
  }
  return isImg
}

const submitVoucher = async () => {
  if (!voucherUrl.value) return
  try {
    await request.post('/order/voucher', null, {
      params: { 
        orderId: currentOrder.value.orderId,
        url: voucherUrl.value
      }
    })
    ElMessage.success('凭证上传成功')
    dialogVisible.value = false
    fetchOrders()
  } catch (error) {
    // handled
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  flex-direction: column;
}
.order-handle-container {
  padding: 20px;
}
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}
.address-cell {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
</style>
