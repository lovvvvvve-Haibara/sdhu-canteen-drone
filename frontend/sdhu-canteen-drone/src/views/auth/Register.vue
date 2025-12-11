<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="title">食堂无人机点餐系统</h2>
      <p class="subtitle">创建新账号</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter.native="onSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="建议使用学号 / 工号"
            clearable
          />
        </el-form-item>

        <el-form-item label="显示名" prop="displayName">
          <el-input
            v-model="form.displayName"
            placeholder="用于展示的昵称"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="用于登录与联系（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="不少于 6 位"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入密码"
          />
        </el-form-item>

        <div class="auth-actions">
          <el-button type="primary" :loading="loading" @click="onSubmit">
            注册
          </el-button>
          <el-button link @click="goLogin">已有账号？去登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  displayName: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '长度在 3 到 64 个字符', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入显示名', trigger: 'blur' },
    { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '长度在 6 到 64 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirm, trigger: 'blur' }
  ]
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({
        username: form.username,
        displayName: form.displayName,
        phone: form.phone || null,
        password: form.password
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } finally {
      loading.value = false
    }
  })
}

const goLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at top left, #ecf5ff, #f5f7fa);
}

.auth-card {
  width: 420px;
  padding: 32px 28px 28px;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.06);
}

.title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  text-align: center;
}

.subtitle {
  margin: 0 0 24px;
  font-size: 13px;
  color: #909399;
  text-align: center;
}

.auth-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
