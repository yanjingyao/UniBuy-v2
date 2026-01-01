import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useUserStore } from './user'
import { ElNotification } from 'element-plus'
import router from '@/router'

export const useSocketStore = defineStore('socket', () => {
  const socket = ref(null)
  const isConnected = ref(false)
  const userStore = useUserStore()
  const unreadCount = ref(0) // Unread message count
  
  // Callbacks for active chat windows
  const messageCallbacks = ref([])

  function connect() {
    if (isConnected.value) return
    if (!userStore.user || !userStore.role) return

    let roleId = 1
    if (userStore.role === 'student') roleId = 1
    else if (userStore.role === 'merchant') roleId = 2
    else if (userStore.role === 'admin') roleId = 3
    
    let userId = 0
    if (roleId === 1) userId = userStore.user.studentId
    else if (roleId === 2) userId = userStore.user.merchantId
    else if (roleId === 3) userId = userStore.user.adminId || userStore.user.id // Handle admin ID variations

    if (!userId) {
        console.warn('Socket connect failed: No User ID')
        return
    }

    const wsUrl = `ws://localhost:8080/ws/${roleId}/${userId}`
    socket.value = new WebSocket(wsUrl)

    socket.value.onopen = () => {
      console.log('WebSocket Connected')
      isConnected.value = true
    }

    socket.value.onmessage = (event) => {
      if (event.data === 'pong') return
      try {
        const msg = JSON.parse(event.data)
        handleMessage(msg)
      } catch (e) {
        // Maybe plain text
        console.log('WS Message:', event.data)
      }
    }

    socket.value.onclose = () => {
      console.log('WebSocket Closed')
      isConnected.value = false
      socket.value = null
    }
    
    socket.value.onerror = (err) => {
        console.error('WebSocket Error', err)
    }
  }

  function disconnect() {
    if (socket.value) {
      socket.value.close()
    }
    isConnected.value = false
  }

  function handleMessage(msg) {
    // 1. Dispatch to active subscribers (e.g. Chat window)
    messageCallbacks.value.forEach(cb => cb(msg))

    // 2. Increment Unread Count (Only if not currently in chat with this person?)
    // Ideally, we should check current route. For now simple logic:
    // If not system message, and we are not in chat page (or we are but with someone else), increment.
    // But simplified: Always increment, let Chat.vue clear it when opened?
    // Or better: Just increment, and if user enters chat, we fetch unread count again (or clear).
    unreadCount.value++

    // 3. Show Global Notification
    // Logic: If user is already on Chat page with this sender, maybe skip notification?
    // But for now, let's show it.
    
    let title = '新消息'
    let content = msg.content
    
    if (msg.type === 1) content = '[图片]'
    else if (msg.type === 2) {
        title = '系统通知'
        // System message content usually includes text
    }

    ElNotification({
      title: title,
      message: content.length > 50 ? content.substring(0, 50) + '...' : content,
      type: msg.type === 2 ? 'warning' : 'info',
      duration: 5000,
      onClick: () => {
        // If system message (senderId=0), maybe go to inbox or order detail?
        if (msg.type === 2) {
             if (msg.orderId) {
                 // Determine route based on role
                 if (userStore.role === 'student') router.push('/student/orders')
                 else if (userStore.role === 'merchant') router.push('/merchant/orders')
             } else {
                 router.push('/message-center')
             }
        } else {
             // Normal chat
             router.push({
                 path: '/chat',
                 query: { targetId: msg.senderId, role: msg.senderRole } // Need to pass role!
             })
        }
      }
    })
  }

  function onMessage(callback) {
    messageCallbacks.value.push(callback)
  }
  
  function offMessage(callback) {
    const index = messageCallbacks.value.indexOf(callback)
    if (index > -1) {
      messageCallbacks.value.splice(index, 1)
    }
  }

  function resetUnread() {
    unreadCount.value = 0
  }

  return { connect, disconnect, onMessage, offMessage, isConnected, unreadCount, resetUnread }
})
