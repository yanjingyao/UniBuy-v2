<template>
  <div class="page-container">
    <div class="chat-window">
      
      <div class="chat-header blur-effect">
        <div class="header-left" @click="goBack">
          <div class="back-icon-wrapper">
            <el-icon><ArrowLeftBold /></el-icon>
          </div>
          <div class="user-info">
            <span class="nickname">{{ targetName }}</span>
          </div>
        </div>
        <div class="header-right">
           <el-dropdown trigger="click">
            <el-button circle plain icon="MoreFilled" class="more-btn" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>查看订单</el-dropdown-item>
                <el-dropdown-item>清空记录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="chat-body" ref="msgListRef">
        <div class="scroll-content">
          <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
            
            <div class="time-capsule" v-if="shouldShowTime(index)">
              <span>{{ formatTime(msg.createTime) }}</span>
            </div>

            <div v-if="msg.type === 2" class="system-notify">
              <span class="notify-text">{{ msg.content }}</span>
            </div>

            <div v-else :class="['msg-row', isMe(msg) ? 'msg-me' : 'msg-other']">
              
              <div class="avatar-col" v-if="!isMe(msg)">
                <el-avatar :size="36" :src="targetAvatar" class="custom-avatar shadow-sm">
                  {{ targetName.charAt(0) }}
                </el-avatar>
              </div>

              <div class="content-col">
                <div class="bubble-container">
                  <div v-if="msg.type === 1" class="image-bubble">
                     <el-image 
                      :src="msg.content" 
                      :preview-src-list="[msg.content]"
                      fit="cover"
                      class="msg-img"
                    />
                  </div>
                  <div v-else class="text-bubble">
                    {{ msg.content }}
                  </div>
                </div>
              </div>

              <div class="avatar-col" v-if="isMe(msg)">
                <el-avatar :size="36" :src="myAvatar" class="custom-avatar shadow-sm">
                  我
                </el-avatar>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-footer">
        <div class="input-capsule">
          <el-upload
            action="http://localhost:8080/file/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
            class="upload-trigger"
          >
            <el-button circle text class="tool-btn">
              <el-icon :size="20"><PictureRounded /></el-icon>
            </el-button>
          </el-upload>

          <input 
            v-model="inputText" 
            class="clean-input" 
            placeholder="发消息..." 
            @keyup.enter="sendMessage"
          />

          <button 
            class="send-btn" 
            :class="{ 'active': inputText.trim() }"
            @click="sendMessage"
          >
            <el-icon><Position /></el-icon>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSocketStore } from '@/stores/socket'
import { ArrowLeftBold, MoreFilled, PictureRounded, Position } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

// --- 逻辑部分保持不变，直接复用 ---
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const socketStore = useSocketStore()

const targetId = ref(route.query.targetId)
const targetRole = ref(route.query.role || 1)
const orderId = ref(route.query.orderId)
const requestId = ref(route.query.requestId)

const messages = ref([])
const inputText = ref('')
const msgListRef = ref(null)

const targetName = ref('系统通知') // 默认名
const myAvatar = computed(() => userStore.user.avatar || '')

const myId = computed(() => {
  if (userStore.role === 'student') return userStore.user.studentId
  if (userStore.role === 'merchant') return userStore.user.merchantId
  if (userStore.role === 'admin') return userStore.user.adminId || userStore.user.id
  return 0
})

const myRole = computed(() => {
  if (userStore.role === 'student') return 1
  if (userStore.role === 'merchant') return 2
  if (userStore.role === 'admin') return 3
  return 0
})

const isMe = (msg) => msg.senderRole === myRole.value && msg.senderId == myId.value

// 辅助：显示时间规则
const shouldShowTime = (index) => {
  if (index === 0) return true
  // Fix for invalid date or 1970 issue
  if (!messages.value[index].createTime || messages.value[index].createTime.startsWith('1970')) return false
  
  const prev = new Date(messages.value[index - 1].createTime).getTime()
  const curr = new Date(messages.value[index].createTime).getTime()
  return (curr - prev) > 5 * 60 * 1000
}

const formatTime = (t) => {
  if (!t || t.startsWith('1970')) return ''
  const d = new Date(t)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const date = d.getDate().toString().padStart(2, '0')
  const hours = d.getHours().toString().padStart(2, '0')
  const minutes = d.getMinutes().toString().padStart(2, '0')
  const seconds = d.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${date} ${hours}:${minutes}:${seconds}`
}

// API与Socket逻辑
const loadHistory = async () => {
  if (!targetId.value && targetRole.value != 0) return
  try {
    const res = await request.get('/chat/history', {
      params: { userId: myId.value, otherId: targetId.value, role: myRole.value, orderId: orderId.value }
    })
    messages.value = res || []
    scrollToBottom()
    
    // Clear unread count locally when chat is opened
    socketStore.resetUnread()
    
    // Mark read on server
    if (targetId.value) {
       request.post('/chat/read', null, { params: { userId: myId.value, role: myRole.value, otherId: targetId.value } })
    }

    // Let's try to get simple info.
     if (targetId.value) {
         try {
            const uRes = await request.get('/user/public-info', { params: { userId: targetId.value, role: targetRole.value } })
            if (uRes && uRes.name) {
              targetName.value = uRes.name
            } else if (uRes && uRes.nickname) {
              targetName.value = uRes.nickname
            } else if (uRes && uRes.username) {
              targetName.value = uRes.username
            }
            if (uRes && uRes.avatar) {
               targetAvatar.value = uRes.avatar
            }
         } catch(e) {}
     } else {
         // System or Order context
         targetName.value = '系统通知'
     }
  } catch(e) {}
}

const sendMessage = async () => {
  if (!inputText.value.trim()) return
  await doSend(inputText.value, 0)
  inputText.value = ''
}

const doSend = async (content, type) => {
  const msg = {
    senderId: myId.value, senderRole: myRole.value,
    receiverId: targetId.value, receiverRole: targetRole.value,
    orderId: orderId.value, content, type,
    createTime: new Date().toISOString()
  }
  try {
    await request.post('/chat/send', msg)
    messages.value.push(msg)
    scrollToBottom()
  } catch(e) { ElMessage.error('发送失败') }
}

const handleSocketMessage = (msg) => {
   // 简化判断逻辑，实际需根据业务调整
   messages.value.push(msg)
   scrollToBottom()
}

const handleUploadSuccess = (res) => {
  if (res.code === '200' || res.code === 200) doSend(res.data, 1)
}

const beforeUpload = (file) => file.type.startsWith('image/')

const scrollToBottom = () => {
  nextTick(() => {
    if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  })
}

const goBack = () => router.back()

onMounted(() => {
  loadHistory()
  socketStore.onMessage(handleSocketMessage)
})
onUnmounted(() => socketStore.offMessage(handleSocketMessage))
</script>

<style scoped lang="scss">
/* 全局容器：柔和的浅色背景 */
.page-container {
  height: calc(100vh - 80px); /* 减去顶部导航高度 */
  background-color: #f7f8fa;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

/* 聊天主窗口：仿手机/卡片设计 */
.chat-window {
  width: 100%;
  max-width: 800px; /* 限制最大宽度，宽屏下更好看 */
  height: 100%;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 圆角裁剪 */
  position: relative;
}

/* --- 1. 顶部 Header --- */
.chat-header {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(0,0,0,0.03);
  z-index: 10;
  
  /* 玻璃拟态 */
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px); 
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  
  .back-icon-wrapper {
    width: 36px;
    height: 36px;
    border-radius: 12px;
    background: #f2f3f5;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    &:hover { background: #e5e6eb; }
  }

  .user-info {
    display: flex;
    flex-direction: column;
    .nickname {
      font-weight: 700;
      font-size: 16px;
      color: #1d2129;
    }
    .status-badge {
      font-size: 11px;
      color: #00b42a;
      display: flex;
      align-items: center;
      &::before {
        content: "";
        width: 6px;
        height: 6px;
        background: #00b42a;
        border-radius: 50%;
        margin-right: 4px;
      }
    }
  }
}

/* --- 2. 消息内容区 --- */
.chat-body {
  flex: 1;
  background-color: #f7f8fa; /* 消息区底色 */
  overflow-y: auto;
  padding: 20px 20px 0; /* 底部留白给输入框 */
  scroll-behavior: smooth;
}

.scroll-content {
  padding-bottom: 20px;
}

/* 时间胶囊 */
.time-capsule {
  text-align: center;
  margin: 20px 0;
  span {
    background: rgba(0,0,0,0.04);
    color: #86909c;
    font-size: 11px;
    padding: 4px 12px;
    border-radius: 4px; /* 修改为圆角矩形，不再是胶囊 */
  }
}

/* 系统通知样式美化 */
.system-notify {
  text-align: center;
  margin: 16px 0;
  display: flex;
  justify-content: center;
}

.notify-text {
  background-color: #f2f3f5;
  color: #86909c;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  max-width: 80%;
  line-height: 1.5;
}

/* 消息行布局 */
.msg-row {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start; /* 改为顶部对齐，如果去除了头像，其实不需要这个了，但保留结构 */
  gap: 12px;
  
  .avatar-col {
      /* display: none; */ /* 隐藏头像列 */
  }
}

.msg-me {
  flex-direction: row-reverse;
  .text-bubble {
    /* 核心：我的气泡使用渐变色 */
    background: linear-gradient(135deg, #2979ff 0%, #00d2ff 100%);
    color: white;
    border-radius: 8px; /* 修改为小圆角 */
    box-shadow: 0 4px 12px rgba(41, 121, 255, 0.2);
  }
}

.msg-other {
  .text-bubble {
    background: #ffffff;
    color: #1d2129;
    border-radius: 8px; /* 修改为小圆角 */
    box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  }
}

/* 文本/图片气泡通用 */
.text-bubble {
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.5;
  max-width: fit-content;
  word-break: break-all;
}

.image-bubble {
  .msg-img {
    border-radius: 16px;
    max-width: 200px;
    max-height: 200px;
    border: 1px solid rgba(0,0,0,0.05);
    display: block;
  }
}

/* --- 3. 底部输入区 (胶囊悬浮) --- */
.chat-footer {
  padding: 16px 24px 24px;
  background: #f7f8fa; /* 与body一致，实现无缝连接感 */
}

.input-capsule {
  background: #ffffff;
  border-radius: 50px; /* 胶囊形状 */
  padding: 6px 8px 6px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 悬浮感阴影 */
  border: 1px solid rgba(0,0,0,0.02);
  transition: all 0.3s;

  &:focus-within {
    box-shadow: 0 6px 24px rgba(41, 121, 255, 0.12);
  }
}

/* 原生 Input 替代 Element UI 以获得更好的自定义性 */
.clean-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #1d2129;
  background: transparent;
  padding: 8px 0;
  
  &::placeholder {
    color: #c9cdd4;
  }
}

/* 工具按钮 */
.tool-btn {
  color: #86909c;
  &:hover { color: #2979ff; background: transparent; }
}

/* 发送按钮 */
.send-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #f2f3f5;
  color: #c9cdd4;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  
  &.active {
    background: #2979ff;
    color: #fff;
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(41, 121, 255, 0.4);
  }
  
  &:hover.active {
    background: #1c66e5;
  }
}
</style>