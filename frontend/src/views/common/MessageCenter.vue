<template>
  <div class="message-center-container">
    <div class="page-header">
      <h2>消息中心</h2>
    </div>

    <div class="inbox-list" v-loading="loading">
      <el-empty v-if="!loading && sessions.length === 0" description="暂无消息" />
      
      <div 
        v-for="item in sessions" 
        :key="item.otherRole + '_' + item.otherId"
        class="session-item"
        @click="goToChat(item)"
      >
        <div class="avatar-area">
          <el-avatar :size="50" :src="item.otherAvatar" :icon="getIcon(item.otherRole)" :style="{ background: getBgColor(item.otherRole) }">
             {{ item.otherName ? item.otherName.substring(0, 1) : '' }}
          </el-avatar>
          <div class="unread-badge" v-if="item.unreadCount > 0">{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</div>
        </div>
        
        <div class="info-area">
          <div class="info-top">
            <span class="name">{{ item.otherName }}</span>
            <span class="time">{{ formatTime(item.lastTime) }}</span>
          </div>
          <div class="info-bottom">
            <span class="preview-msg">{{ item.lastMessage }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSocketStore } from '@/stores/socket'
import request from '@/utils/request'
import { User, Shop, Bell, Service } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const socketStore = useSocketStore()
const loading = ref(false)
const sessions = ref([])

onMounted(() => {
  fetchInbox()
  socketStore.onMessage(handleSocketMessage)
})

onUnmounted(() => {
  socketStore.offMessage(handleSocketMessage)
})

const handleSocketMessage = (msg) => {
  fetchInbox()
}

const fetchInbox = async () => {
  if (!userStore.user.username) return
  
  loading.value = true
  try {
    let myId = 0
    let myRole = 0
    if (userStore.role === 'student') {
        myId = userStore.user.studentId
        myRole = 1
    } else if (userStore.role === 'merchant') {
        myId = userStore.user.merchantId
        myRole = 2
    } else if (userStore.role === 'admin') {
        myId = userStore.user.adminId || userStore.user.id
        myRole = 3
    }
    
    const res = await request.get('/chat/inbox', {
      params: { userId: myId, role: myRole }
    })
    sessions.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goToChat = (item) => {
  router.push({
    path: '/chat',
    query: {
      targetId: item.otherId,
      role: item.otherRole
    }
  })
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 24 * 3600 * 1000 && date.getDate() === now.getDate()) {
    // Today: HH:mm
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0')
  } else {
    // Before: MM-DD
    return (date.getMonth() + 1) + '-' + date.getDate()
  }
}

const getIcon = (role) => {
  if (role === 0) return Bell
  if (role === 1) return User
  if (role === 2) return Shop
  if (role === 3) return Service
  return User
}

const getBgColor = (role) => {
  if (role === 0) return '#E6A23C'
  if (role === 1) return '#409EFF'
  if (role === 2) return '#67C23A'
  if (role === 3) return '#909399'
  return '#C0C4CC'
}
</script>

<style scoped>
.message-center-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  min-height: 80vh;
}

.page-header {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.inbox-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  overflow: hidden;
}

.session-item {
  display: flex;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item:last-child {
  border-bottom: none;
}

.avatar-area {
  position: relative;
  margin-right: 15px;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 0 6px;
  font-size: 12px;
  height: 18px;
  line-height: 18px;
  min-width: 18px;
  text-align: center;
  border: 2px solid white;
}

.info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.info-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.time {
  font-size: 12px;
  color: #909399;
}

.info-bottom {
  display: flex;
}

.preview-msg {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
</style>
