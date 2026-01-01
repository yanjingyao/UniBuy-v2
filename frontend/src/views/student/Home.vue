<template>
  <div class="mall-wrapper">
    <aside class="mall-sidebar">
      <div class="sidebar-header">
        <h2 class="sidebar-title">
          <el-icon class="icon"><Shop /></el-icon> 发现好物
        </h2>
      </div>

      <div class="category-nav">
        <div
            class="nav-item"
            :class="{ active: category === '' }"
            @click="changeCategory('')"
        >
          <span class="dot"></span> 全部商品
        </div>
        <div
            v-for="cat in categoryList"
            :key="cat"
            class="nav-item"
            :class="{ active: category === cat }"
            @click="changeCategory(cat)"
        >
          <span class="dot"></span> {{ cat }}
        </div>
      </div>
    </aside>

    <main class="mall-content">
      <header class="content-header">
        <div class="search-section">
          <el-input
              v-model="keyword"
              placeholder="搜索商品名称..."
              class="modern-search"
              :prefix-icon="Search"
              clearable
              @clear="fetchProducts"
              @keyup.enter="fetchProducts"
          >
            <template #append>
              <el-button @click="fetchProducts">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="actions-section">
          <el-dropdown @command="handleSort" trigger="click">
            <el-button class="sort-btn" plain>
              {{ currentSortLabel }} <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="">默认排序</el-dropdown-item>
                <el-dropdown-item command="price_asc">价格: 低到高</el-dropdown-item>
                <el-dropdown-item command="price_desc">价格: 高到低</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <transition name="el-fade-in">
        <div v-if="announcement" class="announcement-wrapper">
          <el-alert
              :title="announcement.title"
              type="warning"
              :description="announcement.content"
              show-icon
              effect="dark"
              close-text="知道了"
          />
        </div>
      </transition>

      <div class="scroll-container" v-loading="loading">
        <div v-if="!loading && products.length === 0" class="empty-state">
          <el-empty description="暂时没有找到相关商品" />
        </div>

        <div class="product-grid" v-else>
          <div
              v-for="product in products"
              :key="product.productId"
              class="product-item"
              @click="buyProduct(product)"
          >
            <div class="img-box">
              <img :src="product.images || 'https://via.placeholder.com/300?text=No+Image'" loading="lazy" />
              <div class="badges">
                <span class="badge channel">{{ product.channel }}</span>
                <span class="badge stock" v-if="product.stock < 5">仅剩 {{ product.stock }}</span>
              </div>
            </div>

            <div class="info-box">
              <div class="main-info">
                <h3 class="name" :title="product.name">{{ product.name }}</h3>
                <div class="meta">
                  <el-tag size="small" type="info" effect="light" round>{{ product.category }}</el-tag>
                </div>
              </div>

              <div class="action-bar">
                <div class="price-block">
                  <span class="symbol">¥</span>
                  <span class="amount">{{ product.proxyPrice }}</span>
                </div>
                <div class="btns">
                  <!-- Favorite Button -->
                  <el-button
                      :type="isFavorited(product.productId) ? 'warning' : 'default'"
                      circle
                      size="small"
                      class="fav-btn"
                      :icon="isFavorited(product.productId) ? StarFilled : Star"
                      @click.stop="toggleFavorite(product)"
                  />
                  <!-- Cart Button -->
                  <el-button
                      type="primary"
                      circle
                      size="small"
                      class="cart-btn"
                      :icon="ShoppingCart"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <el-dialog v-model="orderDialogVisible" title="确认购买" width="480px" align-center destroy-on-close>
      <div class="order-preview">
        <div class="preview-left">
          <img :src="currentProduct?.images || 'https://via.placeholder.com/100'" />
        </div>
        <div class="preview-right">
          <h4>{{ currentProduct?.name }}</h4>
          <div class="price-tag">¥ {{ currentProduct?.proxyPrice }}</div>
        </div>
      </div>

      <el-form :model="orderForm" label-width="80px" class="order-form">
        <el-form-item label="购买数量">
          <el-input-number v-model="orderForm.quantity" :min="1" :max="currentProduct?.stock || 1" />
        </el-form-item>
        <div class="total-calc">
          <span>合计:</span>
          <span class="final-price">¥ {{ (currentProduct ? currentProduct.proxyPrice * orderForm.quantity : 0).toFixed(2) }}</span>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOrder" :loading="orderLoading" round>立即支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Search, ShoppingCart, ArrowDown, Shop, Star, StarFilled } from '@element-plus/icons-vue'
import { addFavorite, removeFavorite, getFavoriteList } from '@/api/favorite'

const userStore = useUserStore()
const products = ref([])
const loading = ref(false)
const keyword = ref('')
const category = ref('')
const sort = ref('')
const announcement = ref(null)

// Favorite set for quick lookup
const favoriteSet = ref(new Set())

// Hardcoded categories for the sidebar loop
const categoryList = ['零食饮料', '生活用品', '学习用品', '生鲜水果', '数码配件']

const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const currentProduct = ref(null)
const orderForm = ref({ quantity: 1 })

// Helper for UI label
const currentSortLabel = computed(() => {
  if (sort.value === 'price_asc') return '价格低到高'
  if (sort.value === 'price_desc') return '价格高到低'
  return '默认排序'
})

onMounted(() => {
  fetchProducts()
  fetchAnnouncement()
  fetchUserFavorites()
})

// Simplified category switcher
const changeCategory = (val) => {
  category.value = val
  fetchProducts()
}

// Favorite Logic
const fetchUserFavorites = async () => {
    if (!userStore.user.studentId) return
    try {
        const res = await getFavoriteList(userStore.user.studentId, 1, 1000)
        const records = res.records || []
        const ids = records.map(item => item.productId)
        favoriteSet.value = new Set(ids)
    } catch (e) {
        console.error(e)
    }
}

const isFavorited = (productId) => {
    return favoriteSet.value.has(productId)
}

const toggleFavorite = async (product) => {
    if (!userStore.user.studentId) {
        ElMessage.warning('请先登录')
        return
    }
    const pid = product.productId
    try {
        if (favoriteSet.value.has(pid)) {
            await removeFavorite(userStore.user.studentId, pid)
            favoriteSet.value.delete(pid)
            ElMessage.success('已取消收藏')
        } else {
            await addFavorite(userStore.user.studentId, pid)
            favoriteSet.value.add(pid)
            ElMessage.success('收藏成功')
        }
    } catch (e) {
        ElMessage.error('操作失败')
    }
}

// ... (Rest of your original logic remains exactly the same)
const fetchAnnouncement = async () => {
  try {
    const res = await request.get('/admin/announcement/list')
    if (res && res.length > 0) announcement.value = res[0]
  } catch (e) {}
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await request.get('/product/list', {
      params: { keyword: keyword.value, category: category.value, sort: sort.value }
    })
    products.value = res.records || res
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const handleSort = (command) => {
  sort.value = command
  fetchProducts()
}

const buyProduct = (product) => {
  currentProduct.value = product
  orderForm.value.quantity = 1
  orderDialogVisible.value = true
}

const submitOrder = async () => {
  orderLoading.value = true
  try {
    const totalPrice = currentProduct.value.proxyPrice * orderForm.value.quantity
    const data = {
      order: {
        studentId: userStore.user.studentId,
        merchantId: currentProduct.value.merchantId,
        totalPrice: totalPrice
      },
      items: [
        {
          productId: currentProduct.value.productId,
          quantity: orderForm.value.quantity,
          price: currentProduct.value.proxyPrice
        }
      ]
    }
    await request.post('/order/create', data)
    ElMessage.success('订单创建成功')
    orderDialogVisible.value = false
  } catch (error) {
  } finally {
    orderLoading.value = false
  }
}
</script>

<style scoped>
/* Main Layout */
.mall-wrapper {
  display: flex;
  height: calc(100vh - 80px); /* Adjust based on your global nav height */
  background-color: #f7f8fa;
  overflow: hidden;
}

/* Sidebar */
.mall-sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #edf2f7;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 24px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.category-nav {
  flex: 1;
  padding: 0 16px;
  overflow-y: auto;
}

.nav-item {
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: 12px;
  cursor: pointer;
  color: #4a5568;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
}

.nav-item:hover {
  background-color: #f7fafc;
  color: var(--primary-color, #409EFF);
}

.nav-item.active {
  background-color: #ebf5ff; /* Light blue bg */
  color: var(--primary-color, #409EFF);
  font-weight: 600;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #cbd5e0;
  margin-right: 10px;
  transition: background 0.3s;
}

.nav-item.active .dot {
  background-color: var(--primary-color, #409EFF);
}

/* Main Content */
.mall-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
}

.content-header {
  padding: 16px 32px;
  background: white;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  justify-content: space-between;
  align-items: center;
  z-index: 10;
}

.search-section {
  width: 400px;
}

.modern-search :deep(.el-input__wrapper) {
  background-color: #f7f8fa;
  box-shadow: none !important;
  border-radius: 8px 0 0 8px;
}

.modern-search :deep(.el-input__wrapper.is-focus) {
  background-color: white;
  box-shadow: 0 0 0 1px var(--primary-color) inset !important;
}

.modern-search :deep(.el-input-group__append) {
  border-radius: 0 8px 8px 0;
  background-color: var(--primary-color, #409EFF);
  color: white;
  border: none;
  box-shadow: none;
}

/* Announcement */
.announcement-wrapper {
  padding: 12px 32px;
}

/* Grid Scroll Area */
.scroll-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px 32px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 24px;
  padding-bottom: 40px;
}

/* Product Card - Modern Look */
.product-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.product-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.08);
}

.img-box {
  width: 100%;
  aspect-ratio: 1;
  position: relative;
  background: #f8fafc;
}

.img-box img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badges {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.badge {
  font-size: 10px;
  padding: 4px 8px;
  border-radius: 6px;
  color: white;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.badge.channel {
  background: rgba(0, 0, 0, 0.6);
}

.badge.stock {
  background: rgba(245, 108, 108, 0.9);
}

.info-box {
  padding: 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.name {
  font-size: 15px;
  margin: 0 0 8px 0;
  line-height: 1.4;
  color: #2d3748;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin-bottom: 12px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-block {
  color: #f56c6c;
  font-weight: 700;
}

.symbol {
  font-size: 12px;
  margin-right: 2px;
}

.amount {
  font-size: 18px;
}

.btns {
  display: flex;
  gap: 6px;
}

.fav-btn.active {
  color: #F56C6C;
  border-color: #F56C6C;
  background-color: #fef0f0;
}

/* Order Dialog Styling */
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