<template>
  <div class="audit-container">
    <el-card class="uni-card">
      <template #header>
        <div class="card-header">
          <span class="text-h3">审核中心</span>
          <el-button type="primary" plain :icon="Refresh" @click="fetchData">刷新列表</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="audit-tabs">
        <el-tab-pane label="商家审核" name="merchant">
           <el-table :data="merchants" v-loading="loading" stripe style="width: 100%">
             <el-table-column prop="username" label="用户名" width="150" />
             <el-table-column prop="shopName" label="店铺名" width="200" />
             <el-table-column prop="pickupLocation" label="取货点" />
             <el-table-column prop="createTime" label="申请时间" width="180" />
             <el-table-column label="操作" width="200" fixed="right">
               <template #default="scope">
                 <el-button type="success" size="small" :icon="Check" @click="auditMerchant(scope.row, 1)">通过</el-button>
                 <el-button type="danger" size="small" :icon="Close" @click="auditMerchant(scope.row, 2)">驳回</el-button>
               </template>
             </el-table-column>
           </el-table>
           <el-empty v-if="!loading && merchants.length === 0" description="暂无待审核商家" />
        </el-tab-pane>
        
        <el-tab-pane label="商品审核" name="product">
           <el-table :data="products" v-loading="loading" stripe style="width: 100%">
             <el-table-column label="商品信息" width="300">
               <template #default="scope">
                 <div class="product-info-cell">
                   <img :src="scope.row.images || 'https://via.placeholder.com/50'" class="thumb" />
                   <span>{{ scope.row.name }}</span>
                 </div>
               </template>
             </el-table-column>
             <el-table-column prop="category" label="分类" width="120" />
             <el-table-column prop="description" label="推荐理由" min-width="150" show-overflow-tooltip />
             <el-table-column prop="proxyPrice" label="代购价" width="120">
               <template #default="scope">
                 <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.proxyPrice }}</span>
               </template>
             </el-table-column>
             <el-table-column prop="channel" label="渠道" width="150" />
             <el-table-column label="来源" width="120">
               <template #default="scope">
                 <el-tag v-if="scope.row.sourceRequestId" type="warning" effect="dark">需求转化</el-tag>
                 <el-tag v-else type="info" effect="plain">商家发布</el-tag>
               </template>
             </el-table-column>
             <el-table-column label="操作" width="200" fixed="right">
               <template #default="scope">
                 <el-button type="success" size="small" :icon="Check" @click="auditProduct(scope.row, 1)">通过</el-button>
                 <el-button type="danger" size="small" :icon="Close" @click="auditProduct(scope.row, 2)">驳回</el-button>
               </template>
             </el-table-column>
           </el-table>
           <el-empty v-if="!loading && products.length === 0" description="暂无待审核商品" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Refresh, Check, Close } from '@element-plus/icons-vue'

const activeTab = ref('merchant')
const loading = ref(false)
const merchants = ref([])
const products = ref([])

onMounted(() => {
  fetchData()
})

const handleTabChange = () => {
  fetchData()
}

const fetchData = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'merchant') {
      const res = await request.get('/admin/merchant/pending')
      merchants.value = res.records
    } else {
      const res = await request.get('/product/admin/pending')
      products.value = res.records
    }
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const auditMerchant = async (row, status) => {
  try {
    await request.post('/admin/audit/merchant', null, {
      params: { merchantId: row.merchantId, status }
    })
    ElMessage.success('操作成功')
    fetchData()
  } catch (error) {
    // handled
  }
}

const auditProduct = async (row, status) => {
  try {
    await request.post('/product/audit', null, {
      params: { productId: row.productId, status }
    })
    ElMessage.success('操作成功')
    fetchData()
  } catch (error) {
    // handled
  }
}
</script>

<style scoped>
.audit-container {
  padding-bottom: 40px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.audit-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f0f0;
}

.product-info-cell {
  display: flex;
  align-items: center;
}

.thumb {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  margin-right: 10px;
  object-fit: cover;
  background: #f5f7fa;
}
</style>
