<template>
  <div class="user-mgr-container">
    <el-card class="uni-card">
      <template #header>
        <div class="card-header">
          <span class="text-h3">用户管理</span>
          <el-radio-group v-model="role" @change="fetchData" size="small">
            <el-radio-button label="student">学生</el-radio-button>
            <el-radio-button label="merchant">商家</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column v-if="role==='student'" prop="nickname" label="昵称" width="150" />
        <el-table-column v-if="role==='student'" prop="schoolId" label="学号" width="150" />
        
        <el-table-column v-if="role==='merchant'" prop="shopName" label="店铺名" width="200" />
        <el-table-column v-if="role==='merchant'" prop="pickupLocation" label="取货点" />
        
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <div>
              <el-tag v-if="scope.row.status === 1" type="success">正常</el-tag>
              <el-tag v-else type="danger">禁用</el-tag>
              <div v-if="scope.row.status === 0 && scope.row.banReason" class="ban-info">
                <el-tooltip :content="`原因: ${scope.row.banReason}`" placement="top">
                  <span class="ban-reason">{{ scope.row.banReason.substring(0, 10) }}...</span>
                </el-tooltip>
                <div v-if="scope.row.banEndTime" class="ban-end-time">
                  解禁: {{ formatDate(scope.row.banEndTime) }}
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 1" 
              type="danger" 
              size="small" 
              @click="banUser(scope.row)"
            >
              禁用
            </el-button>
            <el-button 
              v-else 
              type="success" 
              size="small" 
              @click="unbanUser(scope.row)"
            >
              解禁
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const role = ref('student')
const users = ref([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/user/list', {
      params: {
        role: role.value,
        pageNum: pageNum.value,
        pageSize: 10
      }
    })
    users.value = res.records
    total.value = res.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const banUser = (row) => {
  let banReason = ''
  
  ElMessageBox.prompt('请输入禁用原因', '禁用用户', {
    inputType: 'textarea',
    inputPlaceholder: '请输入禁用原因...',
    inputValidator: (value) => {
      if (!value) return '禁用原因不能为空'
      return true
    }
  }).then(({ value: reason }) => {
    banReason = reason
    return ElMessageBox.prompt('请输入禁用天数', '禁用时长', {
      inputType: 'number',
      inputValue: 7,
      inputPlaceholder: '请输入禁用天数',
      inputValidator: (value) => {
        if (!value || value <= 0) return '禁用天数必须大于0'
        return true
      }
    })
  }).then(({ value: durationDays }) => {
    return request.post('/admin/user/ban', null, {
      params: {
        role: role.value,
        userId: role.value === 'student' ? row.studentId : row.merchantId,
        reason: banReason,
        durationDays: parseInt(durationDays)
      }
    })
  }).then(() => {
    ElMessage.success('禁用成功')
    fetchData()
  }).catch(error => {
    if (error !== 'cancel') {
      ElMessage.error('禁用失败: ' + (error.message || '未知错误'))
    }
  })
}

const unbanUser = (row) => {
  ElMessageBox.confirm('确认解禁该用户吗？', '提示', {
    type: 'info'
  }).then(() => {
    return request.post('/admin/user/unban', null, {
      params: {
        role: role.value,
        userId: role.value === 'student' ? row.studentId : row.merchantId
      }
    })
  }).then(() => {
    ElMessage.success('解禁成功')
    fetchData()
  }).catch(error => {
    if (error !== 'cancel') {
      ElMessage.error('解禁失败: ' + (error.message || '未知错误'))
    }
  })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ban-info {
  font-size: 12px;
  margin-top: 4px;
}

.ban-reason {
  color: #f56c6c;
  cursor: help;
}

.ban-end-time {
  color: #909399;
  font-size: 11px;
}
</style>
