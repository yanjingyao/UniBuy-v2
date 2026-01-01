<template>
  <div class="request-pool-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="left">
            <span>需求广场</span>
            <el-radio-group v-model="filterCategory" @change="fetchRequests" size="small" style="margin-left: 20px">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="数码产品">数码</el-radio-button>
              <el-radio-button label="生鲜食品">生鲜</el-radio-button>
              <el-radio-button label="日用百货">日用</el-radio-button>
              <el-radio-button label="其他">其他</el-radio-button>
            </el-radio-group>
          </div>
          <el-button icon="Refresh" @click="fetchRequests">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="requests" style="width: 100%" v-loading="loading">
        <el-table-column prop="productName" label="商品需求" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="urgencyLevel" label="紧急程度" width="100">
           <template #default="scope">
             <el-tag :type="getUrgencyType(scope.row.urgencyLevel)" effect="dark">
               {{ getUrgencyText(scope.row.urgencyLevel) }}
             </el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="expectedPrice" label="期望价" width="100">
           <template #default="scope">¥{{ scope.row.expectedPrice }}</template>
        </el-table-column>
        <el-table-column label="剩余时间" width="150">
           <template #default="scope">
             <span :style="{ color: getTimeColor(scope.row.deadline) }">
               {{ getCountdown(scope.row.deadline) }}
             </span>
           </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleChat(scope.row)">联系学生</el-button>
            <el-button type="primary" size="small" @click="openRespond(scope.row)">我要报价</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="响应需求报价">
      <el-form :model="respondForm">
        <el-form-item label="需求商品">
          {{ currentRequest?.productName }}
        </el-form-item>
        <el-form-item label="期望总价">
          ¥{{ (currentRequest?.expectedPrice || 0) + (currentRequest?.budgetFee || 0) }}
          <span style="color: #999; font-size: 12px; margin-left: 10px">
            (含商品 ¥{{ currentRequest?.expectedPrice }} + 代购费 ¥{{ currentRequest?.budgetFee }})
          </span>
        </el-form-item>
        <el-form-item label="商品报价">
          <el-input-number v-model="respondForm.quotedPrice" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="代购服务费">
          <el-input-number v-model="respondForm.serviceFee" :precision="2" :step="5" />
        </el-form-item>
        <el-form-item label="备注说明">
          <el-input v-model="respondForm.responseNote" type="textarea" placeholder="例如：有现货，下午可送" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResponse" :loading="submitting">提交报价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const requests = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const currentRequest = ref(null)
const filterCategory = ref('')
let timer = null

const respondForm = ref({
  quotedPrice: 0,
  serviceFee: 0,
  responseNote: ''
})

onMounted(() => {
  fetchRequests()
  timer = setInterval(() => {
    // Force update for countdown
    requests.value = [...requests.value]
  }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const fetchRequests = async () => {
  loading.value = true
  try {
    const res = await request.get('/request/square', {
      params: { 
        pageNum: 1, 
        pageSize: 20,
        category: filterCategory.value
      }
    })
    requests.value = res.records
  } catch (error) {
    // handled
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

const handleChat = (row) => {
  router.push({
    path: '/chat',
    query: {
      targetId: row.studentId,
      requestId: row.requestId
    }
  })
}

const openRespond = (row) => {
  currentRequest.value = row
  respondForm.value.quotedPrice = row.expectedPrice
  respondForm.value.serviceFee = 0 // Default 0 or maybe row.budgetFee? Let's use 0
  respondForm.value.responseNote = ''
  dialogVisible.value = true
}

const submitResponse = async () => {
  submitting.value = true
  try {
    const data = {
      requestId: currentRequest.value.requestId,
      merchantId: userStore.user.merchantId,
      quotedPrice: respondForm.value.quotedPrice,
      serviceFee: respondForm.value.serviceFee,
      responseNote: respondForm.value.responseNote
    }
    await request.post('/request/respond', data)
    ElMessage.success('报价提交成功')
    dialogVisible.value = false
    fetchRequests()
  } catch (error) {
    // handled
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.request-pool-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.left {
  display: flex;
  align-items: center;
}
</style>
