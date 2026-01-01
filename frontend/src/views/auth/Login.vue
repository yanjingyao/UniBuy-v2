<template>
  <div class="glass-container">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>

    <div class="main-card animate__animated animate__fadeInUp">
      <div class="card-header">
        <div class="logo-circle">
          <el-icon :size="32" color="#fff"><Goods /></el-icon>
        </div>
        <h2 class="app-name">UniBuy</h2>
        <p class="page-title">欢迎回来，请登录</p>
      </div>

      <div class="role-capsule-three">
        <div
            class="capsule-bg"
            :style="capsuleStyle"
        ></div>
        <div
            class="role-item"
            :class="{ active: form.role === 'student' }"
            @click="form.role = 'student'"
        >
          学生
        </div>
        <div
            class="role-item"
            :class="{ active: form.role === 'merchant' }"
            @click="form.role = 'merchant'"
        >
          商家
        </div>
        <div
            class="role-item"
            :class="{ active: form.role === 'admin' }"
            @click="form.role = 'admin'"
        >
          管理员
        </div>
      </div>

      <el-form :model="form" class="glass-form" size="large">

        <div class="input-group">
          <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              class="filled-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="input-group no-margin-bottom">
          <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="filled-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="forgot-pwd-row">
          <span class="forgot-link" @click="goToForgotPassword">忘记密码？</span>
        </div>

        <button
            type="button"
            class="action-btn"
            @click="handleLogin"
            :disabled="loading"
        >
          <span v-if="!loading">登 录</span>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
        </button>

        <div class="footer-area">
          <span class="footer-text">
            还没有账号？<span class="highlight" @click="goToRegister">立即注册</span>
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock, Goods, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = ref({
  username: '',
  password: '',
  role: 'student'
})

// 计算角色滑块的位置
const capsuleStyle = computed(() => {
  let translateValue = '0%'
  if (form.value.role === 'merchant') {
    translateValue = '100%'
  } else if (form.value.role === 'admin') {
    translateValue = '200%'
  }
  return {
    transform: `translateX(${translateValue})`
  }
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    // 模拟登录成功 (实际请取消注释下方的真实请求)
    const res = await request.post('/auth/login', form.value)
    
    const userInfo = { ...res.user }
    // Ensure ID is set based on role (though res.user should have it)
    if (res.role === 'student') {
      userInfo.studentId = res.user.studentId
    } else if (res.role === 'merchant') {
      userInfo.merchantId = res.user.merchantId
    } else if (res.role === 'admin') {
      userInfo.adminId = res.user.adminId || res.user.id
    }
    userStore.setUser(userInfo, res.role)

    ElMessage.success(`登录成功，欢迎 ${res.role}`)

    const role = form.value.role
    if (role === 'student') {
      router.push('/student/home')
    } else if (role === 'merchant') {
      router.push('/merchant/dashboard')
    } else if (role === 'admin') {
      router.push('/admin/dashboard')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  console.log('跳转注册')
  router.push('/register')
}

const goToForgotPassword = () => {
  console.log('跳转忘记密码')
  router.push('/forgot-password')
}
</script>

<style scoped>
/* --- 1. 布局与背景 (保持一致) --- */
.glass-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  overflow: hidden;
  font-family: 'PingFang SC', -apple-system, BlinkMacSystemFont, Roboto, Helvetica Neue, sans-serif;
}

/* 动态背景球 */
.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  opacity: 0.6;
  animation: float 10s infinite ease-in-out alternate;
}
.blob-1 {
  width: 450px; height: 450px;
  background: #3b82f6; /* 更深一点的蓝 */
  top: -15%; left: -10%;
}
.blob-2 {
  width: 350px; height: 350px;
  background: #8b5cf6; /* 紫色 */
  bottom: -10%; right: -5%;
  animation-delay: -4s;
}
.blob-3 {
  width: 300px; height: 300px;
  background: #06b6d4; /* 青色 */
  bottom: 30%; left: 20%;
  animation-delay: -7s;
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(40px, 60px) scale(1.05); }
}

/* --- 2. 主卡片 (磨砂玻璃) --- */
.main-card {
  position: relative;
  z-index: 10;
  width: 440px; /* 稍微宽一点适应三个角色按钮 */
  padding: 45px 40px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.7); /* 稍微不透明一点 */
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
      0 20px 40px rgba(0, 0, 0, 0.08),
      0 5px 15px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 头部 */
.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-circle {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.25);
}

.app-name {
  font-size: 26px;
  font-weight: 800;
  color: #111827;
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}

.page-title {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
  font-weight: 500;
}

/* --- 3. 角色切换 (适配3个选项) --- */
.role-capsule-three {
  position: relative;
  display: flex;
  width: 100%;
  background: rgba(0, 0, 0, 0.05);
  padding: 5px;
  border-radius: 16px;
  margin-bottom: 30px;
  cursor: pointer;
  user-select: none;
}

.role-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #6b7280;
  z-index: 2;
  transition: color 0.3s;
  position: relative;
}
.role-item.active {
  color: #1f2937;
}

/* 滑块背景 - 宽度是 1/3 */
.capsule-bg {
  position: absolute;
  top: 5px;
  left: 5px;
  bottom: 5px;
  width: calc(33.333% - 6.66px); /* 精确计算宽度 */
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 1;
}

/* --- 4. 表单样式 (填充风格) --- */
.glass-form {
  width: 100%;
}

.input-group {
  margin-bottom: 22px;
}
.input-group.no-margin-bottom {
  margin-bottom: 12px; /* 为忘记密码留出空间 */
}

/* 覆盖 Element Plus 输入框样式 */
:deep(.filled-input .el-input__wrapper) {
  background-color: rgba(243, 244, 246, 0.8); /* 浅灰半透明填充 */
  box-shadow: none !important;
  border-radius: 14px;
  padding: 4px 14px;
  transition: all 0.25s ease;
  border: 2px solid transparent;
}

:deep(.filled-input .el-input__wrapper:hover) {
  background-color: rgba(229, 231, 235, 0.9);
}

:deep(.filled-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #3b82f6; /* 聚焦时亮色边框 */
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1) !important;
}

:deep(.filled-input .el-input__inner) {
  height: 44px;
  font-size: 15px;
  color: #1f2937;
  font-weight: 500;
}
/* 修复密码框显示切换图标的大小和位置 */
:deep(.filled-input .el-input__suffix-inner) {
  font-size: 16px;
  color: #9ca3af;
}

.input-icon {
  font-size: 19px;
  color: #9ca3af;
  margin-right: 4px;
}

/* 忘记密码链接 */
.forgot-pwd-row {
  text-align: right;
  margin-bottom: 24px;
}
.forgot-link {
  font-size: 14px;
  color: #6b7280;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.2s;
}
.forgot-link:hover {
  color: #3b82f6;
}

/* 登录按钮 */
.action-btn {
  width: 100%;
  height: 56px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  border-radius: 16px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 25px rgba(37, 99, 235, 0.4);
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 1px;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 15px 30px rgba(37, 99, 235, 0.5);
  background: linear-gradient(135deg, #438eff, #2d6cf5);
}
.action-btn:active:not(:disabled) {
  transform: scale(0.99);
}
.action-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 底部区域 */
.footer-area {
  margin-top: 28px;
  text-align: center;
}

.footer-text {
  font-size: 14px;
  color: #6b7280;
}

.highlight {
  color: #3b82f6;
  font-weight: 600;
  margin-left: 6px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  z-index: 20; /* 提高层级 */
}
.highlight:hover {
  color: #1d4ed8;
}
/* 底部链接下划线动画 */
.highlight::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 2px;
  bottom: -2px;
  left: 0;
  background-color: #3b82f6;
  transform: scaleX(0);
  transform-origin: bottom right;
  transition: transform 0.3s ease-out;
  pointer-events: none; /* 防止遮挡点击 */
}
.highlight:hover::after {
  transform: scaleX(1);
  transform-origin: bottom left;
}

/* Element Plus Loading 图标样式修正 */
.is-loading {
  animation: rotating 2s linear infinite;
}
</style>