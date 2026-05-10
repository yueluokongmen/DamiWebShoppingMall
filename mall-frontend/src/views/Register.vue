<template>
    <div class="login-container">
        <div class="login-header">
            <div class="header-content">
                <h1 class="logo" @click="$router.push('/')">大米商城</h1>
                <span class="divider">|</span>
                <span class="title">注册账号</span>
            </div>
        </div>

        <div class="login-body">
            <div class="login-content-wrapper">
                <el-card class="login-card">
                    <div class="card-header">
                        <h3>注册大米账号</h3>
                    </div>

                    <el-form :model="form" :rules="rules" ref="formRef" size="large" class="login-form">
                        <el-form-item prop="username">
                            <el-input v-model="form.username" placeholder="请输入注册账号" />
                        </el-form-item>

                        <el-form-item prop="password">
                            <el-input v-model="form.password" type="password" placeholder="设置密码" show-password />
                        </el-form-item>

                        <el-form-item prop="confirmPassword">
                            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password />
                        </el-form-item>

                        <el-form-item>
                            <el-button type="primary" :loading="loading" @click="handleRegister" class="login-btn">
                                立即注册
                            </el-button>
                        </el-form-item>

                        <div class="form-footer">
                            <span class="text">已有账号？</span>
                            <span class="link-text login-link" @click="$router.push('/login')">直接登录</span>
                        </div>
                    </el-form>
                </el-card>
            </div>
        </div>

        <div class="login-footer">
            <p>大米商城版权所有-京ICP备10046444号</p>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import request from '../utils/request' // 直接用 request 发请求
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)

const form = reactive({
    username: '',
    password: '',
    confirmPassword: ''
})

// 自定义校验规则：检查两次密码是否一致
const validatePass2 = (rule: any, value: any, callback: any) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error('两次输入密码不一致!'))
    } else {
        callback()
    }
}

const rules = {
    username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const formRef = ref()

const handleRegister = async () => {
    //校验表单
    if (!formRef.value) return
    await formRef.value.validate(async (valid: boolean) => {
        if (valid) {
            loading.value = true
            try {
                //送注册请求
                await request.post('/user/register', {
                    username: form.username,
                    password: form.password
                })

                ElMessage.success('注册成功，请登录')
                //跳转去登录页
                router.push('/login')
            } catch (e) {
            } finally {
                loading.value = false
            }
        }
    })
}
</script>

<style scoped lang="scss">
/* 直接复用 Login.vue 的样式，为了节省篇幅，这里只写有差异的部分 */
/* 你可以直接把 Login.vue 的 <style> 块完全复制过来 */

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
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
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
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);

    .card-header {
        margin-bottom: 20px;

        h3 {
            font-size: 22px;
            color: #333;
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

        .login-link {
            color: #ff6700;
            cursor: pointer;
            margin-left: 5px;
            font-weight: bold;
        }
    }
}

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

    .login-header,
    .login-footer {
        display: none;
    }

    .login-card {
        width: 100%;
        margin: 20px;
    }
}
</style>