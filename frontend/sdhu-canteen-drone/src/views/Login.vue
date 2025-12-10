<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">食堂无人机点餐系统</h2>

      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form
            :model="loginForm"
            :rules="loginRules"
            ref="loginFormRef"
            label-width="90px"
          >
            <el-form-item label="账号/手机号" prop="usernameOrPhone">
              <el-input v-model="loginForm.usernameOrPhone" placeholder="用户名或手机号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="密码"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="onLogin">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            :model="registerForm"
            :rules="registerRules"
            ref="registerFormRef"
            label-width="90px"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="登录用户名" />
            </el-form-item>
            <el-form-item label="显示名" prop="displayName">
              <el-input v-model="registerForm.displayName" placeholder="在系统中显示的昵称" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                placeholder="至少 6 位"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="onRegister">
                注册并登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { login, register } from '../api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loading = ref(false)

const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({
  usernameOrPhone: '',
  password: '',
})

const registerForm = reactive({
  username: '',
  displayName: '',
  password: '',
})

const loginRules = {
  usernameOrPhone: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入显示名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

const doAfterLogin = (user) => {
  userStore.setUser(user)
  ElMessage.success('登录成功')
  const redirect = route.query.redirect || '/canteens'
  router.push(redirect)
}

const onLogin = () => {
  if (!loginFormRef.value) return
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      loading.value = true
      const user = await login({
        usernameOrPhone: loginForm.usernameOrPhone,
        password: loginForm.password,
      })
      doAfterLogin(user)
    } catch (e) {
      // 错误已在拦截器或后端 message 中提示
    } finally {
      loading.value = false
    }
  })
}

const onRegister = () => {
  if (!registerFormRef.value) return
  registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      loading.value = true
      const user = await register({
        username: registerForm.username,
        displayName: registerForm.displayName,
        password: registerForm.password,
      })
      doAfterLogin(user)
    } catch (e) {
      // 已提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.login-card {
  width: 420px;
}

.title {
  text-align: center;
  margin-bottom: 20px;
}
</style>
