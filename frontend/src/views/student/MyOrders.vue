<template>
  <div class="page-container" v-loading="loading">
    <div class="content-wrapper">
      
      <div class="header-section">
        <h2 class="page-title">我的订单</h2>
        <div class="tabs-wrapper">
          <el-tabs v-model="activeStatus" @tab-change="handleTabChange" class="custom-tabs">
            <el-tab-pane label="全部" name="all" />
            <el-tab-pane label="待支付" name="0" />
            <el-tab-pane label="待接单" name="1" />
            <el-tab-pane label="待取货" name="2" />
            <el-tab-pane label="已完成" name="3" />
          </el-tabs>
        </div>
      </div>

      <div class="order-list">
        <el-empty v-if="orders.length === 0 && !loading" description="暂无相关订单" />

        <div v-for="item in orders" :key="item.orderId" class="order-card">
          
          <div class="card-top">
            <div class="order-no">
              <span class="label">订单号</span>
              <span class="value">{{ item.orderNo }}</span>
              <span class="time">{{ item.createTime }}</span>
            </div>
            <div class="status-tag">
              <el-tag :type="getStatusType(item.orderStatus)" effect="light" round>
                {{ getStatusText(item.orderStatus) }}
              </el-tag>
            </div>
          </div>

          <el-divider style="margin: 12px 0;" />

          <div class="card-body" @click="handleDetail(item)">
            <div class="img-wrapper">
              <el-image 
                v-if="item.voucherImg" 
                class="voucher-img"
                :src="item.voucherImg" 
                fit="cover"
              >
                <template #error>
                  <div class="img-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="img-placeholder text-only">
                <span>无凭证</span>
              </div>
            </div>

            <div class="info-wrapper">
              <div class="info-row">
                <span class="info-label">收货地址</span>
                <span class="info-val address-text">{{ item.addressSnapshot ? item.addressSnapshot.split(' ').slice(0,2).join(' ') : '暂未设置' }}...</span>
              </div>
              <div class="price-row">
                <span class="price-label">总金额</span>
                <span class="price-val">¥<span class="big-num">{{ item.totalPrice }}</span></span>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="left-actions">
              <el-button link type="info" size="small" @click="handleChat(item)">
                <el-icon style="margin-right: 4px"><Service /></el-icon> 联系商家
              </el-button>
            </div>

            <div class="right-actions">
              <el-button round size="small" @click="handleDetail(item)">详情</el-button>
              
              <template v-if="item.orderStatus === 0">
                <el-button round size="small" @click="handleCancel(item)">取消</el-button>
                <el-button round type="danger" size="small" class="accent-btn" @click="handlePay(item)">去支付</el-button>
              </template>

              <template v-if="item.orderStatus === 1">
                 <el-button round size="small" @click="handleCancel(item)">取消订单</el-button>
              </template>

              <template v-if="item.orderStatus === 2">
                <el-button round size="small" type="warning" plain @click="handleCancel(item)">申请维权</el-button>
                <el-button round type="primary" size="small" class="accent-btn" @click="handleFinish(item)">确认收货</el-button>
              </template>

              <template v-if="item.orderStatus === 3">
                <el-button round size="small" type="danger" plain @click="handleCancel(item)">申请售后</el-button>
              </template>
            </div>
          </div>
        </div>
      </div>
      
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          v-model:current-page="pageNum"
          @current-change="fetchOrders"
        />
      </div>

    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="500px" align-center class="custom-dialog">
      <div v-if="currentOrder">
        <div class="detail-header">
           <span class="status-big">{{ getStatusText(currentOrder.orderStatus) }}</span>
           <span class="price-big">¥{{ currentOrder.totalPrice }}</span>
        </div>
        
        <div class="detail-group">
          <div class="detail-item">
            <span class="label">订单编号</span>
            <span class="value">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="detail-item">
            <span class="label">创建时间</span>
            <span class="value">{{ currentOrder.createTime }}</span>
          </div>
        </div>

        <div class="detail-card">
           <div class="card-title">收货信息</div>
           <div class="addr-box">
             <el-icon><Location /></el-icon>
             <div class="addr-text">{{ currentOrder.addressSnapshot || '未设置' }}</div>
             <el-button link type="primary" size="small" @click="openAddressUpdate">修改</el-button>
           </div>
        </div>

        <div class="detail-card">
          <div class="card-title">商品清单</div>
          <div v-for="(item, idx) in orderItems" :key="idx" class="order-item-row">
             <span>商品ID: {{ item.productId }}</span>
             <span>x{{ item.quantity }}</span>
             <span class="item-price">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
          </div>
        </div>

        <div class="detail-card" v-if="currentOrder.voucherImg">
          <div class="card-title">代购凭证</div>
          <el-image 
            style="width: 100px; height: 100px; border-radius: 8px;" 
            :src="currentOrder.voucherImg" 
            :preview-src-list="[currentOrder.voucherImg]" 
          />
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="payDialogVisible" title="收银台" width="400px" center class="pay-dialog">
      <div class="pay-amount-box">
        <p>需支付金额</p>
        <h2>¥{{ currentOrder?.totalPrice }}</h2>
      </div>
      
      <div class="pay-content">
        <div class="section-label">确认收货地址</div>
        <div v-if="currentOrder?.addressSnapshot" class="address-confirm-card">
           <div class="addr-icon"><el-icon><Location /></el-icon></div>
           <div class="addr-detail">{{ currentOrder.addressSnapshot }}</div>
           <el-button link type="primary" @click="openAddressUpdate">修改</el-button>
        </div>
        <div v-else class="address-warning-card" @click="openAddressUpdate">
           <el-icon><Warning /></el-icon> 请先设置收货地址
        </div>

        <div class="section-label" style="margin-top: 20px;">选择支付方式</div>
        <el-radio-group v-model="payMethod" class="pay-methods">
          <div class="pay-method-item" :class="{active: payMethod === 'balance'}" @click="payMethod = 'balance'">
            <el-radio value="balance">余额支付</el-radio>
          </div>
          <div class="pay-method-item" :class="{active: payMethod === 'proxy'}" @click="payMethod = 'proxy'">
            <el-radio value="proxy">找人代付</el-radio>
          </div>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button type="primary" class="full-width-btn" size="large" @click="submitPayment" :loading="paying" :disabled="!currentOrder?.addressSnapshot">
          确认支付 ¥{{ currentOrder?.totalPrice }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="addressDialogVisible" title="选择地址" width="450px" class="custom-dialog">
      <div class="addr-list-scroll">
        <div 
          v-for="(item, index) in addressList" 
          :key="index"
          class="addr-select-item"
          @click="updateOrderAddress(item.name + ' ' + item.phone + ' ' + item.detail)"
        >
          <div class="top">
            <span class="name">{{ item.name }}</span>
            <span class="phone">{{ item.phone }}</span>
          </div>
          <div class="btm">{{ item.detail }}</div>
        </div>
      </div>
      
      <div class="new-addr-form">
        <div class="form-title">或 新增地址</div>
        <el-form :model="newAddressForm" label-width="70px" size="small">
          <el-form-item label="收货人"><el-input v-model="newAddressForm.name" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="newAddressForm.phone" /></el-form-item>
          <el-form-item label="地址"><el-input v-model="newAddressForm.detail" type="textarea" :rows="2"/></el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" @click="saveAddress" class="full-width-btn">保存并使用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// ---------------------------------------------------------------
// 逻辑代码完全保持原样，未做任何修改
// ---------------------------------------------------------------
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Picture, Service, Warning } from '@element-plus/icons-vue' // 增加了图标引入

const router = useRouter()
const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)
const activeStatus = ref('all')
const pageNum = ref(1)
const total = ref(0)

const detailVisible = ref(false)
const currentOrder = ref(null)
const orderItems = ref([])

const payDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const reviewForm = ref({
  scoreAttitude: 5,
  scoreSpeed: 5,
  scoreQuality: 5,
  comment: ''
})
const payMethod = ref('balance')
const paying = ref(false)

const addressDialogVisible = ref(false)
const newAddressForm = ref({ name: '', phone: '', detail: '' })
const addressList = ref([])

onMounted(() => {
  fetchOrders()
  try {
    addressList.value = JSON.parse(userStore.user.addressList || '[]')
  } catch (e) {
    addressList.value = []
  }
})

const handleTabChange = () => {
  pageNum.value = 1
  fetchOrders()
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      userId: userStore.user.studentId,
      role: 'student',
      pageNum: pageNum.value,
      pageSize: 10
    }
    if (activeStatus.value !== 'all') {
      params.status = parseInt(activeStatus.value)
    }
    
    const res = await request.get('/order/list', { params })
    if (res && res.records) {
      orders.value = res.records
      total.value = res.total
    } else if (Array.isArray(res)) {
       orders.value = res
       total.value = res.length
    } else {
      orders.value = []
      total.value = 0
    }
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  if (status === 0) return 'danger'
  if (status === 1) return 'warning'
  if (status === 2) return 'primary'
  if (status === 4) return 'info'
  return 'info'
}

const handleChat = (row) => {
  if (!row.merchantId) {
    ElMessage.warning('该订单暂无商家接单')
    return
  }
  router.push({
    path: '/chat',
    query: {
      targetId: row.merchantId,
      role: 2, 
      orderId: row.orderId
    }
  })
}

const getStatusText = (status) => {
  const map = { 0: '待支付', 1: '待接单', 2: '待取货', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

const handlePay = (row) => {
  currentOrder.value = row
  payDialogVisible.value = true
}

const submitPayment = async () => {
  paying.value = true
  try {
    await request.post('/pay/confirm', null, {
      params: { 
        orderId: currentOrder.value.orderId,
        method: payMethod.value
      }
    })
    ElMessage.success('支付成功')
    payDialogVisible.value = false
    fetchOrders()
  } catch (error) {
  } finally {
    paying.value = false
  }
}

const handleReview = (row) => {
  currentOrder.value = row
  reviewForm.value = {
    scoreAttitude: 5,
    scoreSpeed: 5,
    scoreQuality: 5,
    comment: ''
  }
  reviewDialogVisible.value = true
}

const submitReview = async () => {
  if (!reviewForm.value.comment) {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  submittingReview.value = true
  try {
    await request.post('/review/add', {
      orderId: currentOrder.value.orderId,
      studentId: userStore.user.studentId,
      merchantId: currentOrder.value.merchantId,
      scoreAttitude: reviewForm.value.scoreAttitude,
      scoreSpeed: reviewForm.value.scoreSpeed,
      scoreQuality: reviewForm.value.scoreQuality,
      comment: reviewForm.value.comment
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    fetchOrders()
  } catch (e) {
  } finally {
    submittingReview.value = false
  }
}

const handleFinish = async (row) => {
  ElMessageBox.confirm('确认收到商品且无误吗？', '确认收货', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/order/finish', null, {
        params: { orderId: row.orderId }
      })
      ElMessage.success('订单完成')
      fetchOrders()
    } catch (error) {
    }
  })
}

const handleCancel = (row) => {
  let title = '取消订单'
  let msg = '请输入取消原因'
  if (row.orderStatus >= 2) {
    title = '申请维权/售后'
    msg = '请输入申请原因（商家同意后将退款）'
  }
  
  ElMessageBox.prompt(msg, title, {
    confirmButtonText: '提交',
    cancelButtonText: '放弃',
  }).then(async ({ value }) => {
    try {
      await request.post('/order/cancel', null, {
        params: { orderId: row.orderId, reason: value }
      })
      if (row.orderStatus >= 2) {
        ElMessage.success('维权申请已提交，请等待管理员处理')
      } else {
        ElMessage.success('订单已取消')
      }
      fetchOrders()
    } catch (error) {
    }
  })
}

const handleContactCancel = () => {
  ElMessage.warning('商家已接单，请联系商家协商取消')
}

const handleDetail = async (row) => {
  currentOrder.value = row
  try {
    const res = await request.get('/order/items', {
      params: { orderId: row.orderId }
    })
    orderItems.value = res
    detailVisible.value = true
  } catch (error) {
  }
}

const openAddressUpdate = () => {
  try {
    addressList.value = JSON.parse(userStore.user.addressList || '[]')
  } catch (e) {
    addressList.value = []
  }
  addressDialogVisible.value = true
}

const updateOrderAddress = async (addrStr) => {
  try {
    await request.post('/order/updateAddress', null, {
      params: { 
        orderId: currentOrder.value.orderId,
        address: addrStr 
      }
    })
    
    currentOrder.value.addressSnapshot = addrStr
    ElMessage.success('地址已更新')
    addressDialogVisible.value = false
  } catch (e) {
    ElMessage.error(e.msg || '地址更新失败')
  }
}

const saveAddress = async () => {
  if (!newAddressForm.value.name || !newAddressForm.value.phone || !newAddressForm.value.detail) {
    ElMessage.warning('请填写完整地址信息')
    return
  }
  const newAddr = { ...newAddressForm.value }
  addressList.value.push(newAddr)
  
  try {
    const jsonStr = JSON.stringify(addressList.value)
    await request.post('/user/update', {
        id: userStore.user.studentId,
        addressList: jsonStr,
        role: 'student'
    })
    userStore.user.addressList = jsonStr
    
    const fullAddr = newAddr.name + ' ' + newAddr.phone + ' ' + newAddr.detail
    await updateOrderAddress(fullAddr)
    
    newAddressForm.value = { name: '', phone: '', detail: '' }
  } catch (e) {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
/* 全局容器：柔和灰背景 */
.page-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 100%;
  max-width: 800px; /* 限制宽度，模拟APP/移动端视觉 */
}

/* 顶部区域 */
.header-section {
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 15px;
  padding-left: 5px;
}

/* 标签页自定义 */
.custom-tabs {
  background: #fff;
  padding: 6px 15px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0; /* 移除Tabs底部灰线 */
}
:deep(.el-tabs__item) {
  font-size: 15px;
}
:deep(.el-tabs__item.is-active) {
  font-weight: bold;
}

/* 订单卡片列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: default;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

/* 卡片顶部 */
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-no {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
}

.order-no .value {
  color: #303133;
  font-family: monospace;
}

.order-no .time {
  margin-left: 5px;
  font-size: 12px;
  color: #c0c4cc;
}

/* 卡片中间内容 */
.card-body {
  display: flex;
  gap: 15px;
  cursor: pointer;
}

.img-wrapper {
  flex-shrink: 0;
  width: 70px;
  height: 70px;
  border-radius: 8px;
  background: #f0f2f5;
  overflow: hidden;
}

.voucher-img {
  width: 100%;
  height: 100%;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #c0c4cc;
  font-size: 24px;
}
.img-placeholder.text-only {
  font-size: 12px;
}

.info-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 2px 0;
}

.info-row {
  display: flex;
  font-size: 13px;
  color: #606266;
}

.info-label {
  color: #909399;
  margin-right: 8px;
}
.address-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-row {
  display: flex;
  align-items: baseline;
  justify-content: flex-end; /* 金额靠右，突出显示 */
}

.price-label {
  font-size: 12px;
  color: #909399;
  margin-right: 4px;
}

.price-val {
  color: #f56c6c;
  font-size: 14px;
  font-weight: bold;
}

.big-num {
  font-size: 20px;
}

/* 卡片底部按钮 */
.card-footer {
  margin-top: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.right-actions {
  display: flex;
  gap: 10px;
}

.accent-btn {
  font-weight: 600;
  padding: 8px 20px;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* --- 弹窗样式优化 --- */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #ebeef5;
}
.status-big { font-size: 20px; font-weight: bold; color: #303133; }
.price-big { font-size: 24px; font-weight: bold; color: #f56c6c; }

.detail-group { display: flex; flex-direction: column; gap: 8px; margin-bottom: 15px; }
.detail-item { display: flex; justify-content: space-between; font-size: 13px; color: #606266; }

.detail-card {
  background: #f9fafe;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 12px;
}
.card-title { font-size: 14px; font-weight: bold; margin-bottom: 8px; color: #333; }
.addr-box { display: flex; alignItems: center; gap: 8px; font-size: 13px; color: #666; }
.addr-text { flex: 1; }

.order-item-row { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 4px; color: #555; }
.item-price { font-weight: bold; color: #333; }

/* 支付弹窗 */
.pay-amount-box { text-align: center; margin-bottom: 20px; }
.pay-amount-box p { color: #909399; font-size: 13px; margin-bottom: 5px; }
.pay-amount-box h2 { font-size: 36px; color: #333; margin: 0; }

.section-label { font-size: 12px; color: #909399; margin-bottom: 8px; }
.address-confirm-card { 
  display: flex; align-items: center; gap: 10px; background: #ecf5ff; padding: 10px; border-radius: 6px; color: #409eff; font-size: 13px; 
}
.address-warning-card {
  display: flex; align-items: center; gap: 6px; background: #fef0f0; padding: 10px; border-radius: 6px; color: #f56c6c; cursor: pointer; font-size: 13px;
}

.pay-methods { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.pay-method-item { border: 1px solid #dcdfe6; padding: 12px; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.pay-method-item.active { border-color: #409eff; background: #ecf5ff; }

/* 地址选择列表 */
.addr-list-scroll { max-height: 200px; overflow-y: auto; margin-bottom: 15px; border: 1px solid #ebeef5; border-radius: 8px; }
.addr-select-item { padding: 12px; border-bottom: 1px solid #f0f0f0; cursor: pointer; }
.addr-select-item:hover { background: #f5f7fa; }
.addr-select-item .top { font-weight: bold; font-size: 14px; margin-bottom: 4px; }
.addr-select-item .top .phone { margin-left: 10px; font-weight: normal; font-size: 13px; color: #666; }
.addr-select-item .btm { font-size: 12px; color: #999; }
.new-addr-form { background: #fafafa; padding: 15px; border-radius: 8px; }
.form-title { font-size: 12px; color: #909399; margin-bottom: 10px; text-align: center; }
.full-width-btn { width: 100%; border-radius: 20px; font-weight: bold; height: 40px; }
</style>