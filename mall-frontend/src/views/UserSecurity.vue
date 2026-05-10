<template>
    <div class="security-page">
        <div class="site-header">
            <div class="container header-content">
                <h2 class="logo" @click="$router.push('/')">大米商城</h2>
                <div class="header-title">个人中心</div>
                <div class="user-area"><span class="link" @click="$router.push('/')">返回首页</span></div>
            </div>
        </div>

        <div class="container">
            <div class="profile-layout">
                <div class="side-menu">
                    <div class="avatar-box">
                        <img :src="userStore.userInfo.avatar || defaultAvatar" alt="avatar">
                        <h3>{{ userStore.userInfo.nickname || userStore.userInfo.username }}</h3>
                    </div>
                    <ul class="menu-list">
                        <li @click="$router.push('/user')">我的个人中心</li>
                        <li @click="$router.push('/order/list')">我的订单</li>
                        <li @click="$router.push('/user/address')">收货地址</li>
                        <li class="active">账户安全</li>
                    </ul>
                </div>

                <div class="main-content">
                    <div class="content-header">
                        <h3>修改密码</h3>
                    </div>

                    <div class="form-box">
                        <el-form :model="pwdForm" label-width="100px" style="width: 400px">
                            <el-form-item label="原密码">
                                <el-input v-model="pwdForm.oldPassword" type="password" show-password
                                    placeholder="请输入当前密码"></el-input>
                            </el-form-item>
                            <el-form-item label="新密码">
                                <el-input v-model="pwdForm.newPassword" type="password" show-password
                                    placeholder="请输入新密码"></el-input>
                            </el-form-item>
                            <el-form-item label="确认密码">
                                <el-input v-model="pwdForm.confirmPassword" type="password" show-password
                                    placeholder="请再次输入新密码"></el-input>
                            </el-form-item>
                            <el-form-item>
                                <el-button type="primary" @click="handleUpdatePwd" :loading="loading"
                                    class="btn-save">提交修改</el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const loading = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const pwdForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

const handleUpdatePwd = async () => {
    if (!pwdForm.oldPassword || !pwdForm.newPassword) {
        return ElMessage.warning('密码不能为空')
    }
    if (pwdForm.newPassword !== pwdForm.confirmPassword) {
        return ElMessage.warning('两次输入的新密码不一致')
    }
    if (pwdForm.oldPassword === pwdForm.newPassword) {
        return ElMessage.warning('新密码不能与旧密码相同')
    }

    loading.value = true
    try {
        await request.post('/user/updatePassword', {
            userId: userStore.userInfo.userId,
            oldPassword: pwdForm.oldPassword,
            newPassword: pwdForm.newPassword
        })

        ElMessageBox.alert('密码修改成功，请重新登录', '提示', {
            confirmButtonText: '去登录',
            callback: () => {
                userStore.logout()
                router.push('/login')
            },
        })
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}
</script>

<style scoped lang="scss">
.container {
    width: 1226px;
    margin: 0 auto;
}

.site-header {
    border-bottom: 2px solid #ff6700;
    background: #fff;
    height: 100px;
    display: flex;
    align-items: center;

    .header-content {
        display: flex;
        align-items: center;
        height: 100%;
        width: 100%;
    }

    .logo {
        color: #ff6700;
        font-size: 28px;
        cursor: pointer;
        font-weight: bold;
        margin: 0;
    }

    .header-title {
        font-size: 22px;
        color: #333;
        margin-left: 20px;
        font-weight: normal;
    }

    .user-area {
        margin-left: auto;
        font-size: 14px;

        .link {
            cursor: pointer;

            &:hover {
                color: #ff6700
            }
        }
    }
}

.profile-layout {
    display: flex;
    margin-top: 30px;
    padding-bottom: 50px;

    .side-menu {
        width: 234px;
        background: #fff;
        padding: 30px 0;
        margin-right: 14px;

        .avatar-box {
            text-align: center;
            margin-bottom: 30px;

            img {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                border: 2px solid #f5f5f5;
                object-fit: cover;
            }

            h3 {
                font-size: 16px;
                margin-top: 10px;
                color: #333;
            }
        }

        .menu-list {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
                height: 50px;
                line-height: 50px;
                text-align: center;
                cursor: pointer;
                color: #333;

                &:hover {
                    color: #ff6700;
                }

                &.active {
                    color: #ff6700;
                    font-weight: bold;
                }
            }
        }
    }

    .main-content {
        flex: 1;
        background: #fff;
        padding: 30px;

        .content-header {
            margin-bottom: 30px;
            border-bottom: 1px solid #e0e0e0;
            padding-bottom: 20px;

            h3 {
                font-size: 24px;
                font-weight: normal;
                margin: 0;
                color: #333;
            }
        }

        .form-box {
            padding-top: 20px;

            .btn-save {
                background: #ff6700;
                border-color: #ff6700;
                width: 120px;
            }
        }
    }
}
</style>