<template>
  <div class="page-container">
    <div class="main-content">

      <div class="action-header">
        <div class="header-left">
          <h2 class="page-title">商品管理</h2>
          <span class="sub-title">共 {{ total }} 款商品在售/库中</span>
        </div>
        <el-button type="primary" size="large" icon="Plus" class="create-btn" @click="openAdd">
          发布新商品
        </el-button>
      </div>

      <div class="table-card">
        <el-table
            :data="products"
            style="width: 100%"
            v-loading="loading"
            :header-cell-style="{ background: '#f8f9fa', color: '#606266' }"
        >
          <el-table-column label="商品信息" min-width="260">
            <template #default="scope">
              <div class="product-info-cell">
                <div class="img-box">
                  <el-image
                      v-if="scope.row.images"
                      :src="scope.row.images"
                      fit="cover"
                      class="product-img"
                  />
                  <div v-else class="img-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </div>
                <div class="info-text">
                  <div class="name" :title="scope.row.name">{{ scope.row.name }}</div>
                  <div class="tags">
                    <el-tag size="small" type="info" effect="plain">{{ scope.row.category }}</el-tag>
                    <span class="specs" v-if="scope.row.specs">{{ scope.row.specs }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="proxyPrice" label="价格" width="150">
            <template #default="scope">
              <div class="price-cell">
                <span class="currency">¥</span>
                <span class="amount">{{ scope.row.proxyPrice }}</span>
                <span class="label">代购价</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="库存/渠道" width="180">
            <template #default="scope">
              <div class="stock-info">
                <div class="stock-row">
                  <span class="label">库存:</span>
                  <span class="val" :class="{ 'low-stock': scope.row.stock < 5 }">{{ scope.row.stock }}</span>
                </div>
                <div class="channel-row">
                  <el-icon><Shop /></el-icon> {{ scope.row.channel || '未知渠道' }}
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="160">
            <template #default="scope">
              <div class="status-group">
                <div class="status-item">
                  <span class="dot" :class="getAuditType(scope.row.auditStatus)"></span>
                  <span>{{ getAuditText(scope.row.auditStatus) }}</span>
                </div>
                <div class="status-item" v-if="scope.row.auditStatus === 1">
                  <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'" round>
                    {{ scope.row.status === 1 ? '销售中' : '已下架' }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="scope">
              <div class="actions-cell">
                <el-tooltip content="编辑商品" placement="top">
                  <el-button circle size="small" icon="Edit" @click="handleEdit(scope.row)" />
                </el-tooltip>

                <template v-if="scope.row.auditStatus === 1">
                  <el-tooltip :content="scope.row.status === 1 ? '下架商品' : '上架商品'" placement="top">
                    <el-button
                        circle
                        size="small"
                        :type="scope.row.status === 1 ? 'danger' : 'success'"
                        :icon="scope.row.status === 1 ? 'SoldOut' : 'Sell'"
                        plain
                        @click="handleStatus(scope.row)"
                    />
                  </el-tooltip>
                </template>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-footer">
          <el-pagination
              v-if="total > 0"
              background
              layout="prev, pager, next"
              :total="total"
              v-model:current-page="pageNum"
              @current-change="fetchProducts"
          />
        </div>
      </div>
    </div>

    <el-dialog
        v-model="dialogVisible"
        :title="isEdit ? '编辑商品信息' : '发布新商品'"
        width="650px"
        class="custom-dialog"
        destroy-on-close
    >
      <el-form :model="form" label-width="80px" label-position="top" class="compact-form">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="商品名称" required>
              <el-input v-model="form.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="所属分类">
              <el-select v-model="form.category" style="width: 100%" placeholder="选择分类">
                <el-option label="零食饮料" value="零食饮料" />
                <el-option label="生活用品" value="生活用品" />
                <el-option label="学习用品" value="学习用品" />
                <el-option label="生鲜水果" value="生鲜水果" />
                <el-option label="数码配件" value="数码配件" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规格参数">
              <el-input v-model="form.specs" placeholder="如：500ml / 红色" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代购来源渠道">
              <el-input v-model="form.channel" placeholder="如：山姆会员店" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="highlight-block">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="原价 (参考)">
                <el-input-number v-model="form.originalPrice" :precision="2" :controls="false" style="width: 100%">
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="代购价 (实收)" required>
                <el-input-number v-model="form.proxyPrice" :precision="2" :controls="false" style="width: 100%" class="price-input">
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="库存数量" required>
                <el-input-number v-model="form.stock" :min="1" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="8">
            <el-form-item label="商品主图">
              <el-upload
                  class="avatar-uploader"
                  action="http://localhost:8080/file/upload"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                  name="file"
              >
                <img v-if="form.images" :src="form.images" class="avatar" />
                <div v-else class="upload-placeholder">
                  <el-icon class="icon"><Plus /></el-icon>
                  <span>点击上传</span>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="代购/推荐理由">
              <el-input
                  type="textarea"
                  v-model="form.description"
                  rows="5"
                  placeholder="请简述商品特点或代购理由，这有助于通过审核..."
                  resize="none"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="submitProduct" :loading="submitting" size="large">
            {{ isEdit ? '保存修改' : '立即发布' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// ----------------------------------------------------------------
// 逻辑代码保持原样 (引入图标除外，为了UI效果补充了图标引入)
// ----------------------------------------------------------------
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Plus, Edit, SoldOut, Sell, Picture, Shop } from '@element-plus/icons-vue' // 补充图标

const userStore = useUserStore()
const products = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)

const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const form = ref({
  productId: null,
  name: '',
  category: '',
  specs: '',
  channel: '',
  description: '',
  originalPrice: 0,
  proxyPrice: 0,
  stock: 1,
  images: ''
})

onMounted(() => {
  fetchProducts()
})

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/product/merchant/list', {
      params: {
        merchantId: userStore.user.merchantId,
        pageNum: pageNum.value,
        pageSize: 10
      }
    })
    products.value = res.records
    total.value = res.total
  } catch (error) {
    // handled
  } finally {
    loading.value = false
  }
}

const getAuditType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  return 'danger'
}

const getAuditText = (status) => {
  if (status === 0) return '待审核'
  if (status === 1) return '已上架'
  if (status === 2) return '被驳回'
  return '未知'
}

const openAdd = () => {
  isEdit.value = false
  form.value = {
    productId: null,
    name: '',
    category: '',
    specs: '',
    channel: '',
    description: '',
    originalPrice: 0,
    proxyPrice: 0,
    stock: 1,
    images: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await request.post('/product/status', null, {
      params: { productId: row.productId, status: newStatus }
    })
    ElMessage.success('状态更新成功')
    fetchProducts()
  } catch (error) {
    // handled
  }
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    form.value.images = res.data
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isImg = file.type.startsWith('image/')
  if (!isImg) {
    ElMessage.error('只能上传图片文件!')
  }
  return isImg
}

const submitProduct = async () => {
  if (!form.value.name) return
  submitting.value = true
  try {
    const data = {
      ...form.value,
      merchantId: userStore.user.merchantId
    }

    if (isEdit.value) {
      await request.post('/product/update', data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/product/publish', data)
      ElMessage.success('发布成功')
    }

    dialogVisible.value = false
    fetchProducts()
  } catch (error) {
    // handled
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 页面容器 */
.page-container {
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 24px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部 Header */
.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #1f2d3d;
  margin: 0 0 4px 0;
  font-weight: 600;
}

.sub-title {
  font-size: 13px;
  color: #909399;
}

.create-btn {
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.3);
  font-weight: 500;
  border-radius: 8px;
}

/* 表格卡片 */
.table-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

/* 组合信息列样式 */
.product-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.img-box {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  overflow: hidden;
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.product-img {
  width: 100%;
  height: 100%;
}

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  font-size: 20px;
}

.info-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  display: flex;
  align-items: center;
  gap: 8px;
}

.specs {
  font-size: 12px;
  color: #909399;
}

/* 价格样式 */
.price-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.price-cell .currency { font-size: 12px; color: #f56c6c; margin-right: 2px; }
.price-cell .amount { font-size: 16px; font-weight: bold; color: #f56c6c; }
.price-cell .label { font-size: 12px; color: #c0c4cc; transform: scale(0.9); transform-origin: left; }

/* 库存样式 */
.stock-info { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #606266; }
.stock-row .val { font-weight: bold; margin-left: 4px; }
.stock-row .low-stock { color: #f56c6c; }
.channel-row { display: flex; align-items: center; gap: 4px; font-size: 12px; color: #909399; }

/* 状态样式 */
.status-group { display: flex; flex-direction: column; gap: 6px; align-items: flex-start; }
.status-item { display: flex; align-items: center; gap: 6px; font-size: 13px; }

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.dot.success { background-color: #67c23a; box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.2); }
.dot.warning { background-color: #e6a23c; box-shadow: 0 0 0 2px rgba(230, 162, 60, 0.2); }
.dot.danger { background-color: #f56c6c; box-shadow: 0 0 0 2px rgba(245, 108, 108, 0.2); }

/* 操作列 */
.actions-cell { display: flex; gap: 8px; justify-content: center; }

.pagination-footer {
  padding: 15px 20px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #ebeef5;
}

/* 弹窗表单优化 */
.highlight-block {
  background: #f8fafe;
  padding: 20px 20px 0 20px;
  border-radius: 8px;
  margin-bottom: 0;
  border: 1px solid #eef1f6;
}

.price-input :deep(.el-input__inner) {
  color: #f56c6c;
  font-weight: bold;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: .2s;
  display: flex;
  justify-content: center;
  align-items: center;
}
.avatar-uploader:hover { border-color: #409eff; }
.avatar { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #8c939d; font-size: 12px; }
.upload-placeholder .icon { font-size: 24px; margin-bottom: 4px; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; margin-top: 10px; }
</style>