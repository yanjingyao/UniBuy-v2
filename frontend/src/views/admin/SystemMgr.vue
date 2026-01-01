<template>
  <div class="system-mgr-container">
    <el-tabs type="border-card">
      <el-tab-pane label="数据统计">
         <div class="stats-cards">
            <el-card shadow="hover" class="stat-card">
               <template #header>用户总数</template>
               <div class="stat-value">{{ (stats.totalStudents || 0) + (stats.totalMerchants || 0) }}</div>
            </el-card>
            <el-card shadow="hover" class="stat-card">
               <template #header>订单总数</template>
               <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
            </el-card>
         </div>
         <div style="margin-top: 20px;">
            <el-button type="primary" @click="exportStats">导出统计报表 (JSON)</el-button>
         </div>
      </el-tab-pane>

      <el-tab-pane label="公告管理">
        <div class="card-header" style="margin-bottom: 20px;">
          <el-button type="primary" @click="dialogVisible = true">发布公告</el-button>
        </div>
        <el-table :data="announcements" stripe>
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="content" label="内容" show-overflow-tooltip />
          <el-table-column prop="createTime" label="发布时间" width="180" />
          <el-table-column label="操作" width="100">
             <template #default="scope">
                <el-button type="danger" size="small" @click="deleteAnnouncement(scope.row.id)">删除</el-button>
             </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="系统参数">
        <el-form :model="configForm" label-width="150px" style="max-width: 600px; margin-top: 20px;">
          <el-form-item label="支付超时时间(分钟)">
            <el-input-number v-model="configForm.payTimeout" />
          </el-form-item>
          <el-form-item label="评价保留期限(天)">
            <el-input-number v-model="configForm.reviewRetention" />
          </el-form-item>
          <el-form-item label="需求默认时效(天)">
            <el-input-number v-model="configForm.requestDefaultDuration" />
          </el-form-item>
          <el-form-item label="平台客服电话">
            <el-input v-model="configForm.systemPhone" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

<el-tab-pane label="登录日志">
        <div style="margin-bottom: 15px; display: flex; gap: 10px;">
            <el-input v-model="logFilter.username" placeholder="管理员用户名" clearable style="width: 150px" @input="fetchLoginLogs" />
        </div>
        <el-table :data="loginLogs" stripe style="width: 100%">
           <el-table-column prop="adminId" label="管理员ID" width="100" />
           <el-table-column prop="adminUsername" label="管理员用户名" width="150" />
           <el-table-column prop="loginIp" label="IP地址" width="150" />
           <el-table-column prop="loginStatus" label="登录状态" width="100">
              <template #default="scope">
                 <el-tag v-if="scope.row.loginStatus === 'SUCCESS'" type="success">成功</el-tag>
                 <el-tag v-else type="danger">失败</el-tag>
              </template>
           </el-table-column>
           <el-table-column prop="loginTime" label="登录时间" />
        </el-table>
        <el-pagination
           v-if="logTotal > 0"
           background
           layout="prev, pager, next"
           :total="logTotal"
           v-model:current-page="logPageNum"
           @current-change="fetchLoginLogs"
           style="margin-top: 20px"
        />
     </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" title="发布新公告">
      <el-form :model="announceForm">
        <el-form-item label="标题">
          <el-input v-model="announceForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="announceForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="publishAnnouncement">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, nextTick } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const announcements = ref([])
const loginLogs = ref([])
const stats = ref({ totalStudents: 0, totalMerchants: 0, totalOrders: 0 })
const dialogVisible = ref(false)
const announceForm = reactive({ title: '', content: '' })
const logPageNum = ref(1)
const logTotal = ref(0)
const logFilter = reactive({ username: '' })

const configForm = reactive({
  payTimeout: 30,
  reviewRetention: 180,
  requestDefaultDuration: 3,
  systemPhone: ''
})

// Categories
const categories = ref(['零食', '饮料', '生活用品', '数码电子', '书籍文具'])
const inputVisible = ref(false)
const inputValue = ref('')
const InputRef = ref()

onMounted(() => {
  fetchAnnouncements()
  fetchStats()
  fetchLoginLogs()
  fetchConfigs()
})

const fetchStats = async () => {
  try {
    const res = await request.get('/admin/stats')
    if (res) stats.value = res
  } catch (e) {}
}

const fetchLoginLogs = async () => {
  try {
    const res = await request.get('/admin/login/logs', {
        params: { 
            pageNum: logPageNum.value, 
            pageSize: 10,
            username: logFilter.username || undefined
        }
    })
    loginLogs.value = res.records
    logTotal.value = res.total
  } catch (e) {}
}

const fetchAnnouncements = async () => {
  try {
    const res = await request.get('/admin/announcement/list')
    announcements.value = res
  } catch (e) {}
}

const fetchConfigs = async () => {
    try {
        const res = await request.get('/admin/config/list')
        if (res && res.length) {
            res.forEach(c => {
                if (c.configKey === 'pay_timeout') configForm.payTimeout = parseInt(c.configValue)
                if (c.configKey === 'review_retention') configForm.reviewRetention = parseInt(c.configValue)
                if (c.configKey === 'request_duration') configForm.requestDefaultDuration = parseInt(c.configValue)
                if (c.configKey === 'system_phone') configForm.systemPhone = c.configValue
            })
            // Categories
            const catConfig = res.find(c => c.configKey === 'product_categories')
            if (catConfig) {
                try {
                    categories.value = JSON.parse(catConfig.configValue)
                } catch(e) {}
            }
        }
    } catch(e) {}
}

const publishAnnouncement = async () => {
  if (!announceForm.title) return
  try {
    await request.post('/admin/announcement/publish', announceForm)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    announceForm.title = ''
    announceForm.content = ''
    fetchAnnouncements()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const deleteAnnouncement = (id) => {
  ElMessageBox.confirm('确定删除此公告吗？', '提示', { type: 'warning' })
    .then(async () => {
        try {
            await request.post('/admin/announcement/delete', null, { params: { id } })
            ElMessage.success('已删除')
            fetchAnnouncements()
        } catch(e) {}
    })
}

const saveConfig = async () => {
  try {
     const configs = [
         { key: 'pay_timeout', val: configForm.payTimeout, desc: 'Payment Timeout (min)' },
         { key: 'review_retention', val: configForm.reviewRetention, desc: 'Review Retention (days)' },
         { key: 'request_duration', val: configForm.requestDefaultDuration, desc: 'Default Request Duration (days)' },
         { key: 'system_phone', val: configForm.systemPhone, desc: 'System Phone' }
     ]
     
     for (let c of configs) {
         await request.post('/admin/config/update', { 
             configKey: c.key, 
             configValue: c.val.toString(), 
             description: c.desc 
         })
     }
     ElMessage.success('系统参数已更新')
  } catch(e) {
     ElMessage.error('保存失败')
  }
}

const exportStats = () => {
   const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(stats.value));
   const downloadAnchorNode = document.createElement('a');
   downloadAnchorNode.setAttribute("href",     dataStr);
   downloadAnchorNode.setAttribute("download", "stats.json");
   document.body.appendChild(downloadAnchorNode); 
   downloadAnchorNode.click();
   downloadAnchorNode.remove();
}
</script>

<style scoped>
.stats-cards {
  display: flex;
  gap: 20px;
}
.stat-card {
  width: 200px;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
