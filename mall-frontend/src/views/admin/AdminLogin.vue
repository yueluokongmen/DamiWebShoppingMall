<template>
    <div class="admin-login-bg">
        <div class="login-card">
            <h2>大米商城后台管理</h2>
            <el-form :model="form" ref="formRef" :rules="rules">
                <el-form-item prop="username">
                    <el-input v-model="form.username" placeholder="账号" size="large">
                        <template #prefix><el-icon>
                                <User />
                            </el-icon></template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
                        <template #prefix><el-icon>
                                <Lock />
                            </el-icon></template>
                    </el-input>
                </el-form-item>
                <el-button type="primary" class="login-btn" size="large" :loading="loading" @click="handleLogin">
                    登录后台
                </el-button>
            </el-form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import request from '../../utils/request'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
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
    if (!form.username || !form.password) return

    loading.value = true
    try {
        //调用后端接口
        const res: any = await request.post('/admin/login', form)
        
        
        userStore.token = res.token 
        
        //存入用户信息
        userStore.userInfo = res.user
        
        //写入localStorage
        localStorage.setItem('token', res.token)

        ElMessage.success('欢迎回来，管理员')
        router.push('/admin/dashboard')

    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.admin-login-bg {
    height: 100vh;
    background: #2d3a4b;
    display: flex;
    justify-content: center;
    align-items: center;
}

.login-card {
    width: 400px;
    background: #fff;
    padding: 40px;
    border-radius: 4px;
    text-align: center;
}

.login-card h2 {
    margin-bottom: 30px;
    color: #333;
}

.login-btn {
    width: 100%;
    margin-top: 20px;
    background-color: #ff6700;
    border-color: #ff6700;
}

.login-btn:hover {
    background-color: #f25807;
    border-color: #f25807;
}
</style>