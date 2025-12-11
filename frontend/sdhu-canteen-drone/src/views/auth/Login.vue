<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="title">食堂无人机点餐系统</h2>
      <p class="subtitle">欢迎登录</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter.native="onSubmit"
      >
        <el-form-item label="账号 / 手机号" prop="usernameOrPhone">
          <el-input
            v-model="form.usernameOrPhone"
            placeholder="请输入账号或手机号"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>

        <div class="auth-actions">
          <el-button type="primary" :loading="loading" @click="onSubmit">
            登录
          </el-button>
          <el-button link @click="goRegister">没有账号？去注册</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { setCurrentUser, setToken } from '../../utils/auth'

const router = useRouter()
const route = useRoute()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  usernameOrPhone: '',
  password: ''
})

// 表单校验规则
const rules = {
  usernameOrPhone: [
    { required: true, message: '请输入账号或手机号', trigger: 'blur' },
    { min: 3, max: 64, message: '长度在 3 到 64 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '长度在 6 到 64 个字符', trigger: 'blur' }
  ]
}

const onSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const user = await login({
        usernameOrPhone: form.usernameOrPhone,
        password: form.password
      })

      // user: SelfUser.UserDetail，后续如果后端加 token，可以一起返回并保存
      setCurrentUser(user)
      // 如果后端未来在 data 中增加 token 字段，这里顺便保存
      if (user.token) {
        setToken(user.token)
      } else {
        // 当前项目暂未真正使用 token，可留空
        setToken('')
      }

      ElMessage.success('登录成功')

      const redirect = route.query.redirect
      if (redirect) {
        router.push(String(redirect))
        return
      }

      if (user.role === 'CUSTOMER') {
        router.push('/user/home')
      } else {
        router.push('/admin/dashboard')
      }
    } catch (e) {
      // 错误已由拦截器提示，这里不再重复
    } finally {
      loading.value = false
    }
  })
}

const goRegister = () => {
  router.push('/register')
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
  width: 380px;
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
