<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-bar">
        <div>
          <el-input
            v-model="state.keyword"
            placeholder="搜索用户名 / 显示名"
            size="small"
            style="width: 220px"
            @keyup.enter.native="onSearch"
          >
            <template #append>
              <el-button size="small" @click="onSearch">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div>
          <el-select
            v-model="state.role"
            placeholder="全部角色"
            clearable
            size="small"
            style="width: 140px; margin-right: 8px"
            @change="loadUsers"
          >
            <el-option label="顾客" value="CUSTOMER" />
            <el-option label="食堂管理员" value="CANTEEN" />
            <el-option label="系统管理员" value="ADMIN" />
          </el-select>
          <el-select
            v-model="state.status"
            placeholder="全部状态"
            clearable
            size="small"
            style="width: 140px"
            @change="loadUsers"
          >
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </div>
      </div>
    </template>

    <el-table
      v-loading="state.loading"
      :data="state.users"
      size="small"
      stripe
    >
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="displayName" label="显示名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column prop="role" label="角色" width="130">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">
            {{ roleLabel(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag
            size="small"
            :type="row.status === 'ACTIVE' ? 'success' : 'info'"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-dropdown @command="(cmd) => onRoleCommand(row, cmd)">
            <el-button type="text" size="small">
              设置角色
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="CUSTOMER">顾客</el-dropdown-item>
                <el-dropdown-item command="CANTEEN">食堂管理员</el-dropdown-item>
                <el-dropdown-item command="ADMIN">系统管理员</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button
            type="text"
            size="small"
            @click="toggleStatus(row)"
          >
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
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
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserList,
  searchUsers,
  updateUserStatus,
  updateUserRole
} from '../../api/user'

const state = reactive({
  loading: false,
  users: [],
  page: 0,
  pageSize: 10,
  total: 0,
  role: '',
  status: '',
  keyword: ''
})

const loadUsers = async () => {
  state.loading = true
  try {
    let pageData
    if (state.keyword) {
      pageData = await searchUsers({
        keyword: state.keyword,
        page: state.page,
        size: state.pageSize
      })
    } else {
      pageData = await getUserList({
        page: state.page,
        size: state.pageSize,
        role: state.role || undefined,
        status: state.status || undefined
      })
    }
    state.users = pageData.content || []
    state.total = pageData.totalElements || 0
  } finally {
    state.loading = false
  }
}

const onPageChange = (page) => {
  state.page = page - 1
  loadUsers()
}

const onSearch = () => {
  state.page = 0
  loadUsers()
}

const roleLabel = (role) => {
  switch (role) {
    case 'CUSTOMER':
      return '顾客'
    case 'CANTEEN':
      return '食堂管理员'
    case 'ADMIN':
      return '系统管理员'
    default:
      return role
  }
}

const statusLabel = (status) => {
  switch (status) {
    case 'ACTIVE':
      return '正常'
    case 'DISABLED':
      return '禁用'
    default:
      return status
  }
}

const toggleStatus = async (row) => {
  const target = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  try {
    await ElMessageBox.confirm(
      `确定将用户【${row.displayName}】状态设置为 ${statusLabel(target)}？`,
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await updateUserStatus(row.id, target)
  ElMessage.success('状态已更新')
  loadUsers()
}

const onRoleCommand = async (row, role) => {
  try {
    await ElMessageBox.confirm(
      `确定将用户【${row.displayName}】角色设置为 ${roleLabel(role)}？`,
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }
  await updateUserRole(row.id, role)
  ElMessage.success('角色已更新')
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.header-bar {
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
