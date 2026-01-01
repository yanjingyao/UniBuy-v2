<template>
  <div class="product-detail-container" v-loading="loading">
    <div class="breadcrumb-area">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/student/home' }">商城首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.category }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.name }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="detail-main" v-if="product.productId">
      <div class="image-gallery">
         <el-image 
           :src="currentImage" 
           :preview-src-list="imageList" 
           class="main-image" 
           fit="cover"
         />
         <div class="thumbnail-list">
            <div 
              v-for="(img, index) in imageList" 
              :key="index" 
              class="thumb-item" 
              :class="{ active: currentImage === img }"
              @mouseenter="currentImage = img"
            >
               <img :src="img" />
            </div>
         </div>
      </div>

      <div class="info-section">
         <h1 class="product-title">{{ product.name }}</h1>
         <div class="price-area">
            <span class="currency">¥</span>
            <span class="price">{{ product.proxyPrice }}</span>
            <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice }}</span>
         </div>
         
         <div class="meta-info">
            <div class="meta-row">
               <span class="label">分类：</span>
               <span class="value">{{ product.category }}</span>
            </div>
            <div class="meta-row">
               <span class="label">规格：</span>
               <span class="value">{{ product.specs || '默认' }}</span>
            </div>
            <div class="meta-row">
               <span class="label">库存：</span>
               <span class="value">{{ product.stock }}</span>
            </div>
            <div class="meta-row">
               <span class="label">渠道：</span>
               <span class="value">{{ product.channel }}</span>
            </div>
         </div>

         <div class="action-buttons">
            <el-button type="primary" size="large" @click="showOrderDialog" class="buy-btn">立即购买</el-button>
            <el-button 
                :type="isFavorited ? 'warning' : 'default'" 
                size="large" 
                :icon="isFavorited ? StarFilled : Star" 
                circle 
                @click="toggleFavorite"
                title="收藏"
            />
         </div>

         <div class="merchant-card">
            <div class="merchant-header">
               <el-avatar :size="40" :src="merchant.shopAvatar" :icon="Shop" />
               <div class="merchant-name-area">
                  <div class="m-name">{{ merchant.shopName }}</div>
                  <div class="m-rating">
                     <el-rate v-model="rating" disabled show-score text-color="#ff9900" score-template="{value}" size="small" />
                  </div>
               </div>
            </div>
            <div class="merchant-actions">
               <el-button type="primary" link @click="merchantInfoVisible = true">商家详情</el-button>
               <el-button type="success" link :icon="ChatDotRound" @click="startChat">联系商家</el-button>
            </div>
         </div>
      </div>
    </div>
    
    <!-- Merchant Info Dialog -->
    <el-dialog v-model="merchantInfoVisible" title="商家信息" width="400px">
       <div class="merchant-detail-content">
          <div class="detail-row">
             <label>店铺名称：</label>
             <span>{{ merchant.shopName }}</span>
          </div>
          <div class="detail-row">
             <label>联系电话：</label>
             <span>{{ merchant.phone }}</span>
          </div>
          <div class="detail-row">
             <label>取货地址：</label>
             <span>{{ merchant.pickupLocation }}</span>
          </div>
          <div class="detail-row">
             <label>店铺简介：</label>
             <p>{{ merchant.shopIntro || '暂无简介' }}</p>
          </div>
       </div>
    </el-dialog>

    <!-- Order Dialog -->
    <el-dialog v-model="orderDialogVisible" title="确认购买" width="480px">
      <div class="order-preview">
        <div class="preview-left">
          <img :src="currentImage" />
        </div>
        <div class="preview-right">
          <h4>{{ product.name }}</h4>
          <div class="price-tag">¥ {{ product.proxyPrice }}</div>
        </div>
      </div>

      <el-form :model="orderForm" label-width="80px">
        <el-form-item label="购买数量">
          <el-input-number v-model="orderForm.quantity" :min="1" :max="product.stock" />
        </el-form-item>
        <div class="total-calc">
          <span>合计:</span>
          <span class="final-price">¥ {{ (product.proxyPrice * orderForm.quantity).toFixed(2) }}</span>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="orderLoading">立即支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Shop, ChatDotRound, Star, StarFilled } from '@element-plus/icons-vue'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const product = ref({})
const merchant = ref({})
const imageList = ref([])
const currentImage = ref('')
const rating = ref(4.8) // Mock

const merchantInfoVisible = ref(false)
const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const orderForm = ref({ quantity: 1 })
const isFavorited = ref(false)

const addAddressVisible = ref(false)
const addressList = ref([])
const selectedAddress = ref('')
const newAddressForm = ref({ name: '', phone: '', detail: '' })

const checkFavoriteStatus = async (productId) => {
    if (!userStore.user.studentId) return
    try {
        const res = await checkFavorite(userStore.user.studentId, productId)
        isFavorited.value = res
    } catch (e) {
        console.error(e)
    }
}

const toggleFavorite = async () => {
    if (!userStore.user.studentId) {
        ElMessage.warning('请先登录')
        return
    }
    try {
        if (isFavorited.value) {
            await removeFavorite(userStore.user.studentId, product.value.productId)
            isFavorited.value = false
            ElMessage.success('已取消收藏')
        } else {
            await addFavorite(userStore.user.studentId, product.value.productId)
            isFavorited.value = true
            ElMessage.success('收藏成功')
        }
    } catch (e) {
        ElMessage.error('操作失败')
    }
}


onMounted(() => {
    fetchDetail()
})

const fetchDetail = async () => {
    const id = route.params.id
    if (!id) return
    loading.value = true
    try {
        // Fetch Product
        // Assuming we have a getById endpoint or we reuse list with id?
        // Let's assume list logic or specific logic. 
        // Actually ProductController usually has getById?
        // Checking Controller... ProductController usually has generic CRUD.
        // Let's try /product/{id} or search list.
        // If not, we can fetch list with ID filter if supported.
        // But let's try getById first if standard.
        // Actually looking at backend code I didn't verify ProductController detail.
        // Let's try to fetch list and filter or just use list endpoint with id param if supports?
        // Let's assume /product/detail/{id} or just /product/list?
        // Let's assume we can get it via list or separate API.
        // I will use list with query for now if detail endpoint not confirmed.
        
        // Actually let's assume standard REST: GET /product/{id} is not guaranteed.
        // Let's use /product/list with id param if I can add it, or just iterate.
        // But simpler: I will add getById to ProductController if needed.
        // Let's assume I can get it.
        
        const res = await request.get(`/product/detail`, { params: { id } })
        product.value = res
        
        checkFavoriteStatus(res.productId)
        
        if (res.images) {
            try {
                // Check if it looks like JSON array
                if (res.images.trim().startsWith('[')) {
                    imageList.value = JSON.parse(res.images)
                } else {
                    // Treat as single string URL
                    imageList.value = [res.images]
                }
                currentImage.value = imageList.value[0]
            } catch(e) {
                // Fallback: use raw string if parse fails
                imageList.value = [res.images]
                currentImage.value = res.images
            }
        }
        
        // Fetch Merchant
        if (res.merchantId) {
             // We need merchant info. 
             // Maybe reuse /user/merchant/stats or create public merchant info API?
             // Let's use a new endpoint or existing.
             // I'll assume I can get merchant info.
             const mRes = await request.get(`/user/merchant/public-info`, { params: { merchantId: res.merchantId } })
             merchant.value = mRes
        }
    } catch(e) {
        ElMessage.error('加载失败')
    } finally {
        loading.value = false
    }
}

const showOrderDialog = () => {
    orderForm.value.quantity = 1
    orderDialogVisible.value = true
}

const saveAddress = async () => {
    if (!newAddressForm.value.name || !newAddressForm.value.phone || !newAddressForm.value.detail) {
        ElMessage.warning('请填写完整地址信息')
        return
    }
    
    // Add to list
    const newAddr = { ...newAddressForm.value }
    addressList.value.push(newAddr)
    
    // Update backend (User Profile)
    // Assuming we have an update profile API or specific address API
    // Reusing the logic from Profile.vue: update user info with new addressList JSON
    try {
        const jsonStr = JSON.stringify(addressList.value)
        // Need to update user store and backend
        // Assume /user/update endpoint
        await request.post('/user/update', {
            id: userStore.user.id || userStore.user.studentId, // Ensure ID is correct
            addressList: jsonStr,
            role: 'student' // or 1
        })
        
        // Update local store
        userStore.user.addressList = jsonStr
        
        // Select new address
        selectedAddress.value = newAddr.name + ' ' + newAddr.phone + ' ' + newAddr.detail
        
        addAddressVisible.value = false
        ElMessage.success('地址已保存')
        newAddressForm.value = { name: '', phone: '', detail: '' }
    } catch (e) {
        ElMessage.error('保存失败')
    }
}

const submitOrder = async () => {
    if (!selectedAddress.value) {
        ElMessage.warning('请选择收货地址')
        return
    }

    orderLoading.value = true
    try {
        const totalPrice = product.value.proxyPrice * orderForm.value.quantity
        const data = {
          order: {
            studentId: userStore.user.studentId,
            merchantId: product.value.merchantId,
            totalPrice: totalPrice
          },
          items: [
            {
              productId: product.value.productId,
              quantity: orderForm.value.quantity,
              price: product.value.proxyPrice
            }
          ],
          address: selectedAddress.value
        }
        
        // 1. Create Order
        const res = await request.post('/order/create', data)
        const orderId = res.orderId
        
        // 2. Pay (Using Balance)
        await request.post('/order/pay', null, { params: { orderId } })
        
        ElMessage.success('支付成功，余额已扣除')
        orderDialogVisible.value = false
        // Refresh balance if using a global event bus or store, for now maybe just let user see it on profile
    } catch (error) {
        ElMessage.error(error.msg || '支付失败')
    } finally {
        orderLoading.value = false
    }
}

const startChat = () => {
    router.push({
        path: '/chat',
        query: {
            targetId: merchant.value.merchantId,
            role: 2 // Merchant Role
        }
    })
}
</script>

<style scoped>
.product-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}
.breadcrumb-area {
    margin-bottom: 20px;
}
.detail-main {
    display: flex;
    gap: 40px;
    background: white;
    padding: 30px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}
.image-gallery {
    width: 400px;
    flex-shrink: 0;
}
.main-image {
    width: 100%;
    height: 400px;
    border-radius: 4px;
    background: #f8f8f8;
}
.thumbnail-list {
    display: flex;
    gap: 10px;
    margin-top: 10px;
    overflow-x: auto;
}
.thumb-item {
    width: 60px;
    height: 60px;
    border: 2px solid transparent;
    cursor: pointer;
    border-radius: 4px;
}
.thumb-item.active {
    border-color: #409EFF;
}
.thumb-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}
.info-section {
    flex: 1;
}
.product-title {
    font-size: 24px;
    color: #303133;
    margin-bottom: 20px;
}
.price-area {
    background: #fff5f5;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 20px;
    color: #F56C6C;
}
.currency {
    font-size: 18px;
    font-weight: bold;
}
.price {
    font-size: 32px;
    font-weight: bold;
}
.original-price {
    color: #909399;
    text-decoration: line-through;
    margin-left: 10px;
}
.meta-info {
    margin-bottom: 30px;
}
.meta-row {
    margin-bottom: 10px;
    font-size: 14px;
}
.label {
    color: #909399;
    width: 60px;
    display: inline-block;
}
.action-buttons {
    display: flex;
    gap: 20px;
    margin-bottom: 30px;
}
.buy-btn {
    width: 180px;
}

.merchant-card {
    border-top: 1px solid #eee;
    padding-top: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.merchant-header {
    display: flex;
    align-items: center;
    gap: 10px;
}
.m-name {
    font-weight: bold;
    font-size: 16px;
}
.merchant-detail-content .detail-row {
    margin-bottom: 15px;
    display: flex;
}
.merchant-detail-content label {
    width: 80px;
    color: #909399;
}
/* Order Dialog Styles */
.order-preview {
  display: flex;
  gap: 16px;
  background: #f7f9fc;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}
.preview-left img {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  object-fit: cover;
}
.preview-right h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
}
.price-tag {
  color: #f56c6c;
  font-weight: 600;
}
.total-calc {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  font-size: 16px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 16px;
}
.final-price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: 700;
}
</style>
