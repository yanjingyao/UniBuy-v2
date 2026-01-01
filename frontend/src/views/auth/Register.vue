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
        <p class="page-title">加入我们，开启校园新体验</p>
      </div>

      <div class="role-capsule-two">
        <div
            class="capsule-bg"
            :style="{ transform: activeRole === 'student' ? 'translateX(0)' : 'translateX(100%)' }"
        ></div>
        <div
            class="role-item"
            :class="{ active: activeRole === 'student' }"
            @click="activeRole = 'student'"
        >
          我是学生
        </div>
        <div
            class="role-item"
            :class="{ active: activeRole === 'merchant' }"
            @click="activeRole = 'merchant'"
        >
          我是商家
        </div>
      </div>

      <el-form class="glass-form" size="large">

        <template v-if="activeRole === 'student'">
          <div class="input-group">
            <el-input v-model="studentForm.username" placeholder="设置用户名" class="filled-input">
              <template #prefix><el-icon class="input-icon"><User /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input v-model="studentForm.password" type="password" placeholder="设置密码 (8-20位)" show-password class="filled-input">
              <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input v-model="studentForm.phone" placeholder="手机号码" class="filled-input">
              <template #prefix><el-icon class="input-icon"><Iphone /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group code-group">
            <el-input v-model="studentForm.code" placeholder="验证码" class="filled-input">
              <template #prefix><el-icon class="input-icon"><Message /></el-icon></template>
            </el-input>
            <button type="button" class="timer-btn" :disabled="timer > 0" @click="sendCode('student')">
              {{ timer > 0 ? `${timer}s` : '获取验证码' }}
            </button>
          </div>
          <div class="input-group">
            <el-input v-model="studentForm.schoolId" placeholder="学号 (验证身份)" class="filled-input">
              <template #prefix><el-icon class="input-icon"><CreditCard /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input v-model="studentForm.nickname" placeholder="昵称 (显示的名称)" class="filled-input">
              <template #prefix><el-icon class="input-icon"><Postcard /></el-icon></template>
            </el-input>
          </div>

          <button type="button" class="action-btn" @click="registerStudent" :disabled="loading">
            <span v-if="!loading">注册学生账号</span>
            <el-icon v-else class="is-loading"><Loading /></el-icon>
          </button>
        </template>

        <template v-else>
          <div class="input-group">
            <el-input v-model="merchantForm.username" placeholder="设置用户名" class="filled-input">
              <template #prefix><el-icon class="input-icon"><User /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input v-model="merchantForm.password" type="password" placeholder="设置密码 (8-20位)" show-password class="filled-input">
              <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input v-model="merchantForm.phone" placeholder="联系手机号" class="filled-input">
              <template #prefix><el-icon class="input-icon"><Iphone /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group code-group">
            <el-input v-model="merchantForm.code" placeholder="验证码" class="filled-input">
              <template #prefix><el-icon class="input-icon"><Message /></el-icon></template>
            </el-input>
            <button type="button" class="timer-btn" :disabled="timer > 0" @click="sendCode('merchant')">
              {{ timer > 0 ? `${timer}s` : '获取验证码' }}
            </button>
          </div>
          <div class="input-group">
            <el-input v-model="merchantForm.shopName" placeholder="店铺名称 (如: 山下小卖部)" class="filled-input">
              <template #prefix><el-icon class="input-icon"><Shop /></el-icon></template>
            </el-input>
          </div>
          <div class="input-group">
            <el-input v-model="merchantForm.pickupLocation" placeholder="取货点 (校内具体位置)" class="filled-input">
              <template #prefix><el-icon class="input-icon"><LocationInformation /></el-icon></template>
            </el-input>
          </div>

          <button type="button" class="action-btn merchant-btn" @click="registerMerchant" :disabled="loading">
            <span v-if="!loading">注册商家账号</span>
            <el-icon v-else class="is-loading"><Loading /></el-icon>
          </button>
        </template>

        <div class="footer-area">
          <span class="footer-text">
            已有账号？<span class="highlight" @click="$router.push('/login')">直接登录</span>
          </span>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
// 引入需要的图标
import {
  Goods, User, Lock, Iphone, Message, CreditCard, Postcard, Shop, LocationInformation, Loading
} from '@element-plus/icons-vue'

const router = useRouter()
// 使用 activeRole 替代原先的 activeTab，更符合当前语境
const activeRole = ref('student')
const loading = ref(false)
const timer = ref(0)
const expectedCode = ref('')

const studentForm = ref({
  username: '', password: '', phone: '', schoolId: '', nickname: '', code: ''
})

const merchantForm = ref({
  username: '', password: '', phone: '', shopName: '', pickupLocation: '', code: ''
})

// --- 保持原有的逻辑不变 ---
const sendCode = async (role) => {
  const phone = role === 'student' ? studentForm.value.phone : merchantForm.value.phone
  if (!phone) return ElMessage.warning('请输入手机号')

  // 模拟发送
  ElMessage.success(`验证码已发送: 123456`)
  expectedCode.value = '123456'
  startTimer()

  // 真实请求示例:
  /* try {
    await request.post('/auth/code', null, { params: { phone } })
    ElMessage.success('验证码已发送')
    startTimer()
  } catch (error) {}
  */
}

const startTimer = () => {
  timer.value = 60
  const interval = setInterval(() => {
    timer.value--
    if (timer.value <= 0) clearInterval(interval)
  }, 1000)
}

const registerStudent = async () => {
  // 简单的演示验证
  if (!studentForm.value.username || !studentForm.value.password) return ElMessage.warning('请填写完整信息')
  if (studentForm.value.code !== '123456') return ElMessage.error('验证码错误')

  loading.value = true
  try {
    // 模拟请求延时
    await new Promise(resolve => setTimeout(resolve, 1500))
    await request.post('/auth/register/student', studentForm.value)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}

const registerMerchant = async () => {
  if (!merchantForm.value.username || !merchantForm.value.password) return ElMessage.warning('请填写完整信息')
  if (merchantForm.value.code !== '123456') return ElMessage.error('验证码错误')

  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    await request.post('/auth/register/merchant', merchantForm.value)
    ElMessage.success('注册提交成功，请等待管理员审核')
    router.push('/login')
  } catch (error) {
    ElMessage.error('注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* --- 1. 布局与背景 (复用统一风格) --- */
.glass-container {
  position: relative;
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f3f4f6;
  overflow: hidden;
  font-family: 'PingFang SC', -apple-system, sans-serif;
  padding: 20px; /* 防止小屏幕贴边 */
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
.blob-1 { width: 450px; height: 450px; background: #3b82f6; top: -10%; left: -10%; }
.blob-2 { width: 350px; height: 350px; background: #8b5cf6; bottom: -10%; right: -5%; animation-delay: -4s; }
.blob-3 { width: 300px; height: 300px; background: #06b6d4; bottom: 20%; left: 15%; animation-delay: -7s; }
@keyframes float {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(40px, 60px) scale(1.05); }
}

/* --- 2. 主卡片 (注册页卡片需要更宽一点) --- */
.main-card {
  position: relative;
  z-index: 10;
  width: 500px; /* 比登录页稍宽，容纳更多字段 */
  max-width: 100%;
  padding: 40px;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(30px);
  -webkit-backdrop-filter: blur(30px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.card-header {
  text-align: center;
  margin-bottom: 25px;
}

.logo-circle {
  width: 56px; height: 56px;
  background: linear-gradient(135deg, #2563eb, #06b6d4); /* 稍微调整了渐变色 */
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 20px rgba(6, 182, 212, 0.25);
}

.app-name { font-size: 24px; font-weight: 800; color: #111827; margin: 0 0 6px; }
.page-title { font-size: 15px; color: #6b7280; margin: 0; font-weight: 500; }

/* --- 3. 角色切换 (双选项胶囊) --- */
.role-capsule-two {
  position: relative;
  display: flex;
  width: 100%;
  background: rgba(0, 0, 0, 0.05);
  padding: 5px;
  border-radius: 14px;
  margin-bottom: 25px;
  cursor: pointer;
  user-select: none;
}

.role-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 15px;
  font-weight: 600;
  color: #6b7280;
  z-index: 2;
  transition: color 0.3s;
}
.role-item.active { color: #1f2937; }

.capsule-bg {
  position: absolute;
  top: 5px; left: 5px; bottom: 5px;
  width: calc(50% - 5px);
  background: #ffffff;
  border-radius: 11px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  z-index: 1;
}

/* --- 4. 表单样式 --- */
.glass-form { width: 100%; }
.input-group { margin-bottom: 18px; }

/* 填充式输入框风格 */
:deep(.filled-input .el-input__wrapper) {
  background-color: rgba(243, 244, 246, 0.8);
  box-shadow: none !important;
  border-radius: 12px;
  padding: 4px 14px;
  transition: all 0.25s ease;
  border: 2px solid transparent;
}
:deep(.filled-input .el-input__wrapper:hover) { background-color: rgba(229, 231, 235, 0.9); }
:deep(.filled-input .el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1) !important;
}
:deep(.filled-input .el-input__inner) { height: 40px; font-size: 15px; color: #1f2937; }
.input-icon { font-size: 18px; color: #9ca3af; margin-right: 4px; }

/* 验证码行 */
.code-group { display: flex; gap: 12px; }
.timer-btn {
  white-space: nowrap; padding: 0 20px; height: 48px; /* 高度与输入框对齐 */
  border-radius: 12px; background: rgba(59, 130, 246, 0.1); color: #3b82f6;
  border: none; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s;
}
.timer-btn:hover:not(:disabled) { background: #3b82f6; color: #fff; }
.timer-btn:disabled { background: #f3f4f6; color: #9ca3af; cursor: not-allowed; }

/* 注册按钮 */
.action-btn {
  width: 100%; height: 52px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white; border: none; border-radius: 14px;
  font-size: 16px; font-weight: 600; cursor: pointer;
  box-shadow: 0 10px 25px rgba(37, 99, 235, 0.3);
  transition: all 0.2s; display: flex; align-items: center; justify-content: center;
  margin-top: 10px;
}
/* 商家注册按钮换个渐变色区分一下 */
.merchant-btn {
  background: linear-gradient(135deg, #0ea5e9, #2563eb);
}
.action-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 15px 30px rgba(37, 99, 235, 0.4); }
.action-btn:active:not(:disabled) { transform: scale(0.99); }
.action-btn:disabled { opacity: 0.7; cursor: not-allowed; }

/* 底部区域 */
.footer-area { margin-top: 25px; text-align: center; }
.footer-text { font-size: 14px; color: #6b7280; }
.highlight {
  color: #3b82f6; font-weight: 600; margin-left: 6px; cursor: pointer; transition: all 0.2s; position: relative;
}
.highlight:hover::after { content: ''; position: absolute; width: 100%; height: 2px; bottom: -2px; left: 0; background-color: #3b82f6; }
.is-loading { animation: rotating 2s linear infinite; }
</style>