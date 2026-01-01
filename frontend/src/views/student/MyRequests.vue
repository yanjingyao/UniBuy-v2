<template>
  <div class="page-container">
    <div class="content-wrapper">
      
      <div class="page-header">
        <div class="header-left">
          <h2 class="title">我的需求</h2>
          <span class="subtitle">管理您的代购请求与报价</span>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          round 
          icon="Plus"
          class="publish-btn"
          @click="$router.push('/student/publish')"
        >
          发布新需求
        </el-button>
      </div>

      <div class="request-list" v-loading="loading">
        <el-empty v-if="requests.length === 0 && !loading" description="您还没有发布过需求" />

        <div v-for="item in requests" :key="item.requestId" class="request-card">
          
          <div class="card-main">
            <div class="info-col">
              <div class="product-row">
                <span class="product-name">{{ item.productName }}</span>
                <el-tag size="small" effect="plain" class="cate-tag">{{ item.category }}</el-tag>
              </div>
              <div class="price-row">
                <span class="label">期望价格</span>
                <span class="price">¥{{ item.expectedPrice }}</span>
              </div>
            </div>
            
            <div class="status-col">
              <el-tag :type="getStatusType(item.status)" effect="dark" round>
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>
          </div>

          <el-divider border-style="dashed" style="margin: 12px 0;" />

          <div class="card-footer">
            <div class="meta-info">
              <el-tag :type="getUrgencyType(item.urgencyLevel)" size="small" effect="light">
                {{ getUrgencyText(item.urgencyLevel) }}
              </el-tag>
              
              <div class="countdown" :style="{ color: getTimeColor(item.deadline) }">
                <el-icon><Timer /></el-icon>
                <span>{{ getCountdown(item.deadline) }}</span>
              </div>
            </div>

            <div class="actions">
               <el-button 
                 v-if="item.status === 1 || item.status === 0" 
                 type="primary" 
                 plain 
                 round 
                 size="small"
                 @click="viewResponses(item)"
               >
                 查看报价 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
               </el-button>
               <el-button v-else disabled round size="small">
                 {{ item.status === 2 ? '已选标' : '无法操作' }}
               </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog 
      v-model="dialogVisible" 
      title="商家报价列表" 
      width="550px" 
      class="custom-dialog"
      align-center
    >
      <div class="response-list-container">
        <el-empty v-if="responses.length === 0" description="暂无商家报价，请耐心等待" :image-size="80" />
        
        <div v-for="resp in responses" :key="resp.responseId" class="response-card">
          <div class="resp-header">
            <div class="merchant-info">
              <el-avatar :size="32" icon="UserFilled" class="merchant-avatar" />
              <span class="merchant-id">商家 #{{ resp.merchantId }}</span>
            </div>
            <div class="resp-actions">
              <el-button link type="info" size="small" @click="handleChat(resp)">
                <el-icon><ChatDotRound /></el-icon> 联系
              </el-button>
            </div>
          </div>
          
          <div class="resp-body">
            <div class="price-box">
              <div class="label">商品报价</div>
              <div class="value">¥{{ resp.quotedPrice }}</div>
            </div>
            <div class="divider-vertical"></div>
            <div class="price-box">
              <div class="label">代购费</div>
              <div class="value sub">¥{{ resp.serviceFee || 0 }}</div>
            </div>
          </div>

          <div class="resp-note" v-if="resp.responseNote">
            <span class="note-label">备注：</span>{{ resp.responseNote }}
          </div>

          <div class="resp-footer">
            <el-button 
              type="success" 
              class="select-btn" 
              @click="confirmSelection(resp)"
            >
              确认选标 (总价 ¥{{ (Number(resp.quotedPrice) + Number(resp.serviceFee || 0)).toFixed(2) }})
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
// -------------------------------------------------------------
// 逻辑代码完全保持原样，未做任何修改
// -------------------------------------------------------------
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
// 补充引入图标，保证 UI 展示
import { Plus, Timer, ArrowRight, UserFilled, ChatDotRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const requests = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const responses = ref([])
const currentRequestId = ref(null)
let timer = null

onMounted(() => {
  fetchRequests()
  timer = setInterval(() => {
    requests.value = [...requests.value]
  }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const fetchRequests = async () => {
  loading.value = true
  try {
    const res = await request.get('/request/my', {
      params: { studentId: userStore.user.studentId }
    })
    requests.value = res
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const getUrgencyType = (level) => {
  if (level === 3) return 'danger'
  if (level === 2) return 'warning'
  return 'info'
}

const getUrgencyText = (level) => {
  if (level === 3) return '24h加急'
  if (level === 2) return '3天常规'
  return '5天集单'
}

const getCountdown = (deadline) => {
  const now = dayjs()
  const end = dayjs(deadline)
  const diff = end.diff(now, 'minute')
  
  if (diff <= 0) return '已结束'
  
  const hours = Math.floor(diff / 60)
  const minutes = diff % 60
  
  if (hours > 24) {
    return Math.floor(hours / 24) + '天' + (hours % 24) + '小时'
  }
  return hours + '小时' + minutes + '分'
}

const getTimeColor = (deadline) => {
  const now = dayjs()
  const end = dayjs(deadline)
  const diff = end.diff(now, 'hour')
  if (diff <= 3) return 'red'
  if (diff <= 24) return 'orange'
  return 'green'
}

const getStatusType = (status) => {
  if (status === 0) return 'info'
  if (status === 1) return 'primary'
  if (status === 2) return 'success'
  return 'danger'
}

const getStatusText = (status) => {
  const map = { 0: '待响应', 1: '报价中', 2: '已选标', 3: '已过期' }
  return map[status] || '未知'
}

const handleChat = (response) => {
  router.push({
    path: '/chat',
    query: {
      targetId: response.merchantId,
      requestId: currentRequestId.value
    }
  })
}

const viewResponses = async (row) => {
  currentRequestId.value = row.requestId
  try {
    const res = await request.get('/request/responses', {
      params: { requestId: row.requestId }
    })
    responses.value = res
    dialogVisible.value = true
  } catch (error) {
    // handled
  }
}

const confirmSelection = (response) => {
  ElMessageBox.confirm(
    `确认选择该商家报价 ¥${response.quotedPrice} 吗？系统将自动生成订单。`,
    '确认选标',
    {
      confirmButtonText: '确认生成订单',
      cancelButtonText: '取消',
      type: 'success',
    }
  ).then(async () => {
    try {
      // API returns orderId
      const orderId = await request.post('/request/confirm', null, {
        params: {
          requestId: currentRequestId.value,
          responseId: response.responseId
        }
      })
      
      ElMessage.success('订单已生成，正在跳转支付...')
      dialogVisible.value = false
      
      // Redirect to My Orders page to pay
      router.push('/student/orders')
      
    } catch (error) {
      // handled
    }
  })
}
</script>

<style scoped>
/* 页面容器：灰背景 */
.page-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 100%;
  max-width: 800px;
}

/* 顶部 Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  color: #303133;
  margin: 0;
}

.subtitle {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
  display: block;
}

.publish-btn {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 列表容器 */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片样式 */
.request-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s, box-shadow 0.2s;
}

.request-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
}

.card-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.info-col {
  flex: 1;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.product-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.cate-tag {
  color: #909399;
  border-color: #e4e7ed;
  background: #f4f4f5;
}

.price-row {
  font-size: 14px;
  color: #606266;
}

.price-row .price {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
  margin-left: 4px;
}

/* 底部区域 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
}

/* --- 弹窗样式重构 --- */
.response-list-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 5px;
}

.response-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fcfcfc;
  transition: all 0.2s;
}

.response-card:hover {
  background: #fff;
  border-color: #dcdfe6;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.resp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.merchant-avatar { background: #c0c4cc; }
.merchant-id { font-size: 13px; font-weight: bold; color: #606266; }

.resp-body {
  display: flex;
  background: #fff;
  border-radius: 8px;
  border: 1px dashed #ebeef5;
  padding: 10px;
  margin-bottom: 10px;
}

.price-box {
  flex: 1;
  text-align: center;
}

.price-box .label { font-size: 12px; color: #909399; margin-bottom: 2px; }
.price-box .value { font-size: 18px; color: #303133; font-weight: bold; }
.price-box .value.sub { color: #606266; font-size: 16px; }

.divider-vertical {
  width: 1px;
  background: #ebeef5;
  margin: 0 10px;
}

.resp-note {
  font-size: 12px;
  color: #606266;
  background: #f0f2f5;
  padding: 8px;
  border-radius: 6px;
  margin-bottom: 12px;
}
.note-label { font-weight: bold; }

.resp-footer .select-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: bold;
}
</style>