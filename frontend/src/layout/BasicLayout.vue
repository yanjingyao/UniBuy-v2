<template>
  <div class="common-layout">
    <el-container class="main-container">
      <el-header class="custom-header">
        <div class="header-inner">
          <div class="logo-area">
            <el-icon :size="28" color="var(--primary-color)" style="margin-right: 10px"><School /></el-icon>
            <span class="logo-text">UniBuy</span>
          </div>
          
          <el-menu
            :default-active="activePath"
            mode="horizontal"
            class="nav-menu"
            :ellipsis="false"
            router
          >
            <!-- Common Message Entry -->
            <el-menu-item index="/message-center" v-if="userStore.role">
              <el-badge :value="socketStore.unreadCount" :hidden="socketStore.unreadCount === 0" class="msg-badge">
                <el-icon><Bell /></el-icon>
              </el-badge>消息
            </el-menu-item>

            <!-- Student Menu -->
            <template v-if="userStore.role === 'student'">
              <el-menu-item index="/student/home">
                <el-icon><Goods /></el-icon>商城首页
              </el-menu-item>
              <el-menu-item index="/student/publish">
                <el-icon><Edit /></el-icon>发布需求
              </el-menu-item>
              <el-menu-item index="/student/my-requests">
                <el-icon><List /></el-icon>我的需求
              </el-menu-item>
              <el-menu-item index="/student/orders">
                <el-icon><ShoppingCart /></el-icon>我的订单
              </el-menu-item>
            </template>

            <!-- Merchant Menu -->
            <template v-if="userStore.role === 'merchant'">
              <el-menu-item index="/merchant/dashboard">
                <el-icon><DataBoard /></el-icon>工作台
              </el-menu-item>
              <el-menu-item index="/merchant/requests">
                <el-icon><Search /></el-icon>需求广场
              </el-menu-item>
              <el-menu-item index="/merchant/products">
                <el-icon><Box /></el-icon>商品管理
              </el-menu-item>
              <el-menu-item index="/merchant/orders">
                <el-icon><Tickets /></el-icon>订单处理
              </el-menu-item>
            </template>

            <!-- Admin Menu -->
            <template v-if="userStore.role === 'admin'">
              <el-menu-item index="/admin/dashboard">
                <el-icon><Odometer /></el-icon>仪表盘
              </el-menu-item>
              <el-menu-item index="/admin/users">
                <el-icon><User /></el-icon>用户管理
              </el-menu-item>
              <el-menu-item index="/admin/admins" v-if="userStore.user.roleLevel === 0">
                <el-icon><Lock /></el-icon>管理员管理
              </el-menu-item>
              <el-menu-item index="/admin/orders">
                <el-icon><Tickets /></el-icon>订单监管
              </el-menu-item>
              <el-menu-item index="/admin/audit">
                <el-icon><Stamp /></el-icon>审核中心
              </el-menu-item>
              <el-menu-item index="/admin/system">
                <el-icon><Setting /></el-icon>系统管理
              </el-menu-item>
            </template>
          </el-menu>

          <div class="user-actions">
            <template v-if="userStore.user.username">
              <el-dropdown trigger="click">
                <div class="user-profile">
                  <el-avatar :size="36" style="background: var(--primary-color)">
                    {{ userStore.user.username.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <span class="username">{{ userStore.user.username }}</span>
                  <el-icon><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>
                      <span class="role-badge">{{ getRoleName(userStore.role) }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item @click="handleProfile">
                      <el-icon><User /></el-icon>个人中心
                    </el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout" style="color: #F56C6C">
                      <el-icon><SwitchButton /></el-icon>退出登录
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" @click="$router.push('/login')">登录 / 注册</el-button>
            </template>
          </div>
        </div>
      </el-header>
      
      <el-main class="custom-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useSocketStore } from '@/stores/socket'
import { useRouter, useRoute } from 'vue-router'
import { 
  School, Goods, Edit, List, ShoppingCart, 
  DataBoard, Search, Box, Tickets, ChatDotSquare,
  Odometer, Stamp, User, SwitchButton, ArrowDown, Setting, Bell
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const socketStore = useSocketStore()
const router = useRouter()
const route = useRoute()

const activePath = computed(() => route.path)

onMounted(() => {
  if (userStore.role) {
    socketStore.connect()
  }
})

const handleLogout = () => {
  socketStore.disconnect()
  userStore.logout()
  router.push('/login')
}

const handleProfile = () => {
  if (userStore.role === 'student') {
    router.push('/student/profile')
  } else if (userStore.role === 'merchant') {
    router.push('/merchant/profile')
  }
}

const getRoleName = (role) => {
  const map = {
    'student': '学生',
    'merchant': '商家',
    'admin': '管理员'
  }
  return map[role] || '用户'
}
</script>

<style scoped>
.main-container {
  min-height: 100vh;
  background-color: var(--bg-color);
}

.custom-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  height: 64px;
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.logo-area {
  display: flex;
  align-items: center;
  width: 200px;
}

.logo-text {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
  letter-spacing: -0.5px;
}

.nav-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
  display: flex;
  justify-content: center;
}

.nav-menu .el-menu-item {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-regular);
  background: transparent !important;
}

.nav-menu .el-menu-item.is-active {
  color: var(--primary-color) !important;
  border-bottom: 2px solid var(--primary-color) !important;
  font-weight: 600;
}

.nav-menu .el-menu-item:hover {
  color: var(--primary-color) !important;
  background-color: rgba(64, 158, 255, 0.05) !important;
}

.user-actions {
  width: 200px;
  display: flex;
  justify-content: flex-end;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-weight: 500;
  color: var(--text-main);
}

.role-badge {
  font-size: 12px;
  color: var(--text-secondary);
}

.custom-main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
  width: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.msg-badge {
  display: flex;
  align-items: center;
  margin-right: 4px;
}
.msg-badge :deep(.el-badge__content) {
  top: 5px;
  right: 0px;
}
</style>
