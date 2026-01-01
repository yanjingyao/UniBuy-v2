<template>
  <div class="admin-user-mgr">
    <el-card class="uni-card">
      <template #header>
        <div class="card-header">
          <span class="text-h3">管理员账号管理</span>
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon> 新增管理员
          </el-button>
        </div>
      </template>

      <el-table :data="adminList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="adminId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="roleLevel" label="角色" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.roleLevel === 0" type="danger">超级管理员</el-tag>
            <el-tag v-else type="info">普通管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              v-if="scope.row.adminId !== userStore.user.adminId"
              @click="handleMessage(scope.row)"
            >发消息</el-button>
            <el-tag v-else type="info" size="small" style="margin-right: 10px">当前账号</el-tag>
            <el-button 
              v-if="scope.row.roleLevel !== 0" 
              type="danger" 
              size="small" 
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Create Dialog -->
    <el-dialog v-model="dialogVisible" title="新增管理员" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreate" :loading="submitting">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const router = useRouter()
const adminList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/list', {
      params: { operatorId: userStore.user.adminId }
    })
    adminList.value = res
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  dialogVisible.value = true
}

const submitCreate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await request.post('/admin/create', form, {
          params: { operatorId: userStore.user.adminId }
        })
        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        // handled
      } finally {
        submitting.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除管理员 "${row.username}" 吗？`, '警告', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.post('/admin/delete', null, {
        params: { 
          targetId: row.adminId,
          operatorId: userStore.user.adminId
        }
      })
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      // handled
    }
  })
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
