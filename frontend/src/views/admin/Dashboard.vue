<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h2 class="text-h2">管理员控制台</h2>
      <p class="text-body">概览系统状态，处理审核任务。</p>
      <el-button type="primary" :icon="Refresh" @click="manualRefresh" :loading="loading">
        刷新数据
      </el-button>
    </div>

    <!-- 错误提示 -->
    <el-alert 
      v-if="error" 
      :title="error" 
      type="error" 
      show-icon 
      :closable="false"
      style="margin-bottom: 20px"
    >
      <template #default>
        <el-button type="text" @click="retryFetchStats">重新加载</el-button>
      </template>
    </el-alert>

    <!-- Stat Cards -->
    <el-row :gutter="24" class="stat-row" v-loading="loading">
      <el-col :span="6">
        <div class="stat-card uni-card orange-gradient">
          <div class="stat-icon">
            <el-icon><Shop /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">待审核商家</span>
            <span class="stat-value">{{ pendingMerchants }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card uni-card blue-gradient">
          <div class="stat-icon">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">待审核商品</span>
            <span class="stat-value">{{ pendingProducts }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card uni-card purple-gradient">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">总用户数</span>
            <span class="stat-value">{{ (totalStats.totalStudents || 0) + (totalStats.totalMerchants || 0) }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card uni-card green-gradient">
          <div class="stat-icon">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">总订单数</span>
            <span class="stat-value">{{ totalStats.totalOrders || 0 }}</span>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- Recent Orders -->
    <h3 class="text-h3" style="margin: 30px 0 20px;">最新订单</h3>
    <el-card shadow="hover">
       <el-table :data="recentOrders" v-loading="loading" stripe style="width: 100%">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column prop="studentName" label="买家" />
          <el-table-column prop="merchantName" label="卖家" />
          <el-table-column prop="totalPrice" label="金额" width="120">
              <template #default="scope">¥{{ scope.row.totalPrice }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column label="状态" width="120">
              <template #default="scope">
                 <el-tag v-if="scope.row.orderStatus === 0">待支付</el-tag>
                 <el-tag v-else-if="scope.row.orderStatus === 1" type="success">待接单</el-tag>
                 <el-tag v-else-if="scope.row.orderStatus === 2" type="warning">进行中</el-tag>
                 <el-tag v-else-if="scope.row.orderStatus === 3" type="info">已完成</el-tag>
                 <el-tag v-else type="danger">已取消</el-tag>
              </template>
          </el-table-column>
       </el-table>
    </el-card>

    <!-- Quick Actions -->
    <h3 class="text-h3" style="margin: 30px 0 20px;">快捷入口</h3>
    <el-row :gutter="24">
      <el-col :span="12">
        <div class="action-card uni-card" @click="$router.push('/admin/audit')">
          <div class="action-icon">
            <el-icon color="#E6A23C"><Stamp /></el-icon>
          </div>
          <div class="action-details">
            <h4>审核中心</h4>
            <p>处理商家注册申请与商品上架审核</p>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Shop, Goods, User, Stamp, ArrowRight, List, Refresh } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const error = ref(null)
const pendingMerchants = ref(0)
const pendingProducts = ref(0)
const totalStats = ref({
  totalStudents: 0,
  totalMerchants: 0,
  totalOrders: 0
})
const recentOrders = ref([])

// 添加定时刷新
const refreshInterval = ref(null)

onMounted(() => {
  fetchStats()
  // 每5分钟自动刷新一次
  refreshInterval.value = setInterval(fetchStats, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshInterval.value) {
    clearInterval(refreshInterval.value)
  }
})

// 手动刷新
const manualRefresh = () => {
  fetchStats()
}

const fetchStats = async () => {
  loading.value = true
  error.value = null
  
  try {
    // 并行执行所有API调用
    const [mRes, pRes, statsRes, ordersRes] = await Promise.all([
      request.get('/admin/merchant/pending', {
        params: { pageNum: 1, pageSize: 1 }
      }),
      request.get('/product/admin/pending', {
        params: { pageNum: 1, pageSize: 1 }
      }),
      request.get('/admin/stats'),
      request.get('/order/list', {
        params: { 
          userId: 0, // Admin doesn't need userId
          role: 'admin',
          pageNum: 1, 
          pageSize: 10 
        }
      })
    ])
    
    // 修复数据访问路径
    // Interceptor returns res.data directly, so mRes is the Page object
    pendingMerchants.value = mRes?.total || 0
    pendingProducts.value = pRes?.total || 0
    totalStats.value = statsRes || {
      totalStudents: 0,
      totalMerchants: 0,
      totalOrders: 0
    }
    recentOrders.value = ordersRes?.records || []
    
  } catch (err) {
    console.error('Failed to fetch stats', err)
    error.value = '数据加载失败，请稍后重试'
    
    // 设置默认值
    pendingMerchants.value = 0
    pendingProducts.value = 0
    totalStats.value = {
      totalStudents: 0,
      totalMerchants: 0,
      totalOrders: 0
    }
    recentOrders.value = []
  } finally {
    loading.value = false
  }
}

// 添加重试机制
const retryFetchStats = async () => {
  await fetchStats()
  if (!error.value) {
    ElMessage.success('数据重新加载成功')
  }
}
</script>

<style scoped>
.dashboard-container {
  padding-bottom: 40px;
}

.welcome-section {
  margin-bottom: 30px;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  color: white;
  height: 120px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 20px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
}

/* Gradients */
.blue-gradient { background: linear-gradient(135deg, #409EFF 0%, #36d1dc 100%); }
.green-gradient { background: linear-gradient(135deg, #67C23A 0%, #85d95e 100%); }
.orange-gradient { background: linear-gradient(135deg, #E6A23C 0%, #f7c97e 100%); }
.purple-gradient { background: linear-gradient(135deg, #a55eea 0%, #8854d0 100%); }

/* Action Cards */
.action-card {
  display: flex;
  align-items: center;
  padding: 24px;
  cursor: pointer;
}

.action-card:hover {
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 20px;
}

.action-details {
  flex: 1;
}

.action-details h4 {
  margin: 0 0 6px 0;
  font-size: 18px;
  color: var(--text-main);
}

.action-details p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.4;
}

.arrow-icon {
  color: #c0c4cc;
}
</style>
