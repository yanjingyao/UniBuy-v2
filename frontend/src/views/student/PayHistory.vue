<template>
  <div class="pay-history-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付记录</span>
        </div>
      </template>
      
      <el-table :data="records" style="width: 100%" v-loading="loading">
        <el-table-column prop="payId" label="支付流水号" width="100" />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="scope">¥{{ scope.row.amount }}</template>
        </el-table-column>
        <el-table-column prop="method" label="支付方式" width="120">
          <template #default="scope">
            <el-tag>{{ scope.row.method === 'balance' ? '余额支付' : '代付确认' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag type="success">支付成功</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="支付时间" />
      </el-table>
      
      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="pageNum"
        @current-change="fetchHistory"
        style="margin-top: 20px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)

onMounted(() => {
  fetchHistory()
})

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/pay/history', {
      params: { 
        userId: userStore.user.studentId,
        pageNum: pageNum.value,
        pageSize: 10
      }
    })
    records.value = res.records
    total.value = res.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pay-history-container {
  padding: 20px;
}
</style>
