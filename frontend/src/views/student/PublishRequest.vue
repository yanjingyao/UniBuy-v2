<template>
  <div class="page-container">
    <div class="content-wrapper">
      <el-card class="publish-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div class="header-title">
              <span class="main-title">发布代购需求</span>
              <span class="sub-title">填写详细信息，我们将尽快为您匹配接单人</span>
            </div>
          </div>
        </template>
        
        <el-form 
          :model="form" 
          label-position="top"
          class="publish-form"
          size="large"
        >
          <div class="form-section">
            <h3 class="section-title">商品信息</h3>
            <el-row :gutter="24">
              <el-col :xs="24" :sm="14">
                <el-form-item label="商品名称" required>
                  <el-input 
                    v-model="form.productName" 
                    placeholder="请输入准确的商品名称，例如：PS5国行光驱版" 
                    clearable
                  >
                    <template #prefix>
                      <el-icon><Goods /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="10">
                <el-form-item label="分类" required>
                  <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
                    <el-option label="数码产品" value="数码产品" />
                    <el-option label="生鲜食品" value="生鲜食品" />
                    <el-option label="日用百货" value="日用百货" />
                    <el-option label="书籍资料" value="书籍资料" />
                    <el-option label="其他" value="其他" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24">
              <el-col :span="24">
                <el-form-item label="商品描述">
                  <el-input 
                    v-model="form.description" 
                    type="textarea" 
                    rows="3" 
                    placeholder="请详细描述规格、颜色、型号等具体要求，以便精准代购" 
                    resize="none"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>

          <el-divider border-style="dashed" />

          <div class="form-section">
            <h3 class="section-title">需求配置</h3>
            <el-row :gutter="24">
              <el-col :xs="24" :sm="8">
                <el-form-item label="期望价格 (元)">
                  <el-input-number 
                    v-model="form.expectedPrice" 
                    :precision="2" 
                    :step="10" 
                    :min="0"
                    controls-position="right"
                    style="width: 100%" 
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="16">
                <el-form-item label="紧急程度">
                  <el-radio-group v-model="form.urgencyLevel" class="urgency-group">
                    <el-radio :label="3" border>
                      <span class="radio-label">加急 (24h)</span>
                    </el-radio>
                    <el-radio :label="2" border>
                      <span class="radio-label">常规 (3天)</span>
                    </el-radio>
                    <el-radio :label="1" border>
                      <span class="radio-label">集单 (5天)</span>
                    </el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="24" v-if="form.urgencyLevel === 1">
               <el-col :span="24">
                 <div class="info-box">
                    <el-form-item label="成团人数限制" style="margin-bottom: 0;">
                      <div class="flex-row">
                        <el-input-number v-model="form.minJoinUsers" :min="1" />
                        <span class="tip-text">当前为集单模式，您可设置最低拼单人数</span>
                      </div>
                    </el-form-item>
                 </div>
               </el-col>
            </el-row>
          </div>

          <el-divider border-style="dashed" />

          <div class="form-section">
            <h3 class="section-title">收货信息</h3>
            <el-form-item label="收货地址" required>
              <el-select v-model="form.address" placeholder="请选择收货地址" style="width: 100%">
                <el-option
                  v-for="item in addressList"
                  :key="item.detail"
                  :label="item.detail + ' (' + item.name + ' ' + item.phone + ')'"
                  :value="item.detail + ' ' + item.name + ' ' + item.phone"
                >
                  <div class="address-option">
                    <span class="addr-detail">{{ item.detail }}</span>
                    <span class="addr-info">{{ item.name }} {{ item.phone }}</span>
                  </div>
                </el-option>
              </el-select>
              <div v-if="addressList.length === 0" class="empty-tip">
                暂无地址，请先前往<router-link to="/student/profile">个人中心</router-link>添加
              </div>
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button @click="$router.back()" size="large" class="action-btn">取消</el-button>
            <el-button type="primary" @click="submitRequest" :loading="loading" size="large" class="action-btn submit-btn">
              立即发布
            </el-button>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
// 逻辑代码完全保持不变
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const addressList = ref([])

const form = ref({
  productName: '',
  category: '',
  expectedPrice: 0,
  budgetFee: 0,
  purchaseLocation: '',
  urgencyLevel: 2,
  minJoinUsers: 1,
  description: '',
  address: ''
})

onMounted(() => {
  // Load addresses from user info
  try {
    const list = JSON.parse(userStore.user.addressList || '[]')
    addressList.value = list
    // Set default
    const def = list.find(a => a.isDefault)
    if (def) {
      form.value.address = def.detail + ' ' + def.name + ' ' + def.phone
    } else if (list.length > 0) {
      const first = list[0]
      form.value.address = first.detail + ' ' + first.name + ' ' + first.phone
    }
  } catch (e) {
    addressList.value = []
  }
})

const submitRequest = async () => {
  if (!form.value.productName) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!form.value.address) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  loading.value = true
  try {
    const data = {
      ...form.value,
      studentId: userStore.user.studentId
    }
    await request.post('/request/publish', data)
    ElMessage.success('发布成功')
    router.push('/student/my-requests')
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 页面容器背景，增加层次感 */
.page-container {
  min-height: 100%;
  background-color: #f5f7fa;
  padding: 20px;
  display: flex;
  justify-content: center;
}

.content-wrapper {
  width: 100%;
  max-width: 800px; /* 限制最大宽度，防止在大屏上过于拉伸 */
}

/* 卡片样式优化 */
.publish-card {
  border-radius: 12px;
  border: none;
  overflow: visible; /* 允许阴影溢出 */
}

.card-header {
  padding: 5px 0;
}

.header-title {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.sub-title {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 表单区块样式 */
.section-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 20px 0;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.form-section {
  padding: 0 10px;
}

/* 交互组件微调 */
.urgency-group {
  display: flex;
  width: 100%;
}

.urgency-group .el-radio {
  margin-right: 15px;
  margin-bottom: 5px;
}

.radio-label {
  font-weight: 500;
}

.info-box {
  background-color: #ecf5ff;
  border-radius: 6px;
  padding: 15px;
  margin-top: 5px;
}

.flex-row {
  display: flex;
  align-items: center;
  gap: 15px;
}

.tip-text {
  color: #606266;
  font-size: 13px;
}

/* 地址下拉框内部样式 */
.address-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.addr-detail {
  font-weight: 500;
  color: #303133;
}

.addr-info {
  color: #909399;
  font-size: 12px;
}

.empty-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #f56c6c;
}

/* 底部按钮 */
.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-bottom: 10px;
}

.action-btn {
  min-width: 120px;
  border-radius: 8px;
}

.submit-btn {
  font-weight: 600;
  letter-spacing: 1px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .page-container {
    padding: 10px;
  }
  
  .urgency-group {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .urgency-group .el-radio {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>