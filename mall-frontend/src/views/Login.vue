<template>
  <div class="login-container">
    <div class="login-header">
      <div class="header-content">
        <h1 class="logo" @click="$router.push('/')">大米商城</h1>
        <span class="divider">|</span>
        <span class="title">大米账号</span>
      </div>
    </div>

    <div class="login-body">
      <div class="login-content-wrapper">
        <el-card class="login-card">
          <div class="card-header">
            <h3>账号登录</h3>
          </div>
          
          <el-form :model="form" :rules="rules" ref="formRef" size="large" class="login-form">
            <el-form-item prop="username">
              <el-input 
                v-model="form.username" 
                placeholder="大米ID" 
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input 
                v-model="form.password" 
                type="password" 
                placeholder="密码" 
                show-password 
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
                登录
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <span class="link-text register" @click="$router.push('/register')">立即注册</span>
              <span class="separator">|</span>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>

    <div class="login-footer">
      <p>大米商城版权所有-粤ICP备XXXXXXXXXX号</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if(!form.username || !form.password) {
      ElMessage.warning('请输入账号和密码')
      return
  }
  loading.value = true
  const success = await userStore.login(form)
  loading.value = false

  if (success) {
    ElMessage.success('登录成功')
    router.push('/')
  }
}
</script>

<style scoped lang="scss">

.login-container {
  min-height: 100vh;
  width: 100%; 
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.login-header {
  height: 90px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  z-index: 10;
  
  .header-content {
    width: 1100px;
    margin: 0 auto;
    height: 100%;
    display: flex;
    align-items: center;

    .logo {
      color: #ff6700;
      font-size: 28px;
      font-weight: bold;
      margin: 0;
      cursor: pointer;
    }
    
    .divider {
      margin: 0 15px;
      color: #ccc;
      font-size: 24px;
    }
    
    .title {
      font-size: 20px;
      color: #333;
    }
  }
}

.login-body {
  flex: 1;
  background: url('https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=2070&auto=format&fit=crop') no-repeat center center;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-content-wrapper {
  width: 1100px;
  display: flex;
  justify-content: flex-end;
}

.login-card {
  width: 410px;
  border: none;
  border-radius: 4px;
  background: #fff; 
  padding: 20px 30px; 
  box-shadow: 0 20px 50px rgba(0,0,0,0.1); 

  .card-header {
    margin-bottom: 30px;
    h3 {
      font-size: 22px;
      color: #ff6700;
      margin: 0;
      text-align: center; 
    }
  }

  .login-btn {
    width: 100%;
    height: 50px;
    font-size: 16px;
    background-color: #ff6700;
    border-color: #ff6700;
    margin-top: 10px;
    
    &:hover {
      background-color: #f25807;
    }
  }

  .form-footer {
    display: flex;
    justify-content: center; 
    margin-top: 20px;
    font-size: 14px;
    color: #999;
    
    .link-text {
      cursor: pointer;
      &:hover { color: #ff6700; }
    }
    
    .register {
      color: #ff6700; 
    }
    
    .separator {
      margin: 0 10px;
      color: #eee;
    }
  }
}

//底部版权
.login-footer {
  height: 80px;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #999;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .login-header .header-content,
  .login-content-wrapper {
    width: 100%;
    padding: 0 40px;
  }
}

@media (max-width: 768px) {
  .login-content-wrapper {
    justify-content: center;
  }
  .login-header, .login-footer {
    display: none;
  }
  .login-card {
    width: 100%;
    margin: 20px;
  }
}
</style>