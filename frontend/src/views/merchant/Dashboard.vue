<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h2 class="text-h2">欢迎回来, {{ userStore.user.username }}</h2>
      <p class="text-body">这里是您的商家控制台，您可以查看销售数据并管理订单。</p>
    </div>

    <!-- Stat Cards -->
    <el-row :gutter="24" class="stat-row">
      <el-col :span="6">
        <div class="stat-card uni-card blue-gradient">
          <div class="stat-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">今日销售额</span>
            <span class="stat-value">¥{{ stats.todaySales.toFixed(2) }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card uni-card green-gradient">
          <div class="stat-icon">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">待处理订单</span>
            <span class="stat-value">{{ stats.pendingOrders }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card uni-card orange-gradient">
          <div class="stat-icon">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">在售商品</span>
            <span class="stat-value">{{ stats.activeProducts }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card uni-card purple-gradient">
          <div class="stat-icon">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">总销售额</span>
            <span class="stat-value">¥{{ stats.totalSales.toFixed(2) }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Quick Actions -->
    <h3 class="text-h3" style="margin: 30px 0 20px;">快捷入口</h3>
    <el-row :gutter="24">
      <el-col :span="8">
        <div class="action-card uni-card" @click="$router.push('/merchant/requests')">
          <div class="action-icon">
            <el-icon color="#409EFF"><Search /></el-icon>
          </div>
          <div class="action-details">
            <h4>需求广场</h4>
            <p>查看学生发布的最新代购需求并进行报价</p>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="action-card uni-card" @click="$router.push('/merchant/orders')">
          <div class="action-icon">
            <el-icon color="#67C23A"><Tickets /></el-icon>
          </div>
          <div class="action-details">
            <h4>订单处理</h4>
            <p>接单、上传代购凭证、确认送达</p>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="action-card uni-card" @click="$router.push('/merchant/products')">
          <div class="action-icon">
            <el-icon color="#E6A23C"><Goods /></el-icon>
          </div>
          <div class="action-details">
            <h4>商品管理</h4>
            <p>发布新商品、管理库存和上下架</p>
          </div>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { Money, List, Box, Star, Search, Tickets, Goods, ArrowRight } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const userStore = useUserStore()
const stats = ref({
  todaySales: 0,
  pendingOrders: 0,
  activeProducts: 0,
  totalSales: 0
})

onMounted(() => {
  fetchStats()
})

const fetchStats = async () => {
  try {
    const res = await request.get('/user/merchant/stats', {
      params: { merchantId: userStore.user.merchantId }
    })
    stats.value = res
  } catch (e) {
    // console.error(e)
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
