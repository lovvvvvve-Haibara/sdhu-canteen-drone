<template>
  <div class="page">
    <el-card class="toolbar-card" shadow="never">
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
        </div>

        <div class="right">
          <el-button type="primary" @click="openFoodDialog()">
            新增菜品
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>菜品列表</span>
          <el-select
            v-model="state.categoryFilter"
            placeholder="全部分类"
            clearable
            size="small"
            style="width: 160px"
            @change="loadFoods"
          >
            <el-option
              v-for="cat in state.categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </div>
      </template>

      <el-table
        v-loading="state.loading"
        :data="state.foods"
        size="small"
        stripe
      >
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column
          label="价格"
          width="90"
          :formatter="(row) => '￥' + ((row.priceCent || 0) / 100).toFixed(2)"
        />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column label="上架" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.onShelf"
              @change="(val) => onShelfChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="openFoodDialog(row)">
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              @click="updateStock(row)"
            >
              修改库存
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="state.pageSize"
          :current-page="state.page + 1"
          :total="state.total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- 菜品编辑弹窗 -->
    <el-dialog
      v-model="foodDialog.visible"
      :title="foodDialog.editing ? '编辑菜品' : '新增菜品'"
      width="520px"
    >
      <el-form
        ref="foodFormRef"
        :model="foodDialog.form"
        :rules="foodRules"
        label-width="90px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="foodDialog.form.name" />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="foodDialog.form.categoryId"
            placeholder="请选择分类"
          >
            <el-option
              v-for="cat in state.categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="价格（元）" prop="price">
          <el-input-number
            v-model="foodDialog.form.price"
            :min="0"
            :step="0.5"
            :precision="2"
          />
        </el-form-item>

        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="foodDialog.form.stock"
            :min="0"
            :max="9999"
          />
        </el-form-item>

        <el-form-item label="图片地址">
          <el-input
            v-model="foodDialog.form.imageUrl"
            placeholder="可选"
          />
        </el-form-item>

        <el-form-item label="是否上架">
          <el-switch v-model="foodDialog.form.onShelf" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="foodDialog.visible = false">取 消</el-button>
        <el-button type="primary" :loading="foodDialog.saving" @click="saveFood">
          保 存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCanteens } from '../../api/canteen'
import { getCategories, listFoods, createFood, updateFood, updateFoodShelf, updateFoodStock } from '../../api/menu'

const state = reactive({
  canteens: [],
  selectedCanteenId: null,
  categories: [],
  categoryFilter: null,
  foods: [],
  loading: false,
  page: 0,
  pageSize: 10,
  total: 0
})

const foodDialog = reactive({
  visible: false,
  editing: false,
  saving: false,
  form: {
    id: null,
    categoryId: null,
    name: '',
    price: 0,
    stock: 0,
    imageUrl: '',
    onShelf: true
  }
})

const foodFormRef = ref(null)

const foodRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'change' }]
}

const loadCanteens = async () => {
  const pageData = await listCanteens({
    page: 0,
    size: 50
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
  state.loading = true
  try {
    const pageData = await listFoods(state.selectedCanteenId, {
      categoryId: state.categoryFilter || undefined,
      page: state.page,
      size: state.pageSize
    })
    state.foods = pageData.content || []
    state.total = pageData.totalElements || 0
  } finally {
    state.loading = false
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

const openFoodDialog = (row) => {
  if (!state.selectedCanteenId) {
    ElMessage.warning('请先选择食堂')
    return
  }
  if (row) {
    foodDialog.editing = true
    foodDialog.form.id = row.id
    foodDialog.form.categoryId = row.categoryId
    foodDialog.form.name = row.name
    foodDialog.form.price = (row.priceCent || 0) / 100
    foodDialog.form.stock = row.stock
    foodDialog.form.imageUrl = row.imageUrl
    foodDialog.form.onShelf = row.onShelf
  } else {
    foodDialog.editing = false
    foodDialog.form.id = null
    foodDialog.form.categoryId = null
    foodDialog.form.name = ''
    foodDialog.form.price = 0
    foodDialog.form.stock = 0
    foodDialog.form.imageUrl = ''
    foodDialog.form.onShelf = true
  }
  foodDialog.visible = true
}

const saveFood = () => {
  foodFormRef.value.validate(async (valid) => {
    if (!valid) return
    foodDialog.saving = true
    try {
      const payload = {
        categoryId: foodDialog.form.categoryId,
        name: foodDialog.form.name,
        priceCent: Math.round((foodDialog.form.price || 0) * 100),
        stock: foodDialog.form.stock,
        imageUrl: foodDialog.form.imageUrl,
        onShelf: foodDialog.form.onShelf
      }

      if (foodDialog.editing) {
        await updateFood(state.selectedCanteenId, foodDialog.form.id, payload)
      } else {
        await createFood(state.selectedCanteenId, payload)
      }

      ElMessage.success('保存成功')
      foodDialog.visible = false
      loadFoods()
    } finally {
      foodDialog.saving = false
    }
  })
}

const onShelfChange = async (row, value) => {
  try {
    await updateFoodShelf(state.selectedCanteenId, row.id, value)
    ElMessage.success('状态已更新')
    row.onShelf = value
  } catch {
    // 拦截器会提示
  }
}

const updateStock = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt(
      '请输入新的库存数量',
      '修改库存',
      {
        inputPattern: /^[0-9]+$/,
        inputErrorMessage: '请输入非负整数',
        inputValue: String(row.stock ?? 0)
      }
    )
    const stock = parseInt(value, 10)
    await updateFoodStock(state.selectedCanteenId, row.id, stock)
    ElMessage.success('库存已更新')
    row.stock = stock
  } catch {
    // 取消或错误
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
.page {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-footer {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
