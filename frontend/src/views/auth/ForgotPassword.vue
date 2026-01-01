<template>
  <div class="glass-container">
    <div class="blob blob-1"></div>
    <div class="blob blob-2"></div>
    <div class="blob blob-3"></div>

    <div class="main-card">
      <div class="card-header">
        <div class="logo-circle">
          <el-icon :size="32" color="#fff"><Goods /></el-icon>
        </div>
        <h2 class="app-name">UniBuy</h2>
        <p class="page-title">重置密码</p>
      </div>

      <div class="role-capsule">
        <div
            class="capsule-bg"
            :style="{ transform: form.role === 'student' ? 'translateX(0)' : 'translateX(100%)' }"
        ></div>
        <div
            class="role-item"
            :class="{ active: form.role === 'student' }"
            @click="form.role = 'student'"
        >
          我是学生
        </div>
        <div
            class="role-item"
            :class="{ active: form.role === 'merchant' }"
            @click="form.role = 'merchant'"
        >
          我是商家
        </div>
      </div>

      <el-form :model="form" class="glass-form" size="large">

        <div class="input-group">
          <el-input
              v-model="form.phone"
              placeholder="请输入注册手机号"
              class="filled-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><Iphone /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="input-group code-group">
          <el-input
              v-model="form.code"
              placeholder="6位验证码"
              class="filled-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><Message /></el-icon>
            </template>
          </el-input>
          <button
              type="button"
              class="timer-btn"
              :disabled="timer > 0"
              @click="sendCode"
          >
            {{ timer > 0 ? `${timer}s 后重试` : '获取验证码' }}
          </button>
        </div>

        <div class="input-group">
          <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="设置新密码 (8-20位)"
              show-password
              class="filled-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </div>

        <button
            type="button"
            class="action-btn"
            @click="handleReset"
        >
          <span v-if="!loading">确认重置</span>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
        </button>

        <div class="footer-area">
          <span class="back-text" @click="$router.push('/login')">
            想起密码了？<span class="highlight">去登录</span>
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Goods, Iphone, Message, Lock, Loading } from '@element-plus/icons-vue'

import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const timer = ref(0)

const form = ref({
  role: 'student',
  phone: '',
  code: '',
  newPassword: ''
})

const sendCode = async () => {
  if (!form.value.phone) return ElMessage.warning('请输入手机号')
  
  try {
    await request.post('/auth/code', null, { params: { phone: form.value.phone } })
    ElMessage.success('验证码已发送: 123456')
    
    timer.value = 60
    const interval = setInterval(() => {
      timer.value--
      if (timer.value <= 0) clearInterval(interval)
    }, 1000)
  } catch(e) {
    ElMessage.error('验证码发送失败')
  }
}

const handleReset = async () => {
  if (!form.value.code || !form.value.newPassword) return ElMessage.warning('请填写完整信息')
  
  loading.value = true
  try {
    await request.post('/auth/reset-password', form.value)
    
    ElMessage.success('重置成功，请使用新密码登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.msg || '重置失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* --- 1. 布局与背景 --- */
.glass-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  overflow: hidden;
  font-family: 'PingFang SC', sans-serif;
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
  width: 400px; height: 400px;
  background: #60a5fa;
  top: -100px; left: -100px;
}
.blob-2 {
  width: 300px; height: 300px;
  background: #a78bfa;
  bottom: -50px; right: -50px;
  animation-delay: -5s;
}
.blob-3 {
  width: 250px; height: 250px;
  background: #34d399;
  bottom: 20%; left: 20%;
  animation-duration: 15s;
}

@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(30px, 50px) scale(1.1); }
}

/* --- 2. 主卡片 (磨砂玻璃) --- */
.main-card {
  position: relative;
  z-index: 1;
  width: 420px;
  padding: 40px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.65); /* 半透明白 */
  backdrop-filter: blur(24px); /* 核心：背景模糊 */
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
      0 20px 40px rgba(0, 0, 0, 0.05),
      0 1px 3px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 头部 */
.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.logo-circle {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 10px 20px rgba(59, 130, 246, 0.3);
}

.app-name {
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}

.page-title {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* --- 3. 角色切换 (胶囊风格) --- */
.role-capsule {
  position: relative;
  display: flex;
  width: 100%;
  background: rgba(0, 0, 0, 0.04);
  padding: 4px;
  border-radius: 14px;
  margin-bottom: 28px;
  cursor: pointer;
}

.role-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  z-index: 2;
  transition: color 0.3s;
}
.role-item.active {
  color: #1f2937;
  font-weight: 600;
}

.capsule-bg {
  position: absolute;
  top: 4px; left: 4px; bottom: 4px;
  width: calc(50% - 4px);
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* --- 4. 表单样式 (填充风格) --- */
.glass-form {
  width: 100%;
}

.input-group {
  margin-bottom: 20px;
}

/* 覆盖 Element Plus 默认样式，做成填充型 */
:deep(.filled-input .el-input__wrapper) {
  background-color: #f3f4f6; /* 浅灰填充 */
  box-shadow: none !important;
  border-radius: 12px;
  padding: 4px 12px;
  transition: all 0.2s;
  border: 2px solid transparent;
}

:deep(.filled-input .el-input__wrapper:hover) {
  background-color: #e5e7eb;
}

:deep(.filled-input .el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #3b82f6; /* 聚焦时亮色边框 */
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1) !important;
}

:deep(.filled-input .el-input__inner) {
  height: 42px;
  font-size: 15px;
  color: #1f2937;
}

.input-icon {
  font-size: 18px;
  color: #9ca3af;
}

/* 验证码行 */
.code-group {
  display: flex;
  gap: 12px;
}

.timer-btn {
  white-space: nowrap;
  padding: 0 16px;
  height: 54px; /* 对齐输入框高度 */
  border-radius: 12px;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.timer-btn:hover:not(:disabled) {
  background: #3b82f6;
  color: #fff;
}
.timer-btn:disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
}

/* 确认按钮 */
.action-btn {
  width: 100%;
  height: 52px;
  margin-top: 10px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.3);
  transition: transform 0.1s, box-shadow 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.4);
}
.action-btn:active {
  transform: scale(0.98);
}

/* 底部文字 */
.footer-area {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.back-text {
  cursor: pointer;
  transition: color 0.2s;
}

.highlight {
  color: #3b82f6;
  font-weight: 600;
  margin-left: 4px;
}
.back-text:hover .highlight {
  text-decoration: underline;
}
</style>