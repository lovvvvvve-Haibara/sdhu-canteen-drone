<template>
  <div class="user-home">
    <!-- 顶部筛选区域 -->
    <el-card class="toolbar-card">
      <div class="toolbar">
        <div class="left">
          <el-select
            v-model="state.selectedCanteenId"
            placeholder="选择食堂"
            style="min-width: 220px"
            @change="onCanteenChange"
          >
            <el-option
              v-for="c in state.canteens"
              :key="c.id"
              :label="c.name + '（' + (c.location || '') + '）'"
              :value="c.id"
            />
          </el-select>

          <el-select
            v-model="state.selectedCategoryId"
            placeholder="全部分类"
            clearable
            style="min-width: 160px; margin-left: 12px"
            @change="loadFoods"
          >
            <el-option
              v-for="cat in state.categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>

          <el-input
            v-model="state.keyword"
            placeholder="搜索菜品名称"
            clearable
            style="width: 220px; margin-left: 12px"
            @clear="loadFoods"
            @keyup.enter.native="loadFoods"
          >
            <template #append>
              <el-button @click="loadFoods">搜索</el-button>
            </template>
          </el-input>
        </div>

        <div class="right">
          <el-radio-group v-model="state.deliveryMethod" size="small">
            <el-radio-button label="DRONE">无人机配送</el-radio-button>
            <el-radio-button label="MANUAL">人工配送</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <!-- 菜品展示 + 购物车布局 -->
    <div class="content">
      <div class="foods-panel">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>今日菜品</span>
            </div>
          </template>

          <el-skeleton v-if="state.loadingFoods" :rows="6" animated />

          <div v-else class="food-list">
            <el-empty
              v-if="state.foods.length === 0"
              description="暂无菜品"
            />
            <div v-else class="food-grid">
              <el-card
                v-for="food in state.foods"
                :key="food.id"
                class="food-card"
                shadow="hover"
              >
                <div class="food-image">
                  <img
                    v-if="food.imageUrl"
                    :src="food.imageUrl"
                    alt=""
                  />
                  <div v-else class="image-placeholder">
                    {{ food.name?.[0] || '菜' }}
                  </div>
                </div>
                <div class="food-info">
                  <div class="name-row">
                    <span class="name">{{ food.name }}</span>
                    <el-tag
                      v-if="food.categoryName"
                      size="small"
                      effect="plain"
                    >
                      {{ food.categoryName }}
                    </el-tag>
                  </div>
                  <div class="price-row">
                    <span class="price">
                      ￥{{ (food.priceCent || 0) / 100 }}
                    </span>
                    <span class="stock">库存：{{ food.stock ?? '-' }}</span>
                  </div>
                </div>
                <div class="food-actions">
                  <el-input-number
                    v-model="foodQtyMap[food.id]"
                    :min="1"
                    :max="food.stock || 99"
                    size="small"
                  />
                  <el-button
                    type="primary"
                    size="small"
                    @click="addToCart(food)"
                  >
                    加入购物车
                  </el-button>
                </div>
              </el-card>
            </div>

            <div class="pagination">
              <el-pagination
                background
                layout="prev, pager, next"
                :page-size="state.pageSize"
                :current-page="state.page + 1"
                :total="state.totalFoods"
                @current-change="onPageChange"
              />
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧购物车 -->
      <div class="cart-panel">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>购物车</span>
            </div>
          </template>

          <el-empty
            v-if="cartItems.length === 0"
            description="购物车是空的"
          />

          <div v-else class="cart-list">
            <div
              v-for="item in cartItems"
              :key="item.foodId"
              class="cart-item"
            >
              <div class="cart-item-main">
                <div class="title">{{ item.name }}</div>
                <div class="meta">
                  ￥{{ item.price }} x {{ item.qty }}
                </div>
              </div>
              <div class="cart-item-actions">
                <el-input-number
                  v-model="item.qty"
                  :min="1"
                  :max="item.stock || 99"
                  size="small"
                  @change="syncCartFromItems"
                />
                <el-button
                  type="text"
                  size="small"
                  @click="removeFromCart(item.foodId)"
                >
                  移除
                </el-button>
              </div>
            </div>
          </div>

          <div class="cart-footer" v-if="cartItems.length > 0">
            <div class="summary">
              <div>合计：<span class="sum-price">￥{{ totalAmount }}</span></div>
            </div>
            <el-input
              v-model="state.deliveryAddress"
              placeholder="配送地址（宿舍/房间号）"
              style="margin-bottom: 8px"
            />
            <el-input
              v-model="state.remarks"
              placeholder="备注（例如：不要香菜）"
              type="textarea"
              :rows="2"
              style="margin-bottom: 8px"
            />
            <el-button
              type="primary"
              :loading="state.submitting"
              style="width: 100%"
              @click="submitOrder"
            >
              提交订单
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser } from '../../utils/auth'
import { listCanteens } from '../../api/canteen'
import { getCategories, listFoods } from '../../api/menu'
import { createOrder } from '../../api/order'

const user = getCurrentUser()

const state = reactive({
  canteens: [],
  selectedCanteenId: null,
  categories: [],
  selectedCategoryId: null,
  keyword: '',
  page: 0,
  pageSize: 8,
  totalFoods: 0,
  foods: [],
  loadingFoods: false,
  deliveryMethod: 'DRONE',
  deliveryAddress: '',
  remarks: '',
  submitting: false
})

// foodId -> 默认数量
const foodQtyMap = reactive({})

// 购物车数组结构：[{ foodId, name, price, qty, stock }]
const cartItems = ref([])

const totalAmount = computed(() => {
  // 单价传入的是元
  return cartItems.value
    .reduce((sum, item) => sum + item.price * item.qty, 0)
    .toFixed(2)
})

const loadCanteens = async () => {
  const pageData = await listCanteens({
    page: 0,
    size: 20,
    status: 'OPEN'
  })
  
  state.canteens = pageData.content || []
 
  if (state.canteens.length > 0 && !state.selectedCanteenId) {
    state.selectedCanteenId = state.canteens[0].id
  }
}

const loadCategories = async () => {
  if (!state.selectedCanteenId) return
  const res = await getCategories(state.selectedCanteenId)
  state.categories = res.categories || []
}

const loadFoods = async () => {
  if (!state.selectedCanteenId) return
  state.loadingFoods = true
  try {
    const pageData = await listFoods(state.selectedCanteenId, {
      categoryId: state.selectedCategoryId || undefined,
      shelfStatus: 'ON',
      keyword: state.keyword || undefined,
      page: state.page,
      size: state.pageSize
    })
    console.log("after-food", pageData)
    state.foods = pageData.content || []
    state.totalFoods = pageData.totalElements || 0
    // 初始化数量
    state.foods.forEach((f) => {
      if (!foodQtyMap[f.id]) {
        foodQtyMap[f.id] = 1
      }
    })
  } finally {
    state.loadingFoods = false
  }

}

const onCanteenChange = async () => {
  await loadCategories()
  state.page = 0
  await loadFoods()
}

const onPageChange = (page) => {
  state.page = page - 1
  loadFoods()
}

const addToCart = (food) => {
  const qty = foodQtyMap[food.id] || 1
  const price = (food.priceCent || 0) / 100
  const exist = cartItems.value.find((i) => i.foodId === food.id)
  if (exist) {
    exist.qty += qty
  } else {
    cartItems.value.push({
      foodId: food.id,
      name: food.name,
      price,
      qty,
      stock: food.stock
    })
  }
  ElMessage.success('已加入购物车')
}

const removeFromCart = (foodId) => {
  cartItems.value = cartItems.value.filter((i) => i.foodId !== foodId)
}

const syncCartFromItems = () => {
  // 这里只需要触发视图更新即可，数组已被双向绑定
}

const submitOrder = async () => {
  if (!user) {
    ElMessage.error('请先登录')
    return
  }
  if (!state.selectedCanteenId) {
    ElMessage.warning('请选择食堂')
    return
  }
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  if (!state.deliveryAddress) {
    ElMessage.warning('请填写配送地址')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认提交订单？共 ${cartItems.value.length} 项，金额 ￥${totalAmount.value}`,
      '确认提交',
      {
        type: 'warning'
      }
    )
  } catch {
    return
  }

  state.submitting = true
  try {
    const items = cartItems.value.map((i) => ({
      foodId: i.foodId,
      qty: i.qty
    }))

    await createOrder({
      customerId: user.id,
      canteenId: state.selectedCanteenId,
      deliveryMethod: state.deliveryMethod,
      deliveryAddress: state.deliveryAddress,
      remarks: state.remarks,
      items
    })

    ElMessage.success('下单成功')
    cartItems.value = []
    state.remarks = ''
  } finally {
    state.submitting = false
  }
}

onMounted(async () => {
  await loadCanteens()
  if (state.selectedCanteenId) {
    await loadCategories()
    await loadFoods()
  }
})
</script>

<style scoped>
.user-home {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toolbar-card {
  border-radius: 12px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left,
.right {
  display: flex;
  align-items: center;
}

.content {
  display: grid;
  grid-template-columns: 3fr 1.2fr;
  gap: 16px;
}

.food-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
}

.food-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.food-image {
  height: 120px;
  border-radius: 10px;
  overflow: hidden;
  background: #f2f3f5;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.food-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  font-size: 32px;
  font-weight: 600;
  color: #909399;
}

.food-info {
  flex: 1;
}

.name-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.name {
  font-weight: 600;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 13px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.stock {
  color: #909399;
}

.food-actions {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.cart-panel {
  min-width: 280px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px dashed #ebeef5;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-main .title {
  font-size: 13px;
  font-weight: 500;
}

.cart-item-main .meta {
  font-size: 12px;
  color: #909399;
}

.cart-footer {
  margin-top: 12px;
}

.summary {
  margin-bottom: 8px;
}

.sum-price {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
