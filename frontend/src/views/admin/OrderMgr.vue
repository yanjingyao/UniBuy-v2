<template>
  <div class="admin-order-mgr">
    <el-card class="uni-card">
      <template #header>
        <div class="card-header">
          <span class="text-h3">订单监管</span>
          <el-radio-group v-model="status" @change="fetchData" size="small">
             <el-radio-button :label="null">全部</el-radio-button>
             <el-radio-button :label="0">待付款</el-radio-button>
             <el-radio-button :label="1">待接单</el-radio-button>
             <el-radio-button :label="2">进行中</el-radio-button>
             <el-radio-button :label="3">已完成</el-radio-button>
             <el-radio-button :label="4">已取消</el-radio-button>
             <el-radio-button :label="5">纠纷中</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="orders" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="totalPrice" label="金额" width="100">
           <template #default="scope">¥{{ scope.row.totalPrice }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
           <template #default="scope">
             <el-tag v-if="scope.row.orderStatus===0">待付款</el-tag>
             <el-tag v-else-if="scope.row.orderStatus===1" type="warning">待接单</el-tag>
             <el-tag v-else-if="scope.row.orderStatus===2" type="primary">进行中</el-tag>
             <el-tag v-else-if="scope.row.orderStatus===3" type="success">已完成</el-tag>
             <el-tag v-else-if="scope.row.orderStatus===4" type="info">已取消</el-tag>
             <el-tag v-else-if="scope.row.orderStatus===5" type="danger">纠纷中</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="买家/卖家" show-overflow-tooltip>
           <template #default="scope">
              买家:{{ scope.row.studentName }} <el-icon><Right /></el-icon> 卖家:{{ scope.row.merchantName }}
           </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
               v-if="scope.row.orderStatus===5" 
               type="warning" 
               size="small" 
               @click="handleDispute(scope.row)"
            >处理纠纷</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        background
        layout="prev, pager, next"
        :total="total"
        v-model:current-page="pageNum"
        @current-change="fetchData"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
       <div v-if="currentOrder">
          <el-descriptions :column="2" border>
             <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
             <el-descriptions-item label="状态">
                <span v-if="currentOrder.orderStatus===0">待付款</span>
                <span v-else-if="currentOrder.orderStatus===1">待接单</span>
                <span v-else-if="currentOrder.orderStatus===2">进行中</span>
                <span v-else-if="currentOrder.orderStatus===3">已完成</span>
                <span v-else-if="currentOrder.orderStatus===4">已取消</span>
                <span v-else-if="currentOrder.orderStatus===5">纠纷中</span>
             </el-descriptions-item>
             <el-descriptions-item label="总金额">¥{{ currentOrder.totalPrice }}</el-descriptions-item>
             <el-descriptions-item label="服务费">¥{{ currentOrder.serviceFee }}</el-descriptions-item>
             <el-descriptions-item label="买家">{{ currentOrder.studentName || '未知' }}</el-descriptions-item>
             <el-descriptions-item label="卖家">{{ currentOrder.merchantName || '未知' }}</el-descriptions-item>
             <el-descriptions-item label="收货地址" :span="2">{{ currentOrder.addressSnapshot }}</el-descriptions-item>
             <el-descriptions-item label="取消原因" :span="2" v-if="currentOrder.cancelReason">{{ currentOrder.cancelReason }}</el-descriptions-item>
          </el-descriptions>

          <div style="margin-top: 20px;">
             <h4>时间节点</h4>
             <el-timeline>
               <el-timeline-item :timestamp="currentOrder.createTime" placement="top">
                 订单创建
               </el-timeline-item>
               <el-timeline-item v-if="currentOrder.payTime" :timestamp="currentOrder.payTime" placement="top" type="primary">
                 支付成功
               </el-timeline-item>
               <el-timeline-item v-if="currentOrder.acceptTime" :timestamp="currentOrder.acceptTime" placement="top" type="warning">
                 商家接单
               </el-timeline-item>
               <el-timeline-item v-if="currentOrder.finishTime" :timestamp="currentOrder.finishTime" placement="top" type="success">
                 订单完成
               </el-timeline-item>
             </el-timeline>
          </div>

          <div style="margin-top: 20px;">
             <h4>商品明细</h4>
             <el-table :data="orderItems" border size="small">
                <el-table-column label="图片" width="80">
                   <template #default="scope">
                     <el-image 
                       style="width: 50px; height: 50px" 
                       :src="scope.row.productImg" 
                       :preview-src-list="[scope.row.productImg]" 
                     />
                   </template>
                </el-table-column>
                <el-table-column prop="productName" label="商品" />
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="price" label="单价" width="100" />
             </el-table>
          </div>
       </div>
    </el-dialog>

    <!-- Dispute Dialog -->
    <el-dialog v-model="disputeVisible" title="纠纷处理" width="500px">
      <div v-if="currentOrder">
        <p><strong>买家诉求：</strong>{{ currentOrder.cancelReason }}</p>
        <el-divider />
        <el-form label-position="top">
           <el-form-item label="处理意见">
              <el-input v-model="disputeNote" type="textarea" placeholder="备注信息（可选）" />
           </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="disputeVisible = false">取消</el-button>
        <el-button type="danger" @click="submitDispute('reject')">驳回申请</el-button>
        <el-button type="primary" @click="submitDispute('refund')">同意退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Right } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const status = ref(null)

const detailVisible = ref(false)
const disputeVisible = ref(false)
const currentOrder = ref(null)
const orderItems = ref([])
const disputeNote = ref('')

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/order/list', {
       params: {
         userId: 0, // Admin doesn't bind to user ID
         role: 'admin',
         status: status.value,
         pageNum: pageNum.value,
         pageSize: 10
       }
    })
    orders.value = res.records
    total.value = res.total
  } catch (e) {
    // handled
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
   currentOrder.value = row
   detailVisible.value = true
   try {
      const res = await request.get('/order/items', { params: { orderId: row.orderId } })
      orderItems.value = res
   } catch(e) {}
}

const handleDispute = (row) => {
  currentOrder.value = row
  disputeNote.value = ''
  disputeVisible.value = true
}

const submitDispute = async (action) => {
  if (action === 'reject') {
     // Ask for restore status
     ElMessageBox.confirm('驳回申请后，订单将恢复为“进行中”状态，确定吗？', '驳回确认', {
       type: 'warning'
     }).then(async () => {
        await doResolve(action, 2) // 2: WaitPickup/Processing
     })
  } else {
     await doResolve(action, null)
  }
}

const doResolve = async (action, targetStatus) => {
  try {
    await request.post('/order/admin/resolveDispute', null, {
      params: {
        orderId: currentOrder.value.orderId,
        action: action,
        targetStatus: targetStatus,
        adminNote: disputeNote.value
      }
    })
    ElMessage.success('处理完成')
    disputeVisible.value = false
    fetchData()
  } catch(e) {
    // handled
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
